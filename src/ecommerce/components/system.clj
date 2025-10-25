(ns ecommerce.components.system
  (:require [com.stuartsierra.component :as component]
            [ecommerce.components.datasource :as datasource]
            [ecommerce.components.web-server :as web-server]))

(defn system-component
  [config]
  (component/system-map
   :datasource (datasource/datasource-component config)
   :web-server (component/using
                (web-server/new-web-server config)
                [:datasource])))
