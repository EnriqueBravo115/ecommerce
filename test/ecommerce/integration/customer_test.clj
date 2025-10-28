(ns ecommerce.integration.customer-test
  (:require
   [cheshire.core :as cheshire]
   [clj-http.client :as client]
   [clojure.test :refer [deftest is testing]]
   [com.stuartsierra.component :as component]
   [ecommerce.components.system :as system]))

(defmacro with-system
  [[bound-var binding-expr] & body]
  `(let [~bound-var (component/start ~binding-expr)]
     (try
       ~@body
       (finally
         (component/stop ~bound-var)))))

(deftest get-customer-by-id
  (testing "GET /api/v1/customer/:id should return customer"
    (with-system
      [sut (system/system-component {:server {:port 3000}
                                     :db-spec {:jdbcUrl "jdbc:postgresql://localhost:5432/ecommerce"
                                               :username "ecommerce"
                                               :password "ecommerce"}})]
      (let [response (client/get "http://localhost:3000/api/v1/customer/1" {:accept :json})
            body (-> response :body (cheshire/parse-string true))
            customer (:customer body)]

        (is (= 200 (:status response)))
        (is (= {:names "María Elena"
                :first_surname "García"
                :second_surname "López"
                :email "maria.garcia@email.com"
                :active true}
               customer))))))

(deftest get-customer-country-count
  (testing "GET /api/v1/customer/country-count should return country count aggregation"
    (with-system
      [sut (system/system-component {:server {:port 3000}
                                     :db-spec {:jdbcUrl "jdbc:postgresql://localhost:5432/ecommerce"
                                               :username "ecommerce"
                                               :password "ecommerce"}})]
      (let [response (client/get "http://localhost:3000/api/v1/customer/country-count" {:accept :json})
            body (-> response :body (cheshire/parse-string true))
            country_count (:country_count body)]

        (is (= 200 (:status response)))
        (is (vector? country_count))
        (let [country-map
              (into {} (map (fn [item] [(:country_of_birth item) (:count item)]) country_count))]
          (is (= 1 (get country-map "COL")))
          (is (= 1 (get country-map "ESP")))
          (is (= 2 (get country-map "MEX")))
          (is (= 1 (get country-map "USA"))))))))
