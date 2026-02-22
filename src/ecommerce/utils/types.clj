(ns ecommerce.utils.types)

(def gender-schema
  [:enum "MALE" "FEMALE"])

(def period-schema
  [:enum "YEAR" "MONTH" "DAY"])

(def product-status-schema
  [:enum "active" "inactive" "draft" "suspended" "pending"])

(def product-condition-schema
  [:enum "new" "used" "refurbished"])

(def weight-unit-schema
  [:enum "kg" "g" "lb"])

(def seller-status-schema
  [:enum "PENDING" "ACTIVE" "SUSPENDED" "REJECTED"])

(def address-schema
  [:map {:closed true}
   [:country [:string {:min 2 :max 100}]]
   [:state [:string {:min 2 :max 100}]]
   [:city [:string {:min 2 :max 100}]]
   [:street [:string {:min 2 :max 200}]]
   [:postal_code [:string {:min 1 :max 20}]]
   [:is_primary [:boolean]]])

(def address-schema-update
  [:map {:closed true}
   [:country [:string {:min 2 :max 100}]]
   [:state [:string {:min 2 :max 100}]]
   [:city [:string {:min 2 :max 100}]]
   [:street [:string {:min 2 :max 200}]]
   [:postal_code [:string {:min 1 :max 20}]]])

(def product-schema
  [:map {:closed true}
   [:category_id pos-int?]
   [:sku [:string {:min 3 :max 100}]]
   [:name [:string {:min 3 :max 200}]]
   [:description [:string {:min 5 :max 2000}]]
   [:short_description [:string {:min 3 :max 500}]]
   [:price [:double {:min 0}]]
   [:compare_at_price [:double {:min 0}]]
   [:cost_price [:double {:min 0}]]
   [:brand [:string {:min 2 :max 100}]]
   [:weight [:double {:min 0}]]
   [:weight_unit weight-unit-schema]
   [:status product-status-schema]
   [:condition product-condition-schema]
   [:tags [:string {:min 1 :max 500}]]])

(def category-schema
  [:map {:closed true}
   [:name [:string {:min 3 :max 100}]]
   [:parent_id [:maybe pos-int?]]
   [:active [:boolean]]])

(def seller-schema
  [:map {:closed true}
   [:business_name [:string {:min 2 :max 200}]]
   [:legal_name [:string {:min 2 :max 200}]]
   [:tax_id [:string {:min 5 :max 50}]]
   [:email [:string {:min 5 :max 254}]]
   [:phone [:string {:min 7 :max 20}]]
   [:country [:string {:min 2 :max 100}]]
   [:state [:string {:min 2 :max 100}]]
   [:city [:string {:min 2 :max 100}]]
   [:address [:string {:min 5 :max 300}]]
   [:postal_code [:string {:min 1 :max 20}]]
   [:website [:maybe [:string {:min 10 :max 500}]]]
   [:status seller-status-schema]
   [:verified [:boolean]]
   [:commission_rate [:double {:min 0 :max 100}]]
   [:password [:string {:min 8 :max 100}]]
   [:bank_account [:string {:min 5 :max 50}]]
   [:bank_name [:string {:min 2 :max 100}]]])

(def seller-location-schema
  [:map {:closed true}
   [:state [:string {:min 2 :max 100}]]
   [:city [:string {:min 2 :max 100}]]
   [:address [:string {:min 5 :max 300}]]
   [:postal_code [:string {:min 1 :max 20}]]])
