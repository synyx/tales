(ns tales.events.project
  (:require [ajax.core :as ajax]
            [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
            [tales.routes :refer [editor-path]]))


(defn open [{db :db} [slug]]
  {:db (assoc-in db [:loading? :project] true)
   :http-xhrio {:method :get
                :uri (str "/api/tales/" slug)
                :format (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success [:project/open-success]
                :on-failure [:api-request-error :project]}})

(defn open-success [{:keys [db]} [response]]
  (let [project (js->clj response)]
    {:db (-> db
           (assoc-in [:loading? :project] false)
           (dissoc :errors)
           (assoc-in [:projects (:slug project)] project)
           (assoc :project project))}))

(defn get-all [{db :db}]
  {:db (assoc-in db [:loading? :projects] true)
   :http-xhrio {:method :get
                :uri "/api/tales/"
                :format (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success [:project/get-all-success]
                :on-failure [:api-request-error :projects]}})

(defn get-all-success [db [response]]
  (let [projects (js->clj response)
        slugs (map #(:slug %) projects)]
    (-> db
      (assoc-in [:loading? :projects] false)
      (dissoc :errors)
      (assoc :projects (zipmap slugs projects)))))

(defn add [{db :db} [project]]
  {:db (assoc-in db [:loading? :project] true)
   :http-xhrio {:method :post
                :uri "/api/tales/"
                :params project
                :format (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success [:project/change-success]
                :on-failure [:api-request-error :project]}})

(defn change [{db :db} [project]]
  {:db (assoc-in db [:loading? :project] true)
   :http-xhrio {:method :put
                :uri (str "/api/tales/" (:slug project))
                :params project
                :format (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success [:project/change-success]
                :on-failure [:api-request-error :project]}})

(defn update-image [{db :db} [{project :project file :file}]]
  {:db (assoc-in db [:loading? :project] true)
   :determine-image-dimensions {:project project :file file}
   :http-xhrio {:method :put
                :uri (str "/api/tales/" (:slug project) "/image")
                :body file
                :headers {:content-type (.-type file)}
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success [:project/change-success]
                :on-failure [:api-request-error :project]}})

(defn change-success [{:keys [db]} [response]]
  (let [project (js->clj response)]
    {:db (-> db
           (assoc-in [:loading? :project] false)
           (dissoc :errors)
           (assoc-in [:projects (:slug project)] project))
     :navigate (editor-path {:slug (:slug project)})}))

(defn api-request-error [{:keys [db]} [request-type response]]
  (let [errors (get-in response [:response :errors])]
    {:db (assoc-in db [:errors request-type] errors)
     :dispatch [:complete-request request-type]}))

(defn complete-request [db [request-type]]
  (assoc-in db [:loading? request-type] false))

(reg-event-fx :project/open [trim-v] open)
(reg-event-fx :project/open-success [trim-v] open-success)
(reg-event-fx :project/get-all get-all)
(reg-event-db :project/get-all-success [trim-v] get-all-success)
(reg-event-fx :project/add [trim-v] add)
(reg-event-fx :project/update [trim-v] change)
(reg-event-fx :project/update-image [trim-v] update-image)
(reg-event-fx :project/change-success [trim-v] change-success)
(reg-event-fx :api-request-error [trim-v] api-request-error)
(reg-event-db :complete-request [trim-v] complete-request)