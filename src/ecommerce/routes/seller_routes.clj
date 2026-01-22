(ns ecommerce.routes.seller-routes
  (:require
   [compojure.core :refer [context defroutes POST GET PUT DELETE]]
   [ecommerce.handlers.seller-handler :as seller-handler]))

(defroutes seller-routes
  (context "/seller" []
    (POST "/create-seller" request
      (seller-handler/create-seller request))
    (DELETE "/delete-seller/:seller_id" request
      (seller-handler/delete-seller request))
    (PUT "/update-seller-status/:seller_id" request
      (seller-handler/update-seller-status request))
    (PUT "/update-seller-location/:seller_id" request
      (seller-handler/update-seller-location request))
    (GET "/get-seller-by-id/:seller_id" request
      (seller-handler/get-seller-by-id request))))
