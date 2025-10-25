(ns ecommerce.routes.core
  (:require
   [compojure.core :refer [context GET defroutes]]
   [ecommerce.routes.user-routes :refer [user-routes]]))

(defroutes api-routes
  (context "/api/v1" [] user-routes)

  (GET "/health" [request]
    {:status 200 :body {:status "healthy"}})

  (GET "*" [request]
    {:status 404
     :body {:error "Endpoint not found"}}))
