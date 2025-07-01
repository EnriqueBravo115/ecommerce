(ns api.core
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [next.jdbc.connection :as connection]
            [api.config :as config]
            [api.components.example-component
             :as example-component]
            [api.components.pedestal-component
             :as pedestal-component]
            [api.components.in-memory-state-component
             :as in-memory-state-component])
  (:import (com.zaxxer.hikari HikariDataSource)
           (org.flywaydb.core Flyway)))

(defn datasource-component
  [config]
  (connection/component
   HikariDataSource
   (assoc (:db-spec config)
          :init-fn (fn [datasource]
                     (log/info "Running database init")
                     (.migrate
                      (.. (Flyway/configure)
                          (dataSource datasource)
                          (locations (into-array String ["classpath:database/migrations"]))
                          (table "schema_version")
                          (load)))))))

(defn real-world-clojure-api-system
  [config]
  (component/system-map
   :example-component (example-component/new-example-component config)
   :in-memory-state-component (in-memory-state-component/new-in-memory-state-component config)
   :datasource (datasource-component config)
   :pedestal-component
   (component/using
    (pedestal-component/new-pedestal-component config)
    [:example-component
     :datasource
     :in-memory-state-component])))

(defn -main
  []
  (let [system (-> (config/read-config)
                   (real-world-clojure-api-system)
                   (component/start-system))]
    (println "Starting Real-World Clojure API Service with config")
    (.addShutdownHook
     (Runtime/getRuntime)
     (new Thread #(component/stop-system system)))))
