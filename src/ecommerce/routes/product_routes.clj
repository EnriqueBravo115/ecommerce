(ns ecommerce.routes.product-routes
  (:require
   [compojure.core :refer [GET POST]]
   [ecommerce.controllers.product-controller :as product-controller]))

(def product-routes
  (compojure.core/routes
   (GET "/products" []
     (product-controller/get-products))

   (GET "/products/:id" [id]
     (product-controller/get-product id))

   (POST "/products" {body :body}
     (product-controller/create-product body))))
