(ns ecommerce.routes.customer-routes
  (:require
   [compojure.core :refer [GET defroutes]]
   [ecommerce.handlers.user-handler :as user-handler]))

(defroutes customer-routes
  (GET "/users/:id" request
    (user-handler/get-user request)))
