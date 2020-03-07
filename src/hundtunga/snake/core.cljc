(ns hundtunga.snake.core
  (:require [ysera.test :refer [is is-not is=]]
            [ysera.random :refer [random-nth]]
            [ysera.collections :refer [seq-contains?]]
            [clojure.test :refer [run-tests]]))

(defn create-empty-state
  []
  {:seed           0
   :board-size     10
   :direction      :right
   :next-direction nil
   :snake          (list [3 0] [2 0] [1 0])
   :food           [3 3]})

(defn create-state
  {:test (fn []
           (is= (create-state ["abc"] :direction :up)
                {:direction      :up
                 :next-direction nil
                 :snake          (list [0 0] [1 0] [2 0])
                 :board-size     10
                 :seed           0
                 :food           [3 3]})
           (is= (create-state [" 654"
                               " 123"])
                {:direction      :right
                 :next-direction nil
                 :seed           0
                 :board-size     10
                 :food           [3 3]
                 :snake          (list [1 1] [2 1] [3 1] [3 0] [2 0] [1 0])}))}

  [strings & kvs]
  (let [snake (->> strings
                   (map-indexed
                     (fn [y row]
                       (->> row
                            (map-indexed
                              (fn [x character]
                                (when (not= character \space)
                                  {:index character
                                   :pos   [x y]}))))))
                   (apply concat)
                   (remove nil?)
                   (sort-by :index compare)
                   (map :pos))]
    (as-> (create-empty-state) $
      (assoc $ :snake snake)
      (if (empty? kvs)
        $
        (apply assoc $ kvs)))))

(defn get-direction
  [state]
  (or (:next-direction state) (:direction state)))

(defn abs
  {:test (fn []
           (is= (abs 2) 2)
           (is= (abs 0) 0)
           (is= (abs -4) 4))}
  [x]
  (if (pos? x) x (- x)))

(defn distance
  {:test (fn []
           (is= (distance [4 4] [4 4]) 0)
           (is= (distance [0 0] [1 0]) 1)
           (is= (distance [1 1] [3 3]) 2)
           (is= (distance [-1 0] [1 1]) 2)
           (is= (distance [4 4] [5 -4]) 8))}
  [cell-1 cell-2]
  (->> (map - cell-1 cell-2)
       (map abs)
       (apply max)))

(defn neighbors
  {:test (fn []
           (is (neighbors [4 4] [5 4]))
           (is (neighbors [0 0] [1 0]))
           (is-not (neighbors [4 4] [4 4]))
           (is-not (neighbors [1 1] [3 3]))
           (is-not (neighbors [-1 0] [1 1])))}
  [cell-1 cell-2]
  (= (distance cell-1 cell-2) 1))

(defn alive?
  {:test (fn []
           (is (-> (create-state ["1 "])
                   (alive? [0 0])))
           (is-not (-> (create-state ["1 "])
                       (alive? [1 0]))))}
  [state cell]
  (seq-contains? (:snake state) cell))

(defn is-head?
  {:test (fn []
           (is (-> (create-state ["123"])
                   (is-head? [0 0])))
           (is (-> (create-state ["432"
                                  "  1"])
                   (is-head? [2 1]))))}
  [state coord]
  (= (first (:snake state)) coord))

(defn get-neighbors
  {:test (fn []
           (is= (get-neighbors [2 0])
                #{[2 -1] [1 0] [1 1] [3 0] [3 -1] [1 -1] [3 1] [2 1]}))}
  [cell]
  (let [directions (for [x     (range -1 2)
                         y     (range -1 2)
                         :when (not= [x y] [0 0])]
                     [x y])]
    (->> directions
         (map (fn [d] (map + d cell)))
         (set))))

(defn next-head-coord
  {:test (fn []
           (is= (-> (create-state ["  "
                                   "12"] :direction :up)
                    (next-head-coord))
                [0 0])
           (is= (-> (create-state ["  "
                                   "12"] :direction :down)
                    (next-head-coord))
                [0 2])
           (is= (-> (create-state [" 1"
                                   " 2"] :direction :left)
                    (next-head-coord))
                [0 0])
           (is= (-> (create-state ["1 "
                                   "2 "] :direction :right)
                    (next-head-coord))
                [1 0])
           (is= (-> (create-state ["1 "
                                   "2 "] :direction :left :next-direction :right)
                    (next-head-coord))
                [1 0]))}
  [state]
  (let [[x y]     (first (:snake state))
        direction (get-direction state)]
    (condp = direction
      :up    [x (dec y)]
      :down  [x (inc y)]
      :left  [(dec x) y]
      :right [(inc x) y])))

(defn board-coords
  {:test (fn []
           (is= (-> (create-state ["1"] :board-size 2)
                    (board-coords))
                [[0 0] [0 1]
                 [1 0] [1 1]])
           (is= (-> (create-state ["1"] :board-size 3)
                    (board-coords))
                [[0 0] [0 1] [0 2]
                 [1 0] [1 1] [1 2]
                 [2 0] [2 1] [2 2]]))}
  [state]
  (let [size (:board-size state)]
    (for [x (range size) y (range size)] [x y])))

(defn is-food?
  [state coord]
  (= coord (:food state)))

(defn update-food
  {:test (fn []
           (is= (-> (create-state [" 12"
                                   "  3"] :board-size 3)
                    (update-food)
                    (:food))
                [0 0])
           ;; Updated seed changes randomness
           (is= (-> (create-state [" 12"
                                   "  3"]
                                  :board-size 3
                                  :seed 23789)
                    (update-food)
                    (:food))
                [2 2]))}
  [state]
  (let [[new-seed random-coord] (as-> state $
                                  (board-coords $)
                                  (remove (partial alive? state) $)
                                  (remove (partial is-food? state) $)
                                  (random-nth (:seed state) $))]
    (assoc state :seed new-seed :food random-coord)))

(defn set-snake
  [state new-snake]
  (assoc state :snake new-snake))

;; (defn coord-in-head-)

(defn grow-head
  [state]
  (set-snake state (conj (:snake state) (next-head-coord state))))

(defn cut-tail
  [state]
  (set-snake state (drop-last (:snake state))))

(defn tick
  {:test (fn []
           (is= (-> (create-state ["123"
                                   "  4"] :direction :down)
                    (tick)
                    (:snake))
                (:snake (create-state ["234"
                                       "1  "]))))}
  [state]
  (as-> state $
    (if (is-food? $ (next-head-coord $))
      (update-food $)
      (cut-tail $))
    (grow-head $)
    (if (:next-direction $)
      (assoc $ :direction (:next-direction $) :next-direction nil)
      $)))

(defn set-direction
  "Updates the direction of the snake to the given one if valid."
  {:test (fn []
           ;; Valid direction change functions correctly
           (is= (-> (create-state ["21"] :direction :right)
                    (set-direction :down)
                    (:next-direction))
                :down)
           ;; Applying same direction as current doesn't change direction
           (is= (-> (create-state ["21"] :direction :right)
                    (set-direction :right)
                    (:next-direction))
                :right)
           ;; Can't move opposite to current direction
           (is= (-> (create-state ["21"] :direction :right)
                    (set-direction :left)
                    (:next-direction))
                :right))}
  [state new-direction]
  (let [old-direction (:direction state)
        opposite      {:up    :down
                       :down  :up
                       :left  :right
                       :right :left}]
    (if (or
          (= old-direction new-direction)
          (= old-direction (opposite new-direction)))
      state
      (assoc state :next-direction new-direction))))

(defn self-collision?
  [state]
  {:test (fn []
           (is= (-> (create-state ["543"
                                   " 12"] :food [3 3] :board-size 4 :direction :up)
                    (tick)
                    (self-collision?))
                true)
           (is= (-> (create-state [" 43"
                                   " 12"] :food [3 3] :board-size 4 :direction :up)
                    (tick)
                    (self-collision?))
                false))}
  (not= (count (:snake state)) (count (set (:snake state)))))

(defn wall-collision?
  {:test (fn []
           (is= (-> (create-state ["21"] :board-size 1)
                    (wall-collision?))
                true)
           (is= (-> (create-state ["2"
                                   "1"] :board-size 2)
                    (wall-collision?))
                false)
           (is= (-> (create-state ["12"] :direction :left :board-size 4)
                    (tick)
                    (wall-collision?))
                true))}
  [state]
  (let [[x y] (first (:snake state))]
    (not (and (< -1 x (:board-size state))
              (< -1 y (:board-size state))))))

(defn game-over?
  [state]
  (or (self-collision? state) (wall-collision? state)))

(comment
  (run-tests))
