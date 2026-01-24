(ns ecommerce.utils.password
  (:require [buddy.hashers :as hashers]))

(defn encode [password]
  (hashers/derive password))

(defn check [password hashed]
  (hashers/check password hashed))
