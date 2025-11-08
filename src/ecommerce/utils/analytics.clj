(ns ecommerce.utils.analytics)

(defn calculate-trends [registrations period]
  (->> registrations
       (map :registration_date)
       (map #(if (instance? java.sql.Date %)
               (.toLocalDate %)
               (.toLocalDateTime %)))
       (group-by (case period
                   "day" #(.format (java.time.format.DateTimeFormatter/ofPattern "yyyy-MM-dd") %)
                   "month" #(str (.getYear %) "-" (.getMonthValue %))
                   "year" #(str (.getYear %))
                   #(str (.getYear %) "-" (.getMonthValue %))))
       (map (fn [[period dates]]
              {:period period
               :count (count dates)}))
       (sort-by :period)))
