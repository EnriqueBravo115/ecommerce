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
        ds (:datasource request)
        password_encoded (password/encode (:password registration-data))
        email (:email registration-data)
        existing-customer (jdbc/execute-one! ds
                                             (queries/get-customer-by-email email)
                                             {:builder-fn rs/as-unqualified-maps})]

    (cond
      (nil? existing-customer)
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
                         :country_code (:country_code registration-data)}))
        (build-response 200 {:message "User registered" :email email}))

      (false? (:active existing-customer))
      (do
        (jdbc/execute! ds
                       (queries/update-customer
                        (:id existing-customer)
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
      (build-response 403 {:error "Email has already been taken"}))))

(defn send-registration-code [request]
  (let [email (get-in request [:params :email])
        ds (:datasource request)
        customer (jdbc/execute-one! ds
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
      (build-response 404 {:error "User not found"}))))

(defn check-registration-code [request]
  (let [code (get-in request [:params :code])
        ds (:datasource request)]

    (if (str/blank? code)
      (build-response 400 {:error "Activation code is required"})
      (let [customer (jdbc/execute-one! ds
                                        (queries/get-user-by-activation-code code)
                                        {:builder-fn rs/as-unqualified-maps})]

        (if customer
          (do
            (jdbc/execute! ds
                           (queries/clear-activation-code {:id (:id customer)}))
            (build-response 200 {:message "User successfully verified" :email (:email customer)}))
          (build-response 404 {:error "Activation code not found"}))))))

(defn end-registration [request]
  (let [{:keys [email password]} (:body request)
        ds (:datasource request)]

    (cond
      (or (str/blank? email) (str/blank? password))
      (build-response 400 {:error "Email and password are required"})

      :else
      (let [customer (jdbc/execute-one! ds
                                        (queries/get-customer-by-email email)
                                        {:builder-fn rs/as-unqualified-maps})]

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
                             :email email})))))))
