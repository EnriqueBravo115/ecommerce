(ns ecommerce.routes.customer-management-routes
  (:require
   [compojure.core :refer [context routes defroutes GET]]
   [ecommerce.handlers.customer-management-handler :as handler]
   [ecommerce.utils.middleware :refer [wrap-auth]]))

(defroutes customer-management-routes
  (context "/customer-management" []
    (-> (routes
         (GET "/customers-with-password-reset-code" request
           (handler/get-customers-with-password-reset-code request))
         (GET "/registration-by-country-code" request
           (handler/get-registration-by-country-code request))
         (GET "/country-count" request
           (handler/get-customers-country-count request))
         (GET "/age-group" request
           (handler/get-customers-by-age-group request))
         (GET "/active" request
           (handler/get-active request))
         (GET "/inactive" request
           (handler/get-inactive request))
         (GET "/gender/:gender" request
           (handler/get-customers-by-gender request))
         (GET "/registration-trend/:period" request
           (handler/get-registration-trend request))
         (GET "/segment-demographics/:country/:gender/:min-age/:max-age" request
           (handler/get-segment-by-demographics request))
         (GET "/:id" request
           (handler/get-customer-by-id request)))
        (wrap-auth ["ADMIN"]))))
