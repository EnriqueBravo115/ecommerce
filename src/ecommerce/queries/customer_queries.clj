(ns ecommerce.queries.customer-queries
  (:require [honey.sql :as sql]))

(defn get-by-id [id]
  (sql/format
   {:select [:names :first_surname :second_surname :email :active]
    :from   [:customer]
    :where  [:= :id id]}
   :inline true))

(defn get-country-count []
  (sql/format
   {:select   [:country_of_birth
               [(sql/call :count :*) :count]]
    :from     [:customer]
    :group-by [:country_of_birth]}
   :inline true))

(defn get-age []
  (sql/format {:select [[[:raw "EXTRACT(YEAR FROM AGE(CURRENT_DATE, birthday::date))"] :age]]
               :from   [:customer]}
              :inline true))

(defn get-by-gender [gender]
  (sql/format
   {:select [:names :first_surname :second_surname :email :active :gender]
    :from [:customer]
    :where [:= :gender gender]}))

(defn get-registration-date []
  (sql/format {:select [:registration_date]
               :from   [:customer]}))

;; COUNT(*) AS total
;; COUNT(CASE WHEN active = true THEN 1 END) AS active
(defn get-active-rate []
  (sql/format
   {:select [[(sql/call :count :*) :total]
             [(sql/call :count (sql/call :case [(sql/call := :active true)] 1)) :active]]
    :from [:customer]}))

(defn get-inactive []
  (sql/format
   {:select [[(sql/call :count :*) :total]]
    :from [:customer]
    :where [:= :active false]}))
