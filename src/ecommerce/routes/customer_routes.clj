(ns ecommerce.routes.customer-routes
  (:require [compojure.core :refer [GET context]]
            [ecommerce.handlers.customer-handler :as customer-handler]))

(defn customer-routes []
  (context "/customer" []
    (compojure.core/routes
     (GET "/country-count" request
       (customer-handler/get-customers-country-count request))
     (GET "/age-group" request
       (customer-handler/get-customers-by-age-group request))
     (GET "/gender" request
       (customer-handler/get-customers-by-gender request))
     (GET "/registration-trend" request
       (customer-handler/get-registration-trend request))
     (GET "/:id" request
       (customer-handler/get-customer-by-id request)))))
