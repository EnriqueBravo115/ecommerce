(ns auth-service.routes.core
  (:require
   [compojure.core :refer [context defroutes GET routes]]
   [auth-service.routes.register-routes :refer [register-routes]]))

(defroutes api-routes
  (GET "/health" []
    {:status 200 :body {:status "healthy"}})

  (context "/api/v1" []
    (routes
     register-routes))

  (GET "*" []
    {:status 404
     :body {:error "Endpoint not found"}}))
