(ns tales.db)

(def default-db
  {:projects (list)
   :active-page nil
   :active-project nil
   :editor {:current-slide nil
            :drawing? false}})
