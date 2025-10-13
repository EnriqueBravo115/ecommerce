(ns dev
  (:require [com.stuartsierra.component.repl :as component-repl]
            [api.components.system :as system]))

(component-repl/set-init
 (fn [_]
   (system/system-component
    {:server {:port 3001}
     :db-spec {:jdbcUrl "jdbc:postgresql://localhost:5432/rwca"
               :username "rwca"
               :password "rwca"}})))
