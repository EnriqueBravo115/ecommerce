(ns ecommerce.components.jwt
  (:require [com.stuartsierra.component :as component]
            [buddy.auth.backends.token :refer [jws-backend]]))

(defrecord Jwt [config]
  component/Lifecycle
  (start [this]
    (let [backend (jws-backend {:secret (-> config :auth :jwt :secret-key)
                                :options {:alg (-> config :auth :jwt :algorithm)}})]
      (assoc this :backend backend)))
  (stop [this]
    (dissoc this :backend)))

(defn new-jwt [config]
  (map->Jwt {:config config}))
