(ns ecommerce.components.webserver
  (:require
   [com.stuartsierra.component :as component]
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :as route]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.json :refer [wrap-json-body wrap-json-response]]))

(defroutes app-routes
  (GET "/" []
    {:status 200
     :body {:message "hello"}})

  (GET "/users" []
    {:status 200
     :body {:users [{:id 1 :name "Juan"}
                    {:id 2 :name "Maria"}]}})

  (POST "/users" {body :body}
    {:status 201
     :body {:message "user created" :user body}})

  (route/not-found
   {:status 404
    :body {:error "endpoint dont found"}}))

(def app
  (-> app-routes
      wrap-json-response
      (wrap-json-body {:keywords? true})))

(defrecord webserver [config]
  component/Lifecycle

  (start [this]
    (println "Starting Webserver on port:" (:port config))
    (assoc this :server (jetty/run-jetty app {:port (:port config) :join? false})))

  (stop [this]
    (println "Stopping Webserver")
    (some-> (:server this) .stop)
    (assoc this :server nil)))

(defn new-web-server [config]
  (map->webserver {:config config}))
