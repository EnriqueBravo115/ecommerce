(ns global-service.integration.category-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [global-service.integration.category-test-helpers :as category-test-helpers]
   [global-service.integration.integration-test-helpers :as test-helper]))

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
          (let [response (category-test-helpers/put-category category-id (category-test-helpers/category-update-data)
                                                             {:headers (test-helper/auth-headers)})
                body     (test-helper/parse-body response)]
            (is (= 200 (:status response)))
            (is (= "Category updated successfully" (:message body)))))))))

(deftest ^:integration delete-category-success-test
  (testing "DELETE /api/v1/category/delete/:id - should deactivate category successfully"
    (test-helper/with-test-database
      (fn []
        (let [create-response (category-test-helpers/post-category (category-test-helpers/category-parent-data)
                                                                   {:headers (test-helper/auth-headers)})
              category-id     (:id (test-helper/parse-body create-response))]
          (is (= 201 (:status create-response)))
          (let [response (category-test-helpers/delete-category category-id {:headers (test-helper/auth-headers)})
                body     (test-helper/parse-body response)]
            (is (= 200 (:status response)))
            (is (= "Category deactivated successfully" (:message body)))))))))

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
          (is (contains? stats :total_active))
          (is (contains? stats :total_inactive)))))))
