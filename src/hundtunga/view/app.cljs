(ns hundtunga.view.app
  (:require [reagent.core]
            [hundtunga.snake.core :as snake]))


(defn button
  [{label       :label
    on-click-fn :on-click-fn
    disabled    :disabled
    style       :style}]
  [:button
   {:disabled disabled
    :style    (merge {:min-width     "70px"
                      :padding       "4px 15px"
                      :border-radius "0"
                      :cursor        "pointer"
                      :border        "1px solid gray"}
                     style)
    :on-click on-click-fn}
   label])


(defn app-component
  [{states    :states
    runner-id :runner-id} trigger-event]
  (let [state (first states)]
    [:div
     [:h1 "Snake!"]
     (let [frame-size 10]
       [:div
        (->> (range frame-size)
             (map (fn [y]
                    [:div {:key   y
                           :style {:display "flex"}}
                     (->> (range frame-size)
                          (map (fn [x]
                                 [:div {:key      x
                                        :style    {:width            "36px"
                                                   :height           "36px"
                                                   :background-color "lightgray"
                                                   :border           "1px solid white"
                                                   :display          "flex"
                                                   :justify-content  "center"
                                                   :align-items      "center"
                                                   :cursor           "pointer"}
                                        :on-click (fn [] (trigger-event {:name :cell-clicked
                                                                         :data {:cell [x y]}}))}

                                  [:div {:style (merge {:width            "80%"
                                                        :height           "80%"
                                                        :background-color "tomato"
                                                        :border-radius    "50%"
                                                        :transform        "scale(1)"
                                                        :transition       "all 200ms ease-out"}
                                                       (when-not (snake/alive? state [x y])
                                                         {:transform "scale(0)"})
                                                       (when (snake/is-food? state [x y])
                                                         {:background-color "blue"
                                                          :transform        "scale(0.4)"})
                                                       (when (snake/is-head? state [x y])
                                                         {:background-color "green"}))}]])))])))])
     [:div {:style {:margin-top "1rem"}}
      [button {:label       "Tick"
               :on-click-fn (fn [] (trigger-event {:name :tick}))}]
      [button {:label       "Back Tick"
               :on-click-fn (fn [] (trigger-event {:name :back-tick}))
               :style       {:margin-left "1rem"}}]
      [button {:label       "Start"
               :on-click-fn (fn [] (trigger-event {:name :start}))
               :disabled    runner-id
               :style       {:margin-left "1rem"}}]
      [button {:label       "Stop"
               :on-click-fn (fn [] (trigger-event {:name :stop}))
               :style       {:margin-left "1rem"}}]]
     [:div {:style {:margin-top "1rem"}}
      [button {:label       "Up"
               :on-click-fn (fn [] (trigger-event {:name :set-direction :data :up}))}]
      [button {:label       "Down"
               :on-click-fn (fn [] (trigger-event {:name :set-direction :data :down}))
               :style       {:margin-left "1rem"}}]
      [button {:label       "Left"
               :on-click-fn (fn [] (trigger-event {:name :set-direction :data :left}))
               :style       {:margin-left "1rem"}}]
      [button {:label       "Right"
               :on-click-fn (fn [] (trigger-event {:name :set-direction :data :right}))
               :style       {:margin-left "1rem"}}]]]))
