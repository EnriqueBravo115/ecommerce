(ns ecommerce.routes.category-routes
  (:require
   [compojure.core :refer [context routes defroutes POST GET PUT DELETE]]
   [ecommerce.handlers.category-handler :as category-handler]
   [ecommerce.utils.middleware :refer [wrap-auth]]))

(defroutes category-routes
  (context "/category" []
    (-> (routes
         (GET "/stats" request
           (category-handler/get-category-statistics request))
         (POST "/create" request
           (category-handler/create-category request))
         (GET "/:id" request
           (category-handler/get-category-by-id request))
         (PUT "/update/:id" request
           (category-handler/update-category request))
         (DELETE "/delete/:id" request
           (category-handler/delete-category request)))
        (wrap-auth ["ADMIN"]))))
