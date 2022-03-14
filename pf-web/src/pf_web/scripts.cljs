(ns pf-web.scripts
  (:require
   [pf-web.db :as db]
   [goog.string :as gstring]
   [clojure.string :refer [replace]]
   [clojure.walk :refer [stringify-keys]]))


(defn output-test-sql [& cli-args]
  (doseq [subtree (vals db/default-subtree-catalog)]
    (let [id (:id subtree)
          st (.stringify js/JSON (clj->js subtree))
          version (->> {:id1 0 :id2 0} clj->js (.stringify js/JSON))
          quoted (replace st "'" "''")]

      ;; weird -- we get an error when running this via node: gstring/format is not a function"
      ;;(println (gstring/format "'%s', 'subtree', '%s', '%s'" id version quoted)))))

      (println id)
      (println version)
      (println quoted)
      (println))))
