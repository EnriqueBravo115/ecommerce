(ns dev
  (:require [com.stuartsierra.component.repl :as component-repl]
            [ecommerce.core :as system]))

(component-repl/set-init
 (fn [_]
   (system/system-component
    {:server {:port 3000}
     :db-spec {:jdbcUrl "jdbc:postgresql://localhost:5432/ecommerce"
               :username "ecommerce"
               :password "ecommerce"}})))
