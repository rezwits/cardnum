(ns meccg.gameboard
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [chan put! <!] :as async]
            [clojure.string :refer [capitalize includes? join lower-case split]]
            [differ.core :as differ]
            [cardnum.utils :refer [str->int]]
            [cardnum.cards :refer [all-cards]]
            [meccg.appstate :refer [app-state]]
            [meccg.auth :refer [avatar] :as auth]
            [meccg.cardbrowser :refer [add-symbols] :as cb]
            [meccg.dice :refer [add-faces-16mm add-faces-18mm create-face]]
            [meccg.dreamcard :refer [dreamcard-map-north dreamcard-map-west dreamcard-map-central dreamcard-map-south]]
            [meccg.standard :refer [standard-map]]
            [meccg.utils :refer [toastr-options influence-dot map-longest]]
            [meccg.ws :as ws]
            [om.core :as om :include-macros true]
            [om.dom :as dom]
            [sablono.core :as sab :include-macros true]))

(defonce game-state (atom {}))
(defonce last-state (atom {}))
(defonce lock (atom false))

(defn image-url [{:keys [set_code ImageName dreamcard flip erratum errata] :as card}]
  (if flip
    (str "/img/cards/" (:set_code card) "/flip-" (:ImageName card))
    (if dreamcard
      (str "/img/cards/" (:set_code card) "/" (:ImageName card))
      (if (and erratum (get-in @game-state [:options :use-dce]))
        (str "/img/cards/" (:set_code card) "/dce-" (:ImageName card))
        (if (and errata (get-in @game-state [:options :use-ice]))
          (str "/img/cards/" (:set_code card) "/ice-" (:ImageName card))
          (let [language (get-in @app-state [:options :language])]
            (case language
              "Dutch"
              (str "/img/cards/" (:set_code card) "/" (:ImageName card))
              "English"
              (str "/img/cards/" (:set_code card) "/" (:ImageName card))
              "Español"
              (str "/img/cards/" (:set_code card) "_ES/" (:ImageName card))
              "Finnish"
              (str "/img/cards/" (:set_code card) "/" (:ImageName card))
              "French"
              (str "/img/cards/" (:set_code card) "/" (:ImageName card))
              "German"
              (str "/img/cards/" (:set_code card) "/" (:ImageName card))
              "Italian"
              (str "/img/cards/" (:set_code card) "/" (:ImageName card))
              "Japanese"
              (str "/img/cards/" (:set_code card) "/" (:ImageName card))
              :default
              (str "/img/cards/" (:set_code card) "/" (:ImageName card)))))))))

(defn site?
  [{:keys [type] :as card}]
  (or (= type "Site") (= type "Region")))

(defn get-side [state]
  (let [user-id (:_id (:user @app-state))]
    (cond
      (= (get-in state [:challenger :user :_id]) user-id) :challenger
      (= (get-in state [:contestant :user :_id]) user-id) :contestant
      :else :spectator)))

(defn not-spectator? []
  (not= :spectator (get-side @game-state)))

(defn init-game [state]
  (let [side (get-side state)]
    (.setItem js/localStorage "gameid" (:gameid @app-state))
    (reset! game-state state)
    (swap! game-state assoc :side side)
    (reset! last-state @game-state)
    (reset! lock false)))

(defn launch-game [{:keys [state]}]
  (init-game state)
  (set! (.-onbeforeunload js/window) #(clj->js "Leaving this page will disconnect you from the game."))
  (-> "#gamelobby" js/$ .fadeOut)
  (-> "#gameboard" js/$ .fadeIn))

(declare toast)

(defn notify
  "Send a notification to the chat, and a toast to the current player of the specified severity"
  [text severity]
  (swap! game-state update-in [:log] #(conj % {:user "__system__" :text text}))
  (toast text severity nil))

(def zoom-channel (chan))

(defn check-lock?
  "Check if we can clear client lock based on action-id"
  []
  (let [aid [(:side @game-state) :aid]]
    (when (not= (get-in @game-state aid)
                (get-in @last-state aid))
      (reset! lock false))))

(defn handle-state [{:keys [state]}] (init-game state))

(defn handle-diff [{:keys [gameid diff]}]
  (when (= gameid (:gameid @game-state))
    (swap! game-state #(differ/patch @last-state diff))
    (check-lock?)
    (reset! last-state @game-state)))

(defn handle-timeout [{:keys [gameid]}]
  (when (= gameid (:gameid @game-state))
    (toast "Game closed due to inactivity" "error" {:time-out 0 :close-button true})))

(defn parse-state [state]
  (js->clj (.parse js/JSON state) :keywordize-keys true))

(ws/register-ws-handler! :meccg/state #(handle-state (parse-state %)))
(ws/register-ws-handler! :meccg/start #(launch-game (parse-state %)))
(ws/register-ws-handler! :meccg/diff #(handle-diff (parse-state %)))
(ws/register-ws-handler! :meccg/timeout #(handle-timeout (parse-state %)))
(ws/register-ws-handler! :meccg/relay #(swap! app-state assoc :save-pref (:save %)
                                              :resumed false))

(defn send-command
  ([command] (send-command command nil))
  ([command {:keys [no-lock] :as args}]
   (when (or (not @lock) no-lock)
     (try (js/ga "send" "event" "game" command) (catch js/Error e))
     (when-not no-lock (reset! lock true))
     (ws/ws-send! [:meccg/action {:gameid-str (:gameid @game-state) :command command :args args}]))))

(defn send-msg [event owner]
  (.preventDefault event)
  (let [input (om/get-node owner "msg-input")
        text (.-value input)
        $div (js/$ ".gameboard .messages")]
    (when-not (empty? text)
      (ws/ws-send! [:meccg/say {:gameid-str (:gameid @game-state) :msg text}])
      (.scrollTop $div (+ (.prop $div "scrollHeight") 500))
      (aset input "value" "")
      (.focus input))))

(defn send-typing [event owner]
  "Send a typing event to server for this user if it is not already set in game state"
  (.preventDefault event)
  (let [input (om/get-node owner "msg-input")
        text (.-value input)]
    (if (empty? text)
      (ws/ws-send! [:meccg/typing {:gameid-str (:gameid @game-state) :typing false}])
      (when (not-any? #{(get-in @app-state [:user :username])} (:typing @game-state))
        (ws/ws-send! [:meccg/typing {:gameid-str (:gameid @game-state) :typing true}])))))

(defn mute-spectators [mute-state]
  (ws/ws-send! [:meccg/mute-spectators {:gameid-str (:gameid @game-state) :mute-state mute-state}]))

(defn save-game []
  (do (send-command "system-msg" {:msg "just SAVED the GAME"})
  (ws/ws-send! [:meccg/save {:gameid-str (:gameid @game-state)
                              :save-pref (:save-pref @game-state)}])))

(defn auto-save-send
  ([command] (auto-save-send command nil))
  ([command {:keys [no-lock] :as args}]
   (when (or (not @lock) no-lock)
     (when (get-in @game-state [:options :eot-auto-save])
       (do
       (send-command "system-msg" {:msg "just SAVED the GAME"})
       (ws/ws-send! [:meccg/save {:gameid-str (:gameid @game-state)
                                  :save-pref (:save-pref @game-state)}])))
     (try (js/ga "send" "event" "game" command) (catch js/Error e))
     (when-not no-lock (reset! lock true))
     (ws/ws-send! [:meccg/action {:gameid-str (:gameid @game-state) :command command :args args}])
     )))

(defn concede []
  (ws/ws-send! [:meccg/concede {:gameid-str (:gameid @game-state)}]))

(defn build-exception-msg [msg error]
  (letfn [(build-report-url [error]
            (js/escape (str "Please describe the circumstances of your error here.\n\n\nStack Trace:\n```clojure\n"
                            error
                            "\n```")))]
    (str "<div>"
         msg
         "<br/>"
         "<button type=\"button\" class=\"reportbtn\" style=\"margin-top: 5px\" "
         "onclick=\"window.open('https://github.com/rezwits/meccg/issues/new?body="
         (build-report-url error)
         "');\">Report on GitHub</button></div>")))

(defn toast-off
  "Display a toast warning with the specified message.
  Sends a command to clear any server side toasts."
  [msg type options]
  true)

(defn toast
  "Display a toast warning with the specified message.
  Sends a command to clear any server side toasts."
  [msg type options]
  (set! (.-options js/toastr) (toastr-options options))
  (let [f (aget js/toastr (if (= "exception" type) "error" type))]
    (f (if (= "exception" type) (build-exception-msg msg (:last-error @game-state)) msg))
    (send-command "toast")))

(defn play-sfx
  "Plays a list of sounds one after another."
  [sfx soundbank]
  (when-not (empty? sfx)
    (when-let [sfx-key (keyword (first sfx))]
      (.volume (sfx-key soundbank) (/ (str->int (get-in @app-state [:options :sounds-volume])) 100))
      (.play (sfx-key soundbank)))
    (play-sfx (rest sfx) soundbank)))

(defn action-list [{:keys [type facedown agent Secondary Home set_code zone revealed tapped wounded rotated inverted] :as card}]
  (-> []
      (#(if (and (#{"Character" "Site" "Region"} type)
                 (#{"locales" "onhost"} (first zone))
                 (not revealed)
                 (#{"Agent"} Secondary))
          (cons "agent" %) %))
      (#(if (and (#{"Character" "Site" "Region"} type)
                 (#{"locales" "onhost"} (first zone))
                 (not revealed))
          (cons "reveal" %) %))
      (#(if (and (#{"Character" "Site" "Region"} type)
                 (#{"locales" "onhost"} (first zone))
                 facedown revealed)
          (cons "reveal" %) %))
      (#(if (and (#{"Character" "Site" "Region"} type)
                 (#{"locales" "onhost"} (first zone))
                 agent (not facedown) revealed)
          (cons "hide" %) %))
      (#(if (and (#{"Character"} type)
                 (#{"locales"} (first zone))
                 revealed (not agent))
          (cons "organize" %) %))
      (#(if (and (#{"Character"} type)
                 (#{"locales" "onhost"} (first zone))
                 revealed (not wounded))
          (cons "wound" %) %))
      (#(if (and (#{"Ally"} Secondary)
                 (#{"onhost"} (first zone))
                 (not wounded))
          (cons "wound" %) %))
      (#(if (and (#{"Region"} type)
                 (boolean (re-find #"tap" Home))
                 revealed (not tapped))
          (cons "tap" %) %))
      (#(if (and (#{"Region"} type)
                 (or (and (boolean (re-find #"tap" Home)) tapped)
                     (and (not (boolean (re-find #"tap" Home))) (not tapped)))
                 revealed)
          (cons "regionize" %) %))
      (#(if (and (#{"Character" "Site"} type)
                 (#{"locales" "onhost"} (first zone))
                 revealed (not tapped))
          (cons "tap" %) %))
      (#(if (and (#{"Character" "Site"} type)
                 (#{"locales" "onhost"} (first zone))
                 (or tapped wounded rotated))
          (cons "untap" %) %))
      (#(if (and (= type "Resource")
                 (some (partial = Secondary) ["Greater Item" "Major Item" "Minor Item" "Gold Ring Item" "Special Item"])
                 (#{"locales" "onhost"} (first zone)))
          (cons "transfer" %) %))
      (#(if (and (= type "Resource")
                 (some (partial = Secondary) ["Greater Item" "Major Item" "Minor Item" "Gold Ring Item" "Special Item"])
                 (#{"locales" "onhost"} (first zone))
                 (not rotated))
          (cons "rotate" %) %))
      (#(if (and (= type "Resource")
                 (some (partial = Secondary) ["Greater Item" "Major Item" "Minor Item" "Gold Ring Item" "Special Item"])
                 (#{"locales" "onhost"} (first zone))
                 (not inverted))
          (cons "invert" %) %))
      (#(if (and (= type "Resource")
                 (some (partial = Secondary) ["Ally" "Greater Item" "Major Item" "Minor Item" "Gold Ring Item" "Special Item"])
                 (#{"locales" "onhost"} (first zone))
                 (not tapped))
          (cons "tap" %) %))
      (#(if (and (= type "Resource")
                 (some (partial = Secondary) ["Ally" "Greater Item" "Major Item" "Minor Item" "Gold Ring Item" "Special Item"])
                 (#{"locales" "onhost"} (first zone))
                 (or tapped wounded inverted rotated))
          (cons "untap" %) %))
      (#(if (and (#{"Resource" "Hazard"} type)
                 (some (partial = Secondary) ["Permanent-event" "Long-event" "Faction" "Avatar"])
                 (boolean (re-find #"rotate" Home))
                 (#{"rig" "onhost"} (first zone))
                 (not rotated))
          (cons "rotate" %) %))
      (#(if (and (#{"Resource" "Hazard"} type)
                 (some (partial = Secondary) ["Permanent-event" "Long-event" "Faction" "Avatar"])
                 (boolean (re-find #"invert" Home))
                 (#{"rig" "onhost"} (first zone))
                 (not inverted))
          (cons "invert" %) %))
      (#(if (and (#{"Resource" "Hazard"} type)
                 (some (partial = Secondary) ["Permanent-event" "Creature/Permanent-event" "Long-event" "Faction" "Avatar"])
                 (boolean (re-find #"tap" Home))
                 (#{"rig" "onhost"} (first zone))
                 (not tapped))
          (cons "tap" %) %))
      (#(if (and (#{"Resource" "Hazard"} type)
                 (some (partial = Secondary) ["Permanent-event" "Creature/Permanent-event" "Long-event" "Faction" "Avatar"])
                 (or (boolean (re-find #"tap" Home))
                     (boolean (re-find #"invert" Home))
                     (boolean (re-find #"rotate" Home)))
                 (#{"rig" "onhost"} (first zone))
                 (or tapped inverted rotated))
          (cons "untap" %) %))
      (#(if (and (some (partial = set_code) ["MENE" "MEWR" "MENW" "MEML"
                                             "MERS" "MESL" "MEGW"])
                 (some (partial = Secondary) ["Permanent-event"])
                 (boolean (re-find #"flip" Home))
                 (#{"rig" "onhost"} (first zone)))
          (cons "flip" %) %))
      ))

(defn handle-abilities [{:keys [abilities facedown side type] :as card} owner]
  (let [actions (action-list card)
        c (+ (count actions) (count abilities))]
    (cond
      ;; Open panel
      (or (> c 1)
          (some #{"hide" "advance"} actions)
          (and (= type "Character")
               (not (:run @game-state))))  ; Horrible hack to check if currently in a run
      (-> (om/get-node owner "abilities") js/$ .toggle)
      ;; Trigger first (and only) ability / action
      (= c 1)
      (if (= (count abilities) 1)
        (send-command "ability" {:card card :ability 0})
        (send-command (first actions) {:card card})))))

(defn handle-card-click [{:keys [type Secondary zone root] :as card} owner]
  (let [side (:side @game-state)]
    (when (not-spectator?)
      (cond
        ;; Selecting card
        (= (get-in @game-state [side :prompt 0 :prompt-type]) "select")
        (send-command "select" {:card card})
        ;; Card is an identity of player's side
        (and (= (:type card) "Identity")
             (= side (keyword (.toLowerCase (:side card)))))
        (handle-abilities card owner)
        ;; Challenger side
        (= side :challenger)
        (case (first zone)
          ("hand" "current" "scored") (case type
                   ("Character") (if root
                                   (send-command "play" {:card card :locale root})
                                   (-> (om/get-node owner "locales") js/$ .toggle))
                   ("Region" "Site") (if (< (count (get-in @game-state [:challenger :locales])) 4)
                                       (send-command "play" {:card card :locale "New party"})
                                       (-> (om/get-node owner "locales") js/$ .toggle))
                   ("Resource") (if (some (partial = Secondary) ["Avatar" "Permanent-event" "Long-event"
                                                                 "Permanent-event/Short-event"
                                                                 "Short-event" "Faction"])
                                  (send-command "play" {:card card})
                                  (send-command "equip" {:card card}))
                   (send-command "play" {:card card}))
          ("onhost") (case type
                       ("Hazard") (send-command "play" {:card card})
                       (handle-abilities card owner))
          ("rig" "locales") (handle-abilities card owner)
          nil)
        ;; Contestant side
        (= side :contestant)
        (case (first zone)
          ("hand" "current" "scored") (case type
                   ("Character") (if root
                                   (send-command "play" {:card card :locale root})
                                   (-> (om/get-node owner "locales") js/$ .toggle))
                   ("Region" "Site") (if (< (count (get-in @game-state [:contestant :locales])) 4)
                                       (send-command "play" {:card card :locale "New party"})
                                       (-> (om/get-node owner "locales") js/$ .toggle))
                   ("Resource") (if (some (partial = Secondary) ["Avatar" "Permanent-event" "Long-event"
                                                                 "Permanent-event/Short-event"
                                                                 "Short-event" "Faction"])
                                  (send-command "play" {:card card})
                                  (send-command "equip" {:card card}))
                   (send-command "play" {:card card}))
          ("onhost") (case type
                       ("Hazard") (send-command "play" {:card card})
                       (handle-abilities card owner))
          ("rig" "locales") (handle-abilities card owner)
          nil)))))

(defn in-play? [card]
  (if (= (:side card) "Challenger")
    (let [dest (when (= (:side card) "Challenger")
                 (get-in @game-state [:challenger :rig (keyword (.toLowerCase (:type card)))]))]
      (some #(= (:title %) (:title card)) dest))
    (let [dest (when (= (:side card) "Contestant")
                 (get-in @game-state [:contestant :rig (keyword (.toLowerCase (:type card)))]))]
      (some #(= (:title %) (:title card)) dest))))

(defn has?
  "Checks the string property of the card to see if it contains the given value"
  [card property value]
  (when-let [p (property card)]
    (> (.indexOf p value) -1)))

(defn has-subtype?
  "Checks if the specified subtype is present in the card."
  [card subtype]
  (or (has? card :subtype subtype)
      (when-let [persistent-subs (-> card :persistent :subtype)]
        (includes? persistent-subs subtype))))

(defn playable? [{:keys [title side zone cost type uniqueness abilities] :as card}]
  (let [my-side (:side @game-state)
        me (my-side @game-state)]
    (and (= (keyword (.toLowerCase side)) my-side)

         (cond

           (has-subtype? card "Double")
           (if (>= (:click me) 2) true false)

           (has-subtype? card "Triple")
           (if (>= (:click me) 3) true false)

           (= (:code card) "07036") ; Day Job
           (if (>= (:click me) 4) true false)

           (has-subtype? card "Priority")
           (if (get-in @game-state [my-side :register :spent-click]) false true)

           :else
           true)

         (and (= zone ["hand"])
              (or (not uniqueness) (not (in-play? card)))
              (or (#{"Agenda" "Site" "Region" "Character" "Resource"} type) (>= (:credit me) cost))
              (pos? (:click me))))))

(defn spectator-view-hidden?
  "Checks if spectators are allowed to see hidden information, such as hands and face-down cards"
  []
  (and (get-in @game-state [:options :spectatorhands])
       (not (not-spectator?))))

(def ci-open "\u2664")
(def ci-seperator "\u2665")
(def ci-close "\u2666")

(defn is-card-item [item]
  (and (> (.indexOf item ci-seperator) -1)
       (= 0 (.indexOf item ci-open))))

(defn extract-card-info [item]
  (if (is-card-item item)
    [(.substring item 1 (.indexOf item ci-seperator))
     (.substring item (inc (.indexOf item ci-seperator)) (dec (count item)))]))

(defn add-regions [card-text]
  (-> (if (nil? card-text) "" card-text)
      (create-face "Border-hold" "img/dc/me_bh.png")
      (create-face "Border-land" "img/dc/me_bl.png")
      (create-face "Triple Coastal Sea" "img/dc/me_tc.png")
      (create-face "Double Coastal Sea" "img/dc/me_dc.png")
      (create-face "Coastal Sea" "img/dc/me_cs.png")
      (create-face "Darkhaven" "img/dc/me_dha.png")
      (create-face "Dark-hold" "img/dc/me_dh.png")
      (create-face "Dark-domain" "img/dc/me_dd.png")
      (create-face "Double Desert" "img/dc/me_ee.png")
      (create-face "Desert" "img/dc/me_er.png")
      (create-face "Free-domain" "img/dc/me_fd.png")
      (create-face "Free-hold" "img/dc/me_fh.png")
      (create-face "Haven" "img/dc/me_ha.png")
      (create-face "Jungle" "img/dc/me_ju.png")
      (create-face "Ruins & Lairs" "img/dc/me_rl.png")
      (create-face "Shadow-hold" "img/dc/me_sh.png")
      (create-face "Shadow-land" "img/dc/me_sl.png")
      (create-face "Triple Wilderness" "img/dc/me_tw.png")
      (create-face "Double Wilderness" "img/dc/me_dw.png")
      (create-face "Wilderness" "img/dc/me_wi.png")
      ))

(defn create-span-impl [item]
  (if (= "[hr]" item)
    [:hr]
    (if (= "[!]" item)
      [:div.smallwarning "!"]
      (if-let [[title image] (extract-card-info item)]
        [:span {:class "fake-link" :id image} title]
        (if (boolean (re-find #"16mm" item))
          [:span {:dangerouslySetInnerHTML #js {:__html (add-faces-16mm (add-faces-16mm item))}}]
          (if (boolean (re-find #"18mm" item))
            [:span {:dangerouslySetInnerHTML #js {:__html (add-faces-18mm (add-faces-18mm item))}}]
            ;[:span {:dangerouslySetInnerHTML #js {:__html (add-regions item)}}]
            [:span {:dangerouslySetInnerHTML #js {:__html
                                                  (loop [new-text item]
                                                    (if (= new-text (add-regions new-text))
                                                      new-text
                                                      (recur (add-regions new-text))))}}]
            ))))))

(defn get-non-alt-art [[title cards]]
  {:title title :ImageName (:ImageName (last cards))})

(comment
(defn prepare-cards []
  (->> @all-cards
       (group-by :title)
       (map get-non-alt-art)
       (sort-by #(count (:title %1)))
       (reverse)))

(def prepared-cards (memoize prepare-cards)))

(defn prepare-image []
  (->> @all-cards
       (sort-by #(count (:ImageName %1)))
       (reverse)))

(def prepared-image (memoize prepare-image))

(def create-span (memoize create-span-impl))

(defn find-card-regex-impl [delimeter]
  (str "(^|[^" ci-open "\\S])" delimeter "(?![" ci-seperator "\\w]|([^" ci-open "]+" ci-close "))"))

(def find-card-regex (memoize find-card-regex-impl))

(defn card-image-token-impl [title code]
  (str "$1" ci-open title ci-seperator code ci-close))

(def card-image-token (memoize card-image-token-impl))

(defn card-image-reducer [text card]
  (let [language (get-in @app-state [:options :language])]
    (case language
      "Dutch"
      (.replace text (js/RegExp. (find-card-regex (:ImageName card)) "g") (card-image-token (:title-du card) (:ImageName card)))
      "English"
      (.replace text (js/RegExp. (find-card-regex (:ImageName card)) "g") (card-image-token (:title card) (:ImageName card)))
      "Español"
      (.replace text (js/RegExp. (find-card-regex (:ImageName card)) "g") (card-image-token (:title-es card) (:ImageName card)))
      "Finnish"
      (.replace text (js/RegExp. (find-card-regex (:ImageName card)) "g") (card-image-token (:title-fn card) (:ImageName card)))
      "French"
      (.replace text (js/RegExp. (find-card-regex (:ImageName card)) "g") (card-image-token (:title-fr card) (:ImageName card)))
      "German"
      (.replace text (js/RegExp. (find-card-regex (:ImageName card)) "g") (card-image-token (:title-gr card) (:ImageName card)))
      "Italian"
      (.replace text (js/RegExp. (find-card-regex (:ImageName card)) "g") (card-image-token (:title-it card) (:ImageName card)))
      "Japanese"
      (.replace text (js/RegExp. (find-card-regex (:ImageName card)) "g") (card-image-token (:title-jp card) (:ImageName card)))
      :default
      (.replace text (js/RegExp. (find-card-regex (:ImageName card)) "g") (card-image-token (:title card) (:ImageName card))))
    ))

(defn card-title-reducer [text card]
  (let [language (get-in @app-state [:options :language])]
    (case language
      "Dutch"
      (.replace text (js/RegExp. (find-card-regex (:title-du card)) "g") (card-image-token (:title-du card) (:ImageName card)))
      "English"
      (.replace text (js/RegExp. (find-card-regex (:title card)) "g") (card-image-token (:title card) (:ImageName card)))
      "Español"
      (.replace text (js/RegExp. (find-card-regex (:title-es card)) "g") (card-image-token (:title-es card) (:ImageName card)))
      "Finnish"
      (.replace text (js/RegExp. (find-card-regex (:title-fn card)) "g") (card-image-token (:title-fn card) (:ImageName card)))
      "French"
      (.replace text (js/RegExp. (find-card-regex (:title-fr card)) "g") (card-image-token (:title-fr card) (:ImageName card)))
      "German"
      (.replace text (js/RegExp. (find-card-regex (:title-gr card)) "g") (card-image-token (:title-gr card) (:ImageName card)))
      "Italian"
      (.replace text (js/RegExp. (find-card-regex (:title-it card)) "g") (card-image-token (:title-it card) (:ImageName card)))
      "Japanese"
      (.replace text (js/RegExp. (find-card-regex (:title-jp card)) "g") (card-image-token (:title-jp card) (:ImageName card)))
      :default
      (.replace text (js/RegExp. (find-card-regex (:title card)) "g") (card-image-token (:title card) (:ImageName card))))
    ))

(comment
  (defn add-image-codes-impl [text]
    (let [by-image (reduce card-image-reducer text (prepared-image))
          by-codes (reduce card-title-reducer text (prepared-cards))]
      (if (> (count by-image) (count by-codes))
        by-image
        by-codes)))
  )
(defn add-image-codes-impl [text]
  (reduce card-image-reducer text (prepared-image)))

(def add-image-codes (memoize add-image-codes-impl))

(defn get-message-parts-impl [text]
  (let [with-image-codes (add-image-codes (if (nil? text) "" text))
        splitted (.split with-image-codes (js/RegExp. (str "(" ci-open "[^" ci-close "]*" ci-close ")") "g"))
        oldstyle (for [i splitted]
                   (seq (.split i (js/RegExp. (str "([1-3]\\[mu\\]|\\[[^\\]]*\\])") "g"))))]
    (flatten oldstyle)))

(def get-message-parts (memoize get-message-parts-impl))

(defn get-card-image [e]
  (let [image (str (.. e -target -id))]
    (when (pos? (count image))
      image)))

(defn handle-key-down [e]
  (if (= (:keys-pick (:options @app-state)) "default")
    (cond
      (= e.keyCode 38) ;// arrow-plus
      (send-command "bonus-key-down")
      (= e.keyCode 40) ;// arrow-down
      (send-command "minus-key-down")
      (= e.keyCode 112) ;// F1
      (send-command "f1-f12-key-down")
      (= e.keyCode 123) ;// F12
      (send-command "f1-f12-key-down")
      (= e.keyCode 220) ;// slash
      (send-command "option-key-down")
      (= e.keyCode 219) ;// square-left
      (send-command "blind-zoom")
      (= e.keyCode 221) ;// square-rght
      (send-command "blind-send")
      )
    (cond
      (= e.keyCode 38) ;// arrow-plus
      (send-command "bonus-key-down")
      (= e.keyCode 40) ;// arrow-down
      (send-command "minus-key-down")
      (= e.keyCode 112) ;// F1
      (send-command "f1-f12-key-down")
      (= e.keyCode 123) ;// F12
      (send-command "f1-f12-key-down")
      (= e.keyCode 18) ;// option
      (send-command "option-key-down")
      (= e.keyCode 37) ;// arrow-left
      (send-command "blind-zoom")
      (= e.keyCode 39) ;// arrow-rght
      (send-command "blind-send")
      ))
  )

(defn handle-key-up [e]
  (if (= (:keys-pick (:options @app-state)) "default")
    (cond
      (= e.keyCode 38) ;// arrow-plus
      (send-command "bonus-key-down")
      (= e.keyCode 40) ;// arrow-down
      (send-command "minus-key-down")
      (= e.keyCode 219) ;// square-left
      (send-command "blind-zoom")
      (= e.keyCode 221) ;// square-rght
      (send-command "blind-send")
      )
    (cond
      (= e.keyCode 38) ;// arrow-plus
      (send-command "bonus-key-down")
      (= e.keyCode 40) ;// arrow-down
      (send-command "minus-key-down")
      (= e.keyCode 37) ;// arrow-left
      (send-command "blind-zoom")
      (= e.keyCode 39) ;// arrow-rght
      (send-command "blind-hold")
      )
    )
  )

(defn card-preview-mouse-over [e channel]
  (.preventDefault e)
  (when-let [image (get-card-image e)]
    (when-let [card (some #(when (= (:ImageName %) image) %) @all-cards)]
      (put! channel (assoc card :implementation :full))))
  nil)

(defn card-preview-mouse-out [e channel]
  (.preventDefault e)
  (when-let [image (get-card-image e)]
    (put! channel false))
  nil)

(defn log-pane [cursor owner]
  (reify
    ;; om/IInitState
    ;; (init-state [this] {:scrolling false})

    om/IDidUpdate
    (did-update [this prev-props prev-state]
      (let [div (om/get-node owner "msg-list")
            ;; curr-msg-count (count (:log cursor))
            ;; prev-msg-count (count (:log prev-props))
            scrolltop (.-scrollTop div)
            height (.-scrollHeight div)]
        (when (or (zero? scrolltop)
                  (< (- height scrolltop (.height (js/$ ".gameboard .log"))) 500))
          (aset div "scrollTop" height))))
    ;;     is-scrolled (om/get-state owner :scrolling)
    ;;     scroll-top (.-scrollTop div)
    ;;     scroll-height (.-scrollHeight div)]
    ;; (when (or (and (zero? scroll-top)
    ;;                (not is-scrolled))
    ;;           (and (not= curr-msg-count prev-msg-count)
    ;;                (not is-scrolled)))
    ;;   (aset div "scrollTop" scroll-height))))

    om/IDidMount
    (did-mount [this]
      (-> ".log" js/$ (.resizable #js {:handles "w"})))

    om/IRenderState
    (render-state [this state]
      (sab/html
        [:div.log {:on-key-down #(handle-key-down %)
                   ;:on-key-up #(handle-key-up %)
                   :on-mouse-over #(card-preview-mouse-over % zoom-channel)
                   :on-mouse-out  #(card-preview-mouse-out % zoom-channel)}
         [:div.panel.blue-shade.messages {:ref "msg-list"}
          ;; :on-scroll #(let [currElt (.-currentTarget %)
          ;;                   scroll-top (.-scrollTop currElt)
          ;;                   scroll-height (.-scrollHeight currElt)
          ;;                   client-height (.-clientHeight currElt)
          ;;                   scrolling (< (+ scroll-top client-height) scroll-height)]
          ;;               (om/set-state! owner :scrolling scrolling))}
          (for [msg (:log cursor)]
            (when-not (and (= (:user msg) "__system__") (= (:text msg) "typing"))
              (if (= (:user msg) "__system__")
                [:div.system (for [item (get-message-parts (:text msg))] (create-span item))]
                [:div.message
                 (om/build avatar (:user msg) {:opts {:size 38}})
                 [:div.content
                  [:div.username (get-in msg [:user :username])]
                  [:div (for [item (get-message-parts (:text msg))] (create-span item))]]])))]
         (when (seq (remove nil? (remove #{(get-in @app-state [:user :username])} (:typing cursor))))
           [:div [:p.typing (for [i (range 10)] [:span " " influence-dot " "])]])
         (if-let [game (some #(when (= (:gameid cursor) (str (:gameid %))) %) (:games @app-state))]
           (when (or (not-spectator?)
                     (not (:mutespectators game)))
               [:form {:on-submit #(send-msg % owner)
                       :on-input #(send-typing % owner)
                       :on-key-up #(case (.-which %)
                                     (219 220 221) (ws/ws-send! [:meccg/typing {:gameid-str (:gameid @game-state) :typing false}])
                                     nil)}
                [:input.direct {:ref "msg-input" :placeholder "Say something" :accessKey "l"
                                :on-key-up #(if (= (:keys-pick (:options @app-state)) "default")
                                              (case (.-which %)
                                                219 (set! (.-value (om/get-node owner "msg-input"))
                                                          (.replace (.-value (om/get-node owner "msg-input")) #"\[" ""))
                                                220 (set! (.-value (om/get-node owner "msg-input"))
                                                          (.replace (.-value (om/get-node owner "msg-input")) #"\\" ""))
                                                221 (set! (.-value (om/get-node owner "msg-input"))
                                                          (.replace (.-value (om/get-node owner "msg-input")) #"]" ""))
                                                nil))}]]))]))))

(defn handle-dragstart [e cursor]
  (-> e .-target js/$ (.addClass "dragged"))
  (-> e .-dataTransfer (.setData "card" (.stringify js/JSON (clj->js @cursor)))))

(defn handle-drop [e locale]
  (-> e .-target js/$ (.removeClass "dragover"))
  (let [card (-> e .-dataTransfer (.getData "card") ((.-parse js/JSON)) (js->clj :keywordize-keys true))
        side (if (#{"HQ" "R&D" "Archives" "Sites"} locale) "Contestant" "Challenger")
        location (if (#{"Sites"} locale) true false)]
    (if location
      (when (or (= "Site" (:type card)) (= "Region" (:type card)))
        (send-command "move" {:card card :locale locale}))
      (send-command "move" {:card card :locale locale})
      )))

(defn abs [n] (max n (- n)))

;; touch support
(defonce touchmove (atom {}))

(defn release-touch [card]
  (-> card (.removeClass "disable-transition"))
  (-> card (.css "position" ""))
  (-> card (.css "top" "")))

(defn update-card-position [card touch]
  (-> card (.css "left" (str (- (int (aget touch "pageX")) 30) "px")))
  (-> card (.css "top"  (str (- (int (aget touch "pageY")) 42) "px")))
  (-> card (.css "right" (str (- (int (aget touch "pageX")) 30) "px")))
  (-> card (.css "bottom"  (str (- (int (aget touch "pageY")) 42) "px")))
  )

(defn get-card [e locale]
  (-> e .-target js/$ (.closest ".card-wrapper")))

(defn get-locale-from-touch [touch]
  (let [cX (.. touch -clientX)
        cY (.. touch -clientY)
        locale (-> (js/document.elementFromPoint cX cY)
                   js/$
                   (.closest "[data-locale]")
                   (.attr "data-locale"))]
    [locale (> (+ (abs (- (:x @touchmove) cX))
                  (abs (- (:y @touchmove) cY)))
               30)]))

(defn handle-touchstart [e cursor]
  (let [touch (aget (.. e -targetTouches) 0)
        [locale _] (get-locale-from-touch touch)
        card (get-card e locale)]
    (-> card (.addClass "disable-transition"))
    (reset! touchmove {:card (.stringify js/JSON (clj->js @cursor))
                       :x (.. touch -clientX)
                       :y (.. touch -clientY)
                       :start-locale locale})))

(defn handle-touchmove [e]
  (let [touch (aget (.. e -targetTouches) 0)
        card (get-card e (:start-locale @touchmove))]
    (-> card (.css "position" "fixed"))
    (update-card-position card touch)))

(defn handle-touchend [e]
  (let [touch (aget (.. e -changedTouches) 0)
        card (get-card e (:start-locale @touchmove))
        [locale moved-enough] (get-locale-from-touch touch)]
    (release-touch card)
    (when (and locale moved-enough (not= locale (:start-locale @touchmove)))
      (let [cardinfo (-> @touchmove :card ((.-parse js/JSON)) (js->clj :keywordize-keys true))]
        (send-command "move" {:card cardinfo :locale locale})))))

(defn ability-costs [ab]
  (when-let [cost (:cost ab)]
    (str (clojure.string/join
           ", " (for [c (partition 2 cost)]
                  (str (case (first c)
                         "credit" (str (second c) " [" (capitalize (name (first c))) "]")
                         (clojure.string/join "" (repeat (second c) (str "[" (capitalize (name (first c))) "]"))))))) ": ")))

(defn party->num [locale]
  (-> locale str (clojure.string/split #":party") last str->int))

(defn party->name [locale]
  (let [num (party->num locale)]
    (str "Locale " num)))

(defn central->name [zone]
  "Converts a central zone keyword to a string."
  (case (if (keyword? zone) zone (last zone))
    :hq "HQ"
    :rd "R&D"
    :archives "Archives"
    :sites "Sites"
    nil))

(defn zone->name [zone]
  "Converts a zone to a string."
  (or (central->name zone)
      (party->name zone)))

(defn zone->sort-key [zone]
  (case (if (keyword? zone) zone (last zone))
    :sites -4
    :archives -3
    :rd -2
    :hq -1
    (str->int
      (last (clojure.string/split (str zone) #":party")))))

(defn zones->sorted-names [zones]
  (->> zones (sort-by zone->sort-key) (map zone->name)))

(defn get-parties [locales]
  (->> locales
       (filter #(not (#{:hq :rd :archives :sites} (first %))))
       (sort-by #(zone->sort-key (first %)))))

(defn party-list [parties]
  (->> parties (map first) zones->sorted-names))

(defn card-counter-type [card]
  (let [counter-type (:counter-type card)]
    ;; Determine the appropriate type of counter for styling, falling back to
    ;; power counters when no other type can be inferred.
    (cond
      ;; If an placed card contains an annotation, use it.
      (and (:placed card)
           (not (nil? counter-type)))
      counter-type
      (= "Agenda" (:type card)) "Agenda"
      ;; Assume unplaced cards with counters are hosted on Personal
      ;; Workshop.
      (not (:placed card)) "Power"
      (not (:subtype card)) "Power"
      (> (.indexOf (:subtype card) "Virus") -1) "Virus"
      :else "Power")))

(defn facedown-card
  "Sab image element of a facedown card"
  ([side] (facedown-card side [] nil))
  ([side class-list alt-alt-text]
   (let [s (.toLowerCase side)
         alt (if (nil? alt-alt-text)
               (str "Facedown " s " card")
               alt-alt-text)
         tag (->> class-list
                  vec
                  (concat ["img" "card"])
                  (join ".")
                  keyword)]
     [tag {:src (str "/img/" s ".jpg")
           :alt alt}])))

(defn card-img
  "Build an image of the card (is always face-up). Only shows the zoomed card image, does not do any interaction."
  [{:keys [code title] :as cursor}]
  (om/component
    (when code
      (sab/html
        [:div.card-frame
         [:div.blue-shade.card {;:on-key-down #(handle-blindzoom %)
                                :on-mouse-enter #(put! zoom-channel cursor)
                                :on-mouse-leave #(put! zoom-channel false)}
          (when-let [url (image-url cursor)]
            [:div
             [:span.cardname title]
             [:img.card.bg {:src url :alt title :onError #(-> % .-target js/$ .hide)}]])]]))))

(defn face-down?
  "Returns true if the placed card should be drawn face down."
  [{:keys [side type facedown revealed host] :as card}]
  (if (or (= side "Contestant") (= side "Challenger"))
    (and (not= type "Resource")
         (not= type "Hazard"))
    facedown))

(defn card-blind [card owner]
  (om/component
    (sab/html
      [:div.card-preview.blind.blue-shade {:class (if (and (#{"Region"} (:type card))
                                                           (re-find #"tap" (:Home card)))
                                                    "region")}
       (when-let [url (image-url card)]
         [:img {:src url :alt (:title card) :onLoad #(-> % .-target js/$ .show)}])]
      )))

(defn card-zoom [card owner]
  (om/component
    (sab/html
      [:div.card-preview.blue-shade {:class (if (and (#{"Region"} (:type card))
                                                     (re-find #"tap" (:Home card)))
                                              "region")}
       (when-let [url (image-url card)]
         [:img {:src url :alt (:title card) :onLoad #(-> % .-target js/$ .show)}])]
      )))

(defn card-view [{:keys [zone code type abilities counter advance-counter advancementcost current-cost subtype
                         advanceable revealed tapped rotated strength current-strength title parties selected hosted
                         side rec-counter facedown locale-target subtype-target icon new challenger-abilities
                         contestant-abilities subroutines]
                  :as cursor}
                 owner {:keys [flipped location] :as opts}]
  (om/component
    (sab/html
      [:div.card-frame
       [:div.blue-shade.card {:class (str (when selected "selected") (when new " new"))
                              :draggable (when (not-spectator?) true)
                              :on-touch-start #(handle-touchstart % cursor)
                              :on-touch-end   #(handle-touchend %)
                              :on-touch-move  #(handle-touchmove %)
                              :on-drag-start #(handle-dragstart % cursor)
                              :on-drag-end #(-> % .-target js/$ (.removeClass "dragged"))
                              :on-mouse-enter #(when (or (not (or (not code) flipped facedown))
                                                         (spectator-view-hidden?)
                                                         (= (:side @game-state) (keyword (.toLowerCase side))))
                                                 (put! zoom-channel cursor))
                              :on-mouse-leave #(put! zoom-channel false)
                              ;:on-key-up #(handle-key-up %)
                              ;:on-key-down #(handle-blindzoom %)
                              :on-click #(handle-card-click cursor owner)}
        (when-let [url (image-url cursor)]
          (if (or (not code) flipped facedown)
            (let [facedown-but-known (or (not (or (not code) flipped facedown))
                                         (spectator-view-hidden?)
                                         (= (:side @game-state) (keyword (.toLowerCase side))))
                  alt-str (if facedown-but-known (str "Facedown " title) nil)
                  locate (site? cursor)]
              (if (or locate location)
                (facedown-card "Locations")
                (facedown-card side ["bg"] alt-str)))
            [:div
             ;[:span.cardname title]
             [:img.card.bg {:src url :alt title :onError #(-> % .-target js/$ .hide)}]]))
        [:div.counters
         (when counter
           (map (fn [[type num-counters]]
                  (when (pos? num-counters)
                    (let [selector (str "div.darkbg." (lower-case (name type)) "-counter.counter")]
                      [(keyword selector) num-counters])))
                counter))
         (when (pos? rec-counter) [:div.darkbg.recurring-counter.counter rec-counter])
         (when (pos? advance-counter) [:div.darkbg.advance-counter.counter advance-counter])]
        (when (and current-strength (not= strength current-strength))
          current-strength [:div.darkbg.strength current-strength])
        (when (get-in cursor [:special :extra-subs])
          [:div.darkbg.extra-subs \+])
        (when-let [{:keys [char color]} icon] [:div.darkbg.icon {:class color} char])
        (when locale-target [:div.darkbg.locale-target locale-target])
        (when subtype-target
          (let [colour-type (case subtype-target
                              ("Barrier" "Sentry") (lower-case subtype-target)
                              "Code Gate" "code-gate"
                              nil)
                label (if (includes? subtype-target " - ")
                        (->> (split subtype-target #" - ")
                             (map first)
                             (join " - "))
                        subtype-target)]
            [:div.darkbg.subtype-target {:class colour-type} label]))
        (when (and (= zone ["hand"]) (#{"Site" "Character" "Region"} type))
          (let [parties (concat (party-list parties) ["New party"])
                locales (case type
                          ("Site" "Region" "Character") parties)]
            [:div.panel.blue-shade.locales-menu {:ref "locales"}
             (map (fn [label]
                    [:div {:on-click #(do (send-command "play" {:card @cursor :locale label})
                                          (-> (om/get-node owner "locales") js/$ .fadeOut))}
                     label])
                  locales)]))
        (when (pos? (+ (count challenger-abilities) (count subroutines)))
          [:div.panel.blue-shade.challenger-abilities {:ref "challenger-abilities"}
           (map-indexed
             (fn [i ab]
               [:div {:on-click #(do (send-command "challenger-ability" {:card @cursor
                                                                         :ability i}))
                      :dangerouslySetInnerHTML #js {:__html (add-symbols (str (ability-costs ab) (:label ab)))}}])
             challenger-abilities)
           (when (> (count subroutines) 1)
             [:div {:on-click #(send-command "system-msg"
                                             {:msg (str "indicates to fire all subroutines on " title)})}
              "Let all subroutines fire"])
           (map (fn [sub]
                  [:div {:on-click #(send-command "system-msg"
                                                  {:msg (str "indicates to fire the \"" (:label sub)
                                                             "\" subroutine on " title)})
                         :dangerouslySetInnerHTML #js {:__html (add-symbols (str "Let fire: \"" (:label sub) "\""))}}])
                subroutines)])
        (when (pos? (count contestant-abilities))
          [:div.panel.blue-shade.contestant-abilities {:ref "contestant-abilities"}
           (map-indexed
             (fn [i ab]
               [:div {:on-click #(do (send-command "contestant-ability" {:card @cursor
                                                                         :ability i}))
                      :dangerouslySetInnerHTML #js {:__html (add-symbols (str (ability-costs ab) (:label ab)))}}])
             contestant-abilities)])
        (let [actions (action-list cursor)
              dynabi-count (count (filter :dynamic abilities))]
          (when (or (> (+ (count actions) (count abilities) (count subroutines)) 1)
                    (some #{"hide" "advance"} actions)
                    (= type "Character"))
            [:div.panel.blue-shade.abilities {:ref "abilities"}
             (map (fn [action]
                    [:div {:on-click #(do (send-command action {:card @cursor}))} (capitalize action)])
                  actions)
             (map-indexed
               (fn [i ab]
                 (if (:dynamic ab)
                   [:div {:on-click #(do (send-command "dynamic-ability" (assoc (select-keys ab [:dynamic :source :index])
                                                                           :card @cursor)))
                          :dangerouslySetInnerHTML #js {:__html (add-symbols (str (ability-costs ab) (:label ab)))}}]
                   [:div {:on-click #(do (send-command "ability" {:card @cursor
                                                                  :ability (- i dynabi-count)})
                                         (-> (om/get-node owner "abilities") js/$ .fadeOut))
                          :dangerouslySetInnerHTML #js {:__html (add-symbols (str (ability-costs ab) (:label ab)))}}]))
               abilities)
             (map-indexed
               (fn [i sub]
                 [:div {:on-click #(do (send-command "subroutine" {:card @cursor :subroutine i})
                                       (-> (om/get-node owner "abilities") js/$ .fadeOut))
                        :dangerouslySetInnerHTML #js {:__html (add-symbols (str "[Subroutine]" (:label sub)))}}])
               subroutines)]))
        (when (#{"locales" "onhost"} (first zone))
          (cond
            (and (= type "Agenda") (>= advance-counter (or current-cost advancementcost)))
            [:div.panel.blue-shade.menu.abilities {:ref "agenda"}
             [:div {:on-click #(send-command "advance" {:card @cursor})} "Advance"]
             [:div {:on-click #(send-command "score" {:card @cursor})} "Score"]]
            (or (= advanceable "always") (and revealed (= advanceable "revealed-only")))
            [:div.panel.blue-shade.menu.abilities {:ref "advance"}
             [:div {:on-click #(send-command "advance" {:card @cursor})} "Advance"]
             [:div {:on-click #(send-command "reveal" {:card @cursor})} "Reveal"]]))]]
       )))

(defn host-view [hosted]
  (let [s (count hosted)]
    (when-not (empty? hosted)
      (map-indexed (fn [i card]
                     [:div.hosted (if (:tapped card)
                                    {:class "tapped"
                                     }
                                    (if (:inverted card)
                                      {:class "inverted"
                                       }
                                      (if (:wounded card)
                                        {:class "wounded"
                                         }
                                        (if (:rotated card)
                                          {:class "rotated"
                                           }
                                          ))))
                      (when (pos? (count (:hosted card)))
                         (host-view (:hosted card)))
                      (om/build card-view card {:opts {:flipped (face-down? card)}})]
                     ) (reverse hosted))))
  )

(defn drop-area [side locale hmap]
  (merge hmap {:on-drop #(handle-drop % locale)
               :on-drag-enter #(-> % .-target js/$ (.addClass "dragover"))
               :on-drag-leave #(-> % .-target js/$ (.removeClass "dragover"))
               :on-drag-over #(.preventDefault %)
               :data-locale locale}))

(defn close-popup [event owner ref msg shuffle? location? board? fw-dc? deck?]
  (-> (om/get-node owner ref) js/$ .fadeOut)
  (cond
    location? (send-command "close-location")
    shuffle? (send-command "shuffle" {:board board? :fwsb fw-dc? :close "true"})
    board? (send-command "close-sideboard")
    fw-dc? (send-command "close-fw-dc-sb")
    deck? (send-command "close-deck")
    msg (when (not (= "" msg)) (send-command "system-msg" {:msg msg})))
  (.stopPropagation event))

(defn label [cursor owner opts]
  (om/component
    (sab/html
      (let [fn (or (:fn opts) count)]
        [:div.header {:class (when (> (count cursor) 0) "darkbg")}
         (str (:name opts) " (" (fn cursor) ")")]))))

(defn label-without [cursor owner opts]
  (om/component
    (sab/html
      [:div.header {:class (when (> (count cursor) 0) "darkbg")}
       (str (:name opts))])))

(defn- this-user?
  [player]
  (= (-> player :user :_id) (-> @app-state :user :_id)))

(defn build-hand-card-view
  [player parties wrapper-class]
  (let [side (get-in player [:identity :side])
        size (count (:hand player))]
    (sab/html
      (map-indexed
        (fn [i card]
          [:div {:class (str
                          (if (and (not= "select" (get-in player [:prompt 0 :prompt-type]))
                                   (this-user? player)
                                   (not (:selected card)) (playable? card))
                            "playable" "")
                          " "
                          wrapper-class)
                 :style {:left (* (/ 320 (dec size)) i)}}
           (if (or (this-user? player)
                   (spectator-view-hidden?)) ;(not this show spectator)
             (om/build card-view (assoc card :parties parties) {:opts {:flipped false}})
             (om/build card-view (assoc card :parties parties) {:opts {:flipped true}})
             )])
        (:hand player)))))

(defn hand-view [{:keys [player parties popup popup-direction] :as cursor} owner]
  (om/component
    (sab/html
      (let [side (get-in player [:identity :side])
            size (count (:hand player))
            name (if (= side "Contestant") "HQ" "Grip")
            status (if (= side "Contestant")
                      (if (not (get-in @game-state [:contestant :drew])) "Pool" "Hand")
                      (if (not (get-in @game-state [:challenger :drew])) "Pool" "Hand"))]
        [:div.hand-container
         [:div.hand-controls
          [:div.panel.blue-shade.hand
           (drop-area (:side @game-state) name {:class (when (> size 6) "squeeze")})
           [:div
            (build-hand-card-view player parties "card-wrapper")]
           (om/build label (:hand player) {:opts {:name status}})]
          (when popup
            [:div.panel.blue-shade.hand-expand
             {:on-click #(-> (om/get-node owner "hand-popup") js/$ .fadeToggle)}
             "+"])]
         (when popup
           [:div.panel.blue-shade.popup {:ref "hand-popup" :class popup-direction}
            [:div
             [:a {:on-click #(close-popup % owner "hand-popup" nil false false false false false)} "Close"]
             [:label (str size " card" (when (not= 1 size) "s") ".")]
             (build-hand-card-view player parties "card-popup-wrapper")
             ]])]))))

(defn build-hand-site-view
  [player parties wrapper-class]
  (let [side (get-in player [:identity :side])
        size (count (:hand player))]
    (sab/html
      (map-indexed
        (fn [i card]
          [:div {:class (str
                          (if (and (not= "select" (get-in player [:prompt 0 :prompt-type]))
                                   (this-user? player)
                                   (not (:selected card)) (playable? card))
                            "playable" "")
                          " "
                          wrapper-class)
                 :style {:left (* (/ 320 (dec size)) i)}}
           (if (or (this-user? player)
                   (and (not (:openhand player)) (:opensite player)) ;(not opensite and shows hand)
                   (spectator-view-hidden?))
             (om/build card-view (assoc card :parties parties) {:opts {:flipped true}})
             (om/build card-view card {:opts {:flipped false}})
             )])
        (:hand player)))))

(defn site-view [{:keys [player parties popup popup-direction] :as cursor} owner]
  (om/component
    (sab/html
      (let [side (get-in player [:identity :side])
            size (count (:hand player))
            name (if (= side "Contestant") "HQ" "Grip")
            status (if (= side "Contestant")
                     (if (not (get-in @game-state [:contestant :drew])) "Pool" "Hand")
                     (if (not (get-in @game-state [:challenger :drew])) "Pool" "Hand"))]
        [:div.hand-container
         [:div.hand-controls
          [:div.panel.blue-shade.hand
           (drop-area (:side @game-state) name {:class (when (> size 6) "squeeze")})
           [:div
            (build-hand-site-view player parties "card-wrapper")]
           (om/build label (:hand player) {:opts {:name status}})]
          (when popup
            [:div.panel.blue-shade.hand-expand
             {:on-click #(-> (om/get-node owner "hand-popup") js/$ .fadeToggle)}
             "+"])]
         (when popup
           [:div.panel.blue-shade.popup {:ref "hand-popup" :class popup-direction}
            [:div
             [:a {:on-click #(close-popup % owner "hand-popup" nil false false false false false)} "Close"]
             [:label (str size " card" (when (not= 1 size) "s") ".")]
             (build-hand-site-view player parties "card-popup-wrapper")
             ]])]))))

(defn show-deck [event owner ref]
  (-> (om/get-node owner (str ref "-content")) js/$ .fadeIn)
  (-> (om/get-node owner (str ref "-menu")) js/$ .fadeOut)
  (send-command "view-deck"))

(defn show-sideboard [event owner ref]
  (-> (om/get-node owner (str ref "-content")) js/$ .fadeIn)
  (-> (om/get-node owner (str ref "-menu")) js/$ .fadeOut)
  (send-command "view-sideboard"))

(defn show-fw-dc-sb [event owner ref]
  (-> (om/get-node owner (str ref "-content")) js/$ .fadeIn)
  (-> (om/get-node owner (str ref "-menu")) js/$ .fadeOut)
  (send-command "view-fw-dc-sb"))

(defn show-sites [event owner ref menu terrain region]
  (if (or (= ref "Ch-regions")
          (= ref "Co-regions")
          (= ref "Ch-regions-north")
          (= ref "Co-regions-north")
          (= ref "Ch-regions-west")
          (= ref "Co-regions-west")
          (= ref "Ch-regions-cent")
          (= ref "Co-regions-cent")
          (= ref "Ch-regions-south")
          (= ref "Co-regions-south")
          )
    (do (-> (om/get-node owner (str ref "-content")) js/$ .fadeIn)
        (-> (om/get-node owner (str ref "-menu")) js/$ .fadeOut)
        (-> (om/get-node owner menu) js/$ .toggle)
        (send-command "system-msg" {:msg (str terrain " " region)}))
    (do (-> (om/get-node owner (str ref "-content")) js/$ .fadeIn)
        (-> (om/get-node owner (str ref "-menu")) js/$ .fadeOut)
        (-> (om/get-node owner menu) js/$ .toggle)
        (if (or (= ref "Ch-sites-north")
                (= ref "Co-sites-north")
                (= ref "Ch-sites-west")
                (= ref "Co-sites-west")
                (= ref "Ch-sites-cent")
                (= ref "Co-sites-cent")
                (= ref "Ch-sites-south")
                (= ref "Co-sites-south"))
          (send-command "view-location" {:region region :dc true})
          (send-command "view-location" {:region region :dc false})
          ))))


(defn show-map [event owner ref]
  (-> (om/get-node owner (str ref "-content")) js/$ .fadeIn)
  (-> (om/get-node owner (str ref "-menu")) js/$ .fadeOut))

(defn deck-view [{:keys [identity deck sideboard fw-dc-sb] :as cursor} owner]
  (om/component
    (sab/html
      (let [is-challenger (= "Challenger" (:side identity))
            side (if is-challenger :challenger :contestant)
            name (if is-challenger "Stack" "R&D")
            ref (if is-challenger "Ch-deck" "Ch-deck")
            menu-ref (str ref "-menu")
            content-ref (str ref "-content")
            deck-name (if is-challenger "Stack" "R&D")
            deck-ref (if is-challenger "stack" "rd")
            deck-menu-ref (str deck-ref "-menu")
            deck-content-ref (str deck-ref "-content")
            side-ref (if is-challenger "Ch-board" "Co-board")
            side-menu-ref (str side-ref "-menu")
            side-content-ref (str side-ref "-content")
            fwdc-ref (if is-challenger "Ch-dc-fw" "Co-dc-fw")
            fwdc-menu-ref (str fwdc-ref "-menu")
            fwdc-content-ref (str fwdc-ref "-content")]
        [:div.blue-shade.deck
         (drop-area (:side @game-state) deck-name
                    {:on-click #(-> (om/get-node owner menu-ref) js/$ .toggle)})
         (when (pos? (count deck))
           (facedown-card (:side identity) ["bg"] nil))
         (om/build label deck {:opts {:name "Play"}})
         (when (= (:side @game-state) side)
           [:div.panel.blue-shade.menu {:ref menu-ref}
            [:div {:on-click #(do (send-command "shuffle")
                                  (-> (om/get-node owner menu-ref) js/$ .fadeOut))} "Shuffle"]
            [:div {:on-click #(show-deck % owner deck-ref)} "Show Deck"]
            [:div {:on-click #(do (send-command "move-to-sb")
                                  (-> (om/get-node owner side-ref) js/$ .fadeOut))} "Move to SB"]
            [:div {:on-click #(show-sideboard % owner side-ref)} "Sideboard"]
            [:div {:on-click #(show-fw-dc-sb % owner fwdc-ref)} "FW-DC-SB"]])
         (when (= (:side @game-state) side)
           [:div.panel.blue-shade.popup {:ref deck-content-ref :style {:left -63}}
            [:div
             [:a {:on-click #(close-popup % owner deck-content-ref "stops looking at their deck" false false false false true)}
              "Close"]
             [:a {:on-click #(close-popup % owner deck-content-ref "" true false false false true)}
              "Close & Shuffle"]]
            (om/build-all card-view deck {:key :cid})])
         (when (= (:side @game-state) side)
           [:div.panel.blue-shade.popup {:ref side-content-ref :style {:left -63}}
            [:div
             [:a {:on-click #(close-popup % owner side-content-ref "stops looking at their sideboard" false false true false false)}
              "Close"]
             [:a {:on-click #(close-popup % owner side-content-ref "" true false true false false)}
              "Close & Shuffle"]]
            (om/build-all card-view sideboard {:key :cid})]
           )
         (when (= (:side @game-state) side)
           [:div.panel.blue-shade.popup {:ref fwdc-content-ref :style {:left -63}}
            [:div
             [:a {:on-click #(close-popup % owner fwdc-content-ref "stops looking at their sideboard" false false false true false)}
              "Close"]
             [:a {:on-click #(close-popup % owner fwdc-content-ref "" true false false true false)}
              "Close & Shuffle"]]
             (om/build-all card-view fw-dc-sb {:key :cid})]
           )
         ]))))

(defmulti discard-view #(get-in % [:identity :side]))

(defmethod discard-view "Challenger" [{:keys [discard locales] :as cursor} owner]
  (om/component
    (sab/html
      (let [faceup? #(or (:seen %) (:revealed %))
            draw-card #(if (faceup? %)
                         (om/build card-view %)
                         (if (or (= (:side @game-state) :challenger)
                                 (spectator-view-hidden?))
                           [:div.unseen (om/build card-view %)]
                           (facedown-card "challenger")))]
        [:div.blue-shade.discard
         (drop-area :challenger "Heap" {:on-click #(-> (om/get-node owner "popup") js/$ .fadeToggle)})

         (when-not (empty? discard) (draw-card (last discard)))

         (om/build label discard {:opts {:name "Discard"
                                         :fn (fn [cursor] (let [total (count cursor)
                                                                face-up (count (filter faceup? cursor))]
                                                            ;; use non-breaking space to keep counts on same line.
                                                            (str face-up "↑ " (- total face-up) "↓")))}})

         [:div.panel.blue-shade.popup {:ref "popup" :class (if (= (:side @game-state) :contestant) "opponent" "me")}
          [:div
           [:a {:on-click #(close-popup % owner "popup" nil false false false false false)} "Close"]
           [:label (let [total (count discard)
                         face-up (count (filter faceup? discard))]
                     (str total " cards, " (- total face-up) " face-down."))]]
          (for [c discard] (draw-card c))]]))))

(defmethod discard-view "Contestant" [{:keys [discard locales] :as cursor} owner]
  (om/component
    (sab/html
      (let [faceup? #(or (:seen %) (:revealed %))
            draw-card #(if (faceup? %)
                         (om/build card-view %)
                         (if (or (= (:side @game-state) :contestant)
                                 (spectator-view-hidden?))
                           [:div.unseen (om/build card-view %)]
                           (facedown-card "contestant")))]
        [:div.blue-shade.discard
         (drop-area :contestant "Archives" {:on-click #(-> (om/get-node owner "popup") js/$ .fadeToggle)})

         (when-not (empty? discard) (draw-card (last discard)))

         (om/build label discard {:opts {:name "Discard"
                                         :fn (fn [cursor] (let [total (count cursor)
                                                                face-up (count (filter faceup? cursor))]
                                                            ;; use non-breaking space to keep counts on same line.
                                                            (str face-up "↑ " (- total face-up) "↓")))}})

         [:div.panel.blue-shade.popup {:ref "popup" :class (if (= (:side @game-state) :challenger) "opponent" "me")}
          [:div
           [:a {:on-click #(close-popup % owner "popup" nil false false false false false)} "Close"]
           [:label (let [total (count discard)
                         face-up (count (filter faceup? discard))]
                     (str total " cards, " (- total face-up) " face-down."))]]
          (for [c discard] (draw-card c))]]))))

(defn rfg-view [{:keys [cards name popup] :as cursor} owner]
  (om/component
    (sab/html
      (when-not (empty? cards)
        (let [size (count cards)]
          [:div.panel.blue-shade.rfg {:class (when (> size 2) "squeeze")
                                      :on-click (when popup #(-> (om/get-node owner "rfg-popup") js/$ .fadeToggle))}
           (map-indexed (fn [i card]
                          [:div.card-wrapper {:style {:left (* (/ 128 size) i)}}
                           [:div (if (:hide card)
                                   (facedown-card "challenger")
                                   (om/build card-view card))]])
                        cards)
           (om/build label cards {:opts {:name name}})

           (when popup
             [:div.panel.blue-shade.popup {:ref "rfg-popup" :class "opponent"}
              [:div
               [:a {:on-click #(close-popup % owner "rfg-popup" nil false false false false false)} "Close"]
               [:label (str size " card" (when (not= 1 size) "s") ".")]]
              (for [c cards] (if (:hide c)
                               (facedown-card "challenger")
                               (om/build card-view c)))])])))))

(defn play-area-view [{:keys [name player popup] :as cursor} owner]
  (om/component
    (sab/html
      (let [cards (:play-area player)
            size (count cards)
            side (get-in player [:identity :side])]
        (when-not (empty? cards)
          [:div.panel.blue-shade.rfg {:class (when (> size 2) "squeeze")
                                      :on-click (when popup #(-> (om/get-node owner "rfg-popup") js/$ .fadeToggle))}
           (map-indexed (fn [i card]
                          [:div.card-wrapper {:style {:left (* (/ 128 size) i)}}
                           (om/build card-view card)])
                        cards)
           (om/build label cards {:opts {:name name}})
           (when popup
             [:div.panel.blue-shade.popup {:ref "rfg-popup" :class "opponent"}
              [:div
               [:a {:on-click #(close-popup % owner "rfg-popup" nil false false false false false)} "Close"]
               [:label (str size " card" (when (not= 1 size) "s") ".")]]
              (for [c cards] (if (:hide c)
                               (facedown-card "challenger")
                               (om/build card-view c)))])
           ])))))

(defn current-view [{:keys [name player] :as cursor}]
  (om/component
    (sab/html
      (let [cards (:current player)
            size (count cards)
            card-side (:side (first cards))
            side-swap (:swap (first cards))
            side (if side-swap
                   (if (= card-side "Contestant") :challenger :contestant)
                   (if (= card-side "Contestant") :contestant :challenger))]
        (when-not (empty? cards)
          [:div.panel.blue-shade.rfg {:class (when (> size 2) "squeeze")}
           (map-indexed (fn [i card]
                          [:div.card-wrapper {:style {:left (* (/ 128 size) i)}}
                           (if (= side (:side @game-state))
                             (om/build card-view card)
                             (facedown-card side))])
                        cards)
           (om/build label cards {:opts {:name name}})])))))

(defn score [cursor owner opts]
  (om/component
    (sab/html
      (let [fn (or (:fn opts) count)]
        [:div.header {:class "darkbg"}
         (str (:name opts))]))))

(defn scored-view [{:keys [scored] :as cursor}]
  (om/component
    (sab/html
      (let [size (count scored)]
        [:div.panel.blue-shade.scored.squeeze
         (map-indexed (fn [i card]
                        [:div
                        [:div.card-wrapper {:style {:left (* (/ 128 (dec size)) i)}}
                         (om/build card-view card)
                         (when (pos? (count (:hosted card)))
                           [:div.host-group
                            (host-view (:hosted card))])]])
                      scored)
         (om/build score scored {:opts {:name "Marshalling Point Pile (Out-of-play)"}})]))))

(defn controls [key]
  (sab/html
    [:div.controls
     [:button.small {:on-click #(send-command "change" {:key key :delta 1}) :type "button"} "+"]
     [:button.small {:on-click #(send-command "change" {:key key :delta -1}) :type "button"} "-"]]))

(defmulti stats-view #(get-in % [:identity :side]))

(defmethod stats-view "Challenger" [{:keys [identity user free_gi char_mp ally_mp item_mp fact_mp kill_mp misc_mp
                                            total_mp stage_pt hand-size-base hand-size-modification active]} owner]
  (om/component
    (sab/html
      (let [me? (= (:side @game-state) :challenger)]
        [:div.panel.blue-shade.stats {:class (when active "active-player")}
         [:h4.ellipsis (om/build avatar user {:opts {:size 22}}) (:username user)]
         [:div (str free_gi " Free G.I." (if (or (= (:alignment identity) "Minion")
                                                 (= (:alignment identity) "Balrog")
                                                 (= (:alignment identity) "War-lord")
                                                 (= (:alignment identity) "Dragon-lord"))
                                           " (5 minion)" "")) (when me? (controls :free_gi))]
         [:div (str stage_pt " Stage Point" (if (not= stage_pt 1) "s" "")) (when me? (controls :stage_pt))]
         [:div (str total_mp " Total MP" (if (not= total_mp 1) "s" "")) (when me? (controls :total_mp))]
         [:div (str char_mp " Character MP" (if (not= char_mp 1) "s" "")) (when me? (controls :char_mp))]
         [:div (str ally_mp " Ally MP" (if (not= ally_mp 1) "s" "")) (when me? (controls :ally_mp))]
         [:div (str item_mp " Item MP" (if (not= item_mp 1) "s" "")) (when me? (controls :item_mp))]
         [:div (str fact_mp " Faction MP" (if (not= fact_mp 1) "s" "")) (when me? (controls :fact_mp))]
         [:div (str kill_mp " Kill MP" (if (not= kill_mp 1) "s" "")) (when me? (controls :kill_mp))]
         [:div (str misc_mp " Misc MP" (if (not= misc_mp 1) "s" "")) (when me? (controls :misc_mp))]
         [:div (str (+ hand-size-base hand-size-modification) " Max hand size")
          (when me? (controls :hand-size-modification))]]))))

(defmethod stats-view "Contestant" [{:keys [identity user free_gi char_mp ally_mp item_mp fact_mp kill_mp misc_mp
                                            total_mp stage_pt hand-size-base hand-size-modification active]} owner]
  (om/component
    (sab/html
      (let [me? (= (:side @game-state) :contestant)]
        [:div.panel.blue-shade.stats {:class (when active "active-player")}
         [:h4.ellipsis (om/build avatar user {:opts {:size 22}}) (:username user)]
         [:div (str free_gi " Free G.I." (if (or (= (:alignment identity) "Minion")
                                                 (= (:alignment identity) "Balrog")
                                                 (= (:alignment identity) "War-lord")
                                                 (= (:alignment identity) "Dragon-lord"))
                                           " (5 minion)" "")) (when me? (controls :free_gi))]         [:div (str stage_pt " Stage Point" (if (not= stage_pt 1) "s" "")) (when me? (controls :stage_pt))]
         [:div (str total_mp " Total MP" (if (not= total_mp 1) "s" "")) (when me? (controls :total_mp))]
         [:div (str char_mp " Character MP" (if (not= char_mp 1) "s" "")) (when me? (controls :char_mp))]
         [:div (str ally_mp " Ally MP" (if (not= ally_mp 1) "s" "")) (when me? (controls :ally_mp))]
         [:div (str item_mp " Item MP" (if (not= item_mp 1) "s" "")) (when me? (controls :item_mp))]
         [:div (str fact_mp " Faction MP" (if (not= fact_mp 1) "s" "")) (when me? (controls :fact_mp))]
         [:div (str kill_mp " Kill MP" (if (not= kill_mp 1) "s" "")) (when me? (controls :kill_mp))]
         [:div (str misc_mp " Misc MP" (if (not= misc_mp 1) "s" "")) (when me? (controls :misc_mp))]
         [:div (str (+ hand-size-base hand-size-modification) " Max hand size")
          (when me? (controls :hand-size-modification))]]))))

(defn locale-view [{:keys [locale central-view run] :as cursor} owner opts]
  (om/component
    (sab/html
      (let [content (:content locale)]
        [:div.locale
         (let [characters (:characters locale)
               run-pos (:position run)
               current-character (when (and run (pos? run-pos) (<= run-pos (count characters)))
                                   (nth characters (dec run-pos)))
               run-arrow (sab/html [:div.run-arrow [:div]])
               tap-arrow (sab/html [:div.tap-arrow [:div]])
               max-hosted (apply max (map #(count (:hosted %)) characters))]
           [:div.characters
            (when-let [run-card (:card (:run-effect run))]
              [:div.run-card (om/build card-img run-card)])
            (for [character (reverse characters)]
              [:div
               [:div.character {:class (if (:tapped character)
                                         "tapped"
                                         (if (:wounded character)
                                           "wounded"
                                           nil))}
                (om/build card-view character {:opts {:flipped (not (:revealed character))}})
                (when (and current-character (= (:cid current-character) (:cid character)))
                  (if (:tapped character)
                    tap-arrow
                    run-arrow))]
               (when (pos? (count (:hosted character)))
                 [:div.host-group
                  (host-view (:hosted character))])
               ])
            (when (and run (not current-character)) run-arrow)
            ])
         [:div.content
          (when central-view
            central-view)
          (when (not-empty content)
            (for [card content
                  :let [is-first (= card (first content))]]
              [:div
               [:div.locale-card {:class (str (when (:tapped card) "tapped ")
                                              (when (or central-view
                                                        (and (< 1 (count content))
                                                             (not is-first)))
                                                "shift"))}
                (om/build card-view card {:opts {:flipped (not (:revealed card)) :location true}})
                (when (and (not central-view) is-first)
                  (om/build label-without content {:opts opts}))]
               (when (pos? (count (:hosted card)))
                 [:div.host-group
                  (host-view (:hosted card))])
               ]
              ))]]))))

(defn location-view [{:keys [identity location] :as cursor} owner]
  (om/component
    (sab/html
      (let [is-challenger (= "Challenger" (:side identity))
            side (if is-challenger :challenger :contestant)
            map-name (if is-challenger "Sites2" "Sites")
            map-ref (if is-challenger "Ch-map" "Co-map")
            map-menu-ref (str map-ref "-menu")
            map-content-ref (str map-ref "-content")
            site-name (if is-challenger "Location2" "Location")
            site-ref (if is-challenger "Ch-sites" "Co-sites")
            site-menu-ref (str site-ref "-menu")
            site-content-ref (str site-ref "-content")
            reg-name (if is-challenger "Regions2" "Regions")
            reg-ref (if is-challenger "Ch-regions" "Co-regions")
            reg-menu-ref (str reg-ref "-menu")
            reg-content-ref (str reg-ref "-content")
            ;;North
            map-name-notrth (if is-challenger "Sites2-north" "Sites-north")
            map-ref-north (if is-challenger "Ch-map-north" "Co-map-north")
            map-menu-ref-north (str map-ref-north "-menu")
            map-content-ref-north (str map-ref-north "-content")
            site-name-north (if is-challenger "Location2-north" "Location-north")
            site-ref-north (if is-challenger "Ch-sites-north" "Co-sites-north")
            site-menu-ref-north (str site-ref-north "-menu")
            site-content-ref-north (str site-ref-north "-content")
            reg-name-north (if is-challenger "Regions2-north" "Regions-north")
            reg-ref-north (if is-challenger "Ch-regions-north" "Co-regions-north")
            reg-menu-ref-north (str reg-ref-north "-menu")
            reg-content-ref-north (str reg-ref-north "-content")
            ;;West
            map-name-west (if is-challenger "Sites2-west" "Sites-west")
            map-ref-west (if is-challenger "Ch-map-west" "Co-map-west")
            map-menu-ref-west (str map-ref-west "-menu")
            map-content-ref-west (str map-ref-west "-content")
            site-name-west (if is-challenger "Location2-west" "Location-west")
            site-ref-west (if is-challenger "Ch-sites-west" "Co-sites-west")
            site-menu-ref-west (str site-ref-west "-menu")
            site-content-ref-west (str site-ref-west "-content")
            reg-name-west (if is-challenger "Regions2-west" "Regions-west")
            reg-ref-west (if is-challenger "Ch-regions-west" "Co-regions-west")
            reg-menu-ref-west (str reg-ref-west "-menu")
            reg-content-ref-west (str reg-ref-west "-content")
            ;;Central
            map-name-cent (if is-challenger "Sites2-cent" "Sites-cent")
            map-ref-cent (if is-challenger "Ch-map-cent" "Co-map-cent")
            map-menu-ref-cent (str map-ref-cent "-menu")
            map-content-ref-cent (str map-ref-cent "-content")
            site-name-cent (if is-challenger "Location2-cent" "Location-cent")
            site-ref-cent (if is-challenger "Ch-sites-cent" "Co-sites-cent")
            site-menu-ref-cent (str site-ref-cent "-menu")
            site-content-ref-cent (str site-ref-cent "-content")
            reg-name-cent (if is-challenger "Regions2-cent" "Regions-cent")
            reg-ref-cent (if is-challenger "Ch-regions-cent" "Co-regions-cent")
            reg-menu-ref-cent (str reg-ref-cent "-menu")
            reg-content-ref-cent (str reg-ref-cent "-content")
            ;;South
            map-name-south (if is-challenger "Sites2-south" "Sites-south")
            map-ref-south (if is-challenger "Ch-map-south" "Co-map-south")
            map-menu-ref-south (str map-ref-south "-menu")
            map-content-ref-south (str map-ref-south "-content")
            site-name-south (if is-challenger "Location2-south" "Location-south")
            site-ref-south (if is-challenger "Ch-sites-south" "Co-sites-south")
            site-menu-ref-south (str site-ref-south "-menu")
            site-content-ref-south (str site-ref-south "-content")
            reg-name-south (if is-challenger "Regions2-south" "Regions-south")
            reg-ref-south (if is-challenger "Ch-regions-south" "Co-regions-south")
            reg-menu-ref-south (str reg-ref-south "-menu")
            reg-content-ref-south (str reg-ref-south "-content")
            ]
        [:div.blue-shade.deck
         (drop-area (:side @game-state) map-name
                    {:on-click #(-> (om/get-node owner map-menu-ref) js/$ .toggle)})
         (facedown-card "Locations")
         (om/build label-without location {:opts {:name "Location"}})
         (when (= (:side @game-state) side)
              [:div.panel.blue-shade.menu {:ref map-menu-ref}
               [:div {:on-click #(show-map % owner reg-ref-south)} "South-regions"]
               [:div {:on-click #(show-map % owner reg-ref-cent)} "Central-regions"]
               [:div {:on-click #(show-map % owner reg-ref-west)} "West-regions"]
               [:div {:on-click #(show-map % owner reg-ref-north)} "North-regions"]
               [:div {:on-click #(show-map % owner reg-ref)} "Std-regions"]
               [:div {:on-click #(show-map % owner map-ref-south)} "South"]
               [:div {:on-click #(show-map % owner map-ref-cent)} "Central"]
               [:div {:on-click #(show-map % owner map-ref-west)} "West"]
               [:div {:on-click #(show-map % owner map-ref-north)} "North"]
               [:div {:on-click #(show-map % owner map-ref)} "Std-sites"]
               ])
         (when (= (:side @game-state) side)
           [:div.panel.blue-shade.popup {:ref reg-content-ref :style {:width 716}}
            [:div {:style {:width 716}}
             (standard-map show-sites owner reg-ref map-menu-ref)
             [:a {:on-click #(close-popup % owner reg-content-ref "" false true false false false)}
              "Close"]]
            ])
         [:div.panel.blue-shade.popup {:ref map-content-ref :style {:width 716}}
          [:div {:style {:width 716}}
           (standard-map show-sites owner site-ref map-menu-ref)
           [:a {:on-click #(close-popup % owner map-content-ref "" false true false false false)}
            "Close"]]
          ]
         [:div.panel.blue-shade.popup {:ref site-content-ref :style {:width 716}}
          [:div {:style {:width 716}}
           [:a {:on-click #(close-popup % owner site-content-ref "" false true false false false)}
            "Close"]]
          (om/build-all card-view location {:key :cid})
          ]
         (when (= (:side @game-state) side)
           [:div.panel.blue-shade.popup-map {:ref reg-content-ref-north}
            [:div
             (dreamcard-map-north show-sites owner reg-ref-north map-menu-ref-north)
             [:a {:on-click #(close-popup % owner reg-content-ref-north "" false true false false false)}
              "Close"]]
            ])
         [:div.panel.blue-shade.popup-map {:ref map-content-ref-north}
          [:div
           (dreamcard-map-north show-sites owner site-ref-north map-menu-ref-north)
           [:a {:on-click #(close-popup % owner map-content-ref-north "" false true false false false)}
            "Close"]]
          ]
         [:div.panel.blue-shade.popup-map {:ref site-content-ref-north}
          [:div
           [:a {:on-click #(close-popup % owner site-content-ref-north "" false true false false false)}
            "Close"]]
          (om/build-all card-view location {:key :cid})
          ]
         (when (= (:side @game-state) side)
           [:div.panel.blue-shade.popup-map {:ref reg-content-ref-west}
            [:div
             (dreamcard-map-west show-sites owner reg-ref-west map-menu-ref-west)
             [:a {:on-click #(close-popup % owner reg-content-ref-west "" false true false false false)}
              "Close"]]
            ])
         [:div.panel.blue-shade.popup-map {:ref map-content-ref-west}
          [:div
           (dreamcard-map-west show-sites owner site-ref-west map-menu-ref-west)
           [:a {:on-click #(close-popup % owner map-content-ref-west "" false true false false false)}
            "Close"]]
          ]
         [:div.panel.blue-shade.popup-map {:ref site-content-ref-west}
          [:div
           [:a {:on-click #(close-popup % owner site-content-ref-west "" false true false false false)}
            "Close"]]
          (om/build-all card-view location {:key :cid})
          ]
         (when (= (:side @game-state) side)
           [:div.panel.blue-shade.popup-map {:ref reg-content-ref-cent}
            [:div
             (dreamcard-map-central show-sites owner reg-ref-cent map-menu-ref-cent)
             [:a {:on-click #(close-popup % owner reg-content-ref-cent "" false true false false false)}
              "Close"]]
            ])
         [:div.panel.blue-shade.popup-map {:ref map-content-ref-cent}
          [:div
           (dreamcard-map-central show-sites owner site-ref-cent map-menu-ref-cent)
           [:a {:on-click #(close-popup % owner map-content-ref-cent "" false true false false false)}
            "Close"]]
          ]
         [:div.panel.blue-shade.popup-map {:ref site-content-ref-cent}
          [:div
           [:a {:on-click #(close-popup % owner site-content-ref-cent "" false true false false false)}
            "Close"]]
          (om/build-all card-view location {:key :cid})
          ]
         (when (= (:side @game-state) side)
           [:div.panel.blue-shade.popup-map {:ref reg-content-ref-south}
            [:div
             (dreamcard-map-south show-sites owner reg-ref-south map-menu-ref-south)
             [:a {:on-click #(close-popup % owner reg-content-ref-south "" false true false false false)}
              "Close"]]
            ])
         [:div.panel.blue-shade.popup-map {:ref map-content-ref-south}
          [:div
           (dreamcard-map-south show-sites owner site-ref-south map-menu-ref-south)
           [:a {:on-click #(close-popup % owner map-content-ref-south "" false true false false false)}
            "Close"]]
          ]
         [:div.panel.blue-shade.popup-map {:ref site-content-ref-south}
          [:div
           [:a {:on-click #(close-popup % owner site-content-ref-south "" false true false false false)}
            "Close"]]
          (om/build-all card-view location {:key :cid})
          ]
         ]))))

(defmulti decks-view #(get-in % [:player :identity :side]))

(defmethod decks-view "Contestant" [{:keys [player run]}]
  (om/component
    (sab/html
      (let [locales (:locales player)
            s (:locale run)
            locale-type (first s)]
        [:div.contestant-board {:class (if (= (:side @game-state) :challenger) "opponent" "me")}
         (om/build locale-view {:locale (:sites locales)
                                :central-view (om/build location-view player)
                                :run (when (= locale-type "hq") run)})
         (om/build locale-view {:locale (:rd locales)
                                :central-view (om/build deck-view player)
                                :run (when (= locale-type "rd") run)})
         (om/build locale-view {:locale (:archives locales)
                                :central-view (om/build discard-view player)
                                :run (when (= locale-type "archives") run)})]))))

(defmethod decks-view "Challenger" [{:keys [player run]}]
  (om/component
    (sab/html
      [:div.challenger-board {:class (if (= (:side @game-state) :contestant) "opponent" "me")}
       (om/build location-view player)
       (om/build deck-view player)
       (om/build discard-view player)])))

(defmulti resource-view #(get-in % [:player :identity :side]))

(defmethod resource-view "Contestant" [{:keys [player run]}]
  (om/component
    (sab/html
      (let [is-me (= (:side @game-state) :contestant)]
        [:div.contestant-rig {:class (if is-me "me" "opponent")}
         (for [zone [:resource :facedown]]
            (for [c (zone (:rig player))]
              [:div
               [:div.card-wrapper {:class (if (:tapped c)
                                            "tapped"
                                            (if (:inverted c)
                                              "inverted"
                                              (if (:rotated c)
                                                "rotated"
                                                nil)))}
                (om/build card-view c)]
               (when (pos? (count (:hosted c)))
                 [:div.host-group
                  (host-view (reverse (:hosted c)))])]
              ))
         ]))))

(defmethod resource-view "Challenger" [{:keys [player run]}]
  (om/component
    (sab/html
      (let [is-me (= (:side @game-state) :challenger)]
        [:div.challenger-rig {:class (if is-me "me" "opponent")}
         (for [zone [:resource :facedown]]
            (for [c (zone (:rig player))]
              [:div
               [:div.card-wrapper {:class (if (:tapped c)
                                            "tapped"
                                            (if (:inverted c)
                                              "inverted"
                                              (if (:rotated c)
                                                "rotated"
                                                nil)))}
                (om/build card-view c)]
               (when (pos? (count (:hosted c)))
                 [:div.host-group
                  (host-view (reverse (:hosted c)))])]
              ))
         ]))))

(defmulti hazard-view #(get-in % [:player :identity :side]))

(defmethod hazard-view "Contestant" [{:keys [player run]}]
  (om/component
    (sab/html
      (let [is-me (= (:side @game-state) :contestant)]
        [:div.contestant-rig {:class (if is-me "me" "opponent")}
         (for [zone [:hazard]]
           (for [c (zone (:rig player))]
             [:div
              [:div.card-wrapper {:class (if (:tapped c)
                                           "tapped"
                                           (if (:inverted c)
                                             "inverted"
                                             (if (:rotated c)
                                               "rotated"
                                               nil)))}
               (om/build card-view c)]
              (when (pos? (count (:hosted c)))
                [:div.host-group
                 (host-view (reverse (:hosted c)))])]
             ))
         ]))))

(defmethod hazard-view "Challenger" [{:keys [player run]}]
  (om/component
    (sab/html
      (let [is-me (= (:side @game-state) :challenger)]
        [:div.challenger-rig {:class (if is-me "me" "opponent")}
         (for [zone [:hazard]]
           (for [c (zone (:rig player))]
             [:div
              [:div.card-wrapper {:class (if (:tapped c)
                                           "tapped"
                                           (if (:inverted c)
                                             "inverted"
                                             (if (:rotated c)
                                               "rotated"
                                               nil)))}
               (om/build card-view c)]
              (when (pos? (count (:hosted c)))
                [:div.host-group
                 (host-view (reverse (:hosted c)))])]
             ))
         ]))))

(defmulti board-view #(get-in % [:player :identity :side]))

(defmethod board-view "Contestant" [{:keys [player run]}]
  (om/component
    (sab/html
      (let [locales (:locales player)
            s (:locale run)
            locale-type (first s)]
        [:div.contestant-board {:class (if (= (:side @game-state) :challenger) "opponent" "me")}
         (for [locale (reverse (get-parties locales))
               :let [num (party->num (first locale))]]
           (om/build locale-view {:locale (second locale)
                                  :run (when (= locale-type (str "party" num)) run)}
                     {:opts {:name (party->name (first locale))}}))]))))

(defmethod board-view "Challenger" [{:keys [player run]}]
  (om/component
    (sab/html
      (let [is-me (= (:side @game-state) :challenger)
            locales (:locales player)
            s (:locale run)
            locale-type (first s)]
        (if is-me
        [:div.challenger-board {:class (if (= (:side @game-state) :contestant) "opponent" "me")}
         (for [locale (reverse (get-parties locales))
               :let [num (party->num (first locale))]]
           (om/build locale-view {:locale (second locale)
                                  :run (when (= locale-type (str "party" num)) run)}
                     {:opts {:name (party->name (first locale))}}))]
        [:div.challenger-board.opponent {:class (if (= (:side @game-state) :contestant) "opponent" "me")}
         (for [locale (reverse (get-parties locales))
               :let [num (party->num (first locale))]]
           (om/build locale-view {:locale (second locale)
                                  :run (when (= locale-type (str "party" num)) run)}
                     {:opts {:name (party->name (first locale))}}))]
        )
        ))))

(defn cond-button [text cond f]
  (sab/html
    (if cond
      [:button {:on-click f} text]
      [:button.disabled text])))

(defn handle-end-of-phase [resolve]
  (let [me ((:side @game-state) @game-state)
        opp (if (= (:side @game-state) :contestant) (:challenger @game-state) (:contestant @game-state))
        max-size (max (+ (:hand-size-base me) (:hand-size-modification me)) 0)
        opp-max-size (max (+ (:hand-size-base opp) (:hand-size-modification opp)) 0)]
    (if (not= (count (:hand me)) max-size)
      (toast (str "Resolve hand to " max-size " card" (when (not= 1 max-size) "s")) "warning" nil)
      (if (and (not= (count (:hand opp)) opp-max-size)
               (not (empty? (get-in @game-state [:challenger :identity :title]))))
        (toast (str "Hazard player needs to get to " opp-max-size " card" (when (not= 1 opp-max-size) "s")) "warning" nil)
        (send-command resolve)))))

(defn handle-next-m-h [resolve]
  (let [me ((:side @game-state) @game-state)
        opp (if (= (:side @game-state) :contestant) (:challenger @game-state) (:contestant @game-state))
        max-size (max (+ (:hand-size-base me) (:hand-size-modification me)) 0)
        opp-max-size (max (+ (:hand-size-base opp) (:hand-size-modification opp)) 0)]
    (if (not= (count (:hand me)) max-size)
      (toast (str "Resolve hand to " max-size " card" (when (not= 1 max-size) "s")) "warning" nil)
        (send-command resolve))))

(defn handle-end-turn []
  (let [me ((:side @game-state) @game-state)
        opp (if (= (:side @game-state) :contestant) (:challenger @game-state) (:contestant @game-state))
        max-size (max (+ (:hand-size-base me) (:hand-size-modification me)) 0)
        opp-max-size (max (+ (:hand-size-base opp) (:hand-size-modification opp)) 0)]
    (if (and (not= (count (:hand opp)) opp-max-size)
             (not (empty? (get-in @game-state [:challenger :identity :title]))))
      (toast (str "Hazard player needs to get to " opp-max-size " card" (when (not= 1 opp-max-size) "s")) "warning" nil)
      (send-command "end-turn"))))

(defn runnable-locales
  "List of locales the challenger can run on."
  [contestant challenger]
  (let [locales (:locales contestant)
        restricted-locales [:hq :rd :archives :sites]]
    ;; remove restricted locales from all locales to just return allowed locales
    (apply dissoc locales restricted-locales)))

(defn button-pane [{:keys [side active-player run end-turn challenger-phase-12 contestant-phase-12 contestant challenger me opponent] :as cursor} owner]
  (reify
    om/IDidUpdate
    (did-update [this prev-props prev-state]
      (when-let [autocomp (get-in cursor [side :prompt 0 :choices :autocomplete])]
        (-> "#card-title" js/$ (.autocomplete (clj->js {"source" autocomp})))))

    om/IRenderState
    (render-state [this state]
      (sab/html
        [:div.button-pane {:on-mouse-over #(card-preview-mouse-over % zoom-channel)
                           :on-mouse-out  #(card-preview-mouse-out % zoom-channel)}
         (if-let [prompt (first (:prompt me))]
           [:div.panel.blue-shade
            [:h4 (for [item (get-message-parts (:msg prompt))] (create-span item))]
            (if-let [n (get-in prompt [:choices :number])]
              [:div
               [:div.credit-select
                [:select#credit {:default-value (get-in prompt [:choices :default] 0)}
                 (for [i (range (inc n))]
                   [:option {:value i} i])]]
               [:button {:on-click #(send-command "choice"
                                                  {:choice (-> "#credit" js/$ .val str->int)})}
                "OK"]]
              (cond
                ;; choice of number of credits
                (= (:choices prompt) "credit")
                [:div
                 (when-let [base (:base prompt)]
                   ;; This is the initial trace prompt
                   (if (nil? (:strength prompt))
                     (if (= "contestant" (:player prompt))
                       ;; This is a trace prompt for the contestant, show challenger link + credits
                       [:div.info "Challenger: " (:link challenger) [:span {:class "anr-icon link"}]
                        " + " (:credit challenger) [:span {:class "anr-icon credit"}]]
                       ;; Trace in which the challenger pays first, showing base trace strength and contestant credits
                       [:div.info "Trace: " (when (:bonus prompt) (+ base (:bonus prompt)) base)
                        " + " (:credit contestant) [:span {:class "anr-icon credit"}]])
                     ;; This is a trace prompt for the responder to the trace, show strength
                     (if (= "contestant" (:player prompt))
                       [:div.info "vs Trace: " (:strength prompt)]
                       [:div.info "vs Challenger: " (:strength prompt) [:span {:class "anr-icon link"}]])))
                 [:div.credit-select
                  ;; Inform user of base trace / link and any bonuses
                  (when-let [base (:base prompt)]
                    (if (nil? (:strength prompt))
                      (if (= "contestant" (:player prompt))
                        (let [strength (when (:bonus prompt) (+ base (:bonus prompt)) base)]
                          [:span (str strength " + ")])
                        [:span (:link challenger) " " [:span {:class "anr-icon link"}] (str " + " )])
                      (if (= "contestant" (:player prompt))
                        [:span (:link challenger) " " [:span {:class "anr-icon link"}] (str " + " )]
                        (let [strength (when (:bonus prompt) (+ base (:bonus prompt)) base)]
                          [:span (str strength " + ")]))))
                  [:select#credit (for [i (range (inc (:credit me)))]
                                    [:option {:value i} i])] " credits"]
                 [:button {:on-click #(send-command "choice"
                                                    {:choice (-> "#credit" js/$ .val str->int)})}
                  "OK"]]

                ;; auto-complete text box
                (:card-title (:choices prompt))
                [:div
                 [:div.credit-select
                  [:input#card-title {:placeholder "Enter a card title"
                                      :onKeyUp #(when (= 13 (.-keyCode %))
                                                  (-> "#card-submit" js/$ .click)
                                                  (.stopPropagation %))}]]
                 [:button#card-submit {:on-click #(send-command "choice" {:choice (-> "#card-title" js/$ .val)})}
                  "OK"]]

                ;; choice of specified counters on card
                (:counter (:choices prompt))
                (let [counter-type (keyword (:counter (:choices prompt)))
                      num-counters (get-in prompt [:card :counter counter-type] 0)]
                  [:div
                   [:div.credit-select
                    [:select#credit (for [i (range (inc num-counters))]
                                      [:option {:value i} i])] " credits"]
                   [:button {:on-click #(send-command "choice"
                                                      {:choice (-> "#credit" js/$ .val str->int)})}
                    "OK"]])
                ;; otherwise choice of all present choices
                :else
                (for [c (:choices prompt)]
                  (when (not= c "Hide")
                    (if (string? c)
                      [:button {:on-click #(send-command "choice" {:choice c})}
                       (for [item (get-message-parts c)] (create-span item))]
                      (let [[title code] (extract-card-info (add-image-codes (:title c)))]
                        [:button {:class (when (:rotated c) :rotated)
                                  :on-click #(send-command "choice" {:card @c}) :id code} title]))))))]
           (if run
             (let [s (:locale run)
                   n (:rerun run)
                   kw (keyword (first s))
                   locale (if-let [n (second s)]
                            (get-in contestant [:locales kw n])
                            (get-in contestant [:locales kw]))]

               [:div.panel.blue-shade
                (if (or (= (:click me) 85) (= (:click me) 80))
                  [:div
                   (cond-button "Next Character" (> n 1) #(send-command "continue"))
                   [:div.run-button
                    (cond-button "Other Locale" (and (pos? (:click me))
                                                     (not (get-in me [:register :cannot-run])))
                                 #(-> (om/get-node owner "locales") js/$ .toggle))
                    [:div.panel.blue-shade.locales-menu {:ref "locales"}
                     (map (fn [label]
                            [:div {:on-click #(do (send-command "run" {:locale label})
                                                  (-> (om/get-node owner "locales") js/$ .fadeOut))}
                             label])
                          (zones->sorted-names (if (= side :contestant)
                                                 (runnable-locales contestant challenger)
                                                 (runnable-locales challenger contestant))))]]
                   (cond-button "Roll Dice" true #(send-command "roll"))
                   (cond-button "Done Facing" (not (:cannot-jack-out run))
                                #(send-command "jack-out"))
                   ])

                (if (= (:click opponent) 85);; set to 45, by me
                  [:div
                   (cond-button "On-guard" (= (:click me) 45) #(send-command "on-guard")) ;; -5
                   (cond-button "No Hazards" (or (= (:click me) 40) (= (:click me) 45)) #(send-command "no-hazards"))
                   ;; set to 35
                   (cond-button "Draw Card" (not-empty (:deck me)) #(send-command "draw"))
                   (cond-button "Next M/H" (= (:click me) 35) #(handle-next-m-h "reset-m-h"))
                   ;; set to 45, by me
                   ]
                (if (= (:click opponent) 80) ;; set to 25, by me
                  [:div
                   ;;(when (> (:click me) 25) (send-command "reset-site"))
                   (cond-button "Reveal On-guard" (<= 21 (:click me) 25) #(send-command "reveal-o-g")) ;;-5

                   (cond
                     (= (:click me) 22)
                     (cond-button "Bluff On-guard" (= (:click me) 22) #(send-command "bluff-o-g")) ;;-2
                     :else
                     (cond-button "Possible Effect" (= (:click me) 25) #(send-command "pre-bluff")) ;;-3
                     )

                   (cond-button "Draw Card" (not-empty (:deck me)) #(send-command "draw"))
                   (cond-button "Next Site" (or (= (:click me) 20) (= (:click me) 25)) #(send-command "reset-site"))
                   ;; set to 25, by me
                   ]))
                ])
             (do ;// else form NOT RUN/Facing attack
               [:div.panel.blue-shade
                ;; --- Start Turn ---
                (if (and (not= (keyword active-player) side)
                         (zero? (:click me)) end-turn)
                    [:div
                     (cond-button "Keep Hand" (not (get-in @game-state [:contestant :keep])) #(send-command "keep"))
                     (cond-button "Draw Hand" (not (get-in @game-state [:contestant :keep])) #(send-command "mulligan"))
                     (cond
                       (and (get-in @game-state [:contestant :keep])
                            (get-in @game-state [:challenger :keep]))
                       (cond-button "Pass Turn" (and (get-in @game-state [:contestant :keep])
                                                     (get-in @game-state [:challenger :keep]))
                                    #(send-command "not-first"))
                       :else
                       (cond-button "Roll Dice" true #(send-command "roll"))
                       )
                     (cond-button "Start Turn" (and (get-in @game-state [:contestant :keep])
                                                    (or (get-in @game-state [:challenger :keep])
                                                        (empty? (get-in @game-state [:challenger :identity :title]))))
                                  #(send-command "start-turn")) ;; -5
                     ]
                  (if (and (zero? (:click opponent))
                           (zero? (:click me)))
                    [:div
                     (cond-button "Keep Hand" (not (get-in @game-state [:challenger :keep])) #(send-command "keep"))
                     (cond-button "Draw Hand" (not (get-in @game-state [:challenger :keep])) #(send-command "mulligan"))
                     (cond
                       (= (:click me) 100)
                       (cond-button "Pass Turn" (and (get-in @game-state [:contestant :keep])
                                                     (get-in @game-state [:challenger :keep]))
                                    #(send-command "not-first"))
                       :else
                       (cond-button "Roll Dice" true #(send-command "roll"))
                       )
                     (cond-button "Start Turn" nil nil)
                     ]
                    )
                  )
                (if (<= 90 (:click me) 100) ;; set to 100, opponent at 50
                  [:div
                   (cond
                     (= (:click me) 100)
                     (cond-button "Untap All" (= (:click me) 100) #(send-command "untap-all")) ;;-5
                     :else
                     (cond-button "Roll Dice" (= (:click me) 95) #(send-command "roll")) ;;-5
                     )
                   (cond-button "Organized" (= (:click me) 95) #(send-command "org-phase")) ;; -5
                   (cond-button "Draw Card" (not-empty (:deck me)) #(send-command "draw"))
                   (cond-button "M/H Phase" (= (:click me) 90) #(send-command "m-h-phase")) ;; -5
                   ]
                (if (= (:click me) 85)
                  [:div
                   [:button {:on-click #(send-command "back-org")} "Back to Organize"]
                   ;; set to 100, and opponent to 50
                   [:div.run-button
                    (cond-button "Face Attack(s)" (and (pos? (:click me))
                                                       (not (get-in me [:register :cannot-run])))
                                 #(-> (om/get-node owner "locales") js/$ .toggle))
                    [:div.panel.blue-shade.locales-menu {:ref "locales"}
                     (map (fn [label]
                            [:div {:on-click #(do (send-command "run" {:locale label})
                                                  (-> (om/get-node owner "locales") js/$ .fadeOut))}
                             label])
                          (zones->sorted-names (if (= side :contestant)
                                                 (runnable-locales contestant challenger)
                                                 (runnable-locales challenger contestant))))]]
                   (cond-button "Draw Card" (not-empty (:deck me)) #(send-command "draw"))
                   [:button {:on-click #(handle-end-of-phase "site-phase")} "Site Phase"] ;; -5
                   ]
                (if (= (:click me) 80)
                  [:div
                   [:button {:on-click #(send-command "back-m-h")} "Back to M/H"]
                   ;; set to 85, and opponent to 45
                   [:div.run-button
                    (cond-button "Face Attack(s)" (and (pos? (:click me))
                                                       (not (get-in me [:register :cannot-run])))
                                 #(-> (om/get-node owner "locales") js/$ .toggle))
                    [:div.panel.blue-shade.locales-menu {:ref "locales"}
                     (map (fn [label]
                            [:div {:on-click #(do (send-command "run" {:locale label})
                                                  (-> (om/get-node owner "locales") js/$ .fadeOut))}
                             label])
                          (zones->sorted-names (if (= side :contestant)
                                                 (runnable-locales contestant challenger)
                                                 (runnable-locales challenger contestant))))]]
                   (cond-button "Draw Card" (not-empty (:deck me)) #(send-command "draw"))
                   [:button {:on-click #(send-command "eot-phase")} "EOT Phase"]; -5
                   ]
                (if (< 65 (:click me) 80)
                  [:div
                   [:button {:on-click #(send-command "back-site")} "Back to Site"]
                   ;; set to 80, and opponent to 25
                   (cond-button "EOT Discard" (= (:click me) 75) #(handle-end-of-phase "eot-discard"));; -5
                   (cond-button "Draw Card" (not-empty (:deck me)) #(send-command "draw"))
                   (cond-button "EOT (1/2 HL?)" (and (= (:click me) 70)
                                                     (and (get-in @game-state [:contestant :eot])
                                                          (get-in @game-state [:challenger :eot]))
                                                     (= (keyword active-player) side) (not end-turn)
                                                     (not contestant-phase-12) (not challenger-phase-12)
                                                     (not (empty? (get-in @game-state [:challenger :identity :title])))
                                                     ) #(auto-save-send "end-turn"))]))))

                ;;------BREAK to Hazard Player

                (if (<= 90 (:click opponent) 100);; set to 50, by me
                  [:div
                   (cond-button "Roll Dice" true #(send-command "roll"))
                   (cond-button "Wait!!!" (zero? (:click me)) #(send-command "wait-alert"))
                   (cond-button "Draw Card" (not-empty (:deck me)) #(send-command "draw"))
                   (cond-button "Waiting" false nil)]
                (if (= (:click opponent) 85);; set to 45, by me
                  [:div
                   (cond-button "On-guard" (= (:click me) 45) #(send-command "on-guard")) ;; -5
                   (cond-button "No Hazards" (or (= (:click me) 40) (= (:click me) 45)) #(send-command "no-hazards"))
                   ;; set to 35
                   (cond-button "Draw Card" (not-empty (:deck me)) #(send-command "draw"))
                   (cond-button "Next M/H" (= (:click me) 35) #(handle-next-m-h "reset-m-h"))
                   ;; set to 45, by me
                   ]
                (if (= (:click opponent) 80) ;; set to 25, by me
                  [:div
                   ;;(when (> (:click me) 25) (send-command "reset-site"))
                   (cond-button "Reveal On-guard" (<= 21 (:click me) 25) #(send-command "reveal-o-g")) ;;-5

                   (cond
                     (= (:click me) 22)
                     (cond-button "Bluff On-guard" (= (:click me) 22) #(send-command "bluff-o-g")) ;;-2
                     :else
                     (cond-button "Possible Effect" (= (:click me) 25) #(send-command "pre-bluff")) ;;-3
                     )

                   (cond-button "Draw Card" (not-empty (:deck me)) #(send-command "draw"))
                   (cond-button "Next Site" (or (= (:click me) 20) (= (:click me) 25)) #(send-command "reset-site"))
                   ;; set to 25, by me
                   ]
                (if (< 65 (:click opponent) 80) ;; set to 0, by me
                  [:div
                   ;;(when (> (:click me) 0) (send-command "reset-done"))
                   (cond-button "EOT Phase" false nil)
                   [:button {:on-click #(send-command "return-o-g")} "Return On-guard"]
                   (cond-button "Draw Card" (not-empty (:deck me)) #(send-command "draw"))
                   (cond-button "Done" (zero? (:click me)) #(send-command "haz-play-done"))])))) ;;-5
                ])))]))))

(defn update-audio [{:keys [gameid sfx sfx-current-id] :as cursor} owner]
  ;; When it's the first game played with this state or when the sound history comes from different game, we skip the cacophony
  (let [sfx-last-played (om/get-state owner :sfx-last-played)]
    (when (and (get-in @app-state [:options :sounds])
               (not (nil? sfx-last-played))
               (= gameid (:gameid sfx-last-played)))
      ;; Skip the SFX from queue with id smaller than the one last played, queue the rest
      (let [sfx-to-play (reduce (fn [sfx-list {:keys [id name]}]
                                  (if (> id (:id sfx-last-played))
                                    (conj sfx-list name)
                                    sfx-list)) [] sfx)]
        (play-sfx sfx-to-play (om/get-state owner :soundbank)))))
  ;; Remember the most recent sfx id as last played so we don't repeat it later
  (when sfx-current-id
    (om/set-state! owner :sfx-last-played {:gameid gameid :id sfx-current-id})))

(def contestant-stats
  (let [s #(-> @game-state :contestant)]
    [["Character MPs" #(-> (s) :char_mp)]
     ["Ally MPs" #(-> (s) :ally_mp)]
     ["Item MPs" #(-> (s) :item_mp)]
     ["Faction MPs" #(-> (s) :fact_mp)]
     ["Kill MPs" #(-> (s) :kill_mp)]
     ["Misc MPs" #(-> (s) :misc_mp)]
     ["Total MPs" #(-> (s) :total_mp)]]))

(def challenger-stats
  (let [s #(-> @game-state :challenger)]
    [["Character MPs" #(-> (s) :char_mp)]
     ["Ally MPs" #(-> (s) :ally_mp)]
     ["Item MPs" #(-> (s) :item_mp)]
     ["Faction MPs" #(-> (s) :fact_mp)]
     ["Kill MPs" #(-> (s) :kill_mp)]
     ["Misc MPs" #(-> (s) :misc_mp)]
     ["Total MPs" #(-> (s) :total_mp)]]))

(defn show-stat
  "Determines statistic counter and if it should be shown"
  [side]
  (when-let [stat-fn (-> side second)]
    (let [stat (stat-fn)]
      (if (pos? stat) stat "-"))))

(defn build-game-stats
  "Builds the end of game statistics div & table"
  []
  (let [stats (map-longest list nil contestant-stats challenger-stats)]
    [:div
     [:table.win.table
      [:tr.win.th
       [:td.win.th (get-in @game-state [:contestant :identity :title])] [:td.win.th]
       [:td.win.th (get-in @game-state [:challenger :identity :title])] [:td.win.th]]
      (for [[contestant challenger] stats]
        [:tr [:td (first contestant)] [:td (show-stat contestant)]
         [:td (first challenger)] [:td (show-stat challenger)]])]]))

(defn build-win-box
  "Builds the end of game pop up game end"
  [game-state]
  [:div.win.centered.blue-shade
   [:div
    (:winning-user @game-state) ;" (" (-> @game-state :winner capitalize)
    (cond
      (= "Concede" (@game-state :reason capitalize))
      (str " wins by concession on turn " (:turn @game-state))

      :else
      (str " wins by scoring more MPs on turn "  (:turn @game-state)))]
   ;[:div "Time taken: " (-> @game-state :stats :time :elapsed) " minutes"]
   [:br]
   (build-game-stats)
   [:button.win-right {:on-click #(swap! app-state assoc :win-shown true) :type "button"} "✘"]])

(defn gameboard [{:keys [side active-player run end-turn challenger-phase-12 contestant-phase-12 turn contestant challenger] :as cursor} owner]
  (reify
    om/IInitState
    (init-state [this]
      (let [audio-sfx (fn [name] (list (keyword name)
                                       (new js/Howl (clj->js {:src [(str "/sound/" name ".ogg")
                                                                    (str "/sound/" name ".mp3")]}))))]
        {:soundbank
         (apply hash-map (concat
                           (audio-sfx "agenda-score")
                           (audio-sfx "agenda-steal")
                           (audio-sfx "click-advance")
                           (audio-sfx "click-card")
                           (audio-sfx "click-run")
                           (audio-sfx "click-remove-tag")
                           (audio-sfx "game-end")
                           (audio-sfx "place-contestant")
                           (audio-sfx "place-challenger")
                           (audio-sfx "play-instant")
                           (audio-sfx "reveal-character")
                           (audio-sfx "reveal-other")
                           (audio-sfx "run-successful")
                           (audio-sfx "run-unsuccessful")
                           (audio-sfx "virus-purge")))}))

    om/IWillMount
    (will-mount [this]
      (go (while true
            (let [card (<! zoom-channel)]
              (-> ".direct" js/$ .focus)
              (om/set-state! owner :zoom card)
              ;(if card (send-command "blind-hold" {:card (:ImageName card)}))
              ))))

    om/IDidUpdate
    (did-update [this prev-props prev-state]
      (when (get-in cursor [side :prompt 0 :show-discard])
        (-> ".me .discard .popup" js/$ .fadeIn))
      (if (= "select" (get-in cursor [side :prompt 0 :prompt-type]))
        (set! (.-cursor (.-style (.-body js/document))) "url('/img/gold_crosshair.png') 12 12, crosshair")
        (set! (.-cursor (.-style (.-body js/document))) "default"))
      (when (= "card-title" (get-in cursor [side :prompt 0 :prompt-type]))
        (-> "#card-title" js/$ .focus))
      (doseq [{:keys [msg type options]} (get-in cursor [side :toast])]
        (toast msg type options))
      (update-audio cursor owner))

    om/IRenderState
    (render-state [this state]
      (sab/html
        (when side
          (let [me       (assoc ((if (= side :challenger) :challenger :contestant) cursor) :active (and (pos? turn) (= (keyword active-player) side)))
                opponent (assoc ((if (= side :challenger) :contestant :challenger) cursor) :active (and (pos? turn) (not= (keyword active-player) side)))]

            [:div.gameboard
             (when (and (:winner @game-state) (not (:win-shown @app-state)))
               (build-win-box game-state))
             [:div {:class (:background (:options @app-state))}]
             [:div.rightpane
              [:div.card-zoom
               (if-let [card (om/get-state owner :zoom)]
                 (if (or (= side :spectator)
                         (get-in @game-state [side :blind]))
                   (om/build card-blind card)
                   (om/build card-zoom card)))
               (when-let [card (om/get-state owner :zoom)]
                 (when (get-in @game-state [side :hold-card])
                   (send-command "blind-send" {:msg (:ImageName card)})))
               (when-let [card (om/get-state owner :zoom)]
                 (when (get-in @game-state [side :fn12-key])
                   (send-command "f1-f12-key-down")))
               (when-let [card (om/get-state owner :zoom)]
                 (when (get-in @game-state [side :opt-key])
                   (send-command "option-key-down" {:msg (str (:ImageName card)
                                                              (cond
                                                                (:tapped card) " auto tapped"
                                                                (:wounded card) " auto wounded"
                                                                :else " auto no-tap")
                                                              )})))
                (when-let [card (om/get-state owner :zoom)]
                 (when (get-in @game-state [side :bonus-key])
                   (send-command "bonus-key-down" {:msg (str (:ImageName card)
                                                              (cond
                                                                (:tapped card) " tapped +1 support"
                                                                :else " going to +1 support")
                                                              )})))
               (when-let [card (om/get-state owner :zoom)]
                 (when (get-in @game-state [side :minus-key])
                   (send-command "minus-key-down" {:msg (str (:ImageName card) " assigned a -1")})))
               ]
              ;; card implementation info
              (when-let [card (om/get-state owner :zoom)]
                (let [implemented (:implementation card)]
                  (case implemented
                    (:full "full") nil
                    [:div.panel.blue-shade.implementation
                     (case implemented
                       nil [:span.unimplemented "Unimplemented"]
                       [:span.impl-msg implemented])])))
              (om/build log-pane cursor)]

             [:div.centralpane
              (om/build board-view {:player opponent :run run})
              (om/build resource-view {:player opponent :run run})
              (om/build hazard-view {:player opponent :run run})
              (om/build hazard-view {:player me :run run})
              (om/build resource-view {:player me :run run})
              (om/build board-view {:player me :run run})]

             [:div.leftpane
              [:div.opponent
               (if (= side :spectator)
                 (om/build hand-view {:player opponent :parties (get-parties (:locales (if (= side :challenger) contestant challenger)))
                                      :popup (= side :spectator) :popup-direction "opponent"})
                 (om/build site-view {:player opponent :parties (get-parties (:locales (if (= side :challenger) contestant challenger)))
                                      :popup (= side :spectator) :popup-direction "opponent"}))]


              [:div.inner-leftpane
               [:div.left-inner-leftpane
                [:div
                 (om/build scored-view opponent)
                 (om/build stats-view opponent)
                 ]
                [:div
                 (om/build stats-view me)
                 (om/build scored-view me)]
                ]

               [:div.right-inner-leftpane
                [:div
                 (om/build decks-view {:player opponent :run run})
                 (om/build rfg-view {:cards (:rfg opponent) :name "Removed from play/game" :popup true})
                 (om/build rfg-view {:cards (:rfg me) :name "Removed from play/game" :popup true})
                 (om/build play-area-view {:player opponent :name "Temporary Zone" :popup true})
                 (om/build play-area-view {:player me :name "Temporary Zone" :popup true})
                 (om/build current-view {:player opponent :name "Waiting..."})
                 (om/build current-view {:player me :name "Deciding..."})]
                [:div
                 (when-not (= side :spectator)
                   (om/build button-pane {:side side :active-player active-player :run run :end-turn end-turn :challenger-phase-12 challenger-phase-12 :contestant-phase-12 contestant-phase-12 :contestant contestant :challenger challenger :me me :opponent opponent}))
                 (om/build decks-view {:player me :run run})]
                ]
               ]

              [:div.me
               (om/build hand-view {:player me :parties (get-parties (:locales (if (= side :challenger) challenger contestant)))
                                    :popup true :popup-direction "me"})]]]

            ))))))

(om/root gameboard game-state {:target (. js/document (getElementById "gameboard"))})
