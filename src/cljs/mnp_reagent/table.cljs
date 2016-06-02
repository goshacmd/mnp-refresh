(ns mnp-reagent.table)

(defn table [& children]
  [:table children])

(defn table-row [& children]
  [:tr children])

(defn table-detail [& children]
  [:td children])

(defn table-header [& children]
  [:thead [:tr children]])

(defn table-header-detail [& children]
  [:th children])

(defn table-body [& children]
  [:tbody children])

(defn- present-val [[key fun] item]
  (fun (item key)))

(defn id [x] x)
(defn data-table [{:keys [columns data]}]
  (let [column-names (map first columns)
        column-keys (map (fn [x] [(second x) (get x 2 id)]) columns)]
    [table
     [table-header
      (map (fn [x] [table-header-detail x]) column-names)]
     [table-body
      (map (fn [item]
             [table-row (map (fn [x] [table-detail (present-val x item)]) column-keys)])
           data)]]))
