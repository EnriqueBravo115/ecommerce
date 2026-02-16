(ns ecommerce.queries.register-queries
  (:require
   [honey.sql :as sql]))

(defn get-customer-by-email [email]
  (sql/format
   {:select [:id :active :password]
    :from [:customer]
    :where [:= :email email]}))

(defn create-customer [{:keys [names first_surname second_surname email country_of_birth birthday
                               gender rfc curp password phone_number phone_code country_code]}]
  (sql/format
   {:insert-into :customer
    :columns [:names :first_surname :second_surname :email :country_of_birth :birthday
              :gender :rfc :curp :password :phone_number :phone_code :country_code
              :role :active]
    :values [[names first_surname second_surname email country_of_birth
              (when birthday (java.time.LocalDate/parse birthday))
              gender rfc curp password phone_number phone_code country_code
              "CUSTOMER" false]]}))

(defn update-customer [id {:keys [names first_surname second_surname email country_of_birth
                                  birthday gender rfc curp password phone_number phone_code
                                  country_code]}]
  (sql/format
   {:update :customer
    :set {:names names :first_surname first_surname :second_surname second_surname :email email
          :country_of_birth country_of_birth :birthday birthday :gender gender :rfc rfc :curp curp
          :password password :phone_number phone_number :phone_code phone_code :country_code country_code}
    :where [:= :id id]}))

(defn update-activation-code [{:keys [id activation_code]}]
  (sql/format
   {:update :customer
    :set {:activation_code activation_code}
    :where [:= :id id]}))

(defn get-user-by-activation-code [code]
  (sql/format
   {:select [:id :email]
    :from [:customer]
    :where [:= :activation_code code]}))

(defn clear-activation-code [{:keys [id]}]
  (sql/format
   {:update :customer
    :set {:activation_code nil}
    :where [:= :id id]}))

(defn activate-user [{:keys [id]}]
  (sql/format
   {:update :customer
    :set {:active true}
    :where [:= :id id]}))
