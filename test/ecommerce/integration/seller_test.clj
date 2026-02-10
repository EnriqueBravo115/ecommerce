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
        (testing "Update seller location as ADMIN"
          (let [location-data {:state "Jalisco"
                               :city "Guadalajara"
                               :address "Av. Chapultepec 789"
                               :postal_code "44100"}
                response (client/put "http://localhost:3001/api/v1/seller/update-seller-location/1"
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

        (testing "Update seller location without authorization should return 401"
          (let [location-data {:state "Veracruz"
                               :city "Veracruz"
                               :address "Boulevard"
                               :postal_code "91000"}
                response (client/put "http://localhost:3001/api/v1/seller/update-seller-location/1"
                                     {:accept :json
                                      :content-type :json
                                      :throw-exceptions false
                                      :form-params location-data})
                body (-> response :body (cheshire/parse-string true))]

            (is (= 401 (:status response)))
            (is (contains? body :error))))))))

(deftest ^:integration delete-seller-test
  (testing "DELETE /api/v1/seller/delete-seller/:seller_id - should delete seller"
    (test-helper/with-test-database
      (fn []
        (testing "Delete seller successfully"
          (let [response (client/delete "http://localhost:3001/api/v1/seller/delete-seller/2"
                                        {:accept :json
                                         :content-type :json
                                         :throw-exceptions false
                                         :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
                body (-> response :body (cheshire/parse-string true))]

            (is (= 200 (:status response)))
            (is (= "Seller deleted successfully" (:message body)))))

        (testing "Delete nonexistent seller should return 404"
          (let [response (client/delete "http://localhost:3001/api/v1/seller/delete-seller/999999"
                                        {:accept :json
                                         :content-type :json
                                         :throw-exceptions false
                                         :headers {"Authorization" (str "Bearer " (jwt/generate-admin-test-token))}})
                body (-> response :body (cheshire/parse-string true))]

            (is (= 404 (:status response)))
            (is (= "Seller not found" (:error body)))))

        (testing "Delete seller without authorization should return 401"
          (let [response (client/delete "http://localhost:3001/api/v1/seller/delete-seller/1"
                                        {:accept :json
                                         :content-type :json
                                         :throw-exceptions false})]
            (is (= 401 (:status response)))))))))

(deftest ^:integration update-seller-status-test
  (testing "PUT /api/v1/seller/update-seller-status/:seller_id - should update seller status"
    (test-helper/with-test-database
      (fn []
        (testing "Update seller status as ADMIN"
          (let [status-data {:status "active"}
                response (client/put "http://localhost:3001/api/v1/seller/update-seller-status/2"
                                     {:accept :json
                                      :content-type :json
                                      :headers {"Authorization"
                                                (str "Bearer " (jwt/generate-admin-test-token))}
                                      :form-params status-data})
                body (-> response :body (cheshire/parse-string true))]

            (is (= 200 (:status response)))
            (is (= "Seller status updated successfully" (:message body)))))

        (testing "Update nonexistent seller should return 404"
          (let [status-data {:status "inactive"}
                response (client/put "http://localhost:3001/api/v1/seller/update-seller-status/999999"
                                     {:accept :json
                                      :content-type :json
                                      :throw-exceptions false
                                      :headers {"Authorization"
                                                (str "Bearer " (jwt/generate-admin-test-token))}
                                      :form-params status-data})
                body (-> response :body (cheshire/parse-string true))]

            (is (= 404 (:status response)))
            (is (= "Seller not found" (:error body)))))

        (testing "Update seller status without authorization should return 401"
          (let [status-data {:status "active"}
                response (client/put "http://localhost:3001/api/v1/seller/update-seller-status/1"
                                     {:accept :json
                                      :content-type :json
                                      :throw-exceptions false
                                      :form-params status-data})
                body (-> response :body (cheshire/parse-string true))]

            (is (= 401 (:status response)))
            (is (contains? body :error))))))))

(deftest ^:integration verify-seller-test
  (testing "PUT /api/v1/seller/verify-seller/:seller_id - should verify seller"
    (test-helper/with-test-database
      (fn []
        (testing "Verify seller successfully as ADMIN"
          (let [response (client/put "http://localhost:3001/api/v1/seller/verify-seller/2"
                                     {:accept :json
                                      :content-type :json
                                      :headers {"Authorization"
                                                (str "Bearer " (jwt/generate-admin-test-token))}})
                body (-> response :body (cheshire/parse-string true))]

            (is (= 200 (:status response)))
            (is (= "Seller verified successfully" (:message body)))))

        (testing "Verify already verified seller should return 400"
          (let [response (client/put "http://localhost:3001/api/v1/seller/verify-seller/1"
                                     {:accept :json
                                      :content-type :json
                                      :throw-exceptions false
                                      :headers {"Authorization"
                                                (str "Bearer " (jwt/generate-admin-test-token))}})
                body (-> response :body (cheshire/parse-string true))]

            (is (= 400 (:status response)))
            (is (= "Seller is already verified" (:error body)))))

        (testing "Verify nonexistent seller should return 404"
          (let [response (client/put "http://localhost:3001/api/v1/seller/verify-seller/999999"
                                     {:accept :json
                                      :content-type :json
                                      :throw-exceptions false
                                      :headers {"Authorization"
                                                (str "Bearer " (jwt/generate-admin-test-token))}})
                body (-> response :body (cheshire/parse-string true))]

            (is (= 404 (:status response)))
            (is (= "Seller not found" (:error body)))))

        (testing "Verify seller without authorization should return 401"
          (let [response (client/put "http://localhost:3001/api/v1/seller/verify-seller/1"
                                     {:accept :json
                                      :content-type :json
                                      :throw-exceptions false})
                body (-> response :body (cheshire/parse-string true))]

            (is (= 401 (:status response)))
            (is (contains? body :error))))))))

(deftest ^:integration get-seller-by-id-test
  (testing "GET /api/v1/seller/get-seller-by-id/:id - should get seller by id"
    (test-helper/with-test-database
      (fn []
        (testing "Get seller by id as ADMIN"
          (let [response (client/get
                          "http://localhost:3001/api/v1/seller/get-seller-by-id/1"
                          {:accept :json
                           :throw-exceptions false
                           :headers {"Authorization"
                                     (str "Bearer " (jwt/generate-admin-test-token))}})
                body (-> response :body (cheshire/parse-string true))]

            (is (= 200 (:status response)))
            (is (contains? body :seller))
            (is (= 1 (get-in body [:seller :id])))))

        (testing "Get nonexistent seller should return 404 from handler"
          (let [response (client/get
                          "http://localhost:3001/api/v1/seller/get-seller-by-id/999999"
                          {:accept :json
                           :throw-exceptions false
                           :headers {"Authorization"
                                     (str "Bearer " (jwt/generate-admin-test-token))}})
                body (-> response :body (cheshire/parse-string true))]

            (is (= 404 (:status response)))
            (is (= "Seller not found" (:error body)))))

        (testing "Get seller without ADMIN role should return 403"
          (let [response (client/get
                          "http://localhost:3001/api/v1/seller/get-seller-by-id/1"
                          {:accept :json
                           :throw-exceptions false})
                body (-> response :body (cheshire/parse-string true))]

            (is (= 401 (:status response)))
            (is (= "Authentication required" (:error body)))))))))

(deftest ^:integration get-seller-by-country-stats-test
  (testing "GET /api/v1/seller/get-sellers-by-country-stats - should return sellers grouped by country"
    (test-helper/with-test-database
      (fn []
        (let [response (client/get "http://localhost:3001/api/v1/seller/get-sellers-by-country-stats"
                                   {:accept :json
                                    :throw-exceptions false
                                    :headers {"Authorization"
                                              (str "Bearer " (jwt/generate-admin-test-token))}})
              body (-> response :body (cheshire/parse-string true))]

          (is (= 200 (:status response)))
          (is (contains? body :sellers_by_country))
          (is (seq (:sellers_by_country body))))))))

(deftest ^:integration get-sellers-by-status-test
  (testing "GET /api/v1/seller/status/:status - should return sellers by status"
    (test-helper/with-test-database
      (fn []
        (testing "Get sellers with existing status"
          (let [response (client/get "http://localhost:3001/api/v1/seller/get-sellers-by-status/active"
                                     {:accept :json
                                      :headers {"Authorization"
                                                (str "Bearer " (jwt/generate-admin-test-token))}})
                body (-> response :body (cheshire/parse-string true))]

            (is (= 200 (:status response)))
            (is (= "active" (:status body)))
            (is (contains? body :sellers))
            (is (seq (:sellers body)))))

        (testing "Get sellers with nonexistent status should return 404"
          (let [response (client/get "http://localhost:3001/api/v1/seller/get-sellers-by-status/unknown"
                                     {:accept :json
                                      :throw-exceptions false
                                      :headers {"Authorization"
                                                (str "Bearer " (jwt/generate-admin-test-token))}})
                body (-> response :body (cheshire/parse-string true))]

            (is (= 404 (:status response)))
            (is (= "No sellers found with this status" (:error body)))))))))

(deftest ^:integration get-top-sellers-test
  (testing "GET /api/v1/seller/top/:limit - should return top sellers"
    (test-helper/with-test-database
      (fn []
        (testing "Get top sellers with default data"
          (let [response (client/get "http://localhost:3001/api/v1/seller/get-top-sellers/2"
                                     {:accept :json
                                      :throw-exceptions false
                                      :headers {"Authorization"
                                                (str "Bearer " (jwt/generate-admin-test-token))}})
                body (-> response :body (cheshire/parse-string true))]

            (is (= 200 (:status response)))
            (is (contains? body :top_sellers))
            (is (seq (:top_sellers body)))))))))

(deftest ^:integration get-unverified-sellers-test
  (testing "GET /api/v1/seller/unverified - should return unverified sellers"
    (test-helper/with-test-database
      (fn []
        (let [response (client/get "http://localhost:3001/api/v1/seller/get-unverified-sellers"
                                   {:accept :json
                                    :headers {"Authorization"
                                              (str "Bearer " (jwt/generate-admin-test-token))}})
              body (-> response :body (cheshire/parse-string true))]

          (is (= 200 (:status response)))

          (if (contains? body :unverified_sellers)
            (is (seq (:unverified_sellers body)))
            (is (= "All sellers are verified" (:message body)))))))))
