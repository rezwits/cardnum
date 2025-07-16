(ns game.cards.hazard
  (:require [game.core :refer :all]
            [game.utils :refer :all]
            [game.macros :refer [effect req msg wait-for continue-ability]]
            [clojure.string :refer [split-lines split join lower-case includes? starts-with?]]
            [clojure.stacktrace :refer [print-stack-trace]]
            [cardnum.utils :refer [str->int]]
            [cardnum.cards :refer [all-cards]]))

(def card-definitions
  {
   "Alone and Unadvised"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Aware of Their Ways"
   {:abilities [{:label "Four"
                 :effect (req
                           (move state side card :discard)
                           (let [opp-side (if (= side :contestant)
                                            :challenger
                                            :contestant)
                                 kount (count (get-in @state [opp-side :discard]))]
                             (loop [k (if (< kount 4) kount 4)]
                               (when (> k 0)
                                 (move state opp-side (rand-nth (get-in @state [opp-side :discard])) :play-area)
                                 (recur (- k 1))))
                             (resolve-ability state side
                                              {:prompt "Select a card to remove from the game"
                                               :choices {:req (fn [t] (card-is? t :side opp-side))}
                                               :effect (req (doseq [c (get-in @state [opp-side :play-area])]
                                                              (if (= c target)
                                                                (move state opp-side c :rfg)
                                                                (move state opp-side c :discard)
                                                                )))} nil nil)))}
                {:label "Six"
                 :effect (req
                           (move state side card :discard)
                           (let [opp-side (if (= side :contestant)
                                            :challenger
                                            :contestant)
                                 kount (count (get-in @state [opp-side :discard]))]
                             (loop [k (if (< kount 6) kount 6)]
                               (when (> k 0)
                                 (move state opp-side (rand-nth (get-in @state [opp-side :discard])) :play-area)
                                 (recur (- k 1))))
                             (resolve-ability state side
                                              {:prompt "Select a card to remove from the game"
                                               :choices {:req (fn [t] (card-is? t :side opp-side))}
                                               :effect (req (doseq [c (get-in @state [opp-side :play-area])]
                                                              (if (= c target)
                                                                (move state opp-side c :rfg)
                                                                (move state opp-side c :discard)
                                                                )))} nil nil)))}]}
   "Bring Our Curses Home"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Cast from the Order"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Covetous Thoughts"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Cruel Claw Perceived"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Desire All for Thy Belly"
   {:abilities [{:label "Spawn"
                 :effect (req (resolve-ability state side
                                               {;:delayed-completion true
                                                :player  side
                                                :prompt  "How Many Spawn or Cancel"
                                                :choices ["1 Spawn" "2 Spawn" "3 Spawn" "4 Spawn"
                                                          "5 Spawn" "6 Spawn" "7 Spawn" "Cancel"]
                                                :effect  (req
                                                           (let [opp-side (if (= side :contestant)
                                                                            :challenger
                                                                            :contestant)
                                                                 spawn (case target
                                                                            "Cancel"
                                                                            0
                                                                            "1 Spawn"
                                                                            1
                                                                            "2 Spawn"
                                                                            2
                                                                            "3 Spawn"
                                                                            3
                                                                            "4 Spawn"
                                                                            4
                                                                            "5 Spawn"
                                                                            5
                                                                            "6 Spawn"
                                                                            6
                                                                            "7 Spawn"
                                                                            7)
                                                                 kount (count (get-in @state [opp-side :deck]))]
                                                             (loop [k (if (<= kount spawn) kount spawn)]
                                                               (when (> k 0)
                                                                 (move state side (assoc (first (get-in @state [opp-side :deck])) :swap true) :current)
                                                                 (recur (- k 1))))
                                                             ;(effect-completed state side nil)
                                                             ))} nil nil))
                 :msg (msg " to look at cards from the top of your deck")}
                {:label "Propose" ;; reveal to opponent
                 :effect (req (resolve-ability state side
                                               {;:delayed-completion true
                                                :player  side
                                                :prompt  "Which Card Left to Right or Cancel"
                                                :choices ["Card 1" "Card 2" "Card 3" "Card 4"
                                                          "Card 5" "Card 6" "Card 7" "Cancel"]
                                                :effect  (req
                                                           (let [opp-side (if (= side :contestant)
                                                                            :challenger
                                                                            :contestant)
                                                                 the-side (if (= 0 (count (get-in @state [side :current])))
                                                                            opp-side
                                                                            side)
                                                                 spawn (case target
                                                                         "Cancel"
                                                                         7
                                                                         "Card 1"
                                                                         0
                                                                         "Card 2"
                                                                         1
                                                                         "Card 3"
                                                                         2
                                                                         "Card 4"
                                                                         3
                                                                         "Card 5"
                                                                         4
                                                                         "Card 6"
                                                                         5
                                                                         "Card 7"
                                                                         6)
                                                                 ;kount (count (get-in @state [opp-side :deck]))
                                                                 ]
                                                             (when (< spawn 7)
                                                               (move state opp-side (dissoc (nth (get-in @state [the-side :current]) spawn nil) :swap) :play-area {:front true}))
                                                             ;(effect-completed state side nil)
                                                             ))} nil nil))
                 :msg (msg " to propose a card")}
                {:label "Shuffle"
                 :effect (req
                           (move state side card :rfg)
                           (resolve-ability state side
                                               {:effect (req
                                                          (let [opp-side (if (= side :contestant)
                                                                           :challenger
                                                                           :contestant)
                                                                the-side (if (= 0 (count (get-in @state [side :play-area])))
                                                                           opp-side
                                                                           side)
                                                                kount (count (get-in @state [the-side :play-area]))]
                                                            (loop [k kount]
                                                              (when (> k 0)
                                                                (move state side (assoc (first (get-in @state [the-side :play-area])) :swap true) :current)
                                                                (recur (- k 1))))
                                                            )
                                                          (let [opp-side (if (= side :contestant)
                                                                           :challenger
                                                                           :contestant)]
                                                            (loop [k (count (get-in @state [side :current]))]
                                                              (when (> k 0)
                                                                (move state opp-side (dissoc (rand-nth (get-in @state [side :current])) :swap) :deck {:front true})
                                                                (recur (- k 1)))))
                                                          )} nil nil))
                 :msg (msg " to shuffle cards back to the top of your deck")}
                ]}
   "Despair of the Heart"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Diminish and Depart"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Dragons Curse"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Enchanted Stream"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Fear of Kin"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Fled into Darkness"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Flies and Spiders"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Foes Shall Fall"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Fools Bane"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Foolish Words"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Goblin-faces"
   {:abilities [{:label "Strikes"
                :effect (req (resolve-ability state side
                                              {:delayed-completion true
                                               :player  side
                                               :prompt  "How Many Succesful Strikes?"
                                               :choices ["1 Strike" "2 Strikes" "3 Strikes" "4 Strikes"
                                                         "5 Strikes" "6 Strikes" "7 Strikes" "Cancel"]
                                               :effect  (req
                                                         (let [opp-side (if (= side :contestant)
                                                                          :challenger
                                                                          :contestant)
                                                               factions (case target
                                                                          "Cancel"
                                                                          0
                                                                          "1 Strike"
                                                                          1
                                                                          "2 Strikes"
                                                                          2
                                                                          "3 Strikes"
                                                                          3
                                                                          "4 Strikes"
                                                                          4
                                                                          "5 Strikes"
                                                                          5
                                                                          "6 Strikes"
                                                                          6
                                                                          "7 Strikes"
                                                                          7)
                                                               kount (count (get-in @state [opp-side :deck]))]
                                                           (loop [k (if (< kount factions) kount factions)]
                                                             (when (> k 0)
                                                               (move state side (assoc (first (get-in @state [opp-side :deck])) :swap true) :current {:front true})
                                                               (recur (- k 1))))
                                                           (effect-completed state side nil)))} nil nil))
                 :msg (msg " look at the top of your deck")}
                {:label "Putback"
                 :effect (req
                          (let [opp-side (if (= side :contestant)
                                           :challenger
                                           :contestant)
                                current-count (count (get-in @state [side :current]))]
                            (letfn [(putback-loop [k]
                                                  (when (>= k 0)
                                                    (resolve-ability
                                                     state side
                                                     {:async true
                                                      :prompt "Click Top, Bottom, Next, or Done"
                                                      :choices ["Top" "Bottom" "Next" "Done"]
                                                      :effect (req
                                                               (case target
                                                                 "Top"
                                                                 (do
                                                                   (move state opp-side
                                                                         (dissoc (nth (get-in @state [side :current]) k nil) :swap)
                                                                         :deck {:front true})
                                                                   (effect-completed state side nil)
                                                                   (putback-loop (dec k)))
                                                                 "Bottom"
                                                                 (do
                                                                   (move state opp-side
                                                                         (dissoc (nth (get-in @state [side :current]) k nil) :swap)
                                                                         :deck)
                                                                   (effect-completed state side nil)
                                                                   (putback-loop (dec k)))
                                                                 "Next"
                                                                 (do
                                                                   (let [current-cards (get-in @state [side :current])
                                                                         top-card (first current-cards)
                                                                         rest-cards (subvec current-cards 1)]
                                                                     (when top-card
                                                                       ;; Rotate top card to bottom
                                                                       (swap! state assoc-in [side :current] (conj rest-cards top-card)))
                                                                     (effect-completed state side nil)
                                                                     (putback-loop k)))
                                                                 "Done"
                                                                 (effect-completed state side nil)))}
                                                     nil nil)))] ; end letfn
                              (putback-loop (dec current-count)))))
                 :msg (msg " scatter the top and/or bottom cards of your deck")}
                ]}
   "Grasping and Ungracious"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Great Secrets Buried There"
   {:abilities [{:label "Not My Deck"
                 :effect (req
                           (let [opp-side (if (= side :contestant)
                                            :challenger
                                            :contestant)
                                 secrets (get-card state card)
                                 kount (count (get-in @state [opp-side :deck]))]
                             (loop [k (if (< kount 10) 0 10)]
                               (when (> k 0)
                                 (move state opp-side (first (get-in @state [opp-side :deck])) :current)
                                 (recur (- k 1))))
                             (resolve-ability state opp-side
                                              {:prompt "Select an item for Great Secrets..."
                                               :choices {:req (fn [t] (card-is? t :side opp-side))}
                                               :effect (req (doseq [c (get-in @state [opp-side :current])]
                                                              (if (= c target)
                                                                (host state opp-side secrets c)
                                                                (move state opp-side c :deck)))
                                                            (shuffle! state opp-side :deck))
                                               } nil nil)))}
                {:label "No item" ;; reveal to opponent
                 :effect (req
                           (let [opp-side (if (= side :contestant)
                                            :challenger
                                            :contestant)
                                 the-side (if (= 0 (count (get-in @state [side :current])))
                                            opp-side
                                            side)]
                             (resolve-ability state the-side
                                              {:effect (req (doseq [c (get-in @state [the-side :current])]
                                                                (move state the-side c :play-area)))
                                               } nil nil)))}
                {:label "Shuffle"
                 :effect (req
                           (let [opp-side (if (= side :contestant)
                                            :challenger
                                            :contestant)
                                 the-side (if (= 0 (count (get-in @state [side :play-area])))
                                            opp-side
                                            side)]
                             (resolve-ability state the-side
                                              {:effect (req (doseq [c (get-in @state [the-side :play-area])]
                                                              (move state the-side c :deck))
                                                            (shuffle! state the-side :deck))
                                               } nil nil)))}
                ]}
   "He is Lost to Us"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Heritage Forsaken"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Icy Touch"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "In the Grip of Ambition"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Inner Rot"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Long Dark Reach"
   {:abilities [{:label "Top seven"
                 :effect (req
                           (if (< 9 (count (get-in @state [side :deck])))
                             (loop [k 7]
                               (when (> k 0)
                                 (move state side (first (get-in @state [side :deck])) :play-area)
                                 (recur (- k 1)))))
                           )}
                {:label "Top shuffle"
                 :effect (req (move state side card :discard)
                              (loop [k (count (get-in @state [side :play-area]))]
                                (when (> k 0)
                                  (move state side (rand-nth (get-in @state [side :play-area])) :deck {:front true})
                                  (recur (- k 1))))
                              )}]}
   "Longing for the West"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Lure of Creation"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Lure of Expedience"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Lure of Nature"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Lure of the Senses"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Many Burdens"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Memories Stolen"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Morgul-knife"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Nothing to Eat or Drink"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Out of Practice"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Pale Dream-maker"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Plague"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Politics"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Power Relinquished to Artifice"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Ransom"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Rebel-talk"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Revealed to all Watchers"
   {:abilities [{:label "Revealed"
                 :effect (req (resolve-ability state side
                                               {:msg (msg "reveal hand")
                                                :effect (req (doseq [c (get-in @state [side :hand])]
                                                               (move state side c :play-area)))
                                                } nil nil))}
                {:label "Hide"
                 :effect (req (resolve-ability state side
                                               {:msg (msg "stack deck")
                                                :effect (req (doseq [c (get-in @state [side :play-area])]
                                                               (move state side c :current)))
                                                } nil nil))}
                ]}
   "Shut Yer Mouth"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "So Youve Come Back"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Something Else at Work"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Something Has Slipped"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Spells of the Barrow-wights"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Summons from Long Sleep"
   {:abilities [{:label "With"
                 :effect (req (let [r (get-card state card)
                                    hosted (count (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if (= hosted 1)
                                                            (msg "There is already a creature hosted")
                                                            (msg "Click a applicable creature from hand"))
                                                  :choices {:req (fn [t] (card-is? t :side side))}
                                                  :msg (msg "place " (card-str state target) " off to the side")
                                                  :effect (effect (host card target))} card nil)))}]}
   "Taint of Glory"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "The Burden of Time"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "The Pale Sword"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Tookish Blood"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Wielders Curse"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Will You Not Come Down?"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   "Wound of Long Burden"
   {:hosting {:req #(and (is-type? % "Character") (revealed? %))}}
   })
