(ns ecommerce.queries.inventory-queries
  (:require [honey.sql :as sql]))

(defn get-by-id [id]
  (sql/format
   {:select [:i.*
             :p.name :product_name
             :p.sku :product_sku
             :p.price :product_price]
    :from [[:inventory :i]]
    :left-join [[:product :p] [:= :i.product_id :p.id]]
    :where [:= :i.id id]}
   :inline true))

(defn get-by-sku [sku]
  (sql/format
   {:select [:i.*
             :p.name :product_name
             :p.sku :product_sku
             :p.price :product_price]
    :from [[:inventory :i]]
    :left-join [[:product :p] [:= :i.product_id :p.id]]
    :where [:= :i.sku sku]}
   :inline true))

(defn get-by-product-id [product-id]
  (sql/format
   {:select [:i.*
             :p.name :product_name
             :p.sku :product_sku]
    :from [[:inventory :i]]
    :left-join [[:product :p] [:= :i.product_id :p.id]]
    :where [:= :i.product_id product-id]
    :order-by [[:i.location :asc]]}
   :inline true))

(defn get-by-location [location]
  (sql/format
   {:select [:i.*
             :p.name :product_name
             :p.sku :product_sku
             :p.price :product_price]
    :from [[:inventory :i]]
    :left-join [[:product :p] [:= :i.product_id :p.id]]
    :where [:= :i.location location]
    :order-by [[:p.name :asc]]}
   :inline true))

(defn get-low-stock []
  (sql/format
   {:select [:i.*
             :p.name :product_name
             :p.sku :product_sku
             [[:- :i.quantity :i.reserved] :available]
             :i.low_stock_threshold]
    :from [[:inventory :i]]
    :left-join [[:product :p] [:= :i.product_id :p.id]]
    :where [:<= [:- :i.quantity :i.reserved] :i.low_stock_threshold]
    :order-by [[:available :asc]]}
   :inline true))

(defn get-below-reorder-point []
  (sql/format
   {:select [:i.*
             :p.name :product_name
             :p.sku :product_sku
             [[:- :i.quantity :i.reserved] :available]
             :i.reorder_point]
    :from [[:inventory :i]]
    :left-join [[:product :p] [:= :i.product_id :p.id]]
    :where [:<= [:- :i.quantity :i.reserved] :i.reorder_point]
    :order-by [[:available :asc]]}
   :inline true))

(defn get-summary []
  (sql/format
   {:select [[(sql/call :count :*) :total_items]
             [(sql/call :sum :quantity) :total_quantity]
             [(sql/call :sum :reserved) :total_reserved]
             [(sql/call :sum [:- :quantity :reserved]) :total_available]
             [(sql/call :count (sql/call :distinct :location)) :unique_locations]
             [(sql/call :count
               (sql/call :case
                 [:<= [:- :quantity :reserved] :low_stock_threshold] 1)) :low_stock_items]
             [(sql/call :count
               (sql/call :case
                 [:<= [:- :quantity :reserved] :reorder_point] 1)) :below_reorder_items]]
    :from [:inventory]}
   :inline true))

(defn get-by-available-quantity [min-available max-available]
  (let [conditions (remove nil?
                    [[:>= [:- :quantity :reserved] min-available]
                     (when max-available
                       [:<= [:- :quantity :reserved] max-available])])]
    (sql/format
     {:select [:i.*
               :p.name :product_name
               :p.sku :product_sku
               [[:- :i.quantity :i.reserved] :available]]
      :from [[:inventory :i]]
      :left-join [[:product :p] [:= :i.product_id :p.id]]
      :where (when (seq conditions) (into [:and] conditions))
      :order-by [[:available :desc]]}
     :inline true)))

(defn get-stats-by-location []
  (sql/format
   {:select [:location
             [(sql/call :count :*) :item_count]
             [(sql/call :sum :quantity) :total_quantity]
             [(sql/call :sum :reserved) :total_reserved]
             [(sql/call :sum [:- :quantity :reserved]) :total_available]
             [(sql/call :count
               (sql/call :case
                 [:<= [:- :quantity :reserved] :low_stock_threshold] 1)) :low_stock_items]]
    :from [:inventory]
    :where [:not= :location nil]
    :group-by [:location]
    :order-by [[:total_available :desc]]}
   :inline true))

(defn get-recently-restocked [days]
  (sql/format
   {:select [:i.*
             :p.name :product_name
             :p.sku :product_sku
             [[:- :i.quantity :i.reserved] :available]]
    :from [[:inventory :i]]
    :left-join [[:product :p] [:= :i.product_id :p.id]]
    :where [:>= :last_restocked [:raw (str "CURRENT_DATE - INTERVAL '" days " days'")]]
    :order-by [[:last_restocked :desc]]}
   :inline true))

(defn update-quantity [id quantity reserved]
  (sql/format
   {:update :inventory
    :set {:quantity quantity
          :reserved reserved
          :updated_at [:raw "current_timestamp"]}
    :where [:and
            [:= :id id]
            [:>= quantity reserved]]}
   :inline true))

(defn reserve-item [id amount]
  (sql/format
   {:update :inventory
    :set {:reserved [:+ :reserved amount]
          :updated_at [:raw "current_timestamp"]}
    :where [:and
            [:= :id id]
            [:>= [:- :quantity :reserved] amount]]}
   :inline true))

(defn release-reservation [id amount]
  (sql/format
   {:update :inventory
    :set {:reserved [:- :reserved amount]
          :updated_at [:raw "current_timestamp"]}
    :where [:and
            [:= :id id]
            [:>= :reserved amount]]}
   :inline true))

(defn create [{:keys [product_id sku quantity reserved location
                      reorder_point low_stock_threshold]}]
  (sql/format
   {:insert-into :inventory
    :columns [:product_id :sku :quantity :reserved :location
              :reorder_point :low_stock_threshold]
    :values [[product_id sku (or quantity 0) (or reserved 0) location
              (or reorder_point 10) (or low_stock_threshold 5)]]}
   :inline true))

(defn update-reorder-points [id reorder-point low-stock-threshold]
  (sql/format
   {:update :inventory
    :set {:reorder_point reorder-point
          :low_stock_threshold low-stock-threshold
          :updated_at [:raw "current_timestamp"]}
    :where [:= :id id]}
   :inline true))

(defn record-restock [id quantity]
  (sql/format
   {:update :inventory
    :set {:quantity [:+ :quantity quantity]
          :last_restocked [:raw "current_timestamp"]
          :updated_at [:raw "current_timestamp"]}
    :where [:= :id id]}
   :inline true))
