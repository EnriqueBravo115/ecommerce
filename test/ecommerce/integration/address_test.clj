(ns ecommerce.integration.address-test
  (:require
   [cheshire.core :as cheshire]
   [clj-http.client :as client]
   [ecommerce.utils.jwt :as jwt]
   [clojure.test :refer [deftest is testing]]
   [ecommerce.integration.address-test-helpers :as address-helpers]
   [ecommerce.integration.integration-test-helpers :as test-helper]))

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

(deftest ^:integration create-address-update-primary-test
  (testing "POST /api/v1/address/create-address - should update primary when another primary address already exists"
    (test-helper/with-test-database
      (fn []
        (address-helpers/create-address (address-helpers/address_primary_true) {:headers (test-helper/auth-headers)})
        (let [response (address-helpers/create-address (address-helpers/address_primary_true)
                                                       {:headers (test-helper/auth-headers)})
              body (test-helper/parse-body response)]
          (is (= 201 (:status response)))
          (is (= "Primary address updated successfully" (:message body))))))))

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

(deftest ^:integration update-address-not-found-test
  (testing "POST /api/v1/address/update-address/:address_id - should return 404 when address does not exist"
    (test-helper/with-test-database
      (fn []
        (let [update-response (address-helpers/update-address 999999 (address-helpers/address_update)
                                                              {:headers (test-helper/auth-headers)})
              update-body     (test-helper/parse-body update-response)]
          (is (= 404 (:status update-response)))
          (is (= "Address not found" (:error update-body))))))))

(deftest ^:integration update-address-not-authorized-test
  (testing "POST /api/v1/address/update-address/:address_id - should return 403 when address belongs to another customer"
    (test-helper/with-test-database
      (fn []
        (let [create-response (address-helpers/create-address (address-helpers/address_primary_true)
                                                              {:headers (test-helper/auth-headers)})
              create-body     (test-helper/parse-body create-response)
              address-id      (:id create-body)]
          (is (= 201 (:status create-response)))
          (let [update-response (address-helpers/update-address address-id (address-helpers/address_update)
                                                                {:headers (test-helper/auth-headers-customer)})
                update-body     (test-helper/parse-body update-response)]
            (is (= 403 (:status update-response)))
            (is (= "Not authorized to update this address" (:error update-body)))))))))

(deftest ^:integration update-address-no-auth-test
  (testing "POST /api/v1/address/update-address/:address_id - should return 401 when no auth headers provided"
    (test-helper/with-test-database
      (fn []
        (let [update-response (address-helpers/update-address 1 (address-helpers/address_update) {})
              update-body     (test-helper/parse-body update-response)]
          (is (= 401 (:status update-response)))
          (is (= "Authentication required" (:error update-body))))))))

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

(deftest ^:integration delete-address-primary-test
  (testing "DELETE /api/v1/address/delete-address/:address_id - should return 400 when deleting primary address"
    (test-helper/with-test-database
      (fn []
        (let [create-response (address-helpers/create-address (address-helpers/address_primary_true)
                                                              {:headers (test-helper/auth-headers)})
              create-body     (test-helper/parse-body create-response)
              address-id      (:id create-body)]
          (is (= 201 (:status create-response)))
          (let [delete-response (address-helpers/delete-address address-id {:headers (test-helper/auth-headers)})
                delete-body     (test-helper/parse-body delete-response)]
            (is (= 400 (:status delete-response)))
            (is (= "Cannot delete primary address" (:error delete-body)))))))))

(deftest ^:integration delete-address-not-found-test
  (testing "DELETE /api/v1/address/delete-address/:address_id - should return 404 when address does not exist"
    (test-helper/with-test-database
      (fn []
        (let [delete-response (address-helpers/delete-address 999999999 {:headers (test-helper/auth-headers)})
              delete-body     (test-helper/parse-body delete-response)]
          (is (= 404 (:status delete-response)))
          (is (= "Address not found" (:error delete-body))))))))

(deftest ^:integration delete-address-not-authorized-test
  (testing "DELETE /api/v1/address/delete-address/:address_id - should return 403 when address belongs to another customer"
    (test-helper/with-test-database
      (fn []
        (let [create-response (address-helpers/create-address (address-helpers/address_primary_false)
                                                              {:headers (test-helper/auth-headers)})
              create-body     (test-helper/parse-body create-response)
              address-id      (:id create-body)]
          (is (= 201 (:status create-response)))
          (let [delete-response (address-helpers/delete-address address-id {:headers (test-helper/auth-headers-customer)})
                delete-body     (test-helper/parse-body delete-response)]
            (is (= 403 (:status delete-response)))
            (is (= "Not authorized to delete this address" (:error delete-body)))))))))

(deftest ^:integration delete-address-no-auth-test
  (testing "DELETE /api/v1/address/delete-address/:address_id - should return 401 when no auth headers provided"
    (test-helper/with-test-database
      (fn []
        (let [delete-response (address-helpers/delete-address 1)
              delete-body     (test-helper/parse-body delete-response)]
          (is (= 401 (:status delete-response)))
          (is (= "Authentication required" (:error delete-body))))))))

(deftest ^:integration set-primary-address-success-test
  (testing "POST /api/v1/address/set-primary-address/:address_id - should set primary address successfully"
    (test-helper/with-test-database
      (fn []
        (let [create-response (address-helpers/create-address (address-helpers/address_primary_false)
                                                              {:headers (test-helper/auth-headers)})
              create-body     (test-helper/parse-body create-response)
              address-id      (:id create-body)]
          (is (= 201 (:status create-response)))
          (let [set-response (address-helpers/set-primary-address address-id {:headers (test-helper/auth-headers)})
                set-body     (test-helper/parse-body set-response)]
            (is (= 200 (:status set-response)))
            (is (= "Primary address updated successfully" (:message set-body)))))))))

(deftest ^:integration set-primary-address-not-found-test
  (testing "POST /api/v1/address/set-primary-address/:address_id - should return 404 when address does not exist"
    (test-helper/with-test-database
      (fn []
        (let [set-response (address-helpers/set-primary-address 999999999 {:headers (test-helper/auth-headers)})
              set-body     (test-helper/parse-body set-response)]
          (is (= 404 (:status set-response)))
          (is (= "Address not found" (:error set-body))))))))

(deftest ^:integration set-primary-address-not-authorized-test
  (testing "POST /api/v1/address/set-primary-address/:address_id - should return 403 when address belongs to another customer"
    (test-helper/with-test-database
      (fn []
        (let [create-response (address-helpers/create-address (address-helpers/address_primary_false)
                                                              {:headers (test-helper/auth-headers)})
              create-body     (test-helper/parse-body create-response)
              address-id      (:id create-body)]
          (is (= 201 (:status create-response)))
          (let [set-response (address-helpers/set-primary-address address-id {:headers (test-helper/auth-headers-customer)})
                set-body     (test-helper/parse-body set-response)]
            (is (= 403 (:status set-response)))
            (is (= "Not authorized to modify this address" (:error set-body)))))))))

(deftest ^:integration set-primary-address-no-auth-test
  (testing "POST /api/v1/address/set-primary-address/:address_id - should return 401 when no auth headers provided"
    (test-helper/with-test-database
      (fn []
        (let [set-response (address-helpers/set-primary-address 1)
              set-body     (test-helper/parse-body set-response)]
          (is (= 401 (:status set-response)))
          (is (= "Authentication required" (:error set-body))))))))

(deftest ^:integration get-customer-addresses-success-test
  (testing "GET /api/v1/address/get-customer-addresses - should return customer addresses"
    (test-helper/with-test-database
      (fn []
        (let [create-response (address-helpers/create-address (address-helpers/address_primary_true)
                                                              {:headers (test-helper/auth-headers)})
              create-body     (test-helper/parse-body create-response)]
          (is (= 201 (:status create-response)))
          (is (= "Primary address created successfully" (:message create-body)))
          (let [get-response (address-helpers/get-customer-addresses {:headers (test-helper/auth-headers)})
                get-body     (test-helper/parse-body get-response)]
            (is (= 200 (:status get-response)))
            (is (= [{:country     "Mexico"
                     :state       "Guanajuato"
                     :city        "Guanajuato"
                     :street      "Main Street"
                     :postal_code "12345"
                     :is_primary  true}]
                   (:addresses get-body)))))))))

(deftest ^:integration get-customer-addresses-no-auth-test
  (testing "GET /api/v1/address/get-customer-addresses - should return 401 when no auth headers provided"
    (test-helper/with-test-database
      (fn []
        (let [get-response (address-helpers/get-customer-addresses)
              get-body     (test-helper/parse-body get-response)]
          (is (= 401 (:status get-response)))
          (is (= "Authentication required" (:error get-body))))))))

(deftest ^:integration get-primary-address-success-test
  (testing "GET /api/v1/address/get-primary-address - should return primary address"
    (test-helper/with-test-database
      (fn []
        (let [create-response (address-helpers/create-address (address-helpers/address_primary_true)
                                                              {:headers (test-helper/auth-headers)})
              create-body     (test-helper/parse-body create-response)]
          (is (= 201 (:status create-response)))
          (is (= "Primary address created successfully" (:message create-body)))
          (let [get-response (address-helpers/get-primary-address {:headers (test-helper/auth-headers)})
                get-body     (test-helper/parse-body get-response)]
            (is (= 200 (:status get-response)))
            (is (= {:country     "Mexico"
                    :state       "Guanajuato"
                    :city        "Guanajuato"
                    :street      "Main Street"
                    :postal_code "12345"
                    :is_primary  true}
                   (:address get-body)))))))))

(deftest ^:integration get-primary-address-no-auth-test
  (testing "GET /api/v1/address/get-primary-address - should return 401 when no auth headers provided"
    (test-helper/with-test-database
      (fn []
        (let [get-response (address-helpers/get-primary-address)
              get-body     (test-helper/parse-body get-response)]
          (is (= 401 (:status get-response)))
          (is (= "Authentication required" (:error get-body))))))))

(deftest ^:integration get-customers-by-location-success-test
  (testing "GET /api/v1/address/admin/get-customers-by-location/:country/:state/:city - should return customers by location"
    (test-helper/with-test-database
      (fn []
        (let [get-response (address-helpers/get-customers-by-location "Mexico" "Jalisco" "Guadalajara"
                                                                      {:headers (test-helper/auth-headers)})
              get-body     (test-helper/parse-body get-response)]
          (is (= 200 (:status get-response)))
          (is (= [{:names          "María Elena"
                   :first_surname  "García"
                   :second_surname "López"
                   :email          "maria.garcia@email.com"
                   :active         true}]
                 (:customers get-body))))))))

(deftest ^:integration get-customers-by-location-no-auth-test
  (testing "GET /api/v1/address/admin/get-customers-by-location/:country/:state/:city - should return 401 when no auth headers provided"
    (test-helper/with-test-database
      (fn []
        (let [get-response (address-helpers/get-customers-by-location "Mexico" "Jalisco" "Guadalajara")
              get-body     (test-helper/parse-body get-response)]
          (is (= 401 (:status get-response)))
          (is (= "Authentication required" (:error get-body))))))))

(deftest ^:integration get-customers-by-postal-code-success-test
  (testing "GET /api/v1/address/admin/get-customers-by-postal-code/:postal_code - should return customers by postal code"
    (test-helper/with-test-database
      (fn []
        (let [get-response (address-helpers/get-customers-by-postal-code "76000" {:headers (test-helper/auth-headers)})
              get-body     (test-helper/parse-body get-response)]
          (is (= 200 (:status get-response)))
          (is (= [{:names          "Jorge Luis"
                   :first_surname  "Santos"
                   :second_surname "Cervantes"
                   :email          "jorge.santos@email.com"
                   :active         true}]
                 (:customers get-body))))))))

(deftest ^:integration get-customers-by-postal-code-no-auth-test
  (testing "GET /api/v1/address/admin/get-customers-by-postal-code/:postal_code - should return 401 when no auth headers provided"
    (test-helper/with-test-database
      (fn []
        (let [get-response (address-helpers/get-customers-by-postal-code "76000")
              get-body     (test-helper/parse-body get-response)]
          (is (= 401 (:status get-response)))
          (is (= "Authentication required" (:error get-body))))))))

(deftest ^:integration get-location-statistics-success-test
  (testing "GET /api/v1/address/admin/get-location-statistics - should return location statistics"
    (test-helper/with-test-database
      (fn []
        (let [response (address-helpers/get-location-statistics {:headers (test-helper/auth-headers)})
              body     (test-helper/parse-body response)
              stats    (:statistics body)]
          (is (= 200 (:status response)))
          (is (map? stats))
          (is (contains? stats :top_countries))
          (is (contains? stats :top_states))
          (is (contains? stats :top_cities))
          (is (vector? (:top_countries stats)))
          (is (vector? (:top_states stats)))
          (is (vector? (:top_cities stats))))))))

(deftest ^:integration get-location-statistics-no-auth-test
  (testing "GET /api/v1/address/admin/get-location-statistics - should return 401 when no auth headers provided"
    (test-helper/with-test-database
      (fn []
        (let [response (address-helpers/get-location-statistics)
              body     (test-helper/parse-body response)]
          (is (= 401 (:status response)))
          (is (= "Authentication required" (:error body))))))))
