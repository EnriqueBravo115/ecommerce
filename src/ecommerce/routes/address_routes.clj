(ns ecommerce.routes.address-routes
  (:require
   [compojure.core :refer [context routes defroutes POST GET DELETE]]
   [ecommerce.handlers.address-handler :as address-handler]
   [ecommerce.utils.middleware :refer [wrap-auth]]))

(defroutes address-routes
  (context "/address" []
    (-> (routes
         (POST "/create-address" req
           (address-handler/create-address req))
         (GET "/get-primary-address" req
           (address-handler/get-primary-address req))
         (GET "/get-customer-addresses" req
           (address-handler/get-customer-addresses req))
         (GET "/get-recent-id-address" req
           (address-handler/get-recent-id-address req))
         (DELETE "/delete-address/:address_id" req
           (address-handler/delete-address req))
         (POST "/set-primary-address/:address_id" req
           (address-handler/set-primary-address req))
         (POST "/update-address/:address_id" req
           (address-handler/update-address req)))
        (wrap-auth ["CUSTOMER" "ADMIN"])))

  (context "/address/admin" []
    (-> (routes
         (GET "/get-location-statistics" req
           (address-handler/get-location-statistics req))
         (GET "/get-customers-by-postal-code/:postal_code" req
           (address-handler/get-customers-by-postal-code req))
         (GET "/get-customers-by-location/:country/:state/:city" req
           (address-handler/get-customers-by-location req)))
        (wrap-auth ["ADMIN"]))))
