(ns pf-web.events
  (:require
   [clojure.string]
   [re-frame.core :as re-frame]
   [pf-web.db :as db]
   [pf-web.utils :refer [keywordize-in]]
   [ajax.core :as ajax]
   [goog.string :as gstring]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))


;; routing

(re-frame/reg-event-fx
 :navigate-to
 (fn [_ [_ handler params]]
   {:navigate-route [handler params]}))

(re-frame/reg-event-fx
 :set-active-panel
 (fn [{:keys [db]} [_ active-panel params]]
   (let [subtree (get-in db [:subtree-catalog (:id params)])]
     {:db (assoc db
                 :active-panel [active-panel params]
                 :subtree subtree
                 ;; select last item cause why not
                 :sel-tech (->> subtree :tech keys last))})))

(re-frame/reg-event-db
 ::set-subtree-field
 (fn [db [_ field new-value]]
   (assoc-in db [:subtree field] new-value)))

(re-frame/reg-event-db
 :select-tech
 (fn [db [_ new-value]]
   (assoc db :sel-tech new-value)))

(re-frame/reg-event-db
 ::update-tech
 (fn [db [_ id field value]]
   (assoc-in db [:subtree :tech id field] value)))

(re-frame/reg-event-db
 ::add-tech
 (fn [db [_]]
   (let [uid (str (random-uuid))]
     (-> db
         (assoc-in [:subtree :tech uid]
                   {:id uid
                    :title (gstring/format "untitled (%s)" uid)
                    :short-description ""
                    :long-description ""
                    :dependencies []})
         (assoc :sel-tech uid)))))

(re-frame/reg-event-db
 ::delete-tech
 (fn [db [_ id]]
   (-> db
       (update-in [:subtree :tech] dissoc id)
       (assoc :sel-tech nil))))

(re-frame/reg-event-db
 :overwrite-tech
 (fn [{:keys [db] :as cofx} [_ tech]]
   (let [tech (keywordize-in ["id" "title" "author" "email" "short-description" "long-description" "description" "tech" "dependencies" "seq" "text" "tech-id" "comments"] tech)
         tech-id (:id tech)]
     {:db (assoc-in db [:subtree-catalog tech-id] tech)
      :navigate-route [:subtree-panel {:id tech-id}]})))

(re-frame/reg-event-db
 :push-alert
 (fn [db [_ message severity]]
   (assoc db :alert-message [message severity])))

(re-frame/reg-event-fx
 :make-download-link
 (fn [{:keys [db] :as cofx} [_]]
   (let [data (->> db (:subtree) (clj->js))
         encoded (.stringify js/JSON data nil 4)
         old-link (db :download-link)
         link (.createObjectURL js/URL (js/Blob. [encoded]))]
     {:db (assoc db :download-link link)
      :dispatch [:rotate-and-open-download-link old-link link]})))

(re-frame/reg-event-fx
 :rotate-and-open-download-link
 (fn [{:keys [db] :as cofx} [_ old-link link]]
   (if old-link
     (.revokeObjectURL js/URL old-link))
   (js/window.open link)
   {}))

(re-frame/reg-event-fx
 :read-from-file

 (fn [cofx [_ file]]
   (let [reader (js/FileReader.)]
     (set! (.-onload reader)
           (fn [e]
             (let [input (-> e .-target .-result)
                   parsed (.parse js/JSON input)]
               (re-frame/dispatch [:overwrite-tech (js->clj parsed)]))))
     (.readAsText reader file))))


(re-frame/reg-event-db
 :add-comment
 (fn [db [_ tech-id comment]]
   (let [comment (clojure.string/trim comment)]
     (if (empty? comment)
       db
       (let [comments (get-in db [:subtree :comments])
             comment-id (str (random-uuid))
             next-seq (->> (vals comments) (apply max-key :seq) :seq inc)
             new-comments (conj comments {comment-id {:id comment-id
                                                      :seq next-seq
                                                      :tech-id tech-id
                                                      :text comment}})]
         (js/console.log new-comments)
         (assoc-in db [:subtree :comments] new-comments))))))

(re-frame/reg-event-db
 :delete-comment
 (fn [db [_ comment-id]]
   (update-in db [:subtree :comments] dissoc comment-id)))

(re-frame/reg-event-db
 ::add-tech-reference
 (fn [db [_ tech-id reference]]
   (if (not-empty reference)
     (let [references (get-in db [:subtree :tech tech-id :references])
           new-references (sort (distinct (conj references reference)))]
       (assoc-in db [:subtree :tech tech-id :references] new-references))
     db)))

(re-frame/reg-event-fx
 :load-catalog
 (fn [{:keys [db]} _]
   {:db   (assoc db :show-twirly true)
    :http-xhrio {:method          :get
                 :uri             "http://localhost:8080/api/catalog"
                 :timeout         8000
                 :response-format (ajax/json-response-format)
                 :on-success      [:load-catalog-success]
                 :on-failure      [:load-catalog-failure]}}))

(re-frame/reg-event-db
 :load-catalog-success
 (fn [db [_ result]]
   (let [keywordized (->> result
                          (vals)
                          ;; terrible terrible terrible.. we want to
                          ;; turn anything that doesn't look like a
                          ;; uuid into a keyword
                          (keywordize-in ["id" "title" "author" "email" "short-description" "long-description" "description" "tech" "dependencies" "seq" "text" "tech-id" "comments"])
                          (map (fn [s] [(:id s) s]))
                          (into {}))]
     (assoc db :subtree-catalog keywordized))))

(re-frame/reg-event-fx
 :load-catalog-failure
 (fn [cofx [_]]
   {:dispatch [:push-alert "error loading subtree catalog" "error"]}))

(re-frame/reg-event-fx
 ::save-subtree
 (fn [{:keys [db] :as cofx} [_]]
   (let [subtree (:subtree db)
         id (:id subtree)]
     {:dispatch [:push-alert "saving" "info"]
      :db (assoc-in db [:subtree-catalog id] subtree)
      :http-xhrio {:method          :post
                   :uri             "http://localhost:8080/api/subtrees"
                   :timeout         8000
                   :params          subtree
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format)
                   :on-success      [:save-subtree-success]
                   :on-failure      [:save-subtree-failure]}})))

(re-frame/reg-event-fx
 :save-subtree-success
 (fn [cofx [_]]
   {:dispatch [:push-alert "save subtree succeeded" "success"]}))

(re-frame/reg-event-fx
 :save-subtree-failure
 (fn [cofx [_]]
   {:dispatch [:push-alert "save subtree failed" "error"]}))

(re-frame/reg-event-fx
 ::new-subtree
 (fn [{:keys [db] :as cofx} [_]]
   (let [id (str (random-uuid))
         subtree {:id id
                  :title ""
                  :author ""
                  :email ""
                  :description ""
                  :tech {}
                  :comments {}}]
     (js/console.log subtree)
     {:db (assoc-in db [:subtree-catalog id] subtree)
      :navigate-route [:subtree-panel {:id id}]})))
