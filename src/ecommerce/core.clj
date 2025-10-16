(ns ecommerce.core
  (:require
   [aero.core :as aero]
   [clojure.java.io :as io]
   [com.stuartsierra.component :as component]
   [ecommerce.components.system :as system]))

(defn read-config
  []
  (-> "config.edn"
      (io/resource)
      (aero/read-config)))

(defn -main
  []
  (let [system (-> (read-config)
                   (system/system-component)
                   (component/start-system))]
    (println "Starting ecommerce API")
    (.addShutdownHook (Runtime/getRuntime) (new Thread #(component/stop-system system)))))
