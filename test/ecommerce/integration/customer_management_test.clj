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
              country-map (into {} (map (fn [item] [(:country_of_birth item) (:count item)]) country-count))]

          (is (= 200 (:status response)))
          (is (vector? country-count))
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
  (testing "GET /api/v1/customer-management/gender should return customers by gender"
    (test-helper/with-test-database
      (fn []
        (let [response (client/get "http://localhost:3001/api/v1/customer-management/gender/FEMALE"
                                   {:accept :json
                                    :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
              body (-> response :body (cheshire/parse-string true))
              customers (:customer-by-gender body)]

          (is (= 200 (:status response)))
          (is (vector? customers))
          (is (= 5 (count customers)))
          (is (every? #(= "FEMALE" (:gender %)) customers)))))))

(deftest ^:integration get-customers-registration-trend
  (testing "GET /api/v1/customer-management/registration-trend should return registration trends"
    (test-helper/with-test-database
      (fn []
        (let [response (client/get "http://localhost:3001/api/v1/customer-management/registration-trend/MONTH"
                                   {:accept :json
                                    :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
              body (-> response :body (cheshire/parse-string true))
              trends (:trends body)]

          (is (= 200 (:status response)))
          (is (vector? trends))
          (is (= 4 (count trends)))
          ;;(is (every? #(= "FEMALE" (:gender %)) trends))
          )))))
