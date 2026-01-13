(ns ecommerce.handlers.customer-handler
  (:require
   [buddy.auth :refer [authenticated?]]
   [ecommerce.queries.customer-queries :as queries]
   [ecommerce.utils.analytics :as analytics]
   [ecommerce.utils.validations :as validations]
   [ecommerce.utils.jwt :as jwt]
   [honey.sql :as sql]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]))

(def ^:private json-headers {"Content-Type" "application/json"})

(defn- build-response [status body]
  {:status status :headers json-headers :body body})

(defn get-customer-by-id [request]
  (cond
    (not (authenticated? request))
    (build-response 401 {:error "Authentication failed"})

    (not (jwt/has-any-role? request "ADMIN"))
    (build-response 403 {:error "Admin access required"})

    :else
    (let [ds (:datasource request)
          id (get-in request [:params :id])
          query (queries/get-by-id id)
          result (jdbc/execute-one! ds query {:builder-fn rs/as-unqualified-maps})]

      (if result
        (build-response 200 {:customer result})
        (build-response 404 {:error "Customer not found"})))))

(defn get-customers-country-count [request]
  (cond
    (not (authenticated? request))
    (build-response 401 {:error "Authentication failed"})

    (not (jwt/has-any-role? request "ADMIN"))
    (build-response 403 {:error "Admin access required"})

    :else
    (let [ds (:datasource request)
          query (queries/get-country-count)
          result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

      (if result
        (build-response 200 {:country-count result})
        (build-response 404 {:error "No customers found"})))))

(defn get-customers-by-age-group [request]
  (cond
    (not (authenticated? request))
    (build-response 401 {:error "Authentication failed"})

    (not (jwt/has-any-role? request "ADMIN"))
    (build-response 403 {:error "Admin access required"})

    :else
    (let [ds (:datasource request)
          query (queries/get-age)
          result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})
          grouped-result (analytics/grouped-result result)]

      (if result
        (build-response 200 {:age-group grouped-result})
        (build-response 404 {:error "No customers found"})))))

(defn get-customers-by-gender [request]
  (cond
    (not (authenticated? request))
    (build-response 401 {:error "Authentication failed"})

    (not (jwt/has-any-role? request "ADMIN"))
    (build-response 403 {:error "Admin access required"})

    :else
    (let [gender (get-in request [:params :gender])
          validation-error (validations/validate-gender gender)]

      (if validation-error
        (build-response 400 {:error validation-error})
        (let [ds (:datasource request)
              query (queries/get-by-gender gender)
              result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

          (if result
            (build-response 200 {:customer-by-gender result})
            (build-response 404 {:error "No customers found"})))))))

(defn get-registration-trend [request]
  (cond
    (not (authenticated? request))
    (build-response 401 {:error "Authentication failed"})

    (not (jwt/has-any-role? request "ADMIN"))
    (build-response 403 {:error "Admin access required"})

    :else
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
            (build-response 404 {:error "No customers found"})))))))

(defn get-active-rate [request]
  (cond
    (not (authenticated? request))
    (build-response 401 {:error "Authentication failed"})

    (not (jwt/has-any-role? request "ADMIN"))
    (build-response 403 {:error "Admin access required"})

    :else
    (let [ds (:datasource request)
          query (queries/get-active-rate)
          result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})
          total (:total (first result))
          active (:active (first result))]

      (if result
        (build-response 200 {:percentage (* 100 (/ active total)) :total total :active active})
        (build-response 404 {:error "No customers found"})))))

(defn get-inactive [request]
  (cond
    (not (authenticated? request))
    (build-response 401 {:error "Authentication failed"})

    (not (jwt/has-any-role? request "ADMIN"))
    (build-response 403 {:error "Admin access required"})

    :else
    (let [ds (:datasource request)
          query (queries/get-inactive)
          result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

      (if result
        (build-response 200 {:inactive result})
        (build-response 404 {:error "No customers found"})))))

(defn get-segment-by-demographics [request]
  (let [country (get-in request [:route-params :country])
        gender (get-in request [:route-params :gender])
        age-group (get-in request [:route-params :age-group])
        ds (:datasource request)
        query (sql/format {:select []
                           :from [:customer]
                           :where []})
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]))

(defn get-registration-by-country-code [request]
  (let [ds (:datasource request)
        query (sql/format {:select []
                           :from [:customer]
                           :where []})
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]))

(defn get-customers-with-password-reset-code [request]
  (let [ds (:datasource request)
        query (sql/format {:select []
                           :from [:customer]
                           :where []})
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]))
