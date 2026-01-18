(ns ecommerce.routes.register-routes
  (:require
   [compojure.core :refer [context defroutes POST]]
   [ecommerce.handlers.register-handler :as register-handler]))

(defroutes register-routes
  (context "/register" []
    (POST "/create-customer" request
      (register-handler/create-customer request))
    (POST "/registration-code/:email" request
      (register-handler/send-registration-code request))))
