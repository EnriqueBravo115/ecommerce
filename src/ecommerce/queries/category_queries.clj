(ns ecommerce.queries.category-queries
  (:require
   [honey.sql :as sql]))

(defn create-category [{:keys [name parent_id active]}]
  (sql/format
   {:insert-into :category
    :columns [:name :parent_id :active]
    :values [[name parent_id active]]
    :returning [:id]}))

(defn get-category-by-id [category_id]
  (sql/format
   {:select [:id :name :parent_id :active :created_at :updated_at]
    :from [:category]
    :where [:= :id category_id]}))

(defn get-category-by-name [name]
  (sql/format
   {:select [:id :name :active]
    :from [:category]
    :where [:= :name name]
    :limit 1}))

(defn update-category [category_id {:keys [name parent_id active updated_at]}]
  (sql/format
   {:update :category
    :set (merge {:name name
                 :parent_id parent_id
                 :active active}
                (when updated_at {:updated_at updated_at}))
    :where [:= :id category_id]}))

(defn deactivate-category [category_id]
  (sql/format
   {:update :category
    :set {:active false
          :updated_at [:raw "current_timestamp"]}
    :where [:= :id category_id]}))

(defn deactivate-child-categories [parent_id]
  (sql/format
   {:update :category
    :set {:active false
          :updated_at [:raw "current_timestamp"]}
    :where [:= :parent_id parent_id]}))

(defn get-total-categories []
  (sql/format
   {:select [[(sql/call :count :*) :total]]
    :from [:category]}))

(defn get-active-categories-count []
  (sql/format
   {:select [[(sql/call :count :*) :active_count]]
    :from [:category]
    :where [:= :active true]}))

(defn get-inactive-categories-count []
  (sql/format
   {:select [[(sql/call :count :*) :inactive_count]]
    :from [:category]
    :where [:= :active false]}))
