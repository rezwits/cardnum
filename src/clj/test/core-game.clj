(in-ns 'test.core)

(deftest corp-rez-unique
  ;; Rezzing a second copy of a unique Corp card
  (do-game
    (new-game (default-corp [(qty "Caprice Nisei" 2)])
              (default-runner))
    (play-from-hand state :corp "Caprice Nisei" "HQ")
    (play-from-hand state :corp "Caprice Nisei" "R&D")
    (core/rez state :corp (get-content state :hq 0))
    (is (:rezzed (get-content state :hq 0)) "First Caprice rezzed")
    (core/rez state :corp (get-content state :rd 0))
    (is (not (:rezzed (get-content state :rd 0))) "Second Caprice could not be rezzed")))

(deftest runner-install-program
  ;; runner-install - Program; ensure costs are paid
  (do-game
    (new-game (default-corp)
              (default-runner [(qty "Gordian Blade" 1)]))
    (take-credits state :corp)
    (play-from-hand state :runner "Gordian Blade")
    (let [gord (get-in @state [:runner :rig :program 0])]
      (is (= (- 5 (:cost gord)) (:credit (get-runner))) "Program cost was applied")
      (is (= (- 4 (:memoryunits gord)) (:memory (get-runner))) "Program MU was applied"))))

(deftest runner-installing-uniques
  ;; Installing a copy of an active unique Runner card is prevented
  (do-game
    (new-game (default-corp)
              (default-runner [(qty "Kati Jones" 2) (qty "Scheherazade" 2)
                               (qty "Off-Campus Apartment" 1) (qty "Hivemind" 2)]))
    (take-credits state :corp)
    (core/gain state :runner :click 1 :memory 2)
    (core/draw state :runner 2)
    (play-from-hand state :runner "Kati Jones")
    (play-from-hand state :runner "Off-Campus Apartment")
    (play-from-hand state :runner "Scheherazade")
    (let [oca (get-in @state [:runner :rig :resource 1])
          scheh (get-in @state [:runner :rig :program 0])]
      (card-ability state :runner scheh 0)
      (prompt-select :runner (find-card "Hivemind" (:hand (get-runner))))
      (is (= "Hivemind" (:title (first (:hosted (refresh scheh))))) "Hivemind hosted on Scheherazade")
      (play-from-hand state :runner "Kati Jones")
      (is (= 1 (:click (get-runner))) "Not charged a click")
      (is (= 2 (count (get-in @state [:runner :rig :resource]))) "2nd copy of Kati couldn't install")
      (card-ability state :runner oca 0)
      (prompt-select :runner (find-card "Kati Jones" (:hand (get-runner))))
      (is (empty? (:hosted (refresh oca))) "2nd copy of Kati couldn't be hosted on OCA")
      (is (= 1 (:click (get-runner))) "Not charged a click")
      (play-from-hand state :runner "Hivemind")
      (is (= 1 (count (get-in @state [:runner :rig :program]))) "2nd copy of Hivemind couldn't install")
      (card-ability state :runner scheh 0)
      (prompt-select :runner (find-card "Hivemind" (:hand (get-runner))))
      (is (= 1 (count (:hosted (refresh scheh)))) "2nd copy of Hivemind couldn't be hosted on Scheherazade")
      (is (= 1 (:click (get-runner))) "Not charged a click"))))

(deftest deactivate-program
  ;; deactivate - Program; ensure MU are restored
  (do-game
    (new-game (default-corp)
              (default-runner [(qty "Gordian Blade" 1)]))
    (take-credits state :corp)
    (play-from-hand state :runner "Gordian Blade")
    (let [gord (get-in @state [:runner :rig :program 0])]
      (core/trash state :runner gord)
      (is (= 4 (:memory (get-runner))) "Trashing the program restored MU"))))

(deftest agenda-forfeit-runner
  ;; forfeit - Don't deactivate agenda to trigger leave play effects if Runner forfeits a stolen agenda
  (do-game
    (new-game (default-corp [(qty "Mandatory Upgrades" 1)])
              (default-runner [(qty "Data Dealer" 1)]))
    (take-credits state :corp)
    (play-from-hand state :runner "Data Dealer")
    (run-empty-server state "HQ")
    (prompt-choice :runner "Steal")
    (is (= 2 (:agenda-point (get-runner))))
    (card-ability state :runner (get-resource state 0) 0)
    (prompt-select :runner (get-scored state :runner 0))
    (is (= 1 (:click (get-runner))) "Didn't lose a click")
    (is (= 4 (:click-per-turn (get-runner))) "Still have 4 clicks per turn")))

(deftest agenda-forfeit-corp
  ;; forfeit - Deactivate agenda to trigger leave play effects if Corp forfeits a scored agenda
  (do-game
    (new-game (default-corp [(qty "Mandatory Upgrades" 1) (qty "Corporate Town" 1)])
              (default-runner))
    (play-from-hand state :corp "Mandatory Upgrades" "New remote")
    (score-agenda state :corp (get-content state :remote1 0))
    (is (= 4 (:click-per-turn (get-corp))) "Up to 4 clicks per turn")
    (play-from-hand state :corp "Corporate Town" "New remote")
    (let [ctown (get-content state :remote2 0)]
      (core/rez state :corp ctown)
      (prompt-select :corp (get-scored state :corp 0))
      (is (= 3 (:click-per-turn (get-corp))) "Back down to 3 clicks per turn"))))

(deftest refresh-recurring-credits-hosted
  ;; host - Recurring credits on cards hosted after install refresh properly
  (do-game
    (new-game (default-corp [(qty "Ice Wall" 3) (qty "Hedge Fund" 3)])
              (default-runner [(qty "Compromised Employee" 1) (qty "Off-Campus Apartment" 1)]))
    (play-from-hand state :corp "Ice Wall" "HQ")
    (take-credits state :corp 2)
    (play-from-hand state :runner "Off-Campus Apartment")
    (play-from-hand state :runner "Compromised Employee")
    (let [iwall (get-ice state :hq 0)
          apt (get-in @state [:runner :rig :resource 0])]
      (card-ability state :runner apt 1) ; use Off-Campus option to host an installed card
      (prompt-select :runner (find-card "Compromised Employee"
                                        (get-in @state [:runner :rig :resource])))
      (let [cehosted (first (:hosted (refresh apt)))]
        (card-ability state :runner cehosted 0) ; take Comp Empl credit
        (is (= 4 (:credit (get-runner))))
        (is (= 0 (:rec-counter (refresh cehosted))))
        (core/rez state :corp iwall)
        (is (= 5 (:credit (get-runner))) "Compromised Employee gave 1 credit from ice rez")
        (take-credits state :runner)
        (take-credits state :corp)
        (is (= 1 (:rec-counter (refresh cehosted)))
            "Compromised Employee recurring credit refreshed")))))

(deftest card-str-test-simple
  ;; ensure card-str names cards in simple situations properly
  (do-game
    (new-game (default-corp [(qty "Ice Wall" 3) (qty "Jackson Howard" 2)])
              (default-runner [(qty "Corroder" 1)
                               (qty "Clone Chip" 1)
                               (qty "Paparazzi" 1)
                               (qty "Parasite" 1)]))
    (core/gain state :corp :click 2)
    (play-from-hand state :corp "Ice Wall" "HQ")
    (play-from-hand state :corp "Ice Wall" "R&D")
    (play-from-hand state :corp "Jackson Howard" "New remote")
    (play-from-hand state :corp "Jackson Howard" "New remote")
    (play-from-hand state :corp "Ice Wall" "HQ")
    (core/end-turn state :corp nil)
    (core/start-turn state :runner nil)
    (play-from-hand state :runner "Corroder")
    (play-from-hand state :runner "Clone Chip")
    (play-from-hand state :runner "Paparazzi")
    (play-from-hand state :runner "Parasite")
    (let [hqiwall0 (get-ice state :hq 0)
          hqiwall1 (get-ice state :hq 1)
          rdiwall (get-ice state :rd 0)
          jh1 (get-content state :remote1 0)
          jh2 (get-content state :remote2 0)
          corr (get-in @state [:runner :rig :program 0])
          cchip (get-in @state [:runner :rig :hardware 0])
          pap (get-in @state [:runner :rig :resource 0])]
      (core/rez state :corp hqiwall0)
      (core/rez state :corp jh1)
      (prompt-select :runner (refresh hqiwall0))
      (is (= (core/card-str state (refresh hqiwall0)) "Ice Wall protecting HQ at position 0"))
      (is (= (core/card-str state (refresh hqiwall1)) "ICE protecting HQ at position 1"))
      (is (= (core/card-str state (refresh rdiwall)) "ICE protecting R&D at position 0"))
      (is (= (core/card-str state (refresh rdiwall) {:visible true})
             "Ice Wall protecting R&D at position 0"))
      (is (= (core/card-str state (refresh jh1)) "Jackson Howard in Server 1"))
      (is (= (core/card-str state (refresh jh2)) "a card in Server 2"))
      (is (= (core/card-str state (refresh corr)) "Corroder"))
      (is (= (core/card-str state (refresh cchip)) "Clone Chip"))
      (is (= (core/card-str state (refresh pap)) "Paparazzi"))
      (is (= (core/card-str state (first (:hosted (refresh hqiwall0))))
             "Parasite hosted on Ice Wall protecting HQ at position 0")))))

(deftest invalid-score-attempt
  ;; Test scoring with an incorrect number of advancement tokens
  (do-game
    (new-game (default-corp [(qty "Ancestral Imager" 1)])
              (default-runner))
    (play-from-hand state :corp "Ancestral Imager" "New remote")
    (let [ai (get-content state :remote1 0)]
      ;; Trying to score without any tokens does not do anything
      (is (not (find-card "Ancestral Imager" (:scored (get-corp)))) "AI not scored")
      (is (not (nil? (get-content state :remote1 0))))
      (core/advance state :corp {:card (refresh ai)})
      (core/score state :corp {:card (refresh ai)})
      (is (not (nil? (get-content state :remote1 0)))))))

(deftest trash-corp-hosted
  ;; Hosted Corp cards are included in all-installed and fire leave-play effects when trashed
  (do-game
    (new-game (default-corp [(qty "Full Immersion RecStudio" 1) (qty "Worlds Plaza" 1) (qty "Director Haas" 1)])
              (default-runner))
    (play-from-hand state :corp "Full Immersion RecStudio" "New remote")
    (let [fir (get-content state :remote1 0)]
      (core/rez state :corp fir)
      (card-ability state :corp fir 0)
      (prompt-select :corp (find-card "Worlds Plaza" (:hand (get-corp))))
      (let [wp (first (:hosted (refresh fir)))]
        (core/rez state :corp wp)
        (card-ability state :corp wp 0)
        (prompt-select :corp (find-card "Director Haas" (:hand (get-corp))))
        (let [dh (first (:hosted (refresh wp)))]
          (is (:rezzed dh) "Director Haas was rezzed")
          (is (= 0 (:credit (get-corp))) "Corp has 0 credits")
          (is (= 4 (:click-per-turn (get-corp))) "Corp has 4 clicks per turn")
          (is (= 3 (count (core/all-installed state :corp))) "all-installed counting hosted Corp cards")
          (take-credits state :corp)
          (run-empty-server state "Server 1")
          (prompt-select :runner dh)
          (prompt-choice :runner "Yes") ; trash Director Haas
          (prompt-choice :runner "Done")
          (is (= 3 (:click-per-turn (get-corp))) "Corp down to 3 clicks per turn"))))))

(deftest trash-remove-per-turn-restriction
  ;; Trashing a card should remove it from [:per-turn] - Issue #1345
  (do-game
    (new-game (default-corp [(qty "Hedge Fund" 3)])
              (default-runner [(qty "Imp" 2) (qty "Scavenge" 1)]))
    (take-credits state :corp)
    (core/gain state :runner :click 1)
    (play-from-hand state :runner "Imp")
    (let [imp (get-program state 0)]
      (run-empty-server state "HQ")
      (card-ability state :runner imp 0)
      (is (= 1 (count (:discard (get-corp)))) "Accessed Hedge Fund is trashed")
      (run-empty-server state "HQ")
      (card-ability state :runner imp 0)
      (is (= 1 (count (:discard (get-corp)))) "Card can't be trashed, Imp already used this turn")
      (prompt-choice :runner "OK")
      (play-from-hand state :runner "Scavenge")
      (prompt-select :runner imp)
      (prompt-select :runner (find-card "Imp" (:discard (get-runner)))))
    (let [imp (get-program state 0)]
      (is (= 2 (get-counters (refresh imp) :virus)) "Reinstalled Imp has 2 counters")
      (run-empty-server state "HQ")
      (card-ability state :runner imp 0))
    (is (= 2 (count (:discard (get-corp)))) "Hedge Fund trashed, reinstalled Imp used on same turn")))

(deftest trash-seen-and-unseen
  ;; Trash installed assets that are both seen and unseen by runner
  (do-game
    (new-game (default-corp [(qty "PAD Campaign" 3)])
              (default-runner))
    (play-from-hand state :corp "PAD Campaign" "New remote")
    (play-from-hand state :corp "PAD Campaign" "New remote")
    (take-credits state :corp 1)
    (run-empty-server state "Server 1")
    (prompt-choice :runner "No")
    ;; run and trash the second asset
    (run-empty-server state "Server 2")
    (prompt-choice :runner "Yes")
    (take-credits state :runner 2)
    (play-from-hand state :corp "PAD Campaign" "Server 1")
    (prompt-choice :corp "OK")
    (is (= 2 (count (:discard (get-corp)))) "Trashed existing asset")
    (is (:seen (first (get-in @state [:corp :discard]))) "Asset trashed by runner is Seen")
    (is (not (:seen (second (get-in @state [:corp :discard]))))
        "Asset trashed by corp is Unseen")
    (is (not (:seen (get-content state :remote1 0))) "New asset is unseen")))

(deftest reinstall-seen-asset
  ;; Install a faceup card in Archives, make sure it is not :seen
  (do-game
    (new-game (default-corp [(qty "PAD Campaign" 1) (qty "Interns" 1)])
              (default-runner))
    (play-from-hand state :corp "PAD Campaign" "New remote")
    (take-credits state :corp 2)
    ;; run and trash the asset
    (run-empty-server state "Server 1")
    (prompt-choice :runner "Yes")
    (is (:seen (first (get-in @state [:corp :discard]))) "Asset trashed by runner is Seen")
    (take-credits state :runner 3)
    (play-from-hand state :corp "Interns")
    (prompt-select :corp (first (get-in @state [:corp :discard])))
    (prompt-choice :corp "New remote")
    (is (not (:seen (get-content state :remote2 0))) "New asset is unseen")))

(deftest all-installed-runner-test
  ;; Tests all-installed for programs hosted on ICE, nested hosted programs, and non-installed hosted programs
  (do-game
    (new-game (default-corp [(qty "Wraparound" 1)])
              (default-runner [(qty "Omni-drive" 1) (qty "Personal Workshop" 1) (qty "Leprechaun" 1) (qty "Corroder" 1) (qty "Mimic" 1) (qty "Knight" 1)]))
    (play-from-hand state :corp "Wraparound" "HQ")
    (let [wrap (get-ice state :hq 0)]
      (core/rez state :corp wrap)
      (take-credits state :corp)
      (core/draw state :runner)
      (core/gain state :runner :credit 7)
      (play-from-hand state :runner "Knight")
      (play-from-hand state :runner "Personal Workshop")
      (play-from-hand state :runner "Omni-drive")
      (take-credits state :corp)
      (let [kn (get-in @state [:runner :rig :program 0])
            pw (get-in @state [:runner :rig :resource 0])
            od (get-in @state [:runner :rig :hardware 0])
            co (find-card "Corroder" (:hand (get-runner)))
            le (find-card "Leprechaun" (:hand (get-runner)))]
        (card-ability state :runner kn 0)
        (prompt-select :runner wrap)
        (card-ability state :runner pw 0)
        (prompt-select :runner co)
        (card-ability state :runner od 0)
        (prompt-select :runner le)
        (let [od (refresh od)
              le (first (:hosted od))
              mi (find-card "Mimic" (:hand (get-runner)))]
          (card-ability state :runner le 0)
          (prompt-select :runner mi)
          (let [all-installed (core/all-installed state :runner)]
            (is (= 5 (count all-installed)) "Number of installed runner cards is correct")
            (is (not-empty (filter #(= (:title %) "Leprechaun") all-installed)) "Leprechaun is in all-installed")
            (is (not-empty (filter #(= (:title %) "Personal Workshop") all-installed)) "Personal Workshop is in all-installed")
            (is (not-empty (filter #(= (:title %) "Mimic") all-installed)) "Mimic is in all-installed")
            (is (not-empty (filter #(= (:title %) "Omni-drive") all-installed)) "Omni-drive is in all-installed")
            (is (not-empty (filter #(= (:title %) "Knight") all-installed)) "Knight is in all-installed")
            (is (empty (filter #(= (:title %) "Corroder") all-installed)) "Corroder is not in all-installed")))))))

(deftest log-accessed-names
  ;; Check that accessed card names are logged - except those on R&D, and no logs on archives
  (do-game
    (new-game
      (default-corp [(qty "PAD Campaign" 7)])
      (default-runner))
    (play-from-hand state :corp "PAD Campaign" "New remote")
    (trash-from-hand state :corp "PAD Campaign")
    (take-credits state :corp)
    (run-empty-server state :hq)
    (prompt-choice :runner "No") ; Dismiss trash prompt
    (is (last-log-contains? state "PAD Campaign") "Accessed card name was logged")
    (run-empty-server state :rd)
    (is (last-log-contains? state "an unseen card") "Accessed card name was not logged")
    (run-empty-server state :remote1)
    (prompt-choice :runner "No") ; Dismiss trash prompt
    (is (last-log-contains? state "PAD Campaign") "Accessed card name was logged")))

(deftest counter-manipulation-commands
  ;; Test interactions of various cards with /counter and /adv-counter commands
  (do-game
    (new-game (default-corp [(qty "Adonis Campaign" 1)
                             (qty "Public Support" 2)
                             (qty "Oaktown Renovation" 1)])
              (default-runner))
    ;; Turn 1 Corp, install oaktown and assets
    (core/gain state :corp :click 4)
    (play-from-hand state :corp "Adonis Campaign" "New remote")
    (play-from-hand state :corp "Public Support" "New remote")
    (play-from-hand state :corp "Public Support" "New remote")
    (play-from-hand state :corp "Oaktown Renovation" "New remote")
    (let [adonis (get-content state :remote1 0)
          publics1 (get-content state :remote2 0)
          publics2 (get-content state :remote3 0)
          oaktown (get-content state :remote4 0)]
    (core/advance state :corp {:card (refresh oaktown)})
    (core/advance state :corp {:card (refresh oaktown)})
    (core/advance state :corp {:card (refresh oaktown)})
    (is (= 8 (:credit (get-corp))) "Corp 5+3 creds from Oaktown")
    (core/end-turn state :corp nil)

    ;; Turn 1 Runner
    (core/start-turn state :runner nil)
    (take-credits state :runner 3)
    (core/click-credit state :runner nil)
    (core/end-turn state :runner nil)
    (core/rez state :corp (refresh adonis))
    (core/rez state :corp (refresh publics1))

    ;; Turn 2 Corp
    (core/start-turn state :corp nil)
    (core/rez state :corp (refresh publics2))
    (is (= 3 (:click (get-corp))))
    (is (= 3 (:credit (get-corp))) "only Adonis money")
    (is (= 9 (get-counters (refresh adonis) :credit)))
    (is (= 2 (get-counters (refresh publics1) :power)))
    (is (= 3 (get-counters (refresh publics2) :power)))

    ;; oops, forgot to rez 2nd public support before start of turn,
    ;; let me fix it with a /command
    (core/command-counter state :corp ["power" 2])
    (prompt-select :corp (refresh publics2))
    (is (= 2 (get-counters (refresh publics2) :power)))
    ;; Oaktown checks and manipulation
    (is (= 3 (:advance-counter (refresh oaktown))))
    (core/command-adv-counter state :corp 2)
    (prompt-select :corp (refresh oaktown))
    ;; score should fail, shouldn't be able to score with 2 advancement tokens
    (core/score state :corp (refresh oaktown))
    (is (= 0 (:agenda-point (get-corp))))
    (core/command-adv-counter state :corp 4)
    (prompt-select :corp (refresh oaktown))
    (is (= 4 (:advance-counter (refresh oaktown))))
    (is (= 3 (:credit (get-corp))))
    (is (= 3 (:click (get-corp))))
    (core/score state :corp (refresh oaktown)) ; now the score should go through
    (is (= 2 (:agenda-point (get-corp))))
    (take-credits state :corp)

    ;; Turn 2 Runner
    ;; cheating with publics1 going too fast. Why? because I can
    (is (= 2 (get-counters (refresh publics1) :power)))
    (core/command-counter state :corp ["power" 1])
    (prompt-select :corp (refresh publics1))
    (is (= 1 (get-counters (refresh publics1) :power)))
    ;; let's adjust Adonis while at it
    (is (= 9 (get-counters (refresh adonis) :credit)))
    (core/command-counter state :corp ["credit" 3])
    (prompt-select :corp (refresh adonis))
    (is (= 3 (get-counters (refresh adonis) :credit)))
    (take-credits state :runner)

    ;; Turn 3 Corp
    (is (= 3 (:agenda-point (get-corp)))) ; cheated PS1 should get scored
    (is (= 9 (:credit (get-corp))))
    (is (= (:zone (refresh publics1) :scored)))
    (is (= (:zone (refresh publics2)) [:servers :remote3 :content]))
    (is (= (:zone (refresh adonis) :discard)))
    (take-credits state :corp)

    ;; Turn 3 Runner
    (take-credits state :runner)

    ;; Turn 4 Corp
    (is (= 4 (:agenda-point (get-corp)))) ; PS2 should get scored
    (is (= (:zone (refresh publics2) :scored)))
    (is (= 12 (:credit (get-corp)))))))

(deftest run-bad-publicity-credits
  ;; Should not lose BP credits until a run is completely over. Issue #1721.
  (do-game
    (new-game (default-corp [(qty "Cyberdex Virus Suite" 3)])
              (make-deck "Valencia Estevez: The Angel of Cayambe" [(qty "Sure Gamble" 3)]))
    (is (= 1 (:bad-publicity (get-corp))) "Corp starts with 1 BP")
    (play-from-hand state :corp "Cyberdex Virus Suite" "New remote")
    (play-from-hand state :corp "Cyberdex Virus Suite" "R&D")
    (play-from-hand state :corp "Cyberdex Virus Suite" "HQ")
    (take-credits state :corp)
    (run-empty-server state :remote1)
    (prompt-choice :corp "No")
    (prompt-choice :runner "Yes")
    (is (= 5 (:credit (get-runner))) "1 BP credit spent to trash CVS")
    (run-empty-server state :hq)
    (prompt-choice :corp "No")
    (prompt-choice :runner "Yes")
    (is (= 5 (:credit (get-runner))) "1 BP credit spent to trash CVS")
    (run-empty-server state :rd)
    (prompt-choice :corp "No")
    (prompt-choice :runner "Yes")
    (is (= 5 (:credit (get-runner))) "1 BP credit spent to trash CVS")))

(deftest run-psi-bad-publicity-credits
  ;; Should pay from Bad Pub for Psi games during run #2374
  (do-game
    (new-game (default-corp [(qty "Caprice Nisei" 3)])
              (make-deck "Valencia Estevez: The Angel of Cayambe" [(qty "Sure Gamble" 3)]))
    (is (= 1 (:bad-publicity (get-corp))) "Corp starts with 1 BP")
    (play-from-hand state :corp "Caprice Nisei" "New remote")
    (take-credits state :corp)
    (let [caprice (get-content state :remote1 0)]
      (core/rez state :corp caprice)
      (run-on state "Server 1")
      (is (prompt-is-card? :corp caprice) "Caprice prompt even with no ice, once runner makes run")
      (is (prompt-is-card? :runner caprice) "Runner has Caprice prompt")
      (prompt-choice :corp "2 [Credits]")
      (prompt-choice :runner "1 [Credits]")
      (is (= 5 (:credit (get-runner))) "Runner spend bad pub credit on psi game")
      (is (= 3 (:credit (get-corp))) "Corp spent 2 on psi game"))))

(deftest purge-nested
  ;; Purge nested-hosted virus counters
  (do-game
    (new-game (default-corp [(qty "Cyberdex Trial" 1)])
              (default-runner [(qty "Djinn" 1) (qty "Imp" 1) (qty "Leprechaun" 1)]))
    (take-credits state :corp)
    (core/gain state :runner :credit 100)
    (play-from-hand state :runner "Leprechaun")
    (let [lep (get-program state 0)]
      (card-ability state :runner lep 0)
      (prompt-select :runner (find-card "Djinn" (:hand (get-runner))))
      (let [djinn (first (:hosted (refresh lep)))]
        (card-ability state :runner djinn 1)
        (prompt-select :runner (find-card "Imp" (:hand (get-runner))))
        (let [imp (first (:hosted (refresh djinn)))]
          (is (= 2 (get-counters imp :virus)) "Imp has 2 virus counters")
          (take-credits state :runner)
          (play-from-hand state :corp "Cyberdex Trial")
          (is (= 0 (get-counters (refresh imp) :virus)) "Imp counters purged"))))))

(deftest multi-access-rd
  ;; multi-access of R&D sees all cards and upgrades
  (do-game
    (new-game (default-corp [(qty "Keegan Lane" 1) (qty "Midway Station Grid" 1)
                             (qty "Sweeps Week" 1) (qty "Manhunt" 1)
                             (qty "Hedge Fund" 1) (qty "Big Brother" 1)])
              (default-runner [(qty "Medium" 1)]))
    (play-from-hand state :corp "Keegan Lane" "R&D")
    (play-from-hand state :corp "Midway Station Grid" "R&D")
    (core/move state :corp (find-card "Hedge Fund" (:hand (get-corp))) :deck)
    (core/move state :corp (find-card "Sweeps Week" (:hand (get-corp))) :deck)
    (core/move state :corp (find-card "Manhunt" (:hand (get-corp))) :deck)
    (core/move state :corp (find-card "Big Brother" (:hand (get-corp))) :deck)
    (core/rez state :corp (get-content state :rd 1))
    (take-credits state :corp)
    (play-from-hand state :runner "Medium")
    (let [keegan (get-content state :rd 0)
          msg (get-content state :rd 1)
          med (get-program state 0)]
      (core/command-counter state :runner ["virus" 2])
      (prompt-select :runner (refresh med))
      (run-empty-server state :rd)
      (prompt-choice :runner 2)
      (prompt-choice :runner "Card from deck")
      (is (= "Hedge Fund" (-> (get-runner) :prompt first :card :title)))
      (prompt-choice :runner "OK")
      (prompt-choice :runner "Unrezzed upgrade in R&D")
      (is (= "Keegan Lane" (-> (get-runner) :prompt first :card :title)))
      (prompt-choice :runner "No")
      (prompt-choice :runner "Card from deck")
      (is (= "Sweeps Week" (-> (get-runner) :prompt first :card :title)))
      (prompt-choice :runner "OK")
      (prompt-choice :runner "Midway Station Grid")
      (is (= "Midway Station Grid" (-> (get-runner) :prompt first :card :title)))
      (prompt-choice :runner "No")
      (prompt-choice :runner "Card from deck")
      (is (= "Manhunt" (-> (get-runner) :prompt first :card :title)))
      (prompt-choice :runner "OK")
      (is (not (:run @state)) "Run ended"))))

(deftest multi-steal-archives
  ;; stealing multiple agendas from archives
  (do-game
    (new-game (default-corp [(qty "Breaking News" 3)])
              (default-runner))
    (trash-from-hand state :corp "Breaking News")
    (trash-from-hand state :corp "Breaking News")
    (trash-from-hand state :corp "Breaking News")
    (take-credits state :corp)
    (run-empty-server state :archives)
    (prompt-choice :runner "Breaking News")
    (prompt-choice :runner "Steal")
    (prompt-choice :runner "Breaking News")
    (prompt-choice :runner "Steal")
    (prompt-choice :runner "Breaking News")
    (prompt-choice :runner "Steal")
    (is (= 3 (count (:scored (get-runner)))) "3 agendas stolen")
    (is (empty (:discard (get-corp))) "0 agendas left in archives")))
