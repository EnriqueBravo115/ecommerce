(ns ecommerce.components.jwt
  (:require
   [buddy.auth.backends :as backends]
   [buddy.sign.jwt :as jwt]
   [clojure.tools.logging :as log]
   [com.stuartsierra.component :as component]))

(defrecord Jwt [config]
  component/Lifecycle
  (start [this]
    (let [secret (-> config :auth :jwt :secret)
          algorithm (-> config :auth :jwt :alg)
          backend (backends/token {:secret secret
                                   :options {:alg algorithm}
                                   :token-name "Bearer"
                                   :on-error (fn [request err]
                                               (println "JWT Error:" err) nil)
                                   :authfn (fn [request token]
                                             (try
                                               (jwt/unsign token secret {:alg algorithm
                                                                         :now (quot (System/currentTimeMillis) 1000)})
                                               (catch Exception e
                                                 (log/error "Token validation failed:" (.getMessage e)) nil)))})]
      (assoc this :backend backend)))
  (stop [this]
    (assoc this :backend nil)))

(defn new-jwt [config]
  (map->Jwt {:config config}))
