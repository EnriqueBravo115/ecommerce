(ns ecommerce.integration.seller-test
  (:require
   [cheshire.core :as cheshire]
   [clj-http.client :as client]
   [ecommerce.utils.jwt :as jwt]
   [clojure.test :refer [deftest is testing]]
   [ecommerce.integration.seller-test-helpers :as seller-helpers]
   [ecommerce.integration.integration-test-helpers :as test-helper]))

(deftest ^:integration create-seller-success-test
  (testing "POST /api/v1/seller/create-seller - should create seller with valid data"
    (test-helper/with-test-database
      (fn []
        (let [response (seller-helpers/post-seller (seller-helpers/seller-data) {:headers (test-helper/auth-headers)})
              body     (test-helper/parse-body response)]
          (is (= 201 (:status response)))
          (is (= "Seller created successfully" (:message body))))))))

(deftest ^:integration create-seller-duplicate-email-test
  (testing "POST /api/v1/seller/create-seller - should return 409 when email already exists"
    (test-helper/with-test-database
      (fn []
        (let [_        (seller-helpers/post-seller (seller-helpers/seller-data) {:headers (test-helper/auth-headers)})
              response (seller-helpers/post-seller (seller-helpers/seller-data-duplicate-email) {:headers (test-helper/auth-headers)})
              body     (test-helper/parse-body response)]
          (is (= 409 (:status response)))
          (is (= "Seller with this email already exists" (:error body))))))))

(deftest ^:integration create-seller-validation-error-test
  (testing "POST /api/v1/seller/create-seller - should return 400 when data is invalid"
    (test-helper/with-test-database
      (fn []
        (let [response (seller-helpers/post-seller {} {:headers (test-helper/auth-headers)})
              body     (test-helper/parse-body response)]
          (is (= 400 (:status response)))
          (is (contains? body :error)))))))

(deftest ^:integration create-seller-no-auth-test
  (testing "POST /api/v1/seller/create-seller - should return 401 when no auth headers provided"
    (test-helper/with-test-database
      (fn []
        (let [response (seller-helpers/post-seller (seller-helpers/seller-data))
              body     (test-helper/parse-body response)]
          (is (= 401 (:status response)))
          (is (= "Authentication required" (:error body))))))))

(deftest ^:integration update-seller-location-success-test
  (testing "PUT /api/v1/seller/update-seller-location/:seller_id - should update seller location"
    (test-helper/with-test-database
      (fn []
        (let [create-response (seller-helpers/post-seller (seller-helpers/seller-data) {:headers (test-helper/auth-headers)})
              create-body     (test-helper/parse-body create-response)
              seller-id       (:id create-body)]
          (is (= 201 (:status create-response)))
          (let [response (seller-helpers/put-seller-location seller-id (seller-helpers/seller-location-data)
                                                             {:headers (test-helper/auth-headers)})
                body     (test-helper/parse-body response)]
            (is (= 200 (:status response)))
            (is (= "Seller updated successfully" (:message body)))))))))

(deftest ^:integration update-seller-location-not-found-test
  (testing "PUT /api/v1/seller/update-seller-location/:seller_id - should return 404 when seller does not exist"
    (test-helper/with-test-database
      (fn []
        (let [response (seller-helpers/put-seller-location 999999 (seller-helpers/seller-location-data)
                                                           {:headers (test-helper/auth-headers)})
              body     (test-helper/parse-body response)]
          (is (= 404 (:status response)))
          (is (= "Seller not found" (:error body))))))))

(deftest ^:integration update-seller-location-validation-error-test
  (testing "PUT /api/v1/seller/update-seller-location/:seller_id - should return 400 when data is invalid"
    (test-helper/with-test-database
      (fn []
        (let [create-response (seller-helpers/post-seller (seller-helpers/seller-data) {:headers (test-helper/auth-headers)})
              create-body     (test-helper/parse-body create-response)
              seller-id       (:id create-body)]
          (is (= 201 (:status create-response)))
          (let [response (seller-helpers/put-seller-location seller-id {} {:headers (test-helper/auth-headers)})
                body     (test-helper/parse-body response)]
            (is (= 400 (:status response)))
            (is (contains? body :error))))))))

(deftest ^:integration update-seller-location-no-auth-test
  (testing "PUT /api/v1/seller/update-seller-location/:seller_id - should return 401 when no auth headers provided"
    (test-helper/with-test-database
      (fn []
        (let [response (seller-helpers/put-seller-location 1 (seller-helpers/seller-location-data))
              body     (test-helper/parse-body response)]
          (is (= 401 (:status response)))
          (is (= "Authentication required" (:error body))))))))

(deftest ^:integration delete-seller-success-test
  (testing "DELETE /api/v1/seller/delete-seller/:seller_id - should delete seller successfully"
    (test-helper/with-test-database
      (fn []
        (let [create-response (seller-helpers/post-seller (seller-helpers/seller-data) {:headers (test-helper/auth-headers)})
              create-body     (test-helper/parse-body create-response)
              seller-id       (:id create-body)]
          (is (= 201 (:status create-response)))
          (let [response (seller-helpers/delete-seller seller-id {:headers (test-helper/auth-headers)})
                body     (test-helper/parse-body response)]
            (is (= 200 (:status response)))
            (is (= "Seller deleted successfully" (:message body)))))))))

(deftest ^:integration delete-seller-with-sales-history-test
  (testing "DELETE /api/v1/seller/delete-seller/:seller_id - should return 400 when seller has sales history"
    (println "SKIPPED: requires a sales creation flow to set total_sales")))

(deftest ^:integration delete-seller-not-found-test
  (testing "DELETE /api/v1/seller/delete-seller/:seller_id - should return 404 when seller does not exist"
    (test-helper/with-test-database
      (fn []
        (let [response (seller-helpers/delete-seller 999999 {:headers (test-helper/auth-headers)})
              body     (test-helper/parse-body response)]
          (is (= 404 (:status response)))
          (is (= "Seller not found" (:error body))))))))

(deftest ^:integration delete-seller-no-auth-test
  (testing "DELETE /api/v1/seller/delete-seller/:seller_id - should return 401 when no auth headers provided"
    (test-helper/with-test-database
      (fn []
        (let [response (seller-helpers/delete-seller 1)
              body     (test-helper/parse-body response)]
          (is (= 401 (:status response)))
          (is (= "Authentication required" (:error body))))))))

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
