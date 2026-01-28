(ns ecommerce.routes.shopping-cart-routes
  (:require
   [compojure.core :refer [context defroutes GET POST PUT DELETE]]
   [ecommerce.handlers.shopping-cart-handler :as handler]
   [ecommerce.utils.middleware :refer [wrap-authenticated wrap-roles]]))

(defroutes shopping-cart-routes
  (context "/cart" []
    (-> (GET "/:customer-id" request
          (handler/get-cart-by-customer-id request))
        (wrap-roles ["CUSTOMER" "ADMIN"])
        (wrap-authenticated))

    (-> (GET "/:customer-id/summary" request
          (handler/get-cart-summary request))
        (wrap-roles ["CUSTOMER" "ADMIN"])
        (wrap-authenticated))

    (-> (GET "/:customer-id/detailed" request
          (handler/get-cart-with-product-details request))
        (wrap-roles ["CUSTOMER" "ADMIN"])
        (wrap-authenticated))

    (-> (GET "/:customer-id/count" request
          (handler/get-cart-count request))
        (wrap-roles ["CUSTOMER" "ADMIN"])
        (wrap-authenticated))

    (-> (GET "/:customer-id/total" request
          (handler/get-cart-total-value request))
        (wrap-roles ["CUSTOMER" "ADMIN"])
        (wrap-authenticated))

    (-> (GET "/:customer-id/check/:product-id" request
          (handler/check-product-in-cart request))
        (wrap-roles ["CUSTOMER" "ADMIN"])
        (wrap-authenticated))

    (-> (POST "/add" request
          (handler/add-to-cart request))
        (wrap-roles ["CUSTOMER" "ADMIN"])
        (wrap-authenticated))

    (-> (PUT "/:customer-id/item/:product-id" request
          (handler/update-cart-item-quantity request))
        (wrap-roles ["CUSTOMER" "ADMIN"])
        (wrap-authenticated))

    (-> (DELETE "/:customer-id/item/:product-id" request
          (handler/remove-from-cart request))
        (wrap-roles ["CUSTOMER" "ADMIN"])
        (wrap-authenticated))

    (-> (DELETE "/:customer-id/clear" request
          (handler/clear-cart request))
        (wrap-roles ["CUSTOMER" "ADMIN"])
        (wrap-authenticated))

    (-> (POST "/:customer-id/bulk-update" request
          (handler/bulk-update-cart request))
        (wrap-roles ["CUSTOMER" "ADMIN"])
        (wrap-authenticated))

    (-> (POST "/merge" request
          (handler/merge-carts request))
        (wrap-roles ["CUSTOMER" "ADMIN"])
        (wrap-authenticated))))
