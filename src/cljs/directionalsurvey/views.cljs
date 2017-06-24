(ns directionalsurvey.views
  (:require [goog.dom :as gdom]
            [reagent.core :as reagent]
            [cljsjs.highcharts]
            [cljsjs.handsontable]
            [directionalsurvey.utils :as utils]
            [directionalsurvey.utils :refer [init-tableconfig]]
            [directionalsurvey.serverevents :as se]
            [cognitect.transit :as t]
            [precept.core :refer [subscribe then]]))

(defn loginform []
  (let [{:keys [loginuser]} @(subscribe [:loginuser])
        {:keys [entry/user]} @(subscribe [:loginentry])]
    [:div.col-sm-2.col-md-2
     [:input
      {:id "my-input-box"
       :type "text"
       :value user
       :onChange (fn [_]
                   (let [v (.-value (gdom/getElement "my-input-box"))]
                     (.log js/console "change something!!!: " v)
                     (then [:global :entry/user v])))}]
     [:button#btn-login
      {:type "button"
       :onClick (fn []
                  (.log js/console "logging in!!!")
                  (then [:transient :button/pressed :login]))}
      "Secure login!"]
     [:div (str "input text: " user)]
     [:div (str "user name: " loginuser)]]))

(defn usernames []
  (let [{:keys [allusers]} @(subscribe [:allusers])
        names (map #(let [r (t/reader :json)]
                      (t/read r (:user/name %))) allusers)]
    [:div.col-sm-2.col-md-2
     [:div "User names: "]
     [:input
      {:type "button"
       :value "Get usernames"
       :on-click (fn [_]
                   (.log js/console "Hi there!!!"))}]
     [:ul
      (for [name names]
        ^{:key name} [:li name])]]))

(defn refreshbutton []
  [:div.col-sm-4.col-md-4
   [:input
    {:type "button"
     :value "Please refresh!!!"
     :on-click (fn [_]
                 (.log js/console "Refresh!!!")
                 (.log js/console "Chon cach khac de refresh di"))}]])

(defn mylocaltable []
  (let [{:keys [localtableconfig]} @(subscribe [:mylocaltable])
        table (atom {:table nil})]
    [:div.col-sm-4
     [:div
      {:style {:min-width "310px" :max-width "800px" :margin "0 auto"}
       :ref (fn [mydiv]
              (if (some? mydiv)
                (swap! table assoc :table
                       (js/Handsontable mydiv (clj->js (assoc-in localtableconfig [:afterChange] #(do
                                                                                                    (.log js/console "Change something!!!: " (js->clj %)))))))
                (let [mytable (:table @table)]
                  (if (some? mytable)
                    (do
                      (.destroy mytable)
                      (swap! table assoc :table nil))))))}]]))

(defn mylocalchart []
  (let [{:keys [localtableconfig]} @(subscribe [:mylocaltable])
        my-chart-config (utils/gen-chart-config-handson localtableconfig)
        chart (atom {:chart nil})]
    [:div.col-sm-4
     [:div
      {:style {:height "100%" :width "100%" :position "relative"}
       :ref (fn [mydiv]
              (if (some? mydiv)
                (swap! chart assoc :chart (js/Highcharts.Chart. mydiv (clj->js @my-chart-config)))
                (let [mychart (:chart @chart)]
                  (if (some? mychart)
                    (do
                      (.destroy mychart)
                      (swap! chart :chart nil))))))}]]))

(defn mylocaltransacts []
  (let [listactions ["test1" "test2"]]
    [:div.col-sm-4
     [:h2 "Local actions: "]
     [:ul
      (for [action listactions]
        ^{:key action} [:li (str action)])]]))

(defn myglobaltable []
  (let [{:keys [globaltableconfig]} @(subscribe [:myglobaltable])
        table (atom {:table nil})]
    [:div.col-sm-4
     [:div
      {:style {:min-width "310px" :max-width "800px" :margin "0 auto"}
       :ref (fn [mydiv]
              (if (some? mydiv)
                (swap! table assoc :table
                       (js/Handsontable mydiv (clj->js (assoc-in globaltableconfig [:afterChange] #(do
                                                                                                    (.log js/console "Change something!!!: " (js->clj %)))))))
                (let [mytable (:table @table)]
                  (if (some? mytable)
                    (do
                      (.destroy mytable)
                      (swap! table assoc :table nil))))))}]]))

(defn myglobalchart []
  (let [{:keys [globaltableconfig]} @(subscribe [:myglobaltable])
        my-chart-config (utils/gen-chart-config-handson globaltableconfig)
        chart (atom {:chart nil})]
    [:div.col-sm-4
     [:div
      {:style {:height "100%" :width "100%" :position "relative"}
       :ref (fn [mydiv]
              (if (some? mydiv)
                (swap! chart assoc :chart (js/Highcharts.Chart. mydiv (clj->js @my-chart-config)))
                (let [mychart (:chart @chart)]
                  (if (some? mychart)
                    (do
                      (.destroy mychart)
                      (swap! chart :chart nil))))))}]]))

(defn myglobaltransacts []
  (let [listactions ["test3" "test4"]]
    [:div.col-sm-4
     [:h2 "Global actions: "]
     [:ul
      (for [action listactions]
        ^{:key action} [:li (str action)])]]))

(defn app []
  [:div.col-sm-12.col-md-12
   [:h2 "Welcome to my Precept experiment"]
   [:div.row
    [loginform]
    [usernames]]
    ;[refreshbutton]]
   [:div.row
    [mylocaltable]
    [mylocalchart]
    [mylocaltransacts]]
   [:div.row
    [myglobaltable]
    [myglobalchart]
    [myglobaltransacts]]])




