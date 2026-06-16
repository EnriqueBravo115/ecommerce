(ns auth-service.core
  (:require
   [aero.core :as aero]
   [clojure.java.io :as io]
   [com.stuartsierra.component :as component]
   [auth-service.components.datasource :as datasource]
   [auth-service.components.web-server :as web-server])
  (:gen-class))

(defn read-config
  []
  (-> "config.edn"
      (io/resource)
      (aero/read-config)))

(defn system-component [config]
  (component/system-map
   :datasource (datasource/new-datasource config)
   :web-server (component/using
                (web-server/new-web-server config)
                [:datasource])))

(defn -main
  []
  (let [system (-> (read-config)
                   (system-component)
                   (component/start-system))]
    (println "Starting auth-service API")
    (.addShutdownHook (Runtime/getRuntime) (new Thread #(component/stop-system system)))))
