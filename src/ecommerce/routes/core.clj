(ns ecommerce.routes.core
  (:require
   [compojure.core :refer [context GET routes]]
   [compojure.route :as route]
   [ecommerce.routes.order-routes :refer [order-routes]]
   [ecommerce.routes.product-routes :refer [product-routes]]
   [ecommerce.routes.user-routes :refer [user-routes]]))

(def api-routes
  (routes
   (context "/api/v1" []
     (routes
      user-routes
      product-routes
      order-routes))

   (GET "/health" []
     {:status 200 :body {:status "healthy"}})

   (route/not-found
    {:status 404
     :body {:error "Endpoint not found"}})))
