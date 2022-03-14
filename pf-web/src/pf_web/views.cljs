(ns pf-web.views
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [pf-web.subs :as subs]
   [pf-web.events :as events]
   [pf-web.routes :as routes]
   [goog.string :as gstring]
   ;;[secretary.core :as secretary]
   ["@mui/material" :refer [AppBar, Toolbar, IconButton, Typography, Alert, Box, Button, Grid,
                            Container, TextField, Fab, Autocomplete, Snackbar, Input, Divider,
                            List, ListItem, ListItemIcon, ListItemButton, ListItemText]]
   ["@mui/icons-material" :refer [Add, Delete, Download, Upload, Save, Home]]
   ))


(defn subtree-render [deps]
  (let [bsort-left 4
        viewbox-height (+ (* (count (deps :ordered-nodes)) 4) 5)
        ;; this is the longest arc we have to draw -- we adjust the
        ;; radius of the arc depending on this number
        max-depth-diff (reduce max (map (fn [[A B]] (- A B)) (deps :edges)))
        arc-radius (float (/ bsort-left (+ max-depth-diff 4)))]
    (fn [deps]
      [:> Box
       {:height "100%"}
       [:svg
        ;; seems like the right strategy is to set the viewBox height to
        ;; whatever we need to cover all the tech elements
        {:width "100%" :viewBox (gstring/format "0 0 32 %d" viewbox-height)}

        ;; bubble nodes and text
        (map (fn [[idx d]]
               [:g
                {:key (:id d)}
                [:circle
                 {:cx bsort-left :cy (+ (* idx 4) 2) :r 1
                  :stroke "gray" :stroke-width "1px" :vector-effect "non-scaling-stroke"
                  :fill "none"}]
                [:a
                 {:href "#"
                  :on-click (fn [e]
                              (.preventDefault e)
                              (re-frame/dispatch [:select-tech (:id d)]))}
                 [:text
                  {:x (+ bsort-left 2) :y (+ (* idx 4) 2)
                   :alignment-baseline "middle"
                   :font-size "1.5" :font-family "Roboto, sans-serif"
                   :color "gray"}
                  (:title d)
                  ]]])
             (deps :ordered-nodes))

        ;; bubble edges
        (map (fn [[A B]]
               (let [sx (- bsort-left 1)
                     sy (+ (* A 4) 2)
                     ex sx
                     ey (+ (* B 4) 2)
                     r arc-radius
                     path (gstring/format "M %d %d A 2 %.02f 90 0 1 %d %d" sx sy r ex ey)]
                 [:g
                  {:key (gstring/format "%d,%d" A B)}
                  [:path
                   {:d path
                    :fill "transparent"
                    :stroke "gray" :stroke-width "1px" :vector-effect "non-scaling-stroke"}]
                  ]
                 )
               )
             (deps :edges))]])))


(defn dyn-text-field [label default-value finish-edit & [meta]]
  "Dynamic TextField component which implements dynamic interactions.

  Losing focus causes the text field to execute a finishing
  function. Escape causes it to reset input text.  Supply it a label,
  default value, and a finish-edit function which takes as an argument
  the user inputted text field and should perform some action.

  We do a funny thing with manipulating the key to force
  re-rendering. See
  https://clojureverse.org/t/controlled-textfield-with-material-ui-reagent/5828"

  (let [;; use rev in the key to force re-rendering when necessary
        rev (reagent/atom 0)
        value (reagent/atom default-value)]
    (fn [label default-value finish-edit]
      [:> TextField
       {:key (gstring/format "%d-%s" @rev default-value)
        :label label
        :type "text"
        :default-value default-value
        :full-width true
        :multiline (:multiline meta)
        :on-change #(reset! value (-> % .-target .-value))
        :on-key-down #(case (-> % .-key)
                        "Enter" (if-not (:multiline meta) (finish-edit @value))
                        ("Escape" "Esc") (do (reset! value default-value)
                                             (swap! rev inc))
                        nil)
        :on-blur #(finish-edit @value)}])))

(defn dependency-selection [deps selected]
  (let [values (reagent/atom (:dependencies selected))]
    (fn [deps selected]
      [:> Autocomplete
       {:multiple true
        :filterSelectedOptions true
        :options (remove #{(:id selected)} (->> deps :id-to-titles keys))
        :get-option-label (fn [^js option] (get-in deps [:id-to-titles option]))
        :value (:dependencies selected)
        :on-change #(re-frame/dispatch [::events/update-tech (:id selected) :dependencies %2])
        :render-input (fn [^js params]
                        ;; see https://github.com/reagent-project/reagent/blob/e70c52531341bba83636e88eb7b60ff5796195b1/examples/material-ui/src/example/core.cljs#L78-L93
                        (set! (.-variant params) "outlined")
                        (set! (.-label params) "Dependencies")
                        (reagent/create-element TextField params))}]
      )))

(defn references-selection [catalog selected]
  (let [values (reagent/atom (:references selected))]
    (fn [catalog selected]
      [:> Autocomplete
       {:disable-portal true
        :options (keys catalog)
        :get-option-label (fn [^js option] (catalog option))
        :on-change #(re-frame/dispatch [::events/add-tech-reference (:id selected) %2])
        :render-input (fn [^js params]
                        ;; see https://github.com/reagent-project/reagent/blob/e70c52531341bba83636e88eb7b60ff5796195b1/examples/material-ui/src/example/core.cljs#L78-L93
                        (set! (.-variant params) "outlined")
                        (set! (.-label params) "References")
                        (reagent/create-element TextField params))}]
      )))

(defn comment-detail [comments]
  [:> Box
   [:> Typography
    {:variant "h5"}
    "Comments"]
   (map (fn [c] [:> ListItem
                 {:key (:id c)}
                 [:> ListItemText
                  {:primary (:text c)}]
                 [:> IconButton
                  {:edge "end" :aria-label "delete"
                   :on-click #(re-frame/dispatch [:delete-comment (:id c)])}
                  [:> Delete]]
                 ])
        comments)])

(defn tech-detail [deps selected comments]
  (let [new-comment-key (reagent/atom 0)
        new-comment (reagent/atom "")
        ;; bah, this should be passed in as an argument
        subtree-catalog (re-frame/subscribe [::subs/catalog-entries])]
    (fn [deps selected comments]
      [:div
       [:> Box
        {:m "1em"}
        [dyn-text-field
         "Title"
         (:title selected)
         #(re-frame/dispatch [::events/update-tech (:id selected) :title %])]]

       [:> Box
        {:m "1em"}
        [dyn-text-field
         "Summary"
         (:short-description selected)
         #(re-frame/dispatch [::events/update-tech (:id selected) :short-description %])
         {:multiline true}]]

       [:> Box
        {:m "1em"}
        [dyn-text-field
         "Description"
         (:long-description selected)
         #(re-frame/dispatch [::events/update-tech (:id selected) :long-description %])
         {:multiline true}]]

       [:> Box
        {:m "1em"}
        [dependency-selection deps selected]]

       [:> Box
        {:m "1em"}
        [references-selection @subtree-catalog selected]]

       [:> Box
        {:m "1em"}

        (map (fn [r]
               [:> ListItem
                {:key r :disablePadding true}
                [:> ListItemButton
                 ;;{:on-click #(js/window.open (routes/url-for :subtree-panel (select-keys s [:id])) "_blank")}
                 {:on-click #(re-frame/dispatch [:navigate-to :subtree-panel {:id r}])}
                 [:> ListItemText {:primary (@subtree-catalog r)}]]])
             (sort-by #(@subtree-catalog %) (:references selected)))
        ]

       [:> Box
        {:m "1em"}
        [comment-detail comments]

        [:> TextField
         {:key @new-comment-key
          :variant "standard" :multiline true :full-width true
          :type "text"
          :default-value @new-comment
          :label "new comment"
          :on-change #(reset! new-comment (-> % .-target .-value))}]

        [:> Box
         {:pt "1em"}
         [:> Button
          {:variant "outlined"
           :on-click (fn [e]
                       (re-frame/dispatch [:add-comment (:id selected) @new-comment])
                       (reset! new-comment "")
                       (swap! new-comment-key inc))}
          "submit"]]]
       ])))

(defn notification [alert]
  (fn [alert]
    (let [[message severity] alert
          visible (boolean message)]
      [:> Snackbar
       {:open visible
        :auto-hide-duration 3000
        :on-close #(re-frame/dispatch [:push-alert nil "info"])}
       [:> Alert
        {:severity severity}
        message]
       ])))

(defn panel-frame [inner]
  (let [name (re-frame/subscribe [::subs/name])
        alert-message (re-frame/subscribe [::subs/alert-message])]
    [:div
     [:> Container
      {:maxWidth "xl"}

      [:> AppBar
       {:position "sticky"}
       [:> Toolbar
        {:variant "dense"}

        [:> IconButton
         {:edge "start" :color "inherit" :aria-label "menu"
          :on-click #(re-frame/dispatch [:navigate-to :home-panel nil])}
         [:> Home]]
        
        [:> Typography
         {:variant "h6" :color "inherit" :component "div"}
         @name]
        ]]

      inner
      ]
     [notification @alert-message]]))

(defn subtree-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])
        [_ {subtree-id :id}] @active-panel
        name (re-frame/subscribe [::subs/name])
        subtree (re-frame/subscribe [::subs/subtree])
        {subtree-title :title
         subtree-author :author
         subtree-email :email
         subtree-description :description} @subtree

        selected (re-frame/subscribe [::subs/sel-tech])
        deps (re-frame/subscribe [::subs/sel-tech-dep])
        comments (re-frame/subscribe [::subs/tech-comments])
        ;; controls the left offset for bubbles and edges
        bsort-left 4]
    [panel-frame
     [:> Grid
      {:container true}

      ;; topological bubble sort
      [:> Grid
       {:item true :xs 4}

       [:> Box
        {:m "1em"}
        [dyn-text-field
         "Title"
         subtree-title
         #(re-frame/dispatch [::events/set-subtree-field :title %])]]

       [:> Box
        {:m "1em"}
        [dyn-text-field
         "Author"
         subtree-author
         #(re-frame/dispatch [::events/set-subtree-field :author %])]]

       [:> Box
        {:m "1em"}
        [dyn-text-field
         "Email"
         subtree-email
         #(re-frame/dispatch [::events/set-subtree-field :email %])]]

       [:> Box
        {:m "1em"}
        [dyn-text-field
         "Description"
         subtree-description
         #(re-frame/dispatch [::events/set-subtree-field :description %])
         {:multiline true}]]

       [subtree-render @deps]]

      ;; tech detail
      [:> Grid
       {:item true :xs 6}

       (if @selected
         [tech-detail @deps @selected @comments]
         [:> Box
          {:m "1em"}
          [:> Typography
           {:variant "body"}
           "no tech selected"]])
       ]

      ;; toolbar
      [:> Grid
       {:item true :xs 2}

       [:> Box
        {:m "1em"}

        [:> List
         {:component "nav"}

         [:> ListItemButton
          {:on-click #(re-frame/dispatch [::events/add-tech])}

          [:> ListItemIcon
           [:> Add]]
          [:> ListItemText
           {:primary "Add New Tech"}]]

         [:> ListItemButton
          {:on-click #(re-frame/dispatch [::events/save-subtree])}

          [:> ListItemIcon
           [:> Save]]
          [:> ListItemText
           {:primary "Save Changes"}]]

         [:> ListItemButton
          {:on-click #(re-frame/dispatch [:make-download-link])}

          [:> ListItemIcon
           [:> Download]]
          [:> ListItemText
           {:primary "Download"}]]

         [:label
          {:html-for "input-button"}
          [:input
           {:accept "application/json" :type "file" :id "input-button" :style {:display "none"}
            :on-change #(re-frame/dispatch [:read-from-file (-> % .-target .-files first)])}]]
         [:> ListItemButton
          {:on-click #(-> js/document (.getElementById "input-button") (.click))}

          [:> ListItemIcon
           [:> Upload]]
          [:> ListItemText
           {:primary "Upload"}]]

         [:> Divider]

         [:> ListItemButton
          {:on-click #(let [dependents (get-in @deps [:id-to-dependent-pairs (:id @selected)])]
                        (if dependents
                          (re-frame/dispatch [:push-alert
                                              (gstring/format "can't delete %s because it has dependents" (:title @selected)) "error"])
                          (re-frame/dispatch [::events/delete-tech (:id @selected)])))}

          [:> ListItemIcon
           [:> Delete]]
          [:> ListItemText
           {:primary (str "Delete " (:title @selected))}]]
         ]

        ]]
      ]]))

(defn subtree-catalog-list []
  (let [subtree-catalog (re-frame/subscribe [::subs/subtree-catalog])
        sorted-subtrees (sort-by :title (vals @subtree-catalog))]
    [:> Box
     {:m "1em"}
     [:> Typography
      {:variant "h4"}
      "Catalog"]
     [:> Button
      {:on-click #(re-frame/dispatch [:load-catalog])}
      "Refresh Catalog"]

     [:> Button
      {:on-click #(re-frame/dispatch [::events/new-subtree])}
      "New Subtree"]

     (map (fn [s] [:> ListItem
                   {:key (:id s) :disablePadding true}
                   [:> ListItemButton
                    ;;{:on-click #(js/window.open (routes/url-for :subtree-panel (select-keys s [:id])) "_blank")}
                    {:on-click #(re-frame/dispatch [:navigate-to :subtree-panel (select-keys s [:id])])}
                    [:> ListItemText {:primary (:title s)}]]]) sorted-subtrees)
     ]))

(defn home-panel []
  [panel-frame
   [:> Grid
    {:container true}

    [:> Grid
     {:item true :xs 4}

     [subtree-catalog-list]]
    ]])


(defmethod routes/panels :home-panel [] [home-panel])
(defmethod routes/panels :subtree-panel [] [subtree-panel])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])
        [panel params] @active-panel]
    (routes/panels panel)))
