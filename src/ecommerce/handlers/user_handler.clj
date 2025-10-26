(ns ecommerce.handlers.user-handler
  (:require
   [honey.sql :as sql]
   [next.jdbc :as jdbc]))

(defn get-user [request]
  (let [ds (:datasource request)
        user-id (get-in request [:params :id])
        query (sql/format
               {:select [:*]
                :from [:customer]
                :where [:= :id user-id]}
               :inline true)
        user (jdbc/execute-one! ds query)]

    (if user
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body {:user user}}
      {:status 404
       :headers {"Content-Type" "application/json"}
       :body {:error "User not found"}})))
