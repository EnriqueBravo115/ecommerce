(ns ecommerce.components.datasource
  (:require [next.jdbc.connection :as connection]
            [clojure.tools.logging :as log])
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
                          (locations (into-array String ["classpath:db/migrations"]))
                          (table "schema_version")
                          (load)))))))
