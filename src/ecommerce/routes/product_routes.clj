(ns ecommerce.routes.product-routes
  (:require
   [compojure.core :refer [context routes defroutes GET POST DELETE]]
   [ecommerce.handlers.product-handler :as product-handler]
   [ecommerce.utils.middleware :refer [wrap-auth]]))

(defroutes product-routes
  (context "/product" []
    (-> (routes
         (POST "/create" request
           (product-handler/create-product request))
         (GET "/my-products" request
           (product-handler/get-products-by-seller request))
         (POST "/update/:product_id" request
           (product-handler/update-product request))
         (DELETE "/delete/:product_id" request
           (product-handler/delete-product request))
         (GET "/sku/:sku" request
           (product-handler/get-product-by-sku request))
         (GET "/category/:category-id" request
           (product-handler/get-products-by-category request))
         (GET "/price-range" request
           (product-handler/get-products-by-price-range request))
         (GET "/top-viewed" request
           (product-handler/get-top-viewed-products request))
         (GET "/top-rated" request
           (product-handler/get-top-rated-products request))
         (GET "/view/:id" request
           (product-handler/increment-view-count request))
         (GET "/seller/:seller-id" request
           (product-handler/get-products-by-seller request))
         (GET "/status/:status" request
           (product-handler/get-products-by-status request))
         (GET "/:id" request
           (product-handler/get-product-by-id request)))
        (wrap-auth ["ADMIN" "SELLER" "CUSTOMER"]))))
