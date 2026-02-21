(ns ecommerce.integration.address-test-helpers
  (:require
   [clj-http.client :as client]))

(defn create-address
  ([address] (create-address address {}))
  ([address extra-opts]
   (client/post "http://localhost:3001/api/v1/address/create-address"
                (merge {:accept           :json
                        :content-type     :json
                        :throw-exceptions false
                        :form-params      address}
                       extra-opts))))

(defn update-address
  ([address-id address] (update-address address-id address {}))
  ([address-id address extra-opts]
   (client/post (str "http://localhost:3001/api/v1/address/update-address/" address-id)
                (merge {:accept           :json
                        :content-type     :json
                        :throw-exceptions false
                        :form-params      address}
                       extra-opts))))

(defn delete-address
  ([address-id] (delete-address address-id {}))
  ([address-id extra-opts]
   (client/delete (str "http://localhost:3001/api/v1/address/delete-address/" address-id)
                  (merge {:accept           :json
                          :throw-exceptions false}
                         extra-opts))))

(defn set-primary-address
  ([address-id] (set-primary-address address-id {}))
  ([address-id extra-opts]
   (client/post (str "http://localhost:3001/api/v1/address/set-primary-address/" address-id)
                (merge {:accept           :json
                        :throw-exceptions false}
                       extra-opts))))

(defn get-customer-addresses
  ([opts] (client/get "http://localhost:3001/api/v1/address/get-customer-addresses"
                      (merge {:accept           :json
                              :throw-exceptions false}
                             opts)))
  ([] (get-customer-addresses {})))

(defn get-primary-address
  ([opts] (client/get "http://localhost:3001/api/v1/address/get-primary-address"
                      (merge {:accept           :json
                              :throw-exceptions false}
                             opts)))
  ([] (get-primary-address {})))

(defn get-customers-by-location
  ([country state city opts]
   (client/get (str "http://localhost:3001/api/v1/address/admin/get-customers-by-location/" country "/" state "/" city)
               (merge {:accept           :json
                       :throw-exceptions false}
                      opts)))
  ([country state city] (get-customers-by-location country state city {})))

(defn get-customers-by-postal-code
  ([postal-code opts]
   (client/get (str "http://localhost:3001/api/v1/address/admin/get-customers-by-postal-code/" postal-code)
               (merge {:accept           :json
                       :throw-exceptions false}
                      opts)))
  ([postal-code] (get-customers-by-postal-code postal-code {})))

(defn get-location-statistics
  ([opts] (client/get "http://localhost:3001/api/v1/address/admin/get-location-statistics"
                      (merge {:accept           :json
                              :throw-exceptions false}
                             opts)))
  ([] (get-location-statistics {})))

(defn address_primary_true []
  {:country "Mexico"
   :state "Guanajuato"
   :city "Guanajuato"
   :street "Main Street"
   :postal_code "12345"
   :is_primary true})

(defn address_primary_false []
  {:country "Mexico"
   :state "Guanajuato"
   :city "Guanajuato"
   :street "Main Street"
   :postal_code "12345"
   :is_primary false})

(defn address_update []
  {:country     "USA"
   :state       "Texas"
   :city        "Austin"
   :street      "Congress Ave"
   :postal_code "73301"})
