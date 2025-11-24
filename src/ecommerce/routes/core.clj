(ns ecommerce.routes.core
  (:require
   [compojure.core :refer [context defroutes GET]]
   [ecommerce.utils.middleware :refer [wrap-jwt wrap-require-auth]]
   [ecommerce.routes.customer-routes :refer [customer-routes]]))

(defroutes api-routes
  (GET "/health" [request]
    {:status 200 :body {:status "healthy"}})

  (context "/api/v1" []
    (-> customer-routes
        wrap-require-auth
        wrap-jwt))

  (GET "*" [request]
    {:status 404
     :body {:error "Endpoint not found"}}))
