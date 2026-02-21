(ns ecommerce.integration.customer-management-test
  (:require
   [clj-http.client :as client]
   [clojure.test :refer [deftest is testing]]
   [ecommerce.integration.integration-test-helpers :as test-helper]))

;; TODO: add create-customer-test endpoint with ADMIN role for test data isolation
;;- Enable isolated test data creation without relying on database migrations

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
          (is (vector? age-group))
          (is (every? #(every? (fn [k] (contains? % k)) [:age-range :count]) age-group)))))))

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
            (is (every? #(= "MALE" (:gender %)) customers))))

        (testing "FEMALE gender should return female customers"
          (let [response (client/get "http://localhost:3001/api/v1/customer-management/gender/FEMALE"
                                     {:accept :json
                                      :headers (test-helper/auth-headers)})
                body (test-helper/parse-body response)
                customers (:customer-by-gender body)]

            (is (= 200 (:status response)))
            (is (some? customers))
            (is (vector? customers))
            (is (every? #(= "FEMALE" (:gender %)) customers))))))))

(deftest ^:integration get-customers-registration-trend
  (testing "GET /api/v1/customer-management/registration-trend should handle different periods"
    (test-helper/with-test-database
      (fn []
        (testing "YEAR period should return yearly trends"
          (let [response (client/get "http://localhost:3001/api/v1/customer-management/registration-trend/YEAR"
                                     {:accept :json
                                      :headers (test-helper/auth-headers)})
                body (test-helper/parse-body response)
                trends (:trends body)]

            (is (= 200 (:status response)))
            (is (some? trends))
            (is (vector? trends))
            (is (every? #(every? (fn [k] (contains? % k)) [:period :count]) trends))))

        (testing "MONTH period should return monthly trends"
          (let [response (client/get "http://localhost:3001/api/v1/customer-management/registration-trend/MONTH"
                                     {:accept :json
                                      :headers (test-helper/auth-headers)})
                body (test-helper/parse-body response)
                trends (:trends body)]

            (is (= 200 (:status response)))
            (is (some? trends))
            (is (vector? (:trends body)))
            (is (every? #(every? (fn [k] (contains? % k)) [:period :count]) trends))))

        (testing "DAY period should return daily trends"
          (let [response (client/get "http://localhost:3001/api/v1/customer-management/registration-trend/DAY"
                                     {:accept :json
                                      :headers (test-helper/auth-headers)})
                body (test-helper/parse-body response)
                trends (:trends body)]

            (is (= 200 (:status response)))
            (is (some? trends))
            (is (vector? (:trends body)))
            (is (every? #(every? (fn [k] (contains? % k)) [:period :count]) trends))))))))

(deftest ^:integration get-active-customers
  (testing "GET /api/v1/customer-management/active should return active customers"
    (test-helper/with-test-database
      (fn []
        (let [response (client/get "http://localhost:3001/api/v1/customer-management/active"
                                   {:accept :json
                                    :headers (test-helper/auth-headers)})
              body (test-helper/parse-body response)
              active (:active body)]

          (is (= 200 (:status response)))
          (is (some? active))
          (is (vector? active))
          (is (every? #(every? (fn [k] (contains? % k)) [:total]) active)))))))

(deftest ^:integration get-inactive-customers
  (testing "GET /api/v1/customer-management/inactive should return inactive customers"
    (test-helper/with-test-database
      (fn []
        (let [response (client/get "http://localhost:3001/api/v1/customer-management/inactive"
                                   {:accept :json
                                    :headers (test-helper/auth-headers)})
              body (test-helper/parse-body response)
              inactive (:inactive body)]

          (is (= 200 (:status response)))
          (is (vector? inactive))
          (is (vector? inactive))
          (is (every? #(every? (fn [k] (contains? % k)) [:total]) inactive)))))))

(deftest ^:integration get-segment-by-demographics
  (testing "GET /api/v1/customer-management/segment-demographics/:country/:gender/:min-age/:max-age should return 
    segment-demographics customers"
    (test-helper/with-test-database
      (fn []
        (let [response (client/get "http://localhost:3001/api/v1/customer-management/segment-demographics/Mexico/MALE/10/40"
                                   {:accept :json
                                    :headers (test-helper/auth-headers)})
              body (test-helper/parse-body response)
              segment-demographics (:segment-demographics body)]

          (is (= 200 (:status response)))
          (is (some? segment-demographics))
          (is (vector? segment-demographics))
          (is (every? #(every? (fn [k] (contains? % k)) [:names :first_surname :second_surname :email
                                                         :country_of_birth :gender :active :age]) segment-demographics)))))))

(deftest ^:integration get-registration-by-country-code
  (testing "GET /api/v1/customer-management/registration-by-country-code should return customers by country code"
    (test-helper/with-test-database
      (fn []
        (let [response (client/get "http://localhost:3001/api/v1/customer-management/registration-by-country-code"
                                   {:accept :json
                                    :headers (test-helper/auth-headers)})
              body (test-helper/parse-body response)
              registration-by-country-code (:registration-by-country-code body)]

          (is (= 200 (:status response)))
          (is (some? registration-by-country-code))
          (is (vector? registration-by-country-code))
          (is (every? #(every? (fn [k] (contains? % k)) [:country_code :registrations]) registration-by-country-code)))))))

(deftest ^:integration get-customers-with-password-reset-code
  (testing "GET /api/v1/customer-management/customers-with-password-reset-code should return empty response"
    (test-helper/with-test-database
      (fn []
        (let [response (client/get "http://localhost:3001/api/v1/customer-management/customers-with-password-reset-code"
                                   {:accept :json
                                    :headers (test-helper/auth-headers)})
              body (test-helper/parse-body response)
              customers-with-password-reset-code (:customers-with-password-reset-code body)]

          (is (= 200 (:status response)))
          (is (some? customers-with-password-reset-code))
          (is (vector? customers-with-password-reset-code)))))))
