(ns ecommerce.handlers.seller-handler
  (:require
   [ecommerce.queries.seller-queries :as queries]
   [ecommerce.utils.jwt :as jwt]
   [ecommerce.utils.password :as password]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]))

(def ^:private json-headers {"Content-Type" "application/json"})

(defn- build-response [status body]
  {:status status :headers json-headers :body body})

;; TODO: need validation in required fields
(defn create-seller [request]
  (let [seller-data (:body request)
        ds (:datasource request)
        email (:email seller-data)
        existing-seller (jdbc/execute-one! ds
                                           (queries/get-seller-by-email email)
                                           {:builder-fn rs/as-unqualified-maps})]

    (cond
      existing-seller
      (build-response 409 {:error "Seller with this email already exists"})

      :else
      (let [password-encoded (password/encode (:password seller-data))]
        (jdbc/execute! ds
                       (queries/create-seller
                        (assoc seller-data :password password-encoded)))
        (build-response 201 {:message "Seller created successfully"})))))

;; TODO: need validation in required fields
;; TODO: id should match when seller change location
(defn update-seller-location [request]
  (let [seller-id (Long/parseLong (get-in request [:params :seller_id]))
        seller-data (:body request)
        ds (:datasource request)
        existing-seller (jdbc/execute-one! ds
                                           (queries/get-seller-by-id seller-id)
                                           {:builder-fn rs/as-unqualified-maps})]

    (cond
      (nil? existing-seller)
      (build-response 404 {:error "Seller not found"})

      :else
      (do
        (jdbc/execute-one! ds
                           (queries/update-seller-location seller-id seller-data))
        (build-response 200 {:message "Seller updated successfully"})))))

;; needs nullpointer validation
(defn delete-seller [request]
  (let [seller-id (Long/parseLong (get-in request [:params :seller_id]))
        ds (:datasource request)
        existing-seller (jdbc/execute-one! ds
                                           (queries/get-seller-by-id seller-id)
                                           {:builder-fn rs/as-unqualified-maps})]
    (cond
      (nil? existing-seller)
      (build-response 404 {:error "Seller not found"})

      (> (:total_sales existing-seller 0) 0)
      (build-response 400 {:error "Cannot delete seller with sales history"})

      :else
      (do
        (jdbc/execute! ds
                       (queries/delete-seller seller-id))
        (build-response 200 {:message "Seller deleted successfully"})))))

(defn update-seller-status [request]
  (let [seller-id (Long/parseLong (get-in request [:params :seller_id]))
        status (get-in request [:body :status])
        ds (:datasource request)
        existing-seller (jdbc/execute-one! ds
                                           (queries/get-seller-by-id seller-id)
                                           {:builder-fn rs/as-unqualified-maps})]

    (cond
      (nil? existing-seller)
      (build-response 404 {:error "Seller not found"})

      :else
      (do
        (jdbc/execute! ds
                       (queries/update-seller-status seller-id status))
        (build-response 200 {:message "Seller status updated successfully"})))))

(defn verify-seller [request]
  (let [seller-id (Long/parseLong (get-in request [:params :seller_id]))
        ds (:datasource request)
        existing-seller (jdbc/execute-one! ds
                                           (queries/get-seller-by-id seller-id)
                                           {:builder-fn rs/as-unqualified-maps})]

    (cond
      (nil? existing-seller)
      (build-response 404 {:error "Seller not found"})

      (:verified existing-seller)
      (build-response 400 {:error "Seller is already verified"})

      :else
      (do
        (jdbc/execute! ds
                       (queries/verify-seller seller-id))
        (build-response 200 {:message "Seller verified successfully"})))))

(defn get-seller-by-id [request]
  (let [seller-id (Long/parseLong (get-in request [:params :id]))
        ds (:datasource request)
        seller (jdbc/execute-one! ds
                                  (queries/get-seller-by-id seller-id)
                                  {:builder-fn rs/as-unqualified-maps})]

    (cond
      (nil? seller)
      (build-response 404 {:error "Seller not found"})

      (not (jwt/has-any-role? request "ADMIN"))
      (build-response 403 {:error "Not authorized to view this seller"})

      :else
      (build-response 200 {:seller seller}))))

(defn get-seller-by-country-stats [request]
  (let [ds (:datasource request)
        result (jdbc/execute! ds
                              (queries/get-sellers-by-country)
                              {:builder-fn rs/as-unqualified-maps})]

    (if (seq result)
      (build-response 200 {:sellers_by_country result})
      (build-response 404 {:error "No sellers found"}))))

(defn get-sellers-by-status [request]
  (let [status (get-in request [:params :status])
        ds (:datasource request)
        result (jdbc/execute! ds
                              (queries/get-sellers-by-status status)
                              {:builder-fn rs/as-unqualified-maps})]

    (if (seq result)
      (build-response 200 {:sellers result :status status})
      (build-response 404 {:error "No sellers found with this status"}))))

(defn get-top-sellers [request]
  (let [limit (Long/parseLong (get-in request [:params :limit] "10"))
        ds (:datasource request)
        result (jdbc/execute! ds
                              (queries/get-top-sellers-by-sales limit)
                              {:builder-fn rs/as-unqualified-maps})]

    (if (seq result)
      (build-response 200 {:top_sellers result})
      (build-response 404 {:error "No sellers with sales found"}))))

(defn get-unverified-sellers [request]
  (let [ds (:datasource request)
        result (jdbc/execute! ds
                              (queries/get-unverified-sellers)
                              {:builder-fn rs/as-unqualified-maps})]

    (if (seq result)
      (build-response 200 {:unverified_sellers result})
      (build-response 200 {:message "All sellers are verified"}))))
