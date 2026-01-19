(ns ecommerce.routes.address-routes
  (:require
   [compojure.core :refer [context defroutes POST]]
   [ecommerce.handlers.address-handler :as address-handler]))

(defroutes address-routes
  (context "/address" []
    (POST "/create-address" request
      (address-handler/create-address request))))
