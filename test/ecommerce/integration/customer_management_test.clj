(ns ecommerce.integration.customer-management-test
  (:require
   [cheshire.core :as cheshire]
   [clj-http.client :as client]
   [ecommerce.utils.jwt :as jwt]
   [clojure.test :refer [deftest is testing]]
   [ecommerce.integration.integration-test-helpers :as test-helper]))

(deftest ^:integration get-customer-by-id
  (testing "GET /api/v1/customer-management/:id should return customer"
    (test-helper/with-test-database
      (fn []
        (let [response (client/get "http://localhost:3001/api/v1/customer-management/1"
                                   {:accept :json
                                    :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
              body (-> response :body (cheshire/parse-string true))]

          (is (= 200 (:status response)))
          (is (= {:names "María Elena"
                  :first_surname "García"
                  :second_surname "López"
                  :email "maria.garcia@email.com"
                  :active true}
                 (:customer body))))))))

(deftest ^:integration get-customers-country-count
  (testing "GET /api/v1/customer-management/country-count should return country count aggregation"
    (test-helper/with-test-database
      (fn []
        (let [response (client/get "http://localhost:3001/api/v1/customer-management/country-count"
                                   {:accept :json
                                    :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
              body (-> response :body (cheshire/parse-string true))
              country-count (:country-count body)
              country-map (into {} (map (juxt :country_of_birth :count) country-count))]

          (is (= 200 (:status response)))
          (is (vector? country-count))
          (is (= 6 (count country-map)))
          (is (= 1 (get country-map "Colombia")))
          (is (= 1 (get country-map "Chile")))
          (is (= 1 (get country-map "Argentina")))
          (is (= 1 (get country-map "Spain")))
          (is (= 5 (get country-map "Mexico")))
          (is (= 1 (get country-map "United States of America"))))))))

(deftest ^:integration get-customers-by-age-group
  (testing "GET /api/v1/customer-management/age-group should return age group aggregation"
    (test-helper/with-test-database
      (fn []
        (let [response (client/get "http://localhost:3001/api/v1/customer-management/age-group"
                                   {:accept :json
                                    :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
              body (-> response :body (cheshire/parse-string true))
              age-group (:age-group body)
              age-map (into {} (map (juxt :age-range :count) age-group))]

          (is (= 200 (:status response)))
          (is (vector? age-group))
          (is (= 2 (get age-map "18-29")))
          (is (= 4 (get age-map "30-39")))
          (is (= 1 (get age-map "40-49")))
          (is (= 1 (get age-map "50-59")))
          (is (= 2 (get age-map "60-69"))))))))

(deftest ^:integration get-customers-by-gender
  (testing "GET /api/v1/customer-management/gender should handle different genders"
    (test-helper/with-test-database
      (fn []
        (testing "MALE gender should return male customers"
          (let [response (client/get "http://localhost:3001/api/v1/customer-management/gender/MALE"
                                     {:accept :json
                                      :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
                body (-> response :body (cheshire/parse-string true))
                customers (:customer-by-gender body)]

            (is (= 200 (:status response)))
            (is (vector? customers))
            (is (= 5 (count customers)))
            (is (every? #(= "MALE" (:gender %)) customers))))

        (testing "FEMALE gender should retur female customers"
          (let [response (client/get "http://localhost:3001/api/v1/customer-management/gender/FEMALE"
                                     {:accept :json
                                      :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
                body (-> response :body (cheshire/parse-string true))
                customers (:customer-by-gender body)]

            (is (= 200 (:status response)))
            (is (vector? customers))
            (is (= 5 (count customers)))
            (is (every? #(= "FEMALE" (:gender %)) customers))))))))

(deftest ^:integration get-customers-registration-trend
  (testing "GET /api/v1/customer-management/registration-trend should handle different periods"
    (test-helper/with-test-database
      (fn []
        (testing "YEAR period should return yearly trends"
          (let [response (client/get "http://localhost:3001/api/v1/customer-management/registration-trend/YEAR"
                                     {:accept :json
                                      :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
                body (-> response :body (cheshire/parse-string true))
                trends (:trends body)]

            (is (= 200 (:status response)))
            (is (vector? trends))
            (is (= 2 (count trends)))))

        (testing "MONTH period should return monthly trends"
          (let [response (client/get "http://localhost:3001/api/v1/customer-management/registration-trend/MONTH"
                                     {:accept :json
                                      :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
                body (-> response :body (cheshire/parse-string true))
                trends (:trends body)]

            (is (= 200 (:status response)))
            (is (vector? trends))
            (is (= 4 (count trends)))))

        (testing "DAY period should return daily trends"
          (let [response (client/get "http://localhost:3001/api/v1/customer-management/registration-trend/DAY"
                                     {:accept :json
                                      :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
                body (-> response :body (cheshire/parse-string true))
                trends (:trends body)]

            (is (= 200 (:status response)))
            (is (vector? trends))
            (is (= 10 (count trends)))))))))

(deftest ^:integration get-active-customers
  (testing "GET /api/v1/customer-management/active should return active customers"
    (test-helper/with-test-database
      (fn []
        (let [response (client/get "http://localhost:3001/api/v1/customer-management/active"
                                   {:accept :json
                                    :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
              body (-> response :body (cheshire/parse-string true))
              active (:active body)]

          (is (= 200 (:status response)))
          (is (vector? active))
          (is (= 1 (count active)))
          (is (= {:total 8} (first active))))))))

(deftest ^:integration get-inactive-customers
  (testing "GET /api/v1/customer-management/inactive should return inactive customers"
    (test-helper/with-test-database
      (fn []
        (let [response (client/get "http://localhost:3001/api/v1/customer-management/inactive"
                                   {:accept :json
                                    :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
              body (-> response :body (cheshire/parse-string true))
              inactive (:inactive body)]

          (is (= 200 (:status response)))
          (is (vector? inactive))
          (is (= 1 (count inactive)))
          (is (= {:total 2} (first inactive))))))))

(deftest ^:integration get-segment-by-demographics
  (testing "GET /api/v1/customer-management/segment-demographics/:country/:gender/:min-age/:max-age should return 
    segment-demographics customers"
    (test-helper/with-test-database
      (fn []
        (let [response (client/get "http://localhost:3001/api/v1/customer-management/segment-demographics/Mexico/MALE/10/40"
                                   {:accept :json
                                    :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
              body (-> response :body (cheshire/parse-string true))
              segment-demographics (:segment-demographics body)]

          (is (= 200 (:status response)))
          (doseq [customer segment-demographics]
            (is (= "Mexico" (:country_of_birth customer)))
            (is (= "MALE" (:gender customer)))
            (is (<= 10 (:age customer) 40))))))))

(deftest ^:integration get-registration-by-country-code
  (testing "GET /api/v1/customer-management/registration-by-country-code should return customers by country code"
    (test-helper/with-test-database
      (fn []
        (let [response (client/get "http://localhost:3001/api/v1/customer-management/registration-by-country-code"
                                   {:accept :json
                                    :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
              body (-> response :body (cheshire/parse-string true))
              registrations (:registration-by-country-code body)]

          (is (= 200 (:status response)))
          (doseq [item registrations]
            (is (= 2 (count (:country_code item))))
            (is (re-matches #"[A-Z]{2}" (:country_code item)))
            (is (pos? (:registrations item)))))))))

(deftest ^:integration get-customers-with-password-reset-code
  (testing "GET /api/v1/customer-management/customers-with-password-reset-code should return empty response"
    (test-helper/with-test-database
      (fn []
        (let [response (client/get "http://localhost:3001/api/v1/customer-management/customers-with-password-reset-code"
                                   {:accept :json
                                    :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
              body (-> response :body (cheshire/parse-string true))
              password_reset (:customers-with-password-reset-code body)]

          (is (= 200 (:status response)))
          (is (empty? password_reset)))))))
