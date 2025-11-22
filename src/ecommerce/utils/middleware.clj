(ns ecommerce.utils.middleware
  (:require [buddy.auth.middleware :refer [wrap-authentication]]
            [buddy.auth :refer [authenticated?]]
            [ring.util.response :as response]))

(defn wrap-datasource [handler datasource]
  (fn [request]
    (handler (assoc request :datasource datasource))))

(defn wrap-jwt
  [handler jwt-component]
  (wrap-authentication
   (fn [request]
     (if (authenticated? request)
       (handler request)
       (-> (response/response {:error "Unauthorized"})
           (response/status 401)
           (response/header "Content-Type" "application/json"))))
   (:backend jwt-component)))
