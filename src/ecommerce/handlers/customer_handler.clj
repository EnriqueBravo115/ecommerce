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
