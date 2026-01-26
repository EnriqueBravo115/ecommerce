(ns ecommerce.routes.product-routes
  (:require
   [compojure.core :refer [context defroutes GET]]
   [ecommerce.handlers.product-handler :as handler]
   [ecommerce.utils.middleware :refer [wrap-authenticated wrap-roles]]))

(defroutes product-routes
  (context "/product" []
    (GET "/search" request
      (handler/search-products request))

    (GET "/sku/:sku" request
      (handler/get-product-by-sku request))

    (GET "/category/:category-id" request
      (handler/get-products-by-category request))

    (GET "/price-range" request
      (handler/get-products-by-price-range request))

    (GET "/top-viewed" request
      (handler/get-top-viewed-products request))

    (GET "/top-rated" request
      (handler/get-top-rated-products request))

    (GET "/tags/:tags" request
      (handler/get-products-by-tags request))

    (-> (GET "/:id/view" request
          (handler/increment-view-count request))
        (wrap-authenticated))

    (-> (GET "/seller/:seller-id" request
          (handler/get-products-by-seller request))
        (wrap-roles ["SELLER" "ADMIN"])
        (wrap-authenticated))

    (-> (GET "/status/:status" request
          (handler/get-products-by-status request))
        (wrap-roles ["SELLER" "ADMIN"])
        (wrap-authenticated))

    (-> (GET "/stats" request
          (handler/get-product-stats request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (GET "/:id" request
          (handler/get-product-by-id request))
        (wrap-roles ["SELLER" "ADMIN"])
        (wrap-authenticated))))
