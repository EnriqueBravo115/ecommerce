(ns ecommerce.routes.seller-routes
  (:require
   [compojure.core :refer [context defroutes POST GET PUT DELETE]]
   [ecommerce.handlers.seller-handler :as seller-handler]
   [ecommerce.utils.middleware :refer [wrap-authenticated wrap-roles]]))

(defroutes seller-routes
  (context "/seller" []
    (-> (POST "/create-seller" request
          (seller-handler/create-seller request))
        (wrap-authenticated))

    (-> (DELETE "/delete-seller/:seller_id" request
          (seller-handler/delete-seller request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (PUT "/update-seller-status/:seller_id" request
          (seller-handler/update-seller-status request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (PUT "/update-seller-location/:seller_id" request
          (seller-handler/update-seller-location request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (GET "/get-seller-by-id/:id" request
          (seller-handler/get-seller-by-id request))
        (wrap-authenticated))))
