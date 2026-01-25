(ns ecommerce.routes.seller-routes
  (:require
   [compojure.core :refer [context defroutes POST GET PUT DELETE]]
   [ecommerce.handlers.seller-handler :as seller-handler]
   [ecommerce.utils.middleware :refer [wrap-authenticated wrap-roles]]))

(defroutes seller-routes
  (context "/seller" []
    (-> (POST "/create-seller" request
          (seller-handler/create-seller request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (GET "/get-top-sellers" request
          (seller-handler/get-top-sellers request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (GET "/get-unverified-sellers" request
          (seller-handler/get-unverified-sellers request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (GET "/get-sellers-by-country-stats" request
          (seller-handler/get-seller-by-country-stats request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (GET "/get-sellers-by-status/:status" request
          (seller-handler/get-sellers-by-status request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (DELETE "/delete-seller/:seller_id" request
          (seller-handler/delete-seller request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (PUT "/verify-seller/:seller_id" request
          (seller-handler/verify-seller request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (PUT "/update-seller-status/:seller_id" request
          (seller-handler/update-seller-status request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (PUT "/update-seller-location/:seller_id" request
          (seller-handler/update-seller-location request))
        (wrap-roles ["ADMIN" "SELLER"])
        (wrap-authenticated))

    (-> (GET "/get-seller-by-id/:id" request
          (seller-handler/get-seller-by-id request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))))
