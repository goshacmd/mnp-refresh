(ns mnp-reagent.country-desc)

(def country-desc
  [{:country "Belarus"
    :tax {:self-employed {:tax-rates [:flat 5]
                          :soc-sec-rates [:fixed 50]}}}
   {:country "Poland"
    :tax {:self-employed {:tax-rates [:flat 19]
                          :soc-sec-rates [:fixed 300]}}}
   {:country "Czech Republic"
    :tax {:self-employed {:tax-rates [:flat 19]
                          :soc-sec-rates [:flat 14]}}}
   {:country "Australia"
    :tax {:self-employed {:tax-rates [:incremental [[18200 0]
                                                    [37000 19]
                                                    [80000 32.5]
                                                    [180000 37]
                                                    [nil 45]]]
                          :soc-sec-rates [:flat 0]}}}])
