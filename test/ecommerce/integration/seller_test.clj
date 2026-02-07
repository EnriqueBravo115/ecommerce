(ns ecommerce.integration.seller-test
  (:require
   [cheshire.core :as cheshire]
   [clj-http.client :as client]
   [ecommerce.utils.jwt :as jwt]
   [clojure.test :refer [deftest is testing]]
   [ecommerce.integration.integration-test-helpers :as test-helper]))

(deftest ^:integration create-seller-test
  (testing "POST /api/v1/seller/create-seller - should create seller"
    (test-helper/with-test-database
      (fn []
        (testing "Create seller with valid data"
          (let [seller-data {:business_name "Tech Solutions MX"
                             :legal_name "Soluciones Tecnológicas S.A. de C.V."
                             :tax_id "TEC123456789"
                             :email "ventas@techsolutions.mx"
                             :phone "+525512345678"
                             :country "Mexico"
                             :state "Ciudad de México"
                             :city "CDMX"
                             :address "Av. Reforma 123"
                             :postal_code "06600"
                             :website "https://techsolutions.mx"
                             :status "PENDING"
                             :verified false
                             :commission_rate 15.5
                             :password "password"
                             :bank_account "0123456789"
                             :bank_name "Banco Nacional"}
                response (client/post "http://localhost:3001/api/v1/seller/create-seller"
                                      {:accept :json
                                       :content-type :json
                                       :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}
                                       :form-params seller-data})
                body (-> response :body (cheshire/parse-string true))]

            (is (= 201 (:status response)))
            (is (= "Seller created successfully" (:message body)))))

        (testing "Create seller with duplicate email should return conflict"
          (let [seller-data {:business_name "Another Tech Solutions"
                             :legal_name "Otra Solución S.A. de C.V."
                             :tax_id "TEC987654321"
                             :email "ventas@techsolutions.mx"
                             :phone "+525598765432"
                             :country "Mexico"
                             :state "Ciudad de México"
                             :city "CDMX"
                             :address "Av. Insurgentes 456"
                             :postal_code "06610"
                             :website "https://anothertech.mx"
                             :status "PENDING"
                             :verified false
                             :commission_rate 12.0
                             :password "anotherpassword"
                             :bank_account "9876543210"
                             :bank_name "Banco del Norte"}
                response (client/post "http://localhost:3001/api/v1/seller/create-seller"
                                      {:accept :json
                                       :content-type :json
                                       :throw-exceptions false
                                       :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}
                                       :form-params seller-data})
                body (-> response :body (cheshire/parse-string true))]

            (is (= 409 (:status response)))
            (is (= "Seller with this email already exists" (:error body)))))

        (testing "Create seller without authorization should fail"
          (let [seller-data {:business_name "Unauthorized Seller"
                             :legal_name "Vendedor No Autorizado S.A."
                             :tax_id "UNA001234567"
                             :email "unauthorized@example.com"
                             :phone "+525511111111"
                             :country "Mexico"
                             :state "Ciudad de México"
                             :city "CDMX"
                             :address "Some Address"
                             :postal_code "00000"
                             :website "https://unauthorized.mx"
                             :status "PENDING"
                             :verified false
                             :commission_rate 10.0
                             :password "password"
                             :bank_account "1111111111"
                             :bank_name "Some Bank"}
                response (client/post "http://localhost:3001/api/v1/seller/create-seller"
                                      {:accept :json
                                       :content-type :json
                                       :throw-exceptions false
                                       :form-params seller-data})
                body (-> response :body (cheshire/parse-string true))]

            (is (= 401 (:status response)))
            (is (contains? body :error))))))))

(deftest ^:integration update-seller-location-test
  (testing "PUT /api/v1/seller/update-seller-location/:seller_id - should update seller location"
    (test-helper/with-test-database
      (fn []
        (let [seller-id 1]
          (testing "Update seller location as ADMIN"
            (let [location-data {:state "Jalisco"
                                 :city "Guadalajara"
                                 :address "Av. Chapultepec 789"
                                 :postal_code "44100"}
                  response (client/put (str "http://localhost:3001/api/v1/seller/update-seller-location/" seller-id)
                                       {:accept :json
                                        :content-type :json
                                        :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}
                                        :form-params location-data})
                  body (-> response :body (cheshire/parse-string true))]

              (is (= 200 (:status response)))
              (is (= "Seller updated successfully" (:message body)))))

          (testing "Update seller location as SELLER (owner)"
            (let [seller-token (jwt/generate-test-token {:id seller-id
                                                         :email "seller1@example.com"
                                                         :roles "SELLER"})
                  location-data {:state "Nuevo León"
                                 :city "Monterrey"
                                 :address "Av. Constitución 123"
                                 :postal_code "64000"}
                  response (client/put (str "http://localhost:3001/api/v1/seller/update-seller-location/" seller-id)
                                       {:accept :json
                                        :content-type :json
                                        :headers {"Authorization" (str "Bearer " seller-token)}
                                        :form-params location-data})
                  body (-> response :body (cheshire/parse-string true))]

              (is (= 200 (:status response)))
              (is (= "Seller updated successfully" (:message body)))))

          (testing "Update seller location with partial data"
            (let [location-data {:city "Querétaro"
                                 :postal_code "76000"}
                  response (client/put (str "http://localhost:3001/api/v1/seller/update-seller-location/" seller-id)
                                       {:accept :json
                                        :content-type :json
                                        :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}
                                        :form-params location-data})
                  body (-> response :body (cheshire/parse-string true))]

              (is (= 200 (:status response)))
              (is (= "Seller updated successfully" (:message body)))))

          (testing "Update nonexistent seller should return 404"
            (let [location-data {:state "CDMX"
                                 :city "Ciudad de México"
                                 :address "Av. Reforma"
                                 :postal_code "06600"}
                  response (client/put "http://localhost:3001/api/v1/seller/update-seller-location/999999"
                                       {:accept :json
                                        :content-type :json
                                        :throw-exceptions false
                                        :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}
                                        :form-params location-data})
                  body (-> response :body (cheshire/parse-string true))]

              (is (= 404 (:status response)))
              (is (= "Seller not found" (:error body)))))

          (testing "Update seller location as different SELLER (not owner) should return 403"
            (let [other-seller-token (jwt/generate-test-token {:id 2
                                                               :email "other@example.com"
                                                               :roles "SELLER"})
                  location-data {:state "Baja California"
                                 :city "Tijuana"
                                 :address "Calle 5"
                                 :postal_code "22000"}
                  response (client/put (str "http://localhost:3001/api/v1/seller/update-seller-location/" seller-id)
                                       {:accept :json
                                        :content-type :json
                                        :throw-exceptions false
                                        :headers {"Authorization" (str "Bearer " other-seller-token)}
                                        :form-params location-data})
                  body (-> response :body (cheshire/parse-string true))]

              (is (= 403 (:status response)))
              (is (= "Not authorized to update this seller" (:error body)))))

          (testing "Update seller location as CUSTOMER should return 403"
            (let [customer-token (jwt/generate-test-token {:id 3
                                                           :email "customer@example.com"
                                                           :roles "CUSTOMER"})
                  location-data {:state "Sonora"
                                 :city "Hermosillo"
                                 :address "Blvd. Kino"
                                 :postal_code "83000"}
                  response (client/put (str "http://localhost:3001/api/v1/seller/update-seller-location/" seller-id)
                                       {:accept :json
                                        :content-type :json
                                        :throw-exceptions false
                                        :headers {"Authorization" (str "Bearer " customer-token)}
                                        :form-params location-data})
                  body (-> response :body (cheshire/parse-string true))]

              (is (= 403 (:status response)))
              (is (= "Not authorized to update this seller" (:error body)))))

          (testing "Update seller location without authorization should return 401"
            (let [location-data {:state "Veracruz"
                                 :city "Veracruz"
                                 :address "Boulevard"
                                 :postal_code "91000"}
                  response (client/put (str "http://localhost:3001/api/v1/seller/update-seller-location/" seller-id)
                                       {:accept :json
                                        :content-type :json
                                        :throw-exceptions false
                                        :form-params location-data})
                  body (-> response :body (cheshire/parse-string true))]

              (is (= 401 (:status response)))
              (is (contains? body :error))))

          (testing "Update seller location with invalid seller_id format"
            (let [location-data {:state "Puebla"
                                 :city "Puebla"
                                 :address "5 de Mayo"
                                 :postal_code "72000"}
                  response (client/put "http://localhost:3001/api/v1/seller/update-seller-location/not-a-number"
                                       {:accept :json
                                        :content-type :json
                                        :throw-exceptions false
                                        :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}
                                        :form-params location-data})
                  body (-> response :body (cheshire/parse-string true))]

              (is (or (= 400 (:status response))
                      (= 500 (:status response)))
                  "Should return error for invalid ID format"))))))))
