(ns ecommerce.components.web-server
  (:require
   [com.stuartsierra.component :as component]
   [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
   [ecommerce.utils.middleware :refer [wrap-inject-jwt wrap-datasource]]
   [ecommerce.routes.core :as routes]
   [ring.adapter.jetty :as jetty]))

(defrecord Webserver [config datasource jwt]
  component/Lifecycle

  (start [this]
    (println "Starting Webserver on port:" (-> config :server :port))
    (let [app (-> routes/api-routes
                  (wrap-inject-jwt jwt)
                  (wrap-json-body {:keywords? true})
                  (wrap-datasource datasource)
                  wrap-json-response)]
      (assoc this :server (jetty/run-jetty app {:port (-> config :server :port) :join? false}))))

  (stop [this]
    (println "Stopping Webserver")
    (some-> (:server this) .stop)
    (assoc this :server nil)))

(defn new-web-server [config]
  (map->Webserver {:config config}))
