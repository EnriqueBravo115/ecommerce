(ns ecommerce.routes.core
  (:require
   [compojure.core :refer [context defroutes GET]]
   [ecommerce.utils.middleware :refer [wrap-jwt]]
   [ecommerce.routes.customer-routes :refer [customer-routes]]))

(defroutes api-routes
  (GET "/health" []
    {:status 200 :body {:status "healthy"}})

  (context "/api/v1" []
    (-> customer-routes
        wrap-jwt))

  (GET "*" [request]
    {:status 404
     :body {:error "Endpoint not found"}}))
