(ns pf-web.tests.materialize-test
  (:require [cljs.test :refer (deftest is)]
            [pf-web.subs :refer [materialize-tech]]
            [pf-web.db :refer [default-tech]]
            [pf-web.utils :as utils]))


(def simple-tech
  ^:private
  {"uuid-A"
   {:id "uuid-A"
    :title "Tech A"
    :short-description "Tech A"
    :long-description "Tech A"
    :dependencies ["uuid-B"]}

   "uuid-B"
   {:id "uuid-B"
    :title "Tech B"
    :short-description "Tech B"
    :long-description "Tech B"
    :dependencies []}
   })

(deftest simple-test
  (let [{ordered-nodes :ordered-nodes id-to-idx :id-to-idx edges :edges} (materialize-tech simple-tech ["uuid-A"])]
    (is (= (count ordered-nodes) 2))
    (is (= (count id-to-idx) 2))
    (is (= edges '([1 0])))))

(deftest utils-dependencies
  (is (= (utils/dependencies (partial utils/tech-children simple-tech) ["uuid-A"])
         '(("uuid-A", "uuid-B")))))


(def little-bit-harder-tech
  ^:private
  {"uuid-A"
   {:id "uuid-A"
    :title "Tech A"
    :short-description "Tech A"
    :long-description "Tech A"
    :dependencies ["uuid-B", "uuid-C"]}

   "uuid-B"
   {:id "uuid-B"
    :title "Tech B"
    :short-description "Tech B"
    :long-description "Tech B"
    :dependencies []}

   "uuid-C"
   {:id "uuid-C"
    :title "Tech C"
    :short-description "Tech C"
    :long-description "Tech C"
    :dependencies []}
   })

(deftest little-bit-harder-test
  (let [{ordered-nodes :ordered-nodes id-to-idx :id-to-idx edges :edges} (materialize-tech little-bit-harder-tech ["uuid-A"])]
    (is (= (count ordered-nodes) 3))
    (is (= (count id-to-idx) 3))
    (is (= edges '([2 0] [2 1])))))
