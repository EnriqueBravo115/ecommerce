(ns ecommerce.handlers.product-handler
  (:require
   [ecommerce.queries.product-queries :as queries]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]
   [ecommerce.utils.jwt :as jwt]))

(def ^:private json-headers {"Content-Type" "application/json"})

(defn- build-response [status body]
  {:status status :headers json-headers :body body})

(defn create-product [request]
  (let [seller-id (jwt/get-current-identity-id request)
        product-data (:body request)
        ds (:datasource request)
        sku (:sku product-data)

        existing-product (jdbc/execute-one! ds
                                            (queries/get-product-by-sku sku)
                                            {:builder-fn rs/as-unqualified-maps})]

    (cond
      (nil? seller-id)
      (build-response 403 {:error "Unauthorized"})

      existing-product
      (build-response 409 {:error "Product with this SKU already exists"})

      :else
      (let [result (jdbc/execute-one! ds
                                      (queries/create-product
                                       (assoc product-data
                                              :seller_id seller-id))
                                      {:builder-fn rs/as-unqualified-maps})]
        (build-response 201 {:id (:id result) :message "Product created successfully"})))))

;; TODO: check query(needs standard fields)
(defn update-product [request]
  (let [seller-id (jwt/get-current-identity-id request)
        product-id (Long/parseLong (get-in request [:params :product_id]))
        product-data (:body request)
        ds (:datasource request)

        existing-product (jdbc/execute-one! ds
                                            (queries/get-product-by-id product-id)
                                            {:builder-fn rs/as-unqualified-maps})]

    (cond
      (nil? existing-product)
      (build-response 404 {:error "Product not found"})

      (not= seller-id (:seller_id existing-product))
      (build-response 403 {:error "Not authorized to update this product"})

      :else
      (do
        (jdbc/execute! ds
                       (queries/update-product product-id product-data))
        (build-response 200 {:message "Product updated successfully"})))))

(defn delete-product [request]
  (let [seller-id (jwt/get-current-identity-id request)
        product-id (Long/parseLong (get-in request [:params :product_id]))
        ds (:datasource request)

        existing-product (jdbc/execute-one! ds
                                            (queries/get-product-by-id product-id)
                                            {:builder-fn rs/as-unqualified-maps})]

    (cond
      (nil? existing-product)
      (build-response 404 {:error "Product not found"})

      (not= seller-id (:seller_id existing-product))
      (build-response 403 {:error "Not authorized to delete this product"})

      :else
      (do
        (jdbc/execute! ds
                       (queries/delete-product product-id))
        (build-response 200 {:message "Product deleted successfully"})))))

;; FIX: only ADMIN
(defn get-product-by-id [request]
  (let [ds (:datasource request)
        id (Long/parseLong (get-in request [:params :id]))
        query (queries/get-product-by-id id)
        result (jdbc/execute-one! ds query {:builder-fn rs/as-unqualified-maps})]

    (if result
      (build-response 200 {:product result})
      (build-response 404 {:error "Product not found"}))))

(defn get-product-by-sku [request]
  (let [ds (:datasource request)
        sku (get-in request [:params :sku])
        query (queries/get-product-by-sku sku)
        result (jdbc/execute-one! ds query {:builder-fn rs/as-unqualified-maps})]

    (if result
      (build-response 200 {:product result})
      (build-response 404 {:error "Product not found"}))))

(defn get-products-by-seller [request]
  (let [ds (:datasource request)
        seller-id (jwt/get-current-identity-id request)
        query (queries/get-by-seller seller-id)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

    (if (seq result)
      (build-response 200 {:products result})
      (build-response 404 {:error "No products found for this seller"}))))

(defn get-products-by-category [request]
  (let [ds (:datasource request)
        category-id (get-in request [:params :category-id])
        query (queries/get-by-category category-id)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

    (if (seq result)
      (build-response 200 {:products result})
      (build-response 404 {:error "No products found in this category"}))))

(defn get-products-by-status [request]
  (let [ds (:datasource request)
        status (get-in request [:params :status])

        query (queries/get-by-status status)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

    (if (seq result)
      (build-response 200 {:products result})
      (build-response 404 {:error "No products found with this status"}))))

(defn get-products-by-price-range [request]
  (let [ds (:datasource request)
        min-price (get-in request [:params :min-price])
        max-price (get-in request [:params :max-price])
        query (queries/get-by-price-range min-price max-price)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

    (if (seq result)
      (build-response 200 {:products result})
      (build-response 404 {:error "No products found in this price range"}))))

(defn get-top-viewed-products [request]
  (let [ds (:datasource request)
        limit (or (get-in request [:params :limit]) 10)
        query (queries/get-top-viewed limit)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

    (if (seq result)
      (build-response 200 {:top-viewed result})
      (build-response 404 {:error "No products found"}))))

(defn get-top-rated-products [request]
  (let [ds (:datasource request)
        min-reviews (or (get-in request [:params :min-reviews]) 5)
        min-rating (or (get-in request [:params :min-rating]) 4.0)
        limit (or (get-in request [:params :limit]) 10)
        query (queries/get-top-rated min-reviews min-rating limit)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

    (if (seq result)
      (build-response 200 {:top-rated result})
      (build-response 404 {:error "No products found"}))))

(defn increment-view-count [request]
  (let [ds (:datasource request)
        product-id (get-in request [:params :id])
        update-query (queries/increment-view-count product-id)
        result (jdbc/execute-one! ds update-query)]

    (if (= 1 (:next.jdbc/update-count result))
      (build-response 200 {:success true :message "View count incremented"})
      (build-response 404 {:error "Product not found"}))))
