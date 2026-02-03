(ns ecommerce.routes.customer-management-routes
  (:require
   [compojure.core :refer [context defroutes GET]]
   [ecommerce.handlers.customer-management-handler :as handler]
   [ecommerce.utils.middleware :refer [wrap-authenticated wrap-roles]]))

(defroutes customer-management-routes
  (context "/customer-management" []
    (-> (GET "/customers-with-password-reset-code" request
          (handler/get-customers-with-password-reset-code request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (GET "/registration-by-country-code" request
          (handler/get-registration-by-country-code request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (GET "/country-count" request
          (handler/get-customers-country-count request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (GET "/age-group" request
          (handler/get-customers-by-age-group request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (GET "/active" request
          (handler/get-active request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (GET "/inactive" request
          (handler/get-inactive request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (GET "/gender/:gender" request
          (handler/get-customers-by-gender request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (GET "/registration-trend/:period" request
          (handler/get-registration-trend request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (GET "/segment-demographics/:country/:gender/:min-age/:max-age" request
          (handler/get-segment-by-demographics request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (GET "/:id" request
          (handler/get-customer-by-id request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))))
