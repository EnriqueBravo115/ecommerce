(ns ecommerce.integration.integration-test-helpers
  (:require
   [com.stuartsierra.component :as component]
   [ecommerce.core :as system]
   [jackdaw.client :as j.client]
   [jackdaw.admin :as j.admin])
  (:import (org.testcontainers.containers PostgreSQLContainer)
           (org.testcontainers.kafka KafkaContainer)))

(def ^:dynamic *kafka-bootstrap-server* nil)

(defmacro with-system
  [[bound-var binding-expr] & body]
  `(let [~bound-var (component/start ~binding-expr)]
     (try
       ~@body
       (finally
         (component/stop ~bound-var)))))

(defn test-system-config [database-container]
  {:server {:port 3001}
   :db-spec {:jdbcUrl   (.getJdbcUrl database-container)
             :username  (.getUsername database-container)
             :password  (.getPassword database-container)}
   :auth    {:jwt {:secret     "123456789"
                   :alg        :hs512
                   :expires-in 3600}}})

(defn test-system-config-with-kafka [database-container kafka-container]
  {:server {:port 3001}
   :db-spec {:jdbcUrl   (.getJdbcUrl database-container)
             :username  (.getUsername database-container)
             :password  (.getPassword database-container)}
   :auth    {:jwt {:secret     "123456789"
                   :alg        :hs512
                   :expires-in 3600}}
   :kafka   {:bootstrap-servers (.getBootstrapServers kafka-container)
             :acks              "all"
             :retries           2
             :linger.ms         1}})

(defn with-test-database [test-fn]
  (let [database-container (PostgreSQLContainer. "postgres:15.4")]
    (try
      (.start database-container)
      (with-system [sut (system/system-component (test-system-config database-container))]
        (test-fn))
      (finally (.stop database-container)))))

(defn with-test-database-and-kafka [test-fn]
  (let [database-container (PostgreSQLContainer. "postgres:15.4")
        kafka-container    (KafkaContainer. "apache/kafka:4.2.0")]
    (try
      (.start database-container)
      (.start kafka-container)
      (binding [*kafka-bootstrap-server* (.getBootstrapServers kafka-container)]
        (with-system [sut (system/system-component
                           (test-system-config-with-kafka database-container kafka-container))]
          (test-fn)))
      (finally
        (.stop database-container)
        (.stop kafka-container)))))

(defmacro with-admin-client
  [[bound-var] & body]
  `(let [~bound-var (j.admin/->AdminClient {"bootstrap.servers" *kafka-bootstrap-server*})]
     (try
       ~@body
       (finally (.close ~bound-var)))))

(defn with-test-consumer
  "Creates a consumer with a unique group-id to avoid offset collisions between tests.
   Calls (test-fn consumer)."
  [test-fn]
  (let [consumer (j.client/consumer
                  {"bootstrap.servers" *kafka-bootstrap-server*
                   "group.id"          (str "test-" (random-uuid))
                   "auto.offset.reset" "earliest"
                   "key.deserializer"  "org.apache.kafka.common.serialization.StringDeserializer"
                   "value.deserializer" "org.apache.kafka.common.serialization.StringDeserializer"})]
    (try
      (test-fn consumer)
      (finally (.close consumer)))))

(defn consume-next-message
  "Polls Kafka until one message arrives on `topic` or `timeout-ms` elapses.
   Returns the first record map, or nil on timeout."
  [consumer topic timeout-ms]
  (j.client/subscribe consumer [{:topic-name topic}])
  (let [deadline (+ (System/currentTimeMillis) timeout-ms)]
    (loop []
      (let [records (j.client/poll consumer 500)]
        (if (seq records)
          (first records)
          (when (< (System/currentTimeMillis) deadline)
            (recur)))))))
