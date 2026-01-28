(ns ecommerce.handlers.inventory-handler
  (:require
   [ecommerce.queries.inventory-queries :as queries]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]
   [clojure.string :as str]))

(def ^:private json-headers {"Content-Type" "application/json"})

(defn- build-response [status body]
  {:status status :headers json-headers :body body})

(defn get-inventory-by-id [request]
  (let [ds (:datasource request)
        id (get-in request [:params :id])
        query (queries/get-by-id id)
        result (jdbc/execute-one! ds query {:builder-fn rs/as-unqualified-maps})]

    (if result
      (build-response 200 {:inventory result})
      (build-response 404 {:error "Inventory record not found"}))))

(defn get-inventory-by-sku [request]
  (let [ds (:datasource request)
        sku (get-in request [:params :sku])
        query (queries/get-by-sku sku)
        result (jdbc/execute-one! ds query {:builder-fn rs/as-unqualified-maps})]

    (if result
      (build-response 200 {:inventory result})
      (build-response 404 {:error "Inventory record not found"}))))

(defn get-inventory-by-product-id [request]
  (let [ds (:datasource request)
        product-id (get-in request [:params :product-id])
        query (queries/get-by-product-id product-id)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

    (if (seq result)
      (build-response 200 {:inventory result})
      (build-response 404 {:error "No inventory records found for this product"}))))

(defn get-inventory-by-location [request]
  (let [ds (:datasource request)
        location (get-in request [:params :location])
        query (queries/get-by-location location)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

    (if (seq result)
      (build-response 200 {:inventory result})
      (build-response 404 {:error "No inventory records found for this location"}))))

(defn get-low-stock-items [request]
  (let [ds (:datasource request)
        query (queries/get-low-stock)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

    (if (seq result)
      (build-response 200 {:low-stock-items result})
      (build-response 404 {:error "No low stock items found"}))))

(defn get-items-below-reorder-point [request]
  (let [ds (:datasource request)
        query (queries/get-below-reorder-point)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

    (if (seq result)
      (build-response 200 {:items-below-reorder-point result})
      (build-response 404 {:error "No items below reorder point found"}))))

(defn get-inventory-summary [request]
  (let [ds (:datasource request)
        query (queries/get-summary)
        result (jdbc/execute-one! ds query {:builder-fn rs/as-unqualified-maps})]

    (if result
      (build-response 200 {:summary result})
      (build-response 404 {:error "No inventory data found"}))))

(defn get-inventory-by-available-quantity [request]
  (let [ds (:datasource request)
        min-available (or (get-in request [:params :min-available]) 0)
        max-available (get-in request [:params :max-available])
        query (queries/get-by-available-quantity min-available max-available)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

    (if (seq result)
      (build-response 200 {:inventory result})
      (build-response 404 {:error "No inventory records found with this availability"}))))

(defn get-inventory-stats-by-location [request]
  (let [ds (:datasource request)
        query (queries/get-stats-by-location)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

    (if (seq result)
      (build-response 200 {:location-stats result})
      (build-response 404 {:error "No inventory location statistics found"}))))

(defn get-recently-restocked [request]
  (let [ds (:datasource request)
        days (or (get-in request [:params :days]) 7)
        query (queries/get-recently-restocked days)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

    (if (seq result)
      (build-response 200 {:recently-restocked result})
      (build-response 404 {:error "No recently restocked items found"}))))

(defn update-inventory-quantity [request]
  (let [ds (:datasource request)
        id (get-in request [:params :id])
        {:keys [quantity reserved]} (:body request)
        query (queries/update-quantity id quantity reserved)
        result (jdbc/execute-one! ds query)]

    (if (= 1 (:next.jdbc/update-count result))
      (build-response 200 {:success true :message "Inventory updated"})
      (build-response 404 {:error "Inventory record not found"}))))

(defn reserve-inventory [request]
  (let [ds (:datasource request)
        id (get-in request [:params :id])
        amount (get-in request [:body :amount])

        query (queries/reserve-item id amount)
        result (jdbc/execute-one! ds query)]

    (if (= 1 (:next.jdbc/update-count result))
      (build-response 200 {:success true :message "Inventory reserved"})
      (build-response 400 {:error "Insufficient available inventory or record not found"}))))

(defn release-inventory-reservation [request]
  (let [ds (:datasource request)
        id (get-in request [:params :id])
        amount (get-in request [:body :amount])

        query (queries/release-reservation id amount)
        result (jdbc/execute-one! ds query)]

    (if (= 1 (:next.jdbc/update-count result))
      (build-response 200 {:success true :message "Inventory reservation released"})
      (build-response 400 {:error "Invalid release amount or record not found"}))))

(defn create-inventory-record [request]
  (let [ds (:datasource request)
        inventory-data (:body request)
        {:keys [product_id sku]} inventory-data]

    (try
      (let [query (queries/create inventory-data)
            result (jdbc/execute-one! ds (assoc query :return-keys true))]
        (build-response 201 {:success true :message "Inventory record created" :id (:inventory/id result)}))
      (catch Exception e
        (if (str/includes? (.getMessage e) "duplicate key")
          (build-response 409 {:error "SKU already exists"})
          (build-response 500 {:error "Failed to create inventory record"}))))))

(defn update-inventory-reorder-point [request]
  (let [ds (:datasource request)
        id (get-in request [:params :id])
        {:keys [reorder_point low_stock_threshold]} (:body request)

        query (queries/update-reorder-points id reorder_point low_stock_threshold)
        result (jdbc/execute-one! ds query)]

    (if (= 1 (:next.jdbc/update-count result))
      (build-response 200 {:success true :message "Reorder points updated"})
      (build-response 404 {:error "Inventory record not found"}))))
