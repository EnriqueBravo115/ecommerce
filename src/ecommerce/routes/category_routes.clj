(ns ecommerce.routes.category-routes
  (:require
   [compojure.core :refer [context defroutes POST GET PUT DELETE]]
   [ecommerce.handlers.category-handler :as category-handler]
   [ecommerce.utils.middleware :refer [wrap-authenticated wrap-roles]]))

(defroutes raw-category-routes
  (context "/category" []
    (GET "/active" request
      (category-handler/get-active-categories request))
    (GET "/tree" request
      (category-handler/get-category-tree request))
    (GET "/stats" request
      (category-handler/get-category-statistics request))
    (POST "/create" request
      (category-handler/create-category request))
    (GET "/:id" request
      (category-handler/get-category-by-id request))
    (PUT "/:id/update" request
      (category-handler/update-category request))
    (DELETE "/:id/delete" request
      (category-handler/delete-category request))
    (POST "/:id/toggle-status" request
      (category-handler/toggle-category-status request))))

(def category-routes
  (-> raw-category-routes
      (wrap-authenticated)
      (wrap-roles ["ADMIN"])))
