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
  (let [{:keys [rawallusers]} @(subscribe [:allusers])
        names (map #(let [r (t/reader :json)]
                      (t/read r (:user/name %))) rawallusers)]
    [:div.col-sm-2.col-md-2
     [:div "User names: "]
     ;[:div (str "allusers: " allusers)]
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
                       (js/Handsontable mydiv (clj->js (assoc-in localtableconfig [:afterChange] #(let [changeDatas (js->clj %)]
                                                                                                    (then [:transient :setdata changeDatas]))))))
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
  (let [{:keys [rawallusers]} @(subscribe [:allusers])
        {:keys [rawallactions currentpick]} @(subscribe [:myglobaltransacts])
        {:keys [loginuser]} @(subscribe [:loginuser])
        allusers (reduce #(assoc %1 (:db/id %2) (let [r (t/reader :json)]
                                                  (t/read r (:user/name %2)))) {} rawallusers)
        tmpallactions (map #(let [r (t/reader :json)]
                              (t/read r (:action/value %))) rawallactions)
        allactions (vec (sort-by :instant < (map (fn [in] {:user (get allusers (:user in))
                                                           :row (:row in)
                                                           :col (:col in)
                                                           :val (:val in)
                                                           :instant (:instant in)}) tmpallactions)))
        pickactions (if (> (count allactions) 0) (subvec allactions 0 currentpick) allactions)
        localactions (filterv #(= loginuser (:user %)) pickactions)]
    (then [:global :localactions localactions])
    [:div.col-sm-4
     [:h2 "Local actions: "]
     ;[:div (str "loginuser: " loginuser)]
     ;[:div (str "allusers: " allusers)]
     ;[:div (str "allactions: " allactions)]
     ;[:div (str "localactions: " localactions)]
     [:div
      [:ul
       (for [action localactions]
         ^{:key action} [:li (str "User " (:user action) " changed at " (:instant action))])]]]))

(defn myglobaltable []
  (let [{:keys [globaltableconfig]} @(subscribe [:myglobaltable])
        table (atom {:table nil})]
    [:div.col-sm-4
     [:div
      {:style {:min-width "310px" :max-width "800px" :margin "0 auto"}
       :ref (fn [mydiv]
              (if (some? mydiv)
                (swap! table assoc :table
                       (js/Handsontable mydiv (clj->js (assoc-in globaltableconfig [:afterChange] #(let [changeDatas (js->clj %)]
                                                                                                     (then [:transient :setdata changeDatas]))))))
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
  (let [{:keys [rawallusers]} @(subscribe [:allusers])
        {:keys [rawallactions currentpick]} @(subscribe [:myglobaltransacts])
        allusers (reduce #(assoc %1 (:db/id %2) (let [r (t/reader :json)]
                                                  (t/read r (:user/name %2)))) {} rawallusers)
        tmpallactions (map #(let [r (t/reader :json)]
                              (t/read r (:action/value %))) rawallactions)
        allactions (vec (sort-by :instant < (map (fn [in] {:user (get allusers (:user in))
                                                           :row (:row in)
                                                           :col (:col in)
                                                           :val (:val in)
                                                           :instant (:instant in)}) tmpallactions)))
        pickactions (if (> (count allactions) 0) (subvec allactions 0 currentpick) allactions)]
        ;pickactions (if (some? allactions) (subvec allactions 0 currentpick) [])]
    (then [:global :globalactions allactions])
    (then [:global :pickactions pickactions])
    ;(then [:global :totalactions (count allactions)])
    ;(then [:global :currentpick (count allactions)])
    [:div.col-sm-4
     [:h2 "Global actions: "]
     ;[:div (str "allusers: " rawallusers)]
     ;[:div (str "rawallactions: " allactions)]
     [:div
       [:ul
        (for [action pickactions]
          ^{:key action} [:li (str "User " (:user action) " changed at " (:instant action))])]]]))

(defn myslider []
  (let [{:keys [globalactions totalactions currentpick]} @(subscribe [:myslider])]
    [:div.col-sm-12
     [:input#myrange {:type "range"
                      :min 0
                      :max totalactions
                      :value currentpick
                      :step 1
                      :on-change (fn [_]
                                  (let
                                    [v (.-value (gdom/getElement "myrange"))]
                                    ;(.log js/console (str "v: " v))
                                    (then [:global :currentpick (js/parseInt v)])))}]
     [:div (str "Username: ")]
     [:div (str "at: ")]]))

(defn dropbox-unit []
  (let [{:keys [unit-item-map selected-unit-item]} @(subscribe [:unitoption])]
    (if (and (some? unit-item-map)
             (some? selected-unit-item))
      [:div.col-md-12
       [:select
        {:value selected-unit-item
         :on-change (fn [e]
                      (let [value (.-value (.-target e))
                            x (if (= "" value ) nil (keyword value))]
                        (then [:transient :command-server-change-unit x])))}
        ;^{:key :select-none} [:option {:value nil} ""]
        ;^{:key :option1} [:option {:value "SI ab."} "SI metric"]]])
        (for [item unit-item-map]
          ^{:key item} [:option {:value (str (name (first (keys item))))} (first (vals item))])]]
      [:div.col-md-12])))


(defn app []
  [:div.col-sm-12.col-md-12
   [:h2 "Welcome to my Precept experiment"]
   [:div.row
    ;(let [item-map [{:US "US metric"} {:SI "SI Metric"}]]
    ;  (js/console.log (str "item-map: " (get item-map 0)))
    [dropbox-unit]]
   [:div.row {:style {:margin-bottom "5px"}}
    [loginform]
    [usernames]]
    ;[refreshbutton]]
   [:div.row
    [mylocaltable]
    [mylocalchart]
    [mylocaltransacts]]
   [:div.row
    [myslider]]
   [:div.row
    [myglobaltable]
    [myglobalchart]
    [myglobaltransacts]]])




