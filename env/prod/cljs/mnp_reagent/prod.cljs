(ns mnp-reagent.prod
  (:require [mnp-reagent.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
