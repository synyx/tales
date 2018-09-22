(ns tales.events.project
  (:require [ajax.core :as ajax]
            [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
            [tales.routes :refer [editor-path]]))

(reg-event-fx :project/get-all
  (fn [{db :db}]
    {:db (assoc-in db [:loading? :projects] true)
     :http-xhrio {:method :get
                  :uri "/api/tales/"
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [:project/get-all-success]
                  :on-failure [:api-request-error :projects]}}))

(reg-event-db :project/get-all-success
  [trim-v]
  (fn [db [response]]
    (let [projects (js->clj response)
          slugs (map #(:slug %) projects)]
      (-> db
        (assoc-in [:loading? :projects] false)
        (dissoc :errors)
        (assoc :projects (zipmap slugs projects))))))

(reg-event-fx :project/add
  [trim-v]
  (fn [{db :db} [project]]
    {:db (assoc-in db [:loading? :project] true)
     :http-xhrio {:method :post
                  :uri "/api/tales/"
                  :params project
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [:project/change-success]
                  :on-failure [:api-request-error :project]}}))

(reg-event-fx :project/update
  [trim-v]
  (fn [{db :db} [project]]
    {:db (assoc-in db [:loading? :project] true)
     :http-xhrio {:method :put
                  :uri (str "/api/tales/" (:slug project))
                  :params project
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [:project/change-success]
                  :on-failure [:api-request-error :project]}}))

(reg-event-fx :project/change-success
  [trim-v]
  (fn [{:keys [db]} [response]]
    (let [project (js->clj response)]
      {:db (-> db
             (assoc-in [:loading? :project] false)
             (dissoc :errors)
             (assoc-in [:projects (:slug project)] project))
       :navigate (editor-path {:slug (:slug project)})})))

(reg-event-fx :project/update-image
  [trim-v]
  (fn [{db :db} [{project :project file :file}]]
    {:db (assoc-in db [:loading? :project] true)
     :determine-image-dimensions {:project project :file file}
     :http-xhrio {:method :put
                  :uri (str "/api/tales/" (:slug project) "/image")
                  :body file
                  :headers {:content-type (.-type file)}
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [:project/change-success]
                  :on-failure [:api-request-error :project]}}))

(reg-event-fx :api-request-error
  [trim-v]
  (fn [{:keys [db]} [request-type response]]
    (let [errors (get-in response [:response :errors])]
      {:db (assoc-in db [:errors request-type] errors)
       :dispatch [:complete-request request-type]})))

(reg-event-db :complete-request
  [trim-v]
  (fn [db [request-type]]
    (assoc-in db [:loading? request-type] false)))