(ns directionalsurvey.views
  (:require [goog.dom :as gdom]
            [reagent.core :as reagent]
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
        names (map #(:user/name %) allusers)]
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

(defn app []
  [:div.col-sm-12.col-md-12
   [:h2 "Welcome to my Precept experiment"]
   [:div.row
    [loginform]
    [usernames]]])