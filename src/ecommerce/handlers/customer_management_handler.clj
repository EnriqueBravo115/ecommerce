(ns ecommerce.handlers.customer-management-handler
  (:require
   [ecommerce.queries.customer-queries :as queries]
   [ecommerce.utils.analytics :as analytics]
   [ecommerce.utils.validations :as validations]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]))

(def ^:private json-headers {"Content-Type" "application/json"})

(defn- build-response [status body]
  {:status status :headers json-headers :body body})

(defn get-customer-by-id [request]
  (let [ds (:datasource request)
        id (Long/parseLong (get-in request [:params :id]))
        query (queries/get-customer-by-id id)
        result (jdbc/execute-one! ds query {:builder-fn rs/as-unqualified-maps})]

    (if result
      (build-response 200 {:customer result})
      (build-response 404 {:error "Customer not found"}))))

(defn get-customers-country-count [request]
  (let [ds (:datasource request)
        query (queries/get-country-count)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

    (if result
      (build-response 200 {:country-count result})
      (build-response 404 {:error "No customers found"}))))

(defn get-customers-by-age-group [request]
  (let [ds (:datasource request)
        query (queries/get-age)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})
        grouped-result (analytics/grouped-result result)]

    (if result
      (build-response 200 {:age-group grouped-result})
      (build-response 404 {:error "No customers found"}))))

(defn get-customers-by-gender [request]
  (let [gender (get-in request [:params :gender])
        validation-error (validations/validate-gender gender)]

    (if validation-error
      (build-response 400 {:error validation-error})
      (let [ds (:datasource request)
            query (queries/get-by-gender gender)
            result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

        (if result
          (build-response 200 {:customer-by-gender result})
          (build-response 404 {:error "No customers found"}))))))

(defn get-registration-trend [request]
  (let [period (get-in request [:params :period])
        validation-error (validations/validate-period period)]

    (if validation-error
      (build-response 400 {:error validation-error})
      (let [ds (:datasource request)
            query (queries/get-registration-date)
            result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})
            trends (analytics/calculate-trends result period)]

        (if result
          (build-response 200 {:period period :trends trends})
          (build-response 404 {:error "No customers found"}))))))

(defn get-active [request]
  (let [ds (:datasource request)
        query (queries/get-active)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

    (if result
      (build-response 200 {:active result})
      (build-response 404 {:error "No customers found"}))))

(defn get-inactive [request]
  (let [ds (:datasource request)
        query (queries/get-inactive)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

    (if result
      (build-response 200 {:inactive result})
      (build-response 404 {:error "No customers found"}))))

(defn get-segment-by-demographics [request]
  (let [country (get-in request [:route-params :country])
        gender (get-in request [:route-params :gender])
        min-age (Long/parseLong (get-in request [:route-params :min-age]))
        max-age (Long/parseLong (get-in request [:route-params :max-age]))
        ds (:datasource request)

        query (queries/get-segment-by-demographics country gender min-age max-age)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

    (if result
      (build-response 200 {:segment-demographics result})
      (build-response 404 {:error "No customers found"}))))

(defn get-registration-by-country-code [request]
  (let [ds (:datasource request)
        query (queries/get-registration-by-country-code)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

    (if result
      (build-response 200 {:registration-by-country-code result})
      (build-response 404 {:error "No customers found"}))))

(defn get-customers-with-password-reset-code [request]
  (let [ds (:datasource request)
        query (queries/get-customers-with-password-reset-code)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

    (if result
      (build-response 200 {:customers-with-password-reset-code result})
      (build-response 404 {:error "No customers found"}))))
