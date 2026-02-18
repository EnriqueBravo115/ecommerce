(ns ecommerce.integration.product-test-helpers
  (:require
   [clj-http.client :as client]))

(defn valid-product []
  {:category_id       1
   :sku               "LAPTOP-001"
   :name              "Gaming Laptop RTX 5080"
   :description       "High performance gaming laptop 2026 edition"
   :short_description "RTX 50-series Gaming"
   :price             25999.99
   :compare_at_price  28999.99
   :cost_price        20500.00
   :brand             "Asus ROG"
   :weight            2.8
   :weight_unit       "kg"
   :status            "active"
   :condition         "new"
   :tags              "laptop,gaming,rtx,2026"})

(defn post-product
  ([product] (post-product product {}))
  ([product extra-opts]
   (client/post "http://localhost:3001/api/v1/product/create"
                (merge {:accept           :json
                        :content-type     :json
                        :throw-exceptions false
                        :form-params      product}
                       extra-opts))))
