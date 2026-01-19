(ns ecommerce.handlers.register-handler
  (:require
   [clojure.string :as str]
   [ecommerce.queries.register-queries :as queries]
   [ecommerce.utils.email :as email]
   [ecommerce.utils.password :as password]
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
                                             (queries/get-customer-by-email email)
                                             {:builder-fn rs/as-unqualified-maps})
            password_encoded (password/encode (:password registration-data))]
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
                             :password password_encoded
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
                             :password password_encoded
                             :phone_number (:phone_number registration-data)
                             :phone_code (:phone_code registration-data)
                             :country_code (:country_code registration-data)}))
            (build-response 200 {:message "User updated" :email email}))

          :else
          (build-response 403 {:error "Email has already been taken"})))

      (catch Exception e
        (build-response 500 {:error "Internal server error" :details (.getMessage e)})))))

(defn send-registration-code [request]
  (let [email (get-in request [:params :email])
        ds (:datasource request)]

    (if (str/blank? email)
      (build-response 400 {:error "Email is required"})
      (try
        (let [customer (jdbc/execute-one! ds
                                          (queries/get-customer-by-email email)
                                          {:builder-fn rs/as-unqualified-maps})]

          (if customer
            (let [activation-code (generate-activation-code)]
              (jdbc/execute! ds
                             (queries/update-activation-code
                              {:id (:id customer)
                               :activation_code activation-code}))

              (email/send-activation-code-email
               email
               activation-code)

              (build-response 200 {:message "Verification code sent" :email email}))
            (build-response 404 {:error "User not found"})))

        (catch Exception e
          (build-response 500 {:error "Internal server error" :details (.getMessage e)}))))))

(defn check-registration-code [request]
  (let [code (get-in request [:params :code])
        ds (:datasource request)]

    (if (str/blank? code)
      (build-response 400 {:error "Activation code is required"})
      (try
        (let [customer (jdbc/execute-one! ds
                                          (queries/get-user-by-activation-code code)
                                          {:builder-fn rs/as-unqualified-maps})]

          (if customer
            (do
              (jdbc/execute! ds
                             (queries/clear-activation-code {:id (:id customer)}))
              (build-response 200 {:message "User successfully verified" :email (:email customer)}))
            (build-response 404 {:error "Activation code not found"})))

        (catch Exception e
          (build-response 500 {:error "Internal server error" :details (.getMessage e)}))))))

(defn end-registration [request]
  (let [{:keys [email password]} (:body request)
        ds (:datasource request)]

    (cond
      (or (str/blank? email) (str/blank? password))
      (build-response 400 {:error "Email and password are required"})

      :else
      (try
        (let [customer (jdbc/execute-one! ds
                                          (queries/get-customer-by-email email)
                                          {:builder-fn rs/as-unqualified-maps})]

          (println customer)

          (cond
            (nil? customer)
            (build-response 404 {:error "User not found"})

            (:active customer)
            (build-response 400 {:error "User is already active"})

            (not (password/check password (:password customer)))
            (build-response 401 {:error "Invalid password"})

            :else
            (do
              (jdbc/execute! ds
                             (queries/activate-user {:id (:id customer)}))
              (build-response 200
                              {:message "Registration completed successfully"
                               :email email}))))

        (catch Exception e
          (build-response 500 {:error "Internal server error" :details (.getMessage e)}))))))
