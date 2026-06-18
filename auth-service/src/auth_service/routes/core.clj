(ns auth-service.routes.core
  (:require
   [reitit.ring :as ring]
   [auth-service.routes.register-routes :refer [register-routes]]))

(def router
  (ring/router
   [["/health" {:get (fn [_] {:status 200 :body {:status "healthy"}})}]
    ["/api/v1" register-routes]]))

(def handler
  (ring/ring-handler
   router
   (ring/create-default-handler
    {:not-found (fn [_] {:status 404 :body {:error "Endpoint not found"}})})))
