(ns ecommerce.queries.register-queries
  (:require
   [honey.sql :as sql]))

(defn get-customer-by-email [email]
  (sql/format
   {:select [:customer.id :customer.names :customer.first_surname :customer.second_surname
             :customer.email :customer.active]
    :from [:customer]
    :where [:= :customer.email email]}
   :inline true))

(defn create-customer [{:keys [names first_surname second_surname email country_of_birth birthday gender
                               rfc curp password phone_number phone_code country_code role]}]
  (sql/format
   {:insert-into :customer
    :columns [:names :first_surname :second_surname :email :country_of_birth :birthday
              :gender :rfc :curp :password :phone_number :phone_code :country_code
              :role :active]
    :values [[names first_surname second_surname email country_of_birth birthday gender
              rfc curp password phone_number phone_code country_code role false]]}
   :inline true))

(defn update-customer [id {:keys [names first_surname second_surname email country_of_birth
                                  birthday gender rfc curp password phone_number phone_code
                                  country_code]}]
  (sql/format
   {:update :customer
    :set {:names names
          :first_surname first_surname
          :second_surname second_surname
          :email email
          :country_of_birth country_of_birth
          :birthday birthday
          :gender gender
          :rfc rfc
          :curp curp
          :password password
          :phone_number phone_number
          :phone_code phone_code
          :country_code country_code}
    :where [:= :id id]}
   :inline true))

(defn update-activation-code [{:keys [id activation_code]}]
  (sql/format
   {:update :customer
    :set {:activation_code activation_code}
    :where [:= :id id]}
   :inline true))
