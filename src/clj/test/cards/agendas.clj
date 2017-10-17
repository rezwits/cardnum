(ns test.cards.agendas
  (:require [game.core :as core]
            [test.core :refer :all]
            [test.utils :refer :all]
            [test.macros :refer :all]
            [clojure.test :refer :all]))

(deftest fifteen-minutes
  ;; 15 Minutes - check if it works correctly from both sides
  (do-game
    (new-game (default-corp [(qty "15 Minutes" 1)]) (default-runner))
    (play-from-hand state :corp "15 Minutes" "New remote")
    (take-credits state :corp)
    ;; use 15 minutes to take it away from runner
    (run-empty-server state "Server 1")
    (prompt-choice :runner "Steal")
    (take-credits state :runner)
    (is (= 1 (:agenda-point (get-runner))))
    (is (= 1 (count (:scored (get-runner)))))
    (let [fifm (first (:scored (get-runner)))]
      (is (= 3 (:click (get-corp))))
      (is (= 1 (count (:abilities (refresh fifm)))))
      (card-ability state :corp (refresh fifm) 0)
      (is (= 0 (:agenda-point (get-runner))))
      (is (= 0 (count (:scored (get-runner))))))
    (is (= "15 Minutes" (:title (first (:deck (get-corp))))))
    ;; TODO: could also check for deck shuffle
    (is (= 2 (:click (get-corp))))
    ;; use 15 minutes to take it away from corp (hey, maybe some obscure case happens where corp would want that)
    (core/click-draw state :corp 1)
    (play-from-hand state :corp "15 Minutes" "New remote")
    (take-credits state :runner)
    (score-agenda state :corp (get-content state :remote2 0))
    (is (= 1 (:agenda-point (get-corp))))
    (is (= 1 (count (:scored (get-corp)))))
    (let [fifm (first (:scored (get-corp)))]
      (is (= 1 (count (:abilities (refresh fifm)))))
      (card-ability state :corp (refresh fifm) 0)
      (is (= 0 (:agenda-point (get-corp))))
      (is (= 0 (count (:scored (get-corp))))))
    (is (= "15 Minutes" (:title (first (:deck (get-corp))))))))

(deftest ancestral-imager
  ;; Ancestral Imager - damage on jack out
  (do-game
    (new-game (default-corp [(qty "Ancestral Imager" 3)])
              (default-runner))
    (play-from-hand state :corp "Ancestral Imager" "New remote")
    (let [ai (get-content state :remote1 0)]
      (score-agenda state :corp ai)
      (take-credits state :corp)
      (is (= 3 (count(get-in @state [:runner :hand]))) "Runner has 3 cards in hand")
      (run-on state :hq)
      (run-jack-out state)
      (is (= 2 (count(get-in @state [:runner :hand]))) "Runner took 1 net damage"))))

(deftest astro-script-token
  ;; AstroScript token placement
  (do-game
    (new-game (default-corp [(qty "AstroScript Pilot Program" 3) (qty "Ice Wall" 2)])
              (default-runner))
    (core/gain state :corp :click 3)
    (letfn [(try-place [from to]
              (card-ability state :corp (refresh from) 0)
              (prompt-select :corp (refresh to)))
            (should-not-place [from to msg]
              (try-place from to)
              (prompt-choice :corp "Done")
              (is (= 1 (get-counters (refresh from) :agenda))
                  (str (:title from)" token was not used on " (:title to) msg))
              (is (or (= nil (:advance-counter (refresh to)))
                      (= 0 (:advance-counter (refresh to))))
                  (str "Advancement token not placed on " (:title to) msg)))
            (should-place [from to msg]
              (try-place from to)
              (is (= 0 (get-counters (refresh from) :agenda))
                  (str (:title from) " token was used on " (:title to) msg))
              (is (= 1 (:advance-counter (refresh to)))
                  (str "Advancement token placed on " (:title to) msg)))]
      (play-from-hand state :corp "AstroScript Pilot Program" "New remote")
      (score-agenda state :corp (get-content state :remote1 0))
      (play-from-hand state :corp "AstroScript Pilot Program" "New remote")
      (let [scored-astro (get-in @state [:corp :scored 0])
            installed-astro (get-content state :remote2 0)
            hand-astro (find-card "AstroScript Pilot Program" (:hand get-corp))]
        (should-not-place scored-astro hand-astro " in hand")
        (should-place scored-astro installed-astro " that is installed")
        (core/advance state :corp {:card (refresh installed-astro)})
        (core/advance state :corp {:card (refresh installed-astro)})
        (core/score   state :corp {:card (refresh installed-astro)}))
      (play-from-hand state :corp "Ice Wall" "HQ")
      (let [no-token-astro (get-in @state [:corp :scored 0])
            token-astro (get-in @state [:corp :scored 1])
            hand-ice-wall (find-card "Ice Wall" (:hand get-corp))
            installed-ice-wall (get-ice state :hq 0)]
        (should-not-place token-astro no-token-astro " that is scored")
        (should-not-place token-astro hand-ice-wall " in hand")
        (should-place token-astro installed-ice-wall " that is installed")))))

(deftest braintrust
  ;; Braintrust - Discount ICE rez by 1 for every 2 over-advancements when scored
  (do-game
    (new-game (default-corp [(qty "Braintrust" 1) (qty "Ichi 1.0" 1)])
              (default-runner))
    (play-from-hand state :corp "Braintrust" "New remote")
    (let [bt (get-content state :remote1 0)]
      (core/add-prop state :corp bt :advance-counter 7)
      (core/score state :corp {:card (refresh bt)})
      (let [scored-bt (get-in @state [:corp :scored 0])]
        (is (= 2 (get-counters (refresh scored-bt) :agenda))
            "Scored w/ 4 over-advancements; 2 agenda counters")
        (play-from-hand state :corp "Ichi 1.0" "HQ")
        (core/rez state :corp (get-ice state :hq 0))
        (is (= 2 (:credit (get-corp))) "2c discount to rez Ichi")))))

(deftest breaking-news
  ;; Test scoring breaking news
  (do-game
    (new-game (default-corp [(qty "Breaking News" 3)])
              (default-runner))
    (play-from-hand state :corp "Breaking News" "New remote")
    (score-agenda state :corp (get-content state :remote1 0))
    (is (= 2 (get-in @state [:runner :tag])) "Runner receives 2 tags from Breaking News")
    (take-credits state :corp)
    (is (= 0 (get-in @state [:runner :tag]))) "Two tags removed at the end of the turn"))

(deftest character-assassination
  ;; Character Assassination - Unpreventable trash of 1 resource when scored
  (do-game
    (new-game (default-corp [(qty "Character Assassination" 1)])
              (default-runner [(qty "Fall Guy" 1) (qty "Kati Jones" 1)]))
    (take-credits state :corp)
    (play-from-hand state :runner "Kati Jones")
    (play-from-hand state :runner "Fall Guy")
    (take-credits state :runner)
    (play-from-hand state :corp "Character Assassination" "New remote")
    (score-agenda state :corp (get-content state :remote1 0))
    (let [kati (get-in @state [:runner :rig :resource 0])]
      (prompt-select :corp kati)
      (is (empty? (:prompt (get-runner))) "Fall Guy prevention didn't occur")
      (is (= 1 (count (:discard (get-runner)))) "Kati Jones trashed"))))
	  
(deftest corporate-sales-team
  ;; Corporate Sales Team - Places 10c on card, corp takes 1c on each turn start
  (do-game
    (new-game (default-corp [(qty "Corporate Sales Team" 2)])
              (default-runner))
    (play-from-hand state :corp "Corporate Sales Team" "New remote")
    (is (= 5 (:credit (get-corp))))
    (score-agenda state :corp (get-content state :remote1 0))
	(let [scored-cst (get-in @state [:corp :scored 0])]
	  (core/end-turn state :corp nil)
	  (core/start-turn state :runner nil)	
	  (is (= 6 (:credit (get-corp))) "Increments at runner's start of turn")
	  (is (= 9 (get-counters (refresh scored-cst) :credit)))
	  (core/end-turn state :runner nil)
	  (core/start-turn state :corp nil)	
	  (is (= 7 (:credit (get-corp))) "Increments at corp's start of turn")
	  (is (= 8 (get-counters (refresh scored-cst) :credit)))
	)))

(deftest corporate-war
  ;; Corporate War - Gain 7c if you have 7c or more when scoring, otherwise lose all credits
  (do-game
    (new-game (default-corp [(qty "Corporate War" 2)])
              (default-runner))
    (play-from-hand state :corp "Corporate War" "New remote")
    (is (= 5 (:credit (get-corp))))
    (score-agenda state :corp (get-content state :remote1 0))
    (is (= 0 (:credit (get-corp))) "Lost all credits")
    (core/gain state :corp :credit 7)
    (play-from-hand state :corp "Corporate War" "New remote")
    (score-agenda state :corp (get-content state :remote2 0))
    (is (= 14 (:credit (get-corp))) "Had 7 credits when scoring, gained another 7")))

(deftest crisis-management
  ;; Crisis Management - Do 1 meat damage at turn start if Runner is tagged
  (do-game
    (new-game (default-corp [(qty "Crisis Management" 1)])
              (default-runner))
    (play-from-hand state :corp "Crisis Management" "New remote")
    (score-agenda state :corp (get-content state :remote1 0))
    (take-credits state :corp)
    (take-credits state :runner)
    (is (= 3 (count (:hand (get-runner)))) "No damage done, Runner not tagged")
    (take-credits state :corp)
    (core/gain state :runner :tag 1)
    (take-credits state :runner)
    (is (= 2 (count (:hand (get-runner)))) "Crisis Management dealt 1 meat damage")))

(deftest dedicated-neural-net
  ;; Dedicated Neural Net
  (do-game
    (new-game (default-corp [(qty "Dedicated Neural Net" 1) (qty "Scorched Earth" 2)
                             (qty "Hedge Fund" 1) "Caprice Nisei"])
              (default-runner [(qty "HQ Interface" 1)]))
    (play-from-hand state :corp "Dedicated Neural Net" "New remote")
    (play-from-hand state :corp "Caprice Nisei" "HQ")
    (score-agenda state :corp (get-content state :remote1 0))
    (take-credits state :corp)
    (run-empty-server state :hq)
    (prompt-choice :runner "0")
    (prompt-choice :corp "1")
    (is (-> @state :run :run-effect :replace-access) "Replace-access tiggered")
    (prompt-select :corp (find-card "Hedge Fund" (:hand (get-corp))))
    (prompt-choice :runner "Card from hand")
    (is (accessing state "Hedge Fund") "Runner accessing Hedge Fund")
    (prompt-choice :runner "OK")
    ;; test for #2376
    (prompt-choice :runner "Unrezzed upgrade in HQ")
    (is (accessing state "Caprice Nisei") "Runner accessing Caprice")
    (prompt-choice :runner "No")
    (is (not (:run @state)) "Run completed")
    (run-empty-server state :hq)
    (prompt-choice :runner "OK")
    (take-credits state :runner)
    (take-credits state :corp)
    (play-from-hand state :runner "HQ Interface")
    (run-empty-server state :hq)
    (prompt-choice :runner "0")
    (prompt-choice :corp "1")
    (is (= 2 (-> (get-corp) :selected first :max)) "Corp chooses 2 cards for Runner to access")))

(deftest eden-fragment
  ;; Test that Eden Fragment ignores the install cost of the first ice
  (do-game
    (new-game (default-corp [(qty "Eden Fragment" 3) (qty "Ice Wall" 3)])
              (default-runner))
    (play-from-hand state :corp "Ice Wall" "HQ")
    (play-from-hand state :corp "Eden Fragment" "New remote")
    (score-agenda state :corp (get-content state :remote1 0))
    (take-credits state :corp)
    (take-credits state :runner)
    (take-credits state :runner)
    (take-credits state :runner)
    (take-credits state :runner)
    (play-from-hand state :corp "Ice Wall" "HQ")
    (is (not (nil? (get-ice state :hq 1))) "Corp has two ice installed on HQ")
    (is (= 6 (get-in @state [:corp :credit])) "Corp does not pay for installing the first ICE of the turn")
    (play-from-hand state :corp "Ice Wall" "HQ")
    (is (not (nil? (get-ice state :hq 2))) "Corp has three ice installed on HQ")
    (is (= 4 (get-in @state [:corp :credit])) "Corp pays for installing the second ICE of the turn")))

(deftest efficiency-committee
  ;; Efficiency Committee - Cannot advance cards if agenda counter is used
  (do-game
    (new-game (default-corp [(qty "Efficiency Committee" 3) (qty "Shipment from SanSan" 2)
                             (qty "Ice Wall" 1)])
              (default-runner))
    (core/gain state :corp :click 4)
    (play-from-hand state :corp "Efficiency Committee" "New remote")
    (play-from-hand state :corp "Efficiency Committee" "New remote")
    (play-from-hand state :corp "Efficiency Committee" "New remote")
    (play-from-hand state :corp "Ice Wall" "HQ")
    (let [ec1 (get-content state :remote1 0)
          ec2 (get-content state :remote2 0)
          ec3 (get-content state :remote3 0)
          iw (get-ice state :hq 0)]
      (score-agenda state :corp ec1)
      (let [ec1_scored (get-in @state [:corp :scored 0])]
        (is (= 3 (get-counters (refresh ec1_scored) :agenda)))
        (is (= 2 (:agenda-point (get-corp))))
        ;; use token
        (is (= 3 (:click (get-corp))))
        (card-ability state :corp ec1_scored 0)
        (is (= 4 (:click (get-corp))))
        ;; try to advance Ice Wall
        (core/advance state :corp {:card (refresh iw)})
        (is (= 4 (:click (get-corp))))
        (is (= nil (:advance-counter (refresh iw))))
        ;; try to advance Efficiency Committee
        (core/advance state :corp {:card (refresh ec2)})
        (is (= 4 (:click (get-corp))))
        (is (= nil (:advance-counter (refresh ec2))))
        ;; advance with Shipment from SanSan
        (play-from-hand state :corp "Shipment from SanSan")
        (prompt-choice :corp "2")
        (prompt-select :corp ec2)
        (is (= 2 (:advance-counter (refresh ec2))))
        (play-from-hand state :corp "Shipment from SanSan")
        (prompt-choice :corp "2")
        (prompt-select :corp ec2)
        (is (= 4 (:advance-counter (refresh ec2))))
        (core/score state :corp {:card (refresh ec2)})
        (is (= 4 (:agenda-point (get-corp))))
        (take-credits state :corp)
        (take-credits state :runner)
        ;; can advance again
        (core/advance state :corp {:card (refresh iw)})
        (is (= 1 (:advance-counter (refresh iw))))
        (core/advance state :corp {:card (refresh ec3)})
        (is (= 1 (:advance-counter (refresh ec3))))))))

(deftest explode-a-palooza
  ;; Explode-a-palooza - Gain 5 credits when Runner accesses it
  (do-game
    (new-game (default-corp [(qty "Explode-a-palooza" 1)])
              (default-runner))
    (play-from-hand state :corp "Explode-a-palooza" "New remote")
    (take-credits state :corp)
    (run-empty-server state :remote1)
    (prompt-choice :runner "Access")
    (prompt-choice :runner "Steal")
    (prompt-choice :corp "Yes")
    (is (= 12 (:credit (get-corp))) "Gained 5 credits")))

(deftest explode-ttw
  ;; Explode-a-palooza - Interaction with The Turning Wheel. Issue #1717.
  (do-game
    (new-game (default-corp [(qty "Explode-a-palooza" 3)])
              (default-runner [(qty "The Turning Wheel" 1)]))
    (starting-hand state :corp ["Explode-a-palooza" "Explode-a-palooza"])
    (play-from-hand state :corp "Explode-a-palooza" "New remote")
    (take-credits state :corp)
    (play-from-hand state :runner "The Turning Wheel")
    (run-empty-server state :remote1)
    (prompt-choice :runner "Access")
    (prompt-choice :corp "Yes")
    (prompt-choice :runner "Steal")
    (let [ttw (get-resource state 0)]
      (is (= 0 (get-counters (refresh ttw) :power)) "TTW did not gain counters")
      (is (= 1 (count (:scored (get-runner)))) "Runner stole Explodapalooza")
      (is (= 12 (:credit (get-corp))) "Gained 5 credits")
      (run-empty-server state :rd)
      (prompt-choice :runner "Access")
      (prompt-choice :corp "Yes")
      (prompt-choice :runner "Steal")
      (is (= 0 (get-counters (refresh ttw) :power)) "TTW did not gain counters")
      (is (= 2 (count (:scored (get-runner)))) "Runner stole Explodapalooza")
      (is (= 17 (:credit (get-corp))) "Gained 5 credits"))))

(deftest fetal-ai-damage
  ;; Fetal AI - damage on access
  (do-game
    (new-game (default-corp [(qty "Fetal AI" 3)])
              (default-runner [(qty "Sure Gamble" 3) (qty "Diesel" 3) (qty "Quality Time" 3)]))
    (play-from-hand state :corp "Fetal AI" "New remote")
    (take-credits state :corp 2)
    (run-empty-server state "Server 1")
    (prompt-choice :runner "Access")
    (prompt-choice :runner "Yes")
    (is (= 3 (count (:hand (get-runner)))) "Runner took 2 net damage from Fetal AI")
    (is (= 3 (:credit (get-runner))) "Runner paid 2cr to steal Fetal AI")
    (is (= 1 (count (:scored (get-runner)))) "Runner stole Fetal AI")))

(deftest fetal-ai-cant-afford
  ;; Fetal AI - can't afford to steal
  (do-game
    (new-game (default-corp [(qty "Fetal AI" 3)])
              (default-runner [(qty "Sure Gamble" 3) (qty "Diesel" 3) (qty "Quality Time" 3)]))
    (play-from-hand state :corp "Fetal AI" "New remote")
    (take-credits state :corp 2)
    (core/lose state :runner :credit 5)
    (run-empty-server state "Server 1")
    (prompt-choice :runner "Yes")
    (is (= 3 (count (:hand (get-runner)))) "Runner took 2 net damage from Fetal AI")
    (is (= 0 (count (:scored (get-runner)))) "Runner could not steal Fetal AI")))

(deftest genetic-resequencing
  ;; Genetic Resequencing - Place 1 agenda counter on a scored agenda
  (do-game
    (new-game (default-corp [(qty "Genetic Resequencing" 1) (qty "Braintrust" 2)])
              (default-runner))
    (play-from-hand state :corp "Braintrust" "New remote")
    (play-from-hand state :corp "Braintrust" "New remote")
    (play-from-hand state :corp "Genetic Resequencing" "New remote")
    (let [bt1 (get-content state :remote1 0)
          bt2 (get-content state :remote2 0)
          gr (get-content state :remote3 0)]
      (score-agenda state :corp bt1)
      (let [btscored (get-in @state [:corp :scored 0])]
        (is (= 0 (get-counters (refresh btscored) :agenda)) "No agenda counters on scored Braintrust")
        (score-agenda state :corp gr)
        (prompt-select :corp bt2)
        (is (zero? (get-counters (refresh bt2) :agenda))
            "No agenda counters on installed Braintrust; not a valid target")
        (prompt-select :corp btscored)
        (is (= 1 (get-counters (refresh btscored) :agenda))
            "1 agenda counter placed on scored Braintrust")))))

(deftest government-contracts
  ;; Government Contracts - Spend 2 clicks for 4 credits
  (do-game
    (new-game (default-corp [(qty "Government Contracts" 1)])
              (default-runner))
    (play-from-hand state :corp "Government Contracts" "New remote")
    (score-agenda state :corp (get-content state :remote1 0))
    (is (= 2 (:click (get-corp))))
    (card-ability state :corp (get-in @state [:corp :scored 0]) 0)
    (is (= 0 (:click (get-corp))) "Spent 2 clicks")
    (is (= 9 (:credit (get-corp))) "Gained 4 credits")))

(deftest high-risk-investment
  ;; High-Risk Investment - Gain 1 agenda counter when scored; spend it to gain credits equal to Runner's credits
  (do-game
    (new-game (default-corp [(qty "High-Risk Investment" 1)])
              (default-runner))
    (play-from-hand state :corp "High-Risk Investment" "New remote")
    (let [hri (get-content state :remote1 0)]
      (score-agenda state :corp hri)
      (let [hriscored (get-in @state [:corp :scored 0])]
        (is (= 1 (get-counters (refresh hriscored) :agenda)) "Has 1 agenda counter")
        (take-credits state :corp)
        (is (= 7 (:credit (get-corp))))
        (take-credits state :runner)
        (is (= 9 (:credit (get-runner))))
        (card-ability state :corp hriscored 0)
        (is (= 16 (:credit (get-corp))) "Gained 9 credits")
        (is (= 2 (:click (get-corp))) "Spent 1 click")
        (is (= 0 (get-counters (refresh hriscored) :agenda)) "Spent agenda counter")))))

(deftest hostile-takeover
  ;; Hostile Takeover - Gain 7 credits and take 1 bad publicity
  (do-game
    (new-game (default-corp [(qty "Hostile Takeover" 1)])
              (default-runner))
    (play-from-hand state :corp "Hostile Takeover" "New remote")
    (let [ht (get-content state :remote1 0)]
      (score-agenda state :corp ht)
      (is (= 12 (:credit (get-corp))) "Gain 7 credits")
      (is (= 1 (:bad-publicity (get-corp))) "Take 1 bad publicity"))))

(deftest labyrinthine-servers
  ;; Labyrinthine Servers - Prevent the Runner from jacking out as long as there is still a power counter
  (do-game
    (new-game (default-corp [(qty "Labyrinthine Servers" 2)])
              (default-runner))
    (play-from-hand state :corp "Labyrinthine Servers" "New remote")
    (play-from-hand state :corp "Labyrinthine Servers" "New remote")
    (score-agenda state :corp (get-content state :remote1 0))
    (score-agenda state :corp (get-content state :remote2 0))
    (take-credits state :corp)
    (let [ls1 (get-in @state [:corp :scored 0])
          ls2 (get-in @state [:corp :scored 1])]
      (is (= 2 (get-counters (refresh ls1) :power)))
      (is (= 2 (get-counters (refresh ls2) :power)))
      ;; don't use token
      (run-on state "HQ")
      (run-jack-out state)
      (is (:run @state) "Jack out prevent prompt")
      (prompt-choice :corp "Done")
      (is (not (:run @state)) "Corp does not prevent the jack out, run ends")
      ;; use token
      (run-on state "HQ")
      (run-jack-out state)
      (card-ability state :corp ls1 0)
      (card-ability state :corp ls2 0)
      (card-ability state :corp ls1 0)
      (prompt-choice :corp "Done")
      (is (:run @state) "Jack out prevented, run is still ongoing")
      (is (true? (get-in @state [:run :cannot-jack-out])) "Cannot jack out flag is in effect")
      (run-successful state)
      (is (not (:run @state)))
      ;; one Labyrinthine is empty but the other still has one token, ensure prompt still occurs
      (is (= 0 (get-counters (refresh ls1) :power)))
      (is (= 1 (get-counters (refresh ls2) :power)))
      (run-on state "HQ")
      (run-jack-out state)
      (is (:run @state))
      (card-ability state :corp ls2 0)
      (prompt-choice :corp "Done")
      (is (true? (get-in @state [:run :cannot-jack-out])))
      (run-successful state)
      (is (not (:run @state)))
      ;; no more tokens
      (run-on state "HQ")
      (run-jack-out state)
      (is (not (:run @state)) "No jack out prevent prompt"))))

(deftest medical-breakthrough
  ;; Medical Breakthrough - Lower advancement requirement by 1 for each scored/stolen copy
  (do-game
    (new-game (default-corp [(qty "Medical Breakthrough" 3) (qty "Hedge Fund" 3)])
              (default-runner))
    (play-from-hand state :corp "Medical Breakthrough" "New remote")
    (play-from-hand state :corp "Medical Breakthrough" "New remote")
    (play-from-hand state :corp "Hedge Fund")
    (take-credits state :corp)
    (run-empty-server state :remote1)
    (prompt-choice :runner "Steal")
    (take-credits state :runner)
    (let [mb2 (get-content state :remote2 0)]
      (core/advance state :corp {:card (refresh mb2)})
      (core/advance state :corp {:card (refresh mb2)})
      (core/advance state :corp {:card (refresh mb2)})
      (core/score state :corp {:card (refresh mb2)})
      (is (= 2 (:agenda-point (get-corp))) "Only needed 3 advancements to score")
      (take-credits state :corp)
      (take-credits state :runner)
      (play-from-hand state :corp "Medical Breakthrough" "New remote")
      (let [mb3 (get-content state :remote3 0)]
        (core/advance state :corp {:card (refresh mb3)})
        (core/advance state :corp {:card (refresh mb3)})
        (core/score state :corp {:card (refresh mb3)})
        (is (= 4 (:agenda-point (get-corp))) "Only needed 2 advancements to score")))))

(deftest napd-contract
  ;; NAPD Contract - Requires 4 credits to steal; scoring requirement increases with bad publicity
  (do-game
    (new-game (default-corp [(qty "NAPD Contract" 1)])
              (default-runner))
    (play-from-hand state :corp "NAPD Contract" "New remote")
      (let [napd (get-content state :remote1 0)]
        (core/advance state :corp {:card (refresh napd)})
        (core/advance state :corp {:card (refresh napd)})
        (take-credits state :corp)
        (core/lose state :runner :credit 2)
        (run-empty-server state "Server 1")
        (prompt-choice :runner "Yes")
        (is (= 0 (count (:scored (get-runner)))) "Runner could not steal NAPD Contract")
        (is (= 3 (:credit (get-runner))) "Runner couldn't afford to steal, so no credits spent")
        (take-credits state :runner)
        (core/gain state :corp :bad-publicity 1)
        (core/advance state :corp {:card (refresh napd)})
        (core/advance state :corp {:card (refresh napd)})
        (core/score state :corp {:card (refresh napd)})
        (is (not (nil? (get-content state :remote1 0)))
            "Corp can't score with 4 advancements because of BP")
        (core/advance state :corp {:card (refresh napd)})
        (core/score state :corp {:card (refresh napd)})
        (is (= 2 (:agenda-point (get-corp))) "Scored NAPD for 2 points after 5 advancements"))))

(deftest napd-contract-corporate-scandal
  ;; NAPD Contract - scoring requirement increases with bad publicity from Corporate Scandal
  (do-game
    (new-game (default-corp [(qty "NAPD Contract" 1)])
              (default-runner [(qty "Corporate Scandal" 1)]))
    (play-from-hand state :corp "NAPD Contract" "New remote")
      (let [napd (get-content state :remote1 0)]
        (core/advance state :corp {:card (refresh napd)})
        (core/advance state :corp {:card (refresh napd)})
        (take-credits state :corp)
        (play-from-hand state :runner "Corporate Scandal")
        (take-credits state :runner)
        (core/advance state :corp {:card (refresh napd)})
        (core/advance state :corp {:card (refresh napd)})
        (core/score state :corp {:card (refresh napd)})
        (is (not (nil? (get-content state :remote1 0)))
            "Corp can't score with 4 advancements because of BP")
        (core/advance state :corp {:card (refresh napd)})
        (core/score state :corp {:card (refresh napd)})
        (is (= 2 (:agenda-point (get-corp))) "Scored NAPD for 2 points after 5 advancements"))))

(deftest net-quarantine
  ;; The Runner's base link strength is reduced to 0 during the first trace each turn.
  ;; Whenever the Runner increases his or her link strength by spending credits, gain 1 for every 2 spent.
  (do-game
    (new-game (default-corp [(qty "Net Quarantine" 1)])
              (default-runner))
    (core/gain state :runner :link 1)
    (core/gain state :corp :click 3)
    (play-from-hand state :corp "Net Quarantine" "New remote")
    (score-agenda state :corp (get-content state :remote1 0))
    (is (= 5 (:credit (get-corp))) "Corp has 5 credits")
    (is (= 1 (:link (get-runner))) "Runner has 1 link")
    (core/corp-trace-prompt state {:title "/trace command" :side :corp} {:base 1})
    (prompt-choice :corp 0)
    (is (= 0 (:link (get-runner))) "Runner has 0 link")
    (prompt-choice :runner 3)
    (is (= 1 (:link (get-runner))) "Runner has 1 link again")
    (is (= 6 (:credit (get-corp))) "Corp gained a credit from NQ")
    ; second trace of turn - no link reduction
    (core/corp-trace-prompt state {:title "/trace command" :side :corp} {:base 1})
    (prompt-choice :corp 0)
    (is (= 1 (:link (get-runner))) "Runner has 1 link")
    (prompt-choice :runner 2)
    (is (= 7 (:credit (get-corp))) "Corp gained a credit from NQ")))

(deftest nisei-mk-ii-step-43
  ;; Nisei MK II - Remove hosted counter to ETR, check this works in 4.3
  (do-game
   (new-game (default-corp [(qty "Nisei MK II" 1)])
             (default-runner))
   (play-from-hand state :corp "Nisei MK II" "New remote")
   (score-agenda state :corp (get-content state :remote1 0))
   (let [scored-nisei (get-in @state [:corp :scored 0])]
     (is (= 1 (get-counters (refresh scored-nisei) :agenda)) "Scored Nisei has one counter")
     (take-credits state :corp)

     (run-on state "HQ")
     (run-phase-43 state)
     (card-ability state :corp (refresh scored-nisei) 0)
     (prompt-choice :corp "Done") ; close 4.3 corp
     (is (not (:run @state)) "Run ended by using Nisei counter")
     (is (= 0 (get-counters (refresh scored-nisei) :agenda)) "Scored Nisei has no counters"))))

(deftest oaktown-renovation
  ;; Oaktown Renovation - Installed face up, gain credits with each conventional advancement
  (do-game
    (new-game (default-corp [(qty "Oaktown Renovation" 1) (qty "Shipment from SanSan" 1)])
              (default-runner))
    (core/gain state :corp :click 3)
    (play-from-hand state :corp "Oaktown Renovation" "New remote")
    (let [oak (get-content state :remote1 0)]
      (is (get-in (refresh oak) [:rezzed]) "Oaktown installed face up")
      (core/advance state :corp {:card (refresh oak)})
      (is (= 6 (:credit (get-corp))) "Spent 1 credit to advance, gained 2 credits from Oaktown")
      (play-from-hand state :corp "Shipment from SanSan")
      (prompt-choice :corp "2")
      (prompt-select :corp oak)
      (is (= 3 (:advance-counter (refresh oak))))
      (is (= 6 (:credit (get-corp))) "No credits gained due to advancements being placed")
      (core/advance state :corp {:card (refresh oak)})
      (is (= 7 (:credit (get-corp))) "Spent 1 credit to advance, gained 2 credits from Oaktown")
      (core/advance state :corp {:card (refresh oak)})
      (is (= 5 (:advance-counter (refresh oak))))
      (is (= 9 (:credit (get-corp)))
          "Spent 1 credit to advance, gained 3 credits from Oaktown"))))

(deftest obokata-protocol
  ;; Pay 4 net damage to steal.  Runner win retained on flatline
  (do-game
    (new-game (make-deck "Jinteki: Personal Evolution" [(qty "Obokata Protocol" 10)])
              (default-runner [(qty "Sure Gamble" 4)]))
    (play-from-hand state :corp "Obokata Protocol" "New remote")
    (take-credits state :corp)
    (core/gain state :runner :agenda-point 6)
    (run-empty-server state "Server 1")
    (prompt-choice :runner "Yes")
    (is (= 4 (count (:discard (get-runner)))) "Runner paid 4 net damage")
    (is (= :runner (:winner @state)) "Runner wins")
    (is (= "Agenda" (:reason @state)) "Win condition reports agenda points")
    (is (last-log-contains? state "wins the game") "PE did not fire")))

(deftest personality-profiles
  ;; Personality Profiles - Full test
  (do-game
    (new-game (default-corp [(qty "Personality Profiles" 1)])
              (default-runner [(qty "Self-modifying Code" 1) (qty "Clone Chip" 1)
                               (qty "Corroder" 1) (qty "Patron" 2)]))
    (starting-hand state :runner ["Self-modifying Code" "Clone Chip" "Patron" "Patron"])
    (score-agenda state :corp (find-card "Personality Profiles" (:hand (get-corp))))
    (take-credits state :corp)
    (play-from-hand state :runner "Self-modifying Code")
    (play-from-hand state :runner "Clone Chip")
    (let [smc (get-in @state [:runner :rig :program 0])]
      (card-ability state :runner smc 0)
      (prompt-choice :runner (find-card "Corroder" (:deck (get-runner))))
      (is (= 2 (count (:discard (get-runner))))))
    (let [chip (get-in @state [:runner :rig :hardware 0])]
      (card-ability state :runner chip 0)
      (prompt-select :runner (find-card "Self-modifying Code" (:discard (get-runner))))
      (is (second-last-log-contains? state "Patron")
          "Personality Profiles trashed card name is in log")
      (is (= 3 (count (:discard (get-runner))))))))

(deftest personality-profiles-empty-hand
  ;; Personality Profiles - Ensure effects still fire with an empty hand, #1840
  (do-game
    (new-game (default-corp [(qty "Personality Profiles" 1)])
              (default-runner [(qty "Self-modifying Code" 1) (qty "Clone Chip" 1)
                               (qty "Corroder" 1)]))
    (starting-hand state :runner ["Self-modifying Code" "Clone Chip"])
    (score-agenda state :corp (find-card "Personality Profiles" (:hand (get-corp))))
    (take-credits state :corp)
    (play-from-hand state :runner "Self-modifying Code")
    (play-from-hand state :runner "Clone Chip")
    (let [smc (get-in @state [:runner :rig :program 0])]
      (card-ability state :runner smc 0)
      (prompt-choice :runner (find-card "Corroder" (:deck (get-runner))))
      (let [cor (get-in @state [:runner :rig :program 0])]
        (is (not (nil? cor)))
        (is (= (:title cor) "Corroder"))
        (is (= "Self-modifying Code" (:title (first (:discard (get-runner))))))))
    (let [chip (get-in @state [:runner :rig :hardware 0])]
      (card-ability state :runner chip 0)
      (prompt-select :runner (find-card "Self-modifying Code" (:discard (get-runner))))
      (let [smc (get-in @state [:runner :rig :program 1])]
        (is (not (nil? smc)))
        (is (= (:title smc) "Self-modifying Code"))
        (is (= "Clone Chip" (:title (first (:discard (get-runner))))))))))

(deftest philotic-entanglement
  ;; Philotic Entanglement - When scored, do 1 net damage for each agenda in the Runner's score area
  (do-game
    (new-game (default-corp [(qty "Philotic Entanglement" 1) (qty "House of Knives" 3)])
              (default-runner [(qty "Sure Gamble" 3) (qty "Cache" 2)]))
    (play-from-hand state :corp "House of Knives" "New remote")
    (play-from-hand state :corp "House of Knives" "New remote")
    (play-from-hand state :corp "House of Knives" "New remote")
    (take-credits state :corp)
    (run-empty-server state :remote1)
    (prompt-choice :runner "Steal")
    (run-empty-server state :remote2)
    (prompt-choice :runner "Steal")
    (run-empty-server state :remote3)
    (prompt-choice :runner "Steal")
    (is (= 3 (count (:scored (get-runner)))))
    (take-credits state :runner)
    (play-from-hand state :corp "Philotic Entanglement" "New remote")
    (score-agenda state :corp (get-content state :remote4 0))
    (is (= 2 (:agenda-point (get-corp))))
    (is (= 3 (count (:discard (get-runner)))) "Dealt 3 net damage upon scoring")))
	  
(deftest posted-bounty-yes
  ;; Posted Bounty - Forfeiting takes 1 bad publicity
  (do-game
    (new-game (default-corp [(qty "Posted Bounty" 1)])
              (default-runner))
    (play-from-hand state :corp "Posted Bounty" "New remote")
    (let [pb (get-content state :remote1 0)]
      (score-agenda state :corp pb)
	  (prompt-choice :corp "Yes")
	  (is (= 0 (:agenda-point (get-corp))) "Forfeiting Posted Bounty nullifies agenda points")
      (is (= 1 (:bad-publicity (get-corp))) "Forfeiting takes 1 bad publicity"))
	  (is (= 1 (get-in @state [:runner :tag])) "Runner receives 1 tag forfeiting Posted Bounty")))
	  
(deftest posted-bounty-no
  ;; Posted Bounty - Choosing not to forfeit scores normally
  (do-game
    (new-game (default-corp [(qty "Posted Bounty" 1)])
              (default-runner))
    (play-from-hand state :corp "Posted Bounty" "New remote")
    (let [pb (get-content state :remote1 0)]
      (score-agenda state :corp pb)
	  (prompt-choice :corp "No")
	  (is (= 1 (:agenda-point (get-corp))))
      (is (= 0 (:bad-publicity (get-corp)))))
	  (is (= 0 (get-in @state [:runner :tag])))))

(deftest profiteering
  ;; Profiteering - Gain 5 credits per bad publicity taken
  (do-game
    (new-game (default-corp [(qty "Profiteering" 1)])
              (default-runner))
    (play-from-hand state :corp "Profiteering" "New remote")
    (let [prof (get-content state :remote1 0)]
      (score-agenda state :corp prof)
      (prompt-choice :corp "3")
      (is (= 1 (:agenda-point (get-corp))))
      (is (= 3 (:bad-publicity (get-corp))) "Took 3 bad publicity")
      (is (= 20 (:credit (get-corp))) "Gained 15 credits"))))

(deftest project-ares
  ;; Project Ares - Full test
  (do-game
    (new-game (default-corp [(qty "Project Ares" 2)])
              (default-runner [(qty "Clone Chip" 1)]))
    (take-credits state :corp)
    (play-from-hand state :runner "Clone Chip")
    (take-credits state :runner)
    (score-agenda state :corp (find-card "Project Ares" (:hand (get-corp))))
    (is (empty? (get-in @state [:runner :prompt])) "No prompt for Runner if scored with 4 advancement tokens")
    (core/gain state :corp :click 4)
    (play-from-hand state :corp "Project Ares" "New remote")
    (let [ares (get-content state :remote1 0)]
      (core/advance state :corp {:card (refresh ares)})
      (core/advance state :corp {:card (refresh ares)})
      (core/advance state :corp {:card (refresh ares)})
      (core/advance state :corp {:card (refresh ares)})
      (core/advance state :corp {:card (refresh ares)})
      (core/advance state :corp {:card (refresh ares)})
      (is (= 6 (:advance-counter (refresh ares)))
      (core/score state :corp {:card (refresh ares)}))
      (is (prompt-is-card? :runner ares) "Runner has Ares prompt to trash installed cards"))
    (prompt-select :runner (find-card "Clone Chip" (:hardware (:rig (get-runner)))))
    (is (empty? (get-in @state [:runner :prompt])) "Runner must trash 2 cards but only has 1 card in rig, prompt ended")
    (is (= 1 (count (:discard (get-runner)))))
    (is (= 1 (:bad-publicity (get-corp))))))

(deftest project-beale
  ;; Project Beale - Extra agenda points for over-advancing
  (do-game
    (new-game (default-corp [(qty "Project Beale" 2)])
              (default-runner))
    (core/gain state :corp :click 8 :credit 8)
    (play-from-hand state :corp "Project Beale" "New remote")
    (let [pb1 (get-content state :remote1 0)]
      (core/advance state :corp {:card (refresh pb1)})
      (core/advance state :corp {:card (refresh pb1)})
      (core/advance state :corp {:card (refresh pb1)})
      (core/advance state :corp {:card (refresh pb1)})
      (core/score state :corp {:card (refresh pb1)})
      (is (= 2 (:agenda-point (get-corp))) "Only 4 advancements: scored for standard 2 points")
      (play-from-hand state :corp "Project Beale" "New remote")
        (let [pb2 (get-content state :remote2 0)]
          (core/advance state :corp {:card (refresh pb2)})
          (core/advance state :corp {:card (refresh pb2)})
          (core/advance state :corp {:card (refresh pb2)})
          (core/advance state :corp {:card (refresh pb2)})
          (core/advance state :corp {:card (refresh pb2)})
          (core/score state :corp {:card (refresh pb2)})
          (is (= 5 (:agenda-point (get-corp))) "5 advancements: scored for 3 points")))))

(deftest puppet-master
  ;; Puppet Master - game progresses if no valid targets. Issue #1661.
  (do-game
    (new-game (default-corp [(qty "Puppet Master" 1)])
              (default-runner))
    (play-from-hand state :corp "Puppet Master" "New remote")
    (score-agenda state :corp (get-content state :remote1 0))
    (take-credits state :corp)
    (run-empty-server state :archives)
    (prompt-choice :corp "Done")
    (is (empty? (:prompt (get-runner))) "Runner's waiting prompt resolved")))

(deftest rebranding-team
  ;; Rebranding Team - Full test
  (do-game
    (new-game (default-corp [(qty "Rebranding Team" 1) (qty "Launch Campaign" 1) (qty "City Surveillance" 1)
                             (qty "Jackson Howard" 1) (qty "Museum of History" 1)])
              (default-runner))
    (score-agenda state :corp (find-card "Rebranding Team" (:hand (get-corp))))
    (is (core/has-subtype? (find-card "Launch Campaign" (:hand (get-corp))) "Advertisement"))
    (is (core/has-subtype? (find-card "City Surveillance" (:hand (get-corp))) "Advertisement"))
    (is (core/has-subtype? (find-card "Jackson Howard" (:hand (get-corp))) "Advertisement"))
    (is (core/has-subtype? (find-card "Jackson Howard" (:hand (get-corp))) "Executive"))
    (is (core/has-subtype? (find-card "Museum of History" (:hand (get-corp))) "Advertisement"))
    (is (core/has-subtype? (find-card "Museum of History" (:hand (get-corp))) "Alliance"))
    (is (core/has-subtype? (find-card "Museum of History" (:hand (get-corp))) "Ritzy"))
    (core/move state :corp (find-card "Rebranding Team" (:scored (get-corp))) :deck)
    (is (core/has-subtype? (find-card "Launch Campaign" (:hand (get-corp))) "Advertisement"))
    (is (not (core/has-subtype? (find-card "City Surveillance" (:hand (get-corp))) "Advertisement")))
    (is (not (core/has-subtype? (find-card "Jackson Howard" (:hand (get-corp))) "Advertisement")))
    (is (core/has-subtype? (find-card "Jackson Howard" (:hand (get-corp))) "Executive"))
    (is (not (core/has-subtype? (find-card "Museum of History" (:hand (get-corp))) "Advertisement")))
    (is (core/has-subtype? (find-card "Museum of History" (:hand (get-corp))) "Alliance"))
    (is (core/has-subtype? (find-card "Museum of History" (:hand (get-corp))) "Ritzy"))))

(deftest reeducation
  ;; Reeducation - Simple test
  (do-game
    (new-game (default-corp [(qty "Reeducation" 1) (qty "Sweeps Week" 1) (qty "Hedge Fund" 1)
                             (qty "Jackson Howard" 1) (qty "Gutenberg" 1)])
              (default-runner [(qty "Self-modifying Code" 1) (qty "Clone Chip" 1)
                               (qty "Corroder" 1) (qty "Sure Gamble" 1) (qty "Desperado" 1)]))
    (starting-hand state :corp ["Reeducation" "Sweeps Week"])
    (starting-hand state :runner ["Self-modifying Code"])
    (score-agenda state :corp (find-card "Reeducation" (:hand (get-corp))))
    (is (prompt-is-type? :runner :waiting) "Runner has wait prompt")
    (is (= 1 (count (get-in @state [:corp :hand]))))
    (is (= 1 (count (get-in @state [:runner :hand]))))
    (prompt-choice :corp (find-card "Sweeps Week" (:hand (get-corp)))) ; put Sweeps Week at bottom of R&D
    (prompt-choice :corp "Done") ; finished selecting cards
    (prompt-choice :corp "Done") ; corp prompt for Done/Start Over
    (is (= "Sweeps Week" (:title (last (:deck (get-corp))))))
    (is (= "Self-modifying Code" (:title (last (:deck (get-runner))))))
    (is (= 1 (count (get-in @state [:corp :hand]))))
    (is (= 0 (count (get-in @state [:runner :hand]))))))

(deftest reeducation-extra-cards
  ;; Reeducation - If Corp is adding more cards in HQ than Runner has in their Grip, Runner
  ;; is not 'able' to resolve the effect and doesn't have to add to bottom of Stack
  (do-game
    (new-game (default-corp [(qty "Reeducation" 1) (qty "Sweeps Week" 1) (qty "Hedge Fund" 1)
                             (qty "Jackson Howard" 1) (qty "Gutenberg" 1)])
              (default-runner [(qty "Self-modifying Code" 1) (qty "Clone Chip" 1)
                               (qty "Corroder" 1) (qty "Sure Gamble" 1) (qty "Desperado" 1)]))
    (starting-hand state :corp ["Reeducation" "Sweeps Week" "Hedge Fund"])
    (starting-hand state :runner ["Self-modifying Code"])
    (score-agenda state :corp (find-card "Reeducation" (:hand (get-corp))))
    (is (prompt-is-type? :runner :waiting) "Runner has wait prompt")
    (is (= 2 (count (get-in @state [:corp :hand]))))
    (is (= 1 (count (get-in @state [:runner :hand]))))
    (prompt-choice :corp (find-card "Sweeps Week" (:hand (get-corp))))
    (prompt-choice :corp (find-card "Hedge Fund" (:hand (get-corp)))) ; this is the bottom card of R&D
    (prompt-choice :corp "Done") ; finished selecting cards
    (prompt-choice :corp "Done") ; corp prompt for Done/Start Over
    (is (= "Hedge Fund" (:title (last (:deck (get-corp))))))
    (is (= "Sweeps Week" (:title (last (butlast (:deck (get-corp)))))))
    (is (= "Self-modifying Code" (:title (first (:hand (get-runner))))))
    (is (= 2 (count (get-in @state [:corp :hand]))))
    (is (= 1 (count (get-in @state [:runner :hand]))))))

(deftest sentinel-defense-program
  ;; Sentinel Defense Program - Doesn't fire if brain damage is prevented
  (do-game
    (new-game (default-corp [(qty "Sentinel Defense Program" 1) (qty "Viktor 1.0" 1)])
              (default-runner [(qty "Feedback Filter" 1) (qty "Sure Gamble" 3)]))
    (score-agenda state :corp (find-card "Sentinel Defense Program" (:hand (get-corp))))
    (play-from-hand state :corp "Viktor 1.0" "HQ")
    (take-credits state :corp)
    (play-from-hand state :runner "Feedback Filter")
    (let [viktor (get-ice state :hq 0)
          ff (get-in @state [:runner :rig :hardware 0])]
      (run-on state "HQ")
      (core/rez state :corp viktor)
      (card-subroutine state :corp viktor 0)
      (prompt-choice :runner "Done") ;don't prevent the brain damage
      (is (= 1 (count (:discard (get-runner)))))
      (is (= 1 (:brain-damage (get-runner))))
      (prompt-choice :runner "Done") ;so we take the net, but don't prevent it either
      (is (= 2 (count (:discard (get-runner)))))
      (card-subroutine state :corp viktor 0)
      (card-ability state :runner ff 1) ;prevent the brain damage this time
      (prompt-choice :runner "Done")
      (is (= 3 (count (:discard (get-runner)))) "Feedback filter trashed, didn't take another net damage")
      (is (= 1 (:brain-damage (get-runner)))))))

;; OHG still not working...
(deftest tgtbt
  ;; TGTBT - Give the Runner 1 tag when they access
  (do-game
    (new-game (default-corp [(qty "TGTBT" 2) (qty "Old Hollywood Grid" 1)])
              (default-runner))
    (play-from-hand state :corp "TGTBT" "New remote")
    (play-from-hand state :corp "Old Hollywood Grid" "Server 1")
    (play-from-hand state :corp "TGTBT" "New remote")
    (take-credits state :corp)
    (let [tg1 (get-content state :remote1 0)
          ohg (get-content state :remote1 1)]
      (run-on state "Server 1")
      (core/rez state :corp ohg)
      (run-successful state)
      (prompt-select :runner tg1)
      ;; Accesses TGTBT but can't steal
      (prompt-choice :runner "Access")
      (is (= 1 (:tag (get-runner))) "Runner took 1 tag from accessing without stealing")
      (prompt-select :runner ohg))
    (prompt-choice :runner "Yes") ; Trashes OHG
    (run-empty-server state "Server 2")
    ;; Accesses TGTBT and can steal
    (prompt-choice :runner "Access")
    (prompt-choice :runner "Steal")

    (is (= 2 (:tag (get-runner))) "Runner took 1 tag from accessing and stealing")))

(deftest the-cleaners
  ;; The Cleaners - Bonus damage
  (do-game
    (new-game (default-corp [(qty "The Cleaners" 1) (qty "Scorched Earth" 1)])
              (default-runner [(qty "Sure Gamble" 3) (qty "Diesel" 3)]))
    (play-from-hand state :corp "The Cleaners" "New remote")
    (let [clean (get-content state :remote1 0)]
      (score-agenda state :corp clean)
      (core/gain state :runner :tag 1)
      (play-from-hand state :corp "Scorched Earth")
      (is (= 0 (count (:hand (get-runner)))) "5 damage dealt to Runner"))))

(deftest the-cleaners-cybernetics
  ;; The Cleaners - No bonus damage when runner "suffers" damage
  (do-game
    (new-game (default-corp [(qty "The Cleaners" 1)])
              (default-runner [(qty "Respirocytes" 3)]))
    (play-from-hand state :corp "The Cleaners" "New remote")
    (score-agenda state :corp (get-content state :remote1 0))
    (take-credits state :corp)
    (play-from-hand state :runner "Respirocytes")
    (is (= 1 (count (:hand (get-runner)))) "Only 1 damage dealt to Runner from Cybernetics")))

(deftest the-future-perfect
  ;; The Future Perfect - cannot steal on failed psi game (if not installed)
  (do-game
    (new-game (default-corp [(qty "The Future Perfect" 2)])
              (default-runner))
    (play-from-hand state :corp "The Future Perfect" "New remote")
    (take-credits state :corp)

    (testing "No steal on not-equal Psi game"
      (run-empty-server state "HQ")
      (prompt-choice :runner "Access")
      (prompt-choice :corp "1 [Credits]")
      (prompt-choice :runner "0 [Credits]")
      ;; Cannot steal prompt
      (prompt-choice :runner "OK")
      (is (= 0 (:agenda-point (get-runner))) "Runner did not steal TFP"))

    (testing "Successful steal on equal Psi game"
      (run-empty-server state "HQ")
      (prompt-choice :runner "Access")
      (prompt-choice :corp "1 [Credits]")
      (prompt-choice :runner "1 [Credits]")
      (prompt-choice :runner "Steal")
      (is (= 3 (:agenda-point (get-runner))) "Runner stole TFP"))

    (testing "No Psi game and successful steal when installed"
      (run-empty-server state "Server 1")
      (prompt-choice :runner "Steal")
      (is (= 6 (:agenda-point (get-runner))) "Runner stole TFP - no Psi game on installed TFP"))))

(deftest underway-renovation
  ;; Underway Renovation - Mill the Runner when advanced
  (do-game
    (new-game (default-corp [(qty "Underway Renovation" 1) (qty "Shipment from SanSan" 1)])
              (default-runner))
    (core/gain state :corp :click 2)
    (core/move state :runner (find-card "Sure Gamble" (:hand (get-runner))) :deck)
    (core/move state :runner (find-card "Sure Gamble" (:hand (get-runner))) :deck)
    (core/move state :runner (find-card "Sure Gamble" (:hand (get-runner))) :deck)
    (play-from-hand state :corp "Underway Renovation" "New remote")
    (let [ur (get-content state :remote1 0)]
      (core/advance state :corp {:card (refresh ur)})
      (is (last-log-contains? state "Sure Gamble")
          "Underway Renovation trashed card name is in log")
      ; check for #2370
      (is (not (last-log-contains? state "Sure Gamble, Sure Gamble"))
          "Underway Renovation trashed card name is in log")
      (is (= 1 (count (:discard (get-runner)))) "1 card milled from Runner Stack")
      (play-from-hand state :corp "Shipment from SanSan")
      (prompt-choice :corp "2")
      (prompt-select :corp ur)
      (is (= 3 (:advance-counter (refresh ur))))
      (is (= 1 (count (:discard (get-runner)))) "No Runner mills; advancements were placed")
      (core/advance state :corp {:card (refresh ur)})
      (is (= 4 (:advance-counter (refresh ur))))
      (is (last-log-contains? state "Sure Gamble, Sure Gamble")
          "Underway Renovation trashed card name is in log")
      (is (= 3 (count (:discard (get-runner)))) "2 cards milled from Runner Stack; 4+ advancements"))))

(deftest vulcan-coverup
  ;; Vulcan Coverup - Do 2 meat damage when scored; take 1 bad pub when stolen
  (do-game
    (new-game (default-corp [(qty "Vulcan Coverup" 2)])
              (default-runner))
    (play-from-hand state :corp "Vulcan Coverup" "New remote")
    (take-credits state :corp)
    (run-empty-server state :remote1)
    (prompt-choice :runner "Steal")
    (is (= 1 (:bad-publicity (get-corp))) "Took 1 bad pub from stolen agenda")
    (take-credits state :runner)
    (play-from-hand state :corp "Vulcan Coverup" "New remote")
    (let [vc (get-content state :remote2 0)]
      (score-agenda state :corp vc)
      (is (= 2 (count (:discard (get-runner)))) "Did 2 meat damage upon scoring"))))
