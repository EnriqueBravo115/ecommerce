(ns ecommerce.queries.seller-queries
  (:require
   [honey.sql :as sql]))

(defn create-seller [{:keys [business_name legal_name tax_id email phone
                             country state city address postal_code website
                             status verified commission_rate rating total_sales
                             account_balance bank_account bank_name password]}]
  (sql/format
   {:insert-into :seller
    :columns [:business_name :legal_name :tax_id :email :phone
              :country :state :city :address :postal_code :website
              :status :verified :commission_rate :rating :total_sales
              :account_balance :bank_account :bank_name :password]
    :values [[business_name legal_name tax_id email phone
              country state city address postal_code website
              status verified commission_rate rating total_sales
              account_balance bank_account bank_name password]]}
   :inline true))

(defn get-seller-by-id [seller_id]
  (sql/format
   {:select [:*]
    :from [:seller]
    :where [:= :id seller_id]}
   :inline true))

(defn get-seller-by-email [email]
  (sql/format
   {:select [:id :business_name :email :verified :status :password]
    :from [:seller]
    :where [:= :email email]}))

(defn update-seller-location [seller_id {:keys [state city address postal_code]}]
  (sql/format
   {:update :seller
    :set {:state state
          :city city
          :address address
          :postal_code postal_code
          :updated_at [:raw "current_timestamp"]}
    :where [:= :id seller_id]}))

(defn delete-seller [seller_id]
  (sql/format
   {:delete-from :seller
    :where [:= :id seller_id]}
   :inline true))

(defn update-seller-status [seller_id status]
  (sql/format
   {:update :seller
    :set {:status status
          :updated_at [:raw "current_timestamp"]}
    :where [:= :id seller_id]}
   :inline true))

(defn verify-seller [seller_id]
  (sql/format
   {:update :seller
    :set {:verified true
          :updated_at [:raw "current_timestamp"]}
    :where [:= :id seller_id]}
   :inline true))

(defn get-sellers-by-country []
  (sql/format
   {:select [:country [:%count.* :seller_count]]
    :from [:seller]
    :group-by [:country]
    :order-by [[:seller_count :desc]]}))

(defn get-sellers-by-status [status]
  (sql/format
   {:select [:business_name :bank_name :email :verified :total_sales :account_balance]
    :from [:seller]
    :where [:= :status status]
    :order-by [[:created_at :desc]]}))

(defn get-top-sellers-by-sales [limit]
  (sql/format
   {:select [:business_name :total_sales :rating :country :verified]
    :from [:seller]
    :where [:> :total_sales 0]
    :order-by [[:total_sales :desc]]
    :limit limit}))

(defn get-unverified-sellers []
  (sql/format
   {:select [:business_name :email :created_at :country]
    :from [:seller]
    :where [:= :verified false]
    :order-by [[:created_at :desc]]}))
