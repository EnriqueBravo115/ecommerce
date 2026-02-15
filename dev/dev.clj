(ns dev
  (:require [com.stuartsierra.component.repl :as component-repl]
            [ecommerce.core :as system]))

(component-repl/set-init
 (fn [_]
   (system/system-component
    {:server {:port 3000}

     :db-spec {:jdbcUrl  "jdbc:postgresql://localhost:5432/ecommerce"
               :username "ecommerce"
               :password "ecommerce"}

     :auth {:jwt
            {:secret     "123456789"
             :alg        :hs512
             :expires-in 3600}}

     :kafka {:bootstrap-servers "localhost:9092"
             :producer-opts {"acks" "all"
                             "retries" "3"
                             "linger.ms" "10"
                             "enable.idempotence" "true"}}})))
