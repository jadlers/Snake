(ns hundtunga.all
  (:require [clojure.test :refer [run-tests successful?]]
            [ysera.test :refer [deftest is]]
            [hundtunga.snake.core]))

(deftest test-all
  "Bootstrapping with the required namespaces, finds all the hundtunga.*
  namespaces (except this one), requires them and runs all thei tests."
  (let [namespaces (->> (all-ns)
                        (map str)
                        (filter (fn [x] (re-matches #"hundtunga\..*" x)))
                        (remove (fn [x] (= "hundtunga.all" x)))
                        (map symbol))]
    (is (successful? (time (apply run-tests namespaces))))))
