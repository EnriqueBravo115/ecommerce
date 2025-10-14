(ns ecommerce.core
  (:require [com.stuartsierra.component :as component]
            [ecommerce.config :as config]
            [ecommerce.components.system :as system]))

(defn -main
  []
  (let [system (-> (config/read-config)
                   (system/system-component)
                   (component/start-system))]
    (println "Starting ecommerce API")
    (.addShutdownHook (Runtime/getRuntime) (new Thread #(component/stop-system system)))))
