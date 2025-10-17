(ns ecommerce.routes.user-routes
  (:require
   [compojure.core :refer [GET POST DELETE PUT]]
   [ecommerce.controllers.user-controller :as user-controller]))

(def user-routes
  (compojure.core/routes
   (GET "/users" []
     (user-controller/get-users))

   (GET "/users/:id" [id]
     (user-controller/get-user id))

   (POST "/users" {body :body}
     (user-controller/create-user body))

   (DELETE "/users/:id" [id]
     (user-controller/delete-user id))))
