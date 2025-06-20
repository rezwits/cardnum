(ns game.cards.resources
  (:require [game.core :refer :all]
            [game.utils :refer :all]
            [game.macros :refer [effect req msg wait-for continue-ability while-let]]
            [clojure.string :refer [split-lines split join lower-case includes? starts-with?]]
            [clojure.stacktrace :refer [print-stack-trace]]
            [cardnum.utils :refer [str->int]]
            [cardnum.cards :refer [all-cards]]))

(def card-definitions
  {
   "A Flush of Air"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Elrond"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "A New Ringlord"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on your Avatar"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "A Worthy Substitute"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (site? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different Site")
                                                            (msg "Place this card on a Site"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (site? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host-reveal target card))} card nil)))}]}
   "Against All Counsel"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on your Elf-lord"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Align Palantír"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different Palantîr")
                                                            (msg "Place this card on a Palantîr"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (can-host? %))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Ancient Knowledge"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a elf"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Ancient Skill and Wisdom"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on an elf"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Andúril, the Flame of the West"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on the sage"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}

   "Arcane School"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a character"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Ard Once Vain"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different ringwraith")
                                                            (msg "Place this card on your Ringwraith"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Await the Advent of Allies"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a non-wizard"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Bade to Rule"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different ringwraith")
                                                            (msg "Place this card on your Ringwraith"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Balin, Dwarf of All Trades"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Balin"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Black Rider"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card your Ringwraith"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Blades of Sorcery"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card a Ringwraith"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Blood of Huan"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (ally? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different ally")
                                                            (msg "Place this card on a character with a noble hound"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (ally? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Bow of Alatar"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Alatar"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "By the Ringwraiths Word"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a character"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Captain of the House"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Glorfindel"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Choice of Lúthien"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Arwen"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Collar of Spikes"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (ally? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on another ally")
                                                            (msg "Place this card on an ally"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (ally? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Creating Their Domain"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card your Ringwraith"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Crept Along Carefully"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (msg "Place on a character that represents company")
                                                  :choices {:req #(if (or hosted? (not hosted?))
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Crimson Hood"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Balin"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Dark Forges"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a character"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Dark Tryst"
   {:abilities [{:label "Resolve"
                 :msg (msg "to draw 3 cards.")
                 :effect (req (draw state side 3) (move state side card :rfg))}]}
   "Dog-lord of Waw"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on your Ringwraith"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Dragon-lore"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a Sage"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Dreams of Lore"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on the sage"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Durins Day"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a dwarf"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Dwarf-friend"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a non-dwarf, non-wizard"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Dwarven Alchemist"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a dwarf"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Dwarven Art of War"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a dwarf leader"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Elf-friend"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a non-elf, non-wizard"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Eyes of Mandos"
   {:abilities [{:label "Reveal"
                 :effect (req
                           (let [kount (count (get-in @state [side :deck]))]
                             (when (> kount 0)
                               (move state side (first (get-in @state [side :deck])) :play-area))))}
                {:label "Select"
                 :effect (req (resolve-ability state side
                                               {:prompt "Select a card to put in your hand"
                                                :choices {:req (fn [t] (card-is? t :side side))}
                                                :effect (req (doseq [c (get-in @state [side :play-area])]
                                                               (if (= c target)
                                                                 (move state side c :hand)
                                                                 (move state side c :deck)))
                                                             (shuffle! state side :deck))
                                                :msg (msg "picks a card and shuffles")
                                                } nil nil))}]}
   "Fate of the Ithil-stone"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (msg "Which character represents the company")
                                                  :choices {:req #(and (character? %)
                                                                       (can-host? %))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Favor of the Valar"
   {:abilities [{:label "Perform"
                 :effect (req (doseq [c (get-in @state [side :discard])]
                                (when (= "Site" (:type c))
                                  (move state side c :play-area)))
                              (shuffle-into-deck state side :hand :discard)
                              (draw state side 8)
                              (doseq [c (get-in @state [side :play-area])]
                                (when (= "Site" (:type c))
                                  (move state side c :discard)))
                              (move state side card :rfg)
                              )}]}
   "Favoured by the Eye"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card your Ringwraith"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Fell Rider"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card your Ringwraith"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}   "Fellowship"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "Discard this card if the company changes composition")
                                                            (msg "Place this card with a character/company"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Free to Choose"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card with a character"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Focus Palantír"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different Palantîr")
                                                            (msg "Place this card on a Palantîr"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (can-host? %))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Fortress of the Towers"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (site? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "Place this on a different copy")
                                                            (msg "Place this card on The White Towers"))
                                                  :choices {:req #(if (site? %) (can-host? %))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host-reveal target card))} card nil)))}]}
   "Fosterhome of Royal Heirs"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Elrond"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Gandalfs Friend"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card with a character"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Gimlis Axe"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Gimili with an Axe"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Give Welcome to the Unexpected"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this on Gandalf"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Going Ever Under Dark"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (msg "Place on a character that represents company")
                                                  :choices {:req #(if (or hosted? (not hosted?))
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Great Achievement"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on your Avatar"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Great Bow of Lórien"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Legolas or Fanar bearing Galadhrim"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Great Friendship"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Gimli or Legolas"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Grey Embassy"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Gandalf"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Grim Morn and Golden Sunset"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Théoden King"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Grim Voiced and Grim Faced"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Bard Bowman"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Guarded Haven"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (site? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different Haven")
                                                            (msg "Place this card on a (Hidden) Haven"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (site? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host-reveal target card))} card nil)))}]}
   "Guardian Spirit"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (or (ally? (:host r)) (character? (:host r)))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on another character or ally")
                                                            (msg "Place this card on an character or ally"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (or (character? %) (ally? %))
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Have You Seen Baggins?"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (ally? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on another ally")
                                                            (msg "Place this card on an ally"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (ally? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Helm of Her Secrecy"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card with a character"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Heralded Lord"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card your Ringwraith"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Herb-lore"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Radagast"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Hidden Haven"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (site? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different site")
                                                            (msg "Place this card on a site"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (site? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host-reveal target card))} card nil)))}]}
   "His Beard Long and Forked"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a dwarf"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Hobbit-lore"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Gandalf"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Hionvor of Womawas Drus"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on your Ringwraith"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Horror Spreads before Them"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a Ringwraith"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Huntsmans Garb"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Alatar"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Into the Smoking Cone"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (msg "Which character represents the company")
                                                  :choices {:req #(and (character? %)
                                                                       (can-host? %))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Ironfoot"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Dáin II"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "King of Seven Houses"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on your Dwarf-lord"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "King under the Mountain"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a dwarf"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Kinsman and Ambassador"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Círdan"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Knowledge of Ardas Treasures"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on your Dwarf-lord"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Knowledge of the Enemy"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card with a character"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Lady of Many Gifts"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Galadriel"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Lady of Many Songs"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Galadriel"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Lady of Many Visions"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Galadriel"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Last of the Seven"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Thráin"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Light of Lothlórien"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Galadriel"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Long-time Visitor"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Elrond"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Longbottom Leaf"
   {:abilities [{:label "Resolve"
                 :effect (req (move state side card :rfg))}]}
   "Looking from Mind to Mind"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a character"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Lord of Dwarrowdelf"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a Dwarf-lord"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Lore of the Ages"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on an elf"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Lucky Search"
   {:abilities [{:label "Search"
                 :effect (req (while (and (not (some (partial = (:Secondary (first (get-in @state [side :deck]))))
                                                     ["Greater Item" "Major Item"
                                                      "Minor Item" "Gold Ring Item"]))
                                          (not-empty (take 1 (get-in @state [side :deck]))))
                                (move state side (first (get-in @state [side :deck])) :play-area))
                              (when (not-empty (take 1 (get-in @state [side :deck])))
                                (move state side (first (get-in @state [side :deck])) :play-area))
                              )}
                {:label "Discard"
                 :effect (req (doseq [c (get-in @state [side :play-area])]
                                (move state side c :deck))
                              (shuffle! state side :deck)
                              (move state side card :discard)
                              (system-msg state side "uses metw_luckysearch.jpg to return cards and shuffle")
                              )}]}
   "Many-coloured Robes"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Saruman"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Master of Shapes"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Radagast"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Memories of Old Torture"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on another character")
                                                            (msg "Place this card on a character"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (= (last (:zone %)) :characters)
                                                                         (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Mighty in Their Day"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on your Ringwraith"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Mirror of Galadriel"
   {:abilities [{:label "Yours"
                 :effect (req
                           (let [kount (count (get-in @state [side :deck]))]
                             (loop [k (if (< kount 5) kount 5)]
                               (when (> k 0)
                                 (move state side (first (get-in @state [side :deck])) :current)
                                 (recur (- k 1))))
                             (resolve-ability state side
                                              {:delayed-completion true
                                               :player  side
                                               :prompt  "Click done when ready"
                                               :choices ["Done"]
                                               :effect  (req (case target
                                                               "Done"
                                                               (loop [k (count (get-in @state [side :current]))]
                                                                 (when (> k 0)
                                                                   (move state side (rand-nth (get-in @state [side :current])) :deck {:front true})
                                                                   (recur (- k 1)))))
                                                             (effect-completed state side nil)
                                                             (system-msg state side "uses metw_mirrorofgaladriel.jpg to look at his top 5 cards")
                                                             )} nil nil)))}
                {:label "Theirs"
                 :effect (req
                           (let [opp-side (if (= side :contestant)
                                            :challenger
                                            :contestant)
                                 kount (count (get-in @state [opp-side :deck]))]
                             (loop [k (if (< kount 5) kount 5)]
                               (when (> k 0)
                                 (move state side (assoc (first (get-in @state [opp-side :deck])) :swap true) :current)
                                 (recur (- k 1))))
                             (resolve-ability state side
                                              {:delayed-completion true
                                               :player  side
                                               :prompt  "Click done when ready"
                                               :choices ["Done"]
                                               :effect  (req (case target
                                                               "Done"
                                                               (loop [k (count (get-in @state [side :current]))]
                                                                 (when (> k 0)
                                                                   (move state opp-side (dissoc (rand-nth (get-in @state [side :current])) :swap) :deck {:front true})
                                                                   (recur (- k 1)))))
                                                             (effect-completed state side nil)
                                                             (system-msg state side "uses metw_mirrorofgaladriel.jpg to look at your top 5 cards")
                                                             )} nil nil)))}]}
   "Mithrandir"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (msg "Which character represents the company")
                                                  :choices {:req #(and (character? %)
                                                                       (can-host? %))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Moon-runes"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Elrond"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "More Than Meets the Eye"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a Dwarf-lord"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Morinehtar"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (msg "Which character represents the company")
                                                  :choices {:req #(and (character? %)
                                                                       (can-host? %))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Narya"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Gandalf"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Nenya"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Galadriel"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "New Closeness to His Kin"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on an elf"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Nursed with Fell Meats"
   {:abilities [{:label "With"
                 :effect (req (let [r (get-card state card)
                                    hosted (count (:host r))]
                                (resolve-ability state side
                                                 {:prompt (msg "Pick a Black Horse or Winged Terror")
                                                  :choices {:req (fn [t] (card-is? t :side side))}
                                                  :msg (msg "place " (card-str state target) " off to the side")
                                                  :effect (effect (host card target))} card nil)))}]}
   "Oakenshield"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Thorin II"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Of the Race of Dale"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Bard Bowman, Bain, or Brand"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}

   "Open to the Summons"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on an agent"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Orc-garrison"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (site? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different site")
                                                            (msg "Place this card with a Region, SH or DH"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (or (site? %) (region? %))
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host-reveal target card))} card nil)))}]}
   "Orders from Lugbúrz"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "Move to your hand to re-assign company")
                                                            (msg "Place this card with a company"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Overlord of Dol Guldur"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on your Ringwraith"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Overlord of Minas Morgul"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on your Ringwraith"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Palantír of Minas Ithil"
   {:abilities [{:label "Yours-if"
                 :effect (req
                           (let [kount (count (get-in @state [side :deck]))]
                             (loop [k (if (< kount 5) kount 5)]
                               (when (> k 0)
                                 (move state side (first (get-in @state [side :deck])) :current)
                                 (recur (- k 1))))
                             (resolve-ability state side
                                              {:delayed-completion true
                                               :player  side
                                               :prompt  "Click done when ready"
                                               :choices ["Done"]
                                               :effect  (req (case target
                                                               "Done"
                                                               (loop [k (count (get-in @state [side :current]))]
                                                                 (when (> k 0)
                                                                   (move state side (rand-nth (get-in @state [side :current])) :deck {:front true})
                                                                   (recur (- k 1)))))
                                                             (effect-completed state side nil)
                                                             (system-msg state side "uses Palantír of Minas Tirith to look at his top 5 cards")
                                                             )} nil nil)))}
                {:label "Theirs-if"
                 :effect (req
                           (let [opp-side (if (= side :contestant)
                                            :challenger
                                            :contestant)
                                 kount (count (get-in @state [opp-side :deck]))]
                             (loop [k (if (< kount 5) kount 5)]
                               (when (> k 0)
                                 (move state side (assoc (first (get-in @state [opp-side :deck])) :swap true) :current)
                                 (recur (- k 1))))
                             (resolve-ability state side
                                              {:delayed-completion true
                                               :player  side
                                               :prompt  "Click done when ready"
                                               :choices ["Done"]
                                               :effect  (req (case target
                                                               "Done"
                                                               (loop [k (count (get-in @state [side :current]))]
                                                                 (when (> k 0)
                                                                   (move state opp-side (dissoc (rand-nth (get-in @state [side :current])) :swap) :deck {:front true})
                                                                   (recur (- k 1)))))
                                                             (effect-completed state side nil)
                                                             (system-msg state side "uses Palantír of Minas Tirith to look at your top 5 cards")
                                                             )} nil nil)))}]}
   "Palantír of Minas Tirith"
   {:abilities [{:label "Yours"
                 :effect (req
                           (let [kount (count (get-in @state [side :deck]))]
                             (loop [k (if (< kount 5) kount 5)]
                               (when (> k 0)
                                 (move state side (first (get-in @state [side :deck])) :current)
                                 (recur (- k 1))))
                             (resolve-ability state side
                                              {:delayed-completion true
                                               :player  side
                                               :prompt  "Click done when ready"
                                               :choices ["Done"]
                                               :effect  (req (case target
                                                               "Done"
                                                               (loop [k (count (get-in @state [side :current]))]
                                                                 (when (> k 0)
                                                                   (move state side (rand-nth (get-in @state [side :current])) :deck {:front true})
                                                                   (recur (- k 1)))))
                                                             (effect-completed state side nil)
                                                             (system-msg state side "uses Palantír of Minas Tirith to look at his top 5 cards")
                                                             )} nil nil)))}
                {:label "Theirs"
                 :effect (req
                           (let [opp-side (if (= side :contestant)
                                            :challenger
                                            :contestant)
                                 kount (count (get-in @state [opp-side :deck]))]
                             (loop [k (if (< kount 5) kount 5)]
                               (when (> k 0)
                                 (move state side (assoc (first (get-in @state [opp-side :deck])) :swap true) :current)
                                 (recur (- k 1))))
                             (resolve-ability state side
                                              {:delayed-completion true
                                               :player  side
                                               :prompt  "Click done when ready"
                                               :choices ["Done"]
                                               :effect  (req (case target
                                                               "Done"
                                                               (loop [k (count (get-in @state [side :current]))]
                                                                 (when (> k 0)
                                                                   (move state opp-side (dissoc (rand-nth (get-in @state [side :current])) :swap) :deck {:front true})
                                                                   (recur (- k 1)))))
                                                             (effect-completed state side nil)
                                                             (system-msg state side "uses Palantír of Minas Tirith to look at your top 5 cards")
                                                             )} nil nil)))}]}
   "Palantír of Osgiliath"
   {:abilities [{:label "Yours-if"
                 :effect (req
                           (let [kount (count (get-in @state [side :deck]))]
                             (loop [k (if (< kount 5) kount 5)]
                               (when (> k 0)
                                 (move state side (first (get-in @state [side :deck])) :current)
                                 (recur (- k 1))))
                             (resolve-ability state side
                                              {:delayed-completion true
                                               :player  side
                                               :prompt  "Click done when ready"
                                               :choices ["Done"]
                                               :effect  (req (case target
                                                               "Done"
                                                               (loop [k (count (get-in @state [side :current]))]
                                                                 (when (> k 0)
                                                                   (move state side (rand-nth (get-in @state [side :current])) :deck {:front true})
                                                                   (recur (- k 1)))))
                                                             (effect-completed state side nil)
                                                             (system-msg state side "uses Palantír of Minas Tirith to look at his top 5 cards")
                                                             )} nil nil)))}
                {:label "Theirs-if"
                 :effect (req
                           (let [opp-side (if (= side :contestant)
                                            :challenger
                                            :contestant)
                                 kount (count (get-in @state [opp-side :deck]))]
                             (loop [k (if (< kount 5) kount 5)]
                               (when (> k 0)
                                 (move state side (assoc (first (get-in @state [opp-side :deck])) :swap true) :current)
                                 (recur (- k 1))))
                             (resolve-ability state side
                                              {:delayed-completion true
                                               :player  side
                                               :prompt  "Click done when ready"
                                               :choices ["Done"]
                                               :effect  (req (case target
                                                               "Done"
                                                               (loop [k (count (get-in @state [side :current]))]
                                                                 (when (> k 0)
                                                                   (move state opp-side (dissoc (rand-nth (get-in @state [side :current])) :swap) :deck {:front true})
                                                                   (recur (- k 1)))))
                                                             (effect-completed state side nil)
                                                             (system-msg state side "uses Palantír of Minas Tirith to look at your top 5 cards")
                                                             )} nil nil)))}]}
   "Pallandos Apprentice"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a character"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Pallandos Hood"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Pallando"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Piercing All Shadows"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a ranger"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Pipe Weed"
   {:abilities [{:label "Look"
                 :effect (req
                           (let [kount (count (get-in @state [side :deck]))]
                             (loop [k (if (< kount 5) kount 5)]
                               (when (> k 0)
                                 (move state side (first (get-in @state [side :deck])) :current)
                                 (recur (- k 1))))
                             (resolve-ability state side
                                              {:delayed-completion true
                                               :player  side
                                               :prompt  "Click done when ready"
                                               :choices ["Done"]
                                               :effect  (req (case target
                                                               "Done"
                                                               (loop [k (count (get-in @state [side :current]))]
                                                                 (when (> k 0)
                                                                   (move state side (last (get-in @state [side :current])) :deck {:front true})
                                                                   (recur (- k 1)))))
                                                             (effect-completed state side nil)
                                                             (system-msg state side "uses Pipe Weed to look at his top 5 cards")
                                                             )} nil nil)))}]}
   "Pocketed Robes"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Radagast"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Prince of Mirkwood"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Thranduil"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Promise of Treasure"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a non-avatar dwarf"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Promptings of Wisdom"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a ranger"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Ready to His Will"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on another character")
                                                            (msg "Place this card on a character"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (= (last (:zone %)) :characters)
                                                                         (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Reforging"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on the sage"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Rescue Prisoners"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a character"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Return of the King"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Aragorn II"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Ring of Air"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Elrond"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Ring of Fire"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Gandalf"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Ring of Water"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Galadriel"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Ring-drawn"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on your Ringwraith"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Rivermen of the Anduin Vales"
   {:abilities [{:label "Fish"
                 :effect (req
                           (let [kount (count (get-in @state [side :deck]))]
                             (loop [k (if (< kount 5) kount 5)]
                               (when (> k 0)
                                 (move state side (first (get-in @state [side :deck])) :play-area)
                                 (recur (- k 1))))
                             (resolve-ability state side
                                              {:delayed-completion true
                                               :player  side
                                               :prompt "Put a ring item in hand, or click done"
                                               :choices ["Done"]
                                               :effect (req (case target
                                                              "Done"
                                                              (do (loop [k (count (get-in @state [side :play-area]))]
                                                                    (when (> k 0)
                                                                      (move state side (first (get-in @state [side :play-area])) :deck {:front true})
                                                                      (recur (- k 1))))
                                                                  (shuffle! state side :deck))))
                                               :msg (msg "find a ring item")
                                               } nil nil)))}]}
   "Rómestámo"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (msg "Which character represents the company")
                                                  :choices {:req #(and (character? %)
                                                                       (can-host? %))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Royal Duo"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Galadriel"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Rumours of Rings"
   {:abilities [{:label "With"
                 :effect (req (let [r (get-card state card)
                                    hosted (count (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if (= hosted 2)
                                                            (msg "There are two rings with Rumours")
                                                            (msg "Pick a ring to place off to the side"))
                                                  :choices {:req (fn [t] (card-is? t :side side))}
                                                  :msg (msg "place " (card-str state target) " off to the side")
                                                  :effect (effect (host card target))} card nil)))}]}
   "Sacrifice of Form"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on your returned Wizard"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Sarumans Ring"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Saruman"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Secret News"
   {:abilities [{:label "News"
                 :effect (req
                           (let [opp-side (if (= side :contestant)
                                            :challenger
                                            :contestant)
                                 kount (count (get-in @state [opp-side :deck]))]
                             (move state side card :discard)
                             (loop [k (if (< kount 5) kount 5)]
                               (when (> k 0)
                                 (move state side (rand-nth (get-in @state [opp-side :hand])) :play-area)
                                 (recur (- k 1))))
                             (resolve-ability state side
                                              {:delayed-completion true
                                               :player  side
                                               :prompt  "Click done when ready"
                                               :choices ["Done"]
                                               :effect  (req (case target
                                                               "Done"
                                                               (loop [k (count (get-in @state [side :play-area]))]
                                                                 (when (> k 0)
                                                                   (move state opp-side (first (get-in @state [side :play-area])) :hand {:front true})
                                                                   (recur (- k 1)))))
                                                             (effect-completed state side nil)
                                                             (system-msg state side "uses metd_secretnews.jpg to look at your top 5 cards")
                                                             )} nil nil)))}]}

   "Shelob Ahungered"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (ally? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on another ally")
                                                            (msg "Place this card on an ally"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (ally? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Shifter of Hues"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Radagast"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Sneakin"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (msg "Place this card on a Scout")
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Son of Dáin"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Thorin III Stonehelm"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Son of Fundin"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Balin"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Son of Náin"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Dáin"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Son of Thráin"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Thorin"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Son of Thrór"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Thráin"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Squire of the Hunt"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a character"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Stave of Pallando"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Pallando"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Stealth"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (msg "Place this card on a Scout")
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Swordmaster"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a character"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Sworn to the Lord"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on your Lord"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "That's Been Heard Before Tonight"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on character"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "The Fiery Blade"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on your Ringwraith"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "The Forge-master"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a character"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "The Fortress of Isen"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (site? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "Place this on a different copy")
                                                            (msg "Place this card on Isengard"))
                                                  :choices {:req #(if (site? %) (can-host? %))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host-reveal target card))} card nil)))}]}
   "The Great Hunt"
   {:abilities [{:label "Hunt Deck"
                 :effect (req (let [opp-side (if (= side :contestant)
                                               :challenger
                                               :contestant)]
                                (while (and (not (some (partial = (:Secondary (first (get-in @state [opp-side :deck]))))
                                                       ["Creature" "Creature/Short-event"
                                                        "Creature/Permanent-event" "Creature/Long-event"]))
                                            (not-empty (take 1 (get-in @state [opp-side :deck]))))
                                  (move state opp-side (first (get-in @state [opp-side :deck])) :play-area))
                                (when (not-empty (take 1 (get-in @state [opp-side :deck])))
                                  (move state opp-side (first (get-in @state [opp-side :deck])) :play-area))
                                ))}
                {:label "Hunt Discard"
                 :effect (req (let [opp-side (if (= side :contestant)
                                               :challenger
                                               :contestant)]
                                (while (and (not (some (partial = (:Secondary (last (get-in @state [opp-side :discard]))))
                                                       ["Creature" "Creature/Short-event"
                                                        "Creature/Permanent-event" "Creature/Long-event"]))
                                            (not-empty (take 1 (get-in @state [opp-side :discard]))))
                                  (move state opp-side (last (get-in @state [opp-side :discard])) :play-area))
                                (when (not-empty (take 1 (get-in @state [opp-side :discard])))
                                  (move state opp-side (last (get-in @state [opp-side :discard])) :play-area))
                                ))}
                {:label "Shuffle Deck"
                 :effect (req (let [opp-side (if (= side :contestant)
                                               :challenger
                                               :contestant)]
                                (doseq [c (get-in @state [opp-side :play-area])]
                                  (move state opp-side c :deck))
                                (shuffle! state opp-side :deck)
                                (system-msg state side "uses mewh_thegreathunt.jpg to return cards and shuffle")
                                ))}
                {:label "Put to Discard"
                 :effect (req (let [opp-side (if (= side :contestant)
                                               :challenger
                                               :contestant)]
                                (if (< 0 (count (get-in @state [opp-side :play-area])))
                                  (loop [k (count (get-in @state [opp-side :play-area]))]
                                    (when (> k 0)
                                      (move state opp-side (last (get-in @state [opp-side :play-area])) :discard)
                                      (recur (- k 1)))))
                                (system-msg state side "uses mewh_thegreathunt.jpg to return cards")
                                ))}
                ]}
   "The Grey Hat"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Gandalf"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "The Kings Hunting"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Thranduil"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "The Lidless Eye"
   {:abilities [{:label "Random 5"
                 :effect (req
                           (let [opp-side (if (= side :contestant)
                                            :challenger
                                            :contestant)
                                 kount (count (get-in @state [opp-side :hand]))]
                             (loop [k (if (< kount 5) kount 5)]
                               (when (> k 0)
                                 (move state opp-side (rand-nth (get-in @state [opp-side :hand])) :play-area)
                                 (recur (- k 1))))
                             (resolve-ability state side
                                              {:delayed-completion true
                                               :player  side
                                               :prompt  "Click done when ready"
                                               :choices ["Done"]
                                               :effect  (req (case target
                                                               "Done"
                                                               (loop [k (count (get-in @state [opp-side :play-area]))]
                                                                 (when (> k 0)
                                                                   (move state opp-side (first (get-in @state [opp-side :play-area])) :hand)
                                                                   (recur (- k 1)))))
                                                             (effect-completed state side nil)
                                                             (system-msg state side " discards a card to look at up to 5 random cards from your hand")
                                                             )} nil nil)))}]}
   "The Morgul-lord"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on your Ringwraith"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "The Seas Prowess"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Círdan"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "The Three Are Together"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on one of the three"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "The Three Hunters"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on one of the three"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "The White Wizard"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on your Wizard"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "The Windlord Found Me"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a character"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Thrall of the Voice"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on another character")
                                                            (msg "Place this card on a character"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (= (last (:zone %)) :characters)
                                                                         (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Three Golden Hairs"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a character"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "To Fealty Sworn"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a hobbit"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "To Satisfy the Questioner"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on character"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Towers Destroyed"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (msg "Which character represents the company")
                                                  :choices {:req #(and (character? %)
                                                                       (can-host? %))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "True-hearted Man"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Boromir II"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Trusted Counsellor"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a character"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Valleys Have Ears"
   {:abilities [{:label "Yours"
                 :effect (req (resolve-ability state side
                                               {:delayed-completion true
                                                :player  side
                                                :prompt  "How Many Factions or Cancel"
                                                :choices ["1 Faction" "2 Factions" "3 Factions" "4 Factions"
                                                          "5 Factions" "6 Factions" "7 Factions" "Cancel"]
                                                :effect  (req
                                                           (let [factions (case target
                                                                            "Cancel"
                                                                            0
                                                                            "1 Faction"
                                                                            2
                                                                            "2 Factions"
                                                                            4
                                                                            "3 Factions"
                                                                            6
                                                                            "4 Factions"
                                                                            8
                                                                            "5 Factions"
                                                                            10
                                                                            "6 Factions"
                                                                            12
                                                                            "7 Factions"
                                                                            14)
                                                                 kount (count (get-in @state [side :deck]))]
                                                             (loop [k (if (< kount factions) kount factions)]
                                                               (when (> k 0)
                                                                 (move state side (first (get-in @state [side :deck])) :current)
                                                                 (recur (- k 1))))
                                                             (effect-completed state side nil)))} nil nil))
                 :msg (msg " to look at the top of his deck")}
                {:label "Theirs"
                 :effect (req (resolve-ability state side
                                               {:delayed-completion true
                                                :player  side
                                                :prompt  "How Many Factions or Cancel"
                                                :choices ["1 Faction" "2 Factions" "3 Factions" "4 Factions"
                                                          "5 Factions" "6 Factions" "7 Factions" "Cancel"]
                                                :effect  (req
                                                           (let [opp-side (if (= side :contestant)
                                                                            :challenger
                                                                            :contestant)
                                                                 factions (case target
                                                                            "Cancel"
                                                                            0
                                                                            "1 Faction"
                                                                            2
                                                                            "2 Factions"
                                                                            4
                                                                            "3 Factions"
                                                                            6
                                                                            "4 Factions"
                                                                            8
                                                                            "5 Factions"
                                                                            10
                                                                            "6 Factions"
                                                                            12
                                                                            "7 Factions"
                                                                            14)
                                                                 kount (count (get-in @state [opp-side :deck]))]
                                                             (loop [k (if (< kount factions) kount factions)]
                                                               (when (> k 0)
                                                                 (move state side (assoc (first (get-in @state [opp-side :deck])) :swap true) :current)
                                                                 (recur (- k 1))))
                                                             (effect-completed state side nil)))} nil nil))
                 :msg (msg " to look at the top of your deck")}
                {:label "Shuffle"
                 :effect (req (resolve-ability state side
                                               {:effect (req
                                                          (let [opp-side (if (= side :contestant)
                                                                           :challenger
                                                                           :contestant)]
                                                            (loop [k (count (get-in @state [side :current]))]
                                                                   (when (> k 0)
                                                                     (move state opp-side (dissoc (rand-nth (get-in @state [side :current])) :swap) :deck {:front true})
                                                                     (recur (- k 1))))))} nil nil))
                 :msg (msg " shuffle back cards to the top cards of your deck")}]}
   "Value Food and Cheer"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a character"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Variag-king of Khand"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on your Ringwraith"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Vein of Arda"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on the sage"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Very Active Governess"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Galadriel"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Vilya"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Elrond"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "War Preparations"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on the scout"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "When I Know Anything"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on the sage"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "When You Know More"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on the sage"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "While the Yellow Face Sleeps"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on your Ringwraith"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Winged Change-master"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on Radagast"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   "Whispers of Rings"
   {:abilities [{:label "With"
                 :effect (req (let [r (get-card state card)
                                    hosted (count (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if (= hosted 2)
                                                            (msg "There are two rings with Whispers")
                                                            (msg "Pick a ring to place off to the side"))
                                                  :choices {:req (fn [t] (card-is? t :side side))}
                                                  :msg (msg "place " (card-str state target) " off to the side")
                                                  :effect (effect (host card target))} card nil)))}]}
   "Wizards Myrmidon"
   {:abilities [{:label "Place"
                 :effect (req (let [r (get-card state card)
                                    hosted? (character? (:host r))]
                                (resolve-ability state side
                                                 {:prompt (if hosted?
                                                            (msg "You may not play this on a different character")
                                                            (msg "Place this card on a character"))
                                                  :choices {:req #(if (not hosted?)
                                                                    (and (character? %)
                                                                         (can-host? %)))}
                                                  :msg (msg "host it on " (card-str state target))
                                                  :effect (effect (host target card))} card nil)))}]}
   })
