(ns pf-web.routes
  (:require
   [bidi.bidi :as bidi]
   [pushy.core :as pushy]
   [re-frame.core :as re-frame]
   [pf-web.events :as events]))

(defmulti panels identity)
(defmethod panels :default [] [:div "No panel found for this route."])

(def routes
  (atom
   ["/" {"" :home-panel
         "subtree/" {[:id] :subtree-panel}}]))

(defn parse
  [url]
  (bidi/match-route @routes url))

(defn url-for [handler params]
  (apply bidi/path-for @routes handler (flatten (into () params))))

;; history (below) sets up dispatch to be called on every change in
;; navigation path, ie from set-token! or from the user going directly
;; to a page in the browser
(defn dispatch
  [route]
  (let [panel (-> route :handler name keyword)]
    (re-frame/dispatch [:set-active-panel panel (:route-params route)])))

(defonce history
  (pushy/pushy dispatch parse))

(defn start!
  []
  (pushy/start! history))

(re-frame/reg-fx
  :navigate-route
  (fn [[handler params]]
    (pushy/set-token! history (url-for handler params))))
