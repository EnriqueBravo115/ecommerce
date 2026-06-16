(ns global-service.routes.core
  (:require
   [compojure.core :refer [context defroutes GET routes]]
   [global-service.routes.address-routes :refer [address-routes]]
   [global-service.routes.customer-management-routes :refer [customer-management-routes]]
   [global-service.routes.seller-routes :refer [seller-routes]]
   [global-service.routes.category-routes :refer [category-routes]]
   [global-service.routes.product-routes :refer [product-routes]]
   [global-service.utils.middleware :refer [wrap-jwt-decode]]))

(defroutes api-routes
  (GET "/health" []
    {:status 200 :body {:status "healthy"}})

  (context "/api/v1" []
    (-> (routes
         customer-management-routes
         address-routes
         seller-routes
         category-routes
         product-routes)
        wrap-jwt-decode))

  (GET "*" []
    {:status 404
     :body {:error "Endpoint not found"}}))
