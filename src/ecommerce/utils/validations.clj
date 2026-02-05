(ns ecommerce.utils.validations
  (:require [malli.core :as m]
            [ecommerce.utils.types :as types]))

(defn validate-gender [gender]
  (when-not (m/validate types/gender-schema gender)
    "Invalid gender format, must be FEMALE-MALE"))

(defn validate-period [period]
  (when-not (m/validate types/period-schema period)
    "Invalid period format, must be YEAR-MONTH-DAY"))

(defn validate-address [address]
  (when-not (m/validate types/address-schema address)
    "Invalid address format"))

(defn validate-address-update [address]
  (when-not (m/validate types/address-schema-update address)
    "Invalid address format"))
