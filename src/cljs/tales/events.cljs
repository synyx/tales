(ns tales.events
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [reg-event-db reg-event-fx]]
            [tales.db :as db]
            [tales.routes :refer [editor-path]]))

(reg-event-db :initialise-db
  (fn [_ _] db/default-db))

(reg-event-db :set-active-project
  (fn [db [_ project-slug]]
    (assoc-in db [:editor :project] project-slug)))

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
