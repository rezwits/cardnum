(in-ns 'game.core)

(declare active? all-placed all-active-placed cards card-init deactivate card-flag? gain lose get-card-hosted
         handle-end-run hazard? is-subtype? has-subtype? character? ally? is-type? make-eid resource? site? region? register-events remove-from-host
         remove-icon reset-card radicle? revealed? toast toast-check-mu discard trigger-event update-breaker-strength
         update-hosted! update-character-strength unregister-events use-mu)

;;; Functions for loading card information.
(defn card-def
  "Retrieves a card's abilities definition map."
  [card]
  (when-let [title (:title card)]
    (cards (.replace title "'" ""))))

(defn find-cid
  "Return a card with specific :cid from given sequence"
  [cid from]
  (some #(when (= (:cid %) cid) %) from))

(defn find-latest
  "Returns the newest version of a card where-ever it may be"
  [state card]
  (let [side (-> card :side to-keyword)]
    (find-cid (:cid card) (concat (all-placed state side)
                                  (-> (map #(-> @state side %) [:hand :discard :deck :rfg]) concat flatten)))))

(defn get-scoring-owner
  "Returns the owner of the scoring area the card is in"
  [state {:keys [cid] :as card}]
  (cond
    (find-cid cid (get-in @state [:contestant :scored]))
    :contestant
    (find-cid cid (get-in @state [:challenger :scored]))
    :challenger))

(defn get-card
  "Returns the most recent copy of the card from the current state, as identified
  by the argument's :zone and :cid."
  [state {:keys [cid zone side host type] :as card}]
  (if (= type "Identity")
    (get-in @state [(to-keyword side) :identity])
    (if zone
      (if host
        (get-card-hosted state card)
        (some #(when (= cid (:cid %)) %)
              (let [zones (map to-keyword zone)]
                (if (= (first zones) :scored)
                  (into (get-in @state [:contestant :scored]) (get-in @state [:challenger :scored]))
                  (get-in @state (cons (to-keyword side) zones))))))
      card)))

;;; Functions for updating cards
(defn update!
  "Updates the state so that its copy of the given card matches the argument given."
  [state side {:keys [type zone cid host] :as card}]
  (if (= type "Identity")
    (when (= side (to-keyword (:side card)))
      (swap! state assoc-in [side :identity] card))
    (if host
      (update-hosted! state side card)
      (let [z (cons (to-keyword (or (get-scoring-owner state card) (:side card))) zone)
            [head tail] (split-with #(not= (:cid %) cid) (get-in @state z))]
        (when (not-empty tail)
          (swap! state assoc-in z (vec (concat head [card] (rest tail)))))))))

(defn move
  "Moves the given card to the given new zone."
  ([state side card to] (move state side card to nil))
  ([state side {:keys [zone cid host placed] :as card} to {:keys [front keep-locale-alive force] :as options}]
   (let [to (if (is-type? card "Fake-Identity") :rfg to)          ; Fake-Identities always get moved to RFG
         zone (if host (map to-keyword (:zone host)) zone)
         src-zone (first zone)
         target-zone (if (vector? to) (first to) to)
         same-zone? (= src-zone target-zone)]
     (when (and card (or host
                         (some #(when (= cid (:cid %)) %) (get-in @state (cons :challenger (vec zone))))
                         (some #(when (= cid (:cid %)) %) (get-in @state (cons :contestant (vec zone)))))
                (or (empty? (get-in @state [side :locked (-> card :zone first)]))
                    force))
       (trigger-event state side :pre-card-moved card src-zone target-zone)
       (let [dest (if (sequential? to) (vec to) [to])
             to-facedown (= dest [:rig :facedown])
             to-placed (#{:locales :rig} (first dest))
             discard-hosted (fn [h]
                             (discard state side
                                    (update-in h [:zone] #(map to-keyword %))
                                    {:unpreventable true
                                     :suppress-event true
                                     ;; this handles executives getting discarded before World's Plaza #2949
                                     :host-discarded true})
                               ())
             update-hosted (fn [h]
                             (let [newz (flatten (list (if (vector? to) to [to])))
                                   newh (-> h
                                      (assoc-in [:zone] '(:onhost))
                                      (assoc-in [:host :zone] newz))]
                               (update! state side newh)
                               (unregister-events state side h)
                               (register-events state side (:events (card-def newh)) newh)
                               newh))
             hosted (let [hosted-cards (filter some? (:hosted card))]
                      (when (seq hosted-cards)
                        (mapv (if same-zone? update-hosted discard-hosted) hosted-cards)))
             c (if (and (= side :contestant) (= (first dest) :discard) (revealed? card))
                 (assoc card :seen true) card)
             c (if (and (or placed host (#{:locales :scored :current} (first zone)))
                        (or (#{:hand :deck :discard :rfg} (first dest)) to-facedown)
                        (not (:facedown c)))
                 (deactivate state side c to-facedown) c)
             c (if to-placed (assoc c :placed true) (dissoc c :placed))
             c (if to-facedown (assoc c :facedown true) (dissoc c :facedown))
             moved-card (assoc c :zone dest :host nil :hosted hosted :previous-zone (:zone c))
             moved-card (if (and (#{:hand :deck} (first dest)))
                          (dissoc moved-card :seen) moved-card)
             moved-card (if (and (#{:discard} (first dest)))
                          (dissoc moved-card :seen :revealed) moved-card)
             moved-card (if (and (= (first (:zone moved-card)) :scored) (card-flag? moved-card :has-abilities-when-stolen true))
                          (merge moved-card {:abilities (:abilities (card-def moved-card))}) moved-card)]
         (if front
           (swap! state update-in (cons side dest) #(into [] (cons moved-card (vec %))))
           (swap! state update-in (cons side dest) #(into [] (conj (vec %) moved-card))))
         (doseq [s [:challenger :contestant]]
           (if host
             (remove-from-host state side card)
             (swap! state update-in (cons s (vec zone)) (fn [coll] (remove-once #(= (:cid %) cid) coll))))
           (let [z (vec (cons s (butlast zone)))]
             (when (and (not keep-locale-alive)
                        (is-party? z)
                        (empty? (get-in @state (conj z :content)))
                        (empty? (get-in @state (conj z :characters))))
               (when-let [run (:run @state)]
                 (when (= (last (:locale run)) (last z))
                   (handle-end-run state side)))
               (swap! state dissoc-in z))))
         (when-let [card-moved (:move-zone (card-def c))]
           (card-moved state side (make-eid state) moved-card card))
         (trigger-event state side :card-moved card (assoc moved-card :move-to-side side))
         ;; Default a card when moved to inactive zones (except :persistent key)
         (when (#{:discard :hand :deck :rig :location :sideboard :rfg} to)
           (reset-card state side moved-card)
           (when-let [icon-card (get-in moved-card [:icon :card])]
             ;; Remove icon and icon-card keys
             (remove-icon state side icon-card moved-card)))
         moved-card)))))

(defn move-zone
  "Moves all cards from one zone to another, as in Chronos Project."
  [state side locale to]
  (when-not (seq (get-in @state [side :locked locale]))
    (let [from-zone (cons side (if (sequential? locale) locale [locale]))
          to-zone (cons side (if (sequential? to) to [to]))]
      (swap! state assoc-in to-zone (concat (get-in @state to-zone)
                                            (zone to (get-in @state from-zone))))
      (swap! state assoc-in from-zone []))))

(defn add-prop
  "Adds the given value n to the existing value associated with the key in the card.
  Example: (add-prop ... card :counter 1) adds one power/virus counter. Triggers events."
  ([state side card key n] (add-prop state side card key n nil))
  ([state side card key n {:keys [placed] :as args}]
   (let [updated-card (if (has-subtype? card "Virus")
                        (assoc card :added-virus-counter true)
                        card)]
     (update! state side (update-in updated-card [key] #(+ (or % 0) n)))
     (if (= key :advance-counter)
       (do (when (and (character? updated-card) (revealed? updated-card)) (update-character-strength state side updated-card))
           (if-not placed
             (trigger-event state side :advance (get-card state updated-card))
             (trigger-event state side :advancement-placed (get-card state updated-card))))
       (trigger-event state side :counter-added (get-card state updated-card))))))

(defn set-prop
  "Like add-prop, but sets multiple keys to corresponding values without triggering events.
  Example: (set-prop ... card :counter 4 :current-strength 0)"
  [state side card & args]
  (update! state side (apply assoc (cons card args))))

(defn add-counter
  "Adds n counters of the specified type to a card"
  ([state side card type n] (add-counter state side card type n nil))
  ([state side card type n {:keys [placed] :as args}]
   (let [updated-card (if (= type :virus)
                        (assoc card :added-virus-counter true)
                        card)]
     (update! state side (update-in updated-card [:counter type] #(+ (or % 0) n)))
     (if (= type :advancement)
       ;; if advancement counter use existing system
       (add-prop state side card :advance-counter n args)
       (trigger-event state side :counter-added (get-card state updated-card))))))

;;; Deck-related functions
(defn shuffle!
  "Shuffles the vector in @state [side kw]."
  [state side kw]
  (wait-for (trigger-event-sync state side (keyword (str (name side) "-shuffle-deck")))
            (swap! state update-in [side kw] shuffle)))

(defn shuffle-into-deck
  [state side & args]
  (let [player (side @state)
        zones (filter #(not (seq (get-in @state [side :locked %]))) args)
        deck (shuffle (reduce concat (:deck player) (for [p zones] (zone :deck (p player)))))]
    (swap! state assoc-in [side :deck] deck)
    (doseq [p zones]
      (swap! state assoc-in [side p] []))))

;;; Misc card functions
(defn set-hand-aside
  [state side & args]
  (let [player (side @state)
        zones (filter #(not (seq (get-in @state [side :locked %]))) args)
        temp (reduce concat (:play-area player) (for [p zones] (zone :play-area (p player))))]
    (swap! state assoc-in [side :play-area] temp)
    (doseq [p zones]
      (swap! state assoc-in [side p] []))))

(defn is-virus-resource?
  [card]
  (and (resource? card)
       (has-subtype? card "Virus")))

(defn get-virus-counters
  "Calculate the number of virus counters on the given card, taking Hivemind into account."
  [state side card]
  (let [hiveminds (when (is-virus-resource? card)
                    (filter #(= (:title %) "Hivemind") (all-active-placed state :challenger)))]
    (reduce + (map #(get-counters % :virus) (cons card hiveminds)))))

(defn card->locale
  "Returns the locale map that this card is placed in or protecting."
  [state card]
  (let [z (:zone card)]
    (get-in @state [:contestant :locales (second z)])))

(defn disable-identity
  "Disables the side's identity"
  [state side]
  (let [id (assoc (:identity (side @state)) :disabled true)]
    (update! state side id)
    (unregister-events state side id)
    (when-let [leave-play (:leave-play (card-def id))]
      (leave-play state side (make-eid state) id nil))))

(defn disable-card
  "Disables a card"
  [state side card]
  (let [c (assoc card :disabled true)]
    (deactivate state side card)
    (update! state side c)))

(defn enable-identity
  "Enables the side's identity"
  [state side]
  (let [id (assoc (:identity (side @state)) :disabled false)
        cdef (card-def id)
        events (:events cdef)]
    (update! state side id)
    (when-let [eff (:effect cdef)]
      (eff state side (make-eid state) id nil))
    (when events
      (register-events state side events id))))

(defn enable-card
  "Enables a disabled card"
  [state side {:keys [disabled] :as card}]
  (when disabled
    (let [c (dissoc card :disabled)]
      (update! state side c)
      (when (active? card)
        (card-init state side c {:resolve-effect false})))))

(defn flip-facedown
  "Flips a challenger card facedown, either manually (if it's hosted) or by calling move to facedown"
  [state side {:keys [host] :as card}]
  (if host
    (let [card (deactivate state side card true)
          card (assoc-in card [:facedown] true)]
      (update! state side card))
    (move state side card [:rig :facedown])))

(defn flip-faceup
  "Flips a challenger card facedown, either manually (if it's hosted) or by calling move to correct area.
  Wires events without calling effect/init-data"
  [state side {:keys [host] :as card}]
  (let [card (if host
               (dissoc card :facedown)
               (move state side card (type->rig-zone (:type card))))]
   (card-init state side card {:resolve-effect false :init-data false})
   (when-let [mu (:memoryunits card)]
     (use-mu state mu)
     (toast-check-mu state))
   (when (has-subtype? card "Icebreaker")
     (update-breaker-strength state side card))))
