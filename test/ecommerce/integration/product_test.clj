(ns ecommerce.integration.product-test
  (:require
   [cheshire.core :as cheshire]
   [clojure.test :refer [deftest is testing]]
   [ecommerce.integration.integration-test-helpers :as test-helper]
   [ecommerce.integration.product-test-helpers :as product-test-helpers]))

(deftest ^:integration create-product-unauthorized-test
  (testing "POST /api/v1/product/create - without authorization should return 401"
    (test-helper/with-test-database-and-kafka
      (fn []
        (let [response (product-test-helpers/post-product (product-test-helpers/valid-product))
              body     (test-helper/parse-body response)]
          (is (= 401 (:status response)))
          (is (= "Authentication required" (:error body))))))))

(deftest ^:integration create-product-success-test
  (testing "POST /api/v1/product/create - with valid data should create product"
    (test-helper/with-test-database-and-kafka
      (fn []
        (let [response (product-test-helpers/post-product (product-test-helpers/valid-product) {:headers (test-helper/auth-headers)})
              body     (test-helper/parse-body response)]
          (is (= 201 (:status response)))
          (is (= "Product created successfully" (:message body)))
          (is (some? (:id body))))))))

(deftest ^:integration create-product-publishes-kafka-event-test
  (testing "POST /api/v1/product/create - should publish event to product-events topic"
    (test-helper/with-test-database-and-kafka
      (fn []
        (product-test-helpers/post-product (product-test-helpers/valid-product) {:headers (test-helper/auth-headers)})

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
        (product-test-helpers/post-product (product-test-helpers/valid-product) {:headers (test-helper/auth-headers)})

        (let [response (product-test-helpers/post-product (product-test-helpers/valid-product) {:headers (test-helper/auth-headers)})
              body     (test-helper/parse-body response)]
          (is (= 409 (:status response)))
          (is (= "Product with this SKU already exists" (:error body))))))))
