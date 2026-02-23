(ns ecommerce.integration.category-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [ecommerce.integration.category-test-helpers :as category-test-helpers]
   [ecommerce.integration.integration-test-helpers :as test-helper]))

(deftest ^:integration create-category-success-test
  (testing "POST /api/v1/category/create - should create category with valid data"
    (test-helper/with-test-database
      (fn []
        (let [response (category-test-helpers/post-category (category-test-helpers/category-data) {:headers (test-helper/auth-headers)})
              body     (test-helper/parse-body response)]
          (is (= 201 (:status response)))
          (is (= "Category created successfully" (:message body)))
          (is (some? (:id body))))))))

(deftest ^:integration create-category-duplicate-test
  (testing "POST /api/v1/category/create - should return 409 when category name already exists"
    (test-helper/with-test-database
      (fn []
        (let [_ (category-test-helpers/post-category (category-test-helpers/category-data)
                                                     {:headers (test-helper/auth-headers)})
              response (category-test-helpers/post-category (category-test-helpers/category-data)
                                                            {:headers (test-helper/auth-headers)})
              body     (test-helper/parse-body response)]
          (is (= 409 (:status response)))
          (is (contains? body :error))
          (is (contains? body :existing-id)))))))

(deftest ^:integration create-category-validation-error-test
  (testing "POST /api/v1/category/create - should return 400 when data is invalid"
    (test-helper/with-test-database
      (fn []
        (let [response (category-test-helpers/post-category {} {:headers (test-helper/auth-headers)})
              body     (test-helper/parse-body response)]
          (is (= 400 (:status response)))
          (is (contains? body :error)))))))

(deftest ^:integration create-category-no-auth-test
  (testing "POST /api/v1/category/create - should return 401 when no auth headers provided"
    (test-helper/with-test-database
      (fn []
        (let [response (category-test-helpers/post-category (category-test-helpers/category-data))
              body     (test-helper/parse-body response)]
          (is (= 401 (:status response)))
          (is (= "Authentication required" (:error body))))))))

(deftest ^:integration update-category-success-test
  (testing "PUT /api/v1/category/update/:id - should update category with valid data"
    (test-helper/with-test-database
      (fn []
        (let [create-response (category-test-helpers/post-category (category-test-helpers/category-data)
                                                                   {:headers (test-helper/auth-headers)})
              create-body     (test-helper/parse-body create-response)
              category-id     (:id create-body)]
          (is (= 201 (:status create-response)))
          (is (number? category-id))

          (let [response (category-test-helpers/put-category category-id (category-test-helpers/category-update-data)
                                                             {:headers (test-helper/auth-headers)})
                body     (test-helper/parse-body response)]
            (is (= 200 (:status response)))
            (is (= "Category updated successfully" (:message body)))))))))

(deftest ^:integration update-category-not-found-test
  (testing "PUT /api/v1/category/update/:id - should return 404 when category does not exist"
    (test-helper/with-test-database
      (fn []
        (let [response (category-test-helpers/put-category 999999 (category-test-helpers/category-update-data)
                                                           {:headers (test-helper/auth-headers)})
              body     (test-helper/parse-body response)]
          (is (= 404 (:status response)))
          (is (= "Category not found" (:error body))))))))

(deftest ^:integration update-category-no-auth-test
  (testing "PUT /api/v1/category/update/:id - should return 401 when no auth headers provided"
    (test-helper/with-test-database
      (fn []
        (let [response (category-test-helpers/put-category 1 (category-test-helpers/category-update-data))
              body     (test-helper/parse-body response)]
          (is (= 401 (:status response)))
          (is (= "Authentication required" (:error body))))))))

(deftest ^:integration delete-category-success-test
  (testing "DELETE /api/v1/category/delete/:id - should deactivate category successfully"
    (test-helper/with-test-database
      (fn []
        (let [create-response (category-test-helpers/post-category (category-test-helpers/category-parent-data)
                                                                   {:headers (test-helper/auth-headers)})
              category-id     (:id (test-helper/parse-body create-response))]
          (is (= 201 (:status create-response)))
          (is (number? category-id))
          (let [response (category-test-helpers/delete-category category-id {:headers (test-helper/auth-headers)})
                body     (test-helper/parse-body response)]
            (is (= 200 (:status response)))
            (is (= "Category deactivated successfully" (:message body)))))))))

(deftest ^:integration delete-category-already-inactive-test
  (testing "DELETE /api/v1/category/delete/:id - should return 409 when category is already inactive"
    (test-helper/with-test-database
      (fn []
        (let [create-response (category-test-helpers/post-category (category-test-helpers/category-parent-data)
                                                                   {:headers (test-helper/auth-headers)})
              category-id     (:id (test-helper/parse-body create-response))]
          (is (= 201 (:status create-response)))
          (category-test-helpers/delete-category category-id {:headers (test-helper/auth-headers)})
          (let [response (category-test-helpers/delete-category category-id {:headers (test-helper/auth-headers)})
                body     (test-helper/parse-body response)]
            (is (= 409 (:status response)))
            (is (= "Cannot delete an inactive (already deactivated) category" (:error body)))
            (is (= category-id (:category-id body)))))))))

(deftest ^:integration delete-category-deactivates-children-test
  (testing "DELETE /api/v1/category/delete/:id - should deactivate children when parent is deactivated"
    (test-helper/with-test-database
      (fn []
        (let [parent-response (category-test-helpers/post-category (category-test-helpers/category-parent-data)
                                                                   {:headers (test-helper/auth-headers)})
              parent-id       (:id (test-helper/parse-body parent-response))
              _               (category-test-helpers/post-category (category-test-helpers/category-child-data parent-id)
                                                                   {:headers (test-helper/auth-headers)})]
          (is (= 201 (:status parent-response)))
          (let [response (category-test-helpers/delete-category parent-id {:headers (test-helper/auth-headers)})
                body     (test-helper/parse-body response)]
            (is (= 200 (:status response)))
            (is (= "Category deactivated successfully" (:message body)))))))))

(deftest ^:integration delete-category-not-found-test
  (testing "DELETE /api/v1/category/delete/:id - should return 404 when category does not exist"
    (test-helper/with-test-database
      (fn []
        (let [response (category-test-helpers/delete-category 999999 {:headers (test-helper/auth-headers)})
              body     (test-helper/parse-body response)]
          (is (= 404 (:status response)))
          (is (= "Category not found" (:error body))))))))

(deftest ^:integration delete-category-no-auth-test
  (testing "DELETE /api/v1/category/delete/:id - should return 401 when no auth headers provided"
    (test-helper/with-test-database
      (fn []
        (let [response (category-test-helpers/delete-category 1)
              body     (test-helper/parse-body response)]
          (is (= 401 (:status response)))
          (is (= "Authentication required" (:error body))))))))

(deftest ^:integration get-category-by-id-success-test
  (testing "GET /api/v1/category/:id - should return category by id"
    (test-helper/with-test-database
      (fn []
        (let [create-response (category-test-helpers/post-category (category-test-helpers/category-data)
                                                                   {:headers (test-helper/auth-headers)})
              create-body     (test-helper/parse-body create-response)
              category-id     (:id create-body)]
          (is (= 201 (:status create-response)))
          (is (number? category-id))
          (let [response (category-test-helpers/get-category-by-id category-id {:headers (test-helper/auth-headers)})
                body     (test-helper/parse-body response)
                category (:category body)]
            (is (= 200 (:status response)))
            (is (= category-id (:id category)))
            (is (= (:name (category-test-helpers/category-data)) (:name category)))
            (is (= true (:active category)))))))))

(deftest ^:integration get-category-by-id-not-found-test
  (testing "GET /api/v1/category/:id - should return 404 when category does not exist"
    (test-helper/with-test-database
      (fn []
        (let [response (category-test-helpers/get-category-by-id 999999 {:headers (test-helper/auth-headers)})
              body     (test-helper/parse-body response)]
          (is (= 404 (:status response)))
          (is (= "Category not found" (:error body))))))))

(deftest ^:integration get-category-by-id-no-auth-test
  (testing "GET /api/v1/category/:id - should return 401 when no auth headers provided"
    (test-helper/with-test-database
      (fn []
        (let [response (category-test-helpers/get-category-by-id 1)
              body     (test-helper/parse-body response)]
          (is (= 401 (:status response)))
          (is (= "Authentication required" (:error body))))))))

(deftest ^:integration get-active-categories-success-test
  (testing "GET /api/v1/category/active - should return active categories"
    (test-helper/with-test-database
      (fn []
        (let [create-response (category-test-helpers/post-category (category-test-helpers/category-data)
                                                                   {:headers (test-helper/auth-headers)})]
          (is (= 201 (:status create-response)))
          (let [response   (category-test-helpers/get-active-categories {:headers (test-helper/auth-headers)})
                body       (test-helper/parse-body response)
                categories (:categories body)]
            (is (= 200 (:status response)))
            (is (seq categories))))))))

(deftest ^:integration get-active-categories-no-auth-test
  (testing "GET /api/v1/category/active - should return 401 when no auth headers provided"
    (test-helper/with-test-database
      (fn []
        (let [response (category-test-helpers/get-active-categories)
              body     (test-helper/parse-body response)]
          (is (= 401 (:status response)))
          (is (= "Authentication required" (:error body))))))))

(deftest ^:integration get-category-tree-success-test
  (testing "GET /api/v1/category/tree - should return hierarchical category tree"
    (test-helper/with-test-database
      (fn []
        (let [root-response (category-test-helpers/post-category (category-test-helpers/category-parent-data)
                                                                 {:headers (test-helper/auth-headers)})
              root-id       (:id (test-helper/parse-body root-response))
              _             (category-test-helpers/post-category (category-test-helpers/category-child-data root-id)
                                                                 {:headers (test-helper/auth-headers)})]
          (is (= 201 (:status root-response)))
          (let [response  (category-test-helpers/get-category-tree {:headers (test-helper/auth-headers)})
                body      (test-helper/parse-body response)
                tree      (:category-tree body)
                root-node (some #(when (= root-id (:id %)) %) tree)]
            (is (= 200 (:status response)))
            (is (seq tree))
            (is (some? root-node))
            (is (= 1 (:level root-node)))
            (is (nil? (:parent_id root-node)))))))))

(deftest ^:integration get-category-tree-no-auth-test
  (testing "GET /api/v1/category/tree - should return 401 when no auth headers provided"
    (test-helper/with-test-database
      (fn []
        (let [response (category-test-helpers/get-category-tree)
              body     (test-helper/parse-body response)]
          (is (= 401 (:status response)))
          (is (= "Authentication required" (:error body))))))))

(deftest ^:integration get-category-statistics-success-test
  (testing "GET /api/v1/category/stats - should return category statistics"
    (test-helper/with-test-database
      (fn []
        (let [_ (category-test-helpers/post-category (category-test-helpers/category-data)
                                                     {:headers (test-helper/auth-headers)})
              response (category-test-helpers/get-category-statistics {:headers (test-helper/auth-headers)})
              body     (test-helper/parse-body response)
              stats    (:statistics body)]
          (is (= 200 (:status response)))
          (is (contains? stats :total_categories))
          (is (contains? stats :active_categories))
          (is (number? (:total_categories stats)))
          (is (number? (:active_categories stats))))))))

(deftest ^:integration get-category-statistics-no-auth-test
  (testing "GET /api/v1/category/stats - should return 401 when no auth headers provided"
    (test-helper/with-test-database
      (fn []
        (let [response (category-test-helpers/get-category-statistics)
              body     (test-helper/parse-body response)]
          (is (= 401 (:status response)))
          (is (= "Authentication required" (:error body))))))))
