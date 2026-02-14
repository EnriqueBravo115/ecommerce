(ns ecommerce.queries.product-queries
  (:require [honey.sql :as sql]))

(ns ecommerce.queries.product-queries
  (:require [honey.sql :as sql]))

(defn create-product [data]
  (sql/format
   {:insert-into :product
    :values [data]
    :returning [:id]}
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
   {:select [:p.name :p.price :p.status
             [:s.business_name :seller]]
    :from [[:product :p]]
    :left-join [[:seller :s]
                [:= :p.seller_id :s.id]]
    :where [:= :p.category_id category-id]
    :order-by [[:p.created_at :desc]]}
   :inline true))

(defn get-by-status [status]
  (sql/format
   {:select [:p.name :p.price :p.status
             [:s.business_name :seller_name]
             [:c.name :category_name]]
    :from [[:product :p]]
    :left-join [[:seller :s] [:= :p.seller_id :s.id]
                [:category :c] [:= :p.category_id :c.id]]
    :where [:= :p.status status]
    :order-by [[:p.created_at :desc]]}
   :inline true))

(defn get-by-price-range [min-price max-price]
  (sql/format
   {:select [:p.name :p.price
             [:s.business_name :seller_name]
             [:c.name :category_name]]
    :from [[:product :p]]
    :left-join [[:seller :s] [:= :p.seller_id :s.id]
                [:category :c] [:= :p.category_id :c.id]]
    :where [:between :p.price min-price max-price]
    :order-by [[:p.price :asc]]}
   :inline true))

(defn get-top-viewed [limit]
  (sql/format
   {:select [:p.id :p.sku :p.name :p.price :p.view_count :p.average_rating
             :s.business_name]
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
             :s.business_name]
    :from [[:product :p]]
    :left-join [[:seller :s] [:= :p.seller_id :s.id]]
    :where [:and
            [:>= :p.review_count min-reviews]
            [:>= :p.average_rating min-rating]]
    :order-by [[:p.average_rating :desc] [:p.review_count :desc]]
    :limit limit}
   :inline true))

(defn increment-view-count [product-id]
  (sql/format
   {:update :product
    :set {:view_count [:+ :view_count 1]
          :updated_at [:raw "current_timestamp"]}
    :where [:= :id product-id]}
   :inline true))
