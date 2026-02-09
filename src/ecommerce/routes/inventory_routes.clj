(ns ecommerce.routes.inventory-routes
  (:require
   [compojure.core :refer [context defroutes GET POST PUT routes]]
   [ecommerce.handlers.inventory-handler :as handler]
   [ecommerce.utils.middleware :refer [wrap-auth]]))

(defroutes inventory-routes
  (context "/inventory" []
    (-> (routes
         (GET "/sku/:sku" req
           (handler/get-inventory-by-sku req))
         (GET "/product/:product-id" req
           (handler/get-inventory-by-product-id req))
         (GET "/location/:location" req
           (handler/get-inventory-by-location req))
         (GET "/low-stock" req
           (handler/get-low-stock-items req))
         (GET "/below-reorder" req
           (handler/get-items-below-reorder-point req))
         (GET "/summary" req
           (handler/get-inventory-summary req))
         (GET "/available" req
           (handler/get-inventory-by-available-quantity req)))
        (wrap-auth ["SELLER" "ADMIN"])))

  (context "/inventory/admin" []
    (-> (routes
         (GET "/location-stats" req
           (handler/get-inventory-stats-by-location req))
         (GET "/recently-restocked" req
           (handler/get-recently-restocked req))
         (POST "/create" req
           (handler/create-inventory-record req))
         (PUT "/:id/quantity" req
           (handler/update-inventory-quantity req))
         (PUT "/:id/reserve" req
           (handler/reserve-inventory req))
         (PUT "/:id/release" req
           (handler/release-inventory-reservation req))
         (PUT "/:id/reorder-points" req
           (handler/update-inventory-reorder-point req))
         (GET "/:id" req
           (handler/get-inventory-by-id req)))
        (wrap-auth ["ADMIN"]))))
