(ns ecommerce.core
  (:require
   [aero.core :as aero]
   [clojure.java.io :as io]
   [com.stuartsierra.component :as component]
   [ecommerce.components.datasource :as datasource]
   [ecommerce.components.web-server :as web-server]) (:gen-class))

(defn read-config
  []
  (-> "config.edn"
      (io/resource)
      (aero/read-config)))

(defn system-component
  [config]
  (component/system-map
   :datasource (datasource/datasource-component config)
   :web-server (component/using
                (web-server/new-web-server config)
                [:datasource])))

(defn -main
  []
  (let [system (-> (read-config)
                   (system-component)
                   (component/start-system))]
    (println "Starting ecommerce API")
    (.addShutdownHook (Runtime/getRuntime) (new Thread #(component/stop-system system)))))
