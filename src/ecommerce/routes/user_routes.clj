(ns ecommerce.routes.user-routes
  (:require
   [compojure.core :refer [GET defroutes]]
   [ecommerce.handlers.user-handler :as user-handler]))

(defroutes user-routes
  (GET "/users/:id" request
    (user-handler/get-user request)))
