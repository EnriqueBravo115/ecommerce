(ns ecommerce.utils.email
  (:require
   [aero.core :refer [read-config]]
   [clojure.java.io :as io]
   [clojure.tools.logging :as log]
   [postal.core :as postal]))

(def email-settings
  (let [config (read-config (io/resource "config.edn"))]
    (get config :smtp)))

(defn send-email
  [{:keys [to subject body html attachments]}]
  (try
    (when (:enabled email-settings true)
      (let [smtp-config (select-keys email-settings [:host :user :pass :port :ssl])
            message (cond-> {:from    (:from email-settings)
                             :to      to
                             :subject subject}
                      html      (assoc :body [{:type "text/html" :content html}
                                              {:type "text/plain" :content body}])
                      (not html) (assoc :body body)
                      attachments (assoc :attachments attachments))

            result (postal/send-message smtp-config message)]

        (log/info "Email sent to:" to)
        {:success true :result result}))

    (catch Exception e
      (log/error "Failed to send email to" to ":" (.getMessage e))
      {:success false :error (.getMessage e)})))

(defn send-activation-code-email [email activation-code]
  (send-email
   {:to email
    :subject "Activation code"
    :html "<h1>Welcome </h1>"
    :body (str "Your activation code is: " activation-code)}))
