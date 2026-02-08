(ns ecommerce.routes.seller-routes
  (:require
   [compojure.core :refer [context defroutes POST GET PUT DELETE]]
   [ecommerce.handlers.seller-handler :as seller-handler]
   [ecommerce.utils.middleware :refer [wrap-authenticated wrap-roles]]))

(defroutes raw-seller-routes
  (context "/seller" []
    (POST "/create-seller" request
      (seller-handler/create-seller request))
    (GET "/get-top-sellers" request
      (seller-handler/get-top-sellers request))
    (GET "/get-unverified-sellers" request
      (seller-handler/get-unverified-sellers request))
    (GET "/get-sellers-by-country-stats" request
      (seller-handler/get-seller-by-country-stats request))
    (GET "/get-sellers-by-status/:status" request
      (seller-handler/get-sellers-by-status request))
    (DELETE "/delete-seller/:seller_id" request
      (seller-handler/delete-seller request))
    (PUT "/verify-seller/:seller_id" request
      (seller-handler/verify-seller request))
    (PUT "/update-seller-status/:seller_id" request
      (seller-handler/update-seller-status request))
    (PUT "/update-seller-location/:seller_id" request
      (seller-handler/update-seller-location request))
    (GET "/get-seller-by-id/:id" request
      (seller-handler/get-seller-by-id request))))

(def seller-routes
  (-> raw-seller-routes
      wrap-authenticated
      (wrap-roles ["ADMIN"])))
