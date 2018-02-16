(ns tales.events
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [reg-event-db reg-event-fx]]
            [tales.db :as db]))

(reg-event-db :initialise-db
              (fn [_ _] db/default-db))

(reg-event-db :set-active-page
              (fn [db [_ active-page]]
                (assoc db :active-page active-page)))

(reg-event-db :set-active-project
              (fn [db [_ active-project]]
                (assoc db :active-project active-project)))

(reg-event-fx :get-projects
              (fn [{db :db} _]
                {:db         (assoc-in db [:loading :projects] true)
                 :http-xhrio {:method          :get
                              :uri             "/api/tales/"
                              :format          (ajax/json-request-format)
                              :response-format (ajax/json-response-format {:keywords? true})
                              :on-success      [:get-projects-success]
                              :on-failure      [:api-request-error :projects]}}))

(reg-event-db :get-projects-success
              (fn [db [_ response]]
                (-> db
                    (assoc-in [:loading? :projects] false)
                    (dissoc :errors)
                    (assoc :projects (js->clj response)))))

(reg-event-fx :add-project
              (fn [{db :db} [_ project]]
                {:db         (assoc-in db [:loading :project] true)
                 :http-xhrio {:method          :post
                              :uri             "/api/tales/"
                              :params          project
                              :format          (ajax/json-request-format)
                              :response-format (ajax/json-response-format {:keywords? true})
                              :on-success      [:add-project-success]
                              :on-failure      [:api-request-error :project]}}))

(reg-event-db :add-project-success
              (fn [db [_ response]]
                (-> db
                    (assoc-in [:loading? :project] false)
                    (dissoc :errors)
                    (assoc :projects (conj (:projects db) (js->clj response))))))


(reg-event-fx :api-request-error
              (fn [{:keys [db]} [_ request-type response]]
                {:db       (assoc-in db [:errors request-type] (get-in response [:response :errors]))
                 :dispatch [:complete-request request-type]}))

(reg-event-db :complete-request
              (fn [db [_ request-type]]
                (assoc-in db [:loading? request-type] false)))
