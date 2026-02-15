(ns ecommerce.components.kafka-producer
  (:require
   [com.stuartsierra.component :as component]
   [jackdaw.client :as j.client]))

(defrecord KafkaProducer [config producer]
  component/Lifecycle

  (start [this]
    (if producer
      this
      (let [producer-config
            (merge
             {"bootstrap.servers" (-> config :kafka :bootstrap-servers)
              "acks" "all"
              "client.id" "ecommerce-producer"
              "key.serializer" "org.apache.kafka.common.serialization.StringSerializer"
              "value.serializer" "org.apache.kafka.common.serialization.StringSerializer"}

             (-> config :kafka :producer-opts))]
        (assoc this
               :producer
               (j.client/producer producer-config)))))

  (stop [this]
    (when producer
      (.close producer))
    (assoc this :producer nil)))

(defn new-kafka-producer [config]
  (map->KafkaProducer {:config config}))

(defprotocol EventProducer
  (publish! [this topic key value]))
