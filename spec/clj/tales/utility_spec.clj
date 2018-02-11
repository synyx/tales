(ns tales.utility-spec
  (:require [speclj.core :refer :all]
            [tales.utility :refer [slugify]]))


(describe "slugify"
          (it "joins words with hyphens"
              (should= "multiple-words" (slugify "multiple words"))
              (should= "multiple-words" (slugify "Multiple Words")))

          (it "removes spaces"
              (should= "multiple-words" (slugify "    Multiple Words    "))
              (should= "multiple-words" (slugify "\tMultiple\tWords\t")))

          (it "handles long sentences"
              (should= "i-m-a-snaillike-terrestrial-gastropod-having-no-shell"
                       (slugify "I'm a snaillike terrestrial gastropod having no shell"))
              (should= "i-m-a-snaillike-terrestrial-ga"
                       (slugify "I'm a snaillike terrestrial gastropod having no shell" 30)))

          (it "removes non-ascii characters"
              (should (= "aaaaaaaaaa" (slugify "áÁàÀãÃâÂäÄ")))
              (should (= "eeeeeeeeee" (slugify "éÉèÈẽẼêÊëË")))
              (should (= "iiiiiiiiii" (slugify "íÍìÌĩĨîÎïÏ")))
              (should (= "oooooooooo" (slugify "óÓòÒõÕôÔöÖ")))
              (should (= "uuuuuuuuuu" (slugify "úÚùÙũŨûÛüÜ")))))
