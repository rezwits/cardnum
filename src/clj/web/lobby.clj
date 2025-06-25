(ns web.lobby
  (:require [web.db :refer [db object-id]]
            [web.utils :refer [proj-dir map-values my-value-reader response tick remove-once]]
            [clojure.string :refer [split split-lines join escape] :as s]
            [clojure.java.io :as io]
            [web.ws :as ws]
            [web.sites :refer [standard-wizard-sites standard-minion-sites
                               standard-fallen-sites standard-option-sites
                               standard-balrog-sites
                               standard-metw-sites-only
                               standard-metd-sites-only
                               standard-medm-sites-only
                               standard-meas-sites-only
                               dreamcard-wizard-sites dreamcard-minion-sites
                               dreamcard-fallen-sites dreamcard-option-sites
                               dreamcard-balrog-sites dreamcard-dwarf-sites
                               dreamcard-elf-sites dreamcard-atani-sites
                               dreamcard-dragon-sites dreamcard-warlord-sites]]
            [web.stats :as stats]
            [game.core :as core]
            [crypto.password.bcrypt :as bcrypt]
            [monger.collection :as mc]
            [cardnum.cards :refer [all-cards]]
            [cardnum.decks :as decks]
            [cardnum.utils :refer [str->int parse-deck-string INFINITY] :as utils]
            [cheshire.core :as json]
            [clojure.data.json :as j-data]
            [monger.json]
            [clj-time.core :as t])
  (:import org.bson.types.ObjectId))

;; All games active on the server.
(defonce all-games (atom {}))

;; The most recent state of each game sent to clients.
(defonce old-states (atom {}))

;; A map from client-id to gameid of the game the client is playing or spectating.
(defonce client-gameids (atom {}))

(defn game-for-id
  "Returns the game map for the given gameid."
  [gameid]
  (get @all-games gameid))

(defn game-for-client
  "Returns the game map that the given client-id is playing or spectating."
  [client-id]
  (get @all-games (get @client-gameids client-id)))

(defn lobby-clients
  "Returns a seq of all client-ids playing or spectating a gameid."
  [gameid]
  (let [game (game-for-id gameid)]
    (map :ws-id (concat (:players game) (:spectators game)))))

(defn user-public-view
  "Strips private server information from a player map."
  [started? player]
  (as-> player p
        (dissoc p :ws-id)
        (if-let [{:keys [_id] :as deck} (:deck p)]
          (assoc p :deck (select-keys (assoc deck :_id (str _id))
                                      (if started?
                                        [:_id :status :name :identity]
                                        [:_id :status :name])))
          p)))

(defn game-public-view
  "Strips private server information from a game map, preparing to send the game to clients."
  [{:keys [started] :as game}]
  (-> game
      (dissoc :state :last-update)
      (update-in [:players] #(map (partial user-public-view started) %))
      (update-in [:original-players] #(map (partial user-public-view started) %))
      (update-in [:ending-players] #(map (partial user-public-view started) %))
      (update-in [:spectators] #(map (partial user-public-view started) %))))

(let [lobby-update (atom true)
      lobby-updates (atom {})]

  (defn refresh-lobby
    "Schedules the given gameid to be included in the next push of game lobby updates.
    type is :create, :delete, or :update"
    [type gameid]
    (reset! lobby-update true)
    (swap! lobby-updates assoc-in [type gameid]
           (if (= type :delete)
             "0"
             (game-public-view (game-for-id gameid)))))

  (defn send-lobby
    "Called by a background thread to periodically send game lobby updates to all clients."
    []
    (when @lobby-update
      (reset! lobby-update false)
      (let [[old _] (reset-vals! lobby-updates {})]
        (ws/broadcast! :games/diff {:diff old})))))

(defn player?
  "True if the given client-id is a player in the given gameid"
  [client-id gameid]
  (when-let [game (game-for-id gameid)]
    (some #(when (= client-id (:ws-id %)) %) (:players game))))

(defn first-player?
  "True if the given client-id is the first player in the given gameid"
  [client-id gameid]
  (when-let [game (game-for-id gameid)]
    (= client-id (-> game :players first :ws-id))))

(defn spectator?
  "True if the given client-id is a spectator in the given gameid"
  [client-id gameid]
  (when-let [game (game-for-id gameid)]
    (some #(when (= client-id (:ws-id %)) %) (:spectators game))))

(defn player-or-spectator
  "True if the given client-id is a player or spectator in the given gameid"
  [client-id gameid]
  (when-let [game (game-for-id gameid)]
    (or (player? client-id gameid)
        (spectator? client-id gameid))))

(defn close-lobby
  "Closes the given game lobby, booting all players and updating stats."
  [{:keys [started gameid] :as game}]
  (when started
    (stats/update-deck-stats all-games gameid)
    (stats/update-game-stats all-games gameid)
    (stats/push-stats-update all-games gameid))

  (refresh-lobby :delete gameid)
  (swap! all-games dissoc gameid)
  (swap! old-states dissoc gameid))

(defn clear-inactive-lobbies
  "Called by a background thread to close lobbies that are inactive for some number of seconds."
  [time-inactive]
  (doseq [{:keys [gameid last-update started] :as game} (vals @all-games)]
    (when (and gameid (t/after? (t/now) (t/plus last-update (t/seconds time-inactive))))
      (let [clientids (lobby-clients gameid)]
        (if started
          (do (stats/game-finished game)
            (ws/broadcast-to! clientids :meccg/timeout (json/generate-string
                                                        {:gameid gameid})))
          (ws/broadcast-to! clientids :lobby/timeout {:gameid gameid}))
        (doseq [client-id clientids]
          (swap! client-gameids dissoc client-id))
        (close-lobby game)))))

(defn remove-user
  "Removes the given client-id from the given gameid, whether it is a player or a spectator.
  Deletes the game from the lobby if all players have left."
  [client-id gameid]
  (when-let [{:keys [players started state] :as game} (game-for-id gameid)]
    (cond (player? client-id gameid)
      (swap! all-games update-in [gameid :players] #(remove-once (fn [p] (= client-id (:ws-id p))) %))

      (spectator? client-id gameid)
      (swap! all-games update-in [gameid :spectators] #(remove-once (fn [p] (= client-id (:ws-id p))) %)))

    ;; update ending-players when someone drops to credit a completion properly.  Not if game is over.
    ; TODO add other player back in if other player rejoins

    (when state
      (let [winner (:winning-user @state)]
        (when (and (= 1 (count players)) started (not winner))
          (swap! all-games assoc-in [gameid :ending-players] players))))

    (let [{:keys [players] :as game} (game-for-id gameid)]
      (swap! client-gameids dissoc client-id)

      (if (empty? players)
        (do
          (stats/game-finished game)
          (close-lobby game))
        (refresh-lobby :update gameid)))))

(defn join-game
  "Adds the given user as a player in the given gameid."
  [{options :options :as user} client-id gameid alignment]
  (let [{players :players :as game} (game-for-id gameid)]
    (when (< (count players) 2)
      (let [{side :side :as fplayer} (first players)
            new-side (if (= "Contestant" side) "Challenger" "Contestant")
            new-player {:user      user
                        :ws-id     client-id
                        :side      new-side
                        :alignment alignment
                        :options   options}]
        (swap! all-games update-in [gameid :players] #(conj % new-player))
        (swap! client-gameids assoc client-id gameid)
        (refresh-lobby :update gameid)
        new-player))))

(defn spectate-game
  "Adds the given user as a spectator in the given gameid"
  [user client-id gameid]
  (when-let [{:keys [started spectators] :as game} (game-for-id gameid)]
    (swap! all-games update-in [gameid :spectators]
           #(conj % {:user  user
                     :ws-id client-id}))
    (swap! client-gameids assoc client-id gameid)
    (refresh-lobby :update gameid)))

(defn swap-side
  "Returns a new player map with the player's :side switched"
  [player]
  (-> player
      (update-in [:side] #(if (= % "Contestant")
                           "Challenger"
                           "Contestant"))
      (dissoc :deck)))

(defn blocked-users
  [{:keys [players] :as game}]
  (mapcat #(get-in % [:user :options :blocked-users]) players))

(defn allowed-in-game [{:keys [username]} game]
  (not (some #(= username (:username %)) (blocked-users game))))

(defn handle-ws-connect [{:keys [client-id] :as msg}]
  (ws/send! client-id [:games/list (mapv game-public-view (vals @all-games))]))

(defn handle-lobby-create
  [{{{:keys [username emailhash] :as user} :user} :ring-req
    client-id                                     :client-id
    {:keys [title use-dce use-ice eot-auto-save allowspectator spectatorhands
            metw-site-only metd-site-only medm-site-only meas-site-only
            password room side alignment hero standard dreamcard options]} :?data :as event}]
  (let [gameid (java.util.UUID/randomUUID)
        game {:date            (java.util.Date.)
              :gameid          gameid
              :title           title
              :use-dce         use-dce
              :use-ice         use-ice
              :eot-auto-save   eot-auto-save
              :metw-site-only  metw-site-only
              :allowspectator  allowspectator
              :spectatorhands  spectatorhands
              :mute-spectators false
              :password        (when (not-empty password) (bcrypt/encrypt password))
              :room            room
              :players         [{:user      user
                                 :ws-id     client-id
                                 :side      side
                                 :alignment alignment
                                 :hero      hero
                                 :standard  standard
                                 :dreamcard dreamcard
                                 :options   options}]
              :spectators      []
              :last-update     (t/now)
              }]

    (when (not (.exists (io/file (str proj-dir "/all-pre-g/" username))))
      (do
        (.mkdir (io/file (io/file (str proj-dir "/all-pre-g/" username))))
        (.mkdir (io/file (io/file (str proj-dir "/all-saves/" username))))
        (.mkdir (io/file (io/file (str proj-dir "/all-states/" username))))
        ))

    (spit (str "all-pre-g/" username "/" username "'s game.json")
          (json/generate-string
           game
           {:pretty true}))
    (swap! all-games assoc gameid game)
    (swap! client-gameids assoc client-id gameid)
    (ws/send! client-id [:lobby/select {:gameid gameid}])
    (refresh-lobby :create gameid)))

(defn handle-lobby-loader
  [{{{:keys [username emailhash] :as user} :user} :ring-req
    client-id                                     :client-id
    {:keys [game-save]} :?data :as event}]
  (let [load (j-data/read-str (slurp (str "all-saves/" username "/" game-save "g.json")) :key-fn keyword)
        gameid (java.util.UUID/fromString (str (:gameid load)))
        game {:date            (:date load)
              :gameid          gameid
              :title           (:title load)
              :resumed         true
              :use-dce         (:use-dce load)
              :use-ice         (:use-ice load)
              :eot-auto-save   (:eot-auto-save load)
              :metw-site-only  (:metw-site-only load)
              :metd-site-only  (:metd-site-only load)
              :medm-site-only  (:medm-site-only load)
              :meas-site-only  (:meas-site-only load)
              :allowspectator  (:allowspectator load)
              :spectatorhands  (:spectatorhands load)
              :mute-spectators false
              :password        (:password load)
              :room            (:room load)
              :reserve1        (:id_usern1 load)
              :reserve2        (:id_usern2 load)
              :players         [{:user      user
                                 :ws-id     client-id
                                 :side      (if (= (:id_usern1 load) username)
                                              "Contestant"
                                              "Challenger")
                                 :alignment (if (= (:id_usern1 load) username)
                                              (:id_align1 load)
                                              (:id_align2 load))
                                 :options   (:options load)}]
              :spectators      []
              :last-update     (:last-update load)}]
    (swap! all-games assoc gameid game)
    (swap! client-gameids assoc client-id gameid)
    (ws/send! client-id [:lobby/relay {:save game-save}])
    (ws/send! client-id [:lobby/select {:gameid gameid}])
    (refresh-lobby :create gameid)
    ))

(defn handle-lobby-delete
  [{{{:keys [username emailhash] :as user} :user} :ring-req
    client-id                                     :client-id
    {:keys [game-save]} :?data :as event}]
  (when (and (.exists (io/file (str proj-dir "/all-saves/" username "/" game-save "g.json" )))
             (.exists (io/file (str proj-dir "/all-states/" username "/" game-save "s.json" ))))
    (do
      (io/delete-file (str proj-dir "/all-saves/" username "/" game-save "g.json" ))
      (io/delete-file (str proj-dir "/all-states/" username "/" game-save "s.json")))
    ))

(defn handle-lobby-leave
  [{{{:keys [username emailhash] :as user} :user} :ring-req
    client-id                                     :client-id}]
  (when-let [{gameid :gameid} (game-for-client client-id)]
    (when (player-or-spectator client-id gameid)
      (when (.exists (io/file (str proj-dir "/all-pre-g/" username "/" username "'s game.json")))
        (io/delete-file (str proj-dir "/all-pre-g/" username "/" username "'s game.json")))
      (remove-user client-id gameid)
      (ws/broadcast-to! (lobby-clients gameid)
                        :lobby/message
                        {:user "__system__"
                         :text (str username " left the game.")}))))

(defn handle-lobby-say
  [{{{:keys [username] :as user} :user} :ring-req
    client-id                           :client-id
    {:keys [msg gameid]}                :?data}]
  (when (player-or-spectator client-id gameid)
    (let [game (game-for-id gameid)]
      (ws/broadcast-to!
       (map :ws-id (concat (:players game) (:spectators game)))
       :lobby/message
       {:user user
        :text msg}))))

(defn handle-swap-sides
  [{{{:keys [username] :as user} :user} :ring-req
    client-id                           :client-id
    gameid                              :?data}]
  (let [game (game-for-id gameid)
        fplayer (first (:players game))]
    (when (= (:ws-id fplayer) client-id)
      (swap! all-games update-in [gameid :players] (partial mapv swap-side))
      (refresh-lobby :update gameid)
      (ws/broadcast-to! (lobby-clients gameid)
                        :games/diff
                        {:diff {:update {gameid (game-public-view (game-for-id gameid))}}}))))

(defn already-in-game?
  "Checks if a user with the given database id (:_id) is already in the game"
  [{:keys [_id] :as user} {:keys [players spectators] :as game}]
  (or (some #(= _id (:_id %)) (map :user players))
      (some #(= _id (:_id %)) (map :user spectators))))

(defn handle-lobby-join
  [{{{:keys [username] :as user} :user} :ring-req
    client-id                           :client-id
    {:keys [gameid alignment
            password options]}          :?data
    reply-fn                            :?reply-fn
    :as                                 msg}]
  (if-let [{game-password :password :as game} (@all-games gameid)]
    (when (and user game (allowed-in-game game user))
      (if (and (not (already-in-game? user game))
               (or (empty? game-password)
                   (bcrypt/check password game-password)))
        (do (join-game user client-id gameid alignment)
          (ws/broadcast-to! (lobby-clients gameid)
                            :lobby/message
                            {:user         "__system__"
                             :notification "ting"
                             :text         (str username " joined the game.")})
          (ws/send! client-id [:lobby/select {:gameid gameid}])
          (when reply-fn (reply-fn 200)))
        (when reply-fn (reply-fn 403))))
    (when reply-fn (reply-fn 404))))

(defn handle-lobby-watch
  "Handles a watch command when a game has not yet started."
  [{{{:keys [username] :as user} :user} :ring-req
    client-id                           :client-id
    {:keys [gameid password options]}   :?data
    reply-fn                            :?reply-fn}]
  (if-let [{game-password :password state :state started :started :as game}
           (@all-games gameid)]
    (when (and user game (allowed-in-game game user))
      (if started
        false                                               ; don't handle this message, let game/handle-game-watch.
        (if (and (not (already-in-game? user game))
                 (or (empty? game-password)
                     (bcrypt/check password game-password)))
          (do (spectate-game user client-id gameid)

            (ws/broadcast-to! (lobby-clients gameid)
                              :lobby/message
                              {:user         "__system__"
                               :notification "ting"
                               :text         (str username " joined the game as a spectator.")})
            (ws/send! client-id [:lobby/select {:gameid gameid :started started}])
            (when reply-fn (reply-fn 200))
            true)
          (when reply-fn
            (reply-fn 403)
            false))))
    (when reply-fn
      (reply-fn 404)
      false)))

(defn align-match
  [align code hero-go minion-go fw-go]
  (cond
    (= align code) true
    (and (= align "Hero") (= code "Hero/Any")) true
    (and (= align "Fallen-wizard") (= code "Fallen-wizard/Lords")) true
    (and (= align "Balrog") (= code "Minion")) true
    (and (= align "War-lord") (= code "Minion")) true
    (and minion-go (= align "Dragon-lord") (= code "Minion")) true
    (and fw-go (or (= align "Elf-lord")
                   (= align "Dwarf-lord")
                   (= align "Atani-lord"))
         (= code "Fallen-wizard/Lords")) true
    (and hero-go (= code "Hero/Any")) true
    :else false))

(defn handle-select-deck
  [{{{:keys [username] :as user} :user} :ring-req
    client-id                           :client-id
    {:keys [deck-id match-code]}       :?data}]
  (let [game (game-for-client client-id)
        metws (:metw-site-only game)
        metds (:metd-site-only game)
        medms (:medm-site-only game)
        meass (:meas-site-only game)
        fplayer (first (:players game))
        opp-align (if (> (count (:players game)) 1)
                    (:alignment (if (= client-id (:ws-id fplayer))
                                  (last (:players game))
                                  fplayer))
                    "Practice")
        gameid (:gameid game)

        get-code (fn [c] (if (nil? (:id c))
                           c
                           (assoc c :code (str (:card c) " " (:id c)))))
        get-data (fn [c] (if (nil? (:id c))
                           (update-in c [:card] @all-cards)
                           (update-in c [:code] @all-cards)))
        get-swap (fn [c] (if (nil? (:id c))
                           c
                           (do
                             (dissoc c :card)
                             (assoc c :card (:code c)))))
        rid-code (fn [c] (if (nil? (:id c))
                           c
                           (dissoc c :code :id)))
        rid-card (fn [c] (nil? (:card c)))

        check (as-> (mc/find-one-as-map db "decks" {:_id (object-id deck-id) :username username}) d

                    (map-values d [:resources :hazards :sideboard
                                              :characters :pool :fwsb]
                                #(mapv get-code %))

                    (map-values d [:resources :hazards :sideboard
                                              :characters :pool :fwsb]
                                #(mapv get-data %))

                    (map-values d [:resources :hazards :sideboard
                                              :characters :pool :fwsb]
                                #(mapv get-swap %))

                    (map-values d [:resources :hazards :sideboard
                                              :characters :pool :fwsb]
                                #(mapv rid-code %))

                    (map-values d [:resources :hazards :sideboard
                                              :characters :pool :fwsb]
                                #(vec (remove rid-card %)))

                    (update-in d [:identity] #(@all-cards (str (:title %) " " (:trimCode %))))
                    (assoc d :status (decks/calculate-deck-status d))
                    )

        deck (as-> (mc/find-one-as-map db "decks" {:_id (object-id deck-id) :username username}) d
                   ;(process-sites d)
                   (assoc d :location
                          (let [{:keys [status option]} (get-in check [:status])]
                            (cond
                              (= metws true)
                              standard-metw-sites-only
                              (= metds true)
                              standard-metd-sites-only
                              (= medms true)
                              standard-medm-sites-only
                              (= meass true)
                              standard-meas-sites-only
                              (and (= "standard" status) (= option false))
                              (cond
                                (= (get-in d [:identity :alignment]) "Hero") standard-wizard-sites
                                (= (get-in d [:identity :alignment]) "Minion") standard-minion-sites
                                (= (get-in d [:identity :alignment]) "Fallen-wizard") standard-fallen-sites
                                (= (get-in d [:identity :alignment]) "Balrog") standard-minion-sites)
                              (and (= "standard" status) (= option true)) standard-option-sites
                              (and (= "dreamcard" status) (= option false))
                              (cond
                                (= (get-in d [:identity :alignment]) "Hero") dreamcard-wizard-sites
                                (= (get-in d [:identity :alignment]) "Minion") dreamcard-minion-sites
                                (= (get-in d [:identity :alignment]) "Fallen-wizard") dreamcard-fallen-sites
                                (= (get-in d [:identity :alignment]) "Balrog") dreamcard-minion-sites
                                (= (get-in d [:identity :alignment]) "Elf-lord") dreamcard-elf-sites
                                (= (get-in d [:identity :alignment]) "Dwarf-lord") dreamcard-dwarf-sites
                                (= (get-in d [:identity :alignment]) "Atani-lord") dreamcard-atani-sites
                                (= (get-in d [:identity :alignment]) "Dragon-lord") dreamcard-dragon-sites
                                (= (get-in d [:identity :alignment]) "War-lord") dreamcard-warlord-sites)
                              (and (= "dreamcard" status) (= option true)) dreamcard-option-sites
                              )
                            ))

                   (map-values d [:resources :hazards :sideboard
                                             :characters :pool :fwsb :location]
                               #(mapv get-code %))

                   (map-values d [:resources :hazards :sideboard
                                             :characters :pool :fwsb :location]
                               #(mapv get-data %))

                   (map-values d [:resources :hazards :sideboard
                                             :characters :pool :fwsb :location]
                               #(mapv get-swap %))

                   (map-values d [:resources :hazards :sideboard
                                             :characters :pool :fwsb :location]
                               #(mapv rid-code %))

                   (map-values d [:resources :hazards :sideboard
                                             :characters :pool :fwsb :location]
                               #(vec (remove rid-card %)))

                   (update-in d [:identity] #(@all-cards (str (:title %) " " (:trimCode %))))
                   (assoc d :status (decks/calculate-deck-status d)))]
    (when (and (:identity deck) (player? client-id gameid))
      (let [player [gameid :players (if (= client-id (:ws-id fplayer)) 0 1)]]
        (swap! all-games update-in player (fn [p]
                                            (let [hero-go (if (and (= 0 (count (:standard p)))
                                                                   (= 0 (count (:dreamcard p)))) true false)
                                                  minion-go (if (:dragon (:dreamcard p)) false true)
                                                  fw-go (if (or (= 0 (count (:dreamcard p)))
                                                                (and (= 1 (count (:dreamcard p)))
                                                                     (:dragon (:dreamcard p)))) true false)]
                                              (if (align-match opp-align match-code hero-go minion-go fw-go)
                                                (assoc p :deck deck)
                                                (assoc p :deck (:deck p))))))
        (swap! all-games update-in player
               (fn [p] (case match-code
                         "Practice" (assoc p :hero true)
                         "Hero/Any" (assoc p :hero true)
                         "Minion" (assoc-in p [:standard :minion] true)
                         "Fallen-wizard" (assoc-in p [:standard :fallen] true)
                         "Fallen-wizard/Lords" (assoc-in p [:standard :fallen] true)
                         "Elf-lord" (assoc-in p [:dreamcard :el] true)
                         "Dwarf-lord" (assoc-in p [:dreamcard :dl] true)
                         "Atani-lord" (assoc-in p [:dreamcard :al] true)
                         "Dragon-lord" (assoc-in p [:dreamcard :dragon] true)))))
      (ws/broadcast-to! (lobby-clients gameid)
                        :games/diff
                        {:diff {:update {gameid (game-public-view (game-for-id gameid))}}}))))


(ws/register-ws-handlers!
 :chsk/uidport-open handle-ws-connect
 :lobby/create handle-lobby-create
 :lobby/delete handle-lobby-delete
 :lobby/loader handle-lobby-loader
 :lobby/leave handle-lobby-leave
 :lobby/join handle-lobby-join
 :lobby/watch handle-lobby-watch
 :lobby/say handle-lobby-say
 :lobby/swap handle-swap-sides
 :lobby/deck handle-select-deck)
