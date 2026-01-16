(ns ecommerce.queries.register-queries
  (:require
   [honey.sql :as sql]))

(defn get-user-by-email [email]
  (sql/format
   {:select [:customer.id :customer.names :customer.first_surname :customer.second_surname
             :customer.email :customer.active]
    :from [:customer]
    :where [:= :customer.email email]}
   :inline true))

(defn create-user [{:keys [names first_surname second_surname email country_of_birth birthday gender
                           rfc curp password phone_number phone_code country_code role]}]
  (sql/format
   {:insert-into :customer
    :columns [:names :first_surname :second_surname :email :country_of_birth :birthday
              :gender :rfc :curp :password :phone_number :phone_code :country_code
              :role :active]
    :values [[names first_surname second_surname email country_of_birth birthday gender
              rfc curp password phone_number phone_code country_code role false]]}
   :inline true))
