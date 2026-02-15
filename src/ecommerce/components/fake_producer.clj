(ns ecommerce.components.fake-producer
  (:require [com.stuartsierra.component :as component]
            [ecommerce.components.kafka-producer :refer [EventProducer]]))

(defrecord FakeProducer []
  component/Lifecycle
  (start [this] this)
  (stop [this] this)

  EventProducer
  (publish! [_ _ _ _]
    nil))

(defn new-fake-producer []
  (map->FakeProducer {}))
