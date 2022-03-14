(ns pf-web.subs
  (:require
   [re-frame.core :as re-frame]
   [pf-web.events :as events]
   [pf-web.utils :as utils]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

;; routing

(re-frame/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))


(re-frame/reg-sub
 ::subtree
 (fn [db _]
   (:subtree db)))

(re-frame/reg-sub
 ::subtree-catalog
 (fn [db]
   (:subtree-catalog db)))

(re-frame/reg-sub
 ::sel-tech
 (fn [db]
   (let [item-id (:sel-tech db)
         item (get-in db [:subtree :tech item-id])]
     item)))

(re-frame/reg-sub
 ::alert-message
 (fn [db] (db :alert-message)))

(re-frame/reg-sub
 ::download-link
 (fn [db] (db :download-link)))

(re-frame/reg-sub
 ::comments
 (fn [db] (get-in db [:subtree :comments])))


(defn materialize-tech [tech roots]
  "Expand out a tech subtree into a form that's easier to interpret
  and display as graphs.

  Take as input a full tech tree."

  (let [dep-order (utils/depth-first (partial utils/tech-children tech) roots)
        indexed (map-indexed #(list %1 (tech %2)) dep-order)
        id-to-idx (into (sorted-map)
                        (map (fn [[idx d]] [(:id d) idx]) indexed))
        id-to-titles (into (sorted-map)
                           (map (fn [[id entry]] [id (:title entry)]) tech))
        dep-pairs (utils/dependencies (partial utils/tech-children tech) roots)
        id-to-dependent-pairs (group-by (fn [[A B]] B) dep-pairs)]
    {;; list of (idx, node)
     :ordered-nodes indexed
     ;; map of id to idx
     :id-to-idx id-to-idx
     :id-to-titles id-to-titles
     :id-to-dependent-pairs id-to-dependent-pairs
     :edges
     ;; output index pairs of dependencies, ie ([2, 0] [3, 1] ...)
     ;; meaning 2 depends on 0, 3 depends on 1...
     (sort (map (fn [[A B]] [(id-to-idx A) (id-to-idx B)]) dep-pairs))}))


(re-frame/reg-sub
 ::sel-tech-dep

 (fn [db]
   (let [tech (get-in db [:subtree :tech])
         roots (->> tech keys sort reverse)]
     (materialize-tech tech roots))))

(re-frame/reg-sub
 ;; comments for selected tech
 ::tech-comments
 (fn [_]
   [(re-frame/subscribe [::comments])
    (re-frame/subscribe [::sel-tech])])
 (fn [[comments sel-tech] _]
   (let [tech-id (:id sel-tech)
         sel-comments (filter #(= (:tech-id %) tech-id)
                              (vals comments))]
     (sort-by (juxt :seq :id) sel-comments))))

(re-frame/reg-sub
 ::catalog-entries
 (fn [db]
   (let [catalog (vals (:subtree-catalog db))]
     (into {} (map (fn [e] [(:id e) (:title e)]) catalog)))))
