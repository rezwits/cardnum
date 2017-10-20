(in-ns 'game.core)

;;; Helper functions for Draft cards
(def draft-points-target
  "Set each side's agenda points target at 6, per draft format rules"
  (req (swap! state assoc-in [:runner :agenda-point-req] 6)
       (swap! state assoc-in [:corp :agenda-point-req] 6)))

(defn- has-most-faction?
  "Checks if the faction has a plurality of rezzed / installed cards"
  [state side fc]
  (let [card-list (if (= side :corp)
                    (filter :rezzed (all-installed state :corp))
                    (all-installed state :runner))
        faction-freq (frequencies (map :faction card-list))
        reducer (fn [{:keys [max-count] :as acc} faction count]
                  (cond
                    ;; Has plurality update best-faction
                    (> count max-count)
                    {:max-count count :max-faction faction}
                    ;; Lost plurality
                    (= count max-count)
                    (dissoc acc :max-faction)
                    ;; Count is not more, do not change the accumulator map
                    :default
                    acc))
        best-faction (:max-faction (reduce-kv reducer {:max-count 0 :max-faction nil} faction-freq))]
    (= fc best-faction)))

;;; Card definitions
(def cards-identities
  {"Adam: Compulsive Hacker"
   {:events {:pre-start-game
             {:req (req (= side :runner))
              :delayed-completion true
              :effect (req (show-wait-prompt state :corp "Runner to choose starting directives")
                           (let [is-directive? #(has-subtype? % "Directive")
                                 directives (filter is-directive? (vals @all-cards))
                                 directives (map make-card directives)
                                 directives (zone :play-area directives)]
                             ;; Add directives to :play-area - assumed to be empty
                             (swap! state assoc-in [:runner :play-area] directives)
                             (continue-ability state side
                                               {:prompt (str "Choose 3 starting directives")
                                                :choices {:max 3
                                                          :req #(and (= (:side %) "Runner")
                                                                     (= (:zone %) [:play-area]))}
                                                :effect (req (doseq [c targets]
                                                               (runner-install state side c {:no-cost true
                                                                                             :custom-message (str "starts with " (:title c) " in play")}))
                                                             (swap! state assoc-in [:runner :play-area] [])
                                                             (clear-wait-prompt state :corp))}
                                               card nil)))}}}

   "AgInfusion: New Miracles for a New World"
   {:abilities [{:once :per-turn
                 :req (req (and (:run @state) (not (rezzed? current-ice))))
                 :prompt "Choose another server and redirect the run to its outermost position"
                 :choices (req (cancellable servers))
                 :msg (msg "trash the approached ICE. The Runner is now running on " target)
                 :effect (req (let [dest (server->zone state target)]
                                (trash state side current-ice)
                                (swap! state update-in [:run]
                                       #(assoc % :position (count (get-in corp (conj dest :ices)))
                                                 :server (rest dest)))))}]}

   "Alice Merchant: Clan Agitator"
   {:events {:successful-run
             {:delayed-completion true
              :interactive (req true)
              :req (req (and (= target :archives)
                             (first-successful-run-on-server? state :archives)
                             (not-empty (:hand corp))))
              :effect (effect (show-wait-prompt :runner "Corp to trash 1 card from HQ")
                              (continue-ability
                                {:prompt "Choose a card in HQ to discard"
                                 :player :corp
                                 :choices (req (:hand corp))
                                 :msg "force the Corp to trash 1 card from HQ"
                                 :effect (effect (trash :corp target)
                                                 (clear-wait-prompt :runner))}
                               card nil))}}}

   "Andromeda: Dispossessed Ristie"
   {:events {:pre-start-game {:req (req (= side :runner))
                              :effect (effect (draw 4 {:suppress-event true}))}}
    :mulligan (effect (draw 4 {:suppress-event true}))}

   "Apex: Invasive Predator"
   (let [ability {:prompt "Select a card to install facedown"
                  :label "Install a card facedown (start of turn)"
                  :once :per-turn
                  :choices {:max 1
                            :req #(and (= (:side %) "Runner")
                                       (in-hand? %))}
                  :req (req (and (pos? (count (:hand runner)))
                                 (:runner-phase-12 @state)))
                  :effect (effect (runner-install target {:facedown true}))}]
     {:events {:runner-turn-begins ability}
      :flags {:runner-phase-12 (req (pos? (count (:hand runner))))}
      :abilities [ability]})

   "Argus Security: Protection Guaranteed"
   {:events {:agenda-stolen
             {:prompt "Take 1 tag or suffer 2 meat damage?"
              :delayed-completion true
              :choices ["1 tag" "2 meat damage"] :player :runner
              :msg "make the Runner take 1 tag or suffer 2 meat damage"
              :effect (req (if (= target "1 tag")
                             (do (system-msg state side "chooses to take 1 tag")
                                 (tag-runner state :runner eid 1))
                             (do (system-msg state side "chooses to suffer 2 meat damage")
                                 (damage state :runner eid :meat 2 {:unboostable true :card card}))))}}}

   "Armand \"Geist\" Walker: Tech Lord"
   {:events {:runner-trash {:req (req (and (= side :runner) (= (second targets) :ability-cost)))
                            :msg "draw a card"
                            :effect (effect (draw 1))}}}

   "Ayla \"Bios\" Rahim: Simulant Specialist"
   {:abilities [{:label "[:click] Add 1 card from NVRAM to your grip"
                 :cost [:click 1]
                 :delayed-completion true
                 :prompt "Choose a card from NVRAM"
                 :choices (req (cancellable (:hosted card)))
                 :msg "move a card from NVRAM to their Grip"
                 :effect (effect (move target :hand)
                                 (effect-completed eid card))}]
    :events {:pre-start-game
             {:req (req (= side :runner))
              :delayed-completion true
              :effect (req (show-wait-prompt state :corp "the Runner to choose cards for NVRAM")
                           (doseq [c (take 6 (:deck runner))]
                             (move state side c :play-area))
                             (continue-ability state side
                                               {:prompt (str "Select 4 cards for NVRAM")
                                                :delayed-completion true
                                                :choices {:max 4
                                                          :all true
                                                          :req #(and (= (:side %) "Runner")
                                                                     (= (:zone %) [:play-area]))}
                                                :effect (req (doseq [c targets]
                                                               (host state side (get-card state card) c {:facedown true}))
                                                             (doseq [c (get-in @state [:runner :play-area])]
                                                               (move state side c :deck))
                                                             (shuffle! state side :deck)
                                                             (clear-wait-prompt state :corp)
                                                             (effect-completed state side eid card))} card nil))}}}

   "Blue Sun: Powering the Future"
   {:flags {:corp-phase-12 (req (and (not (:disabled card))
                                     (some #(rezzed? %) (all-installed state :corp))))}
    :abilities [{:choices {:req #(:rezzed %)}
                 :effect (req (trigger-event state side :pre-rez-cost target)
                              (let [cost (rez-cost state side target)]
                                (gain state side :credit cost)
                                (move state side target :hand)
                                (system-msg state side (str "adds " (:title target) " to HQ and gains " cost " [Credits]"))
                                (swap! state update-in [:bonus] dissoc :cost)))}]}

   "Boris \"Syfr\" Kovac: Crafty Veteran"
   {:events {:pre-start-game {:effect draft-points-target}
             :runner-turn-begins {:req (req (and (has-most-faction? state :runner "Criminal")
                                                 (pos? (:tag runner))))
                                  :msg "remove 1 tag"
                                  :effect (effect (lose :tag 1))}}}

   "Cerebral Imaging: Infinite Frontiers"
   {:effect (req (when (> (:turn @state) 1)
                   (swap! state assoc-in [:corp :hand-size-base] (:credit corp)))
                 (add-watch state :cerebral-imaging
                            (fn [k ref old new]
                              (let [credit (get-in new [:corp :credit])]
                                (when (not= (get-in old [:corp :credit]) credit)
                                  (swap! ref assoc-in [:corp :hand-size-base] credit))))))
    :leave-play (req (remove-watch state :cerebral-imaging)
                     (swap! state assoc-in [:corp :hand-size-base] 5))}

   "Chaos Theory: Wünderkind"
   {:effect (effect (gain :memory 1))
    :leave-play (effect (lose :runner :memory 1))}

   "Chronos Protocol: Selective Mind-mapping"
   {:events
    {:corp-phase-12 {:effect (effect (enable-corp-damage-choice))}
     :runner-phase-12 {:effect (effect (enable-corp-damage-choice))}
     :pre-resolve-damage
     {:delayed-completion true
      :req (req (and (= target :net)
                     (corp-can-choose-damage? state)
                     (> (last targets) 0)
                     (empty? (filter #(= :net (first %)) (turn-events state :runner :damage)))))
      :effect (req (damage-defer state side :net (last targets))
                   (if (= 0 (count (:hand runner)))
                     (do (swap! state update-in [:damage] dissoc :damage-choose-corp)
                         (damage state side eid :net (get-defer-damage state side :net nil)
                                 {:unpreventable true :card card}))
                     (do (show-wait-prompt state :runner "Corp to use Chronos Protocol: Selective Mind-mapping")
                         (continue-ability
                           state side
                           {:optional
                            {:prompt (str "Use Chronos Protocol: Selective Mind-mapping to reveal the Runner's "
                                          "Grip to select the first card trashed?")
                             :priority 10
                             :player :corp
                             :yes-ability {:prompt (msg "Select a card to trash")
                                           :choices (req (:hand runner)) :not-distinct true
                                           :priority 10
                                           :msg (msg "trash " (:title target)
                                                     (when (pos? (dec (or (get-defer-damage state side :net nil) 0)))
                                                       (str " and deal " (- (get-defer-damage state side :net nil) 1)
                                                            " more net damage")))
                                           :effect (req (clear-wait-prompt state :runner)
                                                        (swap! state update-in [:damage] dissoc :damage-choose-corp)
                                                        (trash state side target {:cause :net :unpreventable true})
                                                        (let [more (dec (or (get-defer-damage state side :net nil) 0))]
                                                          (damage-defer state side :net more)))}
                             :no-ability {:effect (req (clear-wait-prompt state :runner)
                                                       (swap! state update-in [:damage] dissoc :damage-choose-corp))}}}
                           card nil))))}}
    :req (req (empty? (filter #(= :net (first %)) (turn-events state :runner :damage))))
    :effect (effect (enable-corp-damage-choice))
    :leave-play (req (swap! state update-in [:damage] dissoc :damage-choose-corp))}

   "Cybernetics Division: Humanity Upgraded"
   {:effect (effect (lose :hand-size-modification 1)
                    (lose :runner :hand-size-modification 1))
    :leave-play (effect (gain :hand-size-modification 1)
                        (gain :runner :hand-size-modification 1))}

   "Edward Kim: Humanitys Hammer"
   {:events {:access {:once :per-turn
                      :req (req (and (is-type? target "Operation")
                                     (turn-flag? state side card :can-trash-operation)))
                      :effect (req (trash state side target)
                                   (swap! state assoc-in [:runner :register :trashed-card] true))
                      :msg (msg "trash " (:title target))}
             :successful-run-ends {:req (req (and (= (:server target) [:archives])
                                                  (nil? (:replace-access (:run-effect target)))
                                                  (not= (:max-access target) 0)
                                                  (seq (filter #(is-type? % "Operation") (:discard corp)))))
                                   :effect (effect (register-turn-flag! card :can-trash-operation (constantly false)))}}}

   "Ele \"Smoke\" Scovak: Cynosure of the Net"
   {:recurring 1}

   "Exile: Streethawk"
   {:events {:runner-install {:req (req (and (is-type? target "Program")
                                             (some #{:discard} (:previous-zone target))))
                              :msg (msg "draw a card")
                              :effect (effect (draw 1))}}}

   "Fringe Applications: Tomorrow, Today"
   {:events
    {:pre-start-game {:effect draft-points-target}
     :runner-turn-begins {:player :corp
                          :req (req (and (not (:disabled card))
                                         (has-most-faction? state :corp "Weyland Consortium")
                                         (some ice? (all-installed state side))))
                          :prompt "Select a piece of ICE to place 1 advancement token on"
                          :choices {:req #(and (installed? %)
                                               (ice? %))}
                          :msg (msg "place 1 advancement token on " (card-str state target))
                          :effect (req (add-prop state :corp target :advance-counter 1 {:placed true}))}}}

   "Gabriel Santiago: Consummate Professional"
   {:events {:successful-run {:silent (req true)
                              :req (req (and (= target :hq)
                                             (first-successful-run-on-server? state :hq)))
                              :msg "gain 2 [Credits]"
                              :effect (effect (gain :credit 2)) }}}

   "Gagarin Deep Space: Expanding the Horizon"
   {:flags {:slow-remote-access (req (not (:disabled card)))}
    :events {:pre-access-card {:req (req (is-remote? (second (:zone target))))
                               :effect (effect (access-cost-bonus [:credit 1]))
                               :msg "make the Runner spend 1 [Credits] to access"}}}

   "GRNDL: Power Unleashed"
   {:events {:pre-start-game {:req (req (= :corp side))
                              :effect (req (gain state :corp :credit 5)
                                           (when (= 0 (:bad-publicity corp))
                                             (gain state :corp :bad-publicity 1)))}}}

   "Haarpsichord Studios: Entertainment Unleashed"
   (let [haarp (fn [state side card]
                 (if (is-type? card "Agenda")
                   ((constantly false)
                     (toast state :runner "Cannot steal due to Haarpsichord Studios." "warning"))
                   true))]
     {:events {:agenda-stolen
               {:effect (effect (register-turn-flag! card :can-steal haarp))}}
      :effect (req (when-not (first-event? state side :agenda-stolen)
                     (register-turn-flag! state side card :can-steal haarp)))
      :leave-play (effect (clear-turn-flag! card :can-steal))})

   "Haas-Bioroid: Architects of Tomorrow"
   {:events {:pass-ice
             {:delayed-completion true
              :once :per-turn
              :req (req (and (rezzed? target)
                             (has-subtype? target "Bioroid")
                             (empty? (filter #(and (rezzed? %) (has-subtype? % "Bioroid"))
                                             (turn-events state side :pass-ice)))))
              :effect (effect (show-wait-prompt :runner "Corp to use Haas-Bioroid: Architects of Tomorrow")
                              (continue-ability
                                {:prompt "Select a Bioroid to rez" :player :corp
                                 :choices {:req #(and (has-subtype? % "Bioroid") (not (rezzed? %)))}
                                 :msg (msg "rez " (:title target))
                                 :cancel-effect (final-effect (clear-wait-prompt :runner))
                                 :effect (effect (rez-cost-bonus -4)
                                                 (rez target)
                                                 (clear-wait-prompt :runner))}
                               card nil))}}}

   "Haas-Bioroid: Engineering the Future"
   {:events {:corp-install {:req (req (first-event? state corp :corp-install))
                            :msg "gain 1 [Credits]"
                            :effect (effect (gain :credit 1))}}}

   "Haas-Bioroid: Stronger Together"
   {:events {:pre-ice-strength {:req (req (and (ice? target) (has-subtype? target "Bioroid")))
                                :effect (effect (ice-strength-bonus 1 target))}}}

   "Harishchandra Ent.: Where Youre the Star"
   {:effect (req (when tagged
                   (reveal-hand state :runner))
                 (add-watch state :harishchandra
                            (fn [k ref old new]
                              (when (and (is-tagged? new) (not (is-tagged? old)))
                                (system-msg ref side (str "uses Harishchandra Ent.: Where You're the Star to"
                                                          " make the Runner play with their Grip revealed"))
                                (reveal-hand state :runner))
                              (when (and (is-tagged? old) (not (is-tagged? new)))
                                (conceal-hand state :runner)))))
    :leave-play (req (when tagged
                       (conceal-hand state :runner))
                     (remove-watch state :harishchandra))}

   "Harmony Medtech: Biomedical Pioneer"
   {:effect (effect (lose :agenda-point-req 1) (lose :runner :agenda-point-req 1))
    :leave-play (effect (gain :agenda-point-req 1) (gain :runner :agenda-point-req 1))}

   "Hayley Kaplan: Universal Scholar"
   {:events {:runner-install
             {:silent (req (not (and (first-event? state side :runner-install)
                                     (some #(is-type? % (:type target)) (:hand runner)))))
              :req (req (and (first-event? state side :runner-install)
                             (some #(is-type? % (:type target)) (:hand runner))))
              :once :per-turn
              :delayed-completion true
              :effect
              (req (let [itarget target
                         type (:type itarget)]
                     (continue-ability
                       state side
                       {:optional {:prompt (msg "Install another " type " from your Grip?")
                                   :yes-ability
                                   {:prompt (msg "Select another " type " to install from your Grip")
                                    :choices {:req #(and (is-type? % type)
                                                         (in-hand? %))}
                                    :msg (msg "install " (:title target))
                                    :effect (effect (runner-install eid target nil))}}}
                       card nil)))}}}

   "Iain Stirling: Retired Spook"
   (let [ability {:req (req (> (:agenda-point corp) (:agenda-point runner)))
                  :once :per-turn
                  :msg "gain 2 [Credits]"
                  :effect (effect (gain :credit 2))}]
     {:flags {:drip-economy true}
      :events {:runner-turn-begins ability}
      :abilities [ability]})

   "Industrial Genomics: Growing Solutions"
   {:events {:pre-trash {:effect (effect (trash-cost-bonus
                                           (count (filter #(not (:seen %)) (:discard corp)))))}}}

   "Information Dynamics: All You Need To Know"
   {:events (let [inf {:req (req (and (not (:disabled card))
                                      (has-most-faction? state :corp "NBN")))
                       :msg "give the Runner 1 tag"
                       :delayed-completion true
                       :effect (effect (tag-runner :runner eid 1))}]
              {:pre-start-game {:effect draft-points-target}
               :agenda-scored inf :agenda-stolen inf})}

   "Jamie \"Bzzz\" Micken: Techno Savant"
   {:events {:pre-start-game {:effect draft-points-target}
             :pre-install {:req (req (and (has-most-faction? state :runner "Shaper")
                                          (pos? (count (:deck runner)))
                                          (first-event? state side :pre-install)))
                           :msg "draw 1 card"
                           :once :per-turn
                           :effect (effect (draw 1))}}}

   "Jemison Astronautics: Sacrifice. Audacity. Success."
   {:events {:corp-forfeit-agenda
             {:delayed-completion true
              :effect (req (show-wait-prompt state :runner "Corp to place advancement tokens")
                           (let [p (inc (get-agenda-points state :corp target))]
                             (continue-ability state side
                               {:prompt "Select a card to place advancement tokens on with Jemison Astronautics: Sacrifice. Audacity. Success."
                                :choices {:req #(and (installed? %) (= (:side %) "Corp"))}
                                :msg (msg "place " p " advancement tokens on " (card-str state target))
                                :cancel-effect (effect (clear-wait-prompt :runner))
                                :effect (effect (add-prop :corp target :advance-counter p {:placed true})
                                                (clear-wait-prompt :runner))}
                              card nil)))}}}

   "Jesminder Sareen: Girl Behind the Curtain"
   {:events {:pre-tag {:once :per-run
                       :req (req (:run @state))
                       :msg "avoid the first tag during this run"
                       :effect (effect (tag-prevent 1))}}}

   "Jinteki: Personal Evolution"
   {:events {:agenda-scored {:interactive (req true)
                             :delayed-completion true
                             :req (req (not (:winner @state)))
                             :msg "do 1 net damage"
                             :effect (effect (damage eid :net 1 {:card card}))}
             :agenda-stolen {:msg "do 1 net damage"
                             :delayed-completion true
                             :req (req (not (:winner @state)))
                             :effect (effect (damage eid :net 1 {:card card}))}}}

   "Jinteki: Potential Unleashed"
   {:events {:pre-resolve-damage
             {:req (req (and (-> @state :corp :disable-id not) (= target :net) (pos? (last targets))))
              :effect (req (let [c (first (get-in @state [:runner :deck]))]
                             (system-msg state :corp (str "uses Jinteki: Potential Unleashed to trash " (:title c)
                                                          " from the top of the Runner's Stack"))
                             (mill state :runner)))}}}

   "Jinteki: Replicating Perfection"
   {:events
    {:runner-phase-12 {:effect (req (apply prevent-run-on-server
                                           state card (map first (get-remotes @state))))}
     :run {:once :per-turn
           :req (req (is-central? (:server run)))
           :effect (req (apply enable-run-on-server
                               state card (map first (get-remotes @state))))}}
    :req (req (empty? (let [successes (turn-events state side :successful-run)]
                        (filter #(is-central? %) successes))))
    :effect (req (apply prevent-run-on-server state card (map first (get-remotes @state))))
    :leave-play (req (apply enable-run-on-server state card (map first (get-remotes @state))))}

   "Jinteki Biotech: Life Imagined"
   {:events {:pre-first-turn {:req (req (= side :corp))
                              :prompt "Choose a copy of Jinteki Biotech to use this game"
                              :choices ["[The Brewery~brewery]" "[The Tank~tank]" "[The Greenhouse~greenhouse]"]
                              :effect (effect (update! (assoc card :biotech-target target))
                                              (system-msg (str "has chosen a copy of Jinteki Biotech for this game ")))}}
    :abilities [{:label "Check chosen flip identity"
                 :effect (req (case (:biotech-target card)
                                "[The Brewery~brewery]"
                                (toast state :corp "Flip to: The Brewery (Do 2 net damage)" "info")
                                "[The Tank~tank]"
                                (toast state :corp "Flip to: The Tank (Shuffle Archives into R&D)" "info")
                                "[The Greenhouse~greenhouse]"
                                (toast state :corp "Flip to: The Greenhouse (Place 4 advancement tokens on a card)" "info")))}
                {:cost [:click 3]
                 :req (req (not (:biotech-used card)))
                 :label "Flip this identity"
                 :effect (req (let [flip (:biotech-target card)]
                                (case flip
                                  "[The Brewery~brewery]"
                                  (do (system-msg state side "uses [The Brewery~brewery] to do 2 net damage")
                                      (damage state side eid :net 2 {:card card})
                                      (update! state side (assoc card :code "brewery")))
                                  "[The Tank~tank]"
                                  (do (system-msg state side "uses [The Tank~tank] to shuffle Archives into R&D")
                                      (shuffle-into-deck state side :discard)
                                      (update! state side (assoc card :code "tank")))
                                  "[The Greenhouse~greenhouse]"
                                  (do (system-msg state side (str "uses [The Greenhouse~greenhouse] to place 4 advancement tokens "
                                                                  "on a card that can be advanced"))
                                      (update! state side (assoc card :code "greenhouse"))
                                      (resolve-ability
                                        state side
                                        {:prompt "Select a card that can be advanced"
                                         :choices {:req can-be-advanced?}
                                         :effect (effect (add-prop target :advance-counter 4 {:placed true}))} card nil)))
                                (update! state side (assoc (get-card state card) :biotech-used true))))}]}

   "Kate \"Mac\" McCaffrey: Digital Tinker"
   {:events {:pre-install {:req (req (and (#{"Hardware" "Program"} (:type target))
                                          (not (get-in @state [:per-turn (:cid card)]))))
                           :effect (effect (install-cost-bonus [:credit -1]))}
             :runner-install {:req (req (and (#{"Hardware" "Program"} (:type target))
                                             (not (get-in @state [:per-turn (:cid card)]))))
                              :silent (req true)
                              :msg (msg "reduce the install cost of " (:title target) " by 1 [Credits]")
                              :effect (req (swap! state assoc-in [:per-turn (:cid card)] true))}}}

   "Ken \"Express\" Tenma: Disappeared Clone"
   {:events {:play-event {:req (req (and (has-subtype? target "Run")
                                         (empty? (filter #(has-subtype? % "Run")
                                                         ;; have to flatten because each element is a list containing
                                                         ;; the Event card that was played
                                                         (flatten (turn-events state :runner :play-event))))))
                          :msg "gain 1 [Credits]"
                          :effect (effect (gain :credit 1))}}}

   "Khan: Savvy Skiptracer"
   {:events {:pass-ice
             {:req (req (first-event? state :corp :pass-ice))
              :delayed-completion true
              :effect (req (if (some #(has-subtype? % "Icebreaker") (:hand runner))
                             (continue-ability state side
                                               {:prompt "Select an icebreaker to install from your Grip"
                                                :choices {:req #(and (in-hand? %) (has-subtype? % "Icebreaker"))}
                                                :delayed-completion true
                                                :msg (msg "install " (:title target))
                                                :effect (effect (install-cost-bonus [:credit -1])
                                                                (runner-install eid target nil))}
                                               card nil)
                             (effect-completed state side eid)))}}}

   "Laramy Fisk: Savvy Investor"
   {:events
    {:successful-run
     {:delayed-completion true
      :interactive (req true)
      :req (req (and (is-central? (:server run))
                     (empty? (let [successes (turn-events state side :successful-run)]
                               (filter #(is-central? %) successes)))))
      :effect (effect (continue-ability
                        {:optional
                         {:prompt "Force the Corp to draw a card?"
                          :yes-ability {:msg "force the Corp to draw 1 card"
                                        :effect (effect (draw :corp))}
                          :no-ability {:effect (effect (system-msg "declines to use Laramy Fisk: Savvy Investor"))}}}
                        card nil))}}}

   "Leela Patel: Trained Pragmatist"
   (let [leela {:interactive (req true)
                :prompt "Select an unrezzed card to return to HQ"
                :choices {:req #(and (not (rezzed? %)) (installed? %) (card-is? % :side :corp))}
                :msg (msg "add " (card-str state target) " to HQ")
                :effect (final-effect (move :corp target :hand))}]
     {:flags {:slow-hq-access (req true)}
      :events {:agenda-scored leela
               :agenda-stolen leela}})

   "Los: Data Hijacker"
   {:events {:rez {:once :per-turn
                   :req (req (ice? target))
                   :msg "gain 2 [Credits]"
                   :effect (effect (gain :runner :credit 2))}}}

   "MaxX: Maximum Punk Rock"
   (let [ability {:msg (msg (let [deck (:deck runner)]
                              (if (pos? (count deck))
                                (str "trash " (join ", " (map :title (take 2 deck))) " from their Stack and draw 1 card")
                                "trash the top 2 cards from their Stack and draw 1 card - but their Stack is empty")))
                  :once :per-turn
                  :effect (effect (mill 2) (draw))}]
     {:flags {:runner-turn-draw true
              :runner-phase-12 (req (and (not (:disabled card))
                                         (some #(card-flag? % :runner-turn-draw true) (all-installed state :runner))))}
      :events {:runner-turn-begins ability}
      :abilities [ability]})

   "Nasir Meidan: Cyber Explorer"
   {:events {:rez {:req (req (and (:run @state)
                                  ;; check that the rezzed item is the encountered ice
                                  (= (:cid target)
                                     (:cid (get-card state current-ice)))))
                   :effect (req (toast state :runner "Click Nasir Meidan: Cyber Explorer to lose all credits and gain credits equal to the rez cost of the newly rezzed ice." "info"))}}
    :abilities [{:req (req (and (:run @state)
                                (:rezzed (get-card state current-ice))))
                 :effect (req (let [current-ice (get-card state current-ice)]
                                (trigger-event state side :pre-rez-cost current-ice)
                                (let [cost (rez-cost state side current-ice)]
                                  (lose state side :credit (:credit runner))
                                  (gain state side :credit cost)
                                  (system-msg state side (str "loses all credits and gains " cost
                                                              " [Credits] from the rez of " (:title current-ice)))
                                  (swap! state update-in [:bonus] dissoc :cost))))}]}

   "NBN: Controlling the Message"
   (let [cleanup (effect (update! :corp (dissoc card :saw-trash)))]
   {:events {:corp-turn-ends {:effect cleanup}
             :runner-turn-ends {:effect cleanup}
             :runner-trash
             {:delayed-completion true
              :req (req (and (not (:saw-trash card))
                             (card-is? target :side :corp)
                             (installed? target)))
              :effect (req (show-wait-prompt state :runner "Corp to use NBN: Controlling the Message")
                           (update! state :corp (assoc card :saw-trash true))
                           (continue-ability
                             state :corp
                             {:optional
                              {:prompt "Trace the Runner with NBN: Controlling the Message?"
                               :yes-ability {:trace {:base 4
                                                     :msg "give the Runner 1 tag"
                                                     :delayed-completion true
                                                     :effect (effect (tag-runner :runner eid 1 {:unpreventable true})
                                                                     (clear-wait-prompt :runner))
                                                     :unsuccessful {:effect (effect (clear-wait-prompt :runner))}}}
                               :no-ability {:effect (effect (clear-wait-prompt :runner))}}}
                             card nil))}}})

   "NBN: Making News"
   {:recurring 2}

   "NBN: The World is Yours*"
   {:effect (effect (gain :hand-size-modification 1))
    :leave-play (effect (lose :hand-size-modification 1))}

   "Near-Earth Hub: Broadcast Center"
   {:events {:server-created {:req (req (first-event? state :corp :server-created))
                              :msg "draw 1 card"
                              :effect (effect (draw 1))}}}

   "Nero Severn: Information Broker"
   {:abilities [{:req (req (has-subtype? current-ice "Sentry"))
                 :once :per-turn
                 :msg "jack out when encountering a Sentry"
                 :effect (effect (jack-out nil))}]}

   "New Angeles Sol: Your News"
   (let [nasol {:optional
                {:prompt "Play a Current?" :player :corp
                 :req (req (not (empty? (filter #(has-subtype? % "Current")
                                                (concat (:hand corp) (:discard corp))))))
                 :yes-ability {:prompt "Select a Current to play from HQ or Archives"
                               :show-discard true
                               :delayed-completion true
                               :choices {:req #(and (has-subtype? % "Current")
                                                    (= (:side %) "Corp")
                                                    (#{[:hand] [:discard]} (:zone %)))}
                               :msg (msg "play a current from " (name-zone "Corp" (:zone target)))
                               :effect (effect (play-instant eid target))}}}]
     {:events {:agenda-scored nasol :agenda-stolen nasol}})

   "NEXT Design: Guarding the Net"
   (let [ndhelper (fn nd [n] {:prompt (msg "When finished, click NEXT Design: Guarding the Net to draw back up to 5 cards in HQ. "
                                           "Select a piece of ICE in HQ to install:")
                              :choices {:req #(and (= (:side %) "Corp")
                                                   (ice? %)
                                                   (in-hand? %))}
                              :effect (req (corp-install state side target nil)
                                           (when (< n 3)
                                             (resolve-ability state side (nd (inc n)) card nil)))})]
     {:events {:pre-first-turn {:req (req (= side :corp))
                                :msg "install up to 3 pieces of ICE and draw back up to 5 cards"
                                :effect (effect (resolve-ability (ndhelper 1) card nil)
                                                (update! (assoc card :fill-hq true)))}}
      :abilities [{:req (req (:fill-hq card))
                   :msg (msg "draw " (- 5 (count (:hand corp))) " cards")
                   :effect (effect (draw (- 5 (count (:hand corp))))
                                   (update! (dissoc card :fill-hq)))}]})

   "Nisei Division: The Next Generation"
   {:events {:psi-game {:msg "gain 1 [Credits]" :effect (effect (gain :corp :credit 1))}}}

   "Noise: Hacker Extraordinaire"
   {:events {:runner-install {:msg "force the Corp to trash the top card of R&D"
                              :effect (effect (mill :corp))
                              :req (req (has-subtype? target "Virus"))}}}

   "Null: Whistleblower"
   {:abilities [{:once :per-turn
                 :req (req (and (:run @state) (rezzed? current-ice)))
                 :prompt "Select a card in your Grip to trash"
                 :choices {:req in-hand?}
                 :msg (msg "trash " (:title target) " and reduce the strength of " (:title current-ice)
                           " by 2 for the remainder of the run")
                 :effect (effect (update! (assoc card :null-target current-ice))
                                 (update-ice-strength current-ice)
                                 (trash target {:unpreventable true}))}]
    :events {:pre-ice-strength
             {:req (req (= (:cid target) (get-in card [:null-target :cid])))
              :effect (effect (ice-strength-bonus -2 target))}
             :run-ends
             {:effect (req (swap! state dissoc-in [:runner :identity :null-target]))}}}

   "Omar Keung: Conspiracy Theorist"
   {:abilities [{:cost [:click 1]
                 :msg "make a run on Archives"
                 :once :per-turn
                 :makes-run true
                 :effect (effect (update! (assoc card :omar-run-activated true))
                                 (run :archives nil (get-card state card)))}]
    :events {:pre-successful-run {:interactive (req true)
                                  :req (req (:omar-run-activated card))
                                  :prompt "Treat as a successful run on which server?"
                                  :choices ["HQ" "R&D"]
                                  :effect (req (let [target-server (if (= target "HQ") :hq :rd)]
                                                 (swap! state update-in [:runner :register :successful-run] #(rest %))
                                                 (swap! state assoc-in [:run :server] [target-server])
                                                 ; remove the :req from the run-effect, so that other cards that replace
                                                 ; access don't use Omar's req.
                                                 (swap! state dissoc-in [:run :run-effect :req])
                                                 (trigger-event state :corp :no-action)
                                                 (swap! state update-in [:runner :register :successful-run] #(conj % target-server))
                                                 (system-msg state side (str "uses Omar Keung: Conspiracy Theorist to make a successful run on " target))))}
             :run-ends {:effect (req (swap! state dissoc-in [:runner :identity :omar-run-activated]))}}}

   "Pālanā Foods: Sustainable Growth"
   {:events {:runner-draw {:req (req (and (first-event? state :corp :runner-draw)
                                          (pos? target)))
                           :msg "gain 1 [Credits]"
                           :effect (effect (gain :corp :credit 1))}}}

   "Quetzal: Free Spirit"
   {:abilities [{:once :per-turn :msg "break 1 Barrier subroutine"}]}

   "Reina Roja: Freedom Fighter"
   {:events {:pre-rez {:req (req (and (ice? target) (not (get-in @state [:per-turn (:cid card)]))))
                       :effect (effect (rez-cost-bonus 1))}
             :rez {:req (req (and (ice? target) (not (get-in @state [:per-turn (:cid card)]))))
                   :effect (req (swap! state assoc-in [:per-turn (:cid card)] true))}}}

   "Rielle \"Kit\" Peddler: Transhuman"
   {:abilities [{:req (req (and (:run @state)
                                (:rezzed (get-card state current-ice))))
                 :once :per-turn :msg (msg "make " (:title current-ice) " gain Code Gate until the end of the run")
                 :effect (req (let [ice current-ice
                                    stypes (:subtype ice)]
                                (update! state side (assoc ice :subtype (combine-subtypes true stypes "Code Gate")))
                                (register-events state side
                                                 {:run-ends {:effect (effect (update! (assoc ice :subtype stypes))
                                                                             (trigger-event :ice-subtype-changed ice)
                                                                             (unregister-events card))}} card)
                                (update-ice-strength state side ice)
                                (trigger-event state side :ice-subtype-changed ice)))}]
    :events {:run-ends nil}}

   "Seidr Laboratories: Destiny Defined"
   {:implementation "Manually triggered"
    :abilities [{:req (req (:run @state))
                 :once :per-turn
                 :prompt "Select a card to add to the top of R&D"
                 :show-discard true
                 :choices {:req #(and (= (:side %) "Corp") (in-discard? %))}
                 :effect (effect (move target :deck {:front true}))
                 :msg (msg "add " (if (:seen target) (:title target) "a card") " to the top of R&D")}]}

   "Silhouette: Stealth Operative"
   {:events {:successful-run
             {:interactive (req (some #(not (rezzed? %)) (all-installed state :corp)))
              :delayed-completion true
              :req (req (and (= target :hq)
                             (first-successful-run-on-server? state :hq)))
              :effect (effect (continue-ability {:choices {:req #(and installed? (not (rezzed? %)))}
                                                 :effect (effect (expose eid target)) :msg "expose 1 card"
                                                 :delayed-completion true }
                                                card nil))}}}

   "Skorpios Defense Systems: Persuasive Power"
   {:implementation "Manually triggered, no restriction on which cards in Heap can be targeted"
    :abilities [{:label "Remove a card in the Heap that was just trashed from the game"
                 :once :per-turn
                 :delayed-completion true
                 :effect (effect (show-wait-prompt :runner "Corp to use Skorpios' ability")
                                 (continue-ability {:prompt "Choose a card in the Runner's Heap that was just trashed"
                                                    :choices (req (cancellable (:discard runner)))
                                                    :msg (msg "remove " (:title target) " from the game")
                                                    :effect (req (move state :runner target :rfg)
                                                                 (clear-wait-prompt state :runner)
                                                                 (effect-completed state side eid))
                                                    :cancel-effect (req (clear-wait-prompt state :runner)
                                                                        (effect-completed state side eid))}
                                                   card nil))}]}

   "Spark Agency: Worldswide Reach"
   {:events
    {:rez {:req (req (and (has-subtype? target "Advertisement")
                          (empty? (filter #(has-subtype? % "Advertisement")
                                          (flatten (turn-events state :corp :rez))))))
           :effect (effect (lose :runner :credit 1))
           :msg (msg "make the Runner lose 1 [Credits] by rezzing an Advertisement")}}}

   "Steve Cambridge: Master Grifter"
   {:events {:successful-run
             {:req (req (and (= target :hq)
                             (first-successful-run-on-server? state :hq)
                             (if (-> @state :run :run-effect :card)
                               (> (count (:discard runner)) 2)
                               (> (count (:discard runner)) 1))))
              :interactive (req true)
              :delayed-completion true
              :effect (effect (continue-ability
                                {:delayed-completion true
                                 :prompt "Select 2 cards in your Heap"
                                 :show-discard true
                                 :choices {:max 2 :req #(and (in-discard? %)
                                                             (= (:side %) "Runner")
                                                             (not= (-> @state :run :run-effect :card :cid) (:cid %)))}
                                 :cancel-effect (req (effect-completed state side eid))
                                 :effect (req (let [c1 (first targets)
                                                    c2 (second targets)]
                                                (show-wait-prompt state :runner "Corp to choose which card to remove from the game")
                                                (continue-ability state :corp
                                                  {:prompt "Choose which card to remove from the game"
                                                   :player :corp
                                                   :choices [c1 c2]
                                                   :effect (req (if (= target c1)
                                                                  (do (move state :runner c1 :rfg)
                                                                      (move state :runner c2 :hand)
                                                                      (system-msg state :runner (str "uses Steve Cambridge: Master Grifter"
                                                                                                     " to add " (:title c2) " to their Grip."
                                                                                                     " Corp removes " (:title c1) " from the game")))
                                                                  (do (move state :runner c2 :rfg)
                                                                      (move state :runner c1 :hand)
                                                                      (system-msg state :runner (str "uses Steve Cambridge: Master Grifter"
                                                                                                     " to add " (:title c1) " to their Grip."
                                                                                                     " Corp removes " (:title c2) " from the game"))))
                                                                (clear-wait-prompt state :runner)
                                                                (effect-completed state side eid))} card nil)))}
                               card nil))}}}

   "Strategic Innovations: Future Forward"
   {:events {:pre-start-game {:effect draft-points-target}
             :runner-turn-ends
             {:req (req (and (not (:disabled card))
                             (has-most-faction? state :corp "Haas-Bioroid")
                             (pos? (count (:discard corp)))))
              :prompt "Select a card in Archives to shuffle into R&D"
              :choices {:req #(and (card-is? % :side :corp) (= (:zone %) [:discard]))}
              :player :corp :show-discard true :priority true
              :msg (msg "shuffle " (if (:seen target) (:title target) "a card")
                        " into R&D")
              :effect (effect (move :corp target :deck)
                              (shuffle! :corp :deck))}}}

   ;; No special implementation
   "Sunny Lebeau: Security Specialist"
   {}

   "SYNC: Everything, Everywhere"
   {:effect (req (when (> (:turn @state) 1)
                   (if (:sync-front card)
                     (tag-remove-bonus state side -1)
                     (trash-resource-bonus state side 2))))
    :events {:pre-first-turn {:req (req (= side :corp))
                              :effect (effect (update! (assoc card :sync-front true)) (tag-remove-bonus -1))}}
    :abilities [{:cost [:click 1]
                 :effect (req (if (:sync-front card)
                                (do (tag-remove-bonus state side 1)
                                    (trash-resource-bonus state side 2)
                                    (update! state side (-> card (assoc :sync-front false) (assoc :code "sync"))))
                                (do (tag-remove-bonus state side -1)
                                    (trash-resource-bonus state side -2)
                                    (update! state side (-> card (assoc :sync-front true) (assoc :code "09001"))))))
                 :msg (msg "flip their ID")}]
    :leave-play (req (if (:sync-front card)
                       (tag-remove-bonus state side 1)
                       (trash-resource-bonus state side -2)))}

   "Synthetic Systems: The World Re-imagined"
   {:events {:pre-start-game {:effect draft-points-target}}
    :flags {:corp-phase-12 (req (and (not (:disabled (get-card state card)))
                                     (has-most-faction? state :corp "Jinteki")
                                     (> (count (filter ice? (all-installed state :corp))) 1)))}
    :abilities [{:prompt "Select two pieces of ICE to swap positions"
                 :choices {:req #(and (installed? %) (ice? %)) :max 2}
                 :once :per-turn
                 :effect (req (when (= (count targets) 2)
                                (swap-ice state side (first targets) (second targets))))
                 :msg (msg "swap the positions of " (card-str state (first targets))
                           " and " (card-str state (second targets)))}]}

   "Tennin Institute: The Secrets Within"
   {:flags {:corp-phase-12 (req (and (not (:disabled (get-card state card)))
                                     (not= 1 (:turn @state)) (not (:successful-run runner-reg))))}
    :abilities [{:msg (msg "place 1 advancement token on " (card-str state target))
                 :choices {:req installed?}
                 :req (req (and (:corp-phase-12 @state) (not (:successful-run runner-reg))))
                 :once :per-turn
                 :effect (effect (add-prop target :advance-counter 1 {:placed true}))}]}

   "The Foundry: Refining the Process"
   {:events
    {:rez {:req (req (and (ice? target) ;; Did you rez and ice just now
                          ;; Are there more copies in the deck or play area (ABT interaction)?
                          ;; (some #(= (:title %) (:title target)) (concat (:deck corp) (:play-area corp)))
                          ;; Based on ruling re: searching and failing to find, we no longer enforce the requirement
                          ;; of there being a target ice to bring into HQ.
                          (empty? (let [rezzed-this-turn (map first (turn-events state side :rez))]
                                    (filter ice? rezzed-this-turn))))) ;; Is this the first ice you've rezzed this turn
           :optional
           {:prompt "Add another copy to HQ?"
            :yes-ability {:effect (req (if-let [found-card (some #(when (= (:title %) (:title target)) %) (concat (:deck corp) (:play-area corp)))]
                                         (do (move state side found-card :hand)
                                             (system-msg state side (str "uses The Foundry to add a copy of "
                                                                         (:title found-card) " to HQ, and shuffles their deck"))
                                             (shuffle! state side :deck))
                                         (do (system-msg state side (str "fails to find a target for The Foundry, and shuffles their deck"))
                                             (shuffle! state side :deck))))}}}}}

   "The Masque: Cyber General"
   {:events {:pre-start-game {:effect draft-points-target}}}

   ;; No special implementation
   "The Professor: Keeper of Knowledge"
   {}

   "The Shadow: Pulling the Strings"
   {:events {:pre-start-game {:effect draft-points-target}}}

   "Titan Transnational: Investing In Your Future"
   {:events {:agenda-scored {:msg (msg "add 1 agenda counter to " (:title target))
                             :effect (effect (add-counter (get-card state target) :agenda 1))}}}

   "Valencia Estevez: The Angel of Cayambe"
   {:events {:pre-start-game
             {:req (req (and (= side :runner)
                             (zero? (get-in @state [:corp :bad-publicity]))))
              :effect (effect (gain :corp :bad-publicity 1))}}}

   "Weyland Consortium: Because We Built It"
   {:recurring 1}

   "Weyland Consortium: Builder of Nations"
   {:implementation "Damage triggered manually"
    :abilities [{:label "Do 1 meat damage"
                 :once :per-turn
                 :prompt "Do a meat damage from identity ability?"
                 :choices (cancellable ["Yes"])
                 :delayed-completion true
                 :effect (req (when (= target "Yes")
                                (damage state side eid :meat 1 {:card card})
                                (system-msg state side "uses Weyland Consortium: Builder of Nations to do 1 meat damage")))}]}

   "Weyland Consortium: Building a Better World"
   {:events {:play-operation {:msg "gain 1 [Credits]"
                              :effect (effect (gain :credit 1))
                              :req (req (has-subtype? target "Transaction"))}}}

   "Whizzard: Master Gamer"
   {:recurring 3}

   "Wyvern: Chemically Enhanced"
   {:events {:pre-start-game {:effect draft-points-target}
             :runner-trash {:req (req (and (has-most-faction? state :runner "Anarch")
                                           (card-is? target :side :corp)
                                           (pos? (count (:discard runner)))))
                            :msg (msg "shuffle " (:title (last (:discard runner))) " into their Stack")
                            :effect (effect (move :runner (last (:discard runner)) :deck)
                                            (shuffle! :runner :deck))}}}})
