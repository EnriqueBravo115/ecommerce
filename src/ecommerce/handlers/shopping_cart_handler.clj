(ns ecommerce.handlers.shopping-cart-handler
  (:require
   [ecommerce.queries.shopping-cart-queries :as queries]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]
   [clojure.string :as str]))

(def ^:private json-headers {"Content-Type" "application/json"})

(defn- build-response [status body]
  {:status status :headers json-headers :body body})

(defn get-cart-by-customer-id [request]
  (let [ds (:datasource request)
        customer-id (get-in request [:params :customer-id])
        query (queries/get-by-customer-id customer-id)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

    (if (seq result)
      (build-response 200 {:cart-items result})
      (build-response 200 {:cart-items [] :message "Cart is empty"}))))

(defn get-cart-summary [request]
  (let [ds (:datasource request)
        customer-id (get-in request [:params :customer-id])

        items-query (queries/get-by-customer-id customer-id)
        items-result (jdbc/execute! ds items-query {:builder-fn rs/as-unqualified-maps})

        summary-query (queries/get-cart-summary customer-id)
        summary-result (jdbc/execute-one! ds summary-query {:builder-fn rs/as-unqualified-maps})]

    (build-response 200 {:cart-items items-result
                         :summary summary-result})))

(defn add-to-cart [request]
  (let [ds (:datasource request)
        {:keys [customer_id product_id quantity]} (:body request)]

    (try
      (let [query (queries/upsert-cart-item customer_id product_id quantity)
            result (jdbc/execute-one! ds query)]

        (if (= 1 (:next.jdbc/update-count result))
          (build-response 200 {:success true :message "Cart item added/updated"})
          (build-response 200 {:success true :message "Cart item added/updated"})))
      (catch Exception e
        (if (str/includes? (.getMessage e) "foreign key")
          (build-response 404 {:error "Customer or product not found"})
          (build-response 500 {:error "Failed to add item to cart"}))))))

(defn update-cart-item-quantity [request]
  (let [ds (:datasource request)
        customer-id (get-in request [:params :customer-id])
        product-id (get-in request [:params :product-id])
        {:keys [quantity]} (:body request)

        query (queries/update-quantity customer-id product-id quantity)
        result (jdbc/execute-one! ds query)]

    (if (= 1 (:next.jdbc/update-count result))
      (build-response 200 {:success true :message "Quantity updated"})
      (build-response 404 {:error "Cart item not found"}))))

(defn remove-from-cart [request]
  (let [ds (:datasource request)
        customer-id (get-in request [:params :customer-id])
        product-id (get-in request [:params :product-id])
        query (queries/delete-item customer-id product-id)
        result (jdbc/execute-one! ds query)]

    (if (= 1 (:next.jdbc/update-count result))
      (build-response 200 {:success true :message "Item removed from cart"})
      (build-response 404 {:error "Cart item not found"}))))

(defn clear-cart [request]
  (let [ds (:datasource request)
        customer-id (get-in request [:params :customer-id])
        query (queries/clear-cart customer-id)
        result (jdbc/execute-one! ds query)]

    (build-response 200 {:success true
                         :message "Cart cleared"
                         :items-removed (:next.jdbc/update-count result)})))

(defn get-cart-count [request]
  (let [ds (:datasource request)
        customer-id (get-in request [:params :customer-id])
        query (queries/get-cart-count customer-id)
        result (jdbc/execute-one! ds query {:builder-fn rs/as-unqualified-maps})]

    (build-response 200 {:customer-id customer-id
                         :item-count (:item_count result 0)})))

(defn get-cart-total-value [request]
  (let [ds (:datasource request)
        customer-id (get-in request [:params :customer-id])
        query (queries/get-cart-total-value customer-id)
        result (jdbc/execute-one! ds query {:builder-fn rs/as-unqualified-maps})]

    (build-response 200 {:customer-id customer-id
                         :total-value (:total_value result 0)
                         :formatted-total (format "%.2f" (or (:total_value result) 0))})))

(defn merge-carts [request]
  (let [ds (:datasource request)
        {:keys [source_customer_id target_customer_id]} (:body request)]

    (try
      (jdbc/with-transaction [tx ds]
        (let [source-items-query (queries/get-by-customer-id source_customer_id)
              source-items (jdbc/execute! tx source-items-query {:builder-fn rs/as-unqualified-maps})]

          (doseq [item source-items]
            (let [upsert-query (queries/upsert-cart-item
                                target_customer_id
                                (:product_id item)
                                (:quantity item))]
              (jdbc/execute-one! tx upsert-query)))

          (let [clear-query (queries/clear-cart source_customer_id)
                clear-result (jdbc/execute-one! tx clear-query)]

            (build-response 200 {:success true
                                 :message "Carts merged successfully"
                                 :items-moved (count source-items)}))))
      (catch Exception e
        (build-response 500 {:error "Failed to merge carts"})))))

(defn get-cart-with-product-details [request]
  (let [ds (:datasource request)
        customer-id (get-in request [:params :customer-id])
        query (queries/get-cart-with-products customer-id)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

    (if (seq result)
      (build-response 200 {:cart-items result})
      (build-response 200 {:cart-items [] :message "Cart is empty"}))))

(defn check-product-in-cart [request]
  (let [ds (:datasource request)
        customer-id (get-in request [:params :customer-id])
        product-id (get-in request [:params :product-id])
        query (queries/get-item customer-id product-id)
        result (jdbc/execute-one! ds query {:builder-fn rs/as-unqualified-maps})]

    (if result
      (build-response 200 {:in-cart true :item result})
      (build-response 200 {:in-cart false}))))

(defn bulk-update-cart [request]
  (let [ds (:datasource request)
        customer-id (get-in request [:params :customer-id])
        items (:body request)]

    (try
      (jdbc/with-transaction [tx ds]
        (jdbc/execute-one! tx (queries/clear-cart customer-id))

        (doseq [{:keys [product_id quantity]} items]
          (when (and product_id quantity (> quantity 0))
            (let [query (queries/insert-cart-item customer-id product_id quantity)]
              (jdbc/execute-one! tx query))))

        (build-response 200 {:success true
                             :message "Cart updated successfully"
                             :items-updated (count items)}))
      (catch Exception e
        (build-response 500 {:error "Failed to update cart"})))))
