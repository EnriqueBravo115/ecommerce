(ns ecommerce.handlers.address-handler
  (:require
   [buddy.auth :refer [authenticated?]]
   [ecommerce.queries.address-queries :as queries]
   [ecommerce.utils.jwt :as jwt]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]))

(def ^:private json-headers {"Content-Type" "application/json"})

(defn- build-response [status body]
  {:status status :headers json-headers :body body})

(defn create-address [request]
  (println request)
  (cond
    (not (authenticated? request))
    (build-response 401 {:error "Authentication failed"})

    :else
    (let [customer-id (jwt/get-customer-id request)
          address-data (:body request)
          ds (:datasource request)]

      (jdbc/execute! ds
                     (queries/create-address
                      {:customer_id customer-id
                       :country (:country address-data)
                       :state (:state address-data)
                       :city (:city address-data)
                       :street (:street address-data)
                       :postal_code (:postal_code address-data)
                       :is_primary (:is_primary address-data)}))

      (build-response 201 {:message "Address created successfully"}))))

(defn get-user-addresses [request]
  (cond
    (not (authenticated? request))
    (build-response 401 {:error "Authentication failed"})

    :else
    (let [customer-id (jwt/get-customer-id request)
          ds (:datasource request)
          addresses (jdbc/execute! ds
                                   (queries/get-addresses-by-customer-id customer-id)
                                   {:builder-fn rs/as-unqualified-maps})]

      (build-response 200 {:addresses addresses}))))

(defn update-address [request]
  (cond
    (not (authenticated? request))
    (build-response 401 {:error "Authentication failed"})

    :else
    (let [customer-id (jwt/get-customer-id request)
          address-id (Long/parseLong (get-in request [:params :address_id]))
          address-data (:body request)
          ds (:datasource request)
          existing-address (jdbc/execute-one! ds
                                              (queries/get-address-by-id address-id)
                                              {:builder-fn rs/as-unqualified-maps})]
      (println existing-address)

      (cond
        (nil? existing-address)
        (build-response 404 {:error "Address not found"})

        (not= customer-id (:customer_id existing-address))
        (build-response 403 {:error "Not authorized to update this address"})

        :else
        (do
          (jdbc/execute! ds
                         (queries/update-address
                          address-id
                          {:country (:country address-data)
                           :state (:state address-data)
                           :city (:city address-data)
                           :street (:street address-data)
                           :postal_code (:postal_code address-data)
                           :is_primary (:is_primary address-data)}))

          (build-response 200 {:message "Address updated successfully"}))))))
