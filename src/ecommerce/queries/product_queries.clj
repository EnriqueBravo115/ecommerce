(ns ecommerce.queries.product-queries
  (:require [honey.sql :as sql]))

(ns ecommerce.queries.product-queries
  (:require [honey.sql :as sql]))

(defn create-product [data]
  (sql/format
   {:insert-into :product
    :values [data]}
   :inline true))

(defn get-product-by-id [id]
  (sql/format
   {:select [:*]
    :from [:product]
    :where [:= :id id]}))

(defn get-product-by-sku [sku]
  (sql/format
   {:select [:id :sku]
    :from [:product]
    :where [:= :sku sku]}))

(defn update-product [product-id fields]
  (sql/format
   {:update :product
    :set (assoc fields
                :updated_at [:raw "current_timestamp"])
    :where [:= :id product-id]}
   :inline true))

(defn delete-product [product-id]
  (sql/format
   {:delete-from :product
    :where [:= :id product-id]}
   :inline true))

(defn get-by-id [id]
  (sql/format
   {:select [:p.*
             :s.name :seller_name
             :c.name :category_name]
    :from [[:product :p]]
    :left-join [[:seller :s] [:= :p.seller_id :s.id]
                [:category :c] [:= :p.category_id :c.id]]
    :where [:= :p.id id]}
   :inline true))

(defn get-by-sku [sku]
  (sql/format
   {:select [:p.*
             :s.name :seller_name
             :c.name :category_name]
    :from [[:product :p]]
    :left-join [[:seller :s] [:= :p.seller_id :s.id]
                [:category :c] [:= :p.category_id :c.id]]
    :where [:= :p.sku sku]}
   :inline true))

(defn get-by-seller [seller-id]
  (sql/format
   {:select [:p.name :p.price :p.status
             [:c.name :category]]
    :from   [[:product :p]]
    :left-join [[:category :c]
                [:= :p.category_id :c.id]]
    :where  [:= :p.seller_id seller-id]
    :order-by [[:p.created_at :desc]]}
   :inline true))

(defn get-by-category [category-id]
  (sql/format
   {:select [:p.* :s.name :seller_name]
    :from [[:product :p]]
    :left-join [[:seller :s] [:= :p.seller_id :s.id]]
    :where [:= :p.category_id category-id]
    :order-by [[:p.created_at :desc]]}
   :inline true))

(defn get-by-status [status]
  (sql/format
   {:select [:p.*
             :s.name :seller_name
             :c.name :category_name]
    :from [[:product :p]]
    :left-join [[:seller :s] [:= :p.seller_id :s.id]
                [:category :c] [:= :p.category_id :c.id]]
    :where [:= :p.status status]
    :order-by [[:p.created_at :desc]]}
   :inline true))

(defn get-by-price-range [min-price max-price]
  (sql/format
   {:select [:p.*
             :s.name :seller_name
             :c.name :category_name]
    :from [[:product :p]]
    :left-join [[:seller :s] [:= :p.seller_id :s.id]
                [:category :c] [:= :p.category_id :c.id]]
    :where [:between :p.price min-price max-price]
    :order-by [[:p.price :asc]]}
   :inline true))

(defn get-by-brand [brand]
  (sql/format
   {:select [:p.*
             :s.name :seller_name
             :c.name :category_name]
    :from [[:product :p]]
    :left-join [[:seller :s] [:= :p.seller_id :s.id]
                [:category :c] [:= :p.category_id :c.id]]
    :where [:like :p.brand (str "%" brand "%")]
    :order-by [[:p.created_at :desc]]}
   :inline true))

(defn get-top-viewed [limit]
  (sql/format
   {:select [:p.id :p.sku :p.name :p.price :p.view_count :p.average_rating
             :s.name :seller_name]
    :from [[:product :p]]
    :left-join [[:seller :s] [:= :p.seller_id :s.id]]
    :where [:> :p.view_count 0]
    :order-by [[:p.view_count :desc]]
    :limit limit}
   :inline true))

(defn get-top-rated [min-reviews min-rating limit]
  (sql/format
   {:select [:p.id :p.sku :p.name :p.price :p.view_count
             :p.average_rating :p.review_count
             :s.name :seller_name]
    :from [[:product :p]]
    :left-join [[:seller :s] [:= :p.seller_id :s.id]]
    :where [:and
            [:>= :p.review_count min-reviews]
            [:>= :p.average_rating min-rating]]
    :order-by [[:p.average_rating :desc] [:p.review_count :desc]]
    :limit limit}
   :inline true))

(defn get-by-tags [tags]
  (if (empty? tags)
    (sql/format
     {:select [:p.*
               :s.name :seller_name
               :c.name :category_name]
      :from [[:product :p]]
      :left-join [[:seller :s] [:= :p.seller_id :s.id]
                  [:category :c] [:= :p.category_id :c.id]]
      :where [:!= :p.tags nil]
      :order-by [[:p.created_at :desc]]}
     :inline true)
    (let [tag-conditions (map #(vector :like :p.tags (str "%" % "%")) tags)]
      (sql/format
       {:select [:p.*
                 :s.name :seller_name
                 :c.name :category_name]
        :from [[:product :p]]
        :left-join [[:seller :s] [:= :p.seller_id :s.id]
                    [:category :c] [:= :p.category_id :c.id]]
        :where (into [:or] tag-conditions)
        :order-by [[:p.created_at :desc]]}
       :inline true))))

(defn get-product-stats []
  (sql/format
   {:select [[(sql/call :count :*) :total_products]
             [(sql/call :avg :price) :average_price]
             [(sql/call :sum :view_count) :total_views]
             [(sql/call :avg :average_rating) :overall_rating]
             [(sql/call :sum :review_count) :total_reviews]
             [(sql/call :count (sql/call :distinct :seller_id)) :unique_sellers]
             [(sql/call :count (sql/call :distinct :brand)) :unique_brands]]
    :from [:product]}
   :inline true))

(defn get-count-by-status []
  (sql/format
   {:select [:status [(sql/call :count :*) :count]]
    :from [:product]
    :group-by [:status]
    :order-by [[:count :desc]]}
   :inline true))

(defn get-count-by-category []
  (sql/format
   {:select [:c.name :category_name
             [(sql/call :count :p.*) :count]]
    :from [[:product :p]]
    :left-join [[:category :c] [:= :p.category_id :c.id]]
    :group-by [:c.name]
    :order-by [[:count :desc]]}
   :inline true))

(defn search [search-term]
  (let [search-pattern (str "%" search-term "%")]
    (sql/format
     {:select [:p.*
               :s.name :seller_name
               :c.name :category_name]
      :from [[:product :p]]
      :left-join [[:seller :s] [:= :p.seller_id :s.id]
                  [:category :c] [:= :p.category_id :c.id]]
      :where [:or
              [:like :p.name search-pattern]
              [:like :p.description search-pattern]
              [:like :p.sku search-pattern]
              [:like :p.brand search-pattern]]
      :order-by [[:p.created_at :desc]]}
     :inline true)))

(defn increment-view-count [product-id]
  (sql/format
   {:update :product
    :set {:view_count [:+ :view_count 1]
          :updated_at [:raw "current_timestamp"]}
    :where [:= :id product-id]}
   :inline true))
