(ns ecommerce.utils.middleware)

(defn wrap-datasource [handler datasource]
  (fn [request]
    (handler (assoc request :datasource datasource))))
