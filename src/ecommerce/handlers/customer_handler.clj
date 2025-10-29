(ns ecommerce.handlers.customer-handler
  (:require
   [honey.sql :as sql]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]))

(defn get-customer-by-id [request]
  (let [ds (:datasource request)
        user-id (get-in request [:params :id])
        query (sql/format
               {:select [:names :first_surname :second_surname :email :active]
                :from [:customer]
                :where [:= :id user-id]}
               :inline true)
        result (jdbc/execute-one! ds query {:builder-fn rs/as-unqualified-maps})]

    (if result
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body {:customer result}}
      {:status 404
       :headers {"Content-Type" "application/json"}
       :body {:error "User not found"}})))

(defn get-customers-country-count [request]
  (let [ds (:datasource request)
        query (sql/format
               {:select [:country_of_birth
                         [(sql/call :count :*) :count]]
                :from [:customer]
                :group-by [:country_of_birth]}
               :inline true)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

    (if result
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body {:country_count result}}
      {:status 404
       :headers {"Content-Type" "application/json"}
       :body {:error "No customers found"}})))

(defn get-customers-by-age-group [request]
  (let [ds (:datasource request)
        query (sql/format {:select [[[:raw "EXTRACT(YEAR FROM AGE(CURRENT_DATE, birthday::date))"] :age]]
                           :from [:customer]}
                          :inline true)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})
        classify-age (fn [age]
                       (cond
                         (<= 0 age 17) "0-17"
                         (<= 18 age 29) "18-29"
                         (<= 30 age 39) "30-39"
                         (<= 40 age 49) "40-49"
                         (<= 50 age 59) "50-59"
                         (<= 60 age 69) "60-69"
                         :else "70+"))
        grouped-result (->> result
                            (map :age)
                            (remove nil?)
                            (map #(classify-age (int %)))
                            frequencies
                            (map (fn [[age-group count]] {:age-range age-group :count count}))
                            (sort-by :age-range))]

    (if result
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body {:age_group grouped-result}}
      {:status 404
       :headers {"Content-Type" "application/json"}
       :body {:error "No customers found"}})))
