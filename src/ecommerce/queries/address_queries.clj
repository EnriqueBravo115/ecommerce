(ns ecommerce.queries.address-queries
  (:require
   [honey.sql :as sql]))

(defn create-address [{:keys [customer_id country state city street postal_code is_primary]}]
  (sql/format
   {:insert-into :address
    :columns [:customer_id :country :state :city :street :postal_code :is_primary]
    :values [[customer_id country state city street postal_code is_primary]]} :inline true))

(defn get-addresses-by-customer-id [customer_id]
  (sql/format
   {:select [:country :state :city :street :postal_code :is_primary]
    :from [:address]
    :where [:= :customer_id customer_id]
    :order-by [[:is_primary :desc]]}))

(defn get-address-by-id [address_id]
  (sql/format
   {:select [:customer_id :country :state :city :street :postal_code :is_primary]
    :from [:address]
    :where [:= :id address_id]}))

(defn update-address [address_id {:keys [country state city street postal_code is_primary]}]
  (sql/format
   {:update :address
    :set {:country country :state state :city city :street street :postal_code postal_code :is_primary is_primary}
    :where [:= :id address_id]}
   :inline true))
