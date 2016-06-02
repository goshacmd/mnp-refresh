(ns mnp-reagent.calculate)

(defn- calculate-flat [rate income]
  (* (/ rate 100) income))

;; [[max1 rate1] [max2 rate 2] [nil rateN]]
;; -->
;; [[0 max1 rate1] [max1 max2 rate2] [maxN-1 nil rate3]]
(defn progressive-seq [brackets]
  (map-indexed (fn [idx [max rate]]
                  [(get-in brackets [(dec idx) 0] (if (= idx 0) 0 nil)) max rate])
               brackets))

(defn matching-amount [[min max _] amount]
  (if (< amount min)
    0
    (cond
      (= max nil) (- amount min)
      :else (clojure.core/min (- max min) (- amount min)))))

(defn calculate-progressive-item [[min max rate] amount]
  (->> amount
       (matching-amount [min max rate])
       (calculate-flat rate)))

(defn calculate-progressive [brackets income]
  (->> brackets
       (progressive-seq)
       (map #(calculate-progressive-item % income))
       (reduce +)))

(defn- calculate-tax [[rate-type rate-value] income]
  (case rate-type
    :flat (calculate-flat rate-value income)
    :incremental (calculate-progressive rate-value income)
    (throw (js/Error. (str "Unsupported tax rate type: " rate-type)))))

(defn- calculate-soc-sec [[rate-type rate-value] income]
  (case rate-type
    :flat (* (/ rate-value 100) income)
    :fixed (* rate-value 12) ;; monthly -> yearly
    (throw (js/Error. (str "Unsupported soc sec rate type: " rate-type)))))

(defn calculate [desc income]
  (let [f-tax (calculate-tax (get-in desc [:tax :self-employed :tax-rates]) income)
        f-ss  (calculate-soc-sec (get-in desc [:tax :self-employed :soc-sec-rates]) income)
        f-total (+ f-tax f-ss)
        f-percent (* 100 (/ f-total income))]
    {:country (:country desc)
     :effective_tax f-tax
     :effective_soc_sec f-ss
     :effective_percent f-percent
     :effective_total f-total}))

(defn calculate-items [country-desc income]
  (map #(calculate % income) country-desc))
