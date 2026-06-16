(ns global-service.integration.customer-management-test
  (:require
   [clj-http.client :as client]
   [clojure.test :refer [deftest is testing]]
   [global-service.integration.integration-test-helpers :as test-helper]))

;; TODO: add create-customer-test endpoint with ADMIN role for test data isolation

(deftest ^:integration get-customer-by-id
  (testing "GET /api/v1/customer-management/:id should return customer"
    (test-helper/with-test-database
      (fn []
        (let [response (client/get "http://localhost:3001/api/v1/customer-management/1"
                                   {:accept :json
                                    :headers (test-helper/auth-headers)})
              body (test-helper/parse-body response)
              customer (:customer body)]

          (is (= 200 (:status response)))
          (is (some? customer))
          (is (map? customer))
          (is (every? #(contains? customer %) [:names :first_surname :second_surname :email :active])))))))

(deftest ^:integration get-customers-country-count
  (testing "GET /api/v1/customer-management/country-count should return country count aggregation"
    (test-helper/with-test-database
      (fn []
        (let [response (client/get "http://localhost:3001/api/v1/customer-management/country-count"
                                   {:accept :json
                                    :headers (test-helper/auth-headers)})
              body (test-helper/parse-body response)
              country-count (:country-count body)]

          (is (= 200 (:status response)))
          (is (some? country-count))
          (is (vector? country-count))
          (is (every? #(every? (fn [k] (contains? % k)) [:country_of_birth :count]) country-count)))))))

(deftest ^:integration get-customers-by-age-group
  (testing "GET /api/v1/customer-management/age-group should return age group aggregation"
    (test-helper/with-test-database
      (fn []
        (let [response (client/get "http://localhost:3001/api/v1/customer-management/age-group"
                                   {:accept :json
                                    :headers (test-helper/auth-headers)})
              body (test-helper/parse-body response)
              age-group (:age-group body)]

          (is (= 200 (:status response)))
          (is (some? age-group))
          (is (vector? age-group)))))))

(deftest ^:integration get-customers-by-gender
  (testing "GET /api/v1/customer-management/gender should handle different genders"
    (test-helper/with-test-database
      (fn []
        (testing "MALE gender should return male customers"
          (let [response (client/get "http://localhost:3001/api/v1/customer-management/gender/MALE"
                                     {:accept :json
                                      :headers (test-helper/auth-headers)})
                body (test-helper/parse-body response)
                customers (:customer-by-gender body)]

            (is (= 200 (:status response)))
            (is (some? customers))
            (is (vector? customers))
            (is (every? #(= "MALE" (:gender %)) customers))))))))

(deftest ^:integration get-active-customers
  (testing "GET /api/v1/customer-management/active should return active customers"
    (test-helper/with-test-database
      (fn []
        (let [response (client/get "http://localhost:3001/api/v1/customer-management/active"
                                   {:accept :json
                                    :headers (test-helper/auth-headers)})
              body (test-helper/parse-body response)]

          (is (= 200 (:status response)))
          (is (contains? body :active)))))))

(deftest ^:integration get-segment-by-demographics
  (testing "GET /api/v1/customer-management/segment-demographics/:country/:gender/:min-age/:max-age"
    (test-helper/with-test-database
      (fn []
        (let [response (client/get "http://localhost:3001/api/v1/customer-management/segment-demographics/Mexico/MALE/10/40"
                                   {:accept :json
                                    :headers (test-helper/auth-headers)})
              body (test-helper/parse-body response)]

          (is (= 200 (:status response)))
          (is (contains? body :segment-demographics)))))))

(deftest ^:integration get-registration-by-country-code
  (testing "GET /api/v1/customer-management/registration-by-country-code should return customers by country code"
    (test-helper/with-test-database
      (fn []
        (let [response (client/get "http://localhost:3001/api/v1/customer-management/registration-by-country-code"
                                   {:accept :json
                                    :headers (test-helper/auth-headers)})
              body (test-helper/parse-body response)]

          (is (= 200 (:status response)))
          (is (contains? body :registration-by-country-code)))))))
