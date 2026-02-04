(ns ecommerce.utils.types)

(def gender-schema
  [:enum "MALE" "FEMALE"])

(def period-schema
  [:enum "YEAR" "MONTH" "DAY"])

(def address-schema
  [:map {:closed true}
   [:country [:string {:min 2 :max 100}]]
   [:state [:string {:min 2 :max 100}]]
   [:city [:string {:min 2 :max 100}]]
   [:street [:string {:min 2 :max 200}]]
   [:postal_code [:string {:min 1 :max 20}]]
   [:is_primary [:boolean]]])
