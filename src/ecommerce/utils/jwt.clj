(ns ecommerce.utils.jwt
  (:require [buddy.sign.jwt :as jwt]
            [java-time.api :as jt]
            [clojure.string :as str]
            [clojure.tools.logging :as log]))

(def ^:private secret "123456789")
(def ^:private alg :hs512)
(def ^:private token-expiration 3600)

(defn generate-token
  [id roles exp]
  (let [claims {:user/id id
                :user/roles roles
                :exp (-> (jt/plus (jt/instant) (jt/seconds token-expiration)))}]
    (try
      (jwt/sign claims secret {:alg alg})
      (catch Exception e
        (log/error e "Error generating token")
        (throw e)))))

(defn generate-token
  [id email roles config]
  (let [secret "123456789"
        alg :hs512
        current-time-seconds (quot (System/currentTimeMillis) 1000)
        claims {:id id
                :email email
                :roles roles
                :iat current-time-seconds
                :exp (+ current-time-seconds 86400)}]
    (jwt/sign claims secret {:alg alg})))

(defn generate-test-token []
  (let [secret "123456789"
        alg :hs512
        current-time-seconds (quot (System/currentTimeMillis) 1000)
        claims {:id 3
                :email "ana.hernandez@email.com"
                :roles "CUSTOMER"
                :iat current-time-seconds
                :exp (+ current-time-seconds 86400)}]
    (jwt/sign claims secret {:alg alg})))

(generate-test-token)

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

(defn has-all-roles? [request & required-roles]
  (every? #(has-role? request %) required-roles))
