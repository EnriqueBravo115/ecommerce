(ns ecommerce.handlers.customer-handler
  (:require
   [honey.sql :as sql]
   [next.jdbc :as jdbc]))

(defn get-customer-basic [request]
  (let [ds (:datasource request)
        user-id (get-in request [:params :id])
        query (sql/format
               {:select [:names :first_surname :second_surname :email :registration_date :active]
                :from [:customer]
                :where [:= :id user-id]}
               :inline true)
        customer (jdbc/execute-one! ds query)]

    (if customer
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body {:customer customer}}
      {:status 404
       :headers {"Content-Type" "application/json"}
       :body {:error "User not found"}})))
