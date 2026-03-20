(ns meccg.deckbuilder
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [sablono.core :as sab :include-macros true]
            [cljs.core.async :refer [chan put! <! timeout] :as async]
            [clojure.string :refer [split split-lines join escape] :as s]
            [meccg.appstate :refer [app-state]]
            [meccg.auth :refer [authenticated] :as auth]
            [meccg.cardbrowser :refer [cards-channel image-url card-view card-ndce card-dce filter-title] :as cb]
            [meccg.ajax :refer [POST GET DELETE PUT]]
            [meccg.utils :refer [banned-span restricted-span rotated-span influence-dot influence-dots alliance-dots dots-html make-dots]]
            [goog.string :as gstring]
            [goog.string.format]
            [cardnum.utils :refer [str->int lookup-deck parse-deck-string INFINITY] :as utils]
            [cardnum.cards :refer [all-cards]]
            [cardnum.decks :as decks]
            [cardnum.cards :as cards]))

(def select-channel (chan))
(def zoom-channel (chan))

(defn num->percent
  "Converts an input number to a percent of the second input number for display"
  [num1 num2]
  (if (zero? num2)
    "0"
    (gstring/format "%.0f" (* 100 (float (/ num1 num2))))))

(defn noinfcost? [identity card]
  (or (= (:faction card) (:faction identity))
      (= 0 (:factioncost card)) (= INFINITY (decks/id-inf-limit identity))))

(defn identity-lookup
  "Lookup the identity (query) looking at all cards on specified alignment"
  [alignment card]
  (let [q (.toLowerCase (:title card))
        id (:id card)
        cards (filter #(= (:alignment %) alignment) @all-cards)
        exact-matches (utils/filter-exact-title q cards)]
    (cond (and id
               (first (filter #(= id (:trimCode %)) cards)))
          (let [id-matches (filter #(= id (:trimCode %)) cards)]
            (first (utils/filter-exact-title q id-matches)))
          (not-empty exact-matches) (utils/take-best-card exact-matches)
          :else
          (loop [i 2 matches cards]
            (let [subquery (subs q 0 i)]
              (cond (zero? (count matches)) card
                    (or (= (count matches) 1) (utils/identical-cards? matches)) (utils/take-best-card matches)
                    (<= i (count (:title card))) (recur (inc i) (utils/filter-title subquery matches))
                    :else card))))))

(defn- build-identity-name
  [title set_code art]
  (let [set-title (if set_code (str title " (" set_code ")") title)]
    (if art
      (str set-title " [" art "]")
      set-title)))

(defn parse-identity
  "Parse an id to the corresponding card map"
  [{:keys [alignment title art set_code]}]
  (let [card (identity-lookup alignment {:title title})]
    (assoc card :art art :display-name (build-identity-name title set_code art))))

(defn add-params-to-card
  "Add art and id parameters to a card hash"
  [card id]
  (-> card
      (assoc :id id)))

(defn- clean-param
  "Parse card parameter key value pairs from a string"
  [param]
  (if (and param
           (= 2 (count param)))
    (let [[k v] (map s/trim param)
          allowed-keys '("id" "art")]
      (if (some #{k} allowed-keys)
        [(keyword k) v]
        nil))
    nil))

(defn- param-reducer
  [acc param]
  (if param
    (assoc acc (first param) (second param))
    acc))

(defn- add-params
  "Parse a string of parameters and add them to a map"
  [result params-str]
  (if params-str
    (let [params-groups (split params-str #"\,")
          params-all (map #(split % #":") params-groups)
          params-clean (map #(clean-param %) params-all)]
      (reduce param-reducer result params-clean))
    result))

(defn load-decks [decks]
  (swap! app-state assoc :decks (sort-by :date > decks))
  (when-let [selected-deck (first (sort-by :date > decks))]
    (put! select-channel selected-deck))
  (swap! app-state assoc :decks-loaded true))

(defn org-decks-name [decks owner]
  (let [wizard-decks (when (om/get-state owner :wizard) (sort-by :name < (filter #(= (get-in % [:identity :alignment]) "Hero") decks)))
        minion-decks (if (om/get-state owner :minion)
                       (into wizard-decks (sort-by :name < (filter #(= (get-in % [:identity :alignment]) "Minion") decks)))
                       wizard-decks)
        fallen-decks (if (om/get-state owner :fallen)
                       (into minion-decks (sort-by :name < (filter #(= (get-in % [:identity :alignment]) "Fallen-wizard") decks)))
                       minion-decks)
        balrog-decks (if (om/get-state owner :balrog)
                       (into fallen-decks (sort-by :name < (filter #(= (get-in % [:identity :alignment]) "Balrog") decks)))
                       fallen-decks)
        el-decks (if (om/get-state owner :el)
                   (into balrog-decks (sort-by :name < (filter #(= (get-in % [:identity :alignment]) "Elf-lord") decks)))
                   balrog-decks)
        dl-decks (if (om/get-state owner :dl)
                   (into el-decks (sort-by :name < (filter #(= (get-in % [:identity :alignment]) "Dwarf-lord") decks)))
                   el-decks)
        al-decks (if (om/get-state owner :al)
                   (into dl-decks (sort-by :name < (filter #(= (get-in % [:identity :alignment]) "Atani-lord") decks)))
                   dl-decks)
        wl-decks (if (om/get-state owner :wl)
                   (into al-decks (sort-by :name < (filter #(= (get-in % [:identity :alignment]) "War-lord") decks)))
                   al-decks)
        dragon-decks (if (om/get-state owner :dragon)
                       (into wl-decks (sort-by :name < (filter #(= (get-in % [:identity :alignment]) "Dragon-lord") decks)))
                       wl-decks)
        sorted-decks (sort-by :name < dragon-decks)]
    (om/set-state! owner :decks-filtered sorted-decks)
    (when-let [selected-deck (first (sort-by :name < :decks-filtered))]
      (put! select-channel selected-deck))
    (swap! app-state assoc :decks-loaded true)))

(defn org-decks-date [decks owner]
  (let [wizard-decks (when (om/get-state owner :wizard) (sort-by :date > (filter #(= (get-in % [:identity :alignment]) "Hero") decks)))
        minion-decks (if (om/get-state owner :minion)
                       (into wizard-decks (sort-by :date > (filter #(= (get-in % [:identity :alignment]) "Minion") decks)))
                       wizard-decks)
        fallen-decks (if (om/get-state owner :fallen)
                       (into minion-decks (sort-by :date > (filter #(= (get-in % [:identity :alignment]) "Fallen-wizard") decks)))
                       minion-decks)
        balrog-decks (if (om/get-state owner :balrog)
                       (into fallen-decks (sort-by :date > (filter #(= (get-in % [:identity :alignment]) "Balrog") decks)))
                       fallen-decks)
        el-decks (if (om/get-state owner :el)
                   (into balrog-decks (sort-by :date > (filter #(= (get-in % [:identity :alignment]) "Elf-lord") decks)))
                   balrog-decks)
        dl-decks (if (om/get-state owner :dl)
                   (into el-decks (sort-by :date > (filter #(= (get-in % [:identity :alignment]) "Dwarf-lord") decks)))
                   el-decks)
        al-decks (if (om/get-state owner :al)
                   (into dl-decks (sort-by :date > (filter #(= (get-in % [:identity :alignment]) "Atani-lord") decks)))
                   dl-decks)
        wl-decks (if (om/get-state owner :wl)
                   (into al-decks (sort-by :date > (filter #(= (get-in % [:identity :alignment]) "War-lord") decks)))
                   al-decks)
        dragon-decks (if (om/get-state owner :dragon)
                       (into wl-decks (sort-by :date > (filter #(= (get-in % [:identity :alignment]) "Dragon-lord") decks)))
                       wl-decks)
        sorted-decks (sort-by :date > dragon-decks)]
    (om/set-state! owner :decks-filtered sorted-decks)
    (when-let [selected-deck (first (sort-by :date > :decks-filtered))]
      (put! select-channel selected-deck))
    (swap! app-state assoc :decks-loaded true)))

(defn process-decks
  "Process the raw deck from the database into a more useful format"
  [decks]
  (for [deck decks]
    (let [identity (parse-identity (:identity deck))
          donate-dice (:donate-dice deck)
          donate-size (:donate-size deck)
          resources (lookup-deck (:resources deck))
          hazards (lookup-deck (:hazards deck))
          sideboard (lookup-deck (:sideboard deck))
          characters (lookup-deck (:characters deck))
          pool (lookup-deck (:pool deck))
          fwsb (lookup-deck (:fwsb deck))
          note (:note deck)]
      (assoc deck :resources resources :hazards hazards :sideboard sideboard
                  :characters characters :pool pool :fwsb fwsb
                  :identity identity :note note
                  :donate-dice donate-dice :donate-size donate-size
                  ))))

(defn distinct-by [f coll]
  (letfn [(step [xs seen]
            (lazy-seq (when-let [[x & more] (seq xs)]
                        (let [k (f x)]
                          (if (seen k)
                            (step more seen)
                            (cons x (step more (conj seen k))))))))]
    (step coll #{})))

(defn- add-deck-name
  [all-titles card]
  (let [card-title (:title card)
        indexes (keep-indexed #(if (= %2 card-title) %1 nil) all-titles)
        dups (> (count indexes) 1)]
    (if dups
      (assoc card :display-name (str (:title card) " (" (:set_code card) ")"))
      (assoc card :display-name (:title card)))))

(defn alignment-identities [alignment]
  (let [cards
        (->> @all-cards
             (filter #(and (= (:alignment %) alignment)
                           (= (:Secondary %) "Avatar"))))
        all-titles (map :title cards)
        add-deck (partial add-deck-name all-titles)]
    (map add-deck cards)))

(defn- insert-params
  "Add card parameters into the string representation"
  [trimCode]
    (if (nil? trimCode)
      ""
      (str " " trimCode)))

(defn resources->str [owner]
  (let [resources (om/get-state owner [:deck :resources])
        str (reduce #(str %1 (:qty %2) " " (get-in %2 [:card :title]) (insert-params (get-in %2 [:card :trimCode])) "\n") "" resources)]
    (om/set-state! owner :resource-edit str)))

(defn hazards->str [owner]
  (let [hazards (om/get-state owner [:deck :hazards])
        str (reduce #(str %1 (:qty %2) " " (get-in %2 [:card :title]) (insert-params (get-in %2 [:card :trimCode])) "\n") "" hazards)]
    (om/set-state! owner :hazard-edit str)))

(defn sideboard->str [owner]
  (let [sideboard (om/get-state owner [:deck :sideboard])
        str (reduce #(str %1 (:qty %2) " " (get-in %2 [:card :title]) (insert-params (get-in %2 [:card :trimCode])) "\n") "" sideboard)]
    (om/set-state! owner :sideboard-edit str)))

(defn characters->str [owner]
  (let [characters (om/get-state owner [:deck :characters])
        str (reduce #(str %1 (:qty %2) " " (get-in %2 [:card :title]) (insert-params (get-in %2 [:card :trimCode])) "\n") "" characters)]
    (om/set-state! owner :character-edit str)))

(defn pool->str [owner]
  (let [pool (om/get-state owner [:deck :pool])
        str (reduce #(str %1 (:qty %2) " " (get-in %2 [:card :title]) (insert-params (get-in %2 [:card :trimCode])) "\n") "" pool)]
    (om/set-state! owner :pool-edit str)))

(defn fwsb->str [owner]
  (let [fwsb (om/get-state owner [:deck :fwsb])
        str (reduce #(str %1 (:qty %2) " " (get-in %2 [:card :title]) (insert-params (get-in %2 [:card :trimCode])) "\n") "" fwsb)]
    (om/set-state! owner :fwsb-edit str)))

(defn note->str [owner]
  (let [note (om/get-state owner [:deck :note])]
    (om/set-state! owner :deck-note note)))

(defn edit-deck [owner]
  (let [deck (om/get-state owner :deck)]
    (om/set-state! owner :old-deck deck)
    (om/set-state! owner :edit true)
    (resources->str owner)
    (hazards->str owner)
    (sideboard->str owner)
    (characters->str owner)
    (pool->str owner)
    (fwsb->str owner)
    (note->str owner)
    (-> owner (om/get-node "viewport") js/$ (.addClass "edit"))
    (try (js/ga "send" "event" "deckbuilder" "edit") (catch js/Error e))
    (go (<! (timeout 500))
        (-> owner (om/get-node "deckname") js/$ .select))))

(defn end-edit [owner]
  (om/set-state! owner :edit false)
  (om/set-state! owner :query "")
  (-> owner (om/get-node "viewport") js/$ (.removeClass "edit")))

(defn handle-resource-edit [owner]
  (let [text (.-value (om/get-node owner "resource-edit"))
        cards (parse-deck-string text)]
    (om/set-state! owner :resource-edit text)
    (om/set-state! owner [:deck :resources] cards)))

(defn handle-hazard-edit [owner]
  (let [text (.-value (om/get-node owner "hazard-edit"))
        cards (parse-deck-string text)]
    (om/set-state! owner :hazard-edit text)
    (om/set-state! owner [:deck :hazards] cards)))

(defn handle-character-edit [owner]
  (let [text (.-value (om/get-node owner "character-edit"))
        cards (parse-deck-string text)]
    (om/set-state! owner :character-edit text)
    (om/set-state! owner [:deck :characters] cards)))

(defn handle-pool-edit [owner]
  (let [text (.-value (om/get-node owner "pool-edit"))
        cards (parse-deck-string text)]
    (om/set-state! owner :pool-edit text)
    (om/set-state! owner [:deck :pool] cards)))

(defn handle-sideboard-edit [owner]
  (let [text (.-value (om/get-node owner "sideboard-edit"))
        cards (parse-deck-string text)]
    (om/set-state! owner :sideboard-edit text)
    (om/set-state! owner [:deck :sideboard] cards)))

(defn handle-fwsb-edit [owner]
  (let [text (.-value (om/get-node owner "fwsb-edit"))
        cards (parse-deck-string text)]
    (om/set-state! owner :fwsb-edit text)
    (om/set-state! owner [:deck :fwsb] cards)))

(defn handle-deck-note [owner]
  (let [text (.-value (om/get-node owner "deck-note"))]
    (om/set-state! owner :deck-note text)
    (om/set-state! owner [:deck :note] text)))

(defn cancel-edit [owner]
  (end-edit owner)
  (go (let [deck (om/get-state owner :old-deck)
            all-decks (process-decks (:json (<! (GET (str "/data/decks")))))]
        (load-decks all-decks)
        (put! select-channel deck))))

(defn delete-deck [owner]
  (om/set-state! owner :delete true)
  (resources->str owner)
  (hazards->str owner)
  (sideboard->str owner)
  (characters->str owner)
  (pool->str owner)
  (fwsb->str owner)
  (note->str owner)
  (-> owner (om/get-node "viewport") js/$ (.addClass "delete"))
  (try (js/ga "send" "event" "deckbuilder" "delete") (catch js/Error e)))

(defn end-delete [owner]
  (om/set-state! owner :delete false)
  (-> owner (om/get-node "viewport") js/$ (.removeClass "delete")))

(defn handle-delete [cursor owner]
  (authenticated
    (fn [user]
      (let [deck (om/get-state owner :deck)]
        (try (js/ga "send" "event" "deckbuilder" "delete") (catch js/Error e))
        (go (let [response (<! (DELETE (str "/data/decks/" (:_id deck))))]))
        (do
          (om/transact! cursor :decks (fn [ds] (remove #(= deck %) ds)))
          (om/set-state! owner :deck (first (sort-by :date > (:decks @cursor))))
          (end-delete owner))))))

(defn new-deck [alignment owner]
  (let [old-deck (om/get-state owner :deck)
        id (->> alignment
                alignment-identities
                (sort-by :title)
                first)]
    (om/set-state! owner :deck {:name "New deck" :resources [] :hazards [] :sideboard []
                                :characters [] :pool [] :fwsb [] :identity id :note []
                                :donate-dice "empty" :donate-size "none"
                                })
    (try (js/ga "send" "event" "deckbuilder" "new" alignment) (catch js/Error e))
    (edit-deck owner)
    (om/set-state! owner :old-deck old-deck)))

(defn save-deck [cursor owner]
  (authenticated
    (fn [user]
      (end-edit owner)
      (let [deck (assoc (om/get-state owner :deck) :date (.toJSON (js/Date.)))
            deck (dissoc deck :stats)
            decks (remove #(= (:_id deck) (:_id %)) (:decks @app-state))
            resources (for [card (:resources deck) :when (get-in card [:card :title])]
                        (let [card-map {:qty (:qty card) :card (get-in card [:card :title])}]
                          (if (contains? card :id) (conj card-map {:id (:id card)}) card-map)))
            hazards (for [card (:hazards deck) :when (get-in card [:card :title])]
                      (let [card-map {:qty (:qty card) :card (get-in card [:card :title])}]
                        (if (contains? card :id) (conj card-map {:id (:id card)}) card-map)))
            sideboard (for [card (:sideboard deck) :when (get-in card [:card :title])]
                        (let [card-map {:qty (:qty card) :card (get-in card [:card :title])}]
                          (if (contains? card :id) (conj card-map {:id (:id card)}) card-map)))
            characters (for [card (:characters deck) :when (get-in card [:card :title])]
                         (let [card-map {:qty (:qty card) :card (get-in card [:card :title])}]
                           (if (contains? card :id) (conj card-map {:id (:id card)}) card-map)))
            pool (for [card (:pool deck) :when (get-in card [:card :title])]
                   (let [card-map {:qty (:qty card) :card (get-in card [:card :title])}]
                     (if (contains? card :id) (conj card-map {:id (:id card)}) card-map)))
            fwsb (for [card (:fwsb deck) :when (get-in card [:card :title])]
                   (let [card-map {:qty (:qty card) :card (get-in card [:card :title])}]
                     (if (contains? card :id) (conj card-map {:id (:id card)}) card-map)))
            note (:note deck)
            ;; only include keys that are relevant
            identity (select-keys (:identity deck) [:title :alignment :trimCode])
            donate-dice (:donate-dice deck)
            donate-size (:donate-size deck)
            data (assoc deck :resources resources :hazards hazards :sideboard sideboard
                             :characters characters :pool pool :fwsb fwsb
                             :identity identity :note note
                             :donate-dice donate-dice :donate-size donate-size
                             )]
        (try (js/ga "send" "event" "deckbuilder" "save") (catch js/Error e))
        (go (let [new-id (get-in (<! (if (:_id deck)
                                       (PUT "/data/decks" data :json)
                                       (POST "/data/decks" data :json)))
                                 [:json :_id])
                  new-deck (if (:_id deck) deck (assoc deck :_id new-id))
                  all-decks (process-decks (:json (<! (GET (str "/data/decks")))))]
              (om/update! cursor :decks (conj decks new-deck))
              (om/set-state! owner :deck new-deck)
              (load-decks all-decks)))))))

(defn clear-deck-stats [cursor owner]
  (authenticated
    (fn [user]
      (let [deck (dissoc (om/get-state owner :deck) :stats)
            decks (remove #(= (:_id deck) (:_id %)) (:decks @app-state))]
        (try (js/ga "send" "event" "deckbuilder" "cleardeckstats") (catch js/Error e))
        (go (let [result (<! (DELETE (str "/profile/stats/deck/" (:_id deck))))]
              (om/update! cursor :decks (conj decks deck))
              (om/set-state! owner :deck deck)
              (put! select-channel deck)))))))

(defn html-escape [st]
  (escape st {\< "&lt;" \> "&gt;" \& "&amp;" \" "#034;"}))

(defn card-influence-html
  "Returns hiccup-ready vector with dots for influence as well as restricted / rotated / banned symbols"
  [card qty in-faction allied?]
  (let [influence (* (:factioncost card) qty)
        banned (decks/banned? card)
        restricted (decks/restricted? card)
        released (:released card)]
    (list " "
          (when (and (not banned) (not in-faction))
            [:span.influence {:class (utils/faction-label card)}
             (if allied?
               (alliance-dots influence)
               (influence-dots influence))])
          (if banned
            banned-span
            [:span
             (when restricted restricted-span)
             (when released rotated-span)]))))

(defn deck-influence-html
  "Returns hiccup-ready vector with dots colored appropriately to deck's influence."
  [deck]
  (dots-html influence-dot (decks/influence-map deck)))

(defn build-format-status
  "Builds div for alternative format status"
  [format violation-details? message]
  [:div {:class (if (:legal format) "legal" "invalid") :title (when violation-details? (:reason format))}
   [:span.tick (if (:legal format) "✔" "✘")] message " compliant"])

(defn- build-deck-status-label [valid mwl standard rotation cache-refresh onesies modded violation-details?]
  (let [status (decks/deck-status mwl valid standard rotation)
        message (case status
                  "legal" "Tournament legal"
                  "standard" "Standard"
                  "dreamcard" "Dreamcards"
                  "casual" "Casual play only"
                  "invalid" "Invalid"
                  "")]
    [:div.status-tooltip.blue-shade
     [:div {:class (if valid "legal" "invalid")}
      [:span.tick (if valid "✔" "✘")] "Basic deckbuilding rules"]
     [:div {:class (if mwl "legal" "invalid")}
      [:span.tick (if mwl "✔" "✘")] (:name @cards/mwl)]
     [:div {:class (if rotation "legal" "invalid")}
      [:span.tick (if rotation "✔" "✘")] "Only released cards"]
     (build-format-status cache-refresh violation-details? "Cache Refresh")
     (build-format-status onesies violation-details? "1.1.1.1 format")
     (build-format-status modded violation-details? "Modded format")]))

(defn- deck-status-details
  [deck use-trusted-info]
;  (if use-trusted-info
;    (decks/trusted-deck-status deck)
    (decks/calculate-deck-status deck))

(defn format-deck-status-span
  [deck-status tooltip? violation-details?]
  (let [{:keys [valid mwl standard rotation cache-refresh onesies modded status]} deck-status
        message (case status
                  "legal" "Tournament legal"
                  "standard" "Standard"
                  "dreamcard" "Dreamcard"
                  "casual" "Casual play only"
                  "invalid" "Invalid"
                  "")]
    [:span.deck-status.shift-tooltip {:class status} message
     (when tooltip?
       (build-deck-status-label valid mwl standard rotation cache-refresh onesies modded violation-details?))]))

(defn deck-status-span-impl [sets deck tooltip? violation-details? use-trusted-info]
  (format-deck-status-span (deck-status-details deck use-trusted-info) tooltip? violation-details?))

(def deck-status-span-memoize (memoize deck-status-span-impl))

(defn deck-status-span
  "Returns a [:span] with standardized message and colors depending on the deck validity."
  ([sets deck] (deck-status-span sets deck false false true))
  ([sets deck tooltip? violation-details? use-trusted-info]
   (deck-status-span-memoize sets deck tooltip? violation-details? use-trusted-info)))

(defn match [identity query]
  (->> @all-cards
       (filter #(decks/allowed? % identity))
       (distinct-by :title)
       (utils/filter-title query)
       (take 10)))

(defn handle-keydown [owner event]
  (let [selected (om/get-state owner :selected)
        matches (om/get-state owner :matches)]
    (case (.-keyCode event)
      38 (when (pos? selected)
           (om/update-state! owner :selected dec))
      40 (when (< selected (dec (count matches)))
           (om/update-state! owner :selected inc))
      (9 13) (when-not (= (om/get-state owner :query) (:title (first matches)))
               (.preventDefault event)
               (-> ".deckedit .qty" js/$ .select)
               (om/set-state! owner :query (:title (nth matches selected))))
      (om/set-state! owner :selected 0))))

(defn handle-add [owner event]
  (.preventDefault event)
  (let [qty (js/parseInt (om/get-state owner :quantity))
        card (nth (om/get-state owner :matches) (om/get-state owner :selected))
        best-card (utils/lookup card)]
    (if (js/isNaN qty)
      (om/set-state! owner :quantity 1)
      (let [max-qty 9
            limit-qty (if (> qty max-qty) max-qty qty)]
        (if (= (:type best-card) "Resource")
          (put! (om/get-state owner :resource-edit-channel)
                {:qty limit-qty
                 :card best-card}))
        (if (= (:type best-card) "Hazard")
          (put! (om/get-state owner :hazard-edit-channel)
                {:qty limit-qty
                 :card best-card}))
        (if (= (:type best-card) "Character")
          (put! (om/get-state owner :character-edit-channel)
                {:qty limit-qty
                 :card best-card}))
        (om/set-state! owner :quantity 1)
        (om/set-state! owner :query "")
        (-> ".deckedit .lookup" js/$ .select)))))

(defn card-lookup [{:keys [cards]} owner]
  (reify
    om/IInitState
    (init-state [this]
      {:query ""
       :matches []
       :quantity 1
       :selected 0})

    om/IRenderState
    (render-state [this state]
      (sab/html
        [:p
         [:h3 "Add cards"]
         [:form.card-search {:on-submit #(handle-add owner %)}
          [:input.lookup {:type "text" :placeholder "Card name" :value (:query state)
                          :on-change #(om/set-state! owner :query (.. % -target -value))
                          :on-key-down #(handle-keydown owner %)}]
          " x "
          [:input.qty {:type "text" :value (:quantity state)
                       :on-change #(om/set-state! owner :quantity (.. % -target -value))}]
          [:button "Add to deck"]
          (let [query (:query state)
                matches (match (get-in state [:deck :identity]) query)
                exact-match (= (:title (first matches)) query)]
            (cond
              exact-match
              (do
                (om/set-state! owner :matches matches)
                (om/set-state! owner :selected 0))

              (not (or (empty? query) exact-match))
              (do
                (om/set-state! owner :matches matches)
                [:div.typeahead
                 (for [i (range (count matches))]
                   [:div {:class (if (= i (:selected state)) "selected" "")
                          :on-click (fn [e] (-> ".deckedit .qty" js/$ .select)
                                      (om/set-state! owner :query (.. e -target -textContent))
                                      (om/set-state! owner :selected i))}
                    (:title (nth matches i))])])))]]))))

(defn deck-collection
  [{:keys [sets decks decks-loaded active-deck]} owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (sab/html
        (cond
          (not decks-loaded) [:h4 "Loading deck collection..."]
          (empty? decks) [:h4 "No decks"]
          :else [:div
                 (for [deck decks]
                   [:div.deckline {:class (when (= active-deck deck) "active")
                                   :on-click #(put! select-channel deck)}
                    [:img {:src (image-url (:identity deck))
                           :alt (get-in deck [:identity :title] "")}]
                    [:div.float-right (deck-status-span sets deck)]
                    [:h4 (:name deck)]
                    [:div.float-right (-> (:date deck) js/Date. js/moment (.format "MMM Do YYYY"))]
                    [:p (get-in deck [:identity :title]) [:br]
                     (when (and (:stats deck) (not= "none" (get-in @app-state [:options :deckstats])))
                       (let [stats (:stats deck)
                             games (or (:games stats) 0)
                             started (or (:games-started stats) 0)
                             completed (or (:games-completed stats) 0)
                             wins (or (:wins stats) 0)
                             losses (or (:loses stats) 0)]
                         ; adding key :games to handle legacy stats before adding started vs completed
                         [:span "  Games: " (+ started games)
                          " - Completed: " (+ completed games)
                          " - Won: " wins " (" (num->percent wins (+ wins losses)) "%)"
                          " - Lost: " losses]))]])])))))

(defn line-span
  "Make the view of a single line in the deck - returns a span"
  [sets {:keys [identity cards] :as deck} {:keys [qty card] :as line}]
  [:span qty "  "
   (if-let [name (:title card)]
     (let [language (get-in @app-state [:options :language])
           display (if (:dreamcard card)
                     name
                     (case language
                       "English"
                       name
                       "Dutch"
                       (:title-du card)
                       "Español"
                       (:title-es card)
                       "Finnish"
                       (:title-fn card)
                       "French"
                       (:title-fr card)
                       "German"
                       (:title-gr card)
                       "Italian"
                       (:title-it card)
                       "Japanese"
                       (:title-jp card)
                       :default
                       name
                       ))
           infaction (noinfcost? identity card)
           banned (decks/banned? card)
           allied (decks/alliance-is-free? cards line)
           valid (and (decks/allowed? card identity)
                      (decks/legal-num-copies? identity line))
           released (decks/released? sets card)
           modqty (if (decks/is-prof-prog? deck card) (- qty 1) qty)]
       [:span
        [:span {:class (cond
                         (and valid released (not banned)) "fake-link"
                         valid "casual"
                         :else "invalid")
                :on-mouse-enter #(put! zoom-channel line)
                :on-mouse-leave #(put! zoom-channel false)} display]
        (card-influence-html card modqty infaction allied)])
     card)])

(defn line-qty-span
  "Make the view of a single line in the deck - returns a span"
  [sets {:keys [identity cards] :as deck} {:keys [qty card] :as line}]
  [:span qty "  "])

(defn line-name-span
  "Make the view of a single line in the deck - returns a span"
  [sets {:keys [identity cards] :as deck} {:keys [qty card] :as line}]
  [:span (if-let [name (:title card)]
           (let [infaction (noinfcost? identity card)
                 banned (decks/banned? card)
                 allied (decks/alliance-is-free? cards line)
                 valid (and (decks/allowed? card identity)
                            (decks/legal-num-copies? identity line))
                 released (decks/released? sets card)
                 modqty (if (decks/is-prof-prog? deck card) (- qty 1) qty)]
             [:span
              [:span {:class (cond
                               (and valid released (not banned)) "fake-link"
                               valid "casual"
                               :else "invalid")
                      :on-mouse-enter #(put! zoom-channel line)
                      :on-mouse-leave #(put! zoom-channel false)} name]
              (card-influence-html card modqty infaction allied)])
           card)])

(defn- create-identity
  [state target-value]
  (let [alignment (get-in state [:deck :identity :alignment])
        json-map (.parse js/JSON (.. target-value -target -value))
        id-map (js->clj json-map :keywordize-keys true)
        card (identity-lookup alignment id-map)]
      card))

(defn- identity-option-string
  [card]
  (.stringify js/JSON (clj->js {:title (:title card) :id (:trimCode card)})))

(defn deck-builder
  "Make the deckbuilder view"
  [{:keys [decks decks-loaded sets] :as cursor} owner]
  (reify
    om/IInitState
    (init-state [this]
      {:edit false
       :old-deck nil
       :resource-edit-channel (chan)
       :hazard-edit-channel (chan)
       :sideboard-edit-channel (chan)
       :character-edit-channel (chan)
       :pool-edit-channel (chan)
       :fwsb-edit-channel (chan)
       :note-edit-channel (chan)
       :wizard true
       :minion true
       :fallen true
       :balrog true
       :el true
       :dl true
       :al true
       :wl true
       :dragon true
       :by-date true
       :deck-filtered nil
       :deck nil
       })

    om/IWillMount
    (will-mount [this]
      (let [edit-channel (om/get-state owner :resource-edit-channel)]
        (go (while true
              (let [card (<! zoom-channel)]
                (om/set-state! owner :zoom card))))
        (go (while true
              (let [edit (<! edit-channel)
                    card (:card edit)
                    max-qty (or (:limited card) 9)
                    cards (om/get-state owner [:deck :resources])
                    match? #(when (= (get-in % [:card :title]) (:title card)) %)
                    existing-line (some match? cards)]
                (let [new-qty (+ (or (:qty existing-line) 0) (:qty edit))
                      rest (remove match? cards)
                      draft-id (decks/is-draft-id? (om/get-state owner [:deck :identity]))
                      new-cards (cond (and (not draft-id) (> new-qty max-qty))
                                      (conj rest (assoc existing-line :qty max-qty))
                                      (<= new-qty 0) rest
                                      (empty? existing-line) (conj rest {:qty new-qty :card card})
                                      :else (conj rest (assoc existing-line :qty new-qty)))]
                  (om/set-state! owner [:deck :resources] new-cards))
                (resources->str owner)))))
      (let [edit-channel (om/get-state owner :hazard-edit-channel)]
        (go (while true
              (let [card (<! zoom-channel)]
                (om/set-state! owner :zoom card))))
        (go (while true
              (let [edit (<! edit-channel)
                    card (:card edit)
                    max-qty (or (:limited card) 9)
                    cards (om/get-state owner [:deck :hazards])
                    match? #(when (= (get-in % [:card :title]) (:title card)) %)
                    existing-line (some match? cards)]
                (let [new-qty (+ (or (:qty existing-line) 0) (:qty edit))
                      rest (remove match? cards)
                      draft-id (decks/is-draft-id? (om/get-state owner [:deck :identity]))
                      new-cards (cond (and (not draft-id) (> new-qty max-qty))
                                      (conj rest (assoc existing-line :qty max-qty))
                                      (<= new-qty 0) rest
                                      (empty? existing-line) (conj rest {:qty new-qty :card card})
                                      :else (conj rest (assoc existing-line :qty new-qty)))]
                  (om/set-state! owner [:deck :hazards] new-cards))
                (hazards->str owner)))))
      (let [edit-channel (om/get-state owner :sideboard-edit-channel)]
        (go (while true
              (let [card (<! zoom-channel)]
                (om/set-state! owner :zoom card))))
        (go (while true
              (let [edit (<! edit-channel)
                    card (:card edit)
                    max-qty (or (:limited card) 9)
                    cards (om/get-state owner [:deck :sideboard])
                    match? #(when (= (get-in % [:card :title]) (:title card)) %)
                    existing-line (some match? cards)]
                (let [new-qty (+ (or (:qty existing-line) 0) (:qty edit))
                      rest (remove match? cards)
                      draft-id (decks/is-draft-id? (om/get-state owner [:deck :identity]))
                      new-cards (cond (and (not draft-id) (> new-qty max-qty))
                                      (conj rest (assoc existing-line :qty max-qty))
                                      (<= new-qty 0) rest
                                      (empty? existing-line) (conj rest {:qty new-qty :card card})
                                      :else (conj rest (assoc existing-line :qty new-qty)))]
                  (om/set-state! owner [:deck :sideboard] new-cards))
                (sideboard->str owner)))))
      (let [edit-channel (om/get-state owner :character-edit-channel)]
        (go (while true
              (let [card (<! zoom-channel)]
                (om/set-state! owner :zoom card))))
        (go (while true
              (let [edit (<! edit-channel)
                    card (:card edit)
                    max-qty (or (:limited card) 9)
                    cards (om/get-state owner [:deck :characters])
                    match? #(when (= (get-in % [:card :title]) (:title card)) %)
                    existing-line (some match? cards)]
                (let [new-qty (+ (or (:qty existing-line) 0) (:qty edit))
                      rest (remove match? cards)
                      draft-id (decks/is-draft-id? (om/get-state owner [:deck :identity]))
                      new-cards (cond (and (not draft-id) (> new-qty max-qty))
                                      (conj rest (assoc existing-line :qty max-qty))
                                      (<= new-qty 0) rest
                                      (empty? existing-line) (conj rest {:qty new-qty :card card})
                                      :else (conj rest (assoc existing-line :qty new-qty)))]
                  (om/set-state! owner [:deck :characters] new-cards))
                (characters->str owner)))))
      (let [edit-channel (om/get-state owner :pool-edit-channel)]
        (go (while true
              (let [card (<! zoom-channel)]
                (om/set-state! owner :zoom card))))
        (go (while true
              (let [edit (<! edit-channel)
                    card (:card edit)
                    max-qty (or (:limited card) 9)
                    cards (om/get-state owner [:deck :pool])
                    match? #(when (= (get-in % [:card :title]) (:title card)) %)
                    existing-line (some match? cards)]
                (let [new-qty (+ (or (:qty existing-line) 0) (:qty edit))
                      rest (remove match? cards)
                      draft-id (decks/is-draft-id? (om/get-state owner [:deck :identity]))
                      new-cards (cond (and (not draft-id) (> new-qty max-qty))
                                      (conj rest (assoc existing-line :qty max-qty))
                                      (<= new-qty 0) rest
                                      (empty? existing-line) (conj rest {:qty new-qty :card card})
                                      :else (conj rest (assoc existing-line :qty new-qty)))]
                  (om/set-state! owner [:deck :pool] new-cards))
                (pool->str owner)))))
      (let [edit-channel (om/get-state owner :fwsb-edit-channel)]
        (go (while true
              (let [card (<! zoom-channel)]
                (om/set-state! owner :zoom card))))
        (go (while true
              (let [edit (<! edit-channel)
                    card (:card edit)
                    max-qty (or (:limited card) 9)
                    cards (om/get-state owner [:deck :fwsb])
                    match? #(when (= (get-in % [:card :title]) (:title card)) %)
                    existing-line (some match? cards)]
                (let [new-qty (+ (or (:qty existing-line) 0) (:qty edit))
                      rest (remove match? cards)
                      draft-id (decks/is-draft-id? (om/get-state owner [:deck :identity]))
                      new-cards (cond (and (not draft-id) (> new-qty max-qty))
                                      (conj rest (assoc existing-line :qty max-qty))
                                      (<= new-qty 0) rest
                                      (empty? existing-line) (conj rest {:qty new-qty :card card})
                                      :else (conj rest (assoc existing-line :qty new-qty)))]
                  (om/set-state! owner [:deck :fwsb] new-cards))
                (fwsb->str owner)))))
      (let [edit-channel (om/get-state owner :note-edit-channel)]
        (go (while true
              (let [edit (<! edit-channel)]
                (om/set-state! owner [:deck :note] edit)
                (note->str owner)))))
      (go (while true
            (om/set-state! owner :deck (<! select-channel)))))

    om/IRenderState
    (render-state [this state]
      (sab/html
        [:div
         [:div.deckbuilder.blue-shade.panel
          [:div.viewport {:ref "viewport"}
           [:div.decks
            [:div.button-bar
             [:button {:on-click #(new-deck "Hero" owner)} "New Wizard deck"]
             [:button {:on-click #(new-deck "Minion" owner)} "New Minion deck"]
             [:button {:on-click #(new-deck "Balrog" owner)} "New Balrog deck"]
             [:button {:on-click #(new-deck "Fallen-wizard" owner)} "New Fallen deck"]
             [:button {:on-click #(new-deck "Elf-lord" owner)} "New Elf deck"]
             [:button {:on-click #(new-deck "Dwarf-lord" owner)} "New Dwarf deck"]
             [:button {:on-click #(new-deck "Atani-lord" owner)} "New Atani deck"]
             [:button {:on-click #(new-deck "War-lord" owner)} "New Warlord deck"]
             [:button {:on-click #(new-deck "Dragon-lord" owner)} "New Dragon deck"]
             ]
            [:div
             [:label "Hero" [:input {:type "checkbox"
                              :value true
                              :checked (om/get-state owner :wizard)
                              :on-change #(do
                                            (om/set-state! owner :wizard (.. % -target -checked))
                                            (if (om/get-state owner :by-date)
                                              (org-decks-date decks owner)
                                              (org-decks-name decks owner))
                                            )}]]
             [:label "Minion" [:input {:type "checkbox"
                              :value true
                              :checked (om/get-state owner :minion)
                              :on-change #(do
                                            (om/set-state! owner :minion (.. % -target -checked))
                                            (if (om/get-state owner :by-date)
                                              (org-decks-date decks owner)
                                              (org-decks-name decks owner))
                                            )}]]
             [:label "FW" [:input {:type "checkbox"
                              :value true
                              :checked (om/get-state owner :fallen)
                              :on-change #(do
                                            (om/set-state! owner :fallen (.. % -target -checked))
                                            (if (om/get-state owner :by-date)
                                              (org-decks-date decks owner)
                                              (org-decks-name decks owner))
                                            )}]]
             [:label "BA" [:input {:type "checkbox"
                              :value true
                              :checked (om/get-state owner :balrog)
                              :on-change #(do
                                            (om/set-state! owner :balrog (.. % -target -checked))
                                            (if (om/get-state owner :by-date)
                                              (org-decks-date decks owner)
                                              (org-decks-name decks owner))
                                            )}]]
             [:label "EL" [:input {:type "checkbox"
                              :value true
                              :checked (om/get-state owner :el)
                              :on-change #(do
                                            (om/set-state! owner :el (.. % -target -checked))
                                            (if (om/get-state owner :by-date)
                                              (org-decks-date decks owner)
                                              (org-decks-name decks owner))
                                            )}]]
             [:label "DL" [:input {:type "checkbox"
                              :value true
                              :checked (om/get-state owner :dl)
                              :on-change #(do
                                            (om/set-state! owner :dl (.. % -target -checked))
                                            (if (om/get-state owner :by-date)
                                              (org-decks-date decks owner)
                                              (org-decks-name decks owner))
                                            )}]]
             [:label "AL" [:input {:type "checkbox"
                              :value true
                              :checked (om/get-state owner :al)
                              :on-change #(do
                                            (om/set-state! owner :al (.. % -target -checked))
                                            (if (om/get-state owner :by-date)
                                              (org-decks-date decks owner)
                                              (org-decks-name decks owner))
                                            )}]]
             [:label "WL" [:input {:type "checkbox"
                              :value true
                              :checked (om/get-state owner :wl)
                              :on-change #(do
                                            (om/set-state! owner :wl (.. % -target -checked))
                                            (if (om/get-state owner :by-date)
                                              (org-decks-date decks owner)
                                              (org-decks-name decks owner))
                                            )}]]
             [:label "Dragon" [:input {:type "checkbox"
                              :value true
                              :checked (om/get-state owner :dragon)
                              :on-change #(do
                                            (om/set-state! owner :dragon (.. % -target -checked))
                                            (if (om/get-state owner :by-date)
                                              (org-decks-date decks owner)
                                              (org-decks-name decks owner))
                                            )}]]
             [:label "Date" [:input {:type "checkbox"
                              :value true
                              :checked (om/get-state owner :by-date)
                              :on-change #(do
                                            (om/set-state! owner :by-date (.. % -target -checked))
                                            (if (om/get-state owner :by-date)
                                              (org-decks-date decks owner)
                                              (org-decks-name decks owner))
                                            )}]]]

            [:div.deck-collection
             (when-not (:edit state)
               (if (and (nil? (om/get-state owner :decks-filtered))
                       (and
                         (om/get-state owner :wizard)
                         (om/get-state owner :minion)
                         (om/get-state owner :fallen)
                         (om/get-state owner :balrog)
                         (om/get-state owner :el)
                         (om/get-state owner :dl)
                         (om/get-state owner :al)
                         (om/get-state owner :wl)
                         (om/get-state owner :dragon)
                         (om/get-state owner :by-date)))
               (om/build deck-collection {:sets sets :decks decks :decks-loaded decks-loaded :active-deck (om/get-state owner :deck)})
               (om/build deck-collection {:sets sets :decks (om/get-state owner :decks-filtered) :decks-loaded decks-loaded :active-deck (om/get-state owner :deck)})))
             ]
            [:div {:class (when (:edit state) "edit")}
             (when-let [line (om/get-state owner :zoom)]
               (if (:standard (decks/calculate-deck-status (om/get-state owner :deck)))
                 (om/build card-ndce (:card line) {:state {:cursor cursor}})
                 (om/build card-dce (:card line) {:state {:cursor cursor}})))]]

           [:div.decklist
            (when-let [deck (:deck state)]
              (let [identity (:identity deck)
                    resources (:resources deck)
                    hazards (:hazards deck)
                    sideboard (:sideboard deck)
                    characters (:characters deck)
                    pool (:pool deck)
                    fwsb (:fwsb deck)
                    note (:note deck)
                    edit? (:edit state)
                    delete? (:delete state)]
                [:div
                 (cond
                   edit? [:div.button-bar
                          [:button {:on-click #(save-deck cursor owner)} "Save"]
                          [:button {:on-click #(cancel-edit owner)} "Cancel"]
                          (if (some #{(get-in @app-state [:user :username])} (get-in @app-state [:donators]))
                          [:h3.rgtlabel "Donator deck dice:  "
                          [:select {:value (:donate-dice deck)
                                             :on-change #(om/set-state! owner [:deck :donate-dice] (.. % -target -value))}
                           (for [option [{:name "empty"                          :ref "empty"}
                                         {:name "Black Flat Red Pips 16mm"       :ref "blck-16"}
                                         {:name "Black Swirl Red Pips 18mm"      :ref "blacks-18"}
                                         {:name "Silver Swirl Red Pips 16mm"     :ref "greys-16"}
                                         {:name "Grey Swirl Red Pips 18mm"       :ref "greys-18"}
                                         {:name "Dk. Gold Swirl Black Pips 16mm" :ref "gsdark-16"}
                                         {:name "Lt. Gold Swirl Black Pips 18mm" :ref "gslight-18"}
                                         {:name "Orange Flat Black Pips 16mm"    :ref "orgblack-16"}
                                         {:name "Red Swirl Black Pips 16mm"      :ref "rsblack-16"}
                                         {:name "Red Swirl Black Pips 18mm"      :ref "rsblack-18"}
                                         {:name "Red Swirl White Pips 16mm"      :ref "rswhite-16"}
                                         {:name "Saruman Rune 18mm"              :ref "rune-s-18"}
                                         {:name "Gandalf Rune 18mm"              :ref "rune-g-18"}
                                         {:name "Radagast Rune 18mm"             :ref "rune-r-18"}
                                         {:name "Alatar Rune 18mm"               :ref "rune-a-18"}
                                         {:name "Pallando Rune 18mm"             :ref "rune-p-18"}
                                         {:name "Ringwraith Rune 18mm"           :ref "rune-w-18"}]]
                             [:option {:value (:ref option)} (:name option)])]
                           [:select {:value (:donate-size deck)
                                     :on-change #(om/set-state! owner [:deck :donate-size] (.. % -target -value))}
                            (for [option [{:name "none"     :ref "none"}
                                          {:name "16mm"     :ref "16mm"}
                                          {:name "18mm"     :ref "18mm"}]]
                              [:option {:value (:ref option)} (:name option)])]
                           ])
                          ]
                   delete? [:div.button-bar
                            [:button {:on-click #(handle-delete cursor owner)} "Confirm Delete"]
                            [:button {:on-click #(end-delete owner)} "Cancel"]]
                   :else [:div.button-bar
                          [:button {:on-click #(edit-deck owner)} "Edit"]
                          [:button {:on-click #(delete-deck owner)} "Delete"]
                          (when (and (:stats deck) (not= "none" (get-in @app-state [:options :deckstats])))
                            [:button {:on-click #(clear-deck-stats cursor owner)} "Clear Stats"])])
                 [:h3 (:name deck)]
                 [:div.header
                  [:img {:src (image-url identity)
                         :alt (:title identity)}]
                  [:h4 {:class (if (decks/released? (:sets @app-state) identity) "fake-link" "casual")
                        :on-mouse-enter #(put! zoom-channel {:card identity :art (:art identity) :id (:id identity)})
                        :on-mouse-leave #(put! zoom-channel false)}
                   (:title identity)
                   (if (decks/banned? identity)
                     banned-span
                     (when (:released identity) rotated-span))]
                  (let [count (+ (+ (decks/card-count (:resources deck))
                                    (decks/card-count (:hazards deck)))
                                 (decks/card-count (:characters deck)))
                        min-count (decks/min-deck-size identity)]
                    [:div count " cards"
                     (when (< count min-count)
                       [:span.invalid (str " (minimum " min-count ")")])])
                  (comment
                  (let [inf (decks/influence-count deck)
                        id-limit (decks/id-inf-limit identity)]
                    [:div "Influence: "
                     ;; we don't use valid? and mwl-legal? functions here, since it concerns influence only
                     [:span {:class (if (> inf id-limit) (if (> inf id-limit) "invalid" "casual") "legal")} inf]
                     "/" (if (= INFINITY id-limit) "∞" id-limit)
                     (if (pos? inf)
                       (list " " (deck-influence-html deck)))]))
                  (when (= (:alignment identity) "Crazy")
                    (let [min-point (decks/min-agenda-points deck)
                          points (decks/agenda-points deck)]
                      [:div "Agenda points: " points
                       (when (< points min-point)
                         [:span.invalid " (minimum " min-point ")"])
                       (when (> points (inc min-point))
                         [:span.invalid " (maximum " (inc min-point) ")"])]))
                  [:div (deck-status-span sets deck true true false)]]
                 [:div.cards
                  (if (not-empty pool) [:h3 "Pool"])
                  (for [group (sort-by first (group-by #(get-in % [:card :Secondary]) pool))]
                    [:div.group
                     [:h4 (str (or (first group) "Unknown") " (" (decks/card-count (last group)) ")") ]
                     (for [line (sort-by #(get-in % [:card :title]) (last group))]
                       [:div.line
                        (when (:edit state)
                          (let [ch (om/get-state owner :pool-edit-channel)]
                            [:span
                             [:button.small {:on-click #(put! ch {:qty 1 :card (:card line)})
                                             :type "button"} "+"]
                             [:button.small {:on-click #(put! ch {:qty -1 :card (:card line)})
                                             :type "button"} "-"]]))
                        (line-span sets deck line)])])]
                 [:div.cards
                  (if (not-empty characters) [:h3 "Characters"])
                  (for [group (sort-by first (group-by #(get-in % [:card :Race]) characters))]
                    [:div.group
                     [:h4 (str (or (first group) "Unknown") " (" (decks/card-count (last group)) ")") ]
                     (for [line (sort-by #(get-in % [:card :title]) (last group))]
                       [:div.line
                        (when (:edit state)
                          (let [ch (om/get-state owner :character-edit-channel)]
                            [:span
                             [:button.small {:on-click #(put! ch {:qty 1 :card (:card line)})
                                             :type "button"} "+"]
                             [:button.small {:on-click #(put! ch {:qty -1 :card (:card line)})
                                             :type "button"} "-"]]))
                        (line-span sets deck line)])])]
                 [:div.cards
                  (if (not-empty resources) [:h3 (str "Resources: " (decks/card-count resources) "")])
                  (for [group (sort-by first (group-by #(get-in % [:card :Secondary]) resources))]
                    [:div.group
                     [:h4 (str (or (first group) "Unknown") " (" (decks/card-count (last group)) ")") ]
                     (for [line (sort-by #(get-in % [:card :title]) (last group))]
                       [:div.line
                        (when (:edit state)
                          (let [ch (om/get-state owner :resource-edit-channel)]
                            [:span
                             [:button.small {:on-click #(put! ch {:qty 1 :card (:card line)})
                                             :type "button"} "+"]
                             [:button.small {:on-click #(put! ch {:qty -1 :card (:card line)})
                                             :type "button"} "-"]]))
                        (line-span sets deck line)])])]
                 [:div.cards
                  (if (not-empty hazards) [:h3 (str "Hazards: " (decks/card-count hazards) "")])
                  (for [group (sort-by first (group-by #(get-in % [:card :Secondary]) hazards))]
                    [:div.group
                     [:h4 (str (or (first group) "Unknown") " (" (decks/card-count (last group)) ")") ]
                     (for [line (sort-by #(get-in % [:card :title]) (last group))]
                       [:div.line
                        (when (:edit state)
                          (let [ch (om/get-state owner :hazard-edit-channel)]
                            [:span
                             [:button.small {:on-click #(put! ch {:qty 1 :card (:card line)})
                                             :type "button"} "+"]
                             [:button.small {:on-click #(put! ch {:qty -1 :card (:card line)})
                                             :type "button"} "-"]]))
                        (line-span sets deck line)])])]
                 [:div.cards
                  (if (not-empty sideboard) [:h3 (str "Sideboard: " (decks/card-count sideboard) "")])
                  (for [group (sort-by first (group-by #(get-in % [:card :type]) sideboard))]
                    [:div.group
                     [:h4 (str (or (first group) "Unknown") " (" (decks/card-count (last group)) ")") ]
                     (for [line (sort-by #(get-in % [:card :title]) (last group))]
                       [:div.line
                        (when (:edit state)
                          (let [ch (om/get-state owner :sideboard-edit-channel)]
                            [:span
                             [:button.small {:on-click #(put! ch {:qty 1 :card (:card line)})
                                             :type "button"} "+"]
                             [:button.small {:on-click #(put! ch {:qty -1 :card (:card line)})
                                             :type "button"} "-"]]))
                        (line-span sets deck line)])])]
                 [:div.cards
                  (if (not-empty fwsb) [:h3 "FW-DC-SB"])
                  (for [group (sort-by first (group-by #(get-in % [:card :type]) fwsb))]
                    [:div.group
                     [:h4 (str (or (first group) "Unknown") " (" (decks/card-count (last group)) ")") ]
                     (for [line (sort-by #(get-in % [:card :title]) (last group))]
                       [:div.line
                        (when (:edit state)
                          (let [ch (om/get-state owner :fwsb-edit-channel)]
                            [:span
                             [:button.small {:on-click #(put! ch {:qty 1 :card (:card line)})
                                             :type "button"} "+"]
                             [:button.small {:on-click #(put! ch {:qty -1 :card (:card line)})
                                             :type "button"} "-"]]))
                        (line-span sets deck line)])])]
                 [:div.cards
                  (if (not-empty note) [:h3 "Deck Notes"])
                    [:div.note
                     [:h4 (str note)]]]
                 ]))]

           [:div.deckedit
            [:div
             [:p
              [:h3.lftlabel "Deck name"]
              [:h3.rgtlabel "Avatar"]
              [:input.deckname {:type "text" :placeholder "Deck name"
                                :ref "deckname" :value (get-in state [:deck :name])
                                :on-change #(om/set-state! owner [:deck :name] (.. % -target -value))}]]
             [:p
              [:select.identity {:value (identity-option-string (get-in state [:deck :identity]))
                                 :on-change #(om/set-state! owner [:deck :identity] (create-identity state %))}
               (let [idents (alignment-identities (get-in state [:deck :identity :alignment]))]
                 (for [card (sort-by :display-name idents)]
                   [:option
                    {:value (identity-option-string card)}
                    (:display-name card)]))]]
             (om/build card-lookup cursor {:state state})
             [:div
              [:h3.column1 "Resources"]
              [:h3.column2 "Hazards"]
              [:h3.column3 "Sideboard"]
              ]

             [:textarea.txttop {:ref "resource-edit" :value (:resource-edit state)
                                :on-change #(handle-resource-edit owner)}]
             [:textarea.txttop {:ref "hazard-edit" :value (:hazard-edit state)
                                :on-change #(handle-hazard-edit owner)}]
             [:textarea.txttop {:ref "sideboard-edit" :value (:sideboard-edit state)
                                :on-change #(handle-sideboard-edit owner)}]
             [:div
              [:h3.column1 "Characters"]
              [:h3.column2 "Pool"]
              [:h3.column3 "FW-DC-SB"]
              ]

             [:textarea.txtbot {:ref "character-edit" :value (:character-edit state)
                                :on-change #(handle-character-edit owner)}]
             [:textarea.txtbot {:ref "pool-edit" :value (:pool-edit state)
                                :on-change #(handle-pool-edit owner)}]
             [:textarea.txtbot {:ref "fwsb-edit" :value (:fwsb-edit state)
                                :on-change #(handle-fwsb-edit owner)}]
             [:div
              [:h3 "Deck Notes"]]

             [:textarea.txtnte {:ref "deck-note" :value (:deck-note state)
                                :on-change #(handle-deck-note owner)}]
             ]]]]]))))

(go (swap! app-state assoc :donators (:json (<! (GET "/data/donors")))))

(go (let [cards (<! cards-channel)
          decks (process-decks (:json (<! (GET (str "/data/decks")))))]
      (load-decks decks)
      (>! cards-channel cards)))

(om/root deck-builder app-state {:target (. js/document (getElementById "deckbuilder"))})
