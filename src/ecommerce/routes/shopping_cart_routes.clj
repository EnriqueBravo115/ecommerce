(ns ecommerce.routes.shopping-cart-routes
  (:require
   [compojure.core :refer [context defroutes GET POST PUT DELETE]]
   [ecommerce.handlers.shopping-cart-handler :as handler]
   [ecommerce.utils.middleware :refer [wrap-auth]]))

(defroutes raw-shopping-cart-routes
  (context "/cart" []
    (GET "/:customer-id" request
      (handler/get-cart-by-customer-id request))
    (GET "/:customer-id/summary" request
      (handler/get-cart-summary request))
    (GET "/:customer-id/detailed" request
      (handler/get-cart-with-product-details request))
    (GET "/:customer-id/count" request
      (handler/get-cart-count request))
    (GET "/:customer-id/total" request
      (handler/get-cart-total-value request))
    (GET "/:customer-id/check/:product-id" request
      (handler/check-product-in-cart request))
    (POST "/add" request
      (handler/add-to-cart request))
    (PUT "/:customer-id/item/:product-id" request
      (handler/update-cart-item-quantity request))
    (DELETE "/:customer-id/item/:product-id" request
      (handler/remove-from-cart request))
    (DELETE "/:customer-id/clear" request
      (handler/clear-cart request))
    (POST "/:customer-id/bulk-update" request
      (handler/bulk-update-cart request))
    (POST "/merge" request
      (handler/merge-carts request))))

(def shopping-cart-routes
  (-> raw-shopping-cart-routes
      (wrap-auth ["ADMIN" "CUSTOMER"])))
