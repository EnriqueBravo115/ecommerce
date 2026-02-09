(ns ecommerce.routes.inventory-routes
  (:require
   [compojure.core :refer [context defroutes GET POST PUT]]
   [ecommerce.handlers.inventory-handler :as handler]
   [ecommerce.utils.middleware :refer [wrap-auth]]))

(defroutes raw-inventory-routes-seller
  (context "/inventory" []
    (GET "/sku/:sku" request
      (handler/get-inventory-by-sku request))
    (GET "/product/:product-id" request
      (handler/get-inventory-by-product-id request))
    (GET "/location/:location" request
      (handler/get-inventory-by-location request))
    (GET "/low-stock" request
      (handler/get-low-stock-items request))
    (GET "/below-reorder" request
      (handler/get-items-below-reorder-point request))
    (GET "/summary" request
      (handler/get-inventory-summary request))
    (GET "/available" request
      (handler/get-inventory-by-available-quantity request))))

(defroutes raw-inventory-routes-admin
  (context "/inventory" []
    (GET "/location-stats" request
      (handler/get-inventory-stats-by-location request))
    (GET "/recently-restocked" request
      (handler/get-recently-restocked request))
    (POST "/create" request
      (handler/create-inventory-record request))
    (PUT "/:id/quantity" request
      (handler/update-inventory-quantity request))
    (PUT "/:id/reserve" request
      (handler/reserve-inventory request))
    (PUT "/:id/release" request
      (handler/release-inventory-reservation request))
    (PUT "/:id/reorder-points" request
      (handler/update-inventory-reorder-point request))
    (GET "/:id" request
      (handler/get-inventory-by-id request))))

(def inventory-routes-seller
  (-> raw-inventory-routes-seller
      (wrap-auth ["SELLER" "ADMIN"])))

(def inventory-routes-admin
  (-> raw-inventory-routes-admin
      (wrap-auth ["ADMIN"])))
