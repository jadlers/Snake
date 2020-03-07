(ns hundtunga.main
  (:require [reagent.core :as reagent]
            [hundtunga.view.app :refer [app-component]]
            [hundtunga.snake.core :as snake]))

(enable-console-print!)

(defonce app-state-atom (atom nil))

(def starting-game-states (list (snake/create-state ["321"] :direction :right :board-size 20 :seed 1234)))

(defn handle-event!
  [{name :name data :data}]
  (condp = name
    :tick
    (as-> (swap! app-state-atom
                 update :states (fn [states] (conj states (snake/tick (first states))))) $
      (if (snake/game-over? (first (:states $)))
        (swap! app-state-atom
               assoc :states starting-game-states)
        $))

    :back-tick
    (when (> (count (:states (deref app-state-atom))) 1)
      (swap! app-state-atom
             update :states (fn [states] (drop 1 states))))

    :start
    (when (nil? (:runner-id (deref app-state-atom)))
      (let [id (js/setInterval (fn [] (handle-event! {:name :tick})) 100)]
        (swap! app-state-atom assoc :runner-id id)))

    :stop
    (do (js/clearInterval (:runner-id (deref app-state-atom)))
        (swap! app-state-atom assoc :runner-id nil))

    :play-pause
    (if (:runner-id (deref app-state-atom))
      (handle-event! {:name :stop})
      (handle-event! {:name :start}))

    :set-direction
    (swap! app-state-atom
           update :states (fn [states] (conj states (snake/set-direction (first states) data))))))

(defn handle-keypress!
  [e]
  (let [key (.-key e)]
    (condp = key
      "w"
      (handle-event! {:name :set-direction :data :up})

      "a"
      (handle-event! {:name :set-direction :data :left})

      "s"
      (handle-event! {:name :set-direction :data :down})

      "d"
      (handle-event! {:name :set-direction :data :right})

      "p"
      (handle-event! {:name :play-pause}))))

(defn key-press-event!
  "Forwards key press events to handler function"
  [e]
  (handle-keypress! e))

(defn render
  [app-state]
  (reagent/render-component [app-component app-state handle-event!]
                            (js/document.getElementById "app")))

(when (nil? (deref app-state-atom))

  (add-watch app-state-atom
             :change
             (fn [_ _ old-state new-state]
               (when (not= old-state new-state)
                 (render new-state))))

  (.addEventListener js/window "keypress" key-press-event!)

  (reset! app-state-atom {:states starting-game-states}))

(defn on-js-reload []
  (render (deref app-state-atom)))
