(ns ecommerce.utils.analytics)

(defn- classify-age [age]
  (cond
    (<= 0 age 17) "0-17"
    (<= 18 age 29) "18-29"
    (<= 30 age 39) "30-39"
    (<= 40 age 49) "40-49"
    (<= 50 age 59) "50-59"
    (<= 60 age 69) "60-69"
    :else "70+"))

(defn grouped-result [result]
  (->> result
       (map :age)
       (remove nil?)
       (map #(classify-age (int %)))
       frequencies
       (map (fn [[age-group count]] {:age-range age-group :count count}))
       (sort-by :age-range)))

(defn calculate-trends [registrations period]
  (->> registrations
       (map :registration_date)
       (map #(if (instance? java.sql.Date %)
               (.toLocalDate %)
               (.toLocalDateTime %)))
       (group-by (case period
                   "DAY" #(.format (java.time.format.DateTimeFormatter/ofPattern "yyyy-MM-dd") %)
                   "MONTH" #(str (.getYear %) "-" (.getMonthValue %))
                   "YEAR" #(str (.getYear %))
                   #(str (.getYear %) "-" (.getMonthValue %))))
       (map (fn [[period dates]]
              {:period period
               :count (count dates)}))
       (sort-by :period)))
