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

(defn- auth-headers []
  {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))})

(defn- parse-body [response]
  (-> response :body (cheshire/parse-string true)))

(defn- post-product
  ([product] (post-product product {}))
  ([product extra-opts]
   (client/post "http://localhost:3001/api/v1/product/create"
                (merge {:accept           :json
                        :content-type     :json
                        :throw-exceptions false
                        :form-params      product}
                       extra-opts))))

(deftest ^:integration create-product-unauthorized-test
  (testing "POST /api/v1/product/create - without authorization should return 401"
    (test-helper/with-test-database-and-kafka
      (fn []
        (let [response (post-product (valid-product))
              body     (parse-body response)]
          (is (= 401 (:status response)))
          (is (= "Authentication required" (:error body))))))))

(deftest ^:integration create-product-success-test
  (testing "POST /api/v1/product/create - with valid data should create product"
    (test-helper/with-test-database-and-kafka
      (fn []
        (let [response (post-product (valid-product) {:headers (auth-headers)})
              body     (parse-body response)]
          (is (= 201 (:status response)))
          (is (= "Product created successfully" (:message body)))
          (is (some? (:id body))))))))

(deftest ^:integration create-product-publishes-kafka-event-test
  (testing "POST /api/v1/product/create - should publish event to product-events topic"
    (test-helper/with-test-database-and-kafka
      (fn []
        (post-product (valid-product) {:headers (auth-headers)})

        (test-helper/with-test-consumer
          (fn [consumer]
            (let [msg     (test-helper/consume-next-message consumer "product-events" 10000)
                  payload (some-> msg :value (cheshire/parse-string true))]

              (testing "A message should arrive within the timeout"
                (is (some? msg) "No message received from product-events within 10s"))

              (testing "Event payload should contain correct fields"
                (is (= "product.created" (:event-type payload)))
                (is (= "LAPTOP-001"      (:sku payload)))
                (is (some? (:product-id  payload)))
                (is (some? (:seller-id   payload)))
                (is (some? (:timestamp   payload)))))))))))

(deftest ^:integration create-duplicate-product-test
  (testing "POST /api/v1/product/create - duplicate SKU should return 409"
    (test-helper/with-test-database-and-kafka
      (fn []
        (post-product (valid-product) {:headers (auth-headers)})

        (let [response (post-product (valid-product) {:headers (auth-headers)})
              body     (parse-body response)]
          (is (= 409 (:status response)))
          (is (= "Product with this SKU already exists" (:error body))))))))
