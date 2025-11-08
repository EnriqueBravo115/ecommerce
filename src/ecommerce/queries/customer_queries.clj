(ns ecommerce.queries.customer-queries
  (:require [honey.sql :as sql]))

(defn customer-by-id [id]
  (sql/format
   {:select [:names :first_surname :second_surname :email :active]
    :from   [:customer]
    :where  [:= :id id]}
   :inline true))

(defn country-count []
  (sql/format
   {:select   [:country_of_birth
               [(sql/call :count :*) :count]]
    :from     [:customer]
    :group-by [:country_of_birth]}
   :inline true))

(defn customers-age-groups []
  (sql/format {:select [[[:raw "EXTRACT(YEAR FROM AGE(CURRENT_DATE, birthday::date))"] :age]]
               :from   [:customer]}
              :inline true))

(defn customers-by-gender [gender]
  (sql/format
   {:select [:names :first_surname :second_surname :email :active :gender]
    :from [:customer]
    :where [:= :gender gender]}))

(defn registration-date []
  (sql/format {:select [:registration_date]
               :from   [:customer]}))
