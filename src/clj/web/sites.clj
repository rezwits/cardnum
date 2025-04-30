(ns web.sites)

;; STANDARD
(def standard-region
  [{:qty 3 :card "Andrast" :id "(TW)"}
   {:qty 3 :card "Andrast Coast" :id "(TW)"}
   {:qty 3 :card "Anduin Vales" :id "(TW)"}
   {:qty 3 :card "Anfalas" :id "(TW)"}
   {:qty 3 :card "Angmar" :id "(TW)"}
   {:qty 3 :card "Anórien" :id "(TW)"}
   {:qty 3 :card "Arthedain" :id "(TW)"}
   {:qty 3 :card "Bay of Belfalas" :id "(TW)"}
   {:qty 3 :card "Belfalas" :id "(TW)"}
   {:qty 3 :card "Brown Lands" :id "(TW)"}
   {:qty 3 :card "Cardolan" :id "(TW)"}
   {:qty 3 :card "Dagorlad" :id "(TW)"}
   {:qty 3 :card "Dorwinion" :id "(TW)"}
   {:qty 3 :card "Dunland" :id "(TW)"}
   {:qty 3 :card "Elven Shores" :id "(TW)"}
   {:qty 3 :card "Enedhwaith" :id "(TW)"}
   {:qty 3 :card "Eriadoran Coast" :id "(TW)"}
   {:qty 3 :card "Fangorn" :id "(TW)"}
   {:qty 3 :card "Forochel" :id "(TW)"}
   {:qty 3 :card "Gap of Isen" :id "(TW)"}
   {:qty 3 :card "Gorgoroth" :id "(TW)"}
   {:qty 3 :card "Grey Mountain Narrows" :id "(TW)"}
   {:qty 3 :card "Gundabad" :id "(TW)"}
   {:qty 3 :card "Harondor" :id "(TW)"}
   {:qty 3 :card "Heart of Mirkwood" :id "(TW)"}
   {:qty 3 :card "High Pass" :id "(TW)"}
   {:qty 3 :card "Hollin" :id "(TW)"}
   {:qty 3 :card "Horse Plains" :id "(TW)"}
   {:qty 3 :card "Imlad Morgul" :id "(TW)"}
   {:qty 3 :card "Iron Hills" :id "(TW)"}
   {:qty 3 :card "Ithilien" :id "(TW)"}
   {:qty 3 :card "Khand" :id "(TW)"}
   {:qty 3 :card "Lamedon" :id "(TW)"}
   {:qty 3 :card "Lebennin" :id "(TW)"}
   {:qty 3 :card "Lindon" :id "(TW)"}
   {:qty 3 :card "Mouths of the Anduin" :id "(TW)"}
   {:qty 3 :card "Northern Rhovanion" :id "(TW)"}
   {:qty 3 :card "Númeriador" :id "(TW)"}
   {:qty 3 :card "Nurn" :id "(TW)"}
   {:qty 3 :card "Old Pûkel Gap" :id "(TW)"}
   {:qty 3 :card "Old Pûkel-land" :id "(TW)"}
   {:qty 3 :card "Redhorn Gate" :id "(TW)"}
   {:qty 3 :card "Rhudaur" :id "(TW)"}
   {:qty 3 :card "Rohan" :id "(TW)"}
   {:qty 3 :card "Southern Mirkwood" :id "(TW)"}
   {:qty 3 :card "Southern Rhovanion" :id "(TW)"}
   {:qty 3 :card "The Shire" :id "(TW)"}
   {:qty 3 :card "Udûn" :id "(TW)"}
   {:qty 3 :card "Western Mirkwood" :id "(TW)"}
   {:qty 3 :card "Withered Heath" :id "(TW)"}
   {:qty 3 :card "Wold & Foothills" :id "(TW)"}
   {:qty 3 :card "Woodland Realm" :id "(TW)"}])

(def standard-metw-sites
  [{:qty 1 :card "Amon Hen" :id "[H] (TW)"}
   {:qty 1 :card "Bag End" :id "[H] (TW)"}
   {:qty 1 :card "Bandit Lair" :id "[H] (TW)"}
   {:qty 1 :card "Barad-dûr" :id "[H] (TW)"}
   {:qty 1 :card "Barrow-downs" :id "[H] (TW)"}
   {:qty 1 :card "Beorn's House" :id "[H] (TW)"}
   {:qty 1 :card "Blue Mountain Dwarf-hold" :id "[H] (TW)"}
   {:qty 1 :card "Bree" :id "[H] (TW)"}
   {:qty 1 :card "Cameth Brin" :id "[H] (TW)"}
   {:qty 1 :card "Carn Dûm" :id "[H] (TW)"}
   {:qty 1 :card "Caves of Ûlund" :id "[H] (TW)"}
   {:qty 1 :card "Cirith Ungol" :id "[H] (TW)"}
   {:qty 1 :card "Dancing Spire" :id "[H] (TW)"}
   {:qty 1 :card "Dead Marshes" :id "[H] (TW)"}
   {:qty 1 :card "Dimrill Dale" :id "[H] (TW)"}
   {:qty 1 :card "Dol Amroth" :id "[H] (TW)"}
   {:qty 1 :card "Dol Guldur" :id "[H] (TW)"}
   {:qty 1 :card "Drúadan Forest" :id "[H] (TW)"}
   {:qty 1 :card "Dunharrow" :id "[H] (TW)"}
   {:qty 1 :card "Dunnish Clan-hold" :id "[H] (TW)"}
   {:qty 1 :card "Eagles' Eyrie" :id "[H] (TW)"}
   {:qty 1 :card "Easterling Camp" :id "[H] (TW)"}
   {:qty 1 :card "Edoras" :id "[H] (TW)"}
   {:qty 1 :card "Ettenmoors" :id "[H] (TW)"}
   {:qty 1 :card "Gladden Fields" :id "[H] (TW)"}
   {:qty 1 :card "Glittering Caves" :id "[H] (TW)"}
   {:qty 1 :card "Goblin-gate" :id "[H] (TW)"}
   {:qty 1 :card "Henneth Annûn" :id "[H] (TW)"}
   {:qty 1 :card "Himring" :id "[H] (TW)"}
   {:qty 1 :card "Irerock" :id "[H] (TW)"}
   {:qty 1 :card "Iron Hill Dwarf-hold" :id "[H] (TW)"}
   {:qty 1 :card "Isengard" :id "[H] (TW)"}
   {:qty 1 :card "Isles of the Dead that Live" :id "[H] (TW)"}
   {:qty 1 :card "Lake-town" :id "[H] (TW)"}
   {:qty 1 :card "Lond Galen" :id "[H] (TW)"}
   {:qty 1 :card "Lossadan Cairn" :id "[H] (TW)"}
   {:qty 1 :card "Lossadan Camp" :id "[H] (TW)"}
   {:qty 1 :card "Minas Morgul" :id "[H] (TW)"}
   {:qty 1 :card "Minas Tirith" :id "[H] (TW)"}
   {:qty 1 :card "Moria" :id "[H] (TW)"}
   {:qty 1 :card "Mount Doom" :id "[H] (TW)"}
   {:qty 1 :card "Mount Gram" :id "[H] (TW)"}
   {:qty 1 :card "Mount Gundabad" :id "[H] (TW)"}
   {:qty 1 :card "Old Forest" :id "[H] (TW)"}
   {:qty 1 :card "Ost-in-Edhil" :id "[H] (TW)"}
   {:qty 1 :card "Pelargir" :id "[H] (TW)"}
   {:qty 1 :card "Rhosgobel" :id "[H] (TW)"}
   {:qty 1 :card "Ruined Signal Tower" :id "[H] (TW)"}
   {:qty 1 :card "Sarn Goriwing" :id "[H] (TW)"}
   {:qty 1 :card "Shelob's Lair" :id "[H] (TW)"}
   {:qty 1 :card "Shrel-Kain" :id "[H] (TW)"}
   {:qty 1 :card "Southron Oasis" :id "[H] (TW)"}
   {:qty 1 :card "Stone-circle" :id "[H] (TW)"}
   {:qty 1 :card "The Lonely Mountain" :id "[H] (TW)"}
   {:qty 1 :card "The Stones" :id "[H] (TW)"}
   {:qty 1 :card "The White Towers" :id "[H] (TW)"}
   {:qty 1 :card "The Wind Throne" :id "[H] (TW)"}
   {:qty 1 :card "Thranduil's Halls" :id "[H] (TW)"}
   {:qty 1 :card "Tolfalas" :id "[H] (TW)"}
   {:qty 1 :card "Vale of Erech" :id "[H] (TW)"}
   {:qty 1 :card "Variag Camp" :id "[H] (TW)"}
   {:qty 1 :card "Weathertop" :id "[H] (TW)"}
   {:qty 1 :card "Wellinghall" :id "[H] (TW)"}
   {:qty 1 :card "Woodmen-town" :id "[H] (TW)"}
   {:qty 1 :card "Wose Passage-hold" :id "[H] (TW)"}])

(def standard-metd-sites
  [{:qty 1 :card "Amon Hen" :id "[H] (TW)"}
   {:qty 1 :card "Bag End" :id "[H] (TW)"}
   {:qty 1 :card "Bandit Lair" :id "[H] (TW)"}
   {:qty 1 :card "Barad-dûr" :id "[H] (TW)"}
   {:qty 1 :card "Barrow-downs" :id "[H] (TW)"}
   {:qty 1 :card "Beorn's House" :id "[H] (TW)"}
   {:qty 1 :card "Blue Mountain Dwarf-hold" :id "[H] (TW)"}
   {:qty 1 :card "Bree" :id "[H] (TW)"}
   {:qty 1 :card "Buhr Widu" :id "[H] (TD)"}
   {:qty 1 :card "Cameth Brin" :id "[H] (TW)"}
   {:qty 1 :card "Carn Dûm" :id "[H] (TW)"}
   {:qty 1 :card "Caves of Ûlund" :id "[H] (TW)"}
   {:qty 1 :card "Cirith Ungol" :id "[H] (TW)"}
   {:qty 1 :card "Dale" :id "[H] (TD)"}
   {:qty 1 :card "Dancing Spire" :id "[H] (TW)"}
   {:qty 1 :card "Dead Marshes" :id "[H] (TW)"}
   {:qty 1 :card "Dimrill Dale" :id "[H] (TW)"}
   {:qty 1 :card "Dol Amroth" :id "[H] (TW)"}
   {:qty 1 :card "Dol Guldur" :id "[H] (TW)"}
   {:qty 1 :card "Drúadan Forest" :id "[H] (TW)"}
   {:qty 1 :card "Dunharrow" :id "[H] (TW)"}
   {:qty 1 :card "Dunnish Clan-hold" :id "[H] (TW)"}
   {:qty 1 :card "Eagles' Eyrie" :id "[H] (TW)"}
   {:qty 1 :card "Easterling Camp" :id "[H] (TW)"}
   {:qty 1 :card "Edoras" :id "[H] (TW)"}
   {:qty 1 :card "Ettenmoors" :id "[H] (TW)"}
   {:qty 1 :card "Framsburg" :id "[H] (TD)"}
   {:qty 1 :card "Gladden Fields" :id "[H] (TW)"}
   {:qty 1 :card "Glittering Caves" :id "[H] (TW)"}
   {:qty 1 :card "Goblin-gate" :id "[H] (TW)"}
   {:qty 1 :card "Gold Hill" :id "[H] (TD)"}
   {:qty 1 :card "Gondmaeglom" :id "[H] (TD)"}
   {:qty 1 :card "Henneth Annûn" :id "[H] (TW)"}
   {:qty 1 :card "Himring" :id "[H] (TW)"}
   {:qty 1 :card "Irerock" :id "[H] (TW)"}
   {:qty 1 :card "Iron Hill Dwarf-hold" :id "[H] (TW)"}
   {:qty 1 :card "Isengard" :id "[H] (TW)"}
   {:qty 1 :card "Isles of the Dead that Live" :id "[H] (TW)"}
   {:qty 1 :card "Isle of the Ulond" :id "[H] (TD)"}
   {:qty 1 :card "Lake-town" :id "[H] (TW)"}
   {:qty 1 :card "Lond Galen" :id "[H] (TW)"}
   {:qty 1 :card "Lossadan Cairn" :id "[H] (TW)"}
   {:qty 1 :card "Lossadan Camp" :id "[H] (TW)"}
   {:qty 1 :card "Minas Morgul" :id "[H] (TW)"}
   {:qty 1 :card "Minas Tirith" :id "[H] (TW)"}
   {:qty 1 :card "Moria" :id "[H] (TW)"}
   {:qty 1 :card "Mount Doom" :id "[H] (TW)"}
   {:qty 1 :card "Mount Gram" :id "[H] (TW)"}
   {:qty 1 :card "Mount Gundabad" :id "[H] (TW)"}
   {:qty 1 :card "Old Forest" :id "[H] (TW)"}
   {:qty 1 :card "Ost-in-Edhil" :id "[H] (TW)"}
   {:qty 1 :card "Ovir Hollow" :id "[H] (TD)"}
   {:qty 1 :card "Pelargir" :id "[H] (TW)"}
   {:qty 1 :card "Rhosgobel" :id "[H] (TW)"}
   {:qty 1 :card "Ruined Signal Tower" :id "[H] (TW)"}
   {:qty 1 :card "Sarn Goriwing" :id "[H] (TW)"}
   {:qty 1 :card "Shelob's Lair" :id "[H] (TW)"}
   {:qty 1 :card "Shrel-Kain" :id "[H] (TW)"}
   {:qty 1 :card "Southron Oasis" :id "[H] (TW)"}
   {:qty 1 :card "Stone-circle" :id "[H] (TW)"}
   {:qty 1 :card "Tharbad" :id "[H] (TD)"}
   {:qty 1 :card "The Lonely Mountain" :id "[H] (TW)"}
   {:qty 1 :card "The Stones" :id "[H] (TW)"}
   {:qty 1 :card "The White Towers" :id "[H] (TW)"}
   {:qty 1 :card "The Wind Throne" :id "[H] (TW)"}
   {:qty 1 :card "Thranduil's Halls" :id "[H] (TW)"}
   {:qty 1 :card "Tolfalas" :id "[H] (TW)"}
   {:qty 1 :card "Vale of Erech" :id "[H] (TW)"}
   {:qty 1 :card "Variag Camp" :id "[H] (TW)"}
   {:qty 1 :card "Weathertop" :id "[H] (TW)"}
   {:qty 1 :card "Wellinghall" :id "[H] (TW)"}
   {:qty 1 :card "Woodmen-town" :id "[H] (TW)"}
   {:qty 1 :card "Wose Passage-hold" :id "[H] (TW)"}
   {:qty 1 :card "Zarak Dûm" :id "[H] (TD)"}])

(def standard-medm-sites
  [{:qty 1 :card "Amon Hen" :id "[H] (TW)"}
   {:qty 1 :card "Bag End" :id "[H] (TW)"}
   {:qty 1 :card "Bandit Lair" :id "[H] (TW)"}
   {:qty 1 :card "Barad-dûr" :id "[H] (TW)"}
   {:qty 1 :card "Barrow-downs" :id "[H] (TW)"}
   {:qty 1 :card "Beorn's House" :id "[H] (TW)"}
   {:qty 1 :card "Blue Mountain Dwarf-hold" :id "[H] (TW)"}
   {:qty 1 :card "Bree" :id "[H] (TW)"}
   {:qty 1 :card "Buhr Widu" :id "[H] (TD)"}
   {:qty 1 :card "Cameth Brin" :id "[H] (TW)"}
   {:qty 1 :card "Carn Dûm" :id "[H] (TW)"}
   {:qty 1 :card "Caves of Ûlund" :id "[H] (TW)"}
   {:qty 1 :card "Cirith Ungol" :id "[H] (TW)"}
   {:qty 1 :card "Dale" :id "[H] (TD)"}
   {:qty 1 :card "Dancing Spire" :id "[H] (TW)"}
   {:qty 1 :card "Dead Marshes" :id "[H] (TW)"}
   {:qty 1 :card "Dimrill Dale" :id "[H] (TW)"}
   {:qty 1 :card "Dol Amroth" :id "[H] (TW)"}
   {:qty 1 :card "Dol Guldur" :id "[H] (TW)"}
   {:qty 1 :card "Drúadan Forest" :id "[H] (TW)"}
   {:qty 1 :card "Dunharrow" :id "[H] (TW)"}
   {:qty 1 :card "Dunnish Clan-hold" :id "[H] (TW)"}
   {:qty 1 :card "Eagles' Eyrie" :id "[H] (TW)"}
   {:qty 1 :card "Easterling Camp" :id "[H] (TW)"}
   {:qty 1 :card "Edoras" :id "[H] (TW)"}
   {:qty 1 :card "Ettenmoors" :id "[H] (TW)"}
   {:qty 1 :card "Framsburg" :id "[H] (TD)"}
   {:qty 1 :card "Gladden Fields" :id "[H] (TW)"}
   {:qty 1 :card "Glittering Caves" :id "[H] (TW)"}
   {:qty 1 :card "Goblin-gate" :id "[H] (TW)"}
   {:qty 1 :card "Gold Hill" :id "[H] (TD)"}
   {:qty 1 :card "Gondmaeglom" :id "[H] (TD)"}
   {:qty 1 :card "Haudh-in-Gwanûr" :id "[H] (DM)"}
   {:qty 1 :card "Henneth Annûn" :id "[H] (TW)"}
   {:qty 1 :card "Hermit's Hill" :id "[H] (DM)"}
   {:qty 1 :card "Himring" :id "[H] (TW)"}
   {:qty 1 :card "Irerock" :id "[H] (TW)"}
   {:qty 1 :card "Iron Hill Dwarf-hold" :id "[H] (TW)"}
   {:qty 1 :card "Isengard" :id "[H] (TW)"}
   {:qty 1 :card "Isle of the Ulond" :id "[H] (TD)"}
   {:qty 1 :card "Isles of the Dead that Live" :id "[H] (TW)"}
   {:qty 1 :card "Lake-town" :id "[H] (TW)"}
   {:qty 1 :card "Lond Galen" :id "[H] (TW)"}
   {:qty 1 :card "Lossadan Cairn" :id "[H] (TW)"}
   {:qty 1 :card "Lossadan Camp" :id "[H] (TW)"}
   {:qty 1 :card "Minas Morgul" :id "[H] (TW)"}
   {:qty 1 :card "Minas Tirith" :id "[H] (TW)"}
   {:qty 1 :card "Moria" :id "[H] (TW)"}
   {:qty 1 :card "Mount Doom" :id "[H] (TW)"}
   {:qty 1 :card "Mount Gram" :id "[H] (TW)"}
   {:qty 1 :card "Mount Gundabad" :id "[H] (TW)"}
   {:qty 1 :card "Old Forest" :id "[H] (TW)"}
   {:qty 1 :card "Ost-in-Edhil" :id "[H] (TW)"}
   {:qty 1 :card "Ovir Hollow" :id "[H] (TD)"}
   {:qty 1 :card "Pelargir" :id "[H] (TW)"}
   {:qty 1 :card "Rhosgobel" :id "[H] (TW)"}
   {:qty 1 :card "Ruined Signal Tower" :id "[H] (TW)"}
   {:qty 1 :card "Sarn Goriwing" :id "[H] (TW)"}
   {:qty 1 :card "Shelob's Lair" :id "[H] (TW)"}
   {:qty 1 :card "Shrel-Kain" :id "[H] (TW)"}
   {:qty 1 :card "Southron Oasis" :id "[H] (TW)"}
   {:qty 1 :card "Stone-circle" :id "[H] (TW)"}
   {:qty 1 :card "Tharbad" :id "[H] (TD)"}
   {:qty 1 :card "The Gem-deeps" :id "[H] (DM)"}
   {:qty 1 :card "The Iron-deeps" :id "[H] (DM)"}
   {:qty 1 :card "The Lonely Mountain" :id "[H] (TW)"}
   {:qty 1 :card "The Pûkel-deeps" :id "[H] (DM)"}
   {:qty 1 :card "The Stones" :id "[H] (TW)"}
   {:qty 1 :card "The Sulfur-deeps" :id "[H] (DM)"}
   {:qty 1 :card "The Under-courts" :id "[H] (DM)"}
   {:qty 1 :card "The Under-galleries" :id "[H] (DM)"}
   {:qty 1 :card "The Under-gates" :id "[H] (DM)"}
   {:qty 1 :card "The Under-grottos" :id "[H] (DM)"}
   {:qty 1 :card "The Under-leas" :id "[H] (DM)"}
   {:qty 1 :card "The Under-vaults" :id "[H] (DM)"}
   {:qty 1 :card "The White Towers" :id "[H] (TW)"}
   {:qty 1 :card "The Wind Throne" :id "[H] (TW)"}
   {:qty 1 :card "Thranduil's Halls" :id "[H] (TW)"}
   {:qty 1 :card "Tolfalas" :id "[H] (TW)"}
   {:qty 1 :card "Urlurtsu Nurn" :id "[H] (DM)"}
   {:qty 1 :card "Vale of Erech" :id "[H] (TW)"}
   {:qty 1 :card "Variag Camp" :id "[H] (TW)"}
   {:qty 1 :card "Weathertop" :id "[H] (TW)"}
   {:qty 1 :card "Wellinghall" :id "[H] (TW)"}
   {:qty 1 :card "Woodmen-town" :id "[H] (TW)"}
   {:qty 1 :card "Wose Passage-hold" :id "[H] (TW)"}
   {:qty 1 :card "Zarak Dûm" :id "[H] (TD)"}])

(def standard-wizard
  [{:qty 1 :card "Amon Hen" :id "[H] (TW)"}
   {:qty 1 :card "Bag End" :id "[H] (TW)"}
   {:qty 1 :card "Bandit Lair" :id "[H] (TW)"}
   {:qty 1 :card "Barad-dûr" :id "[H] (TW)"}
   {:qty 1 :card "Barrow-downs" :id "[H] (TW)"}
   {:qty 1 :card "Beorn's House" :id "[H] (TW)"}
   {:qty 1 :card "Blue Mountain Dwarf-hold" :id "[H] (TW)"}
   {:qty 1 :card "Bree" :id "[H] (TW)"}
   {:qty 1 :card "Buhr Widu" :id "[H] (TD)"}
   {:qty 1 :card "Cameth Brin" :id "[H] (TW)"}
   {:qty 1 :card "Carn Dûm" :id "[H] (TW)"}
   {:qty 1 :card "Caves of Ûlund" :id "[H] (TW)"}
   {:qty 1 :card "Cirith Gorgor" :id "[H] (AS)"}
   {:qty 1 :card "Cirith Ungol" :id "[H] (TW)"}
   {:qty 1 :card "Dale" :id "[H] (TD)"}
   {:qty 1 :card "Dancing Spire" :id "[H] (TW)"}
   {:qty 1 :card "Dead Marshes" :id "[H] (TW)"}
   {:qty 1 :card "Dimrill Dale" :id "[H] (TW)"}
   {:qty 1 :card "Dol Amroth" :id "[H] (TW)"}
   {:qty 1 :card "Dol Guldur" :id "[H] (TW)"}
   {:qty 1 :card "Drúadan Forest" :id "[H] (TW)"}
   {:qty 1 :card "Dunharrow" :id "[H] (TW)"}
   {:qty 1 :card "Dunnish Clan-hold" :id "[H] (TW)"}
   {:qty 1 :card "Eagles' Eyrie" :id "[H] (TW)"}
   {:qty 1 :card "Easterling Camp" :id "[H] (TW)"}
   {:qty 1 :card "Edoras" :id "[H] (TW)"}
   {:qty 1 :card "Ettenmoors" :id "[H] (TW)"}
   {:qty 1 :card "Framsburg" :id "[H] (TD)"}
   {:qty 1 :card "Geann a-Lisch" :id "[H] (AS)"}
   {:qty 1 :card "Gladden Fields" :id "[H] (TW)"}
   {:qty 1 :card "Glittering Caves" :id "[H] (TW)"}
   {:qty 1 :card "Gobel Mírlond" :id "[H] (AS)"}
   {:qty 1 :card "Goblin-gate" :id "[H] (TW)"}
   {:qty 1 :card "Gold Hill" :id "[H] (TD)"}
   {:qty 1 :card "Gondmaeglom" :id "[H] (TD)"}
   {:qty 1 :card "Haudh-in-Gwanûr" :id "[H] (DM)"}
   {:qty 1 :card "Henneth Annûn" :id "[H] (TW)"}
   {:qty 1 :card "Hermit's Hill" :id "[H] (DM)"}
   {:qty 1 :card "Himring" :id "[H] (TW)"}
   {:qty 1 :card "Irerock" :id "[H] (TW)"}
   {:qty 1 :card "Iron Hill Dwarf-hold" :id "[H] (TW)"}
   {:qty 1 :card "Isengard" :id "[H] (TW)"}
   {:qty 1 :card "Isle of the Ulond" :id "[H] (TD)"}
   {:qty 1 :card "Isles of the Dead that Live" :id "[H] (TW)"}
   {:qty 1 :card "Lake-town" :id "[H] (TW)"}
   {:qty 1 :card "Lond Galen" :id "[H] (TW)"}
   {:qty 1 :card "Lossadan Cairn" :id "[H] (TW)"}
   {:qty 1 :card "Lossadan Camp" :id "[H] (TW)"}
   {:qty 1 :card "Minas Morgul" :id "[H] (TW)"}
   {:qty 1 :card "Minas Tirith" :id "[H] (TW)"}
   {:qty 1 :card "Moria" :id "[H] (TW)"}
   {:qty 1 :card "Mount Doom" :id "[H] (TW)"}
   {:qty 1 :card "Mount Gram" :id "[H] (TW)"}
   {:qty 1 :card "Mount Gundabad" :id "[H] (TW)"}
   {:qty 1 :card "Nûrniag Camp" :id "[H] (AS)"}
   {:qty 1 :card "Old Forest" :id "[H] (TW)"}
   {:qty 1 :card "Ost-in-Edhil" :id "[H] (TW)"}
   {:qty 1 :card "Ovir Hollow" :id "[H] (TD)"}
   {:qty 1 :card "Pelargir" :id "[H] (TW)"}
   {:qty 1 :card "Raider-hold" :id "[H] (AS)"}
   {:qty 1 :card "Rhosgobel" :id "[H] (TW)"}
   {:qty 1 :card "Ruined Signal Tower" :id "[H] (TW)"}
   {:qty 1 :card "Sarn Goriwing" :id "[H] (TW)"}
   {:qty 1 :card "Shelob's Lair" :id "[H] (TW)"}
   {:qty 1 :card "Shrel-Kain" :id "[H] (TW)"}
   {:qty 1 :card "Southron Oasis" :id "[H] (TW)"}
   {:qty 1 :card "Stone-circle" :id "[H] (TW)"}
   {:qty 1 :card "Tharbad" :id "[H] (TD)"}
   {:qty 1 :card "The Gem-deeps" :id "[H] (DM)"}
   {:qty 1 :card "The Iron-deeps" :id "[H] (DM)"}
   {:qty 1 :card "The Lonely Mountain" :id "[H] (TW)"}
   {:qty 1 :card "The Pûkel-deeps" :id "[H] (DM)"}
   {:qty 1 :card "The Stones" :id "[H] (TW)"}
   {:qty 1 :card "The Sulfur-deeps" :id "[H] (DM)"}
   {:qty 1 :card "The Under-courts" :id "[H] (DM)"}
   {:qty 1 :card "The Under-galleries" :id "[H] (DM)"}
   {:qty 1 :card "The Under-gates" :id "[H] (DM)"}
   {:qty 1 :card "The Under-grottos" :id "[H] (DM)"}
   {:qty 1 :card "The Under-leas" :id "[H] (DM)"}
   {:qty 1 :card "The Under-vaults" :id "[H] (DM)"}
   {:qty 1 :card "The White Towers" :id "[H] (TW)"}
   {:qty 1 :card "The Wind Throne" :id "[H] (TW)"}
   {:qty 1 :card "The Worthy Hills" :id "[H] (AS)"}
   {:qty 1 :card "Thranduil's Halls" :id "[H] (TW)"}
   {:qty 1 :card "Tolfalas" :id "[H] (TW)"}
   {:qty 1 :card "Urlurtsu Nurn" :id "[H] (DM)"}
   {:qty 1 :card "Vale of Erech" :id "[H] (TW)"}
   {:qty 1 :card "Variag Camp" :id "[H] (TW)"}
   {:qty 1 :card "Weathertop" :id "[H] (TW)"}
   {:qty 1 :card "Wellinghall" :id "[H] (TW)"}
   {:qty 1 :card "Woodmen-town" :id "[H] (TW)"}
   {:qty 1 :card "Wose Passage-hold" :id "[H] (TW)"}
   {:qty 1 :card "Zarak Dûm" :id "[H] (TD)"}])

(def standard-minion
  [{:qty 1 :card "Amon Hen" :id "[M] (LE)"}
   {:qty 1 :card "Bag End" :id "[M] (LE)"}
   {:qty 1 :card "Bandit Lair" :id "[M] (LE)"}
   {:qty 1 :card "Barrow-downs" :id "[M] (LE)"}
   {:qty 1 :card "Beorn's House" :id "[M] (LE)"}
   {:qty 1 :card "Blue Mountain Dwarf-hold" :id "[M] (LE)"}
   {:qty 1 :card "Bree" :id "[M] (LE)"}
   {:qty 1 :card "Buhr Widu" :id "[M] (LE)"}
   {:qty 1 :card "Cameth Brin" :id "[M] (LE)"}
   {:qty 1 :card "Caves of Ûlund" :id "[M] (LE)"}
   {:qty 1 :card "Dale" :id "[M] (LE)"}
   {:qty 1 :card "Dancing Spire" :id "[M] (AS)"}
   {:qty 1 :card "Dead Marshes" :id "[M] (LE)"}
   {:qty 1 :card "Dimrill Dale" :id "[M] (LE)"}
   {:qty 1 :card "Dol Amroth" :id "[M] (LE)"}
   {:qty 1 :card "Drúadan Forest" :id "[M] (LE)"}
   {:qty 1 :card "Dunharrow" :id "[M] (LE)"}
   {:qty 1 :card "Dunnish Clan-hold" :id "[M] (LE)"}
   {:qty 1 :card "Eagles' Eyrie" :id "[M] (AS)"}
   {:qty 1 :card "Easterling Camp" :id "[M] (LE)"}
   {:qty 1 :card "Edhellond" :id "[M] (AS)"}
   {:qty 1 :card "Edoras" :id "[M] (LE)"}
   {:qty 1 :card "Ettenmoors" :id "[M] (LE)"}
   {:qty 1 :card "Framsburg" :id "[M] (AS)"}
   {:qty 1 :card "Gladden Fields" :id "[M] (LE)"}
   {:qty 1 :card "Glittering Caves" :id "[M] (LE)"}
   {:qty 1 :card "Gobel Mírlond" :id "[M] (LE)"}
   {:qty 1 :card "Goblin-gate" :id "[M] (LE)"}
   {:qty 1 :card "Gold Hill" :id "[M] (AS)"}
   {:qty 1 :card "Gondmaeglom" :id "[M] (LE)"}
   {:qty 1 :card "Grey Havens" :id "[M] (AS)"}
   {:qty 1 :card "Haudh-in-Gwanûr" :id "[M] (LE)"}
   {:qty 1 :card "Henneth Annûn" :id "[M] (LE)"}
   {:qty 1 :card "Hermit's Hill" :id "[M] (LE)"}
   {:qty 1 :card "Himring" :id "[M] (AS)"}
   {:qty 1 :card "Irerock" :id "[M] (AS)"}
   {:qty 1 :card "Iron Hill Dwarf-hold" :id "[M] (LE)"}
   {:qty 1 :card "Isengard" :id "[M] (LE)"}
   {:qty 1 :card "Isle of the Ulond" :id "[M] (AS)"}
   {:qty 1 :card "Isles of the Dead That Live" :id "[M] (AS)"}
   {:qty 1 :card "Lake-town" :id "[M] (LE)"}
   {:qty 1 :card "Lond Galen" :id "[M] (LE)"}
   {:qty 1 :card "Lórien" :id "[M] (AS)"}
   {:qty 1 :card "Lossadan Cairn" :id "[M] (LE)"}
   {:qty 1 :card "Lossadan Camp" :id "[M] (LE)"}
   {:qty 1 :card "Minas Tirith" :id "[M] (LE)"}
   {:qty 1 :card "Mount Doom" :id "[M] (LE)"}
   {:qty 1 :card "Mount Gram" :id "[M] (LE)"}
   {:qty 1 :card "Mount Gundabad" :id "[M] (LE)"}
   {:qty 1 :card "Nûrniag Camp" :id "[M] (LE)"}
   {:qty 1 :card "Old Forest" :id "[M] (AS)"}
   {:qty 1 :card "Ost-in-Edhil" :id "[M] (LE)"}
   {:qty 1 :card "Ovir Hollow" :id "[M] (AS)"}
   {:qty 1 :card "Pelargir" :id "[M] (LE)"}
   {:qty 1 :card "Raider-hold" :id "[M] (LE)"}
   {:qty 1 :card "Rhosgobel" :id "[M] (AS)"}
   {:qty 1 :card "Rivendell" :id "[M] (AS)"}
   {:qty 1 :card "Ruined Signal Tower" :id "[M] (LE)"}
   {:qty 1 :card "Sarn Goriwing" :id "[M] (LE)"}
   {:qty 1 :card "Shelob's Lair" :id "[M] (LE)"}
   {:qty 1 :card "Shrel-Kain" :id "[M] (LE)"}
   {:qty 1 :card "Southron Oasis" :id "[M] (LE)"}
   {:qty 1 :card "Stone-circle" :id "[M] (LE)"}
   {:qty 1 :card "Tharbad" :id "[M] (LE)"}
   {:qty 1 :card "The Lonely Mountain" :id "[M] (LE)"}
   {:qty 1 :card "The Stones" :id "[M] (LE)"}
   {:qty 1 :card "The White Towers" :id "[M] (LE)"}
   {:qty 1 :card "The Wind Throne" :id "[M] (LE)"}
   {:qty 1 :card "The Worthy Hills" :id "[M] (LE)"}
   {:qty 1 :card "Thranduil's Halls" :id "[M] (LE)"}
   {:qty 1 :card "Tolfalas" :id "[M] (AS)"}
   {:qty 1 :card "Urlurtsu Nurn" :id "[M] (LE)"}
   {:qty 1 :card "Vale of Erech" :id "[M] (LE)"}
   {:qty 1 :card "Variag Camp" :id "[M] (LE)"}
   {:qty 1 :card "Weathertop" :id "[M] (AS)"}
   {:qty 1 :card "Wellinghall" :id "[M] (AS)"}
   {:qty 1 :card "Woodmen-town" :id "[M] (LE)"}
   {:qty 1 :card "Wose Passage-hold" :id "[M] (LE)"}
   {:qty 1 :card "Zarak Dûm" :id "[M] (LE)"}])

(def standard-balrog
  [{:qty 1 :card "Ancient Deep-hold" :id "[B] (BA)"}
   {:qty 1 :card "Barad-dûr" :id "[B] (BA)"}
   {:qty 1 :card "Carn Dûm" :id "[B] (BA)"}
   {:qty 1 :card "Cirith Gorgor" :id "[B] (BA)"}
   {:qty 1 :card "Cirith Ungol" :id "[B] (BA)"}
   {:qty 1 :card "Dol Guldur" :id "[B] (BA)"}
   {:qty 1 :card "Minas Morgul" :id "[B] (BA)"}
   {:qty 4 :card "Moria" :id "[B] (BA)"}
   {:qty 1 :card "Remains of Thangorodrim" :id "[B] (BA)"}
   {:qty 1 :card "The Drowning-deeps" :id "[B] (BA)"}
   {:qty 1 :card "The Gem-deeps" :id "[B] (BA)"}
   {:qty 1 :card "The Iron-deeps" :id "[B] (BA)"}
   {:qty 1 :card "The Pûkel-deeps" :id "[B] (BA)"}
   {:qty 1 :card "The Rusted-deeps" :id "[B] (BA)"}
   {:qty 1 :card "The Sulfur-deeps" :id "[B] (BA)"}
   {:qty 1 :card "The Under-courts" :id "[B] (BA)"}
   {:qty 1 :card "The Under-galleries" :id "[B] (BA)"}
   {:qty 4 :card "The Under-gates" :id "[B] (BA)"}
   {:qty 1 :card "The Under-grottos" :id "[B] (BA)"}
   {:qty 1 :card "The Under-leas" :id "[B] (BA)"}
   {:qty 1 :card "The Under-vaults" :id "[B] (BA)"}
   {:qty 1 :card "The Wind-deeps" :id "[B] (BA)"}])

(def standard-wizard-havens
  [{:qty 4 :card "Edhellond" :id "[H] (TW)"}
   {:qty 4 :card "Grey Havens" :id "[H] (TW)"}
   {:qty 4 :card "Lórien" :id "[H] (TW)"}
   {:qty 4 :card "Rivendell" :id "[H] (TW)"}])

(def standard-wizard-haven
  [{:qty 1 :card "Edhellond" :id "[H] (TW)"}
   {:qty 1 :card "Grey Havens" :id "[H] (TW)"}
   {:qty 1 :card "Lórien" :id "[H] (TW)"}
   {:qty 1 :card "Rivendell" :id "[H] (TW)"}])

(def standard-minion-havens
  [{:qty 4 :card "Carn Dûm" :id "[M] (LE)"}
   {:qty 4 :card "Dol Guldur" :id "[M] (LE)"}
   {:qty 4 :card "Geann a-Lisch" :id "[M] (LE)"}
   {:qty 4 :card "Minas Morgul" :id "[M] (LE)"}])

(def standard-minion-haven
  [{:qty 1 :card "Carn Dûm" :id "[M] (LE)"}
   {:qty 1 :card "Dol Guldur" :id "[M] (LE)"}
   {:qty 1 :card "Geann a-Lisch" :id "[M] (LE)"}
   {:qty 1 :card "Minas Morgul" :id "[M] (LE)"}])

(def standard-fallen-havens
  [{:qty 4 :card "Deep Mines" :id "(WH)"}
   {:qty 4 :card "Isengard" :id "[F] (WH)"}
   {:qty 4 :card "Rhosgobel" :id "[F] (WH)"}
   {:qty 4 :card "The White Towers" :id "[F] (WH)"}])

(def standard-minion-balrog
  [{:qty 1 :card "Barad-dûr" :id "[M] (LE)"}
   {:qty 1 :card "Cirith Gorgor" :id "[M] (LE)"}
   {:qty 1 :card "Cirith Ungol" :id "[M] (LE)"}
   {:qty 1 :card "Moria" :id "[M] (LE)"}
   {:qty 1 :card "The Gem-deeps" :id "[M] (AS)"}
   {:qty 1 :card "The Iron-deeps" :id "[M] (AS)"}
   {:qty 1 :card "The Pûkel-deeps" :id "[M] (AS)"}
   {:qty 1 :card "The Sulfur-deeps" :id "[M] (AS)"}
   {:qty 1 :card "The Under-courts" :id "[M] (AS)"}
   {:qty 1 :card "The Under-galleries" :id "[M] (AS)"}
   {:qty 1 :card "The Under-gates" :id "[M] (AS)"}
   {:qty 1 :card "The Under-grottos" :id "[M] (AS)"}
   {:qty 1 :card "The Under-leas" :id "[M] (AS)"}
   {:qty 1 :card "The Under-vaults" :id "[M] (AS)"}])

(def standard-balrog-extras
  [{:qty 1 :card "Ancient Deep-hold" :id "[B] (BA)"}
   {:qty 1 :card "Remains of Thangorodrim" :id "[B] (BA)"}
   {:qty 1 :card "The Drowning-deeps" :id "[B] (BA)"}
   {:qty 1 :card "The Rusted-deeps" :id "[B] (BA)"}
   {:qty 1 :card "The Wind-deeps" :id "[B] (BA)"}])

;; DREAMCARDS
(def dreamcard-region
  [{:qty 3 :card "Ammu Baj" :id "(BO)"}
   {:qty 3 :card "Andrast" :id "(TW)"}
   {:qty 3 :card "Andrast Coast" :id "(TW)"}
   {:qty 3 :card "Anduin Vales" :id "(TW)"}
   {:qty 3 :card "Anfalas" :id "(TW)"}
   {:qty 3 :card "Angmar" :id "(TW)"}
   {:qty 3 :card "Anórien" :id "(TW)"}
   {:qty 3 :card "Arthedain" :id "(TW)"}
   {:qty 3 :card "Arysis" :id "(SL)"}
   {:qty 3 :card "Azjan" :id "(BU)"}
   {:qty 3 :card "Barl Syrnac" :id "(BU)"}
   {:qty 3 :card "Bay of Belfalas" :id "(TW)"}
   {:qty 3 :card "Bay of Drêl" :id "(DS)"}
   {:qty 3 :card "Bay of Felaya" :id "(SL)"}
   {:qty 3 :card "Bay of Tulwang" :id "(SL)"}
   {:qty 3 :card "Belfalas" :id "(TW)"}
   {:qty 3 :card "Bellazen" :id "(SL)"}
   {:qty 3 :card "Bosiri" :id "(SL)"}
   {:qty 3 :card "Bozisha-Miraz" :id "(SL)"}
   {:qty 3 :card "Brown Lands" :id "(TW)"}
   {:qty 3 :card "Bulchyades" :id "(BO)"}
   {:qty 3 :card "Cardolan" :id "(TW)"}
   {:qty 3 :card "Chelkar" :id "(NE)"}
   {:qty 3 :card "Chennacatt" :id "(SL)"}
   {:qty 3 :card "Chey Sart" :id "(CP)"}
   {:qty 3 :card "Chy" :id "(BO)"}
   {:qty 3 :card "Cleft of Goats" :id "(DS)"}
   {:qty 3 :card "Clyan" :id "(BO)"}
   {:qty 3 :card "Coast of Harad" :id "(SL)"}
   {:qty 3 :card "Curinshiban" :id "(DS)"}
   {:qty 3 :card "Dagorlad" :id "(TW)"}
   {:qty 3 :card "Dor Bendor" :id "(NW)"}
   {:qty 3 :card "Dorwinion" :id "(TW)"}
   {:qty 3 :card "Dragon Gap" :id "(GW)"}
   {:qty 3 :card "Drêl" :id "(DS)"}
   {:qty 3 :card "Drenâd" :id "(CA)"}
   {:qty 3 :card "Dune Sea" :id "(SL)"}
   {:qty 3 :card "Dunland" :id "(TW)"}
   {:qty 3 :card "Dûshera" :id "(CA)"}
   {:qty 3 :card "Dyr" :id "(BU)"}
   {:qty 3 :card "East Bay of Forochel" :id "(NW)"}
   {:qty 3 :card "East Bay of Ormal" :id "(BO)"}
   {:qty 3 :card "Ekkaia" :id "(NW)"}
   {:qty 3 :card "Elorna" :id "(DS)"}
   {:qty 3 :card "Elven Shores" :id "(TW)"}
   {:qty 3 :card "Enedhwaith" :id "(TW)"}
   {:qty 3 :card "Eorstan" :id "(DF)"}
   {:qty 3 :card "Ered Harmal [N]" :id "(CP)"}
   {:qty 3 :card "Ered Lithui" :id "(NE)"}
   {:qty 3 :card "Ered Muil" :id "(NW)"}
   {:qty 3 :card "Ered Ormal" :id "(BO)"}
   {:qty 3 :card "Eriadoran Coast" :id "(TW)"}
   {:qty 3 :card "Erim Póa" :id "(SL)"}
   {:qty 3 :card "Everdalf" :id "(NW)"}
   {:qty 3 :card "Fangorn" :id "(TW)"}
   {:qty 3 :card "Felaya" :id "(SL)"}
   {:qty 3 :card "Forochel" :id "(TW)"}
   {:qty 3 :card "Forovirkain" :id "(ML)"}
   {:qty 3 :card "Forrhûn" :id "(NE)"}
   {:qty 3 :card "Gan" :id "(CA)"}
   {:qty 3 :card "Gap of Isen" :id "(TW)"}
   {:qty 3 :card "Geshaan" :id "(CA)"}
   {:qty 3 :card "Gondalf" :id "(NW)"}
   {:qty 3 :card "Gorgoroth" :id "(TW)"}
   {:qty 3 :card "Grey Mountain Narrows" :id "(TW)"}
   {:qty 3 :card "Grey Mountains" :id "(GW)"}
   {:qty 3 :card "Gundabad" :id "(TW)"}
   {:qty 3 :card "Harondor" :id "(TW)"}
   {:qty 3 :card "Harrhûn" :id "(NE)"}
   {:qty 3 :card "Harshandatt" :id "(BO)"}
   {:qty 3 :card "Haruzan" :id "(SL)"}
   {:qty 3 :card "Hathor" :id "(CA)"}
   {:qty 3 :card "Heart of Mirkwood" :id "(TW)"}
   {:qty 3 :card "Heb Aaraan" :id "(CP)"}
   {:qty 3 :card "Helkëar" :id "(BU)"}
   {:qty 3 :card "High Pass" :id "(TW)"}
   {:qty 3 :card "Hollin" :id "(TW)"}
   {:qty 3 :card "Horse Plains" :id "(TW)"}
   {:qty 3 :card "Hûb Uichel" :id "(NW)"}
   {:qty 3 :card "Hyarmenfalas" :id "(WR)"}
   {:qty 3 :card "Hyarn" :id "(DS)"}
   {:qty 3 :card "Hyarnustar Coast" :id "(DS)"}
   {:qty 3 :card "Imlad Morgul" :id "(TW)"}
   {:qty 3 :card "Iron Hills" :id "(TW)"}
   {:qty 3 :card "Isfahan" :id "(SL)"}
   {:qty 3 :card "Isra" :id "(SL)"}
   {:qty 3 :card "Ithilien" :id "(TW)"}
   {:qty 3 :card "Kes Arik" :id "(SL)"}
   {:qty 3 :card "Khand" :id "(TW)"}
   {:qty 3 :card "Kirmlesra" :id "(BO)"}
   {:qty 3 :card "Koronandë" :id "(CA)"}
   {:qty 3 :card "Koros Bay" :id "(CA)"}
   {:qty 3 :card "Kurryan Bay" :id "(DS)"}
   {:qty 3 :card "Kykurian Kyn" :id "(CP)"}
   {:qty 3 :card "Kythor" :id "(BO)"}
   {:qty 3 :card "Lamedon" :id "(TW)"}
   {:qty 3 :card "Lebennin" :id "(TW)"}
   {:qty 3 :card "Lhûgdalf" :id "(BU)"}
   {:qty 3 :card "Lindalf" :id "(NW)"}
   {:qty 3 :card "Lindon" :id "(TW)"}
   {:qty 3 :card "Lotan" :id "(BU)"}
   {:qty 3 :card "Lurmsakûn" :id "(SL)"}
   {:qty 3 :card "Lyneria" :id "(BO)"}
   {:qty 3 :card "Mag" :id "(DS)"}
   {:qty 3 :card "Mardruak" :id "(SL)"}
   {:qty 3 :card "Mardruak Cape" :id "(SL)"}
   {:qty 3 :card "Methran Cape" :id "(DS)"}
   {:qty 3 :card "Minheldolath" :id "(NW)"}
   {:qty 3 :card "Mirëdor" :id "(DS)"}
   {:qty 3 :card "Mirror of Fire" :id "(SL)"}
   {:qty 3 :card "Misty Mountains - Northern Spur" :id "(GW)"}
   {:qty 3 :card "Misty Mountains - Southern Spur" :id "(TI)"}
   {:qty 3 :card "Mouths of the Anduin" :id "(TW)"}
   {:qty 3 :card "Mûlambur" :id "(CA)"}
   {:qty 3 :card "Mûmakan" :id "(CA)"}
   {:qty 3 :card "Mûmakan Coasts" :id "(CA)"}
   {:qty 3 :card "Mur Fostisyr" :id "(BU)"}
   {:qty 3 :card "Narthalf" :id "(NW)"}
   {:qty 3 :card "Né Tava" :id "(SL)"}
   {:qty 3 :card "Northern Rhovanion" :id "(TW)"}
   {:qty 3 :card "Númeriador" :id "(TW)"}
   {:qty 3 :card "Nûrad" :id "(CP)"}
   {:qty 3 :card "Nuriag" :id "(CP)"}
   {:qty 3 :card "Nurn" :id "(TW)"}
   {:qty 3 :card "Old Forest" :id "(FB)"}
   {:qty 3 :card "Old Pûkel Gap" :id "(TW)"}
   {:qty 3 :card "Old Pûkel-land" :id "(TW)"}
   {:qty 3 :card "Olyas Kriis" :id "(BO)"}
   {:qty 3 :card "Orgothraath" :id "(CP)"}
   {:qty 3 :card "Pel" :id "(DS)"}
   {:qty 3 :card "Pel Bight" :id "(DS)"}
   {:qty 3 :card "Pezarsan" :id "(SL)"}
   {:qty 3 :card "Rast Losnaeth" :id "(NW)"}
   {:qty 3 :card "Redhorn Gate" :id "(TW)"}
   {:qty 3 :card "Relmether" :id "(CP)"}
   {:qty 3 :card "Rhudaur" :id "(TW)"}
   {:qty 3 :card "Rohan" :id "(TW)"}
   {:qty 3 :card "Sakal an-Khâr" :id "(BO)"}
   {:qty 3 :card "Sára Bask" :id "(BO)"}
   {:qty 3 :card "Sea of Rhûn" :id "(CP)"}
   {:qty 3 :card "Seznebab" :id "(SL)"}
   {:qty 3 :card "Shores of Maquatostoth" :id "(CA)"}
   {:qty 3 :card "Shores of Ormal" :id "(BO)"}
   {:qty 3 :card "Siakan" :id "(SL)"}
   {:qty 3 :card "Southern Mirkwood" :id "(TW)"}
   {:qty 3 :card "Southern Rhovanion" :id "(TW)"}
   {:qty 3 :card "Straight of Tumag" :id "(CA)"}
   {:qty 3 :card "Suza Sumar" :id "(SL)"}
   {:qty 3 :card "Talath Oiohelka" :id "(NW)"}
   {:qty 3 :card "Talath Uichel" :id "(NW)"}
   {:qty 3 :card "Tâliran" :id "(CA)"}
   {:qty 3 :card "Tantûrak" :id "(CA)"}
   {:qty 3 :card "Taur Rómen" :id "(FB)"}
   {:qty 3 :card "The Shire" :id "(TW)"}
   {:qty 3 :card "The Sundering Sea" :id "(ML)"}
   {:qty 3 :card "Thorenaer" :id "(NW)"}
   {:qty 3 :card "Tuktan" :id "(CA)"}
   {:qty 3 :card "Tulwang" :id "(SL)"}
   {:qty 3 :card "Tumag" :id "(CA)"}
   {:qty 3 :card "Udûn" :id "(TW)"}
   {:qty 3 :card "Ukal Sêj" :id "(BU)"}
   {:qty 3 :card "Ûsakan" :id "(CA)"}
   {:qty 3 :card "Ûsakan Bay" :id "(CA)"}
   {:qty 3 :card "West Bay of Forochel" :id "(NW)"}
   {:qty 3 :card "West Bay of Ormal" :id "(BO)"}
   {:qty 3 :card "Western Mirkwood" :id "(TW)"}
   {:qty 3 :card "Withered Heath" :id "(TW)"}
   {:qty 3 :card "Wold & Foothills" :id "(TW)"}
   {:qty 3 :card "Woodland Realm" :id "(TW)"}
   {:qty 3 :card "Yellow Mountains - Eastern Spur" :id "(SL)"}
   {:qty 3 :card "Yellow Mountains - Western Spur" :id "(DS)"}
   {:qty 3 :card "Yellow Mountains" :id "(SL)"}
   {:qty 3 :card "Zajantak" :id "(SL)"}
   {:qty 3 :card "Zurghôr" :id "(BO)"}])

(def dreamcard-wizard
  [{:qty 1 :card "Abandoned Caravansary" :id "[H] (SL)"}
   {:qty 1 :card "Achrond" :id "[H] (NW)"}
   {:qty 1 :card "Adan Tomb" :id "[H] (CP)"}
   {:qty 1 :card "Aden Scarlet's Medical Library" :id "[H] (KN)"}
   {:qty 1 :card "Adûn-Tarîk" :id "[H] (SL)"}
   {:qty 1 :card "Aelinost" :id "[H] (BO)"}
   {:qty 1 :card "Aeluin" :id "[H] (DS)"}
   {:qty 1 :card "Alkarrânda" :id "[H] (BO)"}
   {:qty 1 :card "Alkyad" :id "[H] (CP)"}
   {:qty 1 :card "Alsarias" :id "[H] (DS)"}
   {:qty 1 :card "Amaru" :id "[H] (CA)"}
   {:qty 1 :card "Amon Anlug" :id "[H] (NW)"}
   {:qty 1 :card "Amon Hen" :id "[H] (TW)"}
   {:qty 1 :card "Amon Lhaw" :id "[H] (TI)"}
   {:qty 1 :card "Amon Lind" :id "[H] (FB)"}
   {:qty 1 :card "Amrûn" :id "[H] (SL)"}
   {:qty 1 :card "An Karagmir" :id "[H] (SL)"}
   {:qty 1 :card "Andoloki" :id "[H] (KN)"}
   {:qty 1 :card "Ankruz" :id "[H] (SL)"}
   {:qty 1 :card "Annúminas" :id "[H] (KN)"}
   {:qty 1 :card "Arentaurr" :id "[H] (CP)"}
   {:qty 1 :card "Arhazûn-Tarîk" :id "[H] (DS)"}
   {:qty 1 :card "Arig's Tomb" :id "[H] (DS)"}
   {:qty 1 :card "Arpel" :id "[H] (DS)"}
   {:qty 1 :card "Auz Azunan" :id "[H] (SL)"}
   {:qty 1 :card "Azagarbhun" :id "[H] (CP)"}
   {:qty 1 :card "Bag End" :id "[H] (TW)"}
   {:qty 1 :card "Balchoth Camp" :id "[H] (CP)"}
   {:qty 1 :card "Bandit Lair" :id "[H] (TW)"}
   {:qty 1 :card "Bar Falin" :id "[H] (SL)"}
   {:qty 1 :card "Bar-en-Ibûn" :id "[H] (DF)"}
   {:qty 1 :card "Barad Lughilsarik" :id "[H] (NW)"}
   {:qty 1 :card "Barad Tathren" :id "[H] (TI)"}
   {:qty 1 :card "Barad-dûr" :id "[H] (TW)"}
   {:qty 1 :card "Barad-wath" :id "[H] (NE)"}
   {:qty 1 :card "Barak-shathûr" :id "[H] (DF)"}
   {:qty 1 :card "Barrow-downs" :id "[H] (TW)"}
   {:qty 1 :card "Baruzimabûl" :id "[H] (DS)"}
   {:qty 1 :card "Bâtan-Urîd" :id "[H] (SL)"}
   {:qty 1 :card "Benish Armon" :id "[H] (WR)"}
   {:qty 1 :card "Beorn's House" :id "[H] (TW)"}
   {:qty 1 :card "Bernastath" :id "[H] (NW)"}
   {:qty 1 :card "Blue Mountain Dwarf-hold" :id "[H] (TW)"}
   {:qty 1 :card "Bog Hills" :id "[H] (NW)"}
   {:qty 1 :card "Bozisha-Dar" :id "[H] (SL)"}
   {:qty 1 :card "Brandy Hall" :id "[H] (RS)"}
   {:qty 1 :card "Bree" :id "[H] (TW)"}
   {:qty 1 :card "Buhr Thurasig" :id "[H] (GW)"}
   {:qty 1 :card "Buhr Widu" :id "[H] (TD)"}
   {:qty 1 :card "Bulchyaden Marches" :id "[H] (BO)"}
   {:qty 1 :card "Bûr Esmer" :id "[H] (SL)"}
   {:qty 1 :card "Bywater" :id "[H] (WR)"}
   {:qty 1 :card "Cair Andros" :id "[H] (WR)"}
   {:qty 1 :card "Cairn of the Colruh Hazurbal" :id "[H] (SL)"}
   {:qty 1 :card "Calembel" :id "[H] (WR)"}
   {:qty 1 :card "Cameth Brin" :id "[H] (TW)"}
   {:qty 1 :card "Caras Amarth" :id "[H] (FB)"}
   {:qty 1 :card "Carn Dûm" :id "[H] (TW)"}
   {:qty 1 :card "Cave of the Urdharkonur" :id "[H] (GW)"}
   {:qty 1 :card "Caves of Ûlund" :id "[H] (TW)"}
   {:qty 1 :card "Ceber Fanuin" :id "[H] (FB)"}
   {:qty 1 :card "Celeb-Ost" :id "[H] (GW)"}
   {:qty 1 :card "Celebannon" :id "[H] (FB)"}
   {:qty 1 :card "Cerin Amroth" :id "[H] (FB)"}
   {:qty 1 :card "Charnesra" :id "[H] (SL)"}
   {:qty 1 :card "Chey Goumal" :id "[H] (CP)"}
   {:qty 1 :card "Cirith Gorgor" :id "[H] (AS)"}
   {:qty 1 :card "Cirith Helkaloke" :id "[H] (GW)"}
   {:qty 1 :card "Cirith Ungol" :id "[H] (TW)"}
   {:qty 1 :card "Cirlond" :id "[H] (BO)"}
   {:qty 1 :card "Coastal Signal Tower" :id "[H] (WR)"}
   {:qty 1 :card "Collarmount" :id "[H] (GW)"}
   {:qty 1 :card "Cor Angaladh" :id "[H] (FB)"}
   {:qty 1 :card "Cor Minyadhras" :id "[H] (DS)"}
   {:qty 1 :card "Creb Durga" :id "[H] (KN)"}
   {:qty 1 :card "Daeron's Pool" :id "[H] (GW)"}
   {:qty 1 :card "Dale" :id "[H] (TD)"}
   {:qty 1 :card "Dale of Long Silence" :id "[H] (CP)"}
   {:qty 1 :card "Dancing Spire" :id "[H] (TW)"}
   {:qty 1 :card "Dar Egleriador" :id "[H] (BO)"}
   {:qty 1 :card "Dead Man's Dike" :id "[H] (KN)"}
   {:qty 1 :card "Dead Marshes" :id "[H] (TW)"}
   {:qty 1 :card "Deep Cleft" :id "[H] (GW)"}
   {:qty 1 :card "Dimrill Dale" :id "[H] (TW)"}
   {:qty 1 :card "Dol Amroth" :id "[H] (TW)"}
   {:qty 1 :card "Dol Guldur" :id "[H] (TW)"}
   {:qty 1 :card "Drúadan Forest" :id "[H] (TW)"}
   {:qty 1 :card "Dunharrow" :id "[H] (TW)"}
   {:qty 1 :card "Dunnish Clan-hold" :id "[H] (TW)"}
   {:qty 1 :card "Dûrdamal" :id "[H] (CA)"}
   {:qty 1 :card "Durthang" :id "[H] (NE)"}
   {:qty 1 :card "Dûsalan" :id "[H] (SL)"}
   {:qty 1 :card "Eagles' Eyrie" :id "[H] (TW)"}
   {:qty 1 :card "Easterling Camp" :id "[H] (TW)"}
   {:qty 1 :card "Edoras" :id "[H] (TW)"}
   {:qty 1 :card "Ei Missä" :id "[H] (NW)"}
   {:qty 1 :card "Eithel Morgoth" :id "[H] (ML)"}
   {:qty 1 :card "Eldanar" :id "[H] (KN)"}
   {:qty 1 :card "Elgaer" :id "[H] (CP)"}
   {:qty 1 :card "Elornan Swamp" :id "[H] (DS)"}
   {:qty 1 :card "Elyamû" :id "[H] (BU)"}
   {:qty 1 :card "Emyn Din" :id "[H] (DS)"}
   {:qty 1 :card "Ephydis" :id "[H] (BO)"}
   {:qty 1 :card "Eregost" :id "[H] (DS)"}
   {:qty 1 :card "Ettenmoors" :id "[H] (TW)"}
   {:qty 1 :card "Fhûl" :id "[H] (SL)"}
   {:qty 1 :card "Fortress of Bûramak" :id "[H] (CA)"}
   {:qty 1 :card "Framsburg" :id "[H] (TD)"}
   {:qty 1 :card "Fuinur's Well" :id "[H] (SL)"}
   {:qty 1 :card "Gaurblog Lug" :id "[H] (DF)"}
   {:qty 1 :card "Gaven" :id "[H] (CA)"}
   {:qty 1 :card "Geann a-Lisch" :id "[H] (AS)"}
   {:qty 1 :card "Gesathago's Lair" :id "[H] (CA)"}
   {:qty 1 :card "Giant's Isle" :id "[H] (RS)"}
   {:qty 1 :card "Gladden Fields" :id "[H] (TW)"}
   {:qty 1 :card "Glittering Caves" :id "[H] (TW)"}
   {:qty 1 :card "Gobel Mírlond" :id "[H] (AS)"}
   {:qty 1 :card "Goblin-gate" :id "[H] (TW)"}
   {:qty 1 :card "Gold Hill" :id "[H] (TD)"}
   {:qty 1 :card "Gondmaeglom" :id "[H] (TD)"}
   {:qty 1 :card "Gondring's Lair" :id "[H] (NW)"}
   {:qty 1 :card "Gyogorasag Sanctuary" :id "[H] (GW)"}
   {:qty 1 :card "Hall of Alûva" :id "[H] (DS)"}
   {:qty 1 :card "Hall of Malkôra" :id "[H] (DS)"}
   {:qty 1 :card "Harbâz" :id "[H] (BO)"}
   {:qty 1 :card "Haudh-in-Gwanûr" :id "[H] (DM)"}
   {:qty 1 :card "Hazaj Tollin" :id "[H] (SL)"}
   {:qty 1 :card "Helloth" :id "[H] (NW)"}
   {:qty 1 :card "Helm's Deep" :id "[H] (TI)"}
   {:qty 1 :card "Henneth Annûn" :id "[H] (TW)"}
   {:qty 1 :card "Hermit's Hill" :id "[H] (DM)"}
   {:qty 1 :card "Himring" :id "[H] (TW)"}
   {:qty 1 :card "Hollow Spire" :id "[H] (BU)"}
   {:qty 1 :card "Hostel of the Sisters of Nienna" :id "[H] (KN)"}
   {:qty 1 :card "Hyvät Kalat" :id "[H] (NW)"}
   {:qty 1 :card "Iorag Camp" :id "[H] (CP)"}
   {:qty 1 :card "Irerock" :id "[H] (TW)"}
   {:qty 1 :card "Iron Hill Dwarf-hold" :id "[H] (TW)"}
   {:qty 1 :card "Isengard" :id "[H] (TW)"}
   {:qty 1 :card "Isildur's Tomb" :id "[H] (TI)"}
   {:qty 1 :card "Isle of the Ulond" :id "[H] (TD)"}
   {:qty 1 :card "Isles of the Dead that Live" :id "[H] (TW)"}
   {:qty 1 :card "Isvat" :id "[H] (BO)"}
   {:qty 1 :card "Jääkylät" :id "[H] (NW)"}
   {:qty 1 :card "Joghul's Shrine" :id "[H] (CP)"}
   {:qty 1 :card "Kadar an-Khâradun" :id "[H] (DS)"}
   {:qty 1 :card "Kala Dulakurth" :id "[H] (ML)"}
   {:qty 1 :card "Kala-Ogurk" :id "[H] (BO)"}
   {:qty 1 :card "Kanatrass" :id "[H] (NW)"}
   {:qty 1 :card "Khibil Ephalak" :id "[H] (SL)"}
   {:qty 1 :card "Khorsâj" :id "[H] (SL)"}
   {:qty 1 :card "Killing Fields" :id "[H] (SL)"}
   {:qty 1 :card "Kirnak" :id "[H] (CA)"}
   {:qty 1 :card "Kondu Manara" :id "[H] (DS)"}
   {:qty 1 :card "Korlan" :id "[H] (CA)"}
   {:qty 1 :card "Korlea" :id "[H] (SL)"}
   {:qty 1 :card "Korondaj" :id "[H] (SL)"}
   {:qty 1 :card "Kref Masar" :id "[H] (SL)"}
   {:qty 1 :card "Kylmätalo" :id "[H] (NW)"}
   {:qty 1 :card "Lake-town" :id "[H] (TW)"}
   {:qty 1 :card "Lakes of Kann-Sharmûnda" :id "[H] (CA)"}
   {:qty 1 :card "Lâorkí" :id "[H] (CP)"}
   {:qty 1 :card "Lar-Huz" :id "[H] (CP)"}
   {:qty 1 :card "Laurrë's Manor" :id "[H] (CA)"}
   {:qty 1 :card "Leiri" :id "[H] (NW)"}
   {:qty 1 :card "Lighthouse at Kraken Cape" :id "[H] (SL)"}
   {:qty 1 :card "Ligr Wodaize Berne" :id "[H] (NW)"}
   {:qty 1 :card "Lind-or-Burúm" :id "[H] (TI)"}
   {:qty 1 :card "Linhir" :id "[H] (WR)"}
   {:qty 1 :card "Linnarthurras" :id "[H] (NW)"}
   {:qty 1 :card "Litash" :id "[H] (NW)"}
   {:qty 1 :card "Logath Camp" :id "[H] (CP)"}
   {:qty 1 :card "Lond Anarion" :id "[H] (DS)"}
   {:qty 1 :card "Lond Daer" :id "[H] (KN)"}
   {:qty 1 :card "Lond Galen" :id "[H] (TW)"}
   {:qty 1 :card "Long Marshes" :id "[H] (DF)"}
   {:qty 1 :card "Long Peak" :id "[H] (GW)"}
   {:qty 1 :card "Lossadan Cairn" :id "[H] (TW)"}
   {:qty 1 :card "Lossadan Camp" :id "[H] (TW)"}
   {:qty 1 :card "Lossarnach" :id "[H] (WR)"}
   {:qty 1 :card "Lothragh Camp" :id "[H] (BU)"}
   {:qty 1 :card "Lugarlûr" :id "[H] (NE)"}
   {:qty 1 :card "Lugdruong" :id "[H] (BU)"}
   {:qty 1 :card "Mablad-dûm" :id "[H] (SL)"}
   {:qty 1 :card "Malgolodh's Cave" :id "[H] (NW)"}
   {:qty 1 :card "Maresh" :id "[H] (SL)"}
   {:qty 1 :card "Mathlaburg" :id "[H] (NE)"}
   {:qty 1 :card "Medlóshad" :id "[H] (CP)"}
   {:qty 1 :card "Michel Delving" :id "[H] (RS)"}
   {:qty 1 :card "Minas Durlith" :id "[H] (NE)"}
   {:qty 1 :card "Minas Morgul" :id "[H] (TW)"}
   {:qty 1 :card "Minas Tirith" :id "[H] (TW)"}
   {:qty 1 :card "Mirror Halls" :id "[H] (BU)"}
   {:qty 1 :card "Míspir" :id "[H] (DS)"}
   {:qty 1 :card "Mistrand" :id "[H] (NE)"}
   {:qty 1 :card "Monastery of the True Faith" :id "[H] (SL)"}
   {:qty 1 :card "Moria" :id "[H] (TW)"}
   {:qty 1 :card "Morkai" :id "[H] (KN)"}
   {:qty 1 :card "Mornost" :id "[H] (NW)"}
   {:qty 1 :card "Mount Arysis" :id "[H] (SL)"}
   {:qty 1 :card "Mount Doom" :id "[H] (TW)"}
   {:qty 1 :card "Mount Gram" :id "[H] (TW)"}
   {:qty 1 :card "Mount Gundabad" :id "[H] (TW)"}
   {:qty 1 :card "Mount Rerir" :id "[H] (FB)"}
   {:qty 1 :card "Mountains of Mirkwood" :id "[H] (NE)"}
   {:qty 1 :card "Mûmakil Cemetery" :id "[H] (SL)"}
   {:qty 1 :card "Naerphys" :id "[H] (BO)"}
   {:qty 1 :card "Nâlashatûr" :id "[H] (BO)"}
   {:qty 1 :card "Nan Morsereg" :id "[H] (NE)"}
   {:qty 1 :card "Nárad-dûm" :id "[H] (SL)"}
   {:qty 1 :card "Narik-Zadan" :id "[H] (SL)"}
   {:qty 1 :card "Nennûrad" :id "[H] (CP)"}
   {:qty 1 :card "Nevazar's Tomb" :id "[H] (BO)"}
   {:qty 1 :card "Nioca Manara" :id "[H] (DS)"}
   {:qty 1 :card "Nîlûlondê" :id "[H] (SL)"}
   {:qty 1 :card "Norjadar" :id "[H] (SL)"}
   {:qty 1 :card "Norr-dûm" :id "[H] (GW)"}
   {:qty 1 :card "Nûlakad" :id "[H] (CA)"}
   {:qty 1 :card "Númenórean Tomb" :id "[H] (BU)"}
   {:qty 1 :card "Nûrniag Camp" :id "[H] (AS)"}
   {:qty 1 :card "Nurunkhizdín" :id "[H] (DF)"}
   {:qty 1 :card "Ny Chennacatt" :id "[H] (SL)"}
   {:qty 1 :card "Oasis of Fult" :id "[H] (SL)"}
   ;{:qty 1 :card "Old Forest" :id "[H] (TW)"}
   {:qty 1 :card "Olyavud" :id "[H] (BU)"}
   {:qty 1 :card "Oraishapek's Mound" :id "[H] (NE)"}
   {:qty 1 :card "Orod Certhas" :id "[H] (NW)"}
   {:qty 1 :card "Osgiliath" :id "[H] (WR)"}
   {:qty 1 :card "Ost Angthoronion" :id "[H] (BO)"}
   {:qty 1 :card "Ost-in-Edhil" :id "[H] (TW)"}
   {:qty 1 :card "Ostelor" :id "[H] (DS)"}
   {:qty 1 :card "Ostigurth" :id "[H] (NE)"}
   {:qty 1 :card "Ovir Hollow" :id "[H] (TD)"}
   {:qty 1 :card "Pelargir" :id "[H] (TW)"}
   {:qty 1 :card "Pelepelplû" :id "[H] (BO)"}
   {:qty 1 :card "Pelican Islands" :id "[H] (SL)"}
   {:qty 1 :card "Pendrath na-Udûn" :id "[H] (ML)"}
   {:qty 1 :card "Pharabâs" :id "[H] (CA)"}
   {:qty 1 :card "Pieni Satama" :id "[H] (NW)"}
   {:qty 1 :card "Poison Rock" :id "[H] (BO)"}
   {:qty 1 :card "Pred" :id "[H] (SL)"}
   {:qty 1 :card "Puolihmisten Satama" :id "[H] (NW)"}
   {:qty 1 :card "Quarries of Nosharud" :id "[H] (BO)"}
   {:qty 1 :card "Raider-hold" :id "[H] (AS)"}
   {:qty 1 :card "Ramôrth" :id "[H] (CA)"}
   {:qty 1 :card "Relerindú" :id "[H] (CP)"}
   {:qty 1 :card "Rhosgobel" :id "[H] (TW)"}
   {:qty 1 :card "Riavod" :id "[H] (CP)"}
   {:qty 1 :card "Rilgul" :id "[H] (CA)"}
   {:qty 1 :card "Rînaghnâti" :id "[H] (BO)"}
   {:qty 1 :card "Ró-molló" :id "[H] (DS)"}
   {:qty 1 :card "Ruined Signal Tower" :id "[H] (TW)"}
   {:qty 1 :card "Ruins of Anaoshak" :id "[H] (BO)"}
   {:qty 1 :card "Ruskea Vene" :id "[H] (NW)"}
   {:qty 1 :card "Sackville" :id "[H] (RS)"}
   {:qty 1 :card "Samarth" :id "[H] (BO)"}
   {:qty 1 :card "Sarn Goriwing" :id "[H] (TW)"}
   {:qty 1 :card "Sarûl" :id "[H] (CA)"}
   {:qty 1 :card "Seregul's Keep" :id "[H] (DS)"}
   {:qty 1 :card "Setmaenen" :id "[H] (WR)"}
   {:qty 1 :card "Shab Arch" :id "[H] (GW)"}
   {:qty 1 :card "Shapôl Udûn" :id "[H] (BU)"}
   {:qty 1 :card "Shelob's Lair" :id "[H] (TW)"}
   {:qty 1 :card "Shoreless Isles" :id "[H] (NW)"}
   {:qty 1 :card "Shrel-Kain" :id "[H] (TW)"}
   {:qty 1 :card "Sonondor's Mound" :id "[H] (GW)"}
   {:qty 1 :card "Southron Oasis" :id "[H] (TW)"}
   {:qty 1 :card "Steel Fall" :id "[H] (GW)"}
   {:qty 1 :card "Stone-circle" :id "[H] (TW)"}
   {:qty 1 :card "Strayhold" :id "[H] (NE)"}
   {:qty 1 :card "Sturlurtsa" :id "[H] (CP)"}
   {:qty 1 :card "Sud Sicanna" :id "[H] (SL)"}
   {:qty 1 :card "Sudu Cull" :id "[H] (SL)"}
   {:qty 1 :card "Tanith" :id "[H] (CA)"}
   {:qty 1 :card "Târik an-Aruwânâi" :id "[H] (BO)"}
   {:qty 1 :card "Târik an-Bawîba" :id "[H] (BO)"}
   {:qty 1 :card "Tartaust" :id "[H] (SL)"}
   {:qty 1 :card "Tarû-Makar" :id "[H] (CA)"}
   {:qty 1 :card "Telpëmar" :id "[H] (FB)"}
   {:qty 1 :card "Temple of Kondri Odchi" :id "[H] (CP)"}
   {:qty 1 :card "Temple of Lokuthor" :id "[H] (NE)"}
   {:qty 1 :card "Tenolkachyn" :id "[H] (BO)"}
   {:qty 1 :card "Tharagrondost" :id "[H] (WR)"}
   {:qty 1 :card "Tharbad" :id "[H] (TD)"}
   {:qty 1 :card "Thaurung" :id "[H] (NW)"}
   {:qty 1 :card "The Carrock" :id "[H] (DF)"}
   {:qty 1 :card "The Gem-deeps" :id "[H] (DM)"}
   {:qty 1 :card "The Great Oasis" :id "[H] (SL)"}
   {:qty 1 :card "The Hospice of Lost Faith" :id "[H] (SL)"}
   {:qty 1 :card "The Iron-deeps" :id "[H] (DM)"}
   {:qty 1 :card "The Last Bridge" :id "[H] (RS)"}
   {:qty 1 :card "The Lonely Mountain" :id "[H] (TW)"}
   {:qty 1 :card "The Pits of Angband" :id "[H] (ML)"}
   {:qty 1 :card "The Pûkel-deeps" :id "[H] (DM)"}
   {:qty 1 :card "The Riddle Caves" :id "[H] (KN)"}
   {:qty 1 :card "The Stones" :id "[H] (TW)"}
   {:qty 1 :card "The Sulfur-deeps" :id "[H] (DM)"}
   {:qty 1 :card "The Tower of Birds" :id "[H] (SL)"}
   {:qty 1 :card "The Under-caves" :id "[H] (KN)"}
   {:qty 1 :card "The Under-courts" :id "[H] (DM)"}
   {:qty 1 :card "The Under-forges" :id "[H] (ML)"}
   {:qty 1 :card "The Under-galleries" :id "[H] (DM)"}
   {:qty 1 :card "The Under-gates" :id "[H] (DM)"}
   {:qty 1 :card "The Under-grottos" :id "[H] (DM)"}
   {:qty 1 :card "The Under-leas" :id "[H] (DM)"}
   {:qty 1 :card "The Under-vaults" :id "[H] (DM)"}
   {:qty 1 :card "The Vaults of Utûmno" :id "[H] (BU)"}
   {:qty 1 :card "The White Towers" :id "[H] (TW)"}
   {:qty 1 :card "The Willow Dingle" :id "[H] (RS)"}
   {:qty 1 :card "The Wind Throne" :id "[H] (TW)"}
   {:qty 1 :card "The Worthy Hills" :id "[H] (AS)"}
   {:qty 1 :card "Thilgon's Tomb" :id "[H] (NW)"}
   {:qty 1 :card "Thôrion" :id "[H] (CA)"}
   {:qty 1 :card "Thraath" :id "[H] (CP)"}
   {:qty 1 :card "Thranduil's Halls" :id "[H] (TW)"}
   {:qty 1 :card "Three Sisters" :id "[H] (DS)"}
   {:qty 1 :card "Thunder Cleft" :id "[H] (GW)"}
   {:qty 1 :card "Thuringwathost" :id "[H] (NE)"}
   {:qty 1 :card "Tol Buruth" :id "[H] (CP)"}
   {:qty 1 :card "Tol Fuin" :id "[H] (ML)"}
   {:qty 1 :card "Tol Glingal" :id "[H] (SL)"}
   {:qty 1 :card "Tol Lamfirith" :id "[H] (KN)"}
   {:qty 1 :card "Tol Morwen" :id "[H] (ML)"}
   {:qty 1 :card "Tol Ringurthur" :id "[H] (BU)"}
   {:qty 1 :card "Tol Turgul" :id "[H] (DS)"}
   {:qty 1 :card "Tol Uialgaer" :id "[H] (SL)"}
   {:qty 1 :card "Tolfalas" :id "[H] (TW)"}
   {:qty 1 :card "Tom's House" :id "[H] (FB)"}
   {:qty 1 :card "Tomb-fields of Makaburini" :id "[H] (DS)"}
   {:qty 1 :card "Tombs of Oran-Plaar" :id "[H] (CA)"}
   {:qty 1 :card "Tower of Hargrog" :id "[H] (DS)"}
   {:qty 1 :card "Tower of the Wolf-friend" :id "[H] (GW)"}
   {:qty 1 :card "Traith Chefudoc" :id "[H] (KN)"}
   {:qty 1 :card "Tresti" :id "[H] (SL)"}
   {:qty 1 :card "Tuckburrow" :id "[H] (RS)"}
   {:qty 1 :card "Tûl Harar" :id "[H] (BO)"}
   {:qty 1 :card "Tûl Isra" :id "[H] (SL)"}
   {:qty 1 :card "Tûl Póac" :id "[H] (SL)"}
   {:qty 1 :card "Turukulon's Lair" :id "[H] (TI)"}
   {:qty 1 :card "Ty-ar-Rana" :id "[H] (CA)"}
   {:qty 1 :card "Ulk Chey Sart" :id "[H] (CP)"}
   {:qty 1 :card "Ulk Jey Ama" :id "[H] (CP)"}
   {:qty 1 :card "Umbar" :id "[H] (WR)"}
   {:qty 1 :card "Úrcheldor" :id "[H] (BU)"}
   {:qty 1 :card "Urdic Camp" :id "[H] (NW)"}
   {:qty 1 :card "Ûrêzâyan" :id "[H] (SL)"}
   {:qty 1 :card "Urlurtsu Nurn" :id "[H] (DM)"}
   {:qty 1 :card "Urud-an-Khibil" :id "[H] (SL)"}
   {:qty 1 :card "Vale of Erech" :id "[H] (TW)"}
   {:qty 1 :card "Valley of Tombs" :id "[H] (BO)"}
   {:qty 1 :card "Vamag" :id "[H] (WR)"}
   {:qty 1 :card "Variag Camp" :id "[H] (TW)"}
   {:qty 1 :card "Vasaran Ahjo" :id "[H] (NW)"}
   {:qty 1 :card "Ventazvah Ôran-tal" :id "[H] (BO)"}
   {:qty 1 :card "Vog Mur" :id "[H] (CA)"}
   {:qty 1 :card "Wain-Easterling Camp" :id "[H] (CP)"}
   {:qty 1 :card "Watch at Unulló" :id "[H] (DS)"}
   {:qty 1 :card "Weathertop" :id "[H] (TW)"}
   {:qty 1 :card "Wellinghall" :id "[H] (TW)"}
   {:qty 1 :card "Woodmen-town" :id "[H] (TW)"}
   {:qty 1 :card "Wose Passage-hold" :id "[H] (TW)"}
   {:qty 1 :card "Xyunaï" :id "[H] (BO)"}
   {:qty 1 :card "Yatali Kîrgan" :id "[H] (BO)"}
   {:qty 1 :card "Yjuvït" :id "[H] (BU)"}
   {:qty 1 :card "Zadan an-Adûn" :id "[H] (DS)"}
   {:qty 1 :card "Zarak Dûm" :id "[H] (TD)"}
   {:qty 1 :card "Zayandaur" :id "[H] (BU)"}])

(def dreamcard-wizard-havens
  [{:qty 4 :card "Edhellond" :id "[H] (TW)"}
   {:qty 4 :card "Elanthia" :id "[H] (CP)"}
   {:qty 4 :card "Evermist" :id "[H] (NW)"}
   {:qty 4 :card "Grey Havens" :id "[H] (TW)"}
   {:qty 4 :card "Hau Nysrin" :id "[H] (SL)"}
   {:qty 4 :card "Inyalonî" :id "[H] (BO)"}
   {:qty 4 :card "Lórien" :id "[H] (TW)"}
   {:qty 4 :card "Rhûbar" :id "[H] (FB)"}
   {:qty 4 :card "Rivendell" :id "[H] (TW)"}
   {:qty 4 :card "Taurondë" :id "[H] (CA)"}
   {:qty 4 :card "Valagalen" :id "[H] (DS)"}])

(def dreamcard-wizard-haven
  [{:qty 1 :card "Edhellond" :id "[H] (TW)"}
   {:qty 1 :card "Elanthia" :id "[H] (CP)"}
   {:qty 1 :card "Evermist" :id "[H] (NW)"}
   {:qty 1 :card "Grey Havens" :id "[H] (TW)"}
   {:qty 1 :card "Hau Nysrin" :id "[H] (SL)"}
   {:qty 1 :card "Inyalonî" :id "[H] (BO)"}
   {:qty 1 :card "Lórien" :id "[H] (TW)"}
   {:qty 1 :card "Rhûbar" :id "[H] (FB)"}
   {:qty 1 :card "Rivendell" :id "[H] (TW)"}
   {:qty 1 :card "Taurondë" :id "[H] (CA)"}
   {:qty 1 :card "Valagalen" :id "[H] (DS)"}])

(def dreamcard-minion-lord ;;free/border/ruin
  [{:qty 1 :card "Abandoned Caravansary" :id "[M] (SL)"}
   {:qty 1 :card "Achrond" :id "[M] (NW)"}
   {:qty 1 :card "Adan Tomb" :id "[M] (CP)"}
   {:qty 1 :card "Aden Scarlet's Medical Library" :id "[M] (KN)"}
   {:qty 1 :card "Aelinost" :id "[M] (BO)"}
   {:qty 1 :card "Aeluin" :id "[M] (DS)"}
   {:qty 1 :card "Alkarrânda" :id "[M] (BO)"}
   {:qty 1 :card "Alkyad" :id "[M] (CP)"}
   {:qty 1 :card "Alsarias" :id "[M] (DS)"}
   {:qty 1 :card "Amon Anlug" :id "[M] (NW)"}
   {:qty 1 :card "Amon Hen" :id "[M] (LE)"}
   {:qty 1 :card "Amon Lhaw" :id "[M] (TI)"}
   {:qty 1 :card "Amon Lind" :id "[M] (FB)"}
   {:qty 1 :card "An Karagmir" :id "[M] (SL)"}
   {:qty 1 :card "Andoloki" :id "[M] (KN)"}
   {:qty 1 :card "Ankruz" :id "[M] (SL)"}
   {:qty 1 :card "Annúminas" :id "[M] (KN)"}
   {:qty 1 :card "Arentaurr" :id "[M] (CP)"}
   {:qty 1 :card "Arhazûn-Tarîk" :id "[M] (DS)"}
   {:qty 1 :card "Arig's Tomb" :id "[M] (DS)"}
   {:qty 1 :card "Arpel" :id "[M] (DS)"}
   {:qty 1 :card "Auz Azunan" :id "[M] (SL)"}
   {:qty 1 :card "Azagarbhun" :id "[M] (CP)"}
   {:qty 1 :card "Bag End" :id "[M] (LE)"}
   {:qty 1 :card "Bandit Lair" :id "[M] (LE)"}
   {:qty 1 :card "Bar Falin" :id "[M] (SL)"}
   {:qty 1 :card "Bar-en-Ibûn" :id "[M] (DF)"}
   {:qty 1 :card "Barad Tathren" :id "[M] (TI)"}
   {:qty 1 :card "Barak-shathûr" :id "[M] (DF)"}
   {:qty 1 :card "Barrow-downs" :id "[M] (LE)"}
   {:qty 1 :card "Baruzimabûl" :id "[M] (DS)"}
   {:qty 1 :card "Benish Armon" :id "[M] (WR)"}
   {:qty 1 :card "Beorn's House" :id "[M] (LE)"}
   {:qty 1 :card "Bernastath" :id "[M] (NW)"}
   {:qty 1 :card "Blue Mountain Dwarf-hold" :id "[M] (LE)"}
   {:qty 1 :card "Bog Hills" :id "[M] (NW)"}
   {:qty 1 :card "Brandy Hall" :id "[M] (RS)"}
   {:qty 1 :card "Bree" :id "[M] (LE)"}
   {:qty 1 :card "Buhr Thurasig" :id "[M] (GW)"}
   {:qty 1 :card "Buhr Widu" :id "[M] (LE)"}
   {:qty 1 :card "Bulchyaden Marches" :id "[M] (BO)"}
   {:qty 1 :card "Bywater" :id "[M] (WR)"}
   {:qty 1 :card "Cair Andros" :id "[M] (WR)"}
   {:qty 1 :card "Cairn of the Colruh Hazurbal" :id "[M] (SL)"}
   {:qty 1 :card "Calembel" :id "[M] (WR)"}
   {:qty 1 :card "Cameth Brin" :id "[M] (LE)"}
   {:qty 1 :card "Caras Amarth" :id "[M] (FB)"}
   {:qty 1 :card "Cave of the Urdharkonur" :id "[M] (GW)"}
   {:qty 1 :card "Caves of Ûlund" :id "[M] (LE)"}
   {:qty 1 :card "Ceber Fanuin" :id "[M] (FB)"}
   {:qty 1 :card "Celeb-Ost" :id "[M] (GW)"}
   {:qty 1 :card "Celebannon" :id "[M] (FB)"}
   {:qty 1 :card "Cerin Amroth" :id "[M] (FB)"}
   {:qty 1 :card "Charnesra" :id "[M] (SL)"}
   {:qty 1 :card "Cirith Helkaloke" :id "[M] (GW)"}
   {:qty 1 :card "Cirlond" :id "[M] (BO)"}
   {:qty 1 :card "Coastal Signal Tower" :id "[M] (WR)"}
   {:qty 1 :card "Collarmount" :id "[M] (GW)"}
   {:qty 1 :card "Cor Angaladh" :id "[M] (FB)"}
   {:qty 1 :card "Cor Minyadhras" :id "[M] (DS)"}
   {:qty 1 :card "Daeron's Pool" :id "[M] (GW)"}
   {:qty 1 :card "Dale" :id "[M] (LE)"}
   {:qty 1 :card "Dale of Long Silence" :id "[M] (CP)"}
   {:qty 1 :card "Dancing Spire" :id "[M] (AS)"}
   {:qty 1 :card "Dar Egleriador" :id "[M] (BO)"}
   {:qty 1 :card "Dead Man's Dike" :id "[M] (KN)"}
   {:qty 1 :card "Dimrill Dale" :id "[M] (LE)"}
   {:qty 1 :card "Dol Amroth" :id "[M] (LE)"}
   {:qty 1 :card "Drúadan Forest" :id "[M] (LE)"}
   {:qty 1 :card "Dunharrow" :id "[M] (LE)"}
   {:qty 1 :card "Dunnish Clan-hold" :id "[M] (LE)"}
   {:qty 1 :card "Dûrdamal" :id "[M] (CA)"}
   {:qty 1 :card "Eagles' Eyrie" :id "[M] (AS)"}
   {:qty 1 :card "Easterling Camp" :id "[M] (LE)"}
   {:qty 1 :card "Edhellond" :id "[M] (AS)"}
   {:qty 1 :card "Edoras" :id "[M] (LE)"}
   {:qty 1 :card "Ei Missä" :id "[M] (NW)"}
   {:qty 1 :card "Elanthia" :id "[M] (CP)"}
   {:qty 1 :card "Eldanar" :id "[M] (KN)"}
   {:qty 1 :card "Elgaer" :id "[M] (CP)"}
   {:qty 1 :card "Elornan Swamp" :id "[M] (DS)"}
   {:qty 1 :card "Elyamû" :id "[M] (BU)"}
   {:qty 1 :card "Emyn Din" :id "[M] (DS)"}
   {:qty 1 :card "Ephydis" :id "[M] (BO)"}
   {:qty 1 :card "Ettenmoors" :id "[M] (LE)"}
   {:qty 1 :card "Fortress of Bûramak" :id "[M] (CA)"}
   {:qty 1 :card "Framsburg" :id "[M] (AS)"}
   {:qty 1 :card "Gaven" :id "[M] (CA)"}
   {:qty 1 :card "Gesathago's Lair" :id "[M] (CA)"}
   {:qty 1 :card "Giant's Isle" :id "[M] (RS)"}
   {:qty 1 :card "Gladden Fields" :id "[M] (LE)"}
   {:qty 1 :card "Glittering Caves" :id "[M] (LE)"}
   {:qty 1 :card "Gobel Mírlond" :id "[M] (LE)"}
   {:qty 1 :card "Gold Hill" :id "[M] (AS)"}
   {:qty 1 :card "Gondmaeglom" :id "[M] (LE)"}
   {:qty 1 :card "Gondring's Lair" :id "[M] (NW)"}
   {:qty 1 :card "Grey Havens" :id "[M] (AS)"}
   {:qty 1 :card "Gyogorasag Sanctuary" :id "[M] (GW)"}
   {:qty 1 :card "Hall of Alûva" :id "[M] (DS)"}
   {:qty 1 :card "Harbâz" :id "[M] (BO)"}
   {:qty 1 :card "Haudh-in-Gwanûr" :id "[M] (LE)"}
   {:qty 1 :card "Hazaj Tollin" :id "[M] (SL)"}
   {:qty 1 :card "Helloth" :id "[M] (NW)"}
   {:qty 1 :card "Helm's Deep" :id "[M] (TI)"}
   {:qty 1 :card "Henneth Annûn" :id "[M] (LE)"}
   {:qty 1 :card "Hermit's Hill" :id "[M] (LE)"}
   {:qty 1 :card "Himring" :id "[M] (AS)"}
   {:qty 1 :card "Hollow Spire" :id "[M] (BU)"}
   {:qty 1 :card "Hostel of the Sisters of Nienna" :id "[M] (KN)"}
   {:qty 1 :card "Hyvät Kalat" :id "[M] (NW)"}
   {:qty 1 :card "Inyalonî" :id "[M] (BO)"}
   {:qty 1 :card "Iorag Camp" :id "[M] (CP)"}
   {:qty 1 :card "Irerock" :id "[M] (AS)"}
   {:qty 1 :card "Iron Hill Dwarf-hold" :id "[M] (LE)"}
   {:qty 1 :card "Isengard" :id "[M] (LE)"}
   {:qty 1 :card "Isildur's Tomb" :id "[M] (TI)"}
   {:qty 1 :card "Isle of the Ulond" :id "[M] (AS)"}
   {:qty 1 :card "Isles of the Dead That Live" :id "[M] (AS)"}
   {:qty 1 :card "Isvat" :id "[M] (BO)"}
   {:qty 1 :card "Jääkylät" :id "[M] (NW)"}
   {:qty 1 :card "Joghul's Shrine" :id "[M] (CP)"}
   {:qty 1 :card "Kadar an-Khâradun" :id "[M] (DS)"}
   {:qty 1 :card "Kanatrass" :id "[M] (NW)"}
   {:qty 1 :card "Khibil Ephalak" :id "[M] (SL)"}
   {:qty 1 :card "Khorsâj" :id "[M] (SL)"}
   {:qty 1 :card "Killing Fields" :id "[M] (SL)"}
   {:qty 1 :card "Kirnak" :id "[M] (CA)"}
   {:qty 1 :card "Korlan" :id "[M] (CA)"}
   {:qty 1 :card "Korlea" :id "[M] (SL)"}
   {:qty 1 :card "Korondaj" :id "[M] (SL)"}
   {:qty 1 :card "Kref Masar" :id "[M] (SL)"}
   {:qty 1 :card "Kylmätalo" :id "[M] (NW)"}
   {:qty 1 :card "Lake-town" :id "[M] (LE)"}
   {:qty 1 :card "Lakes of Kann-Sharmûnda" :id "[M] (CA)"}
   {:qty 1 :card "Lar-Huz" :id "[M] (CP)"}
   {:qty 1 :card "Laurrë's Manor" :id "[M] (CA)"}
   {:qty 1 :card "Leiri" :id "[M] (NW)"}
   {:qty 1 :card "Lighthouse at Kraken Cape" :id "[M] (SL)"}
   {:qty 1 :card "Ligr Wodaize Berne" :id "[M] (NW)"}
   {:qty 1 :card "Lind-or-Burúm" :id "[M] (TI)"}
   {:qty 1 :card "Linhir" :id "[M] (WR)"}
   {:qty 1 :card "Linnarthurras" :id "[M] (NW)"}
   {:qty 1 :card "Logath Camp" :id "[M] (CP)"}
   {:qty 1 :card "Lond Anarion" :id "[M] (DS)"}
   {:qty 1 :card "Lond Daer" :id "[M] (KN)"}
   {:qty 1 :card "Lond Galen" :id "[M] (LE)"}
   {:qty 1 :card "Long Marshes" :id "[M] (DF)"}
   {:qty 1 :card "Long Peak" :id "[M] (GW)"}
   {:qty 1 :card "Lórien" :id "[M] (AS)"}
   {:qty 1 :card "Lossadan Cairn" :id "[M] (LE)"}
   {:qty 1 :card "Lossadan Camp" :id "[M] (LE)"}
   {:qty 1 :card "Lossarnach" :id "[M] (WR)"}
   {:qty 1 :card "Lothragh Camp" :id "[M] (BU)"}
   {:qty 1 :card "Mablad-dûm" :id "[M] (SL)"}
   {:qty 1 :card "Malgolodh's Cave" :id "[M] (NW)"}
   {:qty 1 :card "Maresh" :id "[M] (SL)"}
   {:qty 1 :card "Mathlaburg" :id "[M] (NE)"}
   {:qty 1 :card "Medlóshad" :id "[M] (CP)"}
   {:qty 1 :card "Michel Delving" :id "[M] (RS)"}
   {:qty 1 :card "Minas Tirith" :id "[M] (LE)"}
   {:qty 1 :card "Mirror Halls" :id "[M] (BU)"}
   {:qty 1 :card "Míspir" :id "[M] (DS)"}
   {:qty 1 :card "Monastery of the True Faith" :id "[M] (SL)"}
   {:qty 1 :card "Mountains of Mirkwood" :id "[M] (NE)"}
   {:qty 1 :card "Mûmakil Cemetery" :id "[M] (SL)"}
   {:qty 1 :card "Nâlashatûr" :id "[M] (BO)"}
   {:qty 1 :card "Nan Morsereg" :id "[M] (NE)"}
   {:qty 1 :card "Nárad-dûm" :id "[M] (SL)"}
   {:qty 1 :card "Narik-Zadan" :id "[M] (SL)"}
   {:qty 1 :card "Nennûrad" :id "[M] (CP)"}
   {:qty 1 :card "Nevazar's Tomb" :id "[M] (BO)"}
   {:qty 1 :card "Nioca Manara" :id "[M] (DS)"}
   {:qty 1 :card "Nîlûlondê" :id "[M] (SL)"}
   {:qty 1 :card "Norjadar" :id "[M] (SL)"}
   {:qty 1 :card "Norr-dûm" :id "[M] (GW)"}
   {:qty 1 :card "Nûlakad" :id "[M] (CA)"}
   {:qty 1 :card "Númenórean Tomb" :id "[M] (BU)"}
   {:qty 1 :card "Nurunkhizdín" :id "[M] (DF)"}
   {:qty 1 :card "Oasis of Fult" :id "[M] (SL)"}
   ;{:qty 1 :card "Old Forest" :id "[M] (AS)"}
   {:qty 1 :card "Olyavud" :id "[M] (BU)"}
   {:qty 1 :card "Oraishapek's Mound" :id "[M] (NE)"}
   {:qty 1 :card "Orod Certhas" :id "[M] (NW)"}
   {:qty 1 :card "Osgiliath" :id "[M] (WR)"}
   {:qty 1 :card "Ost Angthoronion" :id "[M] (BO)"}
   {:qty 1 :card "Ost-in-Edhil" :id "[M] (LE)"}
   {:qty 1 :card "Ostelor" :id "[M] (DS)"}
   {:qty 1 :card "Ostigurth" :id "[M] (NE)"}
   {:qty 1 :card "Ovir Hollow" :id "[M] (AS)"}
   {:qty 1 :card "Pelargir" :id "[M] (LE)"}
   {:qty 1 :card "Pelepelplû" :id "[M] (BO)"}
   {:qty 1 :card "Pelican Islands" :id "[M] (SL)"}
   {:qty 1 :card "Pharabâs" :id "[M] (CA)"}
   {:qty 1 :card "Pieni Satama" :id "[M] (NW)"}
   {:qty 1 :card "Poison Rock" :id "[M] (BO)"}
   {:qty 1 :card "Pred" :id "[M] (SL)"}
   {:qty 1 :card "Puolihmisten Satama" :id "[M] (NW)"}
   {:qty 1 :card "Quarries of Nosharud" :id "[M] (BO)"}
   {:qty 1 :card "Raider-hold" :id "[M] (LE)"}
   {:qty 1 :card "Ramôrth" :id "[M] (CA)"}
   {:qty 1 :card "Relerindú" :id "[M] (CP)"}
   {:qty 1 :card "Resting-Isle of Amandil" :id "[M] (TD)"}
   {:qty 1 :card "Rhosgobel" :id "[M] (AS)"}
   {:qty 1 :card "Rhûbar" :id "[M] (FB)"}
   {:qty 1 :card "Riavod" :id "[M] (CP)"}
   {:qty 1 :card "Rilgul" :id "[M] (CA)"}
   {:qty 1 :card "Rînaghnâti" :id "[M] (BO)"}
   {:qty 1 :card "Rivendell" :id "[M] (AS)"}
   {:qty 1 :card "Ró-molló" :id "[M] (DS)"}
   {:qty 1 :card "Ruined Signal Tower" :id "[M] (LE)"}
   {:qty 1 :card "Ruins of Anaoshak" :id "[M] (BO)"}
   {:qty 1 :card "Ruskea Vene" :id "[M] (NW)"}
   {:qty 1 :card "Sackville" :id "[M] (RS)"}
   {:qty 1 :card "Samarth" :id "[M] (BO)"}
   {:qty 1 :card "Sarûl" :id "[M] (CA)"}
   {:qty 1 :card "Seregul's Keep" :id "[M] (DS)"}
   {:qty 1 :card "Setmaenen" :id "[M] (WR)"}
   {:qty 1 :card "Shab Arch" :id "[M] (GW)"}
   {:qty 1 :card "Shoreless Isles" :id "[M] (NW)"}
   {:qty 1 :card "Shrel-Kain" :id "[M] (LE)"}
   {:qty 1 :card "Sonondor's Mound" :id "[M] (GW)"}
   {:qty 1 :card "Southron Oasis" :id "[M] (LE)"}
   {:qty 1 :card "Steel Fall" :id "[M] (GW)"}
   {:qty 1 :card "Stone-circle" :id "[M] (LE)"}
   {:qty 1 :card "Strayhold" :id "[M] (NE)"}
   {:qty 1 :card "Sud Sicanna" :id "[M] (SL)"}
   {:qty 1 :card "Sudu Cull" :id "[M] (SL)"}
   {:qty 1 :card "Tanith" :id "[M] (CA)"}
   {:qty 1 :card "Târik an-Bawîba" :id "[M] (BO)"}
   {:qty 1 :card "Tartaust" :id "[M] (SL)"}
   {:qty 1 :card "Tarû-Makar" :id "[M] (CA)"}
   {:qty 1 :card "Taurondë" :id "[M] (CA)"}
   {:qty 1 :card "Telpëmar" :id "[M] (FB)"}
   {:qty 1 :card "Temple of Kondri Odchi" :id "[M] (CP)"}
   {:qty 1 :card "Temple of Lokuthor" :id "[M] (NE)"}
   {:qty 1 :card "Tenolkachyn" :id "[M] (BO)"}
   {:qty 1 :card "Tharagrondost" :id "[M] (WR)"}
   {:qty 1 :card "Tharbad" :id "[M] (LE)"}
   {:qty 1 :card "Thaurung" :id "[M] (NW)"}
   {:qty 1 :card "The Carrock" :id "[M] (DF)"}
   {:qty 1 :card "The Gem-deeps" :id "[M] (AS)"}
   {:qty 1 :card "The Great Oasis" :id "[M] (SL)"}
   {:qty 1 :card "The Hospice of Lost Faith" :id "[M] (SL)"}
   {:qty 1 :card "The Last Bridge" :id "[M] (RS)"}
   {:qty 1 :card "The Lonely Mountain" :id "[M] (LE)"}
   {:qty 1 :card "The Pûkel-deeps" :id "[M] (AS)"}
   {:qty 1 :card "The Riddle Caves" :id "[M] (KN)"}
   {:qty 1 :card "The Stones" :id "[M] (LE)"}
   {:qty 1 :card "The Tower of Birds" :id "[M] (SL)"}
   {:qty 1 :card "The Under-grottos" :id "[M] (AS)"}
   {:qty 1 :card "The Under-vaults" :id "[M] (AS)"}
   {:qty 1 :card "The White Towers" :id "[M] (LE)"}
   {:qty 1 :card "The Willow Dingle" :id "[M] (RS)"}
   {:qty 1 :card "The Wind Throne" :id "[M] (LE)"}
   {:qty 1 :card "The Worthy Hills" :id "[M] (LE)"}
   {:qty 1 :card "Thilgon's Tomb" :id "[M] (NW)"}
   {:qty 1 :card "Thôrion" :id "[M] (CA)"}
   {:qty 1 :card "Thraath" :id "[M] (CP)"}
   {:qty 1 :card "Thranduil's Halls" :id "[M] (LE)"}
   {:qty 1 :card "Three Sisters" :id "[M] (DS)"}
   {:qty 1 :card "Thunder Cleft" :id "[M] (GW)"}
   {:qty 1 :card "Tol Buruth" :id "[M] (CP)"}
   {:qty 1 :card "Tol Fuin" :id "[M] (ML)"}
   {:qty 1 :card "Tol Glingal" :id "[M] (SL)"}
   {:qty 1 :card "Tol Lamfirith" :id "[M] (KN)"}
   {:qty 1 :card "Tol Morwen" :id "[M] (ML)"}
   {:qty 1 :card "Tol Ringurthur" :id "[M] (BU)"}
   {:qty 1 :card "Tol Turgul" :id "[M] (DS)"}
   {:qty 1 :card "Tol Uialgaer" :id "[M] (SL)"}
   {:qty 1 :card "Tolfalas" :id "[M] (AS)"}
   {:qty 1 :card "Tom's House" :id "[M] (FB)"}
   {:qty 1 :card "Tomb-fields of Makaburini" :id "[M] (DS)"}
   {:qty 1 :card "Tombs of Oran-Plaar" :id "[M] (CA)"}
   {:qty 1 :card "Tower of the Wolf-friend" :id "[M] (GW)"}
   {:qty 1 :card "Traith Chefudoc" :id "[M] (KN)"}
   {:qty 1 :card "Tresti" :id "[M] (SL)"}
   {:qty 1 :card "Tuckburrow" :id "[M] (RS)"}
   {:qty 1 :card "Tûl Harar" :id "[M] (BO)"}
   {:qty 1 :card "Tûl Isra" :id "[M] (SL)"}
   {:qty 1 :card "Tûl Póac" :id "[M] (SL)"}
   {:qty 1 :card "Turukulon's Lair" :id "[M] (TI)"}
   {:qty 1 :card "Ty-ar-Rana" :id "[M] (CA)"}
   {:qty 1 :card "Umbar" :id "[M] (WR)"}
   {:qty 1 :card "Ûrêzâyan" :id "[M] (SL)"}
   {:qty 1 :card "Urud-an-Khibil" :id "[M] (SL)"}
   {:qty 1 :card "Valagalen" :id "[M] (DS)"}
   {:qty 1 :card "Vale of Erech" :id "[M] (LE)"}
   {:qty 1 :card "Valley of Tombs" :id "[M] (BO)"}
   {:qty 1 :card "Vamag" :id "[M] (WR)"}
   {:qty 1 :card "Variag Camp" :id "[M] (LE)"}
   {:qty 1 :card "Vasaran Ahjo" :id "[M] (NW)"}
   {:qty 1 :card "Vog Mur" :id "[M] (CA)"}
   {:qty 1 :card "Wain-Easterling Camp" :id "[M] (CP)"}
   {:qty 1 :card "Watch at Unulló" :id "[M] (DS)"}
   {:qty 1 :card "Weathertop" :id "[M] (AS)"}
   {:qty 1 :card "Wellinghall" :id "[M] (AS)"}
   {:qty 1 :card "Woodmen-town" :id "[M] (LE)"}
   {:qty 1 :card "Wose Passage-hold" :id "[M] (LE)"}
   {:qty 1 :card "Xyunaï" :id "[M] (BO)"}
   {:qty 1 :card "Yatali Kîrgan" :id "[M] (BO)"}
   {:qty 1 :card "Yjuvït" :id "[M] (BU)"}
   {:qty 1 :card "Zadan an-Adûn" :id "[M] (DS)"}
   {:qty 1 :card "Zarak Dûm" :id "[M] (LE)"}
   {:qty 1 :card "Zayandaur" :id "[M] (BU)"}])

(def dreamcard-minion
  [{:qty 1 :card "Abandoned Caravansary" :id "[M] (SL)"}
   {:qty 1 :card "Achrond" :id "[M] (NW)"}
   {:qty 1 :card "Adan Tomb" :id "[M] (CP)"}
   {:qty 1 :card "Aden Scarlet's Medical Library" :id "[M] (KN)"}
   {:qty 1 :card "Adûn-Tarîk" :id "[M] (SL)"}
   {:qty 1 :card "Aelinost" :id "[M] (BO)"}
   {:qty 1 :card "Aeluin" :id "[M] (DS)"}
   {:qty 1 :card "Alkarrânda" :id "[M] (BO)"}
   {:qty 1 :card "Alkyad" :id "[M] (CP)"}
   {:qty 1 :card "Alsarias" :id "[M] (DS)"}
   {:qty 1 :card "Amon Anlug" :id "[M] (NW)"}
   {:qty 1 :card "Amon Hen" :id "[M] (LE)"}
   {:qty 1 :card "Amon Lhaw" :id "[M] (TI)"}
   {:qty 1 :card "Amon Lind" :id "[M] (FB)"}
   {:qty 1 :card "Amrûn" :id "[M] (SL)"}
   {:qty 1 :card "An Karagmir" :id "[M] (SL)"}
   {:qty 1 :card "Andoloki" :id "[M] (KN)"}
   {:qty 1 :card "Ankruz" :id "[M] (SL)"}
   {:qty 1 :card "Annúminas" :id "[M] (KN)"}
   {:qty 1 :card "Arentaurr" :id "[M] (CP)"}
   {:qty 1 :card "Arhazûn-Tarîk" :id "[M] (DS)"}
   {:qty 1 :card "Arig's Tomb" :id "[M] (DS)"}
   {:qty 1 :card "Arpel" :id "[M] (DS)"}
   {:qty 1 :card "Auz Azunan" :id "[M] (SL)"}
   {:qty 1 :card "Azagarbhun" :id "[M] (CP)"}
   {:qty 1 :card "Bag End" :id "[M] (LE)"}
   {:qty 1 :card "Balchoth Camp" :id "[M] (CP)"}
   {:qty 1 :card "Bandit Lair" :id "[M] (LE)"}
   {:qty 1 :card "Bar Falin" :id "[M] (SL)"}
   {:qty 1 :card "Bar-en-Ibûn" :id "[M] (DF)"}
   {:qty 1 :card "Barad Lughilsarik" :id "[M] (NW)"}
   {:qty 1 :card "Barad Tathren" :id "[M] (TI)"}
   {:qty 1 :card "Barad-wath" :id "[M] (NE)"}
   {:qty 1 :card "Barak-shathûr" :id "[M] (DF)"}
   {:qty 1 :card "Barrow-downs" :id "[M] (LE)"}
   {:qty 1 :card "Baruzimabûl" :id "[M] (DS)"}
   {:qty 1 :card "Bâtan-Urîd" :id "[M] (SL)"}
   {:qty 1 :card "Benish Armon" :id "[M] (WR)"}
   {:qty 1 :card "Beorn's House" :id "[M] (LE)"}
   {:qty 1 :card "Bernastath" :id "[M] (NW)"}
   {:qty 1 :card "Blue Mountain Dwarf-hold" :id "[M] (LE)"}
   {:qty 1 :card "Bog Hills" :id "[M] (NW)"}
   {:qty 1 :card "Brandy Hall" :id "[M] (RS)"}
   {:qty 1 :card "Bree" :id "[M] (LE)"}
   {:qty 1 :card "Buhr Thurasig" :id "[M] (GW)"}
   {:qty 1 :card "Buhr Widu" :id "[M] (LE)"}
   {:qty 1 :card "Bulchyaden Marches" :id "[M] (BO)"}
   {:qty 1 :card "Bûr Esmer" :id "[M] (SL)"}
   {:qty 1 :card "Bywater" :id "[M] (WR)"}
   {:qty 1 :card "Cair Andros" :id "[M] (WR)"}
   {:qty 1 :card "Cairn of the Colruh Hazurbal" :id "[M] (SL)"}
   {:qty 1 :card "Calembel" :id "[M] (WR)"}
   {:qty 1 :card "Cameth Brin" :id "[M] (LE)"}
   {:qty 1 :card "Caras Amarth" :id "[M] (FB)"}
   {:qty 1 :card "Cave of the Urdharkonur" :id "[M] (GW)"}
   {:qty 1 :card "Caves of Ûlund" :id "[M] (LE)"}
   {:qty 1 :card "Ceber Fanuin" :id "[M] (FB)"}
   {:qty 1 :card "Celeb-Ost" :id "[M] (GW)"}
   {:qty 1 :card "Celebannon" :id "[M] (FB)"}
   {:qty 1 :card "Cerin Amroth" :id "[M] (FB)"}
   {:qty 1 :card "Charnesra" :id "[M] (SL)"}
   {:qty 1 :card "Cirith Helkaloke" :id "[M] (GW)"}
   {:qty 1 :card "Cirlond" :id "[M] (BO)"}
   {:qty 1 :card "Coastal Signal Tower" :id "[M] (WR)"}
   {:qty 1 :card "Collarmount" :id "[M] (GW)"}
   {:qty 1 :card "Cor Angaladh" :id "[M] (FB)"}
   {:qty 1 :card "Cor Minyadhras" :id "[M] (DS)"}
   {:qty 1 :card "Creb Durga" :id "[M] (KN)"}
   {:qty 1 :card "Daeron's Pool" :id "[M] (GW)"}
   {:qty 1 :card "Dale" :id "[M] (LE)"}
   {:qty 1 :card "Dale of Long Silence" :id "[M] (CP)"}
   {:qty 1 :card "Dancing Spire" :id "[M] (AS)"}
   {:qty 1 :card "Dar Egleriador" :id "[M] (BO)"}
   {:qty 1 :card "Dead Man's Dike" :id "[M] (KN)"}
   {:qty 1 :card "Dead Marshes" :id "[M] (LE)"}
   {:qty 1 :card "Deep Cleft" :id "[M] (GW)"}
   {:qty 1 :card "Dimrill Dale" :id "[M] (LE)"}
   {:qty 1 :card "Dol Amroth" :id "[M] (LE)"}
   {:qty 1 :card "Drúadan Forest" :id "[M] (LE)"}
   {:qty 1 :card "Dunharrow" :id "[M] (LE)"}
   {:qty 1 :card "Dunnish Clan-hold" :id "[M] (LE)"}
   {:qty 1 :card "Dûrdamal" :id "[M] (CA)"}
   {:qty 1 :card "Durthang" :id "[M] (NE)"}
   {:qty 1 :card "Dûsalan" :id "[M] (SL)"}
   {:qty 1 :card "Eagles' Eyrie" :id "[M] (AS)"}
   {:qty 1 :card "Easterling Camp" :id "[M] (LE)"}
   {:qty 1 :card "Edhellond" :id "[M] (AS)"}
   {:qty 1 :card "Edoras" :id "[M] (LE)"}
   {:qty 1 :card "Ei Missä" :id "[M] (NW)"}
   {:qty 1 :card "Elanthia" :id "[M] (CP)"}
   {:qty 1 :card "Eldanar" :id "[M] (KN)"}
   {:qty 1 :card "Elgaer" :id "[M] (CP)"}
   {:qty 1 :card "Elornan Swamp" :id "[M] (DS)"}
   {:qty 1 :card "Elyamû" :id "[M] (BU)"}
   {:qty 1 :card "Emyn Din" :id "[M] (DS)"}
   {:qty 1 :card "Ephydis" :id "[M] (BO)"}
   {:qty 1 :card "Eregost" :id "[M] (DS)"}
   {:qty 1 :card "Ettenmoors" :id "[M] (LE)"}
   {:qty 1 :card "Evermist" :id "[M] (NW)"}
   {:qty 1 :card "Fhûl" :id "[M] (SL)"}
   {:qty 1 :card "Fortress of Bûramak" :id "[M] (CA)"}
   {:qty 1 :card "Framsburg" :id "[M] (AS)"}
   {:qty 1 :card "Fuinur's Well" :id "[M] (SL)"}
   {:qty 1 :card "Gaurblog Lug" :id "[M] (DF)"}
   {:qty 1 :card "Gaven" :id "[M] (CA)"}
   {:qty 1 :card "Gesathago's Lair" :id "[M] (CA)"}
   {:qty 1 :card "Giant's Isle" :id "[M] (RS)"}
   {:qty 1 :card "Gladden Fields" :id "[M] (LE)"}
   {:qty 1 :card "Glittering Caves" :id "[M] (LE)"}
   {:qty 1 :card "Gobel Mírlond" :id "[M] (LE)"}
   {:qty 1 :card "Goblin-gate" :id "[M] (LE)"}
   {:qty 1 :card "Gold Hill" :id "[M] (AS)"}
   {:qty 1 :card "Gondmaeglom" :id "[M] (LE)"}
   {:qty 1 :card "Gondring's Lair" :id "[M] (NW)"}
   {:qty 1 :card "Grey Havens" :id "[M] (AS)"}
   {:qty 1 :card "Gyogorasag Sanctuary" :id "[M] (GW)"}
   {:qty 1 :card "Hall of Alûva" :id "[M] (DS)"}
   {:qty 1 :card "Hall of Malkôra" :id "[M] (DS)"}
   {:qty 1 :card "Harbâz" :id "[M] (BO)"}
   {:qty 1 :card "Hau Nysrin" :id "[M] (SL)"}
   {:qty 1 :card "Haudh-in-Gwanûr" :id "[M] (LE)"}
   {:qty 1 :card "Hazaj Tollin" :id "[M] (SL)"}
   {:qty 1 :card "Helloth" :id "[M] (NW)"}
   {:qty 1 :card "Helm's Deep" :id "[M] (TI)"}
   {:qty 1 :card "Henneth Annûn" :id "[M] (LE)"}
   {:qty 1 :card "Hermit's Hill" :id "[M] (LE)"}
   {:qty 1 :card "Himring" :id "[M] (AS)"}
   {:qty 1 :card "Hollow Spire" :id "[M] (BU)"}
   {:qty 1 :card "Hostel of the Sisters of Nienna" :id "[M] (KN)"}
   {:qty 1 :card "Hyvät Kalat" :id "[M] (NW)"}
   {:qty 1 :card "Iorag Camp" :id "[M] (CP)"}
   {:qty 1 :card "Inyalonî" :id "[M] (BO)"}
   {:qty 1 :card "Irerock" :id "[M] (AS)"}
   {:qty 1 :card "Iron Hill Dwarf-hold" :id "[M] (LE)"}
   {:qty 1 :card "Isengard" :id "[M] (LE)"}
   {:qty 1 :card "Isildur's Tomb" :id "[M] (TI)"}
   {:qty 1 :card "Isle of the Ulond" :id "[M] (AS)"}
   {:qty 1 :card "Isles of the Dead That Live" :id "[M] (AS)"}
   {:qty 1 :card "Isvat" :id "[M] (BO)"}
   {:qty 1 :card "Jääkylät" :id "[M] (NW)"}
   {:qty 1 :card "Joghul's Shrine" :id "[M] (CP)"}
   {:qty 1 :card "Kadar an-Khâradun" :id "[M] (DS)"}
   {:qty 1 :card "Kala Dulakurth" :id "[M] (ML)"}
   {:qty 1 :card "Kala-Ogurk" :id "[M] (BO)"}
   {:qty 1 :card "Kanatrass" :id "[M] (NW)"}
   {:qty 1 :card "Khibil Ephalak" :id "[M] (SL)"}
   {:qty 1 :card "Khorsâj" :id "[M] (SL)"}
   {:qty 1 :card "Killing Fields" :id "[M] (SL)"}
   {:qty 1 :card "Kirnak" :id "[M] (CA)"}
   {:qty 1 :card "Kondu Manara" :id "[M] (DS)"}
   {:qty 1 :card "Korlan" :id "[M] (CA)"}
   {:qty 1 :card "Korlea" :id "[M] (SL)"}
   {:qty 1 :card "Korondaj" :id "[M] (SL)"}
   {:qty 1 :card "Kref Masar" :id "[M] (SL)"}
   {:qty 1 :card "Kylmätalo" :id "[M] (NW)"}
   {:qty 1 :card "Lake-town" :id "[M] (LE)"}
   {:qty 1 :card "Lakes of Kann-Sharmûnda" :id "[M] (CA)"}
   {:qty 1 :card "Lâorkí" :id "[M] (CP)"}
   {:qty 1 :card "Lar-Huz" :id "[M] (CP)"}
   {:qty 1 :card "Laurrë's Manor" :id "[M] (CA)"}
   {:qty 1 :card "Leiri" :id "[M] (NW)"}
   {:qty 1 :card "Lighthouse at Kraken Cape" :id "[M] (SL)"}
   {:qty 1 :card "Ligr Wodaize Berne" :id "[M] (NW)"}
   {:qty 1 :card "Lind-or-Burúm" :id "[M] (TI)"}
   {:qty 1 :card "Linhir" :id "[M] (WR)"}
   {:qty 1 :card "Linnarthurras" :id "[M] (NW)"}
   {:qty 1 :card "Litash" :id "[M] (NW)"}
   {:qty 1 :card "Logath Camp" :id "[M] (CP)"}
   {:qty 1 :card "Lond Anarion" :id "[M] (DS)"}
   {:qty 1 :card "Lond Daer" :id "[M] (KN)"}
   {:qty 1 :card "Lond Galen" :id "[M] (LE)"}
   {:qty 1 :card "Long Marshes" :id "[M] (DF)"}
   {:qty 1 :card "Long Peak" :id "[M] (GW)"}
   {:qty 1 :card "Lórien" :id "[M] (AS)"}
   {:qty 1 :card "Lossadan Cairn" :id "[M] (LE)"}
   {:qty 1 :card "Lossadan Camp" :id "[M] (LE)"}
   {:qty 1 :card "Lossarnach" :id "[M] (WR)"}
   {:qty 1 :card "Lothragh Camp" :id "[M] (BU)"}
   {:qty 1 :card "Lugarlûr" :id "[M] (NE)"}
   {:qty 1 :card "Lugdruong" :id "[M] (BU)"}
   {:qty 1 :card "Mablad-dûm" :id "[M] (SL)"}
   {:qty 1 :card "Malgolodh's Cave" :id "[M] (NW)"}
   {:qty 1 :card "Maresh" :id "[M] (SL)"}
   {:qty 1 :card "Mathlaburg" :id "[M] (NE)"}
   {:qty 1 :card "Medlóshad" :id "[M] (CP)"}
   {:qty 1 :card "Michel Delving" :id "[M] (RS)"}
   {:qty 1 :card "Minas Durlith" :id "[M] (NE)"}
   {:qty 1 :card "Minas Tirith" :id "[M] (LE)"}
   {:qty 1 :card "Mirror Halls" :id "[M] (BU)"}
   {:qty 1 :card "Míspir" :id "[M] (DS)"}
   {:qty 1 :card "Mistrand" :id "[M] (NE)"}
   {:qty 1 :card "Monastery of the True Faith" :id "[M] (SL)"}
   {:qty 1 :card "Morkai" :id "[M] (KN)"}
   {:qty 1 :card "Mount Arysis" :id "[M] (SL)"}
   {:qty 1 :card "Mount Doom" :id "[M] (LE)"}
   {:qty 1 :card "Mount Gram" :id "[M] (LE)"}
   {:qty 1 :card "Mount Gundabad" :id "[M] (LE)"}
   {:qty 1 :card "Mount Rerir" :id "[M] (FB)"}
   {:qty 1 :card "Mountains of Mirkwood" :id "[M] (NE)"}
   {:qty 1 :card "Mûmakil Cemetery" :id "[M] (SL)"}
   {:qty 1 :card "Naerphys" :id "[M] (BO)"}
   {:qty 1 :card "Nâlashatûr" :id "[M] (BO)"}
   {:qty 1 :card "Nan Morsereg" :id "[M] (NE)"}
   {:qty 1 :card "Nárad-dûm" :id "[M] (SL)"}
   {:qty 1 :card "Narik-Zadan" :id "[M] (SL)"}
   {:qty 1 :card "Nennûrad" :id "[M] (CP)"}
   {:qty 1 :card "Nevazar's Tomb" :id "[M] (BO)"}
   {:qty 1 :card "Nioca Manara" :id "[M] (DS)"}
   {:qty 1 :card "Nîlûlondê" :id "[M] (SL)"}
   {:qty 1 :card "Norjadar" :id "[M] (SL)"}
   {:qty 1 :card "Norr-dûm" :id "[M] (GW)"}
   {:qty 1 :card "Nûlakad" :id "[M] (CA)"}
   {:qty 1 :card "Númenórean Tomb" :id "[M] (BU)"}
   {:qty 1 :card "Nûrniag Camp" :id "[M] (LE)"}
   {:qty 1 :card "Nurunkhizdín" :id "[M] (DF)"}
   {:qty 1 :card "Ny Chennacatt" :id "[M] (SL)"}
   {:qty 1 :card "Oasis of Fult" :id "[M] (SL)"}
   ;{:qty 1 :card "Old Forest" :id "[M] (AS)"}
   {:qty 1 :card "Olyavud" :id "[M] (BU)"}
   {:qty 1 :card "Oraishapek's Mound" :id "[M] (NE)"}
   {:qty 1 :card "Orod Certhas" :id "[M] (NW)"}
   {:qty 1 :card "Osgiliath" :id "[M] (WR)"}
   {:qty 1 :card "Ost Angthoronion" :id "[M] (BO)"}
   {:qty 1 :card "Ost-in-Edhil" :id "[M] (LE)"}
   {:qty 1 :card "Ostelor" :id "[M] (DS)"}
   {:qty 1 :card "Ostigurth" :id "[M] (NE)"}
   {:qty 1 :card "Ovir Hollow" :id "[M] (AS)"}
   {:qty 1 :card "Pelargir" :id "[M] (LE)"}
   {:qty 1 :card "Pelepelplû" :id "[M] (BO)"}
   {:qty 1 :card "Pelican Islands" :id "[M] (SL)"}
   {:qty 1 :card "Pendrath na-Udûn" :id "[M] (ML)"}
   {:qty 1 :card "Pharabâs" :id "[M] (CA)"}
   {:qty 1 :card "Pieni Satama" :id "[M] (NW)"}
   {:qty 1 :card "Poison Rock" :id "[M] (BO)"}
   {:qty 1 :card "Pred" :id "[M] (SL)"}
   {:qty 1 :card "Puolihmisten Satama" :id "[M] (NW)"}
   {:qty 1 :card "Quarries of Nosharud" :id "[M] (BO)"}
   {:qty 1 :card "Raider-hold" :id "[M] (LE)"}
   {:qty 1 :card "Ramôrth" :id "[M] (CA)"}
   {:qty 1 :card "Relerindú" :id "[M] (CP)"}
   {:qty 1 :card "Rhosgobel" :id "[M] (AS)"}
   {:qty 1 :card "Rhûbar" :id "[M] (FB)"}
   {:qty 1 :card "Riavod" :id "[M] (CP)"}
   {:qty 1 :card "Rilgul" :id "[M] (CA)"}
   {:qty 1 :card "Rînaghnâti" :id "[M] (BO)"}
   {:qty 1 :card "Rivendell" :id "[M] (AS)"}
   {:qty 1 :card "Ró-molló" :id "[M] (DS)"}
   {:qty 1 :card "Ruined Signal Tower" :id "[M] (LE)"}
   {:qty 1 :card "Ruins of Anaoshak" :id "[M] (BO)"}
   {:qty 1 :card "Ruskea Vene" :id "[M] (NW)"}
   {:qty 1 :card "Sackville" :id "[M] (RS)"}
   {:qty 1 :card "Samarth" :id "[M] (BO)"}
   {:qty 1 :card "Sarn Goriwing" :id "[M] (LE)"}
   {:qty 1 :card "Sarûl" :id "[M] (CA)"}
   {:qty 1 :card "Seregul's Keep" :id "[M] (DS)"}
   {:qty 1 :card "Setmaenen" :id "[M] (WR)"}
   {:qty 1 :card "Shab Arch" :id "[M] (GW)"}
   {:qty 1 :card "Shelob's Lair" :id "[M] (LE)"}
   {:qty 1 :card "Shoreless Isles" :id "[M] (NW)"}
   {:qty 1 :card "Shrel-Kain" :id "[M] (LE)"}
   {:qty 1 :card "Sonondor's Mound" :id "[M] (GW)"}
   {:qty 1 :card "Southron Oasis" :id "[M] (LE)"}
   {:qty 1 :card "Steel Fall" :id "[M] (GW)"}
   {:qty 1 :card "Stone-circle" :id "[M] (LE)"}
   {:qty 1 :card "Strayhold" :id "[M] (NE)"}
   {:qty 1 :card "Sturlurtsa" :id "[M] (CP)"}
   {:qty 1 :card "Sud Sicanna" :id "[M] (SL)"}
   {:qty 1 :card "Sudu Cull" :id "[M] (SL)"}
   {:qty 1 :card "Tanith" :id "[M] (CA)"}
   {:qty 1 :card "Târik an-Aruwânâi" :id "[M] (BO)"}
   {:qty 1 :card "Târik an-Bawîba" :id "[M] (BO)"}
   {:qty 1 :card "Tartaust" :id "[M] (SL)"}
   {:qty 1 :card "Tarû-Makar" :id "[M] (CA)"}
   {:qty 1 :card "Taurondë" :id "[M] (CA)"}
   {:qty 1 :card "Telpëmar" :id "[M] (FB)"}
   {:qty 1 :card "Temple of Kondri Odchi" :id "[M] (CP)"}
   {:qty 1 :card "Temple of Lokuthor" :id "[M] (NE)"}
   {:qty 1 :card "Tenolkachyn" :id "[M] (BO)"}
   {:qty 1 :card "Tharagrondost" :id "[M] (WR)"}
   {:qty 1 :card "Tharbad" :id "[M] (LE)"}
   {:qty 1 :card "Thaurung" :id "[M] (NW)"}
   {:qty 1 :card "The Carrock" :id "[M] (DF)"}
   {:qty 1 :card "The Great Oasis" :id "[M] (SL)"}
   {:qty 1 :card "The Hospice of Lost Faith" :id "[M] (SL)"}
   {:qty 1 :card "The Last Bridge" :id "[M] (RS)"}
   {:qty 1 :card "The Lonely Mountain" :id "[M] (LE)"}
   {:qty 1 :card "The Pits of Angband" :id "[M] (ML)"}
   {:qty 1 :card "The Riddle Caves" :id "[M] (KN)"}
   {:qty 1 :card "The Stones" :id "[M] (LE)"}
   {:qty 1 :card "The Tower of Birds" :id "[M] (SL)"}
   {:qty 1 :card "The Under-caves" :id "[M] (KN)"}
   {:qty 1 :card "The Vaults of Utûmno" :id "[M] (BU)"}
   {:qty 1 :card "The White Towers" :id "[M] (LE)"}
   {:qty 1 :card "The Willow Dingle" :id "[M] (RS)"}
   {:qty 1 :card "The Wind Throne" :id "[M] (LE)"}
   {:qty 1 :card "The Worthy Hills" :id "[M] (LE)"}
   {:qty 1 :card "Thilgon's Tomb" :id "[M] (NW)"}
   {:qty 1 :card "Thôrion" :id "[M] (CA)"}
   {:qty 1 :card "Thraath" :id "[M] (CP)"}
   {:qty 1 :card "Thranduil's Halls" :id "[M] (LE)"}
   {:qty 1 :card "Three Sisters" :id "[M] (DS)"}
   {:qty 1 :card "Thunder Cleft" :id "[M] (GW)"}
   {:qty 1 :card "Thuringwathost" :id "[M] (NE)"}
   {:qty 1 :card "Tol Buruth" :id "[M] (CP)"}
   {:qty 1 :card "Tol Fuin" :id "[M] (ML)"}
   {:qty 1 :card "Tol Glingal" :id "[M] (SL)"}
   {:qty 1 :card "Tol Lamfirith" :id "[M] (KN)"}
   {:qty 1 :card "Tol Morwen" :id "[M] (ML)"}
   {:qty 1 :card "Tol Ringurthur" :id "[M] (BU)"}
   {:qty 1 :card "Tol Turgul" :id "[M] (DS)"}
   {:qty 1 :card "Tol Uialgaer" :id "[M] (SL)"}
   {:qty 1 :card "Tolfalas" :id "[M] (AS)"}
   {:qty 1 :card "Tom's House" :id "[M] (FB)"}
   {:qty 1 :card "Tomb-fields of Makaburini" :id "[M] (DS)"}
   {:qty 1 :card "Tombs of Oran-Plaar" :id "[M] (CA)"}
   {:qty 1 :card "Tower of the Wolf-friend" :id "[M] (GW)"}
   {:qty 1 :card "Traith Chefudoc" :id "[M] (KN)"}
   {:qty 1 :card "Tresti" :id "[M] (SL)"}
   {:qty 1 :card "Tuckburrow" :id "[M] (RS)"}
   {:qty 1 :card "Tûl Harar" :id "[M] (BO)"}
   {:qty 1 :card "Tûl Isra" :id "[M] (SL)"}
   {:qty 1 :card "Tûl Póac" :id "[M] (SL)"}
   {:qty 1 :card "Turukulon's Lair" :id "[M] (TI)"}
   {:qty 1 :card "Ty-ar-Rana" :id "[M] (CA)"}
   {:qty 1 :card "Ulk Chey Sart" :id "[M] (CP)"}
   {:qty 1 :card "Ulk Jey Ama" :id "[M] (CP)"}
   {:qty 1 :card "Umbar" :id "[M] (WR)"}
   {:qty 1 :card "Úrcheldor" :id "[M] (BU)"}
   {:qty 1 :card "Urdic Camp" :id "[M] (NW)"}
   {:qty 1 :card "Ûrêzâyan" :id "[M] (SL)"}
   {:qty 1 :card "Urlurtsu Nurn" :id "[M] (LE)"}
   {:qty 1 :card "Urud-an-Khibil" :id "[M] (SL)"}
   {:qty 1 :card "Valagalen" :id "[M] (DS)"}
   {:qty 1 :card "Vale of Erech" :id "[M] (LE)"}
   {:qty 1 :card "Valley of Tombs" :id "[M] (BO)"}
   {:qty 1 :card "Vamag" :id "[M] (WR)"}
   {:qty 1 :card "Variag Camp" :id "[M] (LE)"}
   {:qty 1 :card "Vasaran Ahjo" :id "[M] (NW)"}
   {:qty 1 :card "Ventazvah Ôran-tal" :id "[M] (BO)"}
   {:qty 1 :card "Vog Mur" :id "[M] (CA)"}
   {:qty 1 :card "Wain-Easterling Camp" :id "[M] (CP)"}
   {:qty 1 :card "Watch at Unulló" :id "[M] (DS)"}
   {:qty 1 :card "Weathertop" :id "[M] (AS)"}
   {:qty 1 :card "Wellinghall" :id "[M] (AS)"}
   {:qty 1 :card "Woodmen-town" :id "[M] (LE)"}
   {:qty 1 :card "Wose Passage-hold" :id "[M] (LE)"}
   {:qty 1 :card "Xyunaï" :id "[M] (BO)"}
   {:qty 1 :card "Yatali Kîrgan" :id "[M] (BO)"}
   {:qty 1 :card "Yjuvït" :id "[M] (BU)"}
   {:qty 1 :card "Zadan an-Adûn" :id "[M] (DS)"}
   {:qty 1 :card "Zarak Dûm" :id "[M] (LE)"}
   {:qty 1 :card "Zayandaur" :id "[M] (BU)"}])

(def dreamcard-minion-havens
  [{:qty 4 :card "Amaru" :id "[M] (CA)"}
   {:qty 4 :card "Bozisha-Dar" :id "[M] (SL)"}
   {:qty 4 :card "Carn Dûm" :id "[M] (LE)"}
   {:qty 4 :card "Chey Goumal" :id "[M] (CP)"}
   {:qty 4 :card "Dol Guldur" :id "[M] (LE)"}
   {:qty 4 :card "Geann a-Lisch" :id "[M] (LE)"}
   {:qty 4 :card "Minas Morgul" :id "[M] (LE)"}
   {:qty 4 :card "Mornost" :id "[M] (NW)"}
   {:qty 4 :card "Shapôl Udûn" :id "[M] (BU)"}
   {:qty 4 :card "Tower of Hargrog" :id "[M] (DS)"}])

(def dreamcard-minion-haven
  [{:qty 1 :card "Amaru" :id "[M] (CA)"}
   {:qty 1 :card "Bozisha-Dar" :id "[M] (SL)"}
   {:qty 1 :card "Carn Dûm" :id "[M] (LE)"}
   {:qty 1 :card "Chey Goumal" :id "[M] (CP)"}
   {:qty 1 :card "Dol Guldur" :id "[M] (LE)"}
   {:qty 1 :card "Geann a-Lisch" :id "[M] (LE)"}
   {:qty 1 :card "Minas Morgul" :id "[M] (LE)"}
   {:qty 1 :card "Mornost" :id "[M] (NW)"}
   {:qty 1 :card "Shapôl Udûn" :id "[M] (BU)"}
   {:qty 1 :card "Tower of Hargrog" :id "[M] (DS)"}])

(def dreamcard-minion-balrog
  [{:qty 1 :card "Barad-dûr" :id "[M] (LE)"}
   {:qty 1 :card "Cirith Gorgor" :id "[M] (LE)"}
   {:qty 1 :card "Cirith Ungol" :id "[M] (LE)"}
   {:qty 1 :card "Eithel Morgoth" :id "[M] (ML)"}
   {:qty 1 :card "Moria" :id "[M] (LE)"}
   {:qty 1 :card "The Gem-deeps" :id "[M] (AS)"}
   {:qty 1 :card "The Iron-deeps" :id "[M] (AS)"}
   {:qty 1 :card "The Pûkel-deeps" :id "[M] (AS)"}
   {:qty 1 :card "The Sulfur-deeps" :id "[M] (AS)"}
   {:qty 1 :card "The Under-courts" :id "[M] (AS)"}
   {:qty 1 :card "The Under-forges" :id "[M] (ML)"}
   {:qty 1 :card "The Under-galleries" :id "[M] (AS)"}
   {:qty 1 :card "The Under-gates" :id "[M] (AS)"}
   {:qty 1 :card "The Under-grottos" :id "[M] (AS)"}
   {:qty 1 :card "The Under-leas" :id "[M] (AS)"}
   {:qty 1 :card "The Under-vaults" :id "[M] (AS)"}])

(def dreamcard-wizard-dragon ;; shadow/dark
  [{:qty 1 :card "Adûn-Tarîk" :id "[H] (SL)"}
   {:qty 1 :card "Amrûn" :id "[H] (SL)"}
   {:qty 1 :card "Balchoth Camp" :id "[H] (CP)"}
   {:qty 1 :card "Barad Lughilsarik" :id "[H] (NW)"}
   {:qty 1 :card "Barad-wath" :id "[H] (NE)"}
   {:qty 1 :card "Bâtan-Urîd" :id "[H] (SL)"}
   {:qty 1 :card "Bozisha-Dar" :id "[H] (SL)"}
   {:qty 1 :card "Bûr Esmer" :id "[H] (SL)"}
   {:qty 1 :card "Chey Goumal" :id "[H] (CP)"}
   {:qty 1 :card "Creb Durga" :id "[H] (KN)"}
   {:qty 1 :card "Dead Marshes" :id "[H] (TW)"}
   {:qty 1 :card "Deep Cleft" :id "[H] (GW)"}
   {:qty 1 :card "Durthang" :id "[H] (NE)"}
   {:qty 1 :card "Dûsalan" :id "[H] (SL)"}
   {:qty 1 :card "Eithel Morgoth" :id "[H] (ML)"}
   {:qty 1 :card "Eregost" :id "[H] (DS)"}
   {:qty 1 :card "Fhûl" :id "[H] (SL)"}
   {:qty 1 :card "Fuinur's Well" :id "[H] (SL)"}
   {:qty 1 :card "Gaurblog Lug" :id "[H] (DF)"}
   {:qty 1 :card "Goblin-gate" :id "[H] (TW)"}
   {:qty 1 :card "Hall of Malkôra" :id "[H] (DS)"}
   {:qty 1 :card "Kala Dulakurth" :id "[H] (ML)"}
   {:qty 1 :card "Kala-Ogurk" :id "[H] (BO)"}
   {:qty 1 :card "Kondu Manara" :id "[H] (DS)"}
   {:qty 1 :card "Lâorkí" :id "[H] (CP)"}
   {:qty 1 :card "Litash" :id "[H] (NW)"}
   {:qty 1 :card "Lugarlûr" :id "[H] (NE)"}
   {:qty 1 :card "Lugdruong" :id "[H] (BU)"}
   {:qty 1 :card "Minas Durlith" :id "[H] (NE)"}
   {:qty 1 :card "Mistrand" :id "[H] (NE)"}
   {:qty 1 :card "Moria" :id "[H] (TW)"}
   {:qty 1 :card "Morkai" :id "[H] (KN)"}
   {:qty 1 :card "Mount Arysis" :id "[H] (SL)"}
   {:qty 1 :card "Mount Doom" :id "[H] (TW)"}
   {:qty 1 :card "Mount Gram" :id "[H] (TW)"}
   {:qty 1 :card "Mount Gundabad" :id "[H] (TW)"}
   {:qty 1 :card "Mount Rerir" :id "[H] (FB)"}
   {:qty 1 :card "Naerphys" :id "[H] (BO)"}
   {:qty 1 :card "Nûrniag Camp" :id "[H] (AS)"}
   {:qty 1 :card "Ny Chennacatt" :id "[H] (SL)"}
   {:qty 1 :card "Pendrath na-Udûn" :id "[H] (ML)"}
   {:qty 1 :card "Sarn Goriwing" :id "[H] (TW)"}
   {:qty 1 :card "Shelob's Lair" :id "[H] (TW)"}
   {:qty 1 :card "Sturlurtsa" :id "[H] (CP)"}
   {:qty 1 :card "Târik an-Aruwânâi" :id "[H] (BO)"}
   {:qty 1 :card "The Pits of Angband" :id "[H] (ML)"}
   {:qty 1 :card "The Under-caves" :id "[H] (KN)"}
   {:qty 1 :card "The Under-forges" :id "[H] (ML)"}
   {:qty 1 :card "The Under-gates" :id "[H] (DM)"}
   {:qty 1 :card "The Vaults of Utûmno" :id "[H] (BU)"}
   {:qty 1 :card "Thuringwathost" :id "[H] (NE)"}
   {:qty 1 :card "Tower of Hargrog" :id "[H] (DS)"}
   {:qty 1 :card "Ulk Chey Sart" :id "[H] (CP)"}
   {:qty 1 :card "Ulk Jey Ama" :id "[H] (CP)"}
   {:qty 1 :card "Úrcheldor" :id "[H] (BU)"}
   {:qty 1 :card "Urdic Camp" :id "[H] (NW)"}
   {:qty 1 :card "Urlurtsu Nurn" :id "[H] (DM)"}
   {:qty 1 :card "Ventazvah Ôran-tal" :id "[H] (BO)"}])

(def dreamcard-balrog
  [{:qty 1 :card "Amaru" :id "[B] (CA)"}
   {:qty 1 :card "Ancient Deep-hold" :id "[B] (BA)"}
   {:qty 1 :card "Barad-dûr" :id "[B] (BA)"}
   {:qty 1 :card "Carn Dûm" :id "[B] (BA)"}
   {:qty 1 :card "Cirith Gorgor" :id "[B] (BA)"}
   {:qty 1 :card "Cirith Ungol" :id "[B] (BA)"}
   {:qty 1 :card "Dol Guldur" :id "[B] (BA)"}
   {:qty 4 :card "Eithel Morgoth" :id "[B] (ML)"}
   {:qty 1 :card "Minas Morgul" :id "[B] (BA)"}
   {:qty 4 :card "Moria" :id "[B] (BA)"}
   {:qty 1 :card "Mornost" :id "[B] (ML)"}
   {:qty 1 :card "Remains of Thangorodrim" :id "[B] (BA)"}
   {:qty 1 :card "Shapôl Udûn" :id "[B] (BU)"}
   {:qty 1 :card "The Drowning-deeps" :id "[B] (BA)"}
   {:qty 1 :card "The Gem-deeps" :id "[B] (BA)"}
   {:qty 1 :card "The Iron-deeps" :id "[B] (BA)"}
   {:qty 1 :card "The Pûkel-deeps" :id "[B] (BA)"}
   {:qty 1 :card "The Rusted-deeps" :id "[B] (BA)"}
   {:qty 1 :card "The Sulfur-deeps" :id "[B] (BA)"}
   {:qty 1 :card "The Under-courts" :id "[B] (BA)"}
   {:qty 4 :card "The Under-forges" :id "[B] (ML)"}
   {:qty 1 :card "The Under-galleries" :id "[B] (BA)"}
   {:qty 4 :card "The Under-gates" :id "[B] (BA)"}
   {:qty 1 :card "The Under-grottos" :id "[B] (BA)"}
   {:qty 1 :card "The Under-leas" :id "[B] (BA)"}
   {:qty 1 :card "The Under-vaults" :id "[B] (BA)"}
   {:qty 1 :card "The Wind-deeps" :id "[B] (BA)"}])

(def dreamcard-balrog-extras-wizard
  [{:qty 1 :card "Ancient Deep-hold" :id "[B] (BA)"}
   {:qty 1 :card "Remains of Thangorodrim" :id "(ML)"}
   {:qty 1 :card "The Drowning-deeps" :id "[D] (DF)"}
   {:qty 1 :card "The Rusted-deeps" :id "[D] (DF)"}
   {:qty 1 :card "The Wind-deeps" :id "[D] (DF)"}])

(def dreamcard-balrog-extras-minion
  [{:qty 1 :card "Remains of Thangorodrim" :id "(ML)"}
   {:qty 1 :card "The Drowning-deeps" :id "[D] (DF)"}
   {:qty 1 :card "The Rusted-deeps" :id "[D] (DF)"}
   {:qty 1 :card "The Wind-deeps" :id "[D] (DF)"}])

(def dreamcard-fallen-havens
  [{:qty 4 :card "Isengard" :id "[F] (WH)"}
   {:qty 4 :card "Rhosgobel" :id "[F] (WH)"}
   {:qty 4 :card "The White Towers" :id "[F] (WH)"}])

(def dreamcard-dwarf-havens
  [{:qty 4 :card "Blue Mountain Dwarf-hold" :id "[D] (DF)"}
   {:qty 4 :card "Iron Hill Dwarf-hold" :id "[D] (DF)"}])

(def dreamcard-lord-all
  [{:qty 1 :card "Aurax-dûr" :id "(CA)"}
   {:qty 1 :card "Citadel of Ardor" :id "(CA)"}
   {:qty 1 :card "Engkîr" :id "(CA)"}
   {:qty 1 :card "Ithilkîr" :id "(CA)"}
   {:qty 1 :card "Menelcarca" :id "(CA)"}
   {:qty 1 :card "Mirisgroth" :id "(CA)"}
   {:qty 1 :card "Naurlindol" :id "(CA)"}
   {:qty 1 :card "Taurang" :id "(CA)"}
   {:qty 1 :card "Tirgoroth" :id "(CA)"}])

(def dreamcard-lord-only
  [{:qty 4 :card "Deep Mines" :id "(WH)"}
   {:qty 1 :card "Edhellond" :id "[F] (FB)"}
   {:qty 1 :card "Grey Havens" :id "[F] (FB)"}
   {:qty 1 :card "Lórien" :id "[F] (FB)"}
   {:qty 1 :card "Rivendell" :id "[F] (FB)"}
   {:qty 1 :card "Thranduil's Halls" :id "[F] (FB)"}])

(def dreamcard-elf-havens
  [{:qty 4 :card "Grey Havens" :id "[E] (FB)"}
   {:qty 4 :card "Heart of the Halls (FB)"}
   {:qty 4 :card "Lórien" :id "[E] (FB)"}
   {:qty 4 :card "Rivendell" :id "[E] (FB)"}
   {:qty 4 :card "Thranduil's Halls" :id "[E] (FB)"}])

(def dreamcard-dual-sites
  [{:qty 1 :card "Ancient Maze" :id "(ML)"}
   {:qty 1 :card "Arvarien's Maze" :id "(DS)"}
   {:qty 1 :card "Deeps of Fuinur" :id "(SL)"}
   {:qty 1 :card "Durin's Tower" :id "(DF)"}
   {:qty 1 :card "Evefalin Cavern-Systems" :id "(SL)"}
   {:qty 1 :card "Grop-Kûlkodar" :id "(BU)"}
   {:qty 1 :card "Ilpar-Karam" :id "(CP)"}
   {:qty 1 :card "Mines of Angûrath" :id "(DF)"}
   {:qty 1 :card "Mines of Falek-Dim" :id "(DF)"}
   {:qty 1 :card "Osteledan" :id "(FB)"}
   {:qty 1 :card "Quartz-Halls" :id "(DS)"}
   {:qty 1 :card "Ruins of Belegost" :id "(DF)"}
   {:qty 1 :card "Ruins of Kheledkhizdín" :id "(BU)"}
   {:qty 1 :card "Ruins of Nogrod" :id "(DF)"}
   {:qty 1 :card "The Amber-deeps" :id "(SL)"}
   {:qty 1 :card "The Brass-deeps" :id "(BO)"}
   {:qty 1 :card "The Cobalt-deeps" :id "(ML)"}
   {:qty 1 :card "The Ghost-caves" :id "(KN)"}
   {:qty 1 :card "The Lava-deeps" :id "(CP)"}
   {:qty 1 :card "The Limestone-caverns" :id "(BO)"}
   {:qty 1 :card "Wreck of the Thoroval" :id "(NW)"}])

(def dreamcard-dragon
  [{:qty 1 :card "Amaru" :id "[B] (CA)"}
   {:qty 1 :card "Ancient Deep-hold" :id "[B] (BA)"}
   {:qty 1 :card "Barad-dûr" :id "[B] (BA)"}
   {:qty 1 :card "Bozisha-Dar" :id "[M] (SL)"}
   {:qty 1 :card "Carn Dûm" :id "[B] (BA)"}
   {:qty 1 :card "Chey Goumal" :id "[M] (CP)"}
   {:qty 1 :card "Cirith Gorgor" :id "[B] (BA)"}
   {:qty 1 :card "Cirith Ungol" :id "[B] (BA)"}
   {:qty 1 :card "Dol Guldur" :id "[B] (BA)"}
   {:qty 1 :card "Eithel Morgoth" :id "[M] (ML)"}
   {:qty 1 :card "Geann a-Lisch" :id "[M] (LE)"}
   {:qty 1 :card "Minas Morgul" :id "[B] (BA)"}
   {:qty 1 :card "Moria" :id "[M] (LE)"}
   {:qty 1 :card "Mornost" :id "[B] (NW)"}
   {:qty 1 :card "Remains of Thangorodrim" :id "[B] (BA)"}
   {:qty 1 :card "Shapôl Udûn" :id "[B] (BU)"}
   {:qty 1 :card "Tower of Hargrog" :id "[M] (DS)"}
   {:qty 1 :card "The Drowning-deeps" :id "[B] (BA)"}
   {:qty 1 :card "The Gem-deeps" :id "[B] (BA)"}
   {:qty 1 :card "The Iron-deeps" :id "[B] (BA)"}
   {:qty 1 :card "The Pûkel-deeps" :id "[B] (BA)"}
   {:qty 1 :card "The Rusted-deeps" :id "[B] (BA)"}
   {:qty 1 :card "The Sulfur-deeps" :id "[B] (BA)"}
   {:qty 1 :card "The Under-courts" :id "[B] (BA)"}
   {:qty 1 :card "The Under-galleries" :id "[B] (BA)"}
   {:qty 1 :card "The Under-gates" :id "[M] (AS)"}
   {:qty 1 :card "The Under-grottos" :id "[B] (BA)"}
   {:qty 1 :card "The Under-leas" :id "[B] (BA)"}
   {:qty 1 :card "The Under-vaults" :id "[B] (BA)"}
   {:qty 1 :card "The Wind-deeps" :id "[B] (BA)"}])

(def dreamcard-dragon-havens
  [{:qty 4 :card "Dragon's Den" :id "(GW)"}])

;; STANDARD
(def standard-metw-sites-only
  (into standard-region
        (into standard-metw-sites
              standard-wizard-havens)))
(def standard-metd-sites-only
  (into standard-region
        (into standard-metd-sites
              standard-wizard-havens)))
(def standard-medm-sites-only
  (into standard-region
        (into standard-medm-sites
              standard-wizard-havens)))
(def standard-meas-sites-only
  (into standard-region
        (into standard-wizard
              standard-wizard-havens)))
(def standard-wizard-sites
  (into standard-region
        (into standard-wizard
              (into standard-wizard-havens
                    standard-balrog-extras))))
(def standard-minion-sites
  (into standard-region
        (into standard-minion
              (into standard-minion-balrog
                    (into standard-minion-havens
                          standard-balrog)))))
(def standard-fallen-sites
  (into standard-region
        (into standard-wizard
              (into standard-wizard-haven
                    (into standard-minion
                          (into standard-minion-balrog
                                (into standard-minion-haven
                                      (into standard-fallen-havens
                                            standard-balrog-extras))))))))
(def standard-option-sites
  (into standard-region
        (into standard-wizard
              (into standard-wizard-havens
                    (into standard-minion
                          (into standard-minion-balrog
                                (into standard-minion-haven
                                      (into standard-fallen-havens
                                            standard-balrog-extras))))))))
(def standard-balrog-sites
  (into standard-region
        (into standard-minion
              (into standard-minion-balrog
                    (into standard-minion-havens
                          standard-balrog)))))
;; DREAMCARDS
(def dreamcard-wizard-sites
  (into dreamcard-region
        (into dreamcard-wizard
              (into dreamcard-wizard-havens
                    (into dreamcard-lord-all
                          (into dreamcard-dual-sites
                                dreamcard-balrog-extras-wizard))))))
(def dreamcard-minion-sites
  (into dreamcard-region
        (into dreamcard-minion
              (into dreamcard-minion-balrog
                    (into dreamcard-balrog
                          (into dreamcard-minion-havens
                                (into dreamcard-lord-all
                                      (into dreamcard-dual-sites
                                            dreamcard-balrog-extras-minion))))))))
(def dreamcard-balrog-sites
  (into dreamcard-region
        (into dreamcard-minion
              (into dreamcard-minion-balrog
                    (into dreamcard-balrog
                          (into dreamcard-minion-havens
                                (into dreamcard-lord-all
                                      (into dreamcard-dual-sites
                                            dreamcard-balrog-extras-minion))))))))
(def dreamcard-fallen-sites
  (into dreamcard-region
        (into dreamcard-wizard
              (into dreamcard-wizard-haven
                    (into dreamcard-minion
                          (into dreamcard-minion-balrog
                                (into dreamcard-minion-haven
                                      (into dreamcard-fallen-havens
                                            (into dreamcard-lord-only
                                                  (into dreamcard-lord-all
                                                        (into dreamcard-dual-sites
                                                              dreamcard-balrog-extras-wizard)))))))))))
(def dreamcard-option-sites
  (into dreamcard-region
        (into dreamcard-wizard
              (into dreamcard-wizard-havens
                    (into dreamcard-minion
                          (into dreamcard-minion-balrog
                                (into dreamcard-minion-haven
                                      (into dreamcard-fallen-havens
                                            (into dreamcard-lord-only
                                                  (into dreamcard-lord-all
                                                        (into dreamcard-dual-sites
                                                              dreamcard-balrog-extras-wizard)))))))))))
(def dreamcard-elf-sites
  (into dreamcard-region
        (into dreamcard-wizard
              (into dreamcard-wizard-haven
                    (into dreamcard-minion-lord
                          (into dreamcard-elf-havens
                                (into dreamcard-lord-only
                                      (into dreamcard-lord-all
                                            (into dreamcard-dual-sites
                                                  dreamcard-balrog-extras-wizard)))))))))
(def dreamcard-dwarf-sites
  (into dreamcard-region
        (into dreamcard-wizard
              (into dreamcard-wizard-haven
                    (into dreamcard-minion-lord
                          (into dreamcard-dwarf-havens
                                (into dreamcard-lord-only
                                      (into dreamcard-lord-all
                                            (into dreamcard-dual-sites
                                                  dreamcard-balrog-extras-wizard)))))))))
(def dreamcard-atani-sites
  (into dreamcard-region
        (into dreamcard-wizard
              (into dreamcard-wizard-haven
                    (into dreamcard-minion-lord
                          (into dreamcard-lord-only
                                (into dreamcard-lord-all
                                      (into dreamcard-dual-sites
                                            dreamcard-balrog-extras-wizard))))))))
(def dreamcard-dragon-sites
  (into dreamcard-region
        (into dreamcard-wizard-dragon
              (into dreamcard-minion
                    (into dreamcard-dragon
                          (into dreamcard-dragon-havens
                                (into dreamcard-lord-all
                                      dreamcard-dual-sites)))))))
(def dreamcard-warlord-sites
  (into dreamcard-region
        (into dreamcard-wizard-dragon
              (into dreamcard-minion
                    (into dreamcard-dragon
                          (into dreamcard-lord-all
                                dreamcard-dual-sites))))))
