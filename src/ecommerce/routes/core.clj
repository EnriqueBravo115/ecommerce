(ns ecommerce.routes.core
  (:require
   [compojure.core :refer [routes context defroutes GET]]
   [ecommerce.utils.middleware :refer [wrap-jwt]]
   [ecommerce.routes.customer-management-routes :refer [customer-management-routes]]
   [ecommerce.routes.register-routes :refer [register-routes]]
   [ecommerce.routes.address-routes :refer [address-routes]]
   [ecommerce.routes.seller-routes :refer [seller-routes]]))

(defroutes api-routes
  (GET "/health" []
    {:status 200 :body {:status "healthy"}})

  (context "/api/v1" []
    (-> (routes customer-management-routes register-routes address-routes seller-routes)
        wrap-jwt))

  (GET "*" [request]
    {:status 404
     :body {:error "Endpoint not found"}}))
