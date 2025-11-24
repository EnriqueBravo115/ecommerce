(ns ecommerce.components.jwt
  (:require [com.stuartsierra.component :as component]
            [buddy.auth.backends :as backends]))

(defrecord Jwt [config]
  component/Lifecycle
  (start [this]
    (let [secret (-> config :auth :jwt :secret)
          algorithm (-> config :auth :jwt :algorithm)
          backend (backends/token {:secret secret
                                   :options {:alg algorithm}
                                   :on-error (fn [request err]
                                               (println "JWT Error:" err)
                                               nil)
                                   :token-name "Bearer"
                                   :authfn (fn [request token]
                                             (println "Validating token:" token)
                                             token)})]
      (assoc this :backend backend)))
  (stop [this]
    (dissoc this :backend)))

(defn new-jwt [config]
  (map->Jwt {:config config}))
