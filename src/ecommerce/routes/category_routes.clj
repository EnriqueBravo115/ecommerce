(ns ecommerce.routes.category-routes
  (:require
   [compojure.core :refer [context defroutes POST GET PUT DELETE]]
   [ecommerce.handlers.category-handler :as category-handler]
   [ecommerce.utils.middleware :refer [wrap-authenticated wrap-roles]]))

(defroutes category-routes
  (context "/category" []
    (GET "/active" request
      (category-handler/get-active-categories request))

    (GET "/tree" request
      (category-handler/get-category-tree request))

    (-> (GET "/stats" request
          (category-handler/get-category-statistics request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (POST "/create" request
          (category-handler/create-category request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (GET "/:id" request
          (category-handler/get-category-by-id request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (PUT "/:id/update" request
          (category-handler/update-category request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (DELETE "/:id/delete" request
          (category-handler/delete-category request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))

    (-> (POST "/:id/toggle-status" request
          (category-handler/toggle-category-status request))
        (wrap-roles ["ADMIN"])
        (wrap-authenticated))))
