(ns ecommerce.routes.address-routes
  (:require
   [compojure.core :refer [context defroutes POST GET]]
   [ecommerce.handlers.address-handler :as address-handler]))

(defroutes address-routes
  (context "/address" []
    (POST "/create-address" request
      (address-handler/create-address request))
    (POST "/update-address/:address_id" request
      (address-handler/update-address request))
    (GET "/get-customer-addresses" request
      (address-handler/get-user-addresses request))))
