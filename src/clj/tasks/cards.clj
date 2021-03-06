(ns tasks.cards
  "Utilities for card tests"
  (:require [web.db :refer [db] :as webdb]
            [monger.collection :as mc]
            [clojure.data :refer [diff]]
            [clojure.string :as s]
            [game-test.cards.agendas]
            [game-test.cards.sites]
            [game-test.cards.events]
            [game-test.cards.hazard]
            [game-test.cards.ice]
            [game-test.cards.icebreakers]
            [game-test.cards.identities]
            [game-test.cards.operations]
            [game-test.cards.resources]
            [game-test.cards.radicles]
            [game-test.cards.regions]))

(defn- get-card-by-type
  "Get the normalized title (as a symbol) for cards of a specific type in the database"
  [t]
  (let [card-type (if (= "Icebreaker" t) "Resource" t)
        f (case t
            "Icebreaker" filter
            "Resource" remove
            nil)
        func (fn [coll]
               (if f
                 (f #(and (:subtype %)
                          (> (.indexOf (:subtype %) "Icebreaker") -1)) coll)
                 coll))]
    (->> (mc/find-maps db "cards" {:type card-type} [:normalizedtitle :subtype])
      func
      (map :normalizedtitle)
      (map #(s/replace % #"\s+" "-"))
      (map symbol))))

(defn- get-tests
  "Returns the names of all tests in a namespace"
  [nspace]
  (->> nspace
    (ns-publics)
    (filter (fn [[k v]] (contains? (meta v) :test)))
    (remove (fn [[k v]] (:skip-card-coverage (meta v))))
    (map (fn [[k v]] (if-let [title (:card-title (meta v))]
                       (symbol title) k)))))

(def ansi-esc "\u001B")
(def ansi-reset "\u001B[0m")
(def ansi-bold "[1m")
(def ansi-red "[1;31m")
(def ansi-green "[1;32m")
(def ansi-blue "[1;34m")

(defn- format-output
  [s cnt color]
  (str ansi-esc color s ansi-reset ansi-esc ansi-bold cnt ansi-reset))

(defn- compare-tests
  [[k v] show-all show-none]
  (let [cards (get-card-by-type k)
        tests (->> v
                (map get-tests)
                (flatten))
        [cards-wo tests-wo both] (diff (set cards)
                                       (set tests))]
    (println (str ansi-esc ansi-blue k ansi-reset))
    (println "\tUnique cards in db:" (count (set cards)))
    (println "\tTests:" (count tests))
    (println (format-output "\tCards with tests: " (count both) ansi-green))
    (when (and show-all (not show-none))
      (doseq [c (sort both)]
        (println "\t\t" c)))
    (println (format-output "\tCards without tests: " (count cards-wo) ansi-red))
    (when (not show-none)
      (doseq [c (sort cards-wo)]
        (println "\t\t" c)))
    (println (format-output "\tTests without cards: " (count tests-wo) ansi-red))
    (when (not show-none)
      (doseq [c (sort tests-wo)]
        (println "\t\t" c)))))

(defn test-coverage
  "Determine which cards have tests written for them. Takes an `--only <Type>` argument to limit output to a specific card type."
  [& args]
  (webdb/connect)
  (try
    (let [only (some #{"--only"} args)
          card-type (first (remove #(s/starts-with? % "--") args))
          show-all (some #{"--show-all"} args)
          show-none (some #{"--show-none"} args)
          nspaces {"Agenda" '(game-test.cards.agendas)
                   "Site" '(game-test.cards.sites)
                   "Event" '(game-test.cards.events)
                   "Hazard" '(game-test.cards.hazard)
                   "Character" '(game-test.cards.ice)
                   "Icebreaker" '(game-test.cards.icebreakers)
                   "Identity" '(game-test.cards.identities)
                   "Operation" '(game-test.cards.operations)
                   "Resource"  '(game-test.cards.resources)
                   "Radicle" '(game-test.cards.radicles)
                   "Region" '(game-test.cards.regions)}
          filtered-nspaces (if only
                             (select-keys nspaces [card-type])
                             (into (sorted-map) nspaces))]
      (when only
        (println "Only checking cards of type" (str ansi-esc ansi-blue card-type ansi-reset)))
      (doseq [ct filtered-nspaces]
        (compare-tests ct show-all show-none)
        (println)))
    (catch Exception e
      (do
        (println "Card test coverage failed: " (.getMessage e))
        (.printStackTrace e)))
    (finally (webdb/disconnect))))
