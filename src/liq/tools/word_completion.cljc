(ns liq.tools.word-completion
  (:require [clojure.string :as str]
            [liq.modes.clojure-mode :as clojure-mode]
            [liq.editor :as editor]
            [liq.buffer :as buffer]
            [liq.util :as util]))

(def word-list (atom nil))

(defn word-typeahead
  []
  (when (and (nil? @word-list) (editor/get-setting :word-list-path))
    (reset! word-list (str/split-lines (slurp (editor/get-setting :word-list-path)))))
  (let [buf (editor/current-buffer)
        w (or (re-find #"\w.*[\w/-_\\.]" (-> buf buffer/left buffer/word)) "")
        functionlist (clojure-mode/get-functions buf)]
    (((editor/get-mode :typeahead-mode) :init)
     ;(filter #(re-find (re-pattern (str "^" w)) %) (concat functionlist @word-list))
     (concat functionlist @word-list)
     str
     (fn [hit] (editor/apply-to-buffer
                 #(-> (buffer/left % (count w))
                      (buffer/delete-char (count w))
                      (buffer/insert-string (str hit " "))
                      (buffer/right (+ (count hit) 1))
                      buffer/set-insert-mode
                      buffer/right)))
     :search w
     :position :relative)))
