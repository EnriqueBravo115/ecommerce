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

;; TODO: check error cases
(deftest ^:integration update-address-test
  (testing "POST /api/v1/address/update-address/:address_id - should update address"
    (test-helper/with-test-database
      (fn []
        (testing "Create initial address with is_primary=true"
          (let [create-response (client/post "http://localhost:3001/api/v1/address/create-address"
                                             {:accept :json
                                              :content-type :json
                                              :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}
                                              :form-params {:country "Mexico"
                                                            :state "Guanajuato"
                                                            :city "Guanajuato"
                                                            :street "Main Street"
                                                            :postal_code "12345"
                                                            :is_primary true}})
                create-body (-> create-response :body (cheshire/parse-string true))]

            (is (= 201 (:status create-response)))
            (is (= "Primary address created successfully" (:message create-body)))

            (let [recent-id-response (client/get "http://localhost:3001/api/v1/address/get-recent-id-address"
                                                 {:accept :json
                                                  :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
                  recent-id-body (-> recent-id-response :body (cheshire/parse-string true))
                  address-id (-> recent-id-body :recent first :id)]

              (is (= 200 (:status recent-id-response)))

              (testing "Update address with valid data"
                (let [update-response (client/post (str "http://localhost:3001/api/v1/address/update-address/" address-id)
                                                   {:accept :json
                                                    :content-type :json
                                                    :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}
                                                    :form-params {:country "USA"
                                                                  :state "Texas"
                                                                  :city "Austin"
                                                                  :street "Congress Ave"
                                                                  :postal_code "73301"}})
                      update-body (-> update-response :body (cheshire/parse-string true))]

                  (is (= 200 (:status update-response)))
                  (is (= "Address updated successfully" (:message update-body))))))))))))

(deftest ^:integration delete-address-test
  (testing "DELETE /api/v1/address/delete-address - should delete address"
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
                                       :form-params {:country "Mexico"
                                                     :state "Guanajuato"
                                                     :city "Guanajuato"
                                                     :street "Main Street"
                                                     :postal_code "12345"
                                                     :is_primary false}})
                body (-> response :body (cheshire/parse-string true))]

            (is (= 201 (:status response)))
            (is (= "Address created successfully" (:message body)))))

        (let [recent-id-response (client/get "http://localhost:3001/api/v1/address/get-recent-id-address"
                                             {:accept :json
                                              :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
              recent-id-body (-> recent-id-response :body (cheshire/parse-string true))
              address-id (-> recent-id-body :recent first :id)]
          (is (= 200 (:status recent-id-response)))

          (testing "Delete address with is_primary=false should delete address"
            (let [delete-response (client/delete (str "http://localhost:3001/api/v1/address/delete-address/" address-id)
                                                 {:accept :json
                                                  :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
                  delete-body (-> delete-response :body (cheshire/parse-string true))]

              (is (= 200 (:status delete-response)))
              (is (= "Address deleted successfully" (:message delete-body)))))

          (testing "Delete address with is_primary=true should throw error"
            (let [delete-response (client/delete (str "http://localhost:3001/api/v1/address/delete-address/" (- address-id 1))
                                                 {:accept :json
                                                  :throw-exceptions false
                                                  :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
                  delete-body (-> delete-response :body (cheshire/parse-string true))]

              (is (= 400 (:status delete-response)))
              (is (= "Cannot delete primary address" (:error delete-body)))))

          (testing "Delete address with nonexistent id should throw error"
            (let [delete-response (client/delete (str "http://localhost:3001/api/v1/address/delete-address/" 999999999)
                                                 {:accept :json
                                                  :throw-exceptions false
                                                  :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
                  delete-body (-> delete-response :body (cheshire/parse-string true))]

              (is (= 404 (:status delete-response)))
              (is (= "Address not found" (:error delete-body)))))

          (testing "Delete address with not authorization should throw error"
            (let [delete-response (client/delete (str "http://localhost:3001/api/v1/address/delete-address/" 1)
                                                 {:accept :json
                                                  :throw-exceptions false
                                                  :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
                  delete-body (-> delete-response :body (cheshire/parse-string true))]

              (is (= 403 (:status delete-response)))
              (is (= "Not authorized to delete this address" (:error delete-body))))))))))

(deftest ^:integration set-primary-address-test
  (testing "POST /api/v1/address/set-primary-address/:address_id - should set primary address"
    (test-helper/with-test-database
      (fn []
        (testing "Create initial address with is_primary=false"
          (let [create-response (client/post "http://localhost:3001/api/v1/address/create-address"
                                             {:accept :json
                                              :content-type :json
                                              :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}
                                              :form-params {:country "Mexico"
                                                            :state "Guanajuato"
                                                            :city "Guanajuato"
                                                            :street "Main Street"
                                                            :postal_code "12345"
                                                            :is_primary false}})
                create-body (-> create-response :body (cheshire/parse-string true))]

            (is (= 201 (:status create-response)))
            (is (= "Address created successfully" (:message create-body)))

            (let [recent-id-response (client/get "http://localhost:3001/api/v1/address/get-recent-id-address"
                                                 {:accept :json
                                                  :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
                  recent-id-body (-> recent-id-response :body (cheshire/parse-string true))
                  address-id (-> recent-id-body :recent first :id)]

              (is (= 200 (:status recent-id-response)))

              (testing "Set primary address to recent address-id"
                (let [set-response (client/post (str "http://localhost:3001/api/v1/address/set-primary-address/" address-id)
                                                {:accept :json
                                                 :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
                      set-body (-> set-response :body (cheshire/parse-string true))]

                  (is (= 200 (:status set-response)))
                  (is (= "Primary address updated successfully" (:message set-body)))))

              (testing "Set primary address with not authorization should throw error"
                (let [set-response (client/post (str "http://localhost:3001/api/v1/address/set-primary-address/" 1)
                                                {:accept :json
                                                 :throw-exceptions false
                                                 :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
                      set-body (-> set-response :body (cheshire/parse-string true))]

                  (is (= 403 (:status set-response)))
                  (is (= "Not authorized to modify this address" (:error set-body)))))

              (testing "Set primary address to nonexistent id should throw error"
                (let [set-response (client/post (str "http://localhost:3001/api/v1/address/set-primary-address/" 999999999)
                                                {:accept :json
                                                 :throw-exceptions false
                                                 :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
                      set-body (-> set-response :body (cheshire/parse-string true))]

                  (is (= 404 (:status set-response)))
                  (is (= "Address not found" (:error set-body))))))))))))

(deftest ^:integration get-customer-addresses-test
  (testing "GET /api/v1/address/get-customer-addresses should return customer addresses"
    (test-helper/with-test-database
      (fn []
        (testing "Create initial address with is_primary=true"
          (let [create-response (client/post "http://localhost:3001/api/v1/address/create-address"
                                             {:accept :json
                                              :content-type :json
                                              :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}
                                              :form-params {:country "Mexico"
                                                            :state "Guanajuato"
                                                            :city "Guanajuato"
                                                            :street "Main Street"
                                                            :postal_code "12345"
                                                            :is_primary true}})
                create-body (-> create-response :body (cheshire/parse-string true))]

            (is (= 201 (:status create-response)))
            (is (= "Primary address created successfully" (:message create-body)))

            (testing "Get customer addresses"
              (let [get-response (client/get "http://localhost:3001/api/v1/address/get-customer-addresses"
                                             {:accept :json
                                              :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
                    get-body (-> get-response :body (cheshire/parse-string true))]

                (is (= 200 (:status get-response)))
                (is (= [{:country "Mexico"
                         :state "Guanajuato"
                         :city "Guanajuato"
                         :street "Main Street"
                         :postal_code "12345"
                         :is_primary true}]
                       (:addresses get-body)))))))))))

(deftest ^:integration get-primary-address
  (testing "GET /api/v1/address/get-primary-address should return primary address"
    (test-helper/with-test-database
      (fn []
        (testing "Create initial address with is_primary=true"
          (let [create-response (client/post "http://localhost:3001/api/v1/address/create-address"
                                             {:accept :json
                                              :content-type :json
                                              :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}
                                              :form-params {:country "Mexico"
                                                            :state "Guanajuato"
                                                            :city "Guanajuato"
                                                            :street "Main Street"
                                                            :postal_code "12345"
                                                            :is_primary true}})
                create-body (-> create-response :body (cheshire/parse-string true))]

            (is (= 201 (:status create-response)))
            (is (= "Primary address created successfully" (:message create-body)))

            (testing "Get customer primary address"
              (let [get-response (client/get "http://localhost:3001/api/v1/address/get-primary-address"
                                             {:accept :json
                                              :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
                    get-body (-> get-response :body (cheshire/parse-string true))]

                (is (= 200 (:status get-response)))
                (is (= {:country "Mexico"
                        :state "Guanajuato"
                        :city "Guanajuato"
                        :street "Main Street"
                        :postal_code "12345"
                        :is_primary true}
                       (:address get-body)))))))))))

(deftest ^:integration get-customers-by-location
  (testing "GET /api/v1/address/get-customers-by-location/:country/:state/:city should return customers by location"
    (test-helper/with-test-database
      (fn []
        (let [get-response (client/get "http://localhost:3001/api/v1/address/get-customers-by-location/Mexico/Jalisco/Guadalajara"
                                       {:accept :json
                                        :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
              get-body (-> get-response :body (cheshire/parse-string true))]

          (is (= 200 (:status get-response)))
          (is (= [{:names "María Elena"
                   :first_surname "García"
                   :second_surname "López"
                   :email "maria.garcia@email.com"
                   :active true}]
                 (:customers get-body))))))))

(deftest ^:integration get-customers-by-postal-code
  (testing "GET /api/v1/address/get-customers-by-postal-code/:postal_code should return customers by postal code"
    (test-helper/with-test-database
      (fn []
        (let [get-response (client/get "http://localhost:3001/api/v1/address/get-customers-by-postal-code/76000"
                                       {:accept :json
                                        :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
              get-body (-> get-response :body (cheshire/parse-string true))]

          (is (= 200 (:status get-response)))
          (is (= [{:names "Jorge Luis"
                   :first_surname "Santos"
                   :second_surname "Cervantes"
                   :email "jorge.santos@email.com"
                   :active true}]
                 (:customers get-body))))))))

(deftest ^:integration get-location-statistics-test
  (testing "GET /api/v1/address/get-location-statistics - should return location statistics based on populated data"
    (test-helper/with-test-database
      (fn []
        (testing "Get location statistics with admin authorization"
          (let [response (client/get "http://localhost:3001/api/v1/address/get-location-statistics"
                                     {:accept :json
                                      :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
                body (-> response :body (cheshire/parse-string true))
                statistics (:statistics body)]

            (is (= 200 (:status response)))
            (is (map? statistics))

            (let [top-countries (:top_countries statistics)]
              (is (vector? top-countries))
              (is (= 3 (count top-countries)) "Should return top 3 countries")

              (is (= "Mexico" (:country (first top-countries))))
              (is (= 8 (:customer_count (first top-countries))) "Mexico should have 8 addresses"))

            (let [top-states (:top_states statistics)]
              (is (vector? top-states))
              (is (= 3 (count top-states)) "Should return top 3 states"))

            (let [top-cities (:top_cities statistics)]
              (is (vector? top-cities))
              (is (= 3 (count top-cities)) "Should return top 3 cities"))

            (let [total-customers (apply + (map :customer_count (:top_countries statistics)))]
              (is (>= total-customers 12)
                  (str "Sum of top countries should be at least 12, got: " total-customers)))))

        (testing "Get location statistics without authorization should fail"
          (let [response (client/get "http://localhost:3001/api/v1/address/get-location-statistics"
                                     {:accept :json
                                      :throw-exceptions false})
                body (-> response :body (cheshire/parse-string true))]

            (is (= 401 (:status response)))
            (is (contains? body :error))))))))
