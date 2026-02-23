(ns ecommerce.integration.seller-test-helpers
  (:require [clj-http.client :as client]))

(defn seller-data []
  {:business_name   "Tech Solutions MX"
   :legal_name      "Soluciones Tecnológicas S.A. de C.V."
   :tax_id          "TEC123456789"
   :email           "ventas@techsolutions.mx"
   :phone           "+525512345678"
   :country         "Mexico"
   :state           "Ciudad de México"
   :city            "CDMX"
   :address         "Av. Reforma 123"
   :postal_code     "06600"
   :website         "https://techsolutions.mx"
   :status          "PENDING"
   :verified        false
   :commission_rate 15.5
   :password        "password"
   :bank_account    "0123456789"
   :bank_name       "Banco Nacional"})

(defn seller-location-data []
  {:state       "Jalisco"
   :city        "Guadalajara"
   :address     "Av. Chapultepec 789"
   :postal_code "44100"})

(defn seller-data-duplicate-email []
  (assoc (seller-data)
         :business_name   "Another Tech Solutions"
         :legal_name      "Otra Solución S.A. de C.V."
         :tax_id          "TEC987654321"
         :phone           "+525598765432"
         :address         "Av. Insurgentes 456"
         :postal_code     "06610"
         :website         "https://anothertech.mx"
         :commission_rate 12.0
         :password        "anotherpassword"
         :bank_account    "9876543210"
         :bank_name       "Banco del Norte"))

(defn seller-status-data []
  {:status "active"})

(defn post-seller
  ([seller] (post-seller seller {}))
  ([seller extra-opts]
   (client/post "http://localhost:3001/api/v1/seller/create-seller"
                (merge {:accept           :json
                        :content-type     :json
                        :throw-exceptions false
                        :form-params      seller}
                       extra-opts))))

(defn put-seller-location
  ([seller-id location] (put-seller-location seller-id location {}))
  ([seller-id location extra-opts]
   (client/put (str "http://localhost:3001/api/v1/seller/update-seller-location/" seller-id)
               (merge {:accept           :json
                       :content-type     :json
                       :throw-exceptions false
                       :form-params      location}
                      extra-opts))))

(defn delete-seller
  ([seller-id] (delete-seller seller-id {}))
  ([seller-id extra-opts]
   (client/delete (str "http://localhost:3001/api/v1/seller/delete-seller/" seller-id)
                  (merge {:accept           :json
                          :throw-exceptions false}
                         extra-opts))))

(defn put-seller-status
  ([seller-id status] (put-seller-status seller-id status {}))
  ([seller-id status extra-opts]
   (client/put (str "http://localhost:3001/api/v1/seller/update-seller-status/" seller-id)
               (merge {:accept           :json
                       :content-type     :json
                       :throw-exceptions false
                       :form-params      status}
                      extra-opts))))

(defn put-verify-seller
  ([seller-id] (put-verify-seller seller-id {}))
  ([seller-id extra-opts]
   (client/put (str "http://localhost:3001/api/v1/seller/verify-seller/" seller-id)
               (merge {:accept           :json
                       :throw-exceptions false}
                      extra-opts))))

(defn get-seller-by-id
  ([seller-id] (get-seller-by-id seller-id {}))
  ([seller-id extra-opts]
   (client/get (str "http://localhost:3001/api/v1/seller/get-seller-by-id/" seller-id)
               (merge {:accept           :json
                       :throw-exceptions false}
                      extra-opts))))

(defn get-sellers-by-country-stats
  ([opts] (client/get "http://localhost:3001/api/v1/seller/get-sellers-by-country-stats"
                      (merge {:accept           :json
                              :throw-exceptions false}
                             opts)))
  ([] (get-sellers-by-country-stats {})))

(defn get-sellers-by-status
  ([status] (get-sellers-by-status status {}))
  ([status extra-opts]
   (client/get (str "http://localhost:3001/api/v1/seller/get-sellers-by-status/" status)
               (merge {:accept           :json
                       :throw-exceptions false}
                      extra-opts))))

(defn get-top-sellers
  ([limit] (get-top-sellers limit {}))
  ([limit extra-opts]
   (client/get (str "http://localhost:3001/api/v1/seller/get-top-sellers/" limit)
               (merge {:accept           :json
                       :throw-exceptions false}
                      extra-opts))))

(defn get-unverified-sellers
  ([opts] (client/get "http://localhost:3001/api/v1/seller/get-unverified-sellers"
                      (merge {:accept           :json
                              :throw-exceptions false}
                             opts)))
  ([] (get-unverified-sellers {})))
