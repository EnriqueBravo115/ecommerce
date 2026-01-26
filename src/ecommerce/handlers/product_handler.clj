(ns ecommerce.handlers.product-handler
  (:require
   [ecommerce.queries.product-queries :as queries]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]
   [clojure.string :as str]))

(def ^:private json-headers {"Content-Type" "application/json"})

(defn- build-response [status body]
  {:status status :headers json-headers :body body})

(defn get-product-by-id [request]
  (let [ds (:datasource request)
        id (get-in request [:params :id])
        query (queries/get-by-id id)
        result (jdbc/execute-one! ds query {:builder-fn rs/as-unqualified-maps})]

    (if result
      (build-response 200 {:product result})
      (build-response 404 {:error "Product not found"}))))

(defn get-product-by-sku [request]
  (let [ds (:datasource request)
        sku (get-in request [:params :sku])
        query (queries/get-by-sku sku)
        result (jdbc/execute-one! ds query {:builder-fn rs/as-unqualified-maps})]

    (if result
      (build-response 200 {:product result})
      (build-response 404 {:error "Product not found"}))))

(defn get-products-by-seller [request]
  (let [ds (:datasource request)
        seller-id (get-in request [:params :seller-id])
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

(defn get-products-by-brand [request]
  (let [ds (:datasource request)
        brand (get-in request [:params :brand])
        query (queries/get-by-brand brand)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

    (if (seq result)
      (build-response 200 {:products result})
      (build-response 404 {:error "No products found for this brand"}))))

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

(defn get-products-by-tags [request]
  (let [ds (:datasource request)
        tags-param (get-in request [:params :tags])
        tags (when tags-param (str/split tags-param #","))
        query (queries/get-by-tags tags)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

    (if (seq result)
      (build-response 200 {:products result})
      (build-response 404 {:error "No products found with these tags"}))))

(defn get-product-stats [request]
  (let [ds (:datasource request)
        stats-query (queries/get-product-stats)
        stats-result (jdbc/execute-one! ds stats-query {:builder-fn rs/as-unqualified-maps})

        by-status-query (queries/get-count-by-status)
        by-status-result (jdbc/execute! ds by-status-query {:builder-fn rs/as-unqualified-maps})

        by-category-query (queries/get-count-by-category)
        by-category-result (jdbc/execute! ds by-category-query {:builder-fn rs/as-unqualified-maps})]

    (if stats-result
      (build-response 200 {:stats stats-result
                           :by-status by-status-result
                           :by-category by-category-result})
      (build-response 404 {:error "No product statistics available"}))))

(defn search-products [request]
  (let [ds (:datasource request)
        search-term (get-in request [:params :q])
        query (queries/search search-term)
        result (jdbc/execute! ds query {:builder-fn rs/as-unqualified-maps})]

    (if (seq result)
      (build-response 200 {:products result})
      (build-response 404 {:error "No products found matching search term"}))))

(defn increment-view-count [request]
  (let [ds (:datasource request)
        product-id (get-in request [:params :id])
        update-query (queries/increment-view-count product-id)
        result (jdbc/execute-one! ds update-query)]

    (if (= 1 (:next.jdbc/update-count result))
      (build-response 200 {:success true :message "View count incremented"})
      (build-response 404 {:error "Product not found"}))))
