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
