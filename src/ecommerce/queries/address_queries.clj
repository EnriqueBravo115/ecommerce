(ns ecommerce.queries.address-queries
  (:require
   [honey.sql :as sql]))

(defn create-address [{:keys [customer_id country state city street postal_code is_primary]}]
  (sql/format
   {:insert-into :address
    :columns [:customer_id :country :state :city :street :postal_code :is_primary]
    :values [[customer_id country state city street postal_code is_primary]]} :inline true))

(defn count-addresses [customer-id]
  (sql/format
   {:select [:%count.*]
    :from [:address]
    :where [:= :customer_id customer-id]} :inline true))

(defn has-primary-address [customer-id]
  (sql/format
   {:select [:%count.*]
    :from [:address]
    :where [:and
            [:= :customer_id customer-id]
            [:= :is_primary true]]} :inline true))

(defn unset-existing-primary [customer-id]
  (sql/format
   {:update :address
    :set {:is_primary false}
    :where [:and
            [:= :customer_id customer-id]
            [:= :is_primary true]]} :inline true))

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

(defn update-address [address_id {:keys [country state city street postal_code]}]
  (sql/format
   {:update :address
    :set {:country country :state state :city city :street street :postal_code postal_code}
    :where [:= :id address_id]}
   :inline true))

(defn delete-address [address_id]
  (sql/format
   {:delete-from :address
    :where [:= :id address_id]}
   :inline true))

(defn unset-all-primary [customer_id]
  (sql/format
   {:update :address
    :set {:is_primary false}
    :where [:= :customer_id customer_id]}))

(defn set-primary [address_id]
  (sql/format
   {:update :address
    :set {:is_primary true}
    :where [:= :id address_id]}))

(defn get-primary-address [customer_id]
  (sql/format
   {:select [:country :state :city :street :postal_code :is_primary]
    :from [:address]
    :where [:and
            [:= :customer_id customer_id]
            [:= :is_primary true]]}))

(defn get-customers-by-location [country state city]
  (sql/format
   {:select [:c.names :c.first_surname :c.second_surname :c.email :c.active]
    :from [[:customer :c]]
    :join [[:address :a] [:= :c.id :a.customer_id]]
    :where [:and
            [:or [:= country nil] [:= :a.country country]]
            [:or [:= state nil] [:= :a.state state]]
            [:or [:= city nil] [:= :a.city city]]]}))

(defn get-customers-by-postal-code [postal_code]
  (sql/format
   {:select [:c.names :c.first_surname :c.second_surname :c.email :c.active]
    :from [[:customer :c]]
    :join [[:address :a] [:= :c.id :a.customer_id]]
    :where [:= :a.postal_code postal_code]}))

(defn get-top-countries []
  (sql/format
   {:select [:a.country [:%count.* :customer_count]]
    :from [[:address :a]]
    :group-by [:a.country]
    :order-by [[:customer_count :desc]]
    :limit 10}))

(defn get-top-states []
  (sql/format
   {:select [:a.state :a.country [:%count.* :customer_count]]
    :from [[:address :a]]
    :group-by [:a.state :a.country]
    :order-by [[:customer_count :desc]]
    :limit 10}))

(defn get-top-cities []
  (sql/format
   {:select [:a.city :a.state :a.country [:%count.* :customer_count]]
    :from [[:address :a]]
    :group-by [:a.city :a.state :a.country]
    :order-by [[:customer_count :desc]]
    :limit 10}))
