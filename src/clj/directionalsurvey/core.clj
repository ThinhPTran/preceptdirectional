(ns directionalsurvey.core
  (:gen-class)
  (:require [org.httpkit.server :as server]
            [ring.util.response :refer [response resource-response]]
            [compojure.core :refer [defroutes POST GET]]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [org.httpkit.server :refer [run-server]]
            [directionalsurvey.systemevents :as sys]))

(defroutes app-routes
  ;; this here to serve web content, if any
  (GET  "/" [] (resource-response "public/index.html"))
  (GET  "/chsk" req (sys/ring-ws-handoff req))
  (POST "/chsk" req (sys/ring-ws-post req))
  ;(POST "/login" req (sys/login-handler req))
  (route/resources "/")
  (route/not-found "<h1>Page not found</h1>"))

(defn- wrap-request-logging [handler]
  (fn [{:keys [request-method uri] :as req}]
    (let [resp (handler req)]
      (println (name request-method) (:status resp)
               (if-let [qs (:query-string req)]
                 (str uri "?" qs) uri))
      resp)))

(def app
  (-> app-routes
      (handler/site)
      (wrap-request-logging)))

(defn -main [& args]
  (println "Starting server")
  (sys/ws-message-router)
  (server/run-server app {:port 3000})
  (println "Server started. http://localhost:" 3000))
