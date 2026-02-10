(ns ecommerce.handlers.category-handler
  (:require
   [ecommerce.queries.category-queries :as queries]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]))

(def ^:private json-headers {"Content-Type" "application/json"})

(defn- build-response [status body]
  {:status status :headers json-headers :body body})

(defn create-category [request]
  (let [category-data (:body request)
        ds (:datasource request)]

    (jdbc/execute! ds
                   (queries/create-category
                    {:name (:name category-data)
                     :parent_id (:parent_id category-data)
                     :active (:active category-data true)}))

    (build-response 201 {:message "Category created successfully"})))

(defn update-category [request]
  (let [category-id (Long/parseLong (get-in request [:params :id]))
        category-data (:body request)
        ds (:datasource request)
        existing-category (jdbc/execute-one! ds
                                             (queries/get-category-by-id category-id)
                                             {:builder-fn rs/as-unqualified-maps})]

    (cond
      (nil? existing-category)
      (build-response 404 {:error "Category not found"})

      :else
      (do
        (jdbc/execute! ds
                       (queries/update-category
                        category-id
                        {:name (:name category-data)
                         :parent_id (:parent_id category-data)
                         :active (:active category-data)
                         :updated_at [:raw "current_timestamp"]}))
        (build-response 200 {:message "Category updated successfully"})))))

(defn delete-category [request]
  (let [category-id (Long/parseLong (get-in request [:params :id]))
        ds (:datasource request)
        existing-category (jdbc/execute-one! ds
                                             (queries/get-category-by-id category-id)
                                             {:builder-fn rs/as-unqualified-maps})]

    (cond
      (nil? existing-category)
      (build-response 404 {:error "Category not found"})

      (not (:active existing-category))
      (build-response 409 {:error "Cannot delete an inactive (already deactivated) category"
                           :category-id category-id
                           :status :inactive})

      :else
      (do
        (jdbc/execute! ds
                       (queries/deactivate-category category-id))

        (jdbc/execute! ds
                       (queries/deactivate-child-categories category-id))

        (build-response 200 {:message "Category deactivated successfully"})))))

(defn get-category-by-id [request]
  (let [category-id (Long/parseLong (get-in request [:params :id]))
        ds (:datasource request)
        category (jdbc/execute-one! ds
                                    (queries/get-category-by-id category-id)
                                    {:builder-fn rs/as-unqualified-maps})]

    (if category
      (build-response 200 {:category category})
      (build-response 404 {:error "Category not found"}))))

(defn get-active-categories [request]
  (let [ds (:datasource request)
        categories (jdbc/execute! ds
                                  (queries/get-active-categories)
                                  {:builder-fn rs/as-unqualified-maps})]

    (if (seq categories)
      (build-response 200 {:categories categories})
      (build-response 404 {:error "No active categories found"}))))

(defn get-category-tree [request]
  (let [ds (:datasource request)
        categories (jdbc/execute! ds
                                  (queries/get-category-tree)
                                  {:builder-fn rs/as-unqualified-maps})]

    (if (seq categories)
      (build-response 200 {:category-tree categories})
      (build-response 404 {:error "No categories found"}))))

(defn toggle-category-status [request]
  (let [category-id (Long/parseLong (get-in request [:params :id]))
        ds (:datasource request)
        existing-category (jdbc/execute-one! ds
                                             (queries/get-category-by-id category-id)
                                             {:builder-fn rs/as-unqualified-maps})]

    (cond
      (nil? existing-category)
      (build-response 404 {:error "Category not found"})

      :else
      (let [new-status (not (:active existing-category))]
        (jdbc/execute! ds
                       (queries/update-category-status category-id new-status))
        (build-response 200 {:message (str "Category status updated to " new-status)
                             :active new-status})))))

(defn get-category-statistics [request]
  (let [ds (:datasource request)
        total-categories (jdbc/execute-one! ds
                                            (queries/get-total-categories)
                                            {:builder-fn rs/as-unqualified-maps})
        active-categories (jdbc/execute-one! ds
                                             (queries/get-active-categories-count)
                                             {:builder-fn rs/as-unqualified-maps})
        categories-by-level (jdbc/execute! ds
                                           (queries/get-categories-count-by-level)
                                           {:builder-fn rs/as-unqualified-maps})]

    (build-response 200
                    {:statistics
                     {:total_categories (:total total-categories)
                      :active_categories (:active_count active-categories)
                      :categories_by_level categories-by-level}})))
