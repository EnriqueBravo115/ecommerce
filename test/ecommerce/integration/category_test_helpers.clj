(ns ecommerce.integration.category-test-helpers
  (:require [clj-http.client :as client]))

(defn category-data []
  {:name      "Electronics Test"
   :parent_id nil
   :active    true})

(defn category-update-data []
  {:name      "Updated Category"
   :parent_id nil
   :active    false})

(defn post-category
  ([category] (post-category category {}))
  ([category extra-opts]
   (client/post "http://localhost:3001/api/v1/category/create"
                (merge {:accept           :json
                        :content-type     :json
                        :throw-exceptions false
                        :form-params      category}
                       extra-opts))))

(defn category-parent-data []
  {:name      "Parent Category"
   :parent_id nil
   :active    true})

(defn category-child-data [parent-id]
  {:name      "Child Category"
   :parent_id parent-id
   :active    true})

(defn put-category
  ([category-id category] (put-category category-id category {}))
  ([category-id category extra-opts]
   (client/put (str "http://localhost:3001/api/v1/category/update/" category-id)
               (merge {:accept           :json
                       :content-type     :json
                       :throw-exceptions false
                       :form-params      category}
                      extra-opts))))

(defn delete-category
  ([category-id] (delete-category category-id {}))
  ([category-id extra-opts]
   (client/delete (str "http://localhost:3001/api/v1/category/delete/" category-id)
                  (merge {:accept           :json
                          :throw-exceptions false}
                         extra-opts))))

(defn get-category-by-id
  ([category-id] (get-category-by-id category-id {}))
  ([category-id extra-opts]
   (client/get (str "http://localhost:3001/api/v1/category/" category-id)
               (merge {:accept           :json
                       :throw-exceptions false}
                      extra-opts))))

(defn get-category-statistics
  ([opts] (client/get "http://localhost:3001/api/v1/category/stats"
                      (merge {:accept           :json
                              :throw-exceptions false}
                             opts)))
  ([] (get-category-statistics {})))
