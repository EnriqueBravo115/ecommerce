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

(defn wrap-auth [handler allowed-roles]
  (fn [request]
    (cond
      (not (authenticated? request))
      {:status 401
       :body {:error "Authentication required"}}

      (apply jwt/has-any-role? request allowed-roles)
      (handler request)

      :else
      {:status 403
       :body {:error (str "Access denied. Required roles: " allowed-roles)}})))

(defn wrap-kafka [handler kafka-producer]
  (fn [request]
    (handler (assoc request :kafka (:producer kafka-producer)))))
