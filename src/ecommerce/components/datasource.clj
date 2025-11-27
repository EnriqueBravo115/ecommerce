(ns ecommerce.components.datasource
  (:require [com.stuartsierra.component :as component]
            [next.jdbc.connection :as connection])
  (:import (com.zaxxer.hikari HikariDataSource)
           (org.flywaydb.core Flyway)))

(defrecord Datasource [config datasource]
  component/Lifecycle
  (start [this]
    (let [ds (connection/->pool HikariDataSource (:db-spec config))]
      (.migrate
       (.. (Flyway/configure)
           (dataSource ds)
           (locations (into-array String ["classpath:db/migrations"]))
           (table "schema_version")
           (load)))
      (assoc this :datasource ds)))
  (stop [this]
    (when datasource
      (.close datasource))
    (assoc this :datasource nil)))

(defn new-datasource [config]
  (map->Datasource {:config config}))
