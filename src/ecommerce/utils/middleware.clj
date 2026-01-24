(ns ecommerce.utils.middleware
  (:require
   [buddy.auth :refer [authenticated?]]
   [buddy.auth.middleware :refer [wrap-authentication]]
   [ecommerce.utils.jwt :as jwt]))

(defn wrap-datasource [handler datasource]
  (fn [request]
    (handler (assoc request :datasource datasource))))

(defn wrap-jwt [handler jwt-component]
  (fn [request]
    (handler (assoc request :jwt-component jwt-component))))

(defn wrap-jwt-decode [handler]
  (fn [request]
    (if-let [backend (-> request :jwt-component :backend)]
      (let [auth-handler (wrap-authentication handler backend)]
        (auth-handler request))
      (handler request))))

(defn wrap-authenticated [handler]
  (fn [request]
    (if (authenticated? request)
      (handler request)
      {:status 401 :body {:error "Authentication failed"}})))

(defn wrap-roles [handler allowed-roles]
  (fn [request]
    (if (apply jwt/has-any-role? request allowed-roles)
      (handler request)
      {:status 403 :body {:error (str "Access denied. Required roles: " allowed-roles)}})))
