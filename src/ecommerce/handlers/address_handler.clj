(ns ecommerce.handlers.address-handler
  (:require
   [ecommerce.queries.address-queries :as queries]
   [ecommerce.utils.validations :as validations]
   [ecommerce.utils.jwt :as jwt]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]))

(def ^:private json-headers {"Content-Type" "application/json"})

(defn- build-response [status body]
  {:status status :headers json-headers :body body})

(defn create-address [request]
  (let [customer-id (jwt/get-current-identity-id request)
        address-data (:body request)
        validation-error (validations/validate-address address-data)
        ds (:datasource request)]

    (cond
      validation-error
      (build-response 400 {:error validation-error})

      :else
      (let [address-count-result (jdbc/execute! ds (queries/count-addresses customer-id))
            address-count (-> address-count-result first :count)
            is-primary (:is_primary address-data false)
            primary-exists-result (jdbc/execute! ds (queries/has-primary-address customer-id))
            primary-exists (-> primary-exists-result first :count pos?)]

        (cond
          (>= address-count 3)
          (build-response 400 {:error "Customer cannot have more than 3 addresses, delete at least 1"})

          (and is-primary primary-exists)
          (do
            (jdbc/execute! ds (queries/unset-existing-primary customer-id))
            (jdbc/execute! ds
                           (queries/create-address
                            {:customer_id customer-id
                             :country (:country address-data)
                             :state (:state address-data)
                             :city (:city address-data)
                             :street (:street address-data)
                             :postal_code (:postal_code address-data)
                             :is_primary true}))
            (build-response 201 {:message "Primary address updated successfully"}))

          is-primary
          (do
            (jdbc/execute! ds
                           (queries/create-address
                            {:customer_id customer-id
                             :country (:country address-data)
                             :state (:state address-data)
                             :city (:city address-data)
                             :street (:street address-data)
                             :postal_code (:postal_code address-data)
                             :is_primary true}))
            (build-response 201 {:message "Primary address created successfully"}))

          :else
          (do
            (jdbc/execute! ds
                           (queries/create-address
                            {:customer_id customer-id
                             :country (:country address-data)
                             :state (:state address-data)
                             :city (:city address-data)
                             :street (:street address-data)
                             :postal_code (:postal_code address-data)
                             :is_primary false}))
            (build-response 201 {:message "Address created successfully"})))))))

(defn update-address [request]
  (let [customer-id (jwt/get-current-identity-id request)
        address-id (Long/parseLong (get-in request [:params :address_id]))
        address-data (:body request)
        validation-error (validations/validate-address-update address-data)
        ds (:datasource request)
        existing-address (jdbc/execute-one! ds
                                            (queries/get-address-by-id address-id)
                                            {:builder-fn rs/as-unqualified-maps})]

    (cond
      validation-error
      (build-response 400 {:error validation-error})

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
                         :postal_code (:postal_code address-data)}))

        (build-response 200 {:message "Address updated successfully"})))))

(defn delete-address [request]
  (let [customer-id (jwt/get-current-identity-id request)
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

        (build-response 200 {:message "Address deleted successfully"})))))

(defn set-primary-address [request]
  (let [customer-id (jwt/get-current-identity-id request)
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
      (do
        (jdbc/execute! ds (queries/unset-all-primary customer-id))
        (jdbc/execute! ds (queries/set-primary address-id))
        (build-response 200 {:message "Primary address updated successfully"})))))

(defn get-customer-addresses [request]
  (let [customer-id (jwt/get-current-identity-id request)
        ds (:datasource request)
        addresses (jdbc/execute! ds
                                 (queries/get-addresses-by-customer-id customer-id)
                                 {:builder-fn rs/as-unqualified-maps})]

    (build-response 200 {:addresses addresses})))

(defn get-primary-address [request]
  (let [customer-id (jwt/get-current-identity-id request)
        ds (:datasource request)
        primary-address (jdbc/execute-one! ds
                                           (queries/get-primary-address customer-id)
                                           {:builder-fn rs/as-unqualified-maps})]

    (if primary-address
      (build-response 200 {:address primary-address})
      (build-response 404 {:error "No primary address found"}))))

(defn get-customers-by-location [request]
  (let [ds (:datasource request)
        country (get-in request [:params :country])
        state (get-in request [:params :state])
        city (get-in request [:params :city])

        result (jdbc/execute! ds
                              (queries/get-customers-by-location country state city)
                              {:builder-fn rs/as-unqualified-maps})]

    (if result
      (build-response 200 {:customers result :filters {:country country :state state :city city}})
      (build-response 404 {:error "No customers found in specified location"}))))

(defn get-customers-by-postal-code [request]
  (let [postal-code (get-in request [:params :postal_code])
        ds (:datasource request)

        result (jdbc/execute! ds
                              (queries/get-customers-by-postal-code postal-code)
                              {:builder-fn rs/as-unqualified-maps})]

    (if result
      (build-response 200 {:customers result :postal_code postal-code})
      (build-response 404 {:error "No customers found with this postal code"}))))

(defn get-location-statistics [request]
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
                      :top_cities top-cities}})))

(defn get-recent-id-address [request]
  (let [ds (:datasource request)
        result (jdbc/execute! ds
                              (queries/get-recent-id-address)
                              {:builder-fn rs/as-unqualified-maps})]
    (build-response 200 {:recent result})))
