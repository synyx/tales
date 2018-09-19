(ns tales.events
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [subscribe reg-event-db reg-event-fx]]
            [tales.db :as db]
            [tales.slide.core :as slide]
            [tales.routes :refer [editor-path]]))

(defn drop-nth [n coll]
  (concat
    (take n coll)
    (drop (inc n) coll)))

(defn swap [v i1 i2]
  (assoc v i2 (v i1) i1 (v i2)))

(reg-event-db :initialise-db
  (fn [_ _] db/default-db))

(reg-event-db :set-active-page
  (fn [db [_ active-page]]
    (assoc db :active-page active-page)))

(reg-event-db :set-active-project
  (fn [db [_ active-project]]
    (assoc db :active-project active-project)))

(reg-event-db :stage/mounted
  (fn [db [_ dom-node]]
    (assoc-in db [:stage :dom-node] dom-node)))

(reg-event-db :stage/unmounted
  (fn [db _]
    (assoc-in db [:stage :dom-node] nil)))

(reg-event-db :stage/zoom
  (fn [db [_ zoom]]
    (assoc-in db [:stage :zoom] zoom)))

(reg-event-fx :stage/zoom-in
  (fn [{db :db} _]
    (let [current-zoom (get-in db [:stage :zoom])]
      {:dispatch [:stage/zoom (+ current-zoom 1)]})))

(reg-event-fx :stage/zoom-out
  (fn [{db :db} _]
    (let [current-zoom (get-in db [:stage :zoom])]
      {:dispatch [:stage/zoom (- current-zoom 1)]})))

(reg-event-db :stage/move-to
  (fn [db [_ x y]]
    (assoc-in db [:stage :position] {:x x :y y})))

(reg-event-db :stage/fit-rect
  (fn [db [_ rect]]
    (let [center (slide/center rect)
          dom-node (get-in db [:stage :dom-node])
          sx (/ (.-clientWidth dom-node) (:width rect))
          sy (/ (.-clientHeight dom-node) (:height rect))
          scale (Math/min sx sy)
          zoom (/ (Math/log scale) Math/LN2)]
      (if dom-node
        (-> db
          (assoc-in [:stage :position] center)
          (assoc-in [:stage :zoom] zoom))
        db))))

(reg-event-fx :add-slide
  (fn [{db :db} [_ slide]]
    (let [slug (:active-project db)
          project (get-in db [:projects slug])
          slides (:slides project)]
      {:dispatch [:update-project
                  (assoc-in project [:slides] (conj slides slide))]})))

(reg-event-fx :update-slide
  (fn [{db :db} [_ slide]]
    (let [slug (:active-project db)
          project (get-in db [:projects slug])
          slides (:slides project)
          current-slide (get-in db [:editor :current-slide])]
      {:dispatch [:update-project
                  (assoc-in project [:slides] (assoc slides current-slide slide))]})))

(reg-event-fx :activate-slide
  (fn [{db :db} [_ idx]]
    {:db (assoc-in db [:editor :current-slide] idx)}))

(reg-event-fx :delete-current-slide
  (fn [{db :db}]
    (let [slug (:active-project db)
          project (get-in db [:projects slug])
          slides (:slides project)
          current-slide (get-in db [:editor :current-slide])]
      (if-not (nil? current-slide)
        {:dispatch [:update-project
                    (assoc-in project [:slides] (drop-nth current-slide slides))]}))))

(reg-event-fx :move-to-slide
  (fn [_ [_ idx]]
    (let [slide (subscribe [:slide idx])
          rect (:rect @slide)]
      {:dispatch [:stage/fit-rect rect]})))

(reg-event-fx :next-slide
  (fn [{db :db} _]
    (let [slug (:active-project db)
          project (get-in db [:projects slug])
          slides (:slides project)
          current-slide (get-in db [:editor :current-slide])]
      (if (nil? current-slide)
        {:dispatch [:activate-slide 0]}
        {:dispatch [:activate-slide (mod (+ current-slide 1) (count slides))]}))))

(reg-event-fx :prev-slide
  (fn [{db :db} _]
    (let [slug (:active-project db)
          project (get-in db [:projects slug])
          slides (:slides project)
          current-slide (get-in db [:editor :current-slide])]
      (if (nil? current-slide)
        {:dispatch [:activate-slide 0]}
        {:dispatch [:activate-slide (mod (- current-slide 1) (count slides))]}))))

(reg-event-fx :change-order
  (fn [{db :db} [_ delta]]
    (let [slug (:active-project db)
          project (get-in db [:projects slug])
          slides (:slides project)
          current-slide (get-in db [:editor :current-slide])
          next-slide (+ current-slide delta)]
      (if (<= 0 next-slide (- (count slides) 1))
        {:db (assoc-in db [:editor :current-slide] next-slide)
         :dispatch [:update-project
                    (assoc-in project [:slides]
                      (swap slides current-slide next-slide))]}))))

(reg-event-fx :get-projects
  (fn [{db :db} _]
    {:db (assoc-in db [:loading? :projects] true)
     :http-xhrio {:method :get
                  :uri "/api/tales/"
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [:get-projects-success]
                  :on-failure [:api-request-error :projects]}}))

(reg-event-db :get-projects-success
  (fn [db [_ response]]
    (let [projects (js->clj response)
          slugs (map #(:slug %) projects)]
      (-> db
        (assoc-in [:loading? :projects] false)
        (dissoc :errors)
        (assoc :projects (zipmap slugs projects))))))

(reg-event-fx :add-project
  (fn [{db :db} [_ project]]
    {:db (assoc-in db [:loading? :project] true)
     :http-xhrio {:method :post
                  :uri "/api/tales/"
                  :params project
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [:change-project-success]
                  :on-failure [:api-request-error :project]}}))

(reg-event-fx :update-project
  (fn [{db :db} [_ project]]
    {:db (assoc-in db [:loading? :project] true)
     :http-xhrio {:method :put
                  :uri (str "/api/tales/" (:slug project))
                  :params project
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [:change-project-success]
                  :on-failure [:api-request-error :project]}}))


(reg-event-fx :change-project-success
  (fn [{:keys [db]} [_ response]]
    (let [project (js->clj response)]
      {:db (-> db
             (assoc-in [:loading? :project] false)
             (dissoc :errors)
             (assoc-in [:projects (:slug project)] project))
       :navigate (editor-path {:slug (:slug project)})})))

(reg-event-fx :update-project-image
  (fn [cofx [_ {project :project file :file}]]
    {:db (assoc-in (:db cofx) [:loading? :project] true)
     :determine-image-dimensions {:project project :file file}
     :http-xhrio {:method :put
                  :uri (str "/api/tales/" (:slug project) "/image")
                  :body file
                  :headers {:content-type (.-type file)}
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [:change-project-success]
                  :on-failure [:api-request-error :project]}}))

(reg-event-fx :api-request-error
  (fn [{:keys [db]} [_ request-type response]]
    (let [errors (get-in response [:response :errors])]
      {:db (assoc-in db [:errors request-type] errors)
       :dispatch [:complete-request request-type]})))

(reg-event-db :complete-request
  (fn [db [_ request-type]]
    (assoc-in db [:loading? request-type] false)))
