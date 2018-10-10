(ns tales.db)

(def default-db
  {:projects {}
   :active-page nil
   :active-project nil
   :active-slide nil
   :editor {}
   :stage {:zoom 0
           :position {:x 0 :y 0}
           :origin {:x 0 :y 0}}})
