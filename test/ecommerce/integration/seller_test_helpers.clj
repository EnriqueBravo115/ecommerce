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

(defn seller-data-with-sales []
  (assoc (seller-data)
         :email       "seller-with-sales@techsolutions.mx"
         :tax_id      "TEC111111111"
         :total_sales 1500.00))

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
