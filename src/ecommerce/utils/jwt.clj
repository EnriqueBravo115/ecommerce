(ns ecommerce.utils.jwt
  (:require [buddy.sign.jwt :as jwt]
            [java-time.api :as jt]
            [clojure.tools.logging :as log]))

(def ^:private secret "123456789")
(def ^:private alg :hs512)
(def ^:private token-expiration 3600) ;; 1hr in secs

(defn generate-token
  [user-id roles]
  (let [claims {:user/id user-id
                :user/roles roles
                :exp (-> (jt/plus (jt/instant) (jt/seconds token-expiration)))}]
    (try
      (jwt/sign claims secret {:alg alg})
      (catch Exception e
        (log/error e "Error generating token")
        (throw e)))))

(defn generate-test-token []
  (let [secret "123456789"
        alg :hs512
        claims {:user-id 3
                :email "ana.hernandez@email.com" 
                :role "ADMIN"
                :iat (System/currentTimeMillis)
                :exp (+ (System/currentTimeMillis) (* 3600 1000))}]
    (jwt/sign claims secret {:alg alg})))

(generate-test-token)
