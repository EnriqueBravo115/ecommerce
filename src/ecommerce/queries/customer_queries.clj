(ns ecommerce.queries.customer-queries
  (:require [honey.sql :as sql]))

(defn get-customer-by-id [id]
  (sql/format
   {:select [:names :first_surname :second_surname :email :active]
    :from   [:customer]
    :where  [:= :id id]}))

(defn get-country-count []
  (sql/format
   {:select   [:country_of_birth
               [(sql/call :count :*) :count]]
    :from     [:customer]
    :group-by [:country_of_birth]}))

(defn get-age []
  (sql/format {:select [[[:raw "EXTRACT(YEAR FROM AGE(CURRENT_DATE, birthday::date))"] :age]]
               :from   [:customer]}))

(defn get-by-gender [gender]
  (sql/format
   {:select [:names :first_surname :second_surname :email :active :gender]
    :from   [:customer]
    :where  [:= :gender gender]}))

(defn get-registration-date []
  (sql/format {:select [:registration_date]
               :from   [:customer]}))

;; COUNT(*) AS total
;; COUNT(CASE WHEN active = true THEN 1 END) AS active
(defn get-active-rate []
  (sql/format
   {:select [[(sql/call :count :*) :total]
             [(sql/call :count (sql/call :case [(sql/call := :active true)] 1)) :active]]
    :from   [:customer]}))

(defn get-inactive []
  (sql/format
   {:select [[(sql/call :count :*) :total]]
    :from   [:customer]
    :where  [:= :active false]}))

(defn get-segment-by-demographics [country gender min-age max-age]
  (let [base-conditions (remove nil? [(when country [:= :country_of_birth country])
                                      (when gender [:= :gender gender])])

        age-conditions (remove nil? [(when min-age
                                       [:>= [:raw "(EXTRACT(YEAR FROM AGE(CURRENT_DATE, birthday::date)))"]
                                        min-age])
                                     (when max-age
                                       [:<= [:raw "(EXTRACT(YEAR FROM AGE(CURRENT_DATE, birthday::date)))"]
                                        max-age])])

        all-conditions (into [:and] (concat base-conditions age-conditions))]

    (sql/format {:select [:names :first_surname :second_surname :email
                          :country_of_birth :gender :active
                          [[:raw "EXTRACT(YEAR FROM AGE(CURRENT_DATE, birthday::date))"] :age]]
                 :from   [:customer]
                 :where  (when (seq all-conditions) all-conditions)})))

(defn get-registration-by-country-code []
  (sql/format
   {:select   [:country_code
               [(sql/call :count :*) :registrations]]
    :from     [:customer]
    :where    [:not= :country_code nil]
    :group-by [:country_code]
    :order-by [[:country_code :asc]]}))

(defn get-customers-with-password-reset-code []
  (sql/format
   {:select   [:id :names :first_surname :second_surname
               :email :country_code :phone_number
               :password_reset_code :registration_date]
    :from     [:customer]
    :where    [:and
               [:not= :password_reset_code nil]
               [:= :active true]]
    :order-by [[:registration_date :desc]]}))
