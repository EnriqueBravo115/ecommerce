(ns auth-service.handlers.register-handler
  (:require
   [clojure.string :as str]
   [auth-service.queries.register-queries :as queries]
   [auth-service.utils.email :as email]
   [auth-service.utils.password :as password]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]))

(def ^:private json-headers {"Content-Type" "application/json"})

(defn- build-response [status body]
  {:status status :headers json-headers :body body})

(defn- generate-activation-code []
  (subs (str (java.util.UUID/randomUUID)) 0 7))

; birthday is converted to java.sql.Date here because HoneySQL serializes
; java.time.LocalDate back to a string, causing a type mismatch on the date column.
(defn- customer-data [body encoded-password]
  (-> (select-keys body [:names :first_surname :second_surname :email
                         :country_of_birth :birthday :gender :rfc :curp
                         :phone_number :phone_code :country_code])
      (update :birthday #(some-> % java.sql.Date/valueOf))
      (assoc :password encoded-password)))

; Creates a new customer or updates an existing inactive one. 
; Returns 403 if the email belongs to an already active account.
(defn create-customer [request]
  (let [{:keys [body datasource]} request
        email (:email body)
        data (customer-data body (password/encode (:password body)))
        existing (jdbc/execute-one! datasource
                                    (queries/get-customer-by-email email)
                                    {:builder-fn rs/as-unqualified-maps})]
    (cond
      (nil? existing)
      (do
        (jdbc/execute! datasource (queries/create-customer data))
        (build-response 200 {:message "User registered" :email email}))

      (false? (:active existing))
      (do
        (jdbc/execute! datasource (queries/update-customer (:id existing) data))
        (build-response 200 {:message "User updated" :email email}))

      :else
      (build-response 403 {:error "Email has already been taken"}))))

(defn send-registration-code [request]
  (let [email (get-in request [:path-params :email])
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
  (let [code (get-in request [:path-params :code])
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
