(ns meccg.cardbrowser
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [sablono.core :as sab :include-macros true]
            [cljs.core.async :refer [chan put! >! sub pub] :as async]
            [clojure.string :as str]
            [meccg.appstate :refer [app-state]]
            [meccg.ajax :refer [GET]]
            [meccg.utils :refer [toastr-options banned-span restricted-span rotated-span influence-dots]]
            [meccg.artists :refer [artists]]
            [cardnum.cards :refer [all-cards] :as cards]
            [cardnum.decks :as decks]))

(def cards-channel (chan))
(def pub-chan (chan))
(def notif-chan (pub pub-chan :topic))

(go (let [local-cards (js->clj (.parse js/JSON (.getItem js/localStorage "cards")) :keywordize-keys true)
          need-update? (not local-cards)
          cards (sort-by :code (:json (<! (GET "/data/cards"))))
          sets (:json (<! (GET "/data/sets")))
          mwl (:json (<! (GET "/data/mwl")))]
      (reset! cards/mwl mwl)
      (reset! cards/sets sets)
      (swap! app-state assoc :sets sets)
      (reset! all-cards cards)
      (swap! app-state assoc :cards-loaded true)
      (put! cards-channel cards)))

(defn make-span [text symbol class]
  (.replace text (apply str symbol) (str "<img src='" class "'style=\"width:16px;height:16px;\"></img>")))

(defn image-dc
  ([card] (image-dc card true))
  ([card allow-all-users]
   (str "/img/cards/" (:set_code card) "/dce-" (:ImageName card))))

(defn image-ice
  ([card] (image-ice card true))
  ([card allow-all-users]
   (str "/img/cards/" (:set_code card) "/ice-" (:ImageName card))))

(defn image-url
  ([card] (image-url card false))
  ([card allow-all-users]
   (if (:dreamcard card)
     (str "/img/cards/" (:set_code card) "/" (:ImageName card))
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
         (str "/img/cards/" (:set_code card) "/" (:ImageName card)))
       ))))

(defn add-symbols [card-text]
  (-> (if (nil? card-text) "" card-text)
      (make-span "Automatic-attacks" "img/dc/me_aa.png")
      (make-span "Automatic-attack" "img/dc/me_aa.png")
      (make-span "automatic-attacks" "img/dc/me_aa.png")
      (make-span "automatic-attack" "img/dc/me_aa.png")
      (make-span "Border-holds [B]" "img/dc/me_bh.png")
      (make-span "Border-lands [b]" "img/dc/me_bl.png")
      (make-span "Border-hold [B]" "img/dc/me_bh.png")
      (make-span "Border-land [b]" "img/dc/me_bl.png")
      (make-span "company vs. company combat" "img/dc/me_ccc.png")
      (make-span "CvCC" "img/dc/me_ccc.png")
      (make-span "corruption checks" "img/dc/me_cp.png")
      (make-span "corruption check" "img/dc/me_cp.png")
      (make-span "CC" "img/dc/me_cp.png")
      (make-span "corruption point (CP)" "img/dc/me_cp.png")
      (make-span "CP" "img/dc/me_cp.png")
      (make-span "corruption points" "img/dc/me_cp.png")
      (make-span "corruption point" "img/dc/me_cp.png")
      (make-span "Coastal Seas [ccc]" "img/dc/me_tc.png")
      (make-span "Coastal Seas [cc]" "img/dc/me_dc.png")
      (make-span "Coastal Seas [c]" "img/dc/me_cs.png")
      (make-span "Coastal Sea [c]" "img/dc/me_cs.png")
      (make-span "Dark-domains [d]" "img/dc/me_dd.png")
      (make-span "Dark-holds [D]" "img/dc/me_dh.png")
      (make-span "Dark-domain [d]" "img/dc/me_dd.png")
      (make-span "Dark-hold [D]" "img/dc/me_dh.png")
      (make-span "Darkhavens [V]" "img/dc/me_dha.png")
      (make-span "Darkhaven [V]" "img/dc/me_dha.png")
      (make-span "Darkhavens [K]" "img/dc/me_dha.png")
      (make-span "Darkhaven [K]" "img/dc/me_dha.png")
      (make-span "Darkhaven" "img/dc/me_dha.png")
      (make-span "Darkhaven" "img/dc/me_dha.png")
      (make-span "Deserts [ee]" "img/dc/me_ee.png")
      (make-span "Deserts" "img/dc/me_ee.png")
      (make-span "Desert [e]" "img/dc/me_er.png")
      (make-span "Desert" "img/dc/me_er.png")
      (make-span "Direct influence" "img/dc/me_di.png")
      (make-span "direct influence" "img/dc/me_di.png")
      (make-span "DI" "img/dc/me_di.png")
      (make-span "Free-domains [f]" "img/dc/me_fd.png")
      (make-span "Free-holds [F]" "img/dc/me_fh.png")
      (make-span "Free-domain [f]" "img/dc/me_fd.png")
      (make-span "Free-hold [F]" "img/dc/me_fh.png")
      (make-span "General influence" "img/dc/me_gi.png")
      (make-span "general influence" "img/dc/me_gi.png")
      (make-span "GI" "img/dc/me_gi.png")
      (make-span "Havens [H]" "img/dc/me_ha.png")
      (make-span "Haven [H]" "img/dc/me_ha.png")
      (make-span "Jungles [j]" "img/dc/me_ju.png")
      (make-span "Jungle [j]" "img/dc/me_ju.png")
      (make-span " MP." "img/dc/me_mp.png")
      (make-span " MP;" "img/dc/me_mp.png")
      (make-span " MP " "img/dc/me_mp.png")
      (make-span " mp." "img/dc/me_mp.png")
      (make-span " mp;" "img/dc/me_mp.png")
      (make-span " mp " "img/dc/me_mp.png")
      (make-span "marshalling points" "img/dc/me_mp.png")
      (make-span "marshalling point" "img/dc/me_mp.png")
      (make-span "Ruins & Lairs [R]" "img/dc/me_rl.png")
      (make-span "Shadow-holds [S]" "img/dc/me_sh.png")
      (make-span "Shadow-lands [s]" "img/dc/me_sl.png")
      (make-span "Shadow-hold [S]" "img/dc/me_sh.png")
      (make-span "Shadow-land [s]" "img/dc/me_sl.png")
      (make-span "SPs" "img/dc/me_sp.png")
      (make-span "SP" "img/dc/me_sp.png")
      (make-span "stage points" "img/dc/me_sp.png")
      (make-span "stage point" "img/dc/me_sp.png")
      (make-span "tap:" "img/dc/me_tap.png")
      (make-span "Tap " "img/dc/me_tap.png")
      (make-span " tapping" "img/dc/me_tap.png")
      (make-span " tap " "img/dc/me_tap.png")
      (make-span " tap." "img/dc/me_tap.png")
      (make-span "Wildernesses [w]" "img/dc/me_wi.png")
      (make-span "Wilderness [w]" "img/dc/me_wi.png")
      (make-span "Wildernesses [ww]" "img/dc/me_dw.png")
      (make-span "Wilderness [ww]" "img/dc/me_dw.png")
      (make-span "Wildernesses [www]" "img/dc/me_tw.png")
      (make-span "Wilderness [www]" "img/dc/me_tw.png")
      (make-span "[ccc]" "img/dc/me_tc.png")
      (make-span "[cc]" "img/dc/me_dc.png")
      (make-span "[ee]" "img/dc/me_ee.png")
      (make-span "[www]" "img/dc/me_tw.png")
      (make-span "[ww]" "img/dc/me_dw.png")
      (make-span "[b]" "img/dc/me_bl.png")
      (make-span "[c]" "img/dc/me_cs.png")
      (make-span "[d]" "img/dc/me_dd.png")
      (make-span "[e]" "img/dc/me_er.png")
      (make-span "[f]" "img/dc/me_fd.png")
      (make-span "[j]" "img/dc/me_ju.png")
      (make-span "[s]" "img/dc/me_sl.png")
      (make-span "[w]" "img/dc/me_wi.png")
      (make-span "[B]" "img/dc/me_bh.png")
      (make-span "[D]" "img/dc/me_dh.png")
      (make-span "[V]" "img/dc/me_dha.png")
      (make-span "[K]" "img/dc/me_dha.png")
      (make-span "[F]" "img/dc/me_fh.png")
      (make-span "[H]" "img/dc/me_ha.png")
      (make-span "[R]" "img/dc/me_rl.png")
      (make-span "[S]" "img/dc/me_sh.png")))

(defn non-game-toast
  "Display a toast warning with the specified message."
  [msg type options]
  (set! (.-options js/toastr) (toastr-options options))
  (let [f (aget js/toastr type)]
    (f msg)))

(defn- card-text
  "Generate text html representation a card"
  [card cursor]
  [:div
   [:h4 (:full_set card) ": " (:title card)]
   [:div.text
    (if (= false (:dreamcard card))
      [:p [:span.type "Rarity: "] (str (:Rarity card)) [:span.type ", Precise: "] (str (:Precise card))])
    [:p [:span.type (str (:type card))] (if (= (.toLowerCase (:type card)) (:Secondary card))
                                          ""
                                          (str ": " (:Secondary card)))]
    [:pre {:dangerouslySetInnerHTML #js {:__html
                                         (loop [new-text (:text card)]
                                           (if (= new-text (add-symbols new-text))
                                             new-text
                                             (recur (add-symbols new-text))))}}]]])

(defn card-view [card owner]
  (reify
    om/IInitState
    (init-state [_] {:showText false})
    om/IRenderState
    (render-state [_ state]
      (let [cursor (om/get-state owner :cursor)]
        (sab/html
          [:div.card-preview.blue-shade
           (when (om/get-state owner :decorate-card)
             {:class (cond (:selected card) "selected")})
           (if (:showText state)
             (card-text card cursor)
             (when-let [url (if (and (:erratum card)
                                     (= "always" (get-in @app-state [:options :dc-erratum])))
                              (image-dc card true)
                              (if (and (:errata card)
                                       (= "always" (get-in @app-state [:options :ice-errata])))
                                (image-ice card true)
                                (image-url card true)))]
               [:img {:src url
                      ;:decoding "async"
                      :alt (:title card)
                      :onClick #(do (.preventDefault %)
                                    (put! (:pub-chan (om/get-shared owner))
                                          {:topic :card-selected :data card})
                                    nil)
                      :onError #(-> (om/set-state! owner {:showText true}))
                      :onLoad #(-> % .-target js/$ .show)}]))])))))

(defn card-ndce [card owner]
  (reify
    om/IInitState
    (init-state [_] {:showText false})
    om/IRenderState
    (render-state [_ state]
      (let [cursor (om/get-state owner :cursor)]
        (sab/html
          [:div.card-preview.blue-shade
           (when (om/get-state owner :decorate-card)
             {:class (cond (:selected card) "selected")})
           (if (:showText state)
             (card-text card cursor)
             (when-let [url (image-url card true)]
               [:img {:src url
                      :alt (:title card)
                      :onClick #(do (.preventDefault %)
                                    (put! (:pub-chan (om/get-shared owner))
                                          {:topic :card-selected :data card})
                                    nil)
                      :onError #(-> (om/set-state! owner {:showText true}))
                      :onLoad #(-> % .-target js/$ .show)}]))])))))


(defn card-dce [card owner]
  (reify
    om/IInitState
    (init-state [_] {:showText false})
    om/IRenderState
    (render-state [_ state]
      (let [cursor (om/get-state owner :cursor)]
        (sab/html
          [:div.card-preview.blue-shade
           (when (om/get-state owner :decorate-card)
             {:class (cond (:selected card) "selected")})
           (if (:showText state)
             (card-text card cursor)
             (when-let [url (if (and (:erratum card)
                                     (= "always" (get-in @app-state [:options :dc-erratum])))
                              (image-dc card true)
                              (if (and (:errata card)
                                       (= "always" (get-in @app-state [:options :ice-errata])))
                                (image-ice card true)
                                (image-url card true)))]
               [:img {:src url
                      :alt (:title card)
                      :onClick #(do (.preventDefault %)
                                    (put! (:pub-chan (om/get-shared owner))
                                          {:topic :card-selected :data card})
                                    nil)
                      :onError #(-> (om/set-state! owner {:showText true}))
                      :onLoad #(-> % .-target js/$ .show)}]))])))))

(defn card-info-view [cursor owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (sab/html
        (let [selected-card (om/get-state owner :selected-card)]
          (if (nil? selected-card)
            [:div {:display "none"}]
            [:div
             [:h4 "Card text"]
             [:div.blue-shade.panel
              (card-text selected-card cursor)]]))))))

(def primary-order ["Character" "Resource" "Hazard" "Site" "Region"])
(def resource-secondaries ["Ally" "Avatar" "Faction" "Greater Item" "Major Item" "Minor Item" "Gold Ring Item" "Special Item"])
(def site-secondaries ["Under-deeps" "Greater Item" "Major Item" "Minor Item" "Gold Ring Item" "Information" "Hoard" "Scroll" "Palantír" "Battle-gear" "Non-battle-gear"])
(def shared-secondaries ["Permanent-event" "Short-event" "Long-event" "Permanent-event/Short-event" "Permanent-event/Long-event" "Short-event/Long-event"])
(def hazard-secondaries ["Creature" "Creature/Permanent-event" "Creature/Short-event" "Creature/Long-event"])
(def general-alignments ["Hero" "Minion" "Dual" "Balrog" "Fallen-wizard" "Fallen/Lord" "Lord"
                         "Elf-lord" "Dwarf-lord" "Atani-lord" "War-lord" "Dragon-lord" "Grey"])
(def standard-havens ["Carn Dûm" "Dol Guldur" "Edhellond" "Geann a-Lisch"
                      "Grey Havens" "Lórien" "Minas Morgul" "Rivendell"])
(def standard-hero-havens ["Edhellond" "Grey Havens" "Lórien" "Rivendell"])
(def standard-minion-havens ["Carn Dûm" "Dol Guldur" "Geann a-Lisch" "Minas Morgul"])
(def dreamcard-havens ["Amaru" "Bozisha-Dar" "Carn Dûm" "Chey Goumal" "Dol Guldur"
                       "Edhellond" "Elanthia" "Evermist" "Geann a-Lisch" "Grey Havens"
                       "Hau Nysrin" "Inyalonî" "Lórien" "Minas Morgul" "Mornost" "Rhûbar"
                       "Rivendell" "Shapôl Udûn" "Taurondë" "Tower of Hargrog" "Valagalen"])
(def dreamcard-hero-havens ["Edhellond" "Elanthia" "Evermist" "Grey Havens" "Hau Nysrin"
                           "Inyalonî" "Lórien" "Rhûbar" "Rivendell" "Taurondë" "Valagalen"])
(def dreamcard-minion-havens ["Amaru" "Bozisha-Dar" "Carn Dûm" "Chey Goumal" "Dol Guldur"
                              "Geann a-Lisch" "Minas Morgul" "Mornost" "Shapôl Udûn" "Tower of Hargrog"])
(def set-order ["The Wizards" "The Wizards (Unlimited)" "The Dragons" "Dark Minions" "The Lidless Eye" "Against the Shadow" "The White Hand"
                "The Balrog" "Firstborn" "Durin's Folk" "The Necromancer" "The Northern Waste" "Morgoth's Legacy"
                "Return of the Shadow" "Treason of Isengard" "War of the Ring" "The Great Wyrms" "Bay of Ormal"
                "Court of Ardor" "The Great Central Plains" "The Dominion of the Seven" "Kingdom of the North"
                "Nine Rings for Mortal Men" "Red Nightfall" "The Sunlands" "Bay of Utum"])
(def all-skill ["Air" "Balrog" "Black" "Calvary" "Cold-dragon" "Court"
                "Diplomat" "Dragon" "Dragon-magic" "Dúnadan" "Dwarf" "Elf"
                "Fána" "Fire-dragon" "Flying" "Grey" "Heavy" "Heavy Air"
                "Heavy Calvary" "Heavy Infantry" "Hero" "Hobbit" "Infantry"
                "Leader" "Light" "Light Air" "Light Calvary" "Light Infantry"
                "Man" "Minion" "Nazgûl" "One-handed" "Orc" "Ranger" "Ringwraith"
                "Sage" "Sauron" "Scout" "Shadow-magic" "Sorcery" "Spell"
                "Spirit-magic" "Troll" "Two-handed" "Umit" "War" "Warlord"
                "Warrior" "White" "Winged" "Wizard" "Wose" "Wyrm"])
(def all-types ["Animal" "Ape" "Armor" "Awakened Plant" "Axe" "Balrog" "Bear"
                "Boar" "Bow" "Cold-dragon" "Command" "Corruption" "Cult"
                "Dark Enchantment" "Demon" "Disease" "Dragon" "Dragon-lord"
                "Drake" "Drêl" "Dúnadan" "Dúnadan-lord" "Dwarf" "Dwarf-lord"
                "Dwarven Ring" "Eagle" "Elf" "Elf-lord" "Elven Ring"
                "Enchantment" "Ent" "Environment" "Fána" "Female" "Fire-dragon"
                "Firebeard" "Flattery" "Food" "Giant" "Gold Ring" "Half"
                "Half-elf" "Half-orc" "Half-troll" "Hathorian" "Helmet"
                "Hill-troll" "Hoard" "Hobbit" "Horse" "Ice-dragon" "Ice-orc"
                "Information" "Instrument" "Ironfist" "Jewel" "Kelno" "Kirani"
                "Knowledge" "Lesser Ring" "Light Enchantment" "Longbeard"
                "Lord" "Lore" "Lost Knowledge" "Mace" "Magic" "Magic Ring"
                "Maia" "Man" "Man Ring" "Man-lord" "Marsh-drake" "Mercenary"
                "Mind Ring" "Mode" "Nando" "Nazgûl" "Nelya" "Noldo" "Offering"
                "Olog-hai" "One Ring" "Orc" "Ore" "Palantír" "Prisoner"
                "Pûkel-creature" "Quest" "Quest-Side-A" "Quest-Side-B" "Riddling"
                "Ring" "Ringwraith" "Ritual" "Rune" "Sand-drake" "Scara-hai"
                "Sea Serpent" "Shield" "Silvan" "Sinda" "Slave" "Slayer"
                "Song" "Spawn" "Special" "Spider" "Spirit" "Spirit-namer"
                "Spirit Ring" "Staff" "Stature" "Stiffbeard" "Stolen Knowledge"
                "Tatya" "Technology" "Transport" "Trap" "Treasure" "Troll" "Trophy"
                "Umit" "Undead" "Uruk-hai" "Vermin" "Warlord" "Weapon" "Werewolf"
                "Whale" "Whip" "Wild-troll" "Wizard" "Wizardry" "Wolf" "Wose"])
(def char-races ["Drêl" "Dúnadan" "Dwarf" "Firebeard" "Ironfist" "Longbeard"
                 "Stiffbeard" "Elf" "Half-elf" "Kelno" "Nando" "Nelya" "Noldo"
                 "Silvan" "Sinda" "Spirit-namer" "Tatya" "Hathorian" "Hobbit"
                 "Man" "Orc" "Half-orc" "Ice-orc" "Scara-hai" "Uruk-hai" "Troll"
                 "Half-troll" "Hill-troll" "Olog-hai" "Wild-troll" "Slayer"
                 "Kirani" "Umit" "Wose" "Dragon-lord" "Dúnadan-lord"
                 "Dwarf-lord" "Elf-lord" "Man-lord" "Lord" "Ringwraith"
                 "Warlord" "Wizard" "Balrog" "Bear" "Demon" "Dragon"
                 "Werewolf" "Half"])
(def char-skill ["Diplomat" "Ranger" "Sage" "Scout" "Warrior" "Winged" "Wyrm"
                 "Dragon-magic" "Shadow-magic" "Sorcery" "Spell" "Spirit-magic"])
(def hazard-races ["Animal" "Ape" "Awakened Plant" "Balrog" "Bear" "Boar"
                   "Cold-dragon" "Corruption" "Dark Enchantment" "Demon"
                   "Disease" "Dragon" "Drake" "Dúnadan" "Dwarf" "Eagle"
                   "Elf" "Enchantment" "Environment" "Female" "Fire-dragon"
                   "Giant" "Hobbit" "Ice-dragon" "Light Enchantment" "Maia"
                   "Man" "Marsh-drake" "Nazgûl" "Orc" "Prisoner" "Pûkel-creature"
                   "Sand-drake" "Sea Serpent" "Slayer" "Spawn" "Spider" "Spirit"
                   "Trap" "Treasure" "Troll" "Trophy" "Undead" "Vermin" "Whale"
                   "Wolf" "Wose"])
(def hazard-types ["Flying" "Nazgûl" "Winged" "Wyrm"])
(def resource-skill ["Air" "Balrog" "Black" "Calvary" "Cold-dragon" "Court"
                     "Diplomat" "Dragon" "Dragon-magic" "Dúnadan" "Dwarf" "Elf"
                     "Fána" "Fire-dragon" "Flying" "Grey" "Heavy" "Heavy Air"
                     "Heavy Calvary" "Heavy Infantry" "Hero" "Hobbit" "Infantry"
                     "Leader" "Light" "Light Air" "Light Calvary" "Light Infantry"
                     "Lost Knowledge" "Man" "Minion" "One-handed" "Orc" "Ranger"
                     "Ringwraith" "Sage" "Sauron" "Scout" "Shadow-magic" "Sorcery"
                     "Spell" "Spirit-magic" "Stolen Knowledge" "Troll" "Two-handed"
                     "Umit" "War" "Warlord" "Warrior" "White" "Winged" "Wizard"
                     "Wose" "Wyrm"])
(def resource-types ["Animal" "Armor" "Axe" "Balrog" "Bow" "Command" "Cult" "Demon"
                     "Dragon" "Drake" "Dúnadan" "Dwarf" "Dwarven Ring" "Eagle"
                     "Elf" "Elven Ring" "Ent" "Environment" "Fána" "Flattery"
                     "Food" "Giant" "Gold Ring" "Helmet" "Hoard" "Hobbit" "Horse"
                     "Information" "Instrument" "Jewel" "Knowledge" "Lesser Ring"
                     "Light Enchantment" "Lore" "Lost Knowledge" "Mace" "Magic"
                     "Magic Ring" "Maia" "Man" "Man Ring" "Mercenary" "Mind Ring"
                     "Mode" "Offering" "One Ring" "Orc" "Ore" "Palantír" "Prisoner"
                     "Quest" "Quest-Side-A" "Quest-Side-B" "Riddling" "Ring" "Ritual"
                     "Rune" "Shield" "Slave" "Slayer" "Song" "Spawn" "Special"
                     "Spider" "Spirit Ring" "Staff" "Stature" "Stolen Knowledge"
                     "Technology" "Transport" "Treasure" "Troll" "Trophy" "Umit"
                     "Undead" "Vermin" "Weapon" "Whip" "Wizardry" "Wolf" "Wose"])
(def ally-races ["Animal" "Balrog" "Demon" "Eagle" "Elf" "Ent" "Giant" "Hobbit"
                 "Horse" "Maia" "Orc" "Spawn" "Spider" "Troll" "Undead" "Wolf"])
(def ally-skill ["Diplomat" "Ranger" "Sage" "Scout" "Warrior"
                 "Shadow-magic" "Sorcery" "Spirit-magic" "Flying" "Winged"])
(def fact-races ["Animal" "Demon" "Dragon" "Drake" "Dúnadan" "Dwarf" "Eagle"
                 "Elf" "Ent" "Giant" "Hobbit" "Man" "Mercenary" "Orc" "Slave"
                 "Slayer" "Special" "Spider" "Troll" "Umit" "Undead" "Vermin"
                 "Wolf" "Wose"])
(def fact-skill ["Flying" "Air" "Calvary" "Infantry"
                 "Heavy" "Heavy Air" "Heavy Calvary" "Heavy Infantry"
                 "Light" "Light Air" "Light Calvary" "Light Infantry"])
(def item-types ["Armor" "Axe" "Bow" "Dwarven Ring" "Elven Ring" "Food"
                 "Gold Ring" "Helmet" "Hoard" "Instrument" "Jewel" "Knowledge"
                 "Lesser Ring" "Lost Knowledge" "Mace" "Magic Ring" "Man Ring"
                 "Mind Ring" "One Ring" "Ore" "Palantír" "Ring" "Rune" "Shield"
                 "Spirit Ring" "Staff" "Stolen Knowledge" "Technology"
                 "Treasure" "Weapon" "Whip"])
(def item-skill ["Balrog" "Court" "Diplomat" "Dragon" "Dúnadan" "Dwarf" "Elf"
                 "Hobbit" "Leader" "Lost Knowledge"  "Man" "Minion" "One-handed"
                 "Orc" "Ranger" "Ringwraith" "Sage" "Scout" "Spirit-namer"
                 "Stolen Knowledge" "Troll" "Two-handed" "Umit" "Warrior"
                 "White" "Wizard" "Wose"])
(def rarity-choice ["Rare" "Uncommon" "Common" "Fixed" "Promo"])
(def precise-types ["C" "C1" "C2" "C3" "C4" "C5" "CA1" "CA2"
                    "CB" "CB, CS1" "CB, CS2" "CB1" "CB2"
                    "F1" "F1, U" "F2" "F3" "F4" "F5" "F5, CB"
                    "P" "R" "R1" "R2" "R3"
                    "U" "U1" "U2" "U3" "U4"])

(defn secondaries [primary]
  (case primary
    "All" (concat hazard-secondaries shared-secondaries resource-secondaries ["site"] ["region"])
    "Character" ["character" "Avatar" "Leader" "Agent"]
    "Resource" (concat resource-secondaries shared-secondaries)
    "Hazard" (concat hazard-secondaries shared-secondaries)
    "Site" site-secondaries
    "Region" ["region"]))

(defn alignments [primary]
  (case primary
    "All" (concat general-alignments ["Neutral"])
    "Character" (concat general-alignments ["Neutral"])
    "Resource" general-alignments
    "Hazard" ["Neutral"]
    "Site" general-alignments
    "Region" ["Neutral"]))

(defn options [list]
  (let [options (cons "All" list)]
    (for [option options]
      [:option {:value option :dangerouslySetInnerHTML #js {:__html option}}])))

(defn filter-cards [filter-value field cards]
  (if (= filter-value "All")
    cards
    (filter #(= (field %) filter-value) cards)))

(defn filter-parts [filter-value field cards]
  (if (= filter-value "All")
    cards
    (filter #(boolean (re-find (re-pattern filter-value) (field %))) cards)))

(defn filter-greater [filter-value field cards]
  (if (= filter-value "All")
    cards
    (filter #(> (field %) filter-value) cards)))

(defn filter-sites [strict cards]
  (case strict
    "All"
    cards
    "Under-deeps"
    (filter-parts "Under-deeps" :RPath cards)
    "Greater Item"
    (filter-cards true :GreaterItem cards)
    "Major Item"
    (filter-cards true :MajorItem cards)
    "Minor Item"
    (filter-cards true :MinorItem cards)
    "Gold Ring Item"
    (filter-cards true :GoldRing cards)
    "Information"
    (filter-cards true :Information cards)
    "Hoard"
    (filter-cards true :Hoard cards)
    "Scroll"
    (filter-cards true :Scroll cards)
    "Palantír"
    (filter-cards true :Palantiri cards)
    "Battle-gear"
    (filter-cards true :Gear cards)
    "Non-battle-gear"
    (filter-cards true :Non cards)
    ))

(defn filter-race [race-filter filter-value cards]
  (if race-filter
    cards
    (filter-parts filter-value :Race cards)
    ))

(defn filter-skill [skill-filter filter-value cards]
  (if skill-filter
    cards
    (filter-parts filter-value :subtype cards)
    ))

(defn filter-haven [haven-filter filter-value cards]
  (if haven-filter
    cards
    (filter-cards filter-value :Haven cards)
    ))

(defn filter-second [site-filter filter-value cards]
  (if site-filter
    (filter-sites filter-value cards)
    (filter-cards filter-value :Secondary cards)
    ))

(defn filter-dreamcards [should-filter cards]
  (if should-filter
    (filter-cards false :dreamcard cards)
    cards))

(defn filter-released [should-filter cards]
  (if should-filter
    (filter-cards true :released cards)
    cards))

(defn filter-extras [should-filter cards]
  (if should-filter
    (filter-cards false :extras cards)
    cards))

(defn filter-stage [should-filter cards]
  (if should-filter
    (filter-greater 0 :Stage cards)
    cards))

(defn filter-unique [should-filter cards]
  (if should-filter
    (filter-cards true :uniqueness cards)
    cards))

(defn filter-title [query cards]
  (if (empty? query)
    cards
    (let [lcquery (.toLowerCase query)]
      (filter #(or (not= (.indexOf (.toLowerCase (:flip-title %)) lcquery) -1)
                   (not= (.indexOf (:normalizedtitle %) lcquery) -1))
              cards))))

(defn filter-texts [query cards]
  (if (empty? query)
    cards
    (let [lcquery (.toLowerCase query)]
      (filter #(not= (.indexOf (.toLowerCase (:text %)) lcquery) -1)
              cards))))

(defn sort-field [fieldname]
  (case fieldname
    "Set" #((into {} (map-indexed (fn [i e] [e i]) set-order)) (:full_set %))
    "Name" (juxt :flip-title #((into {} (map-indexed (fn [i e] [e i]) set-order)) (:full_set %)))
    "Type" (juxt #((into {} (map-indexed (fn [i e] [e i]) set-order)) (:full_set %))
                    #((into {} (map-indexed (fn [i e] [e i]) primary-order)) (:type %)))
    "Align" (juxt #((into {} (map-indexed (fn [i e] [e i]) set-order)) (:full_set %))
                      #((into {} (map-indexed (fn [i e] [e i]) (concat general-alignments ["Neutral"]))) (:alignment %)))))

(defn selected-set-name [state]
  (-> (:set-filter state)
      (.replace "&nbsp;&nbsp;&nbsp;&nbsp;" "")
      (.replace " Set" "")))

(defn selected-set-dreamcards? [{:keys [sets]} state]
  (let [s (selected-set-name state)
        combined sets]
    (if (= s "All")
      false
      (->> combined
           (filter #(= s (:name %)))
           (first)
           (:dreamcards)))))

(defn selected-set-released? [{:keys [sets]} state]
  (let [s (selected-set-name state)
        combined sets]
    (if (= s "All")
      false
      (->> combined
           (filter #(= s (:name %)))
           (first)
           (:released)))))

(defn selected-set-extras? [{:keys [sets]} state]
  (let [s (selected-set-name state)
        combined sets]
    (if (= s "All")
      false
      (->> combined
           (filter #(= s (:name %)))
           (first)
           (:extras)))))

(defn handle-scroll [e owner {:keys [page]}]
  (let [$cardlist (js/$ ".card-list")
        height (- (.prop $cardlist "scrollHeight") (.innerHeight $cardlist))]
    (when (> (.scrollTop $cardlist) (- height 600))
      (om/update-state! owner :page inc))))

(defn handle-title [e owner]
  (doseq [filter [:set-filter :secondary-filter :sort-filter :alignment-filter]]
    (om/set-state! owner filter "All"))
  (om/set-state! owner :title-query (.. e -target -value)))

(defn handle-texts [e owner]
  (doseq [filter [:set-filter :secondary-filter :sort-filter :alignment-filter]]
    (om/set-state! owner filter "All"))
  (om/set-state! owner :texts-query (.. e -target -value)))

(defn card-browser [{:keys [sets] :as cursor} owner]
  (reify
    om/IInitState
    (init-state [this]
      {:title-query ""
       :texts-query ""
       :sort-field "Name"
       :set-filter "All"
       :primary-filter "All"
       :alignment-filter "All"
       :secondary-filter "All"
       :precise-filter "All"
       :rarity-filter "All"
       :haven-filter "All"
       :skill-filter "All"
       :race-filter "All"
       :art-filter "All"
       :hide-dreamcards false
       :ul-extras true
       :only-released true
       :only-stage false
       :only-unique false
       :show-search true
       :page 1
       :filter-ch (chan)
       :selected-card nil})

    om/IWillMount
    (will-mount [this]
      (go (while true
            (let [f (<! (om/get-state owner :filter-ch))]
              (om/set-state! owner (:filter f) (:value f))))))

    om/IDidMount
    (did-mount [_]
      (let [events (sub (:notif-chan (om/get-shared owner)) :card-selected (chan))]
        (go
          (loop [e (<! events)]
            (om/set-state! owner :selected-card (:data e))
            (recur (<! events))))))

    om/IRenderState
    (render-state [this state]
      (.focus (js/$ ".search"))
      (sab/html
        [:div.cardbrowser
         [:div.blue-shade.panel.filters
          (let [query (:title-query state)]
            [:div.search-box
             [:span.e.search-icon {:dangerouslySetInnerHTML #js {:__html "&#xe822;"}}]
             (when-not (empty? query)
               [:span.e.search-clear {:dangerouslySetInnerHTML #js {:__html "&#xe819;"}
                                      :on-click #(om/set-state! owner :title-query "")}])
             [:input.search {:on-change #(handle-title % owner)
                             :type "text" :placeholder "Search cards" :value query}]])
          (let [query (:texts-query state)]
            [:div.search-alt-box
             [:span.e.search-alt-icon {:dangerouslySetInnerHTML #js {:__html "&#xe822;"}}]
             (when-not (empty? query)
               [:span.e.search-alt-clear {:dangerouslySetInnerHTML #js {:__html "&#xe819;"}
                                      :on-click #(om/set-state! owner :texts-query "")}])
             [:input.search-alt {:on-change #(handle-texts % owner)
                             :type "text" :placeholder "Search texts" :value query}]])
          (if (= (:show-search state) false)
          [:div
           [:h4 "By"]
           [:select {:value (:sort-filter state)
                     :on-change #(om/set-state! owner :sort-field (.trim (.. % -target -value)))}
            (for [field ["Name" "Set" "Type" "Align"]]
              [:option {:value field} field])]]
          (let [format-pack-name (fn [name] (str "&nbsp;&nbsp;&nbsp;&nbsp;" name))
                hide-dreamcards (:hide-dreamcards state)
                released-only (:only-released state)
                dc-filtered (filter-dreamcards hide-dreamcards sets)
                sets-filtered (filter-released released-only dc-filtered)
                ;; Draft is specified as a cycle, but contains no set, nor is it marked as a bigbox
                ;; so we handled it specifically here for formatting purposes
                sets-list (map #(if (not (or (:bigbox %) (= (:name %) "Draft")))
                                  (update-in % [:name] format-pack-name)
                                  %)
                               sets-filtered)
                set-names (map :name
                               (sort-by (juxt :cycle_position :position)
                                        sets-list))]
            (cond
              (= (:primary-filter state) "Site")
              (let [haven-options (if hide-dreamcards
                                    (case (:alignment-filter state)
                                      "All" standard-havens
                                      "Hero" standard-hero-havens
                                      "Minion" standard-minion-havens
                                      ("Balrog" "Fallen-wizard" "Fallen/Lord" "Lord"
                                        "Elf-lord" "Dwarf-lord" "Atani-lord"
                                        "War-lord" "Dragon-lord" "Grey" "Dual") [])
                                    (case (:alignment-filter state)
                                      "All" dreamcard-havens
                                      "Hero" dreamcard-hero-havens
                                      "Minion" dreamcard-minion-havens
                                      ("Balrog" "Fallen-wizard" "Fallen/Lord" "Lord"
                                        "Elf-lord" "Dwarf-lord" "Atani-lord"
                                        "War-lord" "Dragon-lord" "Grey" "Dual") [])
                                    )]
                (for [filter [["Set" :set-filter set-names]
                              ["Type" :primary-filter ["Character" "Resource" "Hazard" "Site" "Region"]]
                              ["Align" :alignment-filter (alignments (:primary-filter state))]
                              ["Strict" :secondary-filter (secondaries (:primary-filter state))]
                              ["Precise" :precise-filter precise-types]
                              ["Rarity" :rarity-filter rarity-choice]
                              ["Haven" :haven-filter haven-options]
                              ["Art" :art-filter artists]]]
                  [:div
                   [:h4 (first filter)]
                   [:select {:value ((second filter) state)
                             :on-change #(om/set-state! owner (second filter) (.. % -target -value))}
                    (options (last filter))]]))
              (= (:secondary-filter state) "Ally")
              (for [filter [["Set" :set-filter set-names]
                            ["Type" :primary-filter ["Character" "Resource" "Hazard" "Site" "Region"]]
                            ["Align" :alignment-filter (alignments (:primary-filter state))]
                            ["Strict" :secondary-filter (secondaries (:primary-filter state))]
                            ["Precise" :precise-filter precise-types]
                            ["Rarity" :rarity-filter rarity-choice]
                            ["Skill" :skill-filter ally-skill]
                            ["Keys" :race-filter ally-races]
                            ["Art" :art-filter artists]]]
                [:div
                 [:h4 (first filter)]
                 [:select {:value ((second filter) state)
                           :on-change #(om/set-state! owner (second filter) (.. % -target -value))}
                  (options (last filter))]])
              (= (:secondary-filter state) "Faction")
              (for [filter [["Set" :set-filter set-names]
                            ["Type" :primary-filter ["Character" "Resource" "Hazard" "Site" "Region"]]
                            ["Align" :alignment-filter (alignments (:primary-filter state))]
                            ["Strict" :secondary-filter (secondaries (:primary-filter state))]
                            ["Precise" :precise-filter precise-types]
                            ["Rarity" :rarity-filter rarity-choice]
                            ["Skill" :skill-filter fact-skill]
                            ["Keys" :race-filter fact-races]
                            ["Art" :art-filter artists]]]
                [:div
                 [:h4 (first filter)]
                 [:select {:value ((second filter) state)
                           :on-change #(om/set-state! owner (second filter) (.. % -target -value))}
                  (options (last filter))]])
              (some (partial = (:secondary-filter state)) ["Greater Item" "Major Item" "Minor Item" "Gold Ring Item" "Special Item"])
              (for [filter [["Set" :set-filter set-names]
                            ["Type" :primary-filter ["Character" "Resource" "Hazard" "Site" "Region"]]
                            ["Align" :alignment-filter (alignments (:primary-filter state))]
                            ["Strict" :secondary-filter (secondaries (:primary-filter state))]
                            ["Precise" :precise-filter precise-types]
                            ["Rarity" :rarity-filter rarity-choice]
                            ["Skill" :skill-filter item-skill]
                            ["Type" :race-filter item-types]
                            ["Art" :art-filter artists]]]
                [:div
                 [:h4 (first filter)]
                 [:select {:value ((second filter) state)
                           :on-change #(om/set-state! owner (second filter) (.. % -target -value))}
                  (options (last filter))]])
              (= (:primary-filter state) "Resource")
              (for [filter [["Set" :set-filter set-names]
                            ["Type" :primary-filter ["Character" "Resource" "Hazard" "Site" "Region"]]
                            ["Align" :alignment-filter (alignments (:primary-filter state))]
                            ["Strict" :secondary-filter (secondaries (:primary-filter state))]
                            ["Precise" :precise-filter precise-types]
                            ["Rarity" :rarity-filter rarity-choice]
                            ["Types" :skill-filter resource-skill]
                            ["Keys" :race-filter resource-types]
                            ["Art" :art-filter artists]]]
                [:div
                 [:h4 (first filter)]
                 [:select {:value ((second filter) state)
                           :on-change #(om/set-state! owner (second filter) (.. % -target -value))}
                  (options (last filter))]])
              (= (:primary-filter state) "Character")
              (for [filter [["Set" :set-filter set-names]
                            ["Type" :primary-filter ["Character" "Resource" "Hazard" "Site" "Region"]]
                            ["Align" :alignment-filter (alignments (:primary-filter state))]
                            ["Strict" :secondary-filter (secondaries (:primary-filter state))]
                            ["Precise" :precise-filter precise-types]
                            ["Rarity" :rarity-filter rarity-choice]
                            ["Skill" :skill-filter char-skill]
                            ["Keys" :race-filter char-races]
                            ["Art" :art-filter artists]]]
                [:div
                 [:h4 (first filter)]
                 [:select {:value ((second filter) state)
                           :on-change #(om/set-state! owner (second filter) (.. % -target -value))}
                  (options (last filter))]])
              (= (:primary-filter state) "Hazard")
              (for [filter [["Set" :set-filter set-names]
                            ["Type" :primary-filter ["Character" "Resource" "Hazard" "Site" "Region"]]
                            ["Align" :alignment-filter (alignments (:primary-filter state))]
                            ["Strict" :secondary-filter (secondaries (:primary-filter state))]
                            ["Precise" :precise-filter precise-types]
                            ["Rarity" :rarity-filter rarity-choice]
                            ["Types" :skill-filter hazard-types]
                            ["Keys" :race-filter hazard-races]
                            ["Art" :art-filter artists]]]
                [:div
                 [:h4 (first filter)]
                 [:select {:value ((second filter) state)
                           :on-change #(om/set-state! owner (second filter) (.. % -target -value))}
                  (options (last filter))]])
              :else
              (for [filter [["Set" :set-filter set-names]
                            ["Type" :primary-filter ["Character" "Resource" "Hazard" "Site" "Region"]]
                            ["Align" :alignment-filter (alignments (:primary-filter state))]
                            ["Strict" :secondary-filter (secondaries (:primary-filter state))]
                            ["Precise" :precise-filter precise-types]
                            ["Rarity" :rarity-filter rarity-choice]
                            ["Terms" :skill-filter all-skill]
                            ["Keys" :race-filter all-types]
                            ["Art" :art-filter artists]]]
                [:div
                 [:h4 (first filter)]
                 [:select {:value ((second filter) state)
                           :on-change #(om/set-state! owner (second filter) (.. % -target -value))}
                  (options (last filter))]])
              )))
          [:div.hide-dreamcards-div
           [:label [:input.hide-dreamcards {:type "checkbox"
                                            :value true
                                            :checked (om/get-state owner :hide-dreamcards)
                                            :on-change #(let [hide (.. % -target -checked)]
                                                          (om/set-state! owner :hide-dreamcards hide)
                                                          (when (and hide (selected-set-dreamcards? cursor state))
                                                            (om/set-state! owner :set-filter "All"))
                                                          )}]
            "Hide Dreamcards"]
           [:label "   "]
           [:label [:input.hide-dreamcards {:type "checkbox"
                                            :value true
                                            :checked (om/get-state owner :ul-extras)
                                            :on-change #(let [hide (.. % -target -checked)]
                                                          (om/set-state! owner :ul-extras hide)
                                                          (when (and hide (selected-set-extras? cursor state))
                                                            (om/set-state! owner :set-filter "All"))
                                                          )}]
            "Hide Extras"]
           [:label "   "]
           [:label [:input.only-stage {:type "checkbox"
                                       :value false
                                       :checked (om/get-state owner :show-search)
                                       :on-change #(let [hide (.. % -target -checked)]
                                                     (om/set-state! owner :show-search hide))
                                       }]
            "Show Choices"]]
          [:div.only-unique-div
           [:label [:input.only-released {:type "checkbox"
                                          :value true
                                          :checked (om/get-state owner :only-released)
                                          :on-change #(let [hide (.. % -target -checked)]
                                                        (om/set-state! owner :only-released hide)
                                                        (when (and hide (selected-set-released? cursor state))
                                                          (om/set-state! owner :set-filter "All"))
                                                        )}]
            "Only Released"]
           [:label "   "]
           [:label [:input.only-unique {:type "checkbox"
                                          :value false
                                          :checked (om/get-state owner :only-unique)
                                          :on-change #(let [hide (.. % -target -checked)]
                                                        (om/set-state! owner :only-unique hide)
                                                        (om/set-state! owner :set-filter "All"))
                                                        }]
            "Only Unique"]
           [:label "   "]
           [:label [:input.only-stage {:type "checkbox"
                                       :value false
                                       :checked (om/get-state owner :only-stage)
                                       :on-change #(let [hide (.. % -target -checked)]
                                                     (om/set-state! owner :only-stage hide)
                                                     (om/set-state! owner :set-filter "All"))
                                       }]
            "Only Stage"]]
          [:button {:on-click #(do
                                 ;(om/set-state! owner :set-filter "All")
                                 ;(om/set-state! owner :primary-filter "All")
                                 (om/set-state! owner :alignment-filter "All")
                                 (om/set-state! owner :secondary-filter "All")
                                 (om/set-state! owner :precise-filter "All")
                                 (om/set-state! owner :rarity-filter "All")
                                 (om/set-state! owner :haven-filter "All")
                                 (om/set-state! owner :skill-filter "All")
                                 (om/set-state! owner :race-filter "All")
                                 (om/set-state! owner :art-filter "All")
                                 )} "Semi-refine"]

          (om/build card-info-view cursor {:state {:selected-card (:selected-card state)}})]

         [:div.card-list {:on-scroll #(handle-scroll % owner state)}
          (om/build-all card-view
                        (let [s (selected-set-name state)
                              [alt-filter cards] (cond
                                                   (= s "All") [nil @all-cards]
                                                   :else
                                                   [nil (filter #(= (:full_set %) s) @all-cards)])]
                          (->> cards
                               (filter-dreamcards (:hide-dreamcards state))
                               (filter-released (:only-released state))
                               (filter-extras (:ul-extras state))
                               (filter-stage (:only-stage state))
                               (filter-unique (:only-unique state))
                               (filter-cards (:primary-filter state) :type)
                               (filter-cards (:alignment-filter state) :alignment)
                               (filter-cards (:rarity-filter state) :Rarity)
                               (filter-cards (:precise-filter state) :Precise)
                               (filter-second (if (= (:primary-filter state) "Site") true false) (:secondary-filter state))
                               (filter-haven (if (= (:primary-filter state) "Site") false true) (:haven-filter state))
                               (filter-cards (:art-filter state) :Artist)
                               (filter-parts (:race-filter state) :Race)
                               (filter-parts (:skill-filter state) :subtype)
                               (filter-texts (:texts-query state))
                               (filter-title (:title-query state))
                               (sort-by (sort-field (:sort-field state)))
                               (take (* (:page state) 28))))
                        {:key-fn #(str (:set_code %) (:code %) (:art %))
                         :fn #(assoc % :selected (and (= (:full_set %) (:full_set (:selected-card state)))
                                                      (= (:code %) (:code (:selected-card state)))))
                         :state {:cursor cursor :decorate-card true}})]
         ]))))

(om/root card-browser
         app-state
         {:shared {:notif-chan notif-chan
                   :pub-chan   pub-chan}
          :target (. js/document (getElementById "cardbrowser"))})
