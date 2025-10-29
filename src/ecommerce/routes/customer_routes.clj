(ns ecommerce.routes.customer-routes
  (:require
   [compojure.core :refer [GET defroutes context]]
   [ecommerce.handlers.customer-handler :as customer-handler]))

(defroutes customer-routes
  (context "/customer" []
    (GET "/country-count" request
      (customer-handler/get-customers-country-count request))
    (GET "/age-group" request
      (customer-handler/get-customers-by-age-group request))
    (GET "/:id" request
      (customer-handler/get-customer-by-id request))))
