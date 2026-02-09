(ns ecommerce.routes.address-routes
  (:require
   [compojure.core :refer [context defroutes POST GET DELETE]]
   [ecommerce.handlers.address-handler :as address-handler]
   [ecommerce.utils.middleware :refer [wrap-auth]]))

(defroutes raw-address-routes-customer
  (context "/address" []
    (POST "/create-address" request
      (address-handler/create-address request))
    (GET "/get-primary-address" request
      (address-handler/get-primary-address request))
    (GET "/get-customer-addresses" request
      (address-handler/get-customer-addresses request))
    (GET "/get-recent-id-address" request
      (address-handler/get-recent-id-address request))
    (DELETE "/delete-address/:address_id" request
      (address-handler/delete-address request))
    (POST "/set-primary-address/:address_id" request
      (address-handler/set-primary-address request))
    (POST "/update-address/:address_id" request
      (address-handler/update-address request))))

(defroutes raw-address-routes-admin
  (context "/address" []
    (GET "/get-location-statistics" request
      (address-handler/get-location-statistics request))
    (GET "/get-customers-by-postal-code/:postal_code" request
      (address-handler/get-customers-by-postal-code request))
    (GET "/get-customers-by-location/:country/:state/:city" request
      (address-handler/get-customers-by-location request))))

(def address-routes-customer
  (-> raw-address-routes-customer
      (wrap-auth ["CUSTOMER" "ADMIN"])))

(def address-routes-admin
  (-> raw-address-routes-admin
      (wrap-auth ["ADMIN"])))
