(ns ecommerce.queries.category-queries
  (:require
   [honey.sql :as sql]))

(defn create-category [{:keys [name parent_id active]}]
  (sql/format
   {:insert-into :category
    :columns [:name :parent_id :active]
    :values [[name parent_id active]]}
   :inline true))

(defn get-category-by-id [category_id]
  (sql/format
   {:select [:id :name :parent_id :active :created_at :updated_at]
    :from [:category]
    :where [:= :id category_id]}
   :inline true))

(defn update-category [category_id {:keys [name parent_id active updated_at]}]
  (sql/format
   {:update :category
    :set (merge {:name name
                 :parent_id parent_id
                 :active active}
                (when updated_at {:updated_at updated_at}))
    :where [:= :id category_id]}
   :inline true))

(defn deactivate-category [category_id]
  (sql/format
   {:update :category
    :set {:active false
          :updated_at [:raw "current_timestamp"]}
    :where [:= :id category_id]}
   :inline true))

(defn deactivate-child-categories [parent_id]
  (sql/format
   {:update :category
    :set {:active false
          :updated_at [:raw "current_timestamp"]}
    :where [:= :parent_id parent_id]}
   :inline true))

(defn get-active-categories []
  (sql/format
   {:select [:name :parent_id]
    :from [:category]
    :where [:= :active true]
    :order-by [[:name :asc]]}))

(defn get-category-tree []
  (sql/format
   {:with-recursive [:category_tree [:id :name :parent_id :active :level]
                     {:union-all
                      [{:select [:id :name :parent_id :active
                                 [1 :level]]
                        :from [:category]
                        :where [:= :parent_id nil]}
                       {:select [:c.id :c.name :c.parent_id :c.active
                                 [(sql/call :+ :ct.level 1) :level]]
                        :from [[:category :c]]
                        :join [[:category_tree :ct] [:= :c.parent_id :ct.id]]}]}]
    :select [:*]
    :from [:category_tree]
    :order-by [[:level :asc] [:name :asc]]}))

(defn get-categories-by-level [level]
  (sql/format
   {:with-recursive [:category_tree [:id :name :parent_id :level]
                     {:union-all
                      [{:select [:id :name :parent_id
                                 [1 :level]]
                        :from [:category]
                        :where [:= :parent_id nil]}
                       {:select [:c.id :c.name :c.parent_id
                                 [(sql/call :+ :ct.level 1) :level]]
                        :from [[:category :c]]
                        :join [[:category_tree :ct] [:= :c.parent_id :ct.id]]}]}]
    :select [:id :name :parent_id :level]
    :from [:category_tree]
    :where [:= :level level]
    :order-by [[:name :asc]]}))

(defn get-categories-with-children []
  (sql/format
   {:select [:p.id :p.name [:array_agg [:json_build_object
                                         [:id :c.id]
                                         [:name :c.name]
                                         [:active :c.active]] :children]]
    :from [[:category :p]]
    :left-join [[:category :c] [:= :p.id :c.parent_id]]
    :where [:= :p.parent_id nil]
    :group-by [:p.id :p.name]
    :order-by [[:p.name :asc]]}))

(defn update-category-status [category_id active]
  (sql/format
   {:update :category
    :set {:active active
          :updated_at [:raw "current_timestamp"]}
    :where [:= :id category_id]}
   :inline true))

(defn get-total-categories []
  (sql/format
   {:select [[(sql/call :count :*) :total]]
    :from [:category]}))

(defn get-active-categories-count []
  (sql/format
   {:select [[(sql/call :count :*) :active_count]]
    :from [:category]
    :where [:= :active true]}))

(defn get-categories-count-by-level []
  (sql/format
   {:with-recursive [:category_tree [:id :level]
                     {:union-all
                      [{:select [:id
                                 [1 :level]]
                        :from [:category]
                        :where [:= :parent_id nil]}
                       {:select [:c.id
                                 [(sql/call :+ :ct.level 1) :level]]
                        :from [[:category :c]]
                        :join [[:category_tree :ct] [:= :c.parent_id :ct.id]]}]}]
    :select [:level [(sql/call :count :*) :count]]
    :from [:category_tree]
    :group-by [:level]
    :order-by [[:level :asc]]}))
