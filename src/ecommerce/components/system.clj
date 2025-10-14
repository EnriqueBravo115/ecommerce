(ns ecommerce.components.system
  (:require [com.stuartsierra.component :as component]
            [ecommerce.components.datasource :as datasource]))

(defn system-component
  [config]
  (component/system-map
   :datasource (datasource/datasource-component config)))
