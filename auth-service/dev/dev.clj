(ns dev
  (:require [com.stuartsierra.component.repl :as component-repl]
            [auth-service.core :as system]))

(component-repl/set-init
 (fn [_]
   (system/system-component
    {:server {:port 3000}

     :db-spec {:jdbcUrl  "jdbc:postgresql://localhost:5432/ecommerce"
               :username "ecommerce"
               :password "ecommerce"}

     :smtp {:host "email-smtp.us-east-1.amazonaws.com"
            :user "AKIARVSK33G3SWJ7LHHZ"
            :pass "BIaTdKox3H9Bo/PTC/77sNqTuWvP7QL7SZTz6wOoq2FG"
            :from "enriquebravo115@gmail.com"
            :ssl true
            :port 465}})))
