(ns ecommerce.queries.seller-queries
  (:require
   [clojure.string :as str]
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
    :where [:= :id seller_id]}))

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

(defn update-seller-password [seller_id password]
  (sql/format
   {:update :seller
    :set {:password password
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
   {:select [:*]
    :from [:seller]
    :where [:= :status status]
    :order-by [[:created_at :desc]]}))

(defn get-sellers-by-verification-status [verified]
  (sql/format
   {:select [:verified [:%count.* :count]]
    :from [:seller]
    :group-by [:verified]
    :order-by [[:verified :desc]]}))

(defn get-top-sellers-by-sales [limit]
  (sql/format
   {:select [:business_name :total_sales :rating :country :verified]
    :from [:seller]
    :where [:> :total_sales 0]
    :order-by [[:total_sales :desc]]
    :limit limit}))

(defn get-sales-statistics []
  (sql/format
   {:select [[(sql/call :sum :total_sales) :total_sales_sum]
             [(sql/call :avg :total_sales) :total_sales_avg]
             [(sql/call :max :total_sales) :total_sales_max]
             [(sql/call :min :total_sales) :total_sales_min]
             [(sql/call :count :*) :seller_count]]
    :from [:seller]}))

(defn get-commission-statistics []
  (sql/format
   {:select [[(sql/call :avg :commission_rate) :avg_commission]
             [(sql/call :max :commission_rate) :max_commission]
             [(sql/call :min :commission_rate) :min_commission]]
    :from [:seller]}))

(defn get-sellers-created-by-period [period]
  (let [interval (case (str/lower-case period)
                   "day" "1 DAY"
                   "week" "1 WEEK"
                   "month" "1 MONTH"
                   "year" "1 YEAR"
                   "1 DAY")]
    (sql/format
     {:select [[:raw "DATE(created_at)"] :date
               [:%count.* :sellers_created]]
      :from [:seller]
      :where [:>= :created_at [:raw (str "CURRENT_DATE - INTERVAL '" interval "'")]]
      :group-by [:date]
      :order-by [[:date :asc]]})))

(defn get-sellers-by-location [country state city]
  (sql/format
   {:select [:business_name :legal_name :email :phone :status :verified :total_sales]
    :from [:seller]
    :where [:and
            [:or [:= country nil] [:= :country country]]
            [:or [:= state nil] [:= :state state]]
            [:or [:= city nil] [:= :city city]]]}))

(defn get-sellers-with-balance-threshold [threshold]
  (sql/format
   {:select [:business_name :account_balance :total_sales :bank_name]
    :from [:seller]
    :where [:> :account_balance threshold]
    :order-by [[:account_balance :desc]]}))

(defn get-rating-distribution []
  (sql/format
   {:select [[:raw "FLOOR(rating)"] :rating_range
             [:%count.* :seller_count]
             [(sql/call :avg :total_sales) :avg_sales]]
    :from [:seller]
    :where [:not= :rating nil]
    :group-by [:rating_range]
    :order-by [[:rating_range :asc]]}))

(defn get-unverified-sellers []
  (sql/format
   {:select [:business_name :email :created_at :country]
    :from [:seller]
    :where [:= :verified false]
    :order-by [[:created_at :desc]]}))

(defn get-sellers-needing-payment []
  (sql/format
   {:select [:business_name :account_balance :bank_account :bank_name]
    :from [:seller]
    :where [:> :account_balance 0]
    :order-by [[:account_balance :desc]]}))
