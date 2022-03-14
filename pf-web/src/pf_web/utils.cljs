(ns pf-web.utils
  (:require
   [clojure.walk :refer [keywordize-keys postwalk]]
   [pf-web.db :as db]))


(defn tech-children [tech id]
  (sort (get-in tech [id :dependencies] [])))


(defn depth-first [get-children roots]
  "Depth first search, returning leaf nodes first.

  get-children should return a list of children given a node."

  (letfn [(dfs [node]
            (let [children (get-children node)]
              (if (empty? children)
                [node]
                (concat (map dfs children)
                        [node]))))]
    (->> roots (map dfs) flatten distinct)))


(defn dependencies [get-children roots]
  "Returns a list of pairs of (A, B) where A depends on B, derived
  from the root."

  (let [deps (depth-first get-children roots)
        pairs (map (fn [d]
                     (map #(list d %1) (get-children d))) deps)]
    (mapcat identity pairs)))

(defn keywordize-in
  [s m]
  (let [set-s (set s)
        f (fn [[k v]] (if (and (string? k) (contains? set-s k)) [(keyword k) v] [k v]))]
    ;; only apply to maps
    (postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))
