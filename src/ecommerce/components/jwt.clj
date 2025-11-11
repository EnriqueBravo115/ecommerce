(ns ecommerce.components.jwt
  (:require [com.stuartsierra.component :as component]))

(defrecord Jwt [config jwt-backend])
