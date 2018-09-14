(ns tales.db)

(def default-db
  {:projects (list)
   :active-page nil
   :active-project nil
   :editor {:current-slide nil
            :drawing? false}
   :stage {:zoom -3
           :position {:x 0 :y 0}}})
