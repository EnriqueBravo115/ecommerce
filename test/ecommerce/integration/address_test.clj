(ns ecommerce.integration.address-test
  (:require
   [cheshire.core :as cheshire]
   [clj-http.client :as client]
   [ecommerce.utils.jwt :as jwt]
   [clojure.test :refer [deftest is testing]]
   [ecommerce.integration.integration-test-helpers :as test-helper]))

(deftest ^:integration create-address-test
  (testing "POST /api/v1/address/create-address - should create address"
    (test-helper/with-test-database
      (fn []
        (testing "Create initial address with is_primary=true"
          (let [response (client/post "http://localhost:3001/api/v1/address/create-address"
                                      {:accept :json
                                       :content-type :json
                                       :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}
                                       :form-params {:country "Mexico"
                                                     :state "Guanajuato"
                                                     :city "Guanajuato"
                                                     :street "Main Street"
                                                     :postal_code "12345"
                                                     :is_primary true}})
                body (-> response :body (cheshire/parse-string true))]

            (is (= 201 (:status response)))
            (is (= "Primary address created successfully" (:message body)))))

        (testing "Create address with is_primary=false"
          (let [response (client/post "http://localhost:3001/api/v1/address/create-address"
                                      {:accept :json
                                       :content-type :json
                                       :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}
                                       :form-params {:country "Canada"
                                                     :state "Ontario"
                                                     :city "Toronto"
                                                     :street "Queen Street"
                                                     :postal_code "M5H 2N2"
                                                     :is_primary false}})

                body (-> response :body (cheshire/parse-string true))]

            (is (= 201 (:status response)))
            (is (= "Address created successfully" (:message body)))))

        (testing "Create new address with is_primary=true when another address exists with is_primary=true"
          (let [response (client/post "http://localhost:3001/api/v1/address/create-address"
                                      {:accept :json
                                       :content-type :json
                                       :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}
                                       :form-params {:country "USA"
                                                     :state "California"
                                                     :city "Los Angeles"
                                                     :street "Sunset Blvd"
                                                     :postal_code "90028"
                                                     :is_primary true}})
                body (-> response :body (cheshire/parse-string true))]
            (is (= 201 (:status response)))
            (is (= "Primary address updated successfully" (:message body)))))
        (testing "Create new address should throw error because exceed 3 address"
          (let [response (client/post "http://localhost:3001/api/v1/address/create-address"
                                      {:accept :json
                                       :content-type :json
                                       :throw-exceptions false
                                       :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}
                                       :form-params {:country "USA"
                                                     :state "California"
                                                     :city "Los Angeles"
                                                     :street "Sunset Blvd"
                                                     :postal_code "90028"
                                                     :is_primary true}})
                body (-> response :body (cheshire/parse-string true))]
            (is (= 400 (:status response)))
            (is (= "Customer cannot have more than 3 addresses, delete at least 1" (:error body)))))))))
