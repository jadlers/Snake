(ns hundtunga.main
  (:require [reagent.core :as reagent]
            [hundtunga.view.app :refer [app-component]]
            [hundtunga.snake.core :as snake]))

(enable-console-print!)

(defonce app-state-atom (atom nil))



(defn handle-event!
  [{name :name data :data}]
  (condp = name
    :tick
    (swap! app-state-atom
           update :states (fn [states] (conj states (snake/tick (first states)))))

    :back-tick
    (when (> (count (:states (deref app-state-atom))) 1)
      (swap! app-state-atom
             update :states (fn [states] (drop 1 states))))

    :start
    (when (nil? (:runner-id (deref app-state-atom)))
      (let [id (js/setInterval (fn [] (handle-event! {:name :tick})) 800)]
        (swap! app-state-atom assoc :runner-id id)))

    :stop
    (do (js/clearInterval (:runner-id (deref app-state-atom)))
        (swap! app-state-atom assoc :runner-id nil))

    :set-direction
    (swap! app-state-atom
           update :states (fn [states] (conj states (snake/set-direction (first states) data))))))

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

  (reset! app-state-atom {:states (list (snake/create-state ["123"]))}))

(defn on-js-reload []
  (render (deref app-state-atom)))
