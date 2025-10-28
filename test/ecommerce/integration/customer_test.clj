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
  (testing "first test"
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
