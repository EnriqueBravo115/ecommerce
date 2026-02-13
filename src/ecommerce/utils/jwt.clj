(ns ecommerce.utils.jwt
  (:require
   [buddy.auth :refer [authenticated?]]
   [buddy.sign.jwt :as jwt]
   [clojure.string :as str]))

(defn has-role? [request required-role]
  (if-let [user-identity (:identity request)]
    (if-let [roles (:roles user-identity)]
      (let [user-roles (if (string? roles)
                         (->> (clojure.string/split roles #",")
                              (map clojure.string/trim))
                         (if (coll? roles) roles [roles]))]
        (some #(= (clojure.string/upper-case required-role)
                  (clojure.string/upper-case (str %)))
              user-roles))
      false)
    false))

(defn has-any-role? [request & required-roles]
  (some #(has-role? request %) required-roles))

(defn- get-identity [request]
  (when (authenticated? request)
    (:identity request)))

(defn get-current-identity-id
  [request]
  (when-let [identity (get-identity request)]
    (:id identity)))

(defn generate-admin-test-token []
  (let [secret "123456789"
        alg :hs512
        current-time-seconds (quot (System/currentTimeMillis) 1000)
        claims {:id 3
                :email "ana.hernandez@email.com"
                :roles "ADMIN"
                :iat current-time-seconds
                :exp (+ current-time-seconds 2592000)}]
    (jwt/sign claims secret {:alg alg})))

(generate-admin-test-token)

(defn generate-seller-test-token []
  (let [secret "123456789"
        alg :hs512
        current-time-seconds (quot (System/currentTimeMillis) 1000)
        claims {:id 1
                :email "contact@techinnovators.com"
                :roles "SELLER"
                :iat current-time-seconds
                :exp (+ current-time-seconds 2592000)}]
    (jwt/sign claims secret {:alg alg})))

(generate-seller-test-token)

(defn generate-customer-test-token []
  (let [secret "123456789"
        alg :hs512
        current-time-seconds (quot (System/currentTimeMillis) 1000)
        claims {:id 1
                :email "contact@techinnovators.com"
                :roles "CUSTOMER"
                :iat current-time-seconds
                :exp (+ current-time-seconds 2592000)}]
    (jwt/sign claims secret {:alg alg})))

(generate-customer-test-token)
