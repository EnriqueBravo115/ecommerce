(ns ecommerce.routes.core
  (:require
   [compojure.core :refer [context defroutes GET routes]]
   [ecommerce.routes.address-routes :refer [address-routes]]
   [ecommerce.routes.customer-management-routes :refer [customer-management-routes]]
   [ecommerce.routes.register-routes :refer [register-routes]]
   [ecommerce.routes.seller-routes :refer [seller-routes]]
   [ecommerce.routes.category-routes :refer [category-routes]]
   [ecommerce.routes.product-routes :refer [product-routes]]
   [ecommerce.routes.inventory-routes :refer [inventory-routes]]
   [ecommerce.routes.shopping-cart-routes :refer [shopping-cart-routes]]
   [ecommerce.utils.middleware :refer [wrap-jwt-decode]]))

(defroutes api-routes
  (GET "/health" []
    {:status 200 :body {:status "healthy"}})

  (context "/api/v1" []
    (-> (routes
         customer-management-routes
         register-routes
         address-routes
         seller-routes
         category-routes
         product-routes
         inventory-routes
         shopping-cart-routes)
        wrap-jwt-decode))

  (GET "*" []
    {:status 404
     :body {:error "Endpoint not found"}}))
