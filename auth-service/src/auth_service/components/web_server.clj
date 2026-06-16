(ns auth-service.components.web-server
  (:require
   [com.stuartsierra.component :as component]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
   [auth-service.utils.middleware :refer [wrap-datasource]]
   [auth-service.routes.core :as routes]
   [ring.adapter.jetty :as jetty]))

(defrecord Webserver [config datasource]
  component/Lifecycle
  (start [this]
    (let [app (-> routes/api-routes
                  (wrap-json-body {:keywords? true})
                  wrap-keyword-params
                  wrap-params
                  (wrap-datasource datasource)
                  wrap-json-response)]
      (assoc this :server (jetty/run-jetty app {:port (-> config :server :port) :join? false}))))
  (stop [this]
    (some-> (:server this) .stop)
    (assoc this :server nil)))

(defn new-web-server [config]
  (map->Webserver {:config config}))
