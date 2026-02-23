(ns ecommerce.integration.seller-test
  (:require
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

;; TODO: requires a sales creation flow to set total_sales
;;(deftest ^:integration delete-seller-with-sales-history-test
;;  (testing "DELETE /api/v1/seller/delete-seller/:seller_id - should return 400 when seller has sales history"

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

(deftest ^:integration update-seller-status-success-test
  (testing "PUT /api/v1/seller/update-seller-status/:seller_id - should update seller status successfully"
    (test-helper/with-test-database
      (fn []
        (let [create-response (seller-helpers/post-seller (seller-helpers/seller-data) {:headers (test-helper/auth-headers)})
              create-body     (test-helper/parse-body create-response)
              seller-id       (:id create-body)]
          (is (= 201 (:status create-response)))
          (let [response (seller-helpers/put-seller-status seller-id (seller-helpers/seller-status-data)
                                                           {:headers (test-helper/auth-headers)})
                body     (test-helper/parse-body response)]
            (is (= 200 (:status response)))
            (is (= "Seller status updated successfully" (:message body)))))))))

(deftest ^:integration update-seller-status-not-found-test
  (testing "PUT /api/v1/seller/update-seller-status/:seller_id - should return 404 when seller does not exist"
    (test-helper/with-test-database
      (fn []
        (let [response (seller-helpers/put-seller-status 999999 (seller-helpers/seller-status-data)
                                                         {:headers (test-helper/auth-headers)})
              body     (test-helper/parse-body response)]
          (is (= 404 (:status response)))
          (is (= "Seller not found" (:error body))))))))

(deftest ^:integration update-seller-status-no-auth-test
  (testing "PUT /api/v1/seller/update-seller-status/:seller_id - should return 401 when no auth headers provided"
    (test-helper/with-test-database
      (fn []
        (let [response (seller-helpers/put-seller-status 1 (seller-helpers/seller-status-data))
              body     (test-helper/parse-body response)]
          (is (= 401 (:status response)))
          (is (= "Authentication required" (:error body))))))))

(deftest ^:integration verify-seller-success-test
  (testing "PUT /api/v1/seller/verify-seller/:seller_id - should verify seller successfully"
    (test-helper/with-test-database
      (fn []
        (let [create-response (seller-helpers/post-seller (seller-helpers/seller-data) {:headers (test-helper/auth-headers)})
              create-body     (test-helper/parse-body create-response)
              seller-id       (:id create-body)]
          (is (= 201 (:status create-response)))
          (let [response (seller-helpers/put-verify-seller seller-id {:headers (test-helper/auth-headers)})
                body     (test-helper/parse-body response)]
            (is (= 200 (:status response)))
            (is (= "Seller verified successfully" (:message body)))))))))

(deftest ^:integration verify-seller-already-verified-test
  (testing "PUT /api/v1/seller/verify-seller/:seller_id - should return 400 when seller is already verified"
    (test-helper/with-test-database
      (fn []
        (let [create-response (seller-helpers/post-seller (seller-helpers/seller-data) {:headers (test-helper/auth-headers)})
              create-body     (test-helper/parse-body create-response)
              seller-id       (:id create-body)]
          (is (= 201 (:status create-response)))
          (seller-helpers/put-verify-seller seller-id {:headers (test-helper/auth-headers)})
          (let [response (seller-helpers/put-verify-seller seller-id {:headers (test-helper/auth-headers)})
                body     (test-helper/parse-body response)]
            (is (= 400 (:status response)))
            (is (= "Seller is already verified" (:error body)))))))))

(deftest ^:integration verify-seller-not-found-test
  (testing "PUT /api/v1/seller/verify-seller/:seller_id - should return 404 when seller does not exist"
    (test-helper/with-test-database
      (fn []
        (let [response (seller-helpers/put-verify-seller 999999 {:headers (test-helper/auth-headers)})
              body     (test-helper/parse-body response)]
          (is (= 404 (:status response)))
          (is (= "Seller not found" (:error body))))))))

(deftest ^:integration verify-seller-no-auth-test
  (testing "PUT /api/v1/seller/verify-seller/:seller_id - should return 401 when no auth headers provided"
    (test-helper/with-test-database
      (fn []
        (let [response (seller-helpers/put-verify-seller 1)
              body     (test-helper/parse-body response)]
          (is (= 401 (:status response)))
          (is (= "Authentication required" (:error body))))))))

(deftest ^:integration get-seller-by-id-success-test
  (testing "GET /api/v1/seller/get-seller-by-id/:id - should return seller by id"
    (test-helper/with-test-database
      (fn []
        (let [create-response (seller-helpers/post-seller (seller-helpers/seller-data) {:headers (test-helper/auth-headers)})
              create-body     (test-helper/parse-body create-response)
              seller-id       (:id create-body)]
          (is (= 201 (:status create-response)))
          (let [response (seller-helpers/get-seller-by-id seller-id {:headers (test-helper/auth-headers)})
                body     (test-helper/parse-body response)]
            (is (= 200 (:status response)))
            (is (contains? body :seller))
            (is (= seller-id (get-in body [:seller :id])))))))))

(deftest ^:integration get-seller-by-id-not-found-test
  (testing "GET /api/v1/seller/get-seller-by-id/:id - should return 404 when seller does not exist"
    (test-helper/with-test-database
      (fn []
        (let [response (seller-helpers/get-seller-by-id 999999 {:headers (test-helper/auth-headers)})
              body     (test-helper/parse-body response)]
          (is (= 404 (:status response)))
          (is (= "Seller not found" (:error body))))))))

(deftest ^:integration get-seller-by-id-no-auth-test
  (testing "GET /api/v1/seller/get-seller-by-id/:id - should return 401 when no auth headers provided"
    (test-helper/with-test-database
      (fn []
        (let [response (seller-helpers/get-seller-by-id 1)
              body     (test-helper/parse-body response)]
          (is (= 401 (:status response)))
          (is (= "Authentication required" (:error body))))))))

(deftest ^:integration get-sellers-by-country-stats-success-test
  (testing "GET /api/v1/seller/get-sellers-by-country-stats - should return sellers grouped by country"
    (test-helper/with-test-database
      (fn []
        (let [_       (seller-helpers/post-seller (seller-helpers/seller-data)
                                                  {:headers (test-helper/auth-headers)})
              response (seller-helpers/get-sellers-by-country-stats
                        {:headers (test-helper/auth-headers)})
              body     (test-helper/parse-body response)]
          (is (= 200 (:status response)))
          (is (contains? body :sellers_by_country))
          (is (seq (:sellers_by_country body))))))))

(deftest ^:integration get-sellers-by-country-stats-no-auth-test
  (testing "GET /api/v1/seller/get-sellers-by-country-stats - should return 401 when no auth headers provided"
    (test-helper/with-test-database
      (fn []
        (let [response (seller-helpers/get-sellers-by-country-stats)
              body     (test-helper/parse-body response)]
          (is (= 401 (:status response)))
          (is (= "Authentication required" (:error body))))))))

(deftest ^:integration get-sellers-by-status-success-test
  (testing "GET /api/v1/seller/get-sellers-by-status/:status - should return sellers by status"
    (test-helper/with-test-database
      (fn []
        (let [_ (seller-helpers/post-seller (seller-helpers/seller-data) {:headers (test-helper/auth-headers)})

              response (seller-helpers/get-sellers-by-status "PENDING" {:headers (test-helper/auth-headers)})
              body     (test-helper/parse-body response)]
          (is (= 200 (:status response)))
          (is (= "PENDING" (:status body)))
          (is (contains? body :sellers))
          (is (seq (:sellers body))))))))

(deftest ^:integration get-sellers-by-status-not-found-test
  (testing "GET /api/v1/seller/get-sellers-by-status/:status - should return 404 when no sellers found"
    (test-helper/with-test-database
      (fn []
        (let [response (seller-helpers/get-sellers-by-status "unknown" {:headers (test-helper/auth-headers)})
              body     (test-helper/parse-body response)]
          (is (= 404 (:status response)))
          (is (= "No sellers found with this status" (:error body))))))))

(deftest ^:integration get-sellers-by-status-no-auth-test
  (testing "GET /api/v1/seller/get-sellers-by-status/:status - should return 401 when no auth headers provided"
    (test-helper/with-test-database
      (fn []
        (let [response (seller-helpers/get-sellers-by-status "PENDING")
              body     (test-helper/parse-body response)]
          (is (= 401 (:status response)))
          (is (= "Authentication required" (:error body))))))))

(deftest ^:integration get-top-sellers-success-test
  (testing "GET /api/v1/seller/get-top-sellers/:limit - should return top sellers"
    (test-helper/with-test-database
      (fn []
        (let [_ (seller-helpers/post-seller (seller-helpers/seller-data) {:headers (test-helper/auth-headers)})
              response (seller-helpers/get-top-sellers 2 {:headers (test-helper/auth-headers)})
              body     (test-helper/parse-body response)]
          (is (= 200 (:status response)))
          (is (contains? body :top_sellers)))))))

(deftest ^:integration get-top-sellers-no-auth-test
  (testing "GET /api/v1/seller/get-top-sellers/:limit - should return 401 when no auth headers provided"
    (test-helper/with-test-database
      (fn []
        (let [response (seller-helpers/get-top-sellers 2)
              body     (test-helper/parse-body response)]
          (is (= 401 (:status response)))
          (is (= "Authentication required" (:error body))))))))

(deftest ^:integration get-unverified-sellers-with-results-test
  (testing "GET /api/v1/seller/get-unverified-sellers - should return unverified sellers"
    (test-helper/with-test-database
      (fn []
        (let [_ (seller-helpers/post-seller (seller-helpers/seller-data) {:headers (test-helper/auth-headers)})
              response (seller-helpers/get-unverified-sellers {:headers (test-helper/auth-headers)})
              body     (test-helper/parse-body response)]
          (is (= 200 (:status response)))
          (is (contains? body :unverified_sellers))
          (is (seq (:unverified_sellers body))))))))

;; TODO: needs database empty to work(right now its filled up with migrations)
;;(deftest ^:integration get-unverified-sellers-empty-test
;;  (testing "GET /api/v1/seller/get-unverified-sellers - should return 404 when no unverified sellers exist"
;;    (test-helper/with-test-database
;;      (fn []
;;        (let [create-response (seller-helpers/post-seller (seller-helpers/seller-data) {:headers (test-helper/auth-headers)})
;;              seller-id       (:id (test-helper/parse-body create-response))]
;;          (seller-helpers/put-verify-seller seller-id {:headers (test-helper/auth-headers)})
;;          (let [response (seller-helpers/get-unverified-sellers {:headers (test-helper/auth-headers)})
;;                body     (test-helper/parse-body response)]
;;            (is (= 404 (:status response)))
;;            (is (= "No sellers with sales found" (:error body)))))))))

(deftest ^:integration get-unverified-sellers-no-auth-test
  (testing "GET /api/v1/seller/get-unverified-sellers - should return 401 when no auth headers provided"
    (test-helper/with-test-database
      (fn []
        (let [response (seller-helpers/get-unverified-sellers)
              body     (test-helper/parse-body response)]
          (is (= 401 (:status response)))
          (is (= "Authentication required" (:error body))))))))
