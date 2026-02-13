(ns ecommerce.integration.category-test
  (:require
   [cheshire.core :as cheshire]
   [clj-http.client :as client]
   [ecommerce.utils.jwt :as jwt]
   [clojure.test :refer [deftest is testing]]
   [ecommerce.integration.integration-test-helpers :as test-helper]))

(deftest ^:integration create-category-test
  (testing "POST /api/v1/category/create - should create category"
    (test-helper/with-test-database
      (fn []
        (testing "Create category with valid data"
          (let [category-data {:name "Electronics Test"
                               :parent_id nil
                               :active true}
                response (client/post "http://localhost:3001/api/v1/category/create"
                                      {:accept :json
                                       :content-type :json
                                       :headers {"Authorization"
                                                 (str "Bearer " (jwt/generate-admin-test-token))}
                                       :form-params category-data})
                body (-> response :body (cheshire/parse-string true))]

            (is (= 201 (:status response)))
            (is (= "Category created successfully" (:message body)))))

        (testing "Create category without authorization should return 401"
          (let [response (client/post "http://localhost:3001/api/v1/category/create"
                                      {:accept :json
                                       :content-type :json
                                       :throw-exceptions false
                                       :form-params {:name "Unauthorized"}})
                body (-> response :body (cheshire/parse-string true))]

            (is (= 401 (:status response)))
            (is (contains? body :error))))))))

(deftest ^:integration update-category-test
  (testing "PUT /api/v1/category/update/:id - should update category"
    (test-helper/with-test-database
      (fn []
        (let [create-response (client/post "http://localhost:3001/api/v1/category/create"
                                           {:accept :json
                                            :content-type :json
                                            :headers {"Authorization"
                                                      (str "Bearer " (jwt/generate-admin-test-token))}
                                            :form-params {:name "Category To Update"
                                                          :parent_id nil
                                                          :active true}})

              create-body (-> create-response :body (cheshire/parse-string true))
              category-id (:id create-body)]

          (is (= 201 (:status create-response)))
          (is (number? category-id))

          (testing "Update category with valid data"
            (let [update-data {:name "Updated Category"
                               :parent_id nil
                               :active false}

                  response (client/put (str "http://localhost:3001/api/v1/category/update/" category-id)
                                       {:accept :json
                                        :content-type :json
                                        :headers {"Authorization"
                                                  (str "Bearer " (jwt/generate-admin-test-token))}
                                        :form-params update-data})

                  body (-> response :body (cheshire/parse-string true))]

              (is (= 200 (:status response)))
              (is (= "Category updated successfully" (:message body)))))

          (testing "Update non-existing category should return 404"
            (let [response (client/put "http://localhost:3001/api/v1/category/update/9999"
                                       {:accept :json
                                        :content-type :json
                                        :headers {"Authorization"
                                                  (str "Bearer " (jwt/generate-admin-test-token))}
                                        :throw-exceptions false
                                        :form-params {:name "Does not exist"}})

                  body (-> response :body (cheshire/parse-string true))]

              (is (= 404 (:status response)))
              (is (= "Category not found" (:error body)))))

          (testing "Update category without authorization should return 401"
            (let [response (client/put (str "http://localhost:3001/api/v1/category/update/" category-id)
                                       {:accept :json
                                        :content-type :json
                                        :throw-exceptions false
                                        :form-params {:name "Unauthorized update"}})

                  body (-> response :body (cheshire/parse-string true))]

              (is (= 401 (:status response)))
              (is (contains? body :error)))))))))

(deftest ^:integration delete-category-test
  (testing "DELETE /api/v1/category/delete/:id - should deactivate category"
    (test-helper/with-test-database
      (fn []
        (let [create-parent-response
              (client/post "http://localhost:3001/api/v1/category/create"
                           {:accept :json
                            :content-type :json
                            :headers {"Authorization"
                                      (str "Bearer " (jwt/generate-admin-test-token))}
                            :form-params {:name "Parent Category"
                                          :parent_id nil
                                          :active true}})

              parent-body (-> create-parent-response :body (cheshire/parse-string true))
              parent-id (:id parent-body)]

          (is (= 201 (:status create-parent-response)))
          (is (number? parent-id))

          (let [create-child-response
                (client/post "http://localhost:3001/api/v1/category/create"
                             {:accept :json
                              :content-type :json
                              :headers {"Authorization"
                                        (str "Bearer " (jwt/generate-admin-test-token))}
                              :form-params {:name "Child Category"
                                            :parent_id parent-id
                                            :active true}})

                child-body (-> create-child-response :body (cheshire/parse-string true))
                child-id (:id child-body)]

            (is (= 201 (:status create-child-response)))
            (is (number? child-id))

            (testing "Deactivate existing category should return 200"
              (let [response (client/delete (str "http://localhost:3001/api/v1/category/delete/" parent-id)
                                            {:accept :json
                                             :content-type :json
                                             :headers {"Authorization"
                                                       (str "Bearer " (jwt/generate-admin-test-token))}})

                    body (-> response :body (cheshire/parse-string true))]

                (is (= 200 (:status response)))
                (is (= "Category deactivated successfully" (:message body)))))

            (testing "Deactivate already inactive category should return 409"
              (let [response (client/delete (str "http://localhost:3001/api/v1/category/delete/" parent-id)
                                            {:accept :json
                                             :content-type :json
                                             :headers {"Authorization"
                                                       (str "Bearer " (jwt/generate-admin-test-token))}
                                             :throw-exceptions false})

                    body (-> response :body (cheshire/parse-string true))]

                (is (= 409 (:status response)))
                (is (= "Cannot delete an inactive (already deactivated) category"
                       (:error body)))
                (is (= parent-id (:category-id body)))))

            (testing "Deactivate non-existing category should return 404"
              (let [response (client/delete "http://localhost:3001/api/v1/category/delete/9999"
                                            {:accept :json
                                             :content-type :json
                                             :headers {"Authorization"
                                                       (str "Bearer " (jwt/generate-admin-test-token))}
                                             :throw-exceptions false})

                    body (-> response :body (cheshire/parse-string true))]

                (is (= 404 (:status response)))
                (is (= "Category not found" (:error body)))))

            (testing "Deactivate category without authorization should return 401"
              (let [response (client/delete (str "http://localhost:3001/api/v1/category/delete/" parent-id)
                                            {:accept :json
                                             :content-type :json
                                             :throw-exceptions false})

                    body (-> response :body (cheshire/parse-string true))]

                (is (= 401 (:status response)))
                (is (contains? body :error))))))))))

(deftest ^:integration get-category-by-id-test
  (testing "GET /api/v1/category/:id - should return category by id"
    (test-helper/with-test-database
      (fn []
        (let [create-response
              (client/post "http://localhost:3001/api/v1/category/create"
                           {:accept :json
                            :content-type :json
                            :headers {"Authorization"
                                      (str "Bearer " (jwt/generate-admin-test-token))}
                            :form-params {:name "Category For Get"
                                          :parent_id nil
                                          :active true}})

              create-body (-> create-response :body (cheshire/parse-string true))
              category-id (:id create-body)]

          (is (= 201 (:status create-response)))
          (is (number? category-id))

          (testing "Get existing category should return 200"
            (let [response
                  (client/get (str "http://localhost:3001/api/v1/category/" category-id)
                              {:accept :json
                               :headers {"Authorization"
                                         (str "Bearer " (jwt/generate-admin-test-token))}})

                  body (-> response :body (cheshire/parse-string true))
                  category (:category body)]

              (is (= 200 (:status response)))
              (is (= category-id (:id category)))
              (is (= "Category For Get" (:name category)))
              (is (= true (:active category)))))

          (testing "Get non-existing category should return 404"
            (let [response
                  (client/get "http://localhost:3001/api/v1/category/9999"
                              {:accept :json
                               :headers {"Authorization"
                                         (str "Bearer " (jwt/generate-admin-test-token))}
                               :throw-exceptions false})

                  body (-> response :body (cheshire/parse-string true))]

              (is (= 404 (:status response)))
              (is (= "Category not found" (:error body)))))

          (testing "Get category without authorization should return 401"
            (let [response
                  (client/get (str "http://localhost:3001/api/v1/category/" category-id)
                              {:accept :json
                               :throw-exceptions false})

                  body (-> response :body (cheshire/parse-string true))]

              (is (= 401 (:status response)))
              (is (contains? body :error)))))))))

(deftest ^:integration get-active-categories-test
  (testing "GET /api/v1/category/active - should include created active category"
    (test-helper/with-test-database
      (fn []
        (let [unique-name (str "Active Test Category " (System/currentTimeMillis))
              create-response
              (client/post "http://localhost:3001/api/v1/category/create"
                           {:accept :json
                            :content-type :json
                            :headers {"Authorization"
                                      (str "Bearer " (jwt/generate-admin-test-token))}
                            :form-params {:name unique-name
                                          :parent_id nil
                                          :active true}})

              create-body (-> create-response :body (cheshire/parse-string true))
              created-id (:id create-body)]

          (is (= 201 (:status create-response)))
          (is (number? created-id))

          (let [response
                (client/get "http://localhost:3001/api/v1/category/active"
                            {:accept :json
                             :headers {"Authorization"
                                       (str "Bearer " (jwt/generate-admin-test-token))}})

                body (-> response :body (cheshire/parse-string true))
                categories (:categories body)]

            (is (= 200 (:status response)))
            (is (seq categories))

            (is (some #(= unique-name (:name %)) categories))
            (is (every? #(not (false? (:active %))) categories))))

        (testing "Should return 401 without authorization"
          (let [response
                (client/get "http://localhost:3001/api/v1/category/active"
                            {:accept :json
                             :throw-exceptions false})

                body (-> response :body (cheshire/parse-string true))]

            (is (= 401 (:status response)))
            (is (contains? body :error))))))))

(deftest ^:integration get-category-tree-test
  (testing "GET /api/v1/category/tree - should return hierarchical category tree"
    (test-helper/with-test-database
      (fn []
        (let [root-name (str "Root Test" (System/currentTimeMillis))
              root-response
              (client/post "http://localhost:3001/api/v1/category/create"
                           {:accept :json
                            :content-type :json
                            :headers {"Authorization"
                                      (str "Bearer " (jwt/generate-admin-test-token))}
                            :form-params {:name root-name
                                          :parent_id nil
                                          :active true}})

              root-body (-> root-response :body (cheshire/parse-string true))
              root-id (:id root-body)]

          (is (= 201 (:status root-response)))

          (let [child-name (str "Child Test " (System/currentTimeMillis))
                child-response
                (client/post "http://localhost:3001/api/v1/category/create"
                             {:accept :json
                              :content-type :json
                              :headers {"Authorization"
                                        (str "Bearer " (jwt/generate-admin-test-token))}
                              :form-params {:name child-name
                                            :parent_id root-id
                                            :active true}})

                child-body (-> child-response :body (cheshire/parse-string true))
                child-id (:id child-body)]

            (is (= 201 (:status child-response)))

            (testing "Should return hierarchical structure with correct levels"
              (let [response
                    (client/get
                     "http://localhost:3001/api/v1/category/tree"
                     {:accept :json
                      :headers {"Authorization"
                                (str "Bearer " (jwt/generate-admin-test-token))}})

                    body (-> response :body (cheshire/parse-string true))
                    tree (:category-tree body)]

                (is (= 200 (:status response)))
                (is (seq tree))

                (let [root-node (some #(when (= root-id (:id %)) %) tree)]
                  (is root-node)
                  (is (= 1 (:level root-node)))
                  (is (nil? (:parent_id root-node))))

                (let [child-node (some #(when (= child-id (:id %)) %) tree)]
                  (is child-node)
                  (is (= 2 (:level child-node)))
                  (is (= root-id (:parent_id child-node))))))

            (testing "Should return 401 without authorization"
              (let [response
                    (client/get
                     "http://localhost:3001/api/v1/category/tree"
                     {:accept :json
                      :throw-exceptions false})

                    body (-> response :body (cheshire/parse-string true))]

                (is (= 401 (:status response)))
                (is (contains? body :error))))))))))

(deftest ^:integration get-category-statistics-test
  (testing "GET /api/v1/category/statistics - should return correct counts"
    (test-helper/with-test-database
      (fn []
        (doseq [active? [true true false]]
          (client/post "http://localhost:3001/api/v1/category/create"
                       {:accept :json
                        :content-type :json
                        :headers {"Authorization"
                                  (str "Bearer " (jwt/generate-admin-test-token))}
                        :form-params {:name (str "Stats Test " (System/currentTimeMillis) "-" active?)
                                      :parent_id nil
                                      :active active?}}))

        (let [response
              (client/get "http://localhost:3001/api/v1/category/stats"
                          {:accept :json
                           :headers {"Authorization"
                                     (str "Bearer " (jwt/generate-admin-test-token))}})

              body (-> response :body (cheshire/parse-string true))
              stats (:statistics body)]

          (is (= 200 (:status response)))
          (is (number? (:total_categories stats)))
          (is (number? (:active_categories stats))))

        (testing "Should return 401 without authorization"
          (let [response
                (client/get "http://localhost:3001/api/v1/category/stats"
                            {:accept :json
                             :throw-exceptions false})

                body (-> response :body (cheshire/parse-string true))]

            (is (= 401 (:status response)))
            (is (contains? body :error))))))))
