(ns ecommerce.components.web-server
  (:require
   [com.stuartsierra.component :as component]
   [ring.middleware.json :refer [wrap-json-response]]
   [ecommerce.commons.middleware :refer [wrap-datasource]]
   [ecommerce.routes.core :as routes]
   [ring.adapter.jetty :as jetty]))

(defrecord webserver [config datasource]
  component/Lifecycle

  (start [this]
    (println "Starting Webserver on port:" (-> config :server :port))
    (let [app (-> routes/api-routes
                  (wrap-datasource datasource)
                  wrap-json-response)]
      (assoc this :server (jetty/run-jetty app {:port (-> config :server :port) :join? false}))))

  (stop [this]
    (println "Stopping Webserver")
    (some-> (:server this) .stop)
    (assoc this :server nil)))

(defn new-web-server [config]
  (map->webserver {:config config}))
