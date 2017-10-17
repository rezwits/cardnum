(in-ns 'game.core)

(declare ice-index parse-command show-error-toast)

(defn say
  "Prints a message to the log as coming from the given username. The special user string
  __system__ shows no user name."
  [state side {:keys [user text]}]
  (let [author (or user (get-in @state [side :user]))
        text (if (= (.trim text) "null") " null" text)]
    (if-let [command (parse-command text)]
      (when (and (not= side nil) (not= side :spectator))
        (command state side)
        (swap! state update-in [:log] #(conj % {:user nil :text (str "[!]" (:username author) " uses a command: " text)})))
      (swap! state update-in [:log] #(conj % {:user author :text text})))
    (swap! state assoc :typing (remove #{(:username author)} (:typing @state)))))

(defn typing
  "Updates game state list with username of whoever is typing"
  [state side {:keys [user]}]
  (let [author (:username (or user (get-in @state [side :user])))]
    (swap! state assoc :typing (distinct (conj (:typing @state) author)))
    ;; say something to force update in client side rendering
    (say state side {:user "__system__" :text "typing"})))

(defn typingstop
  "Clears typing flag from game state for user"
  [state side {:keys [user text]}]
  (let [author (or user (get-in @state [side :user]))]
    (swap! state assoc :typing (remove #{(:username author)} (:typing @state)))
    ;; say something to force update in client side rendering
    (say state side {:user "__system__" :text "typing"})))

(defn system-msg
  "Prints a message to the log without a username."
  ([state side text] (system-msg state side text nil))
  ([state side text {:keys [hr]}]
   (let [username (get-in @state [side :user :username])]
     (say state side {:user "__system__" :text (str username " " text "." (when hr "[hr]"))}))))

(defn enforce-msg
  "Prints a message related to a rules enforcement on a given card.
  Example: 'Architect cannot be trashed while installed.'"
  [state card text]
  (say state nil {:user (get-in card [:title]) :text (str (:title card) " " text ".")}))

(defn toast
  "Adds a message to toast with specified severity (default as a warning) to the toast msg list.
  If message is nil, removes first toast in the list.
  For options see http://codeseven.github.io/toastr/demo.html
  Currently implemented options:
    - type (warning, info etc)
    - time-out (sets both timeOut and extendedTimeOut currently)
    - close-button
    - prevent-duplicates"
  ([state side msg] (toast state side msg "warning" nil))
  ([state side msg type] (toast state side msg type nil))
  ([state side msg type options]
   ;; Allows passing just the toast type as the options parameter
   (if msg
     ;; normal toast - add to list
     (swap! state update-in [side :toast] #(conj % {:msg msg :type type :options options}))
     ;; no msg - remove top toast from list
     (swap! state update-in [side :toast] #(rest %)))))

(defn play-sfx
  "Adds a sound effect to play to the sfx queue.
  Each SFX comes with a unique ID, so each client can track for themselves which sounds have already been played.
  The sfx queue has size limited to 3 to limit the sound torrent tabbed out or lagged players will experience."
  [state side sfx]
  (when-let [current-id (get-in @state [:sfx-current-id])]
    (do
      (swap! state update-in [:sfx] #(take 3 (conj % {:id (inc current-id) :name sfx})))
      (swap! state update-in [:sfx-current-id] #(inc %)))))

;;; "ToString"-like methods
(defn card-str
  "Gets a string description of an installed card, reflecting whether it is rezzed,
  in/protecting a server, facedown, or hosted."
  ([state card] (card-str state card nil))
  ([state card {:keys [visible] :as args}]
  (str (if (card-is? card :side :corp)
         ; Corp card messages
         (str (if (or (rezzed? card) visible) (:title card) (if (ice? card) "ICE" "a card"))
              ; Hosted cards do not need "in server 1" messages, host has them
              (if-not (:host card)
                (str (if (ice? card) " protecting " " in ")
                     ;TODO add naming of scoring area of corp/runner
                     (zone->name (second (:zone card)))
                     (if (ice? card) (str " at position " (ice-index state card))))))
         ; Runner card messages
         (if (or (:facedown card) visible) "a facedown card" (:title card)))
       (if (:host card) (str " hosted on " (card-str state (:host card)))))))

(defn name-zone
  "Gets a string representation for the given zone."
  [side zone]
  (match (vec zone)
         [:hand] (if (= side "Runner") "Grip" "HQ")
         [:discard] (if (= side "Runner") "Heap" "Archives")
         [:deck] (if (= side "Runner") "Stack" "R&D")
         [:rig _] "Rig"
         [:servers :hq _] "the root of HQ"
         [:servers :rd _] "the root of R&D"
         [:servers :archives _] "the root of Archives"
         :else (zone->name (second zone))))


;;; In-game chat commands
(defn set-adv-counter [state side target value]
  (set-prop state side target :advance-counter value)
  (system-msg state side (str "sets advancement counters to " value " on "
                              (card-str state target)))
  (trigger-event state side :advancement-placed target))

(defn command-adv-counter [state side value]
  (resolve-ability state side
                   {:effect (effect (set-adv-counter target value))
                    :choices {:req (fn [t] (card-is? t :side side))}}
                   {:title "/adv-counter command"} nil))

(defn command-counter-smart [state side args]
  (resolve-ability
    state side
    {:effect (req (let [existing (:counter target)
                        value (if-let [n (string->num (first args))] n 0)
                        c-type (cond (= 1 (count existing)) (first (keys existing))
                                     (can-be-advanced? target) :advance-counter
                                     (and (is-type? target "Agenda") (is-scored? target)) :agenda
                                     (and (card-is? target :side :runner) (has-subtype? target "Virus")) :virus)
                        advance (= :advance-counter c-type)]
                    (cond advance (set-adv-counter state side target value)
                          (not c-type) (toast state side "You need to specify a counter type for that card." "error"
                                              {:time-out 0 :close-button true})
                          :else (do (set-prop state side target :counter (merge (:counter target) {c-type value}))
                                    (system-msg state side (str "sets " (name c-type) " counters to " value " on "
                                                                (card-str state target)))))))
     :choices {:req (fn [t] (card-is? t :side side))}}
    {:title "/counter command"} nil))

(defn command-counter [state side args]
  (if (= 1 (count args))
    (command-counter-smart state side args)
    (let [typestr (.toLowerCase (first args))
          value (if-let [n (string->num (second args))] n 0)
          one-letter (if (<= 1 (.length typestr)) (.substring typestr 0 1) "")
          two-letter (if (<= 2 (.length typestr)) (.substring typestr 0 2) one-letter)
          c-type (cond (= "v" one-letter) :virus
                       (= "p" one-letter) :power
                       (= "c" one-letter) :credit
                       (= "ag" two-letter) :agenda
                       :else :advance-counter)
          advance (= :advance-counter c-type)]
      (if advance
        (command-adv-counter state side value)
        (resolve-ability state side
                       {:effect (effect (set-prop target :counter (merge (:counter target) {c-type value}))
                                        (system-msg (str "sets " (name c-type) " counters to " value " on "
                                                         (card-str state target))))
                        :choices {:req (fn [t] (card-is? t :side side))}}
                       {:title "/counter command"} nil)))))

(defn command-rezall [state side value]
  (resolve-ability state side
    {:optional {:prompt "Rez all cards and turn cards in archives faceup?"
                :yes-ability {:effect (req
                                        (swap! state update-in [:corp :discard] #(map (fn [c] (assoc c :seen true)) %))
                                        (doseq [c (all-installed state side)]
                                          (when-not (:rezzed c)
                                            (rez state side c {:ignore-cost :all-costs :force true}))))}}}
    {:title "/rez-all command"} nil))

(defn command-roll [state side value]
  (system-msg state side (str "rolls a " value " sided die and rolls a " (inc (rand-int value)))))

(defn command-close-prompt [state side]
  (when-let [fprompt (-> @state side :prompt first)]
    (swap! state update-in [side :prompt] rest)
    (effect-completed state side (:eid fprompt))))

(defn parse-command [text]
  (let [[command & args] (split text #" ");"
        value (if-let [n (string->num (first args))] n 1)
        num   (if-let [n (-> args first (safe-split #"#") second string->num)] (dec n) 0)]
    (when (<= (count args) 2)
      (if (= (first (first args)) \#)
        (case command
          "/deck"       #(move %1 %2 (nth (get-in @%1 [%2 :hand]) num nil) :deck {:front true})
          "/discard"    #(move %1 %2 (nth (get-in @%1 [%2 :hand]) num nil) :discard)
          nil)
        (case command
          "/adv-counter" #(command-adv-counter %1 %2 value)
          "/bp"         #(swap! %1 assoc-in [%2 :bad-publicity] (max 0 value))
          "/card-info"  #(resolve-ability %1 %2
                                          {:effect (effect (system-msg (str "shows card-info of "
                                                                            (card-str state target)
                                                                            ": " (get-card state target))))
                                           :choices {:req (fn [t] (card-is? t :side %2))}}
                                          {:title "/card-info command"} nil)
          "/clear-win"  #(clear-win %1 %2)
          "/click"      #(swap! %1 assoc-in [%2 :click] (max 0 value))
          "/close-prompt" #(command-close-prompt %1 %2)
          "/counter"    #(command-counter %1 %2 args)
          "/credit"     #(swap! %1 assoc-in [%2 :credit] (max 0 value))
          "/deck"       #(toast %1 %2 "/deck number takes the format #n")
          "/discard"    #(toast %1 %2 "/discard number takes the format #n")
          "/discard-random" #(move %1 %2 (rand-nth (get-in @%1 [%2 :hand])) :discard)
          "/draw"       #(draw %1 %2 (max 0 value))
          "/end-run"    #(when (= %2 :corp) (end-run %1 %2))
          "/error"      #(show-error-toast %1 %2)
          "/handsize"   #(swap! %1 assoc-in [%2 :hand-size-modification] (- (max 0 value) (:hand-size-base %2)))
          "/jack-out"   #(when (= %2 :runner) (jack-out %1 %2 nil))
          "/link"       #(swap! %1 assoc-in [%2 :link] (max 0 value))
          "/memory"     #(swap! %1 assoc-in [%2 :memory] value)
          "/move-bottom"  #(resolve-ability %1 %2
                                            {:prompt "Select a card in hand to put on the bottom of your deck"
                                             :effect (effect (move target :deck))
                                             :choices {:req (fn [t] (and (card-is? t :side %2) (in-hand? t)))}}
                                            {:title "/move-bottom command"} nil)
          "/move-hand"  #(resolve-ability %1 %2
                                          {:prompt "Select a card to move to your hand"
                                           :effect (req (let [c (deactivate %1 %2 target)]
                                                          (move %1 %2 c :hand)))
                                           :choices {:req (fn [t] (card-is? t :side %2))}}
                                          {:title "/move-hand command"} nil)
          "/psi"        #(when (= %2 :corp) (psi-game %1 %2
                                                      {:title "/psi command" :side %2}
                                                      {:equal  {:msg "resolve equal bets effect"}
                                                       :not-equal {:msg "resolve unequal bets effect"}}))
          "/rez"        #(when (= %2 :corp)
                           (resolve-ability %1 %2
                                            {:effect (effect (rez target {:ignore-cost :all-costs :force true}))
                                             :choices {:req (fn [t] (card-is? t :side %2))}}
                                            {:title "/rez command"} nil))
          "/rez-all"    #(when (= %2 :corp) (command-rezall %1 %2 value))
          "/rfg"        #(resolve-ability %1 %2
                                          {:prompt "Select a card to remove from the game"
                                           :effect (req (let [c (deactivate %1 %2 target)]
                                                          (move %1 %2 c :rfg)))
                                           :choices {:req (fn [t] (card-is? t :side %2))}}
                                          {:title "/rfg command"} nil)
          "/roll"       #(command-roll %1 %2 value)
          "/tag"        #(swap! %1 assoc-in [%2 :tag] (max 0 value))
          "/take-brain" #(when (= %2 :runner) (damage %1 %2 :brain (max 0 value)))
          "/take-meat"  #(when (= %2 :runner) (damage %1 %2 :meat  (max 0 value)))
          "/take-net"   #(when (= %2 :runner) (damage %1 %2 :net   (max 0 value)))
          "/trace"      #(when (= %2 :corp) (corp-trace-prompt %1
                                                               {:title "/trace command" :side %2}
                                                               {:base (max 0 value)
                                                                :msg "resolve successful trace effect"}))
          nil)))))

(defn corp-install-msg
  "Gets a message describing where a card has been installed from. Example: Interns. "
  [card]
  (str "install " (if (:seen card) (:title card) "an unseen card") " from " (name-zone :corp (:zone card))))

(defn turn-message
  "Prints a message for the start or end of a turn, summarizing credits and cards in hand."
  [state side start-of-turn]
  (let [pre (if start-of-turn "started" "is ending")
        hand (if (= side :runner) "their Grip" "HQ")
        cards (count (get-in @state [side :hand]))
        credits (get-in @state [side :credit])
        text (str pre " their turn " (:turn @state) " with " credits " [Credit] and " cards " cards in " hand)]
    (system-msg state side text {:hr (not start-of-turn)})))

(defn event-title
  "Gets a string describing the internal engine event keyword"
  [event]
  (if (keyword? event)
    (name event)
    (str event)))

(defn show-error-toast
  [state side]
  (when state
    (toast state side
           (str "Your last action caused a game error on the server. You can keep playing, but there "
                "may be errors in the game's current state. Please click the button below to submit a report "
                "to our GitHub issues page.<br/><br/>Use /error to see this message again.")
           "exception"
           {:time-out 0 :close-button true})))

