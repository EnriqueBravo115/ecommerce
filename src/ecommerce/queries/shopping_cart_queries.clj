(ns ecommerce.queries.shopping-cart-queries
  (:require [honey.sql :as sql]))

(defn get-by-customer-id [customer-id]
  (sql/format
   {:select [:sc.*]
    :from [[:shopping_cart :sc]]
    :where [:= :sc.customer_id customer-id]
    :order-by [[:sc.added_at :desc]]}
   :inline true))

(defn get-cart-summary [customer-id]
  (sql/format
   {:select [[(sql/call :count :*) :total_items]
             [(sql/call :sum :quantity) :total_quantity]
             [(sql/call :max :added_at) :last_added]]
    :from [:shopping_cart]
    :where [:= :customer_id customer-id]}
   :inline true))

(defn upsert-cart-item [customer-id product-id quantity]
  (sql/format
   {:insert-into :shopping_cart
    :columns [:customer_id :product_id :quantity]
    :values [[customer-id product-id quantity]]
    :on-conflict [:customer_id :product_id]
    :do-update-set {:quantity [:+ :shopping_cart.quantity quantity]
                    :updated_at [:raw "current_timestamp"]}}
   :inline true))

(defn update-quantity [customer-id product-id quantity]
  (sql/format
   {:update :shopping_cart
    :set {:quantity quantity
          :updated_at [:raw "current_timestamp"]}
    :where [:and
            [:= :customer_id customer-id]
            [:= :product_id product-id]]}
   :inline true))

(defn delete-item [customer-id product-id]
  (sql/format
   {:delete-from :shopping_cart
    :where [:and
            [:= :customer_id customer-id]
            [:= :product_id product-id]]}
   :inline true))

(defn clear-cart [customer-id]
  (sql/format
   {:delete-from :shopping_cart
    :where [:= :customer_id customer-id]}
   :inline true))

(defn get-cart-count [customer-id]
  (sql/format
   {:select [[(sql/call :count :*) :item_count]]
    :from [:shopping_cart]
    :where [:= :customer_id customer-id]}
   :inline true))

(defn get-cart-total-value [customer-id]
  (sql/format
   {:select [[(sql/call :sum [:* :sc.quantity :p.price]) :total_value]]
    :from [[:shopping_cart :sc]]
    :join [[:product :p] [:= :sc.product_id :p.id]]
    :where [:= :sc.customer_id customer-id]}
   :inline true))

(defn get-cart-with-products [customer-id]
  (sql/format
   {:select [:sc.id :sc.customer_id :sc.quantity :sc.added_at :sc.updated_at
             :p.id :product_id :p.sku :p.name :product_name :p.price :product_price
             :p.compare_at_price :p.status :product_status
             :i.quantity :inventory_quantity :i.reserved :inventory_reserved
             [[:- :i.quantity :i.reserved] :available_stock]]
    :from [[:shopping_cart :sc]]
    :join [[:product :p] [:= :sc.product_id :p.id]]
    :left-join [[:inventory :i] [:= :p.id :i.product_id]]
    :where [:= :sc.customer_id customer-id]
    :order-by [[:sc.added_at :desc]]}
   :inline true))

(defn get-item [customer-id product-id]
  (sql/format
   {:select [:sc.*
             :p.name :product_name :p.price :product_price]
    :from [[:shopping_cart :sc]]
    :join [[:product :p] [:= :sc.product_id :p.id]]
    :where [:and
            [:= :sc.customer_id customer-id]
            [:= :sc.product_id product-id]]}
   :inline true))

(defn insert-cart-item [customer-id product-id quantity]
  (sql/format
   {:insert-into :shopping_cart
    :columns [:customer_id :product_id :quantity]
    :values [[customer-id product-id quantity]]}
   :inline true))

(defn get-recent-carts [limit]
  (sql/format
   {:select [:sc.customer_id
             :c.names :customer_name
             :c.email :customer_email
             [(sql/call :count :sc.*) :item_count]
             [(sql/call :sum :sc.quantity) :total_quantity]
             [(sql/call :max :sc.added_at) :last_activity]]
    :from [[:shopping_cart :sc]]
    :join [[:customer :c] [:= :sc.customer_id :c.id]]
    :group-by [:sc.customer_id :c.names :c.email]
    :order-by [[:last_activity :desc]]
    :limit limit}
   :inline true))

(defn get-abandoned-carts [days-threshold]
  (sql/format
   {:select [:sc.customer_id
             :c.names :customer_name
             :c.email :customer_email
             [(sql/call :count :sc.*) :item_count]
             [(sql/call :sum :sc.quantity) :total_quantity]
             [(sql/call :max :sc.added_at) :last_activity]]
    :from [[:shopping_cart :sc]]
    :join [[:customer :c] [:= :sc.customer_id :c.id]]
    :where [:<= :sc.added_at 
            [:raw (str "CURRENT_DATE - INTERVAL '" days-threshold " days'")]]
    :group-by [:sc.customer_id :c.names :c.email]
    :order-by [[:last_activity :asc]]}
   :inline true))
