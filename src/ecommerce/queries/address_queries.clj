(ns ecommerce.queries.address-queries
  (:require
   [honey.sql :as sql]))

(defn create-address [{:keys [user_id country state city street postal_code is_primary]}]
  (sql/format
   {:insert-into :address
    :columns [:user_id :country :state :city :street :postal_code :is_primary]
    :values [[user_id country state city street postal_code is_primary]]} :inline true))

;;(def get-addresses-by-user-id
;;  "SELECT * FROM address WHERE user_id = ? ORDER BY is_primary DESC, id")
