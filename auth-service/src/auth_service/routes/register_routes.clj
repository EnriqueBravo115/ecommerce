(ns auth-service.routes.register-routes
  (:require
   [auth-service.handlers.register-handler :as register-handler]))

(def register-routes
  ["/register"
   ["/create-customer" {:post register-handler/create-customer}]
   ["/end-registration" {:post register-handler/end-registration}]
   ["/check-registration-code/:code" {:post register-handler/check-registration-code}]
   ["/registration-code/:email" {:post register-handler/send-registration-code}]])
