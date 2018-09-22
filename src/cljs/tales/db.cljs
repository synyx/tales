(ns tales.db)

(def default-db
  {:projects (list)
   :active-page nil
   :active-project nil
   :active-slide nil
   :editor {}
   :stage {:zoom nil
           :position nil}})
