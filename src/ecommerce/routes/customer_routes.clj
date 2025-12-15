(ns ecommerce.routes.customer-routes
  (:require
   [compojure.core :refer [context defroutes GET]]
   [ecommerce.handlers.customer-handler :as customer-handler]))

(defroutes customer-routes
  (context "/customer" []
    (GET "/country-count" request
      (customer-handler/get-customers-country-count request))
    (GET "/age-group" request
      (customer-handler/get-customers-by-age-group request))
    (GET "/active-rate" request
      (customer-handler/get-active-rate request))
    (GET "/inactive" request
      (customer-handler/get-inactive request))
    (GET "/segment-demographics/:country/:gender/:age-group" request
      (customer-handler/get-segment-by-demographics request))
    (GET "/gender/:gender" request
      (customer-handler/get-customers-by-gender request))
    (GET "/registration-trend/:period" request
      (customer-handler/get-registration-trend request))
    (GET "/:id" request
      (customer-handler/get-customer-by-id request))))
