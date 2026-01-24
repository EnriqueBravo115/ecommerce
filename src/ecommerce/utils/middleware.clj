(ns ecommerce.utils.middleware 
  (:require
   [buddy.auth.middleware :refer [wrap-authentication]]))

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
