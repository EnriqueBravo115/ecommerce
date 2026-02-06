(ns ecommerce.routes.address-routes
  (:require
   [compojure.core :refer [context defroutes POST GET DELETE]]
   [ecommerce.handlers.address-handler :as address-handler]
   [ecommerce.utils.middleware :refer [wrap-authenticated wrap-roles]]))

(defroutes address-routes
  (context "/address" []
    (-> (POST "/create-address" request
          (address-handler/create-address request))
        (wrap-authenticated))

    (-> (GET "/get-primary-address" request
          (address-handler/get-primary-address request))
        (wrap-authenticated))

    (-> (GET "/get-location-statistics" request
          (address-handler/get-location-statistics request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (GET "/get-customer-addresses" request
          (address-handler/get-customer-addresses request))
        (wrap-authenticated))

    (-> (GET "/get-recent-id-address" request
          (address-handler/get-recent-id-address request))
        (wrap-authenticated))

    (-> (GET "/get-customers-by-postal-code/:postal_code" request
          (address-handler/get-customers-by-postal-code request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (GET "/get-customers-by-location/:country/:state/:city" request
          (address-handler/get-customers-by-location request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (DELETE "/delete-address/:address_id" request
          (address-handler/delete-address request))
        (wrap-authenticated))

    (-> (POST "/set-primary-address/:address_id" request
          (address-handler/set-primary-address request))
        (wrap-authenticated))

    (-> (POST "/update-address/:address_id" request
          (address-handler/update-address request))
        (wrap-roles ["ADMIN" "CUSTOMER"])
        (wrap-authenticated))))
