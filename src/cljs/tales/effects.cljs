(ns tales.effects
  (:require [accountant.core :as accountant]
            [re-frame.core :refer [dispatch reg-fx]]
            [re-frame.db :refer [app-db]]
            [tales.animation :as anim :refer [easings]]
            [tales.image :as image]
            [tales.ticker :as ticker :refer [PTickHandler]]))

(reg-fx :navigate
  (fn [url]
    (accountant/navigate! url)))

(reg-fx :determine-image-dimensions
  (fn [{project :project file :file}]
    (image/dimensions file
      #(dispatch [:project/update (assoc project :dimensions %)]))))

(defn ^:private now []
  (.getTime (js/Date.)))

(defn animate-db [{:keys [id path from to duration easing]
                   :or {easing :linear}}]
  (if id
    (let [anim-fn (anim/create-anim-fn from to duration easing)
          handler (reify PTickHandler
                    (init-state [_ db]
                      (assoc-in db [:animation id] {:start (now)}))
                    (tick [_ db]
                      (let [start (get-in db [:animation id :start])
                            t (- (now) start)]
                        (when (>= t duration) (ticker/remove-handler id))
                        (assoc-in db (flatten path) (anim-fn t)))))]
      (ticker/add-handler id handler))
    (.error js/console :error "animate: no animation id provided.")))

(reg-fx
  :animate-db
  (fn [value]
    (let [seq-animation-maps (if (sequential? value) value [value])]
      (doseq [animation-map seq-animation-maps]
        (animate-db animation-map)))))
