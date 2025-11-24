(ns ecommerce.utils.middleware
  (:require
   [buddy.auth :refer [authenticated?]]
   [buddy.auth.middleware :refer [wrap-authentication]]
   [ring.util.response :as response]))

(defn wrap-datasource [handler datasource]
  (fn [request]
    (handler (assoc request :datasource datasource))))

(defn wrap-inject-jwt [handler jwt-component]
  (fn [request]
    (handler (assoc request :jwt-component jwt-component))))

(defn wrap-jwt [handler]
  (fn [request]
    (if-let [backend (-> request :jwt-component :backend)]
      (let [auth-handler (wrap-authentication handler backend)]
        (auth-handler request))
      (handler request))))

(defn wrap-require-auth [handler]
  (fn [request]
    (if (authenticated? request)
      (handler request)
      (-> (response/response {:error "Authentication required"})
          (response/status 401)
          (response/header "Content-Type" "application/json")))))
