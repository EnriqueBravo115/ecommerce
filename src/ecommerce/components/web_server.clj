(ns ecommerce.components.web-server
  (:require
   [com.stuartsierra.component :as component]
   [ring.adapter.jetty :as jetty]
   [ecommerce.routes.core :as routes]
   [ring.middleware.json :refer [wrap-json-body wrap-json-response]]))

(def app
  (-> routes/api-routes
      wrap-json-response
      (wrap-json-body {:keywords? true})))

(defrecord webserver [config]
  component/Lifecycle

  (start [this]
    (println "Starting Webserver on port:" (:port config))
    (assoc this :server (jetty/run-jetty app {:port (:port config) :join? false})))

  (stop [this]
    (println "Stopping Webserver")
    (some-> (:server this) .stop)
    (assoc this :server nil)))

(defn new-web-server [config]
  (map->webserver {:config config}))
