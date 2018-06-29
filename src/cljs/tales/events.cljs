(ns tales.events
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [subscribe reg-event-db reg-event-fx]]
            [tales.db :as db]
            [tales.routes :refer [editor-path]]
            [tales.leaflet.core :as L]))

(reg-event-db :initialise-db
  (fn [_ _] db/default-db))

(reg-event-db :set-active-page
  (fn [db [_ active-page]]
    (assoc db :active-page active-page)))

(reg-event-db :set-active-project
  (fn [db [_ active-project]]
    (assoc db :active-project active-project)))

(reg-event-db :navigator-available
  (fn [db [_ navigator]]
    (assoc-in db [:editor :navigator] navigator)))

(reg-event-db :navigator-unavailable
  (fn [db _]
    (update-in db [:editor] dissoc :navigator)))

(reg-event-db :start-draw
  (fn [db [_ start end editing?]]
    (-> db
      (assoc-in [:editor :drawing?] true)
      (assoc-in [:editor :draw :start] start)
      (assoc-in [:editor :draw :end] end)
      (assoc-in [:editor :draw :editing?] editing?)
      (assoc-in [:editor :draw :slide]
        {:rect (L/latlng-bounds->slide-rect
                 (.latLngBounds js/L start (or end start)))}))))

(reg-event-fx :end-draw
  (fn [{db :db} _]
    (let [start (get-in db [:editor :draw :start])
          end (get-in db [:editor :draw :end])
          editing? (get-in db [:editor :draw :editing?])
          slide {:rect (L/latlng-bounds->slide-rect
                         (.latLngBounds js/L start end))}]
      {:db (-> db
             (assoc-in [:editor :drawing?] false)
             (update-in [:editor] dissoc :draw))
       :dispatch (if editing? [:update-slide slide] [:add-slide slide])})))

(reg-event-db :update-draw
  (fn [db [_ latlng]]
    (let [start (get-in db [:editor :draw :start])]
      (-> db
        (assoc-in [:editor :draw :end] latlng)
        (assoc-in [:editor :draw :slide]
          {:rect (L/latlng-bounds->slide-rect
                   (.latLngBounds js/L start latlng))})))))

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
          current-slide (get-in db [:editor :current-slide])
          project (get-in db [:projects slug])
          slides (:slides project)]
      {:dispatch [:update-project
                  (assoc-in project [:slides] (assoc slides current-slide slide))]})))

(reg-event-fx :activate-slide
  (fn [{db :db} [_ idx]]
    {:db (assoc-in db [:editor :current-slide] idx)}))

(reg-event-fx :move-to-slide
  (fn [{db :db} [_ idx]]
    (let [navigator (subscribe [:navigator])
          slide (subscribe [:slide idx])]
      {:navigator-fly-to [@navigator @slide]})))

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
