(ns ecommerce.integration.product-test
  (:require
   [cheshire.core :as cheshire]
   [clj-http.client :as client]
   [ecommerce.utils.jwt :as jwt]
   [clojure.test :refer [deftest is testing]]
   [ecommerce.integration.integration-test-helpers :as test-helper]))

(defn valid-product []
  {:category_id       1
   :sku               "LAPTOP-001"
   :name              "Gaming Laptop RTX 5080"
   :description       "High performance gaming laptop 2026 edition"
   :short_description "RTX 50-series Gaming"
   :price             25999.99
   :compare_at_price  28999.99
   :cost_price        20500.00
   :brand             "Asus ROG"
   :weight            2.8
   :weight_unit       "kg"
   :status            "active"
   :condition         "new"
   :tags              "laptop,gaming,rtx,2026"})

(deftest ^:integration create-product-test
  (testing "POST /api/v1/product/create - should create product"
    (test-helper/with-test-database-and-kafka
      (fn []
        (testing "Create product without authorization should throw error"
          (let [response (client/post "http://localhost:3001/api/v1/product/create"
                                      {:accept :json
                                       :content-type :json
                                       :throw-exceptions false
                                       :form-params (valid-product)})
                body (-> response :body (cheshire/parse-string true))]
            (is (= 401 (:status response)))
            (is (= "Authentication required" (:error body)))))

        (testing "Create product with valid data"
          (let [response (client/post "http://localhost:3001/api/v1/product/create"
                                      {:accept :json
                                       :content-type :json
                                       :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}
                                       :form-params (valid-product)})
                body (-> response :body (cheshire/parse-string true))]
            (is (= 201 (:status response)))
            (is (= "Product created successfully" (:message body))))

          (testing "Create duplicated product should throw error"
            (let [response (client/post "http://localhost:3001/api/v1/product/create"
                                        {:accept :json
                                         :content-type :json
                                         :throw-exceptions false
                                         :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}
                                         :form-params (valid-product)})
                  body (-> response :body (cheshire/parse-string true))]
              (is (= 409 (:status response)))
              (is (= "Product with this SKU already exists" (:error body))))))))))
