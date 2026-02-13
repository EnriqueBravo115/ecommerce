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
         (GET "/search" request
           (product-handler/search-products request))
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
         (GET "/tags/:tags" request
           (product-handler/get-products-by-tags request))
         (GET "/:id/view" request
           (product-handler/increment-view-count request))
         (GET "/seller/:seller-id" request
           (product-handler/get-products-by-seller request))
         (GET "/status/:status" request
           (product-handler/get-products-by-status request))
         (GET "/stats" request
           (product-handler/get-product-stats request))
         (GET "/:id" request
           (product-handler/get-product-by-id request)))
        (wrap-auth ["ADMIN" "SELLER" "CUSTOMER"]))))
