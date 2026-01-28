(ns ecommerce.routes.inventory-routes
  (:require
   [compojure.core :refer [context defroutes GET POST PUT]]
   [ecommerce.handlers.inventory-handler :as handler]
   [ecommerce.utils.middleware :refer [wrap-authenticated wrap-roles]]))

(defroutes inventory-routes
  (context "/inventory" []
    (GET "/sku/:sku" request
      (handler/get-inventory-by-sku request))

    (GET "/product/:product-id" request
      (handler/get-inventory-by-product-id request))

    (GET "/location/:location" request
      (handler/get-inventory-by-location request))

    (-> (GET "/low-stock" request
          (handler/get-low-stock-items request))
        (wrap-roles ["SELLER" "ADMIN" "WAREHOUSE_MANAGER"])
        (wrap-authenticated))

    (-> (GET "/below-reorder" request
          (handler/get-items-below-reorder-point request))
        (wrap-roles ["SELLER" "ADMIN" "WAREHOUSE_MANAGER"])
        (wrap-authenticated))

    (-> (GET "/summary" request
          (handler/get-inventory-summary request))
        (wrap-roles ["SELLER" "ADMIN" "WAREHOUSE_MANAGER"])
        (wrap-authenticated))

    (-> (GET "/available" request
          (handler/get-inventory-by-available-quantity request))
        (wrap-roles ["SELLER" "ADMIN" "WAREHOUSE_MANAGER"])
        (wrap-authenticated))

    (-> (GET "/location-stats" request
          (handler/get-inventory-stats-by-location request))
        (wrap-roles ["ADMIN" "WAREHOUSE_MANAGER"])
        (wrap-authenticated))

    (-> (GET "/recently-restocked" request
          (handler/get-recently-restocked request))
        (wrap-roles ["ADMIN" "WAREHOUSE_MANAGER"])
        (wrap-authenticated))

    (-> (POST "/create" request
          (handler/create-inventory-record request))
        (wrap-roles ["ADMIN" "WAREHOUSE_MANAGER"])
        (wrap-authenticated))

    (-> (PUT "/:id/quantity" request
          (handler/update-inventory-quantity request))
        (wrap-roles ["ADMIN" "WAREHOUSE_MANAGER"])
        (wrap-authenticated))

    (-> (PUT "/:id/reserve" request
          (handler/reserve-inventory request))
        (wrap-roles ["ADMIN" "WAREHOUSE_MANAGER" "ORDER_MANAGER"])
        (wrap-authenticated))

    (-> (PUT "/:id/release" request
          (handler/release-inventory-reservation request))
        (wrap-roles ["ADMIN" "WAREHOUSE_MANAGER" "ORDER_MANAGER"])
        (wrap-authenticated))

    (-> (PUT "/:id/reorder-points" request
          (handler/update-inventory-reorder-point request))
        (wrap-roles ["ADMIN" "WAREHOUSE_MANAGER"])
        (wrap-authenticated))

    (-> (GET "/:id" request
          (handler/get-inventory-by-id request))
        (wrap-roles ["ADMIN" "WAREHOUSE_MANAGER"])
        (wrap-authenticated))))
