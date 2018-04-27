(ns tales.utility_test
  (:require [clojure.test :refer :all]
            [tales.utility :refer [slugify]]))

(deftest test-slugify
  (testing "joins words with hyphens"
    (is (= "multiple-words" (slugify "multiple words")))
    (is (= "multiple-words" (slugify "Multiple Words"))))

  (testing "removes whitespace"
    (is (= "multiple-words" (slugify "    Multiple Words    ")))
    (is (= "multiple-words" (slugify "\tMultiple\tWords\t"))))

  (testing "handles long sentences"
    (is (= "i-m-a-snaillike-terrestrial-gastropod-having-no-shell"
          (slugify "I'm a snaillike terrestrial gastropod having no shell")))
    (is (= "i-m-a-snaillike-terrestrial-ga"
          (slugify "I'm a snaillike terrestrial gastropod..." 30))))

  (testing "removes non-ascii characters"
    (is (= "aaaaaaaaaa" (slugify "áÁàÀãÃâÂäÄ")))
    (is (= "eeeeeeeeee" (slugify "éÉèÈẽẼêÊëË")))
    (is (= "iiiiiiiiii" (slugify "íÍìÌĩĨîÎïÏ")))
    (is (= "oooooooooo" (slugify "óÓòÒõÕôÔöÖ")))
    (is (= "uuuuuuuuuu" (slugify "úÚùÙũŨûÛüÜ")))))
