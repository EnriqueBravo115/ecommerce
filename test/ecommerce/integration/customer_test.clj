(ns ecommerce.integration.customer-test
  (:require
   [cheshire.core :as cheshire]
   [clj-http.client :as client]
   [clojure.test :refer [deftest is testing]]
   [com.stuartsierra.component :as component]
   [ecommerce.core :as system]
   [ecommerce.utils.jwt :as jwt])
  (:import (org.testcontainers.containers PostgreSQLContainer)))

(defmacro with-system
  [[bound-var binding-expr] & body]
  `(let [~bound-var (component/start ~binding-expr)]
     (try
       ~@body
       (finally
         (component/stop ~bound-var)))))

(deftest ^:integration get-customer-by-id
  (testing "GET /api/v1/customer/:id should return customer"
    (let [database-container (PostgreSQLContainer. "postgres:15.4")]
      (try
        (.start database-container)
        (with-system
          [sut (system/system-component {:server {:port 3001}
                                         :db-spec {:jdbcUrl (.getJdbcUrl database-container)
                                                   :username (.getUsername database-container)
                                                   :password (.getPassword database-container)}
                                         :auth {:jwt
                                                {:secret "123456789"
                                                 :alg :hs512
                                                 :expires-in 3600}}})]
          (let [response (client/get "http://localhost:3001/api/v1/customer/1" {:accept :json
                                                                                :headers {"Authorization"
                                                                                          (str "Bearer " (jwt/generate-test-token))}})
                body (-> response :body (cheshire/parse-string true))
                customer (:customer body)]

            (is (= 200 (:status response)))
            (is (= {:names "María Elena"
                    :first_surname "García"
                    :second_surname "López"
                    :email "maria.garcia@email.com"
                    :active true}
                   customer))))
        (finally (.stop database-container))))))

(deftest ^:integration get-customers-country-count
  (testing "GET /api/v1/customer/country-count should return country count aggregation"
    (let [database-container (PostgreSQLContainer. "postgres:15.4")]
      (try
        (.start database-container)
        (with-system
          [sut (system/system-component {:server {:port 3001}
                                         :db-spec {:jdbcUrl (.getJdbcUrl database-container)
                                                   :username (.getUsername database-container)
                                                   :password (.getPassword database-container)}
                                         :auth {:jwt
                                                {:secret "123456789"
                                                 :alg :hs512
                                                 :expires-in 3600}}})]
          (let [response (client/get "http://localhost:3001/api/v1/customer/country-count" {:accept :json
                                                                                            :headers {"Authorization" (str "Bearer " (jwt/generate-test-token))}})
                body (-> response :body (cheshire/parse-string true))
                country-count (:country-count body)]

            (is (= 200 (:status response)))
            (is (vector? country-count))
            (let [country-map
                  (into {} (map (fn [item] [(:country_of_birth item) (:count item)]) country-count))]
              (is (= 1 (get country-map "COL")))
              (is (= 1 (get country-map "CHL")))
              (is (= 1 (get country-map "ARG")))
              (is (= 1 (get country-map "ESP")))
              (is (= 5 (get country-map "MEX")))
              (is (= 1 (get country-map "USA"))))))
        (finally (.stop database-container))))))

(deftest ^:integration get-customers-by-age-group
  (testing "GET /api/v1/customer/age-group should return country count aggregation"
    (let [database-container (PostgreSQLContainer. "postgres:15.4")]
      (try
        (.start database-container)
        (with-system
          [sut (system/system-component {:server {:port 3001}
                                         :db-spec {:jdbcUrl (.getJdbcUrl database-container)
                                                   :username (.getUsername database-container)
                                                   :password (.getPassword database-container)}})]
          (let [response (client/get "http://localhost:3001/api/v1/customer/age-group" {:accept :json})
                body (-> response :body (cheshire/parse-string true))
                age-group (:age-group body)]

            (is (= 200 (:status response)))
            (is (vector? age-group))
            (let [age-map (into {} (map (juxt :age-range :count) age-group))]
              (print age-map)
              (is (= 2 (get age-map "18-29")))
              (is (= 4 (get age-map "30-39")))
              (is (= 1 (get age-map "40-49")))
              (is (= 1 (get age-map "50-59")))
              (is (= 2 (get age-map "60-69"))))))
        (finally (.stop database-container))))))

(deftest ^:integration get-customers-by-gender
  (testing "GET /api/v1/customer/gender should return customer by gender"
    (let [database-container (PostgreSQLContainer. "postgres:15.4")]
      (try
        (.start database-container)
        (with-system
          [sut (system/system-component {:server {:port 3001}
                                         :db-spec {:jdbcUrl (.getJdbcUrl database-container)
                                                   :username (.getUsername database-container)
                                                   :password (.getPassword database-container)}})]
          (let [response (client/get "http://localhost:3001/api/v1/customer/gender"
                                     {:accept :json
                                      :content-type :json
                                      :body (cheshire/generate-string {:gender "FEMALE"})})
                body (-> response :body (cheshire/parse-string true))
                customers (:customer-by-gender body)]

            (is (= 200 (:status response)))
            (is (vector? customers))
            (is (= 5 (count customers)))
            (is (every? #(= "FEMALE" (:gender %)) customers))))
        (finally (.stop database-container))))))
