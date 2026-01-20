(ns ecommerce.utils.password
  (:require [buddy.hashers :as hashers]))

(defn encode [password]
  (hashers/derive password))

(encode "password")

(defn check [password hashed]
  (hashers/check password hashed))
