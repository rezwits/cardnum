(in-ns 'game.core)

(declare character-txt-index parse-command show-error-toast swap-character)

(defn say
  "Prints a message to the log as coming from the given username. The special user string
  __system__ shows no user name."
  [state side {:keys [user text]}]
  (let [author (or user (get-in @state [side :user]))
        text (if (= (.trim text) "null") " null" text)]
    (if-let [command (parse-command text)]
      (when (and (not= side nil) (not= side :spectator))
        (command state side)
        (when-not (or (= text "/z") (= text "/o"))
          (swap! state update-in [:log] #(conj % {:user nil :text (str "[!]" (:username author) " uses a command: " text)}))
          )
        )
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

(defn system-say
  "Prints a system message to log (`say` from user __system__)"
  ([state side text] (system-say state side text nil))
  ([state side text {:keys [hr]}]
   (say state side {:user "__system__" :text (str text (when hr "[hr]"))})))

(defn system-msg
  "Prints a message to the log without a username."
  ([state side text] (system-msg state side text nil))
  ([state side text args]
   (let [username (get-in @state [side :user :username])]
     (system-say state side (str username " " text ".") args))))

(defn enforce-msg
  "Prints a message related to a rules enforcement on a given card.
  Example: 'Architect cannot be discarded while placed.'"
  [state card text]
  (say state nil {:user (get-in card [:title]) :text (str (:title card) " " text ".")}))

(defn toast
  "Adds a message to toast with specified severity (default as a warning) to the toast message list.
  If message is nil, removes first toast in the list.
  For options see http://codeseven.github.io/toastr/demo.html
  Currently implemented options:
    - msg-type (warning, info etc)
    - time-out (sets both timeOut and extendedTimeOut currently)
    - close-button
    - prevent-duplicates"
  ([state side message] (toast state side message "warning" nil))
  ([state side message msg-type] (toast state side message msg-type nil))
  ([state side message msg-type options]
   ;; Allows passing just the toast msg-type as the options parameter
   (if message
     ;; normal toast - add to list
     (swap! state update-in [side :toast] #(conj % {:msg message :type msg-type :options options}))
     ;; no message - remove top toast from list
     (swap! state update-in [side :toast] rest))))

(defn play-sfx
  "Adds a sound effect to play to the sfx queue.
  Each SFX comes with a unique ID, so each client can track for themselves which sounds have already been played.
  The sfx queue has size limited to 3 to limit the sound torrent tabbed out or lagged players will experience."
  [state side sfx]
  (when-let [current-id (get-in @state [:sfx-current-id])]
    (do
      (swap! state update-in [:sfx] #(take 1 (conj % {:id (inc current-id) :name sfx})))
      (swap! state update-in [:sfx-current-id] inc))))

;;; "ToString"-like methods
(defn card-str
  "Gets a string description of an placed card, reflecting whether it is revealed,
  in/protecting a locale, facedown, or hosted."
  ([state card] (card-str state card nil))
  ([state card {:keys [visible] :as args}]
  (str (if (card-is? card :side :contestant)
         ; Contestant card messages
         (str (if (or (revealed? card) visible) (:title card) (if (character? card) "Character" "a card"))
              ; Hosted cards do not need "in locale 1" messages, host has them
              (if-not (:host card)
                (str (if (character? card) " in " " for ")
                     ;TODO add naming of scoring area of contestant/challenger
                     (zone->name (second (:zone card)))
                     (if (character? card) (str " at position " (character-txt-index state card))))))
         ; Challenger card messages
         (if (or (:facedown card) visible) "a facedown card" (:title card)))
       (if (:host card) (str " hosted on " (card-str state (:host card)))))))

(defn card-sb
  "Gets a string description of an placed card, reflecting whether it is revealed,
  in/protecting a locale, facedown, or hosted."
  [card]
  (str (:type card)))

(defn name-zone
  "Gets a string representation for the given zone."
  [side zone]
  (match (vec zone)
         [:hand] "Hand"
         [:discard] "Discard"
         [:deck] "Play Deck"
         [:sideboard] "Sideboard"
         [:fw-dc-sb] "fw-dc-sb"
         [:location] "Location Deck"
         [:rig _] "in play"
         [:locales :hq _] "the root of Hand"
         [:locales :rd _] "the root of Deck"
         [:locales :archives _] "the root of Discard"
         [:locales :sites _] "the root of Location"
         :else (zone->name (second zone))))

;;; In-game chat commands
(defn set-adv-counter [state side target value]
  (set-prop state side target :advance-counter value)
  (system-msg state side (str "sets light blue counters to " value " on "
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
                        counter-type (cond (= 1 (count existing)) (first (keys existing))
                                     (can-be-advanced? target) :advance-counter
                                     (and (is-type? target "Agenda") (is-scored? state side target)) :agenda
                                     (and (card-is? target :side :challenger) (has-subtype? target "Virus")) :virus)
                        counter-name (cond (= :virus counter-type) "red"
                                           (= :power counter-type) "purple"
                                           (= :credit counter-type) "black"
                                           (= :agenda counter-type) "dark blue"
                                           :else "light blue")
                        advance (= :advance-counter counter-type)]
                    (cond
                      advance
                      (set-adv-counter state side target value)

                      (not counter-type)
                      (toast state side
                             (str "Could not infer what counter type you mean. Please specify one manually, by typing "
                                  "'/counter TYPE " value "', where TYPE is light [l]/dark [d] blue, black [b], purple [p], or red [r].")
                             "error" {:time-out 0 :close-button true})

                      :else
                      (do (set-prop state side target :counter (merge (:counter target) {counter-type value}))
                          (system-msg state side (str "sets " counter-name " counters to " value " on "
                                                      (card-str state target)))))))
     :choices {:req (fn [t] (card-is? t :side side))}}
    {:title "/counter command"} nil))

(defn command-facedown [state side]
  (resolve-ability state side
                   {:prompt "Select a card to place facedown"
                    :choices {:req #(and (in-hand? %))}
                    :effect (effect (challenger-place target {:facedown true}))}
                   {:title "/facedown command"} nil))

(defn command-counter [state side args]
  (cond
    (empty? args)
    (command-counter-smart state side `("1"))

    (= 1 (count args))
    (command-counter-smart state side args)

    :else
    (let [typestr (.toLowerCase (first args))
          value (if-let [n (string->num (second args))] n 1)
          one-letter (if (<= 1 (.length typestr)) (.substring typestr 0 1) "")
          two-letter (if (<= 2 (.length typestr)) (.substring typestr 0 2) one-letter)
          counter-type (cond (= "r" one-letter) :virus
                             (= "p" one-letter) :power
                             (= "b" one-letter) :credit
                             (= "d" one-letter) :agenda
                             :else :advance-counter)
          counter-name (cond (= "r" one-letter) "red"
                             (= "p" one-letter) "purple"
                             (= "b" one-letter) "black"
                             (= "d" one-letter) "dark blue"
                             :else "light blue")
          advance (= :advance-counter counter-type)]
      (if advance
        (command-adv-counter state side value)
        (resolve-ability state side
                       {:effect (effect (set-prop target :counter (merge (:counter target) {counter-type value}))
                                        (system-msg (str "sets " counter-name " counters to " value " on "
                                                         (card-str state target))))
                        :choices {:req (fn [t] (card-is? t :side side))}}
                       {:title "/counter command"} nil)))))

(defn command-revealall [state side value]
  (resolve-ability state side
                   {:optional {:prompt "Reveal all cards and turn cards in discard faceup?"
                               :yes-ability {:effect (req
                                                       (swap! state update-in [:contestant :discard] #(map (fn [c] (assoc c :seen true)) %))
                                                       (doseq [c (all-placed state side)]
                                                         (when-not (:revealed c)
                                                           (reveal state side c {:ignore-cost :all-costs :force true}))))}}}
                   {:title "/reveal-all command"} nil))

(defn command-revtop [state side value]
  (resolve-ability state side
                   {:effect (req
                              (let [prior (last (get-in @state [side :discard]))]
                                (reveal state side prior {:ignore-cost :all-costs :force true})
                                ;;(update! state side (assoc prior :seen true))
                                ))}
                   {:title "/revtop command"} nil))

(defn command-roll [state side value]
  (system-msg state side (str "rolls a " value " sided die and rolls " (inc (rand-int value)))))

(defn basic-roll [state side]
  (let [player (side @state)
        pick1 (get-in @state [side :deck-dice])
        size1 (get-in @state [side :deck-mmsz])
        pick2 (get-in player [:user :options :dice-pick])
        size2 (get-in player [:user :options :dice-size])]
    (if (and (not (or (= pick1 "empty") (= size1 "none")))
             pick1 size1)
      (system-msg state side (str "rolls " pick1"-"(inc (rand-int 6))"+"size1
                                  " " pick1"-"(inc (rand-int 6))"+"size1))
        (if (and pick2 size2)
          (system-msg state side (str "rolls " pick2"-"(inc (rand-int 6))"+"size2
                                      " " pick2"-"(inc (rand-int 6))"+"size2))
          (system-msg state side (str "rolls " "roll-"(inc (rand-int 6))"+16mm"
                                      " roll-"(inc (rand-int 6))"+16mm"))))))

(defn command-undo-click
  "Resets the game state back to start of the click"
  [state side]
  (when-let [click-state (:click-state @state)]
    (when (= (:active-player @state) side)
      (reset! state (dissoc (assoc click-state :log (:log @state) :click-state click-state) :run))
      (doseq [s [:challenger :contestant]]
        (toast state s "Game reset to start of click")))))

(defn command-undo-turn
  "Resets the entire game state to how it was at end-of-turn if both players agree"
  [state side]
  (when-let [turn-state (:turn-state @state)]
    (swap! state assoc-in [side :undo-turn] true)
    (when (and (-> @state :challenger :undo-turn) (-> @state :contestant :undo-turn))
      (reset! state (assoc turn-state :log (:log @state) :turn-state turn-state))
      (doseq [s [:challenger :contestant]]
        (toast state s "Game reset to start of turn")))))

(defn command-close-prompt [state side]
  (when-let [fprompt (-> @state side :prompt first)]
    (swap! state update-in [side :prompt] rest)
    (swap! state dissoc-in [side :selected])
    (effect-completed state side (:eid fprompt))))

(defn blind-zoom
  [state side args]
  (if (get-in @state [side :blind])
    (swap! state assoc-in [side :blind] false)
    (swap! state assoc-in [side :blind] true)))

(defn blind-send
  [state side args]
  (if-let [msg (:msg args)]
    (system-msg state side msg))
  (if (get-in @state [side :hold-card])
    (swap! state assoc-in [side :hold-card] false)
    (swap! state assoc-in [side :hold-card] true)))

(defn bonus-key-down
  [state side args]
  (if-let [msg (:msg args)]
    (system-msg state side msg))
  (if (get-in @state [side :bonus-key])
    (swap! state assoc-in [side :bonus-key] false)
    (swap! state assoc-in [side :bonus-key] true)))

(defn minus-key-down
  [state side args]
  (if-let [msg (:msg args)]
    (system-msg state side msg))
  (if (get-in @state [side :minus-key])
    (swap! state assoc-in [side :minus-key] false)
    (swap! state assoc-in [side :minus-key] true)))

(defn option-key-down
  [state side args]
  (if-let [msg (:msg args)]
    (system-msg state side msg))
  (if (get-in @state [side :opt-key])
    (swap! state assoc-in [side :opt-key] false)
    (swap! state assoc-in [side :opt-key] true)))

(defn f1-f12-key-down
  [state side args]
  (if-let [msg (:msg args)]
    (system-msg state side msg))
  (if (get-in @state [side :fn12-key])
    (swap! state assoc-in [side :fn12-key] false)
    (do
      (discard-radicle state side args)
      (swap! state assoc-in [side :fn12-key] true))))

(defn talk-bool
  [state side args]
  (if (get-in @state [side :talk])
    (do (swap! state assoc-in [side :talk] false)
        (system-msg state side "Talk some is off"))
    (do (swap! state assoc-in [side :talk] true)
        (system-msg state side "Talk some is on"))))

(defn tall-bool
  [state side args]
  (if (get-in @state [side :tall])
    (do (swap! state assoc-in [side :tall] false)
        (system-msg state side "Tell all is off"))
    (do (swap! state assoc-in [side :tall] true)
        (system-msg state side "Tell all is on"))))

(defn tell-bool
  [state side args]
  (if (get-in @state [side :tell])
    (do (swap! state assoc-in [side :tell] false)
        (system-msg state side "Tell more is off"))
    (do (swap! state assoc-in [side :tell] true)
        (system-msg state side "Tell more is on"))))

(defn host-any-card
  [state side args]
  (resolve-ability state side
                   {:prompt "Select host character/card"
                    :choices {:req (fn [t] true)}
                    :msg (msg "host " (:title target))
                    :effect (req (let [c (get-card state target)] (resolve-ability state side
                                                                                   {:prompt "Select card to transfer to host"
                                                                                    :choices {:req (fn [t] true)}
                                                                                    :effect (effect (host c (get-card state target)))
                                                                                    ; host target onto card
                                                                                    } c nil )))} nil nil)
  (system-msg state side "host a card"))

(defn host-any-card-hidden
  [state side args]
  (resolve-ability state side
                   {:prompt "Select host character/card"
                    :choices {:req (fn [t] true)}
                    :msg (msg "host " (:title target))
                    :effect (req (let [c (get-card state target)] (resolve-ability state side
                                                                                   {:prompt "Select card to transfer to host"
                                                                                    :choices {:req (fn [t] true)}
                                                                                    :effect (effect (host-hidden c (get-card state target)))
                                                                                    ; host target onto card
                                                                                    } c nil )))} nil nil)
  (system-msg state side "host a card hidden"))

(defn host-any-card-reveal
  [state side args]
  (resolve-ability state side
                   {:prompt "Select host character/card"
                    :choices {:req (fn [t] true)}
                    :effect (req (let [c (get-card state target)] (resolve-ability state side
                                                                                   {:prompt "Select card to transfer to host"
                                                                                    :choices {:req (fn [t] true)}
                                                                                    :effect (effect (host-reveal c (get-card state target)))
                                                                                    :msg (msg "host " (:title (get-card state target)))
                                                                                    ; host target onto card
                                                                                    } (if (revealed? target) target nil) nil )))} nil nil)
  (system-msg state side "host a card revealed"))

(defn hero-pal-flag
  [state side args]
  (if (get-in @state [side :hpf])
    (do (swap! state assoc-in [side :hpf] false)
        (system-msg state side "New discards hidden"))
    (do (swap! state assoc-in [side :hpf] true)
        (system-msg state side "Discards revealed"))))

(defn escher-char
  [state side args]
  (resolve-ability state side
                   (letfn [(msr [] {:prompt "Select two Characters to swap"
                                    :choices {:req #(and (not (:host %))
                                                         (placed? %)
                                                         (character? %)
                                                         (= (:side %) (if (= side :contestant)
                                                                        "Contestant"
                                                                        "Challenger")))
                                              :max 2}
                                    :delayed-completion true
                                    :effect (req (if (= (count targets) 2)
                                                   (do (swap-character state side (first targets) (second targets))
                                                       (system-msg state side
                                                                   (str "swaps the position of two characters "))
                                                       (continue-ability state side (msr) card nil))
                                                   (do (system-msg state side (str "has finished rearranging Characters"))
                                                       (effect-completed state side eid))))})]
                     {:delayed-completion true
                      :msg "rearrange any number of Characters"
                      :effect (effect (continue-ability (msr) card nil))})nil nil))

(defn escher-hazard
  [state side args]
  (resolve-ability state side
                   (letfn [(msr [] {:prompt "Select two Hazards to swap"
                                    :choices {:req #(and (not (:host %))
                                                         (placed? %)
                                                         (hazard? %)
                                                         (= (:side %) (if (= side :contestant)
                                                                        "Contestant"
                                                                        "Challenger")))
                                              :max 2}
                                    :delayed-completion true
                                    :effect (req (if (= (count targets) 2)
                                                   (do (swap-character state side (first targets) (second targets))
                                                       (system-msg state side
                                                                   (str "swaps the position of "
                                                                        (card-str state (first targets))
                                                                        " and "
                                                                        (card-str state (second targets))))
                                                       (continue-ability state side (msr) card nil))
                                                   (do (system-msg state side (str "has finished rearranging Hazards"))
                                                       (effect-completed state side eid))))})]
                     {:delayed-completion true
                      :msg "rearrange any number of Characters"
                      :effect (effect (continue-ability (msr) card nil))})nil nil))

(defn escher-resource
  [state side args]
  (resolve-ability state side
                   (letfn [(msr [] {:prompt "Select two Resources to swap"
                                    :choices {:req #(and (not (:host %))
                                                         (placed? %)
                                                         (resource? %)
                                                         (= (:side %) (if (= side :contestant)
                                                                        "Contestant"
                                                                        "Challenger")))
                                              :max 2}
                                    :delayed-completion true
                                    :effect (req (if (= (count targets) 2)
                                                   (do (swap-character state side (first targets) (second targets))
                                                       (system-msg state side
                                                                   (str "swaps the position of "
                                                                        (card-str state (first targets))
                                                                        " and "
                                                                        (card-str state (second targets))))
                                                       (continue-ability state side (msr) card nil))
                                                   (do (system-msg state side (str "has finished rearranging Resources"))
                                                       (effect-completed state side eid))))})]
                     {:delayed-completion true
                      :msg "rearrange any number of Characters"
                      :effect (effect (continue-ability (msr) card nil))})nil nil))

(defn escher-site
  [state side args]
  (resolve-ability state side
                   (letfn [(msr [] {:prompt "Select two Sites to swap"
                                    :choices {:req #(and (not (:host %))
                                                         (placed? %)
                                                         (or (region? %) (site? %))
                                                         (= (:side %) (if (= side :contestant)
                                                                        "Contestant"
                                                                        "Challenger")))
                                              :max 2}
                                    :delayed-completion true
                                    :effect (req (if (= (count targets) 2)
                                                   (do (swap-character state side (first targets) (second targets))
                                                       (system-msg state side
                                                                   (str "swaps the position of "
                                                                        (card-str state (first targets))
                                                                        " and "
                                                                        (card-str state (second targets))))
                                                       (continue-ability state side (msr) card nil))
                                                   (do (system-msg state side (str "has finished rearranging Sites"))
                                                       (effect-completed state side eid))))})]
                     {:delayed-completion true
                      :msg "rearrange any number of Sites"
                      :effect (effect (continue-ability (msr) card nil))})nil nil))

(defn reveal-hand-step
  [state side args]
  (resolve-ability state side
                   (letfn [(msr [] {:prompt "Reveal card from hand"
                                    :choices {:req (fn [t] (card-is? t :side side))}
                                    :delayed-completion true
                                    :effect (req (do (move state side target :play-area)
                                                       (system-msg state side
                                                                   (str "reveals from his hand "
                                                                        (:ImageName target)))
                                                       (continue-ability state side (msr) card nil))
                                              )})]
                     {:delayed-completion true
                      :msg "show any number of cards"
                      :effect (effect (continue-ability (msr) card nil))})nil nil))

(defn reveal-hand-random
  [state side args]
  (resolve-ability state side
                   {:effect (req
                              (let [value (if-let [n (string->num (first args))] n 0)
                                    kount (count (get-in @state [side :hand]))]
                                (loop [k (if (<= kount value) kount value)]
                                  (when (> k 0)
                                    (move state side (rand-nth (get-in @state [side :hand])) :play-area)
                                    (recur (- k 1))))
                                (resolve-ability state side
                                                 {:delayed-completion true
                                                  :player  side
                                                  :prompt  "Return to hand"
                                                  :choices ["Done"]
                                                  :effect  (req (case target
                                                                  "Done"
                                                                  (loop [k (count (get-in @state [side :play-area]))]
                                                                    (when (> k 0)
                                                                      (move state side (first (get-in @state [side :play-area])) :hand {:front true})
                                                                      (recur (- k 1)))))
                                                                (effect-completed state side nil)
                                                                (system-msg state side (str "revealed " value " card(s) from his hand"))
                                                                )} nil nil)))}
                   {:title "/reveal-hand-random command"} nil))

(defn reveal-deck-hidden
  [state side args]
  (resolve-ability state side
                   {:effect (req
                              (let [value (if-let [n (string->num (first args))] n 0)
                                    opp-side (if (= side :contestant)
                                               :challenger
                                               :contestant)
                                    kount (count (get-in @state [opp-side :deck]))]
                                (loop [k (if (< kount value) kount value)]
                                  (when (> k 0)
                                    (move state opp-side (assoc (first (get-in @state [side :deck])) :swap true) :current)
                                    (recur (- k 1))))
                                (resolve-ability state side
                                                 {:delayed-completion true
                                                  :player  opp-side
                                                  :prompt  "Click done when ready"
                                                  :choices ["Done"]
                                                  :effect  (req (case target
                                                                  "Done"
                                                                  (let [new-opp-side (if (= side :contestant)
                                                                                       :challenger
                                                                                       :contestant)]
                                                                    (loop [k (count (get-in @state [side :current]))]
                                                                    (when (> k 0)
                                                                      (move state new-opp-side (dissoc (rand-nth (get-in @state [side :current])) :swap) :deck {:front true})
                                                                      (recur (- k 1))))))
                                                                (effect-completed state side nil)
                                                                (system-msg state side (str " looked at " value " card(s) from opponents deck"))
                                                                )} nil nil)))}
                   {:title "/reveal-deck-hidden command"} nil))

(defn show-deciding
  [state side args]
  (resolve-ability state side
                   {:effect (req (resolve-ability state side
                                                  {:effect (req
                                                             (let [value (if-let [n (string->num (first args))] n 0)
                                                                   opp-side (if (= side :contestant)
                                                                              :challenger
                                                                              :contestant)
                                                                   the-side (if (= 0 (count (get-in @state [side :current])))
                                                                              opp-side
                                                                              side)
                                                                   kount (count (get-in @state [the-side :current]))]
                                                               (if (and (> value 0) (<= value kount))
                                                                 (move state the-side (nth (get-in @state [the-side :current]) (- value 1)) :play-area))
                                                               ))} nil nil))}
                   {:title "/show-deciding command"} nil))

(defn hide-temp-zone
  [state side]
  (resolve-ability state side
                   {:effect (req (resolve-ability state side
                                                  {;:delayed-completion true
                                                   :effect  (req
                                                              (let [opp-side (if (= side :contestant)
                                                                               :challenger
                                                                               :contestant)
                                                                    the-side (if (= 0 (count (get-in @state [side :play-area])))
                                                                               opp-side
                                                                               side)
                                                                    kount (count (get-in @state [the-side :play-area]))]
                                                                (loop [k kount]
                                                                  (when (> k 0)
                                                                    (move state the-side (first (get-in @state [the-side :play-area])) :current)
                                                                    (recur (- k 1)))))
                                                              )} nil nil))}
                   {:title "/hide-temp-zone command"} nil))

(defn reveal-deck-show
  [state side args]
  (resolve-ability state side
                   {:effect (req
                              (let [value (if-let [n (string->num (first args))] n 0)
                                    kount (count (get-in @state [side :deck]))]
                                (loop [k (if (<= kount value) kount value)]
                                  (when (> k 0)
                                    (move state side (first (get-in @state [side :deck])) :play-area)
                                    (recur (- k 1))))
                                (resolve-ability state side
                                                 {:delayed-completion true
                                                  :player  side
                                                  :prompt  "Shuffle back on top"
                                                  :choices ["Done"]
                                                  :effect  (req (case target
                                                                  "Done"
                                                                  (loop [k (count (get-in @state [side :play-area]))]
                                                                    (when (> k 0)
                                                                      (move state side (rand-nth (get-in @state [side :play-area])) :deck {:front true})
                                                                      (recur (- k 1)))))
                                                                (effect-completed state side nil)
                                                                (system-msg state side (str "revealed " value " card(s) from the top of his deck"))
                                                                )} nil nil)))}
                   {:title "/reveal-deck-show command"} nil))

(defn reveal-deck-self
  [state side args]
  (resolve-ability state side
                   {:effect (req
                              (let [value (if-let [n (string->num (first args))] n 0)
                                    kount (count (get-in @state [side :deck]))]
                                (loop [k (if (<= kount value) kount value)]
                                  (when (> k 0)
                                    (move state side (first (get-in @state [side :deck])) :current)
                                    (recur (- k 1))))
                                (resolve-ability state side
                                                 {:delayed-completion true
                                                  :player  side
                                                  :prompt  "Shuffle back on top"
                                                  :choices ["Done"]
                                                  :effect  (req (case target
                                                                  "Done"
                                                                  (loop [k (count (get-in @state [side :current]))]
                                                                    (when (> k 0)
                                                                      (move state side (rand-nth (get-in @state [side :current])) :deck {:front true})
                                                                      (recur (- k 1)))))
                                                                (effect-completed state side nil)
                                                                (system-msg state side (str "looked at " value " card(s) from the top of his deck"))
                                                                )} nil nil)))}
                   {:title "/reveal-deck-self command"} nil))

(defn starter-path [state side st-path]
  (let [
        b-path1 (.replace st-path " b " " Border-land ")
        b-path2 (.replace b-path1 " b " " Border-land ")
        b-path3 (.replace b-path2 " b" " Border-land ")
        c-path1 (.replace b-path3 " c " " Coastal Sea ")
        c-path2 (.replace c-path1 " c " " Coastal Sea ")
        c-path3 (.replace c-path2 " c" " Coastal Sea ")
        d-path1 (.replace c-path3 " d " " Dark-domain ")
        d-path2 (.replace d-path1 " d " " Dark-domain ")
        d-path3 (.replace d-path2 " d" " Dark-domain ")
        e-path1 (.replace d-path3 " e " " Desert ")
        e-path2 (.replace e-path1 " e " " Desert ")
        e-path3 (.replace e-path2 " e" " Desert ")
        f-path1 (.replace e-path3 " f " " Free-domain ")
        f-path2 (.replace f-path1 " f " " Free-domain ")
        f-path3 (.replace f-path2 " f" " Free-domain ")
        j-path1 (.replace f-path3 " j " " Jungle ")
        j-path2 (.replace j-path1 " j " " Jungle ")
        j-path3 (.replace j-path2 " j" " Jungle ")
        s-path1 (.replace j-path3 " s " " Shadow-land ")
        s-path2 (.replace s-path1 " s " " Shadow-land ")
        s-path3 (.replace s-path2 " s" " Shadow-land ")
        w-path1 (.replace s-path3 " w " " Wilderness ")
        w-path2 (.replace w-path1 " w " " Wilderness ")
        w-path3 (.replace w-path2 " w" " Wilderness ")
        ]
    (system-msg state side w-path3)))

(defn parse-command [text]
  (let [[command & args] (split text #" ");"
        value (if-let [n (string->num (first args))] n 1)
        num   (if-let [n (-> args first (safe-split #"#") second string->num)] (dec n) 0)]
    (when (<= (count args) 2)
      (if (= (ffirst args) \#)
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
          "/clear-log"  clear-log
          "/clear-win"  clear-win
          "/click"      #(swap! %1 assoc-in [%2 :click] (max 0 value))
          "/close-prompt" command-close-prompt
          "/counter"    #(command-counter %1 %2 args)
          "/counters"   #(command-counter %1 %2 args)
          "/credit"     #(swap! %1 assoc-in [%2 :credit] (max 0 value))
          "/deck"       #(toast %1 %2 "/deck number takes the format #n")
          "/discard"    #(resolve-ability %1 %2
                                          {:prompt "Select a card to discard"
                                           :effect (req (let [tell (faceup? target)
                                                              hpf (get-in @%1 [(other-side %2) :hpf])
                                                              c (deactivate %1 %2 target)]
                                                          (move %1 %2 c :discard)
                                                          (when hpf (command-revtop %1 %2 nil))
                                                          (if tell
                                                            (system-msg %1 %2 (str " discarded " name)))
                                                            (system-msg %1 %2 (str " discarded a card"))
                                                          ))
                                           :choices {:req (fn [t] (card-is? t :side %2))}}
                                          {:title "/discard command"} nil)
          "/discard-n"    #(toast %1 %2 "/discard number takes the format #n")
          "/discard-random" #(move %1 %2 (rand-nth (get-in @%1 [%2 :hand])) :discard)
          "/draw"       #(draw %1 %2 (max 0 value))
          "/end-run"    #(when (= %2 :contestant) (end-run %1 %2))
          "/error"      show-error-toast
              ;"/handsize"   #(swap! %1 assoc-in [%2 :hand-size-modification] (- (max 0 value) (:hand-size-base %2)))
          "/facedown"   #(command-facedown %1 %2)
          "/hide"  #(resolve-ability %1 %2
                                        {:prompt "Select a card to hide"
                                         :effect (req (let [c (deactivate %1 %2 target)]
                                                        (hide %1 %2 c)))
                                         :choices {:req (fn [t] (card-is? t :side %2))}}
                                        {:title "/hide command"} nil)
          "/hide-hand"   #(hide-hand %1 %2)
          "/host"       #(host-any-card %1 %2 args)
          "/h"          #(host-any-card %1 %2 args)
          "/hh"         #(host-any-card-hidden %1 %2 args)
          "/pal"        #(hero-pal-flag %1 %2 args)
          "/hr"         #(host-any-card-reveal %1 %2 args)
          "/jack-out"   #(when (= %2 :challenger) (jack-out %1 %2 nil))
          "/link"       #(swap! %1 assoc-in [%2 :link] (max 0 value))
          "/memory"     #(swap! %1 assoc-in [%2 :memory] value)
          "/move-bottom"  #(resolve-ability %1 %2
                                            {:prompt "Select a card in hand to put on the bottom of your deck"
                                             :effect (effect (move %1 %2 target :deck))
                                             :choices {:req (fn [t] (and (card-is? t :side %2) (in-hand? t)))}}
                                            {:title "/move-bottom command"} nil)
          "/move-deck"   #(resolve-ability %1 %2
                                           {:prompt "Select a card to move to the top of your deck"
                                            :effect (req (let [c (deactivate %1 %2 target)]
                                                           (move %1 %2 c :deck {:front true})))
                                            :choices {:req (fn [t] (card-is? t :side %2))}}
                                           {:title "/move-deck command"} nil)
          "/move-hand"  #(resolve-ability %1 %2
                                          {:prompt "Select a card to move to your hand"
                                           :effect (req (let [c (deactivate %1 %2 target)]
                                                          (move %1 %2 c :hand)))
                                           :choices {:req (fn [t] (card-is? t :side %2))}}
                                          {:title "/move-hand command"} nil)
          "/move-site"  #(resolve-ability %1 %2
                                          {:prompt "Select a site to move to your Location Deck"
                                           :effect (req (let [c (deactivate %1 %2 target)]
                                                          (move %1 %2 c :location)))
                                           :choices {:req (fn [t] (card-is? t :side %2))}}
                                          {:title "/move-site command"} nil)
          "/move-sb"  #(resolve-ability %1 %2
                                          {:prompt "Select a site to move to your sideboard"
                                           :effect (req (let [c (deactivate %1 %2 target)]
                                                          (move %1 %2 c :sideboard)))
                                           :choices {:req (fn [t] (card-is? t :side %2))}}
                                          {:title "/move-sb command"} nil)
          "/move-fw-sb"  #(resolve-ability %1 %2
                                        {:prompt "Select a site to move to your FW-Sideboard"
                                         :effect (req (let [c (deactivate %1 %2 target)]
                                                        (move %1 %2 c :fw-dc-sb)))
                                         :choices {:req (fn [t] (card-is? t :side %2))}}
                                        {:title "/move-fw-sb command"} nil)
          "/o"          #(option-key-down %1 %2 nil)
          "/p"          #(resolve-ability %1 %2
                                          {:prompt "Select a site for starter movement"
                                           :effect (req (starter-path %1 %2 (:Path target)))
                                           :choices {:req (fn [t] (and (card-is? t :side %2)
                                                                       (= "Site" (:type t))
                                                                       (or  (= "Hero" (:alignment t))
                                                                            (= "Minion" (:alignment t)))))}}
                                          {:title "/p command"} nil)
          "/re-deck"    #(resolve-ability %1 %2
                                          {:effect (req (doseq [c (get-in @%1 [%2 :discard])]
                                                          (if (= (:type c) "Site")
                                                            (move %1 %2 c :location)
                                                            (move %1 %2 c :deck)))
                                                        (shuffle! %1 %2 :deck))
                                           } {:title "/re-deck command"} nil)
          "/psi"        #(when (= %2 :contestant) (psi-game %1 %2
                                                      {:title "/psi command" :side %2}
                                                      {:equal  {:msg "resolve equal bets effect"}
                                                       :not-equal {:msg "resolve unequal bets effect"}}))
          "/re-order"      #(re-order %1 %2 value)
          "/reveal-hand"   #(reveal-hand %1 %2)
          "/reveal-hand-step"   #(reveal-hand-step %1 %2 args)
          "/reveal-hand-random"   #(reveal-hand-random %1 %2 args)
          "/reveal-deck-hidden"   #(reveal-deck-hidden %1 %2 args)
          "/show-deciding"   #(show-deciding %1 %2 args)
          "/hide-temp-zone"   #(hide-temp-zone %1 %2)
          "/reveal-deck-show"   #(reveal-deck-show %1 %2 args)
          "/reveal-deck-self"   #(reveal-deck-self %1 %2 args)
          "/reveal"        #(resolve-ability %1 %2
                                            {:effect (effect (reveal target {:ignore-cost :all-costs :force true}))
                                             :choices {:req (fn [t] (card-is? t :side %2))}}
                                            {:title "/reveal command"} nil)
          "/reveal-all"    #(when (= %2 :contestant) (command-revealall %1 %2 value))
          "/revtop"     #(command-revtop %1 %2 value)
          "/rfg"        #(resolve-ability %1 %2
                                          {:prompt "Select a card to remove from the game"
                                           :effect (req (let [c (deactivate %1 %2 target)]
                                                          (move %1 %2 c :rfg)))
                                           :choices {:req (fn [t] (card-is? t :side %2))}}
                                          {:title "/rfg command"} nil)
          "/rfgh"       #(resolve-ability %1 %2
                                          {:prompt "Select a card to rfg facedown"
                                           :effect (req (move %1 %2 (assoc target :hide true) :rfg))
                                           :choices {:req (fn [t] (card-is? t :side %2))}}
                                          {:title "/rfgh command"} nil)
          "/score"      #(resolve-ability %1 %2
                                          {:prompt "Select a card to score"
                                           :effect (req (let [c  target]
                                                          (move %1 %2 c :scored)))
                                           :choices {:req (fn [t] true)}}
                                          {:title "/score command"} nil)
          "/store"      #(resolve-ability %1 %2
                                          {:prompt "Select a card to store"
                                           :effect (req (let [c  target]
                                                          (move %1 %2 c :scored)))
                                           :choices {:req (fn [t] true)}}
                                          {:title "/store command"} nil)
          "/r"          #(basic-roll %1 %2)
          "/roll"       #(command-roll %1 %2 value)
;          "/swap"       #(escher-char %1 %2 args)
          "/swapc"      #(escher-char %1 %2 args)
          "/swaph"      #(escher-hazard %1 %2 args)
          "/swapr"      #(escher-resource %1 %2 args)
          "/swaps"      #(escher-site %1 %2 args)
          "/tag"        #(swap! %1 assoc-in [%2 :tag] (max 0 value))
          "/talk"       #(talk-bool %1 %2 nil)
          "/tall"       #(tall-bool %1 %2 nil)
          "/tell"       #(tell-bool %1 %2 nil)
          "/trace"      #(when (= %2 :contestant) (init-trace %1 %2
                                                        {:title "/trace command" :side %2}
                                                        {:base (max 0 value)
                                                         :msg "resolve successful trace effect"}))
          "/undo-click" #(command-undo-click %1 %2)
          "/undo-turn"  #(command-undo-turn %1 %2)
          "/z"          #(blind-zoom %1 %2 nil)
          nil)))))

(defn contestant-place-msg
  "Gets a message describing where a card has been placed from. Example: Interns. "
  [card]
  (str "place " (if (:seen card) (:title card) "an unseen card") " from " (name-zone :contestant (:zone card))))

(defn turn-message
  "Prints a message for the start or end of a turn, summarizing credits and cards in hand."
  [state side start-of-turn]
  (let [pre (if start-of-turn "started" "is ending")
        hand "Hand"
        cards (count (get-in @state [side :hand]))
        credits (get-in @state [side :credit])
        text (str pre " turn " (:turn @state) " with " cards " cards")]
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
           (str "Your last action caused a game error on the locale. You can keep playing, but there "
                "may be errors in the game's current state. Please click the button below to submit a report "
                "to our GitHub issues page.<br/><br/>Use /error to see this message again.")
           "exception"
           {:time-out 0 :close-button true})))
