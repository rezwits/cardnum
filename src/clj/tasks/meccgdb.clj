(ns tasks.meccgdb
  "MECCG import tasks"
  (:require [org.httpkit.client :as http]
            [web.db :refer [db] :as webdb]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [throttler.core :refer [throttle-fn]]
            [clojure.string :as string]
            [clojure.data :as data]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint] :as pprint]
            [cheshire.core :as json]))

(declare faction-map)

(def ^:const dc-image-url "https://github.com/vastorper/dc/blob/master/graphics/Metw/")
(def ^:const me-image-url "https://github.com/rezwits/cardnum/blob/table/fdata/")

(defmacro rename
  "Rename a card field"
  [new-name]
  `(fn [[k# v#]] [~new-name v#]))

(def set-fields
  {
   :name identity
   :code identity
   :format identity
   :position identity
   :dreamcards (rename :dreamcard)
   :released identity
   })

(def mwl-fields
  {
   :NameEN (rename :name)
   :code identity
   :playableAlignment identity
   :unplayableAlignment identity
   :effectedAlignment identity
   :uneffectedAlignment identity
   :swapableAlignment identity
   :Set identity
   })

(def card-fields
  {
   :Set (rename :set_code)
   :Primary (rename :type)
   :Alignment (rename :alignment)
   :Artist identity
   :Rarity identity
   :Precise identity
   :NameEN (rename :title)
   :NameDU (rename :title-du)
   :NameSP (rename :title-es)
   :NameFN (rename :title-fn)
   :NameFR (rename :title-fr)
   :NameGR (rename :title-gr)
   :NameIT (rename :title-it)
   :NameJP (rename :title-jp)
   :ImageName identity
   :Text (rename :text)
   :Skill (rename :subtype)
   :MPs identity
   :Mind identity
   :Direct identity
   :General identity
   :Prowess identity
   :Body identity
   :Corruption identity
   :Home identity
   :Unique (fn [[k v]] [:uniqueness (if (= v "unique") true false)])
   :Secondary identity
   :Race identity
   :RWMPs identity
   :Site identity
   :Path identity
   :Region identity
   :RPath identity
   :Playable identity
   :GoldRing (fn [[k v]] [:GoldRing (if (= v "yes") true "")])
   :GreaterItem (fn [[k v]] [:GreaterItem (if (= v "yes") true "")])
   :MajorItem (fn [[k v]] [:MajorItem (if (= v "yes") true "")])
   :MinorItem (fn [[k v]] [:MinorItem (if (= v "yes") true "")])
   :Information (fn [[k v]] [:Information (if (= v "yes") true "")])
   :Palantiri (fn [[k v]] [:Palantiri (if (= v "yes") true "")])
   :Scroll (fn [[k v]] [:Scroll (if (= v "yes") true "")])
   :Hoard (fn [[k v]] [:Hoard (if (= v "yes") true "")])
   :Gear (fn [[k v]] [:Gear (if (= v "yes") true "")])
   :Non (fn [[k v]] [:Non (if (= v "yes") true "")])
   :Haven identity
   :Stage identity
   :Strikes identity
   :Specific identity
   :code (rename :trimCode)
   :fullCode (rename :code)
   :gccgAlign (fn [[k v]] [:gccgAlign (.replace (.replace v "[" "") "]" "")])
   :gccgSet (fn [[k v]] [:gccgSet (.replace (.replace v "(" "") ")" "")])
   :normalizedtitle (rename :flip-title)
   :DCpath identity
   :dreamcard identity
   :released identity
   :erratum identity
   :ice_errata (rename :errata)
   :extras identity
   })

(def tables
  {:mwl   {:path "bans-dc.json"   :fields mwl-fields   :collection "mwl"}
   :set   {:path "sets-dc.json"   :fields set-fields   :collection "sets"}
   :card  {:path "cards-dc.json"  :fields card-fields  :collection "cards"}
   :config {:collection "config"}})

(defn- translate-fields
  "Modify NRDB json data to our schema"
  [fields data]
  (reduce-kv (fn [m k v]
               (if (contains? fields k)
                 (let [[new-k new-v] ((get fields k) [k v])]
                   (assoc m new-k new-v))
                 m))
             {} data))

(defn- parse-response
  "Parse the http response sent from NRDB"
  [body fields]
  (->> body
       (#(json/parse-string % true))
       :data
       (map (partial translate-fields fields))))

(defn download-meccgdb-data
  "Translate data from NRDB"
  [path fields]
  (println "Downloading" path)
  (let [{:keys [status body error] :as resp} @(http/get (str me-image-url path "?raw=true"))]
    (cond
      error (throw (Exception. (str "Failed to download file " error)))
      (= 200 status) (parse-response (str "{\"data\": " body "}") fields)
      :else (throw (Exception. (str "Failed to download file, status " status))))))

(defn read-local-data
  "Translate data read from local files"
  [base-path filename fields]
  (let [filepath (str base-path "/" filename)
        _ (println "Reading" filepath)
        content (slurp filepath)
        wrapped (str "{\"data\": " content "}")]
    (parse-response wrapped fields)))

(defn read-card-dir
  [base-path _ fields]
  (let [dirpath base-path
        _ (println "Reading card directory" dirpath)
        files (mapv str (filter #(.isFile %) (file-seq (clojure.java.io/file dirpath))))
        json-files (filter #(string/ends-with? % ".json") files)
        contents (map slurp json-files)
        parsed (map #(json/parse-string % true) contents)
        combined (flatten parsed)
        wrapped (json/generate-string {:data combined})]
    (parse-response wrapped fields)))

(defn replace-collection
  "Remove existing collection and insert new data"
  [collection data]
  (mc/remove db collection)
  (mc/insert-batch db collection data))

(defn- make-map-by-code
  "Make a map of the items in the list using the :code as the key"
  [l]
  (reduce #(assoc %1 (:code %2) %2) {} l))

(defn deaccent
  "Remove diacritical marks from a string, from http://www.matt-reid.co.uk/blog_post.php?id=69"
  [s]
  (if (nil? s) ""
               (let [normalized (java.text.Normalizer/normalize s java.text.Normalizer$Form/NFD)]
                 (string/replace normalized #"\p{InCombiningDiacriticalMarks}+" ""))))

(defn- prune-null-fields
  "Remove specified fields if the value is nil"
  [c fields]
  (reduce (fn [acc k]
            (if (or (nil? (c k))
                    (= (c k) ""))
              (dissoc acc k)
              acc))
          c fields))

(defn- make-image-url
  "Create a URI to the card in CardGameDB"
  [card]
  ;;(str dc-image-url (:DCpath card)))
  (string/replace (str dc-image-url (:DCpath card) "?raw=true") " " "%20"))

(defn- add-card-fields
  "Add additional fields to the card documents"
  [set-map c]
  (let [s (set-map (:set_code c))]
    (-> c
        (prune-null-fields [:Artist :MPs :Mind :Direct :General
                            :Prowess :Body :Corruption :RWMPs
                            :Site :Path :Region :RPath
                            :Playable :GoldRing :GreaterItem
                            :MajorItem :MinorItem :Information
                            :Palantiri :Scroll :Haven :Stage
                            :Strikes :Specific])
        (assoc :full_set (:name s)
               :normalizedtitle (string/lower-case (deaccent (:title c)))))))

(defn fetch-data
  "Read NRDB json data. Modify function is mapped to all elements in the data collection."
  ([download-fn m] (fetch-data download-fn m identity))
  ([download-fn m modify-function] (fetch-data download-fn m modify-function replace-collection))
  ([download-fn {:keys [path fields collection]} modify-function collection-function]
   (let [data-list (->> (download-fn path fields)
                        (map modify-function))]
     (collection-function collection data-list)
     (make-map-by-code data-list))))

(defn rotate-cards
  "Added rotation fields to cards"
  [acc [_title prev curr]]
  (-> acc
      (assoc-in [prev :replaced_by] curr)
      (assoc-in [curr :replaces] prev)))

(defn- card-image-file
  "Returns the path to a card's image as a File"
  [card]
  (io/file "resources" "public" "img" "cards" (str (:set_code card)) (str (:ImageName card))))

(defn- download-card-image
  "Download a single card image from NRDB"
  [card]
  (println "Downloading: " (:title card) " (" (:ImageName card) ")")
  (let [card_url (make-image-url card)]
    (http/get card_url {:as :byte-array :timeout 120000}
              (fn [{:keys [status body error]}]
                (case status
                  404 (println "No image for card" (:code card) (:title card))
                  200 (with-open [w (io/output-stream (.getPath (card-image-file card)))]
                        (.write w body))
                  (println "Error downloading art for card" (:title card) error))))))

(def download-card-image-throttled (throttle-fn download-card-image 5 :second))

(defn download-card-images
  "Download card images (if necessary) from NRDB"
  [card-map]
  (doseq [set ["METW" "MEUL" "METD" "MEDM" "MELE" "MEAS"
               "MEWH" "MEBA" "MEFB" "MEDF" "MENE" "MEBO"
               "MECA" "MECP" "MEDS" "MEGW" "MEKN" "MEML"
               "MEMM" "MENW" "MERN" "MERS" "MESL" "METI"
               "MEWR" "MEBU" "MEXX"]]
    (let [img-dir (io/file "resources" "public" "img" "cards" set)]
      (when-not (.isDirectory img-dir)
        (println "Creating card images directory [" (.getPath img-dir) "]")
        (.mkdir img-dir))))
  (let [cards (vals card-map)
        missing-cards (remove #(.exists (card-image-file %)) cards)
        missing (count missing-cards)]
    (when (> missing 0)
      (println "Downloading art for" missing "cards...")
      (let [futures (doall (map download-card-image-throttled missing-cards))]
        (doseq [resp futures]
          ; wait for all the GETs to complete
          (:status @resp)))
      (println "Finished downloading card art"))))

(defn fetch-cards
  "Find the NRDB card json files and import them."
  [download-fn {:keys [collection path] :as card-table} sets download-images]
  (let [cards (fetch-data download-fn
                          card-table
                          (partial add-card-fields sets)
                          (fn [c d] true))]
    (spit "data/cards.json" (str cards))
    (mc/remove db collection)
    (mc/insert-batch db collection (vals cards))
    (when download-images
      (download-card-images cards))
    cards))

(defn update-config
  "Store import meta info in the db"
  [{:keys [collection]}]
  (mc/update db collection
             {:cards-version {$exists true}}
             {$inc {:cards-version 1}
              $currentDate {:last-updated true}}
             {:upsert true}))
