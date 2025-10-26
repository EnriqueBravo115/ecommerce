(ns ecommerce.routes.customer-routes
  (:require
   [compojure.core :refer [GET defroutes]]
   [ecommerce.handlers.customer-handler :as customer-handler]))

(defroutes customer-routes
  (GET "/customer/basic/:id" request
    (customer-handler/get-customer-basic request)))
