(in-ns 'game.core)

(declare card-init card-str close-access-prompt enforce-msg gain-agenda-point get-agenda-points installed? is-type?
         in-corp-scored? prevent-draw resolve-steal-events make-result say show-prompt system-msg trash-cards untrashable-while-rezzed?
         update-all-ice win win-decked play-sfx can-run? untrashable-while-resources?)

;;;; Functions for applying core Middle-earth CCG game rules.

;;; Playing cards.
(defn play-instant
  "Plays an Event or Operation."
  ([state side card] (play-instant state side (make-eid state) card nil))
  ([state side eid? card?] (if (:eid eid?)
                             (play-instant state side eid? card? nil)
                             (play-instant state side (make-eid state) eid? card?)))
  ([state side eid {:keys [title] :as card} {:keys [targets ignore-cost extra-cost no-additional-cost]}]
   (swap! state update-in [:bonus] dissoc :play-cost)
   (trigger-event state side :pre-play-instant card)
   (when-not (seq (get-in @state [side :locked (-> card :zone first)]))
     (let [cdef (card-def card)
           additional-cost (if (has-subtype? card "Triple")
                             (concat (:additional-cost cdef) [:click 2])
                             (:additional-cost cdef))
           additional-cost (if (and (has-subtype? card "Double")
                                    (not (get-in @state [side :register :double-ignore-additional])))
                             (concat (:additional-cost cdef) [:click 1])
                             additional-cost)
           additional-cost (if (and (has-subtype? card "Run")
                                    (get-in @state [:bonus :run-cost]))
                             (concat additional-cost (get-in @state [:bonus :run-cost]))
                             additional-cost)
           total-cost (play-cost state side card
                                 (concat (when-not no-additional-cost additional-cost) extra-cost
                                         [:credit (:cost card)]))
           eid (if-not eid (make-eid state) eid)]
       ;; ensure the instant can be played
       (if (and (if-let [req (:req cdef)]
                  (req state side eid card targets) true) ; req is satisfied
                (not (and (has-subtype? card "Current")
                          (get-in @state [side :register :cannot-play-current])))
                (not (and (has-subtype? card "Run")
                          (not (can-run? state :runner))))
                (not (and (has-subtype? card "Priority")
                          (get-in @state [side :register :spent-click])))) ; if priority, have not spent a click
         (if-let [cost-str (pay state side card (if ignore-cost 0 total-cost) {:action :play-instant})]
           (let [c (move state side (assoc card :seen true) :play-area)]
             (system-msg state side (str (if ignore-cost
                                           "play "
                                           (build-spend-msg cost-str "play"))
                                         title
                                         (when ignore-cost " at no cost")))
             (play-sfx state side "play-instant")
             (if (has-subtype? c "Current")
               (do (doseq [s [:corp :runner]]
                     (when-let [current (first (get-in @state [s :current]))] ; trash old current
                       (say state side {:user "__system__" :text (str (:title current) " is trashed.")})
                       (trash state side current)))
                   (let [c (some #(when (= (:cid %) (:cid card)) %) (get-in @state [side :play-area]))
                         moved-card (move state side c :current)]
                     (card-init state side eid moved-card true)))
               (do (resolve-ability state side (assoc cdef :eid eid) card nil)
                   (when-let [c (some #(when (= (:cid %) (:cid card)) %) (get-in @state [side :play-area]))]
                     (move state side c :discard))
                   (when (has-subtype? card "Terminal")
                     (lose state side :click (-> @state side :click))
                     (swap! state assoc-in [:corp :register :terminal] true))))
             (trigger-event state side (if (= side :corp) :play-operation :play-event) c))
           ;; could not pay the card's price; mark the effect as being over.
           (effect-completed state side eid card))
         ;; card's req was not satisfied; mark the effect as being over.
         (effect-completed state side eid card))))))

(defn max-draw
  "Put an upper limit on the number of cards that can be drawn in this turn."
  [state side n]
  (swap! state assoc-in [side :register :max-draw] n))

(defn remaining-draws
  "Calculate remaining number of cards that can be drawn this turn if a maximum exists"
  [state side]
  (when-let [max-draw (get-in @state [side :register :max-draw])]
    (let [drawn-this-turn (get-in @state [side :register :drawn-this-turn] 0)]
      (max (- max-draw drawn-this-turn) 0))))

(defn draw-bonus
  "Registers a bonus of n draws to the next draw (Daily Business Show)"
  [state side n]
  (swap! state update-in [:bonus :draw] (fnil #(+ % n) 0)))

(defn draw
  "Draw n cards from :deck to :hand."
  ([state side] (draw state side (make-eid state) 1 nil))
  ([state side n] (draw state side (make-eid state) n nil))
  ([state side n args] (draw state side (make-eid state) n args))
  ([state side eid n {:keys [suppress-event] :as args}]
   (swap! state update-in [side :register] dissoc :most-recent-drawn) ;clear the most recent draw in case draw prevented
   (trigger-event state side (if (= side :corp) :pre-corp-draw :pre-runner-draw) n)
   (let [active-player (get-in @state [:active-player])
         n (-> n (+ (or (get-in @state [:bonus :draw]) 0)))
         draws-wanted n
         draws-after-prevent (if (and (= side active-player) (get-in @state [active-player :register :max-draw]))
                                  (min n (remaining-draws state side))
                                  n)
         deck-count (count (get-in @state [side :deck]))]
     (when (and (= side :corp) (> draws-after-prevent deck-count))
       (win-decked state))
     (when-not (and (= side active-player) (get-in @state [side :register :cannot-draw]))
       (let [drawn (zone :hand (take draws-after-prevent (get-in @state [side :deck])))]
         (swap! state update-in [side :hand] #(concat % drawn))
         (swap! state update-in [side :deck] (partial drop draws-after-prevent))
         (swap! state assoc-in [side :register :most-recent-drawn] drawn)
         (swap! state update-in [side :register :drawn-this-turn] (fnil #(+ % draws-after-prevent) 0))
         (swap! state update-in [:bonus] dissoc :draw)
         (if (and (not suppress-event) (pos? deck-count))
           (when-completed
             (trigger-event-sync state side (if (= side :corp) :corp-draw :runner-draw) draws-after-prevent)
             (trigger-event-sync state side eid (if (= side :corp) :post-corp-draw :post-runner-draw) draws-after-prevent))
           (effect-completed state side eid))
         (when (= 0 (remaining-draws state side))
           (prevent-draw state side))))
     (when (< draws-after-prevent draws-wanted)
       (let [prevented (- draws-wanted draws-after-prevent)]
         (system-msg state (other-side side) (str "prevents "
                                                  (quantify prevented "card")
                                                  " from being drawn")))))))

;;; Damage
(defn flatline [state]
  (when-not (:winner state)
    (system-msg state :runner "is flatlined")
    (win state :corp "Flatline")))

(defn damage-count
  "Calculates the amount of damage to do, taking into account prevention and boosting effects."
  [state side dtype n {:keys [unpreventable unboostable] :as args}]
  (-> n
      (+ (or (when-not unboostable (get-in @state [:damage :damage-bonus dtype])) 0))
      (- (or (when-not unpreventable (get-in @state [:damage :damage-prevent dtype])) 0))
      (max 0)))

(defn damage-bonus
  "Registers a bonus of n damage to the next damage application of the given type."
  [state side dtype n]
  (swap! state update-in [:damage :damage-bonus dtype] (fnil #(+ % n) 0)))

(defn damage-prevent
  "Registers a prevention of n damage to the next damage application of the given type."
  [state side dtype n]
  (swap! state update-in [:damage :damage-prevent dtype] (fnil #(+ % n) 0)))

(defn damage-defer
  "Registers n damage of the given type to be deferred until later. (Chronos Protocol.)"
  [state side dtype n]
  (swap! state assoc-in [:damage :defer-damage dtype] n))

(defn get-defer-damage [state side dtype {:keys [unpreventable] :as args}]
  (when-not unpreventable (get-in @state [:damage :defer-damage dtype])))

(defn enable-runner-damage-choice
  [state side]
  (swap! state assoc-in [:damage :damage-choose-runner] true))

(defn enable-corp-damage-choice
  [state side]
  (swap! state assoc-in [:damage :damage-choose-corp] true))

(defn runner-can-choose-damage?
  [state]
  (get-in @state [:damage :damage-choose-runner]))

(defn corp-can-choose-damage?
  [state]
  (get-in @state [:damage :damage-choose-corp]))

(defn damage-choice-priority
  "Determines which side gets to act if either or both have the ability to choose cards for damage.
  Currently just for Chronos Protocol vs Titanium Ribs"
  [state]
  (let [active-player (get-in @state [:active-player])]
    (when (and (corp-can-choose-damage? state) (runner-can-choose-damage? state))
      (if (= active-player :corp)
        (swap! state update-in [:damage] dissoc :damage-choose-runner)
        (swap! state update-in [:damage] dissoc :damage-choose-corp)))))

(defn resolve-damage
  "Resolves the attempt to do n damage, now that both sides have acted to boost or
  prevent damage."
  [state side eid type n {:keys [unpreventable unboostable card] :as args}]
  (swap! state update-in [:damage :defer-damage] dissoc type)
  (damage-choice-priority state)
  (when-completed (trigger-event-sync state side :pre-resolve-damage type card n)
                  (do (when-not (or (get-in @state [:damage :damage-replace])
                                    (runner-can-choose-damage? state))
                        (let [n (if-let [defer (get-defer-damage state side type args)] defer n)]
                          (when (pos? n)
                            (let [hand (get-in @state [:runner :hand])
                                  cards-trashed (take n (shuffle hand))]
                              (when (= type :brain)
                                (swap! state update-in [:runner :brain-damage] #(+ % n))
                                (swap! state update-in [:runner :hand-size-modification] #(- % n)))
                              (when-let [trashed-msg (join ", " (map :title cards-trashed))]
                                (system-msg state :runner (str "trashes " trashed-msg " due to damage")))
                              (if (< (count hand) n)
                                (do (flatline state)
                                    (trash-cards state side (make-eid state) cards-trashed
                                                 {:unpreventable true}))
                                (do (trash-cards state side (make-eid state) cards-trashed
                                                 {:unpreventable true :cause type})
                                    (trigger-event state side :damage type card n)))))))
                      (swap! state update-in [:damage :defer-damage] dissoc type)
                      (effect-completed state side eid card))))

(defn damage
  "Attempts to deal n damage of the given type to the runner. Starts the
  prevention/boosting process and eventually resolves the damage."
  ([state side type n] (damage state side (make-eid state) type n nil))
  ([state side type n args] (damage state side (make-eid state) type n args))
  ([state side eid type n {:keys [unpreventable unboostable card] :as args}]
   (swap! state update-in [:damage :damage-bonus] dissoc type)
   (swap! state update-in [:damage :damage-prevent] dissoc type)
   ;; alert listeners that damage is about to be calculated.
   (trigger-event state side :pre-damage type card n)
   (let [n (damage-count state side type n args)]
     (let [prevent (get-in @state [:prevent :damage type])]
       (if (and (not unpreventable) prevent (pos? (count prevent)))
         ;; runner can prevent the damage.
         (do (system-msg state :runner "has the option to avoid damage")
             (show-wait-prompt state :corp "Runner to prevent damage" {:priority 10})
             (show-prompt
               state :runner nil (str "Prevent any of the " n " " (name type) " damage?") ["Done"]
               (fn [_]
                 (let [prevent (get-in @state [:damage :damage-prevent type])]
                   (when prevent
                     (trigger-event state side :prevented-damage type prevent))
                   (system-msg state :runner
                               (if prevent
                                 (str "prevents " (if (= prevent Integer/MAX_VALUE) "all" prevent)
                                      " " (name type) " damage")
                                 "will not prevent damage"))
                   (clear-wait-prompt state :corp)
                   (resolve-damage state side eid type (max 0 (- n (or prevent 0))) args)))
               {:priority 10}))
         (resolve-damage state side eid type n args))))))


;;; Tagging
(defn tag-count
  "Calculates the number of tags to give, taking into account prevention and boosting effects."
  [state side n {:keys [unpreventable unboostable] :as args}]
  (-> n
      (+ (or (when-not unboostable (get-in @state [:tag :tag-bonus])) 0))
      (- (or (when-not unpreventable (get-in @state [:tag :tag-prevent])) 0))
      (max 0)))

(defn tag-prevent [state side n]
  (swap! state update-in [:tag :tag-prevent] (fnil #(+ % n) 0))
  (trigger-event state side (if (= side :corp) :corp-prevent :runner-prevent) `(:tag ~n)))

(defn tag-remove-bonus
  "Applies a cost increase of n to removing tags with the click action. (SYNC.)"
  [state side n]
  (swap! state update-in [:runner :tag-remove-bonus] (fnil #(+ % n) 0)))

(defn resolve-tag [state side eid n args]
  (if (pos? n)
    (do (gain state :runner :tag n)
        (toast state :runner (str "Took " (quantify n "tag") "!") "info")
        (trigger-event-sync state side eid :runner-gain-tag n))
    (effect-completed state side eid)))

(defn tag-runner
  "Attempts to give the runner n tags, allowing for boosting/prevention effects."
  ([state side n] (tag-runner state side (make-eid state) n nil))
  ([state side eid n] (tag-runner state side eid n nil))
  ([state side eid n {:keys [unpreventable unboostable card] :as args}]
   (swap! state update-in [:tag] dissoc :tag-bonus :tag-prevent)
   (trigger-event state side :pre-tag card)
   (let [n (tag-count state side n args)]
     (let [prevent (get-in @state [:prevent :tag :all])]
       (if (and (pos? n) (not unpreventable) (pos? (count prevent)))
         (do (system-msg state :runner "has the option to avoid tags")
             (show-wait-prompt state :corp "Runner to prevent tags" {:priority 10})
             (show-prompt
               state :runner nil (str "Avoid any of the " n " tags?") ["Done"]
               (fn [_]
                 (let [prevent (get-in @state [:tag :tag-prevent])]
                   (system-msg state :runner
                               (if prevent
                                 (str "avoids " (if (= prevent Integer/MAX_VALUE) "all" prevent)
                                      (if (< 1 prevent) " tags" " tag"))
                                 "will not avoid tags"))
                   (clear-wait-prompt state :corp)
                   (resolve-tag state side eid (max 0 (- n (or prevent 0))) args)))
               {:priority 10}))
         (resolve-tag state side eid n args))))))


;;; Trashing
(defn trash-resource-bonus
  "Applies a cost increase of n to trashing a resource with the click action. (SYNC.)"
  [state side n]
  (swap! state update-in [:corp :trash-cost-bonus] (fnil #(+ % n) 0)))

(defn trash-prevent [state side type n]
  (swap! state update-in [:trash :trash-prevent type] (fnil #(+ % n) 0)))

(defn- resolve-trash-end
  [state side eid {:keys [zone type disabled] :as card}
   {:keys [unpreventable cause keep-server-alive suppress-event] :as args} & targets]
  (let [cdef (card-def card)
        moved-card (move state (to-keyword (:side card)) card :discard {:keep-server-alive keep-server-alive})]
    (when-let [trash-effect (:trash-effect cdef)]
      (when (and (not disabled) (or (and (= (:side card) "Runner")
                                         (:installed card))
                                    (:rezzed card)
                                    (:when-inactive trash-effect)))
        (resolve-ability state side trash-effect moved-card (cons cause targets))))
    (swap! state update-in [:per-turn] dissoc (:cid moved-card))
    (effect-completed state side eid)))

(defn- resolve-trash
  [state side eid {:keys [zone type] :as card}
   {:keys [unpreventable cause keep-server-alive suppress-event] :as args} & targets]
  (if (and (not suppress-event) (not= (last zone) :current)) ; Trashing a current does not trigger a trash event.
    (when-completed (apply trigger-event-sync state side (keyword (str (name side) "-trash")) card cause targets)
                    (apply resolve-trash-end state side eid card args targets))
    (apply resolve-trash-end state side eid card args targets)))

(defn trash
  "Attempts to trash the given card, allowing for boosting/prevention effects."
  ([state side card] (trash state side (make-eid state) card nil))
  ([state side card args] (trash state side (make-eid state) card args))
  ([state side eid {:keys [zone type] :as card} {:keys [unpreventable cause suppress-event] :as args} & targets]
   (if (not (some #{:discard} zone))
     (cond

       (untrashable-while-rezzed? card)
       (do (enforce-msg state card "cannot be trashed while installed")
           (effect-completed state side eid))

       (and (= side :corp)
            (untrashable-while-resources? card)
            (> (count (filter #(is-type? % "Resource") (all-installed state :runner))) 1))
       (do (enforce-msg state card "cannot be trashed while there are other resources installed")
           (effect-completed state side eid))

       ;; Card is not enforced untrashable
       :else
       (let [ktype (keyword (clojure.string/lower-case type))]
         (when (and (not unpreventable) (not= cause :ability-cost))
           (swap! state update-in [:trash :trash-prevent] dissoc ktype))
         (let [prevent (get-in @state [:prevent :trash ktype])]
           ;; Check for prevention effects
           (if (and (not unpreventable) (not= cause :ability-cost) (pos? (count prevent)))
             (do (system-msg state :runner "has the option to prevent trash effects")
                 (show-wait-prompt state :corp "Runner to prevent trash effects" {:priority 10})
                 (show-prompt state :runner nil
                              (str "Prevent the trashing of " (:title card) "?") ["Done"]
                              (fn [_]
                                (clear-wait-prompt state :corp)
                                (if-let [_ (get-in @state [:trash :trash-prevent ktype])]
                                  (do (system-msg state :runner (str "prevents the trashing of " (:title card)))
                                      (swap! state update-in [:trash :trash-prevent] dissoc ktype)
                                      (effect-completed state side eid))
                                  (do (system-msg state :runner (str "will not prevent the trashing of " (:title card)))
                                      (apply resolve-trash state side eid card args targets))))
                              {:priority 10}))
             ;; No prevention effects; resolve the trash.
             (apply resolve-trash state side eid card args targets)))))
     (effect-completed state side eid))))

(defn trash-cards
  ([state side cards] (trash-cards state side (make-eid state) cards nil))
  ([state side eid cards args & targets]
   (letfn [(trashrec [cs]
             (if (not-empty cs)
               (when-completed (apply trash state side (first cs) args targets)
                               (trashrec (next cs)))
               (effect-completed state side eid)))]
     (trashrec cards))))

(defn- resolve-trash-no-cost
  [state side card]
  (trash state side (assoc card :seen true))
  (swap! state assoc-in [:runner :register :trashed-card] true)
  (close-access-prompt state side))

(defn trash-no-cost
  "Trashes a card at no cost while it is being accessed. (Imp.)"
  [state side]
  (let [prompt (-> @state side :prompt first)
             card (:card prompt)
             eid (:eid prompt)]
    (when card
      ;; trashing before the :access events actually fire; fire them manually
      (if (is-type? card "Agenda")
        (when-completed (resolve-steal-events state side card)
                        (resolve-trash-no-cost state side card))
        (resolve-trash-no-cost state side card)))))


;;; Agendas
(defn get-agenda-points
  "Apply agenda-point modifications to calculate the number of points this card is worth
  to the given player."
  [state side card]
  (let [base-points (:agendapoints card)
        runner-fn (:agendapoints-runner (card-def card))
        corp-fn (:agendapoints-corp (card-def card))]
    (if (and (= side :runner) (not (nil? runner-fn)))
      (runner-fn state side (make-eid state) card nil)
      (if (and (= side :corp) (not  (nil? corp-fn)))
        (corp-fn state side (make-eid state) card nil)
        base-points))))

(defn advancement-cost-bonus
  "Applies an advancement requirement increase of n the next agenda whose advancement requirement
  is being calculated. (SanSan City Grid.)"
  [state side n]
  (swap! state update-in [:bonus :advancement-cost] (fnil #(+ % n) 0)))

(defn advancement-cost [state side {:keys [advancementcost] :as card}]
  (if (nil? advancementcost)
    nil
    (-> (if-let [costfun (:advancement-cost-bonus (card-def card))]
          (+ advancementcost (costfun state side (make-eid state) card nil))
          advancementcost)
        (+ (or (get-in @state [:bonus :advancement-cost]) 0))
        (max 0))))

(defn update-advancement-cost
  "Recalculates the advancement requirement for the given agenda."
  [state side agenda]
  (swap! state update-in [:bonus] dissoc :advancement-cost)
  (trigger-event state side :pre-advancement-cost agenda)
  (update! state side (assoc agenda :current-cost (advancement-cost state side agenda))))

(defn update-all-advancement-costs [state side]
  (doseq [ag (->> (mapcat :content (flatten (seq (get-in @state [:corp :servers]))))
                  (filter #(is-type? % "Agenda")))]
    (update-advancement-cost state side ag)))

(defn as-agenda
  "Adds the given card to the given side's :scored area as an agenda worth n points."
  [state side card n]
  (move state side (assoc (deactivate state side card) :agendapoints n) :scored)
  (gain-agenda-point state side n))

(defn forfeit
  "Forfeits the given agenda to the :rfg zone."
  ([state side card] (forfeit state side (make-eid state) card))
  ([state side eid card]
   ;; Remove all hosted cards first
   (doseq [h (:hosted card)]
     (trash state side
            (update-in h [:zone] #(map to-keyword %))
            {:unpreventable true :suppress-event true}))
   (let [card (get-card state card)
         c (if (in-corp-scored? state side card)
             (deactivate state side card) card)]
     (system-msg state side (str "forfeits " (:title c)))
     (gain-agenda-point state side (- (get-agenda-points state side c)))
     (move state side c :rfg)
     (when-completed (trigger-event-sync state side (keyword (str (name side) "-forfeit-agenda")) c)
                     (effect-completed state side eid)))))

(defn gain-agenda-point
  "Gain n agenda points and check for winner."
  [state side n]
  (gain state side :agenda-point n)
  (when (and (>= (get-in @state [side :agenda-point]) (get-in @state [side :agenda-point-req]))
             (not (get-in @state [side :cannot-win-on-points])))
    (win state side "Agenda")))


;;; Miscellaneous
(defn purge
  "Purges viruses."
  [state side]
  (trigger-event state side :pre-purge)
  (let [rig-cards (all-installed state :runner)
        hosted-on-ice (->> (get-in @state [:corp :servers]) seq flatten (mapcat :ices) (mapcat :hosted))]
    (doseq [card (concat rig-cards hosted-on-ice)]
      (when (or (has-subtype? card "Virus")
                (contains? (:counter card) :virus))
        (add-counter state :runner card :virus (- (get-in card [:counter :virus] 0)))))
    (update-all-ice state side))
  (trigger-event state side :purge))

(defn mill
  "Force the discard of n cards from :deck to :discard."
  ([state side] (mill state side 1))
  ([state side n]
   (let [milltargets (take n (get-in @state [side :deck]))]
     (doseq [c milltargets]
       (move state side c :discard)))))

;; Exposing
(defn expose-prevent
  [state side n]
  (swap! state update-in [:expose :expose-prevent] #(+ (or % 0) n)))

(defn- resolve-expose
  [state side eid target args]
  (system-msg state side (str "exposes " (card-str state target {:visible true})))
  (if-let [ability (:expose (card-def target))]
    (when-completed (resolve-ability state side ability target nil)
                    (trigger-event-sync state side (make-result eid true) :expose target))
    (trigger-event-sync state side (make-result eid true) :expose target)))

(defn expose
  "Exposes the given card."
  ([state side target] (expose state side (make-eid state) target))
  ([state side eid target] (expose state side eid target nil))
  ([state side eid target {:keys [unpreventable] :as args}]
   (swap! state update-in [:expose] dissoc :expose-prevent)
   (when-completed (trigger-event-sync state side :pre-expose target)
                   (let [prevent (get-in @state [:prevent :expose :all])]
                     (if (and (not unpreventable) (pos? (count prevent)))
                       (do (system-msg state :corp "has the option to prevent a card from being exposed")
                           (show-wait-prompt state :runner "Corp to prevent the expose" {:priority 10})
                           (show-prompt state :corp nil
                                        (str "Prevent " (:title target) " from being exposed?") ["Done"]
                                        (fn [_]
                                          (clear-wait-prompt state :runner)
                                          (if-let [_ (get-in @state [:expose :expose-prevent])]
                                            (effect-completed state side (make-result eid false)) ;; ??
                                            (do (system-msg state :corp "will not prevent a card from being exposed")
                                                (resolve-expose state side eid target args))))
                                        {:priority 10}))
                       (if-not (get-in @state [:expose :expose-prevent])
                         (resolve-expose state side eid target args)
                         (effect-completed state side (make-result eid false))))))))

(defn reveal-hand
  "Reveals a side's hand to opponent and spectators."
  [state side]
  (swap! state assoc-in [side :openhand] true))

(defn conceal-hand
  "Conceals a side's revealed hand from opponent and spectators."
  [state side]
  (swap! state update-in [side] dissoc :openhand))

(defn clear-win
  "Clears the current win condition.  Requires both sides to have issued the command"
  [state side]
  (swap! state assoc-in [side :clear-win] true)
  (when (and (-> @state :runner :clear-win) (-> @state :corp :clear-win))
    (system-msg state side "cleared the win condition")
    (swap! state dissoc-in [:runner :clear-win])
    (swap! state dissoc-in [:corp :clear-win])
    (swap! state dissoc :winner :loser :winning-user :losing-user :reason :winning-deck-id :losing-deck-id :end-time)))

(defn win
  "Records a win reason for statistics."
  [state side reason]
  (when-not (:winner @state)
    (system-msg state side "wins the game")
    (play-sfx state side "game-end")
    (swap! state assoc
           :winner side
           :loser (other-side side)
           :winning-user (get-in @state [side :user :username])
           :losing-user (get-in @state [(other-side side) :user :username])
           :reason reason :end-time (java.util.Date.)
           :winning-deck-id (get-in @state [side :deck-id])
           :losing-deck-id (get-in @state [(other-side side) :deck-id]))))

(defn win-decked
  "Records a win via decking the corp."
  [state]
  (system-msg state :corp "is decked")
  (win state :runner "Decked"))

(defn init-trace-bonus
  "Applies a bonus base strength of n to the next trace attempt."
  [state side n]
  (swap! state update-in [:bonus :trace] (fnil #(+ % n) 0)))
