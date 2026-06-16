(ns global-service.integration.address-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [global-service.integration.address-test-helpers :as address-helpers]
   [global-service.integration.integration-test-helpers :as test-helper]))

(deftest ^:integration create-address-primary-true-test
  (testing "POST /api/v1/address/create-address - should create primary address successfully"
    (test-helper/with-test-database
      (fn []
        (let [response (address-helpers/create-address (address-helpers/address_primary_true)
                                                       {:headers (test-helper/auth-headers)})
              body (test-helper/parse-body response)]
          (is (= 201 (:status response)))
          (is (= "Primary address created successfully" (:message body))))))))

(deftest ^:integration create-address-primary-false-test
  (testing "POST /api/v1/address/create-address - should create non-primary address successfully"
    (test-helper/with-test-database
      (fn []
        (let [response (address-helpers/create-address (address-helpers/address_primary_false)
                                                       {:headers (test-helper/auth-headers)})
              body (test-helper/parse-body response)]
          (is (= 201 (:status response)))
          (is (= "Address created successfully" (:message body))))))))

(deftest ^:integration create-address-exceed-limit-test
  (testing "POST /api/v1/address/create-address - should return error when exceeding 3 addresses"
    (test-helper/with-test-database
      (fn []
        (address-helpers/create-address (address-helpers/address_primary_true) {:headers (test-helper/auth-headers)})
        (address-helpers/create-address (address-helpers/address_primary_false) {:headers (test-helper/auth-headers)})
        (address-helpers/create-address (address-helpers/address_primary_false) {:headers (test-helper/auth-headers)})
        (let [response (address-helpers/create-address (address-helpers/address_primary_true)
                                                       {:headers (test-helper/auth-headers)})
              body (test-helper/parse-body response)]
          (is (= 400 (:status response)))
          (is (= "Customer cannot have more than 3 addresses, delete at least 1" (:error body))))))))

(deftest ^:integration create-address-no-auth-test
  (testing "POST /api/v1/address/create-address - should return 401 when no auth headers are provided"
    (test-helper/with-test-database
      (fn []
        (let [response (address-helpers/create-address (address-helpers/address_primary_true) {})
              body (test-helper/parse-body response)]
          (is (= 401 (:status response)))
          (is (= "Authentication required" (:error body))))))))

(deftest ^:integration update-address-success-test
  (testing "POST /api/v1/address/update-address/:address_id - should update address with valid data"
    (test-helper/with-test-database
      (fn []
        (let [create-response (address-helpers/create-address (address-helpers/address_primary_true)
                                                              {:headers (test-helper/auth-headers)})
              create-body     (test-helper/parse-body create-response)
              address-id      (:id create-body)]
          (is (= 201 (:status create-response)))
          (let [update-response (address-helpers/update-address address-id (address-helpers/address_update)
                                                                {:headers (test-helper/auth-headers)})
                update-body     (test-helper/parse-body update-response)]
            (is (= 200 (:status update-response)))
            (is (= "Address updated successfully" (:message update-body)))))))))

(deftest ^:integration delete-address-success-test
  (testing "DELETE /api/v1/address/delete-address/:address_id - should delete non-primary address successfully"
    (test-helper/with-test-database
      (fn []
        (let [create-response (address-helpers/create-address (address-helpers/address_primary_false)
                                                              {:headers (test-helper/auth-headers)})
              create-body     (test-helper/parse-body create-response)
              address-id      (:id create-body)]
          (is (= 201 (:status create-response)))
          (let [delete-response (address-helpers/delete-address address-id {:headers (test-helper/auth-headers)})
                delete-body     (test-helper/parse-body delete-response)]
            (is (= 200 (:status delete-response)))
            (is (= "Address deleted successfully" (:message delete-body)))))))))

(deftest ^:integration get-customer-addresses-success-test
  (testing "GET /api/v1/address/get-customer-addresses - should return customer addresses"
    (test-helper/with-test-database
      (fn []
        (let [_            (address-helpers/create-address (address-helpers/address_primary_true)
                                                           {:headers (test-helper/auth-headers)})]
          (let [get-response (address-helpers/get-customer-addresses {:headers (test-helper/auth-headers)})
                get-body     (test-helper/parse-body get-response)]
            (is (= 200 (:status get-response)))
            (is (contains? get-body :addresses))))))))

(deftest ^:integration get-customers-by-location-success-test
  (testing "GET /api/v1/address/admin/get-customers-by-location/:country/:state/:city - should return customers by location"
    (test-helper/with-test-database
      (fn []
        (let [get-response (address-helpers/get-customers-by-location "Mexico" "Jalisco" "Guadalajara"
                                                                      {:headers (test-helper/auth-headers)})
              get-body     (test-helper/parse-body get-response)]
          (is (= 200 (:status get-response)))
          (is (contains? get-body :customers)))))))
