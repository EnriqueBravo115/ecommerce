(ns ecommerce.handlers.address-handler
  (:require
   [buddy.auth :refer [authenticated?]]
   [ecommerce.queries.address-queries :as queries]
   [ecommerce.utils.jwt :as jwt]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]))

(def ^:private json-headers {"Content-Type" "application/json"})

(defn- build-response [status body]
  {:status status :headers json-headers :body body})

(defn create-address [request]
  (println request)
  (cond
    (not (authenticated? request))
    (build-response 401 {:error "Authentication failed"})

    :else
    (let [user-id (jwt/get-user-id request)
          address-data (:body request)
          ds (:datasource request)]

      (jdbc/execute! ds
                     (queries/create-address
                      {:user_id user-id
                       :country (:country address-data)
                       :state (:state address-data)
                       :city (:city address-data)
                       :street (:street address-data)
                       :postal_code (:postal_code address-data)
                       :is_primary (:is_primary address-data)}))

      (build-response 201 {:message "Address created successfully"}))))

(defn get-user-addresses [request]
  (cond
    (not (authenticated? request))
    (build-response 401 {:error "Authentication failed"})

    :else
    (let [user-id (jwt/get-user-id request)
          ds (:datasource request)
          addresses (jdbc/execute! ds
                                   (queries/get-addresses-by-user-id user-id)
                                   {:builder-fn rs/as-unqualified-maps})]

      (build-response 200 {:addresses addresses}))))
