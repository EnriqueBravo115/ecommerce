(ns ecommerce.handlers.register-handler
  (:require
   [ecommerce.queries.register-queries :as queries]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]))

(def ^:private json-headers {"Content-Type" "application/json"})

(defn- build-response [status body]
  {:status status :headers json-headers :body body})

(defn- generate-activation-code []
  (subs (str (java.util.UUID/randomUUID)) 0 7))

(defn create-customer [request]
  (let [registration-data (:body request)
        ds (:datasource request)]

    (try
      (let [email (:email registration-data)
            existing-user (jdbc/execute-one! ds
                                             (queries/get-user-by-email email)
                                             {:builder-fn rs/as-unqualified-maps})]
        (cond
          (nil? existing-user)
          (do
            (jdbc/execute! ds
                           (queries/create-customer
                            {:names (:names registration-data)
                             :first_surname (:first_surname registration-data)
                             :second_surname (:second_surname registration-data)
                             :email (:email registration-data)
                             :country_of_birth (:country_of_birth registration-data)
                             :birthday (:birthday registration-data)
                             :gender (:gender registration-data)
                             :rfc (:rfc registration-data)
                             :curp (:curp registration-data)
                             :password (:password registration-data)
                             :phone_number (:phone_number registration-data)
                             :phone_code (:phone_code registration-data)
                             :country_code (:country_code registration-data)
                             :role "CUSTOMER"}))
            (build-response 200 {:message "User registered" :email email}))

          (false? (:active existing-user))
          (do
            (jdbc/execute! ds
                           (queries/update-customer
                            (:id existing-user)
                            {:names (:names registration-data)
                             :first_surname (:first_surname registration-data)
                             :second_surname (:second_surname registration-data)
                             :country_of_birth (:country_of_birth registration-data)
                             :email (:email registration-data)
                             :birthday (:birthday registration-data)
                             :gender (:gender registration-data)
                             :rfc (:rfc registration-data)
                             :curp (:curp registration-data)
                             :password (:password registration-data)
                             :phone_number (:phone_number registration-data)
                             :phone_code (:phone_code registration-data)
                             :country_code (:country_code registration-data)}))
            (build-response 200 {:message "User updated" :email email}))

          :else
          (build-response 403 {:error "Email has already been taken"})))

      (catch Exception e
        (build-response 500 {:error "Internal server error" :details (.getMessage e)})))))
