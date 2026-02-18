(ns ecommerce.integration.integration-test-helpers
  (:require
   [com.stuartsierra.component :as component]
   [ecommerce.core :as system])
  (:import (org.testcontainers.containers PostgreSQLContainer)))

(defmacro with-system
  [[bound-var binding-expr] & body]
  `(let [~bound-var (component/start ~binding-expr)]
     (try
       ~@body
       (finally
         (component/stop ~bound-var)))))

(defn test-system-config [database-container]
  {:server {:port 3001}
   :db-spec {:jdbcUrl (.getJdbcUrl database-container)
             :username (.getUsername database-container)
             :password (.getPassword database-container)}
   :auth {:jwt {:secret "123456789"
                :alg :hs512
                :expires-in 3600}}})

(defn test-system-config-with-kafka [database-container kafka-container]
  {:server {:port 3001}
   :db-spec {:jdbcUrl   (.getJdbcUrl database-container)
             :username  (.getUsername database-container)
             :password  (.getPassword database-container)}
   :auth   {:jwt {:secret     "123456789"
                  :alg        :hs512
                  :expires-in 3600}}
   :kafka  {:bootstrap-servers (.getBootstrapServers kafka-container)
            :acks             "all"
            :retries          2
            :linger.ms        1}})

(defn with-test-database [test-fn]
  (let [database-container (PostgreSQLContainer. "postgres:15.4")]
    (try
      (.start database-container)
      (with-system [sut (system/system-component (test-system-config database-container))]
        (test-fn))
      (finally (.stop database-container)))))
