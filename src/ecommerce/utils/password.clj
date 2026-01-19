(ns ecommerce.utils.password
  (:require [buddy.hashers :as hashers]))

(defn encode [password]
  (hashers/derive password))

(encode "mypassword2024")

(defn check [password hashed]
  (hashers/check password hashed))

(check "mypassword2024" "bcrypt+sha512$42db655ee7cf336edf1b5a6dd17db401$12$a6ffc21d9c0220812eefda31698dde1550a60d2e1265ca91")
