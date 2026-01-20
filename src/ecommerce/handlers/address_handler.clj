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

(defn delete-address [request]
  (cond
    (not (authenticated? request))
    (build-response 401 {:error "Authentication failed"})

    :else
    (let [customer-id (jwt/get-customer-id request)
          address-id (Long/parseLong (get-in request [:params :address_id]))
          ds (:datasource request)
          existing-address (jdbc/execute-one! ds
                                              (queries/get-address-by-id address-id)
                                              {:builder-fn rs/as-unqualified-maps})]

      (cond
        (nil? existing-address)
        (build-response 404 {:error "Address not found"})

        (not= customer-id (:customer_id existing-address))
        (build-response 403 {:error "Not authorized to delete this address"})

        (:is_primary existing-address)
        (build-response 400 {:error "Cannot delete primary address"})

        :else
        (do
          (jdbc/execute! ds
                         (queries/delete-address address-id))

          (build-response 200 {:message "Address deleted successfully"}))))))

(defn set-primary-address [request]
  (cond
    (not (authenticated? request))
    (build-response 401 {:error "Authentication failed"})

    :else
    (let [customer-id (jwt/get-customer-id request)
          address-id (Long/parseLong (get-in request [:params :address_id]))
          ds (:datasource request)
          existing-address (jdbc/execute-one! ds
                                              (queries/get-address-by-id address-id)
                                              {:builder-fn rs/as-unqualified-maps})]

      (cond
        (nil? existing-address)
        (build-response 404 {:error "Address not found"})

        (not= customer-id (:customer_id existing-address))
        (build-response 403 {:error "Not authorized to modify this address"})

        :else
        (jdbc/with-transaction [tx ds]
          (jdbc/execute! tx
                         (queries/unset-all-primary customer-id))

          (jdbc/execute! tx
                         (queries/set-primary address-id))

          (build-response 200 {:message "Primary address updated successfully"}))))))

(defn get-primary-address [request]
  (cond
    (not (authenticated? request))
    (build-response 401 {:error "Authentication failed"})

    :else
    (let [customer-id (jwt/get-customer-id request)
          ds (:datasource request)
          primary-address (jdbc/execute-one! ds
                                             (queries/get-primary-address customer-id)
                                             {:builder-fn rs/as-unqualified-maps})]

      (if primary-address
        (build-response 200 {:address primary-address})
        (build-response 404 {:error "No primary address found"})))))

(defn get-customers-by-location [request]
  (cond
    (not (authenticated? request))
    (build-response 401 {:error "Authentication failed"})

    (not (jwt/has-any-role? request "ADMIN"))
    (build-response 403 {:error "Admin access required"})

    :else
    (let [ds (:datasource request)
          country (get-in request [:params :country])
          state (get-in request [:params :state])
          city (get-in request [:params :city])

          result (jdbc/execute! ds
                                (queries/get-customers-by-location country state city)
                                {:builder-fn rs/as-unqualified-maps})]

      (if (seq result)
        (build-response 200 {:customers result :filters {:country country :state state :city city}})
        (build-response 404 {:error "No customers found in specified location"})))))

(defn get-customers-by-postal-code [request]
  (cond
    (not (authenticated? request))
    (build-response 401 {:error "Authentication failed"})

    (not (jwt/has-any-role? request "ADMIN"))
    (build-response 403 {:error "Admin access required"})

    :else
    (let [postal-code (get-in request [:params :postal_code])
          ds (:datasource request)

          result (jdbc/execute! ds
                                (queries/get-customers-by-postal-code postal-code)
                                {:builder-fn rs/as-unqualified-maps})]

      (if (seq result)
        (build-response 200 {:customers result :postal_code postal-code})
        (build-response 404 {:error "No customers found with this postal code"})))))

(defn get-location-statistics [request]
  (cond
    (not (authenticated? request))
    (build-response 401 {:error "Authentication failed"})

    (not (jwt/has-any-role? request "ADMIN"))
    (build-response 403 {:error "Admin access required"})

    :else
    (let [ds (:datasource request)
          top-countries (jdbc/execute! ds
                          (queries/get-top-countries)
                          {:builder-fn rs/as-unqualified-maps})
          top-states (jdbc/execute! ds
                       (queries/get-top-states)
                       {:builder-fn rs/as-unqualified-maps})
          top-cities (jdbc/execute! ds
                       (queries/get-top-cities)
                       {:builder-fn rs/as-unqualified-maps})]

      (build-response 200
        {:statistics
         {:top_countries top-countries
          :top_states top-states
          :top_cities top-cities}}))))
