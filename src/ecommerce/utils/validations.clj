(ns ecommerce.utils.validations
  (:require [malli.core :as m]
            [malli.util :as mu]
            [ecommerce.utils.types :as types]))

(defn validate-gender [gender]
  (when-not (m/validate types/Gender gender)
    "Invalid gender format, must be FEMALE or MALE"))
