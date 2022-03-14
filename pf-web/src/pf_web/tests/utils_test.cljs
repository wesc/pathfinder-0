(ns pf-web.tests.utils-test
  (:require [cljs.test :refer (deftest is)]
            [pf-web.utils :as utils]))


(deftest dfs-simple
  "Simple usage check."
  (let [inp {:a [:b :c]
             :b []
             :c []}
        get-children (fn [id] (id inp))]
    (is (= (utils/depth-first get-children [:a])
           '(:b :c :a)))
    (is (= (utils/depth-first get-children [:b])
           '(:b)))
    (is (= (utils/depth-first get-children [:c])
           '(:c)))))

(deftest dfs-repeated
  "Check that repeated elements, in this case :c, are removed."
  (let [inp {:a [:b :c]
             :b [:c]
             :c []}
        get-children (fn [id] (id inp))]
    (is (= (utils/depth-first get-children [:a])
           '(:c :b :a)))))
