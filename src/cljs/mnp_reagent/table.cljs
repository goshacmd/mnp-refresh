(ns mnp-reagent.table)

(defn table [& children]
  [:div.Table children])

(defn table-row [& children]
  (into [:div.TableRow] children))

(defn table-detail [& children]
  [:div.TableDetail children])

(defn -table-header [& children]
  [:div.TableRow children])

(defn table-header-detail [& children]
  [:div.TableHeaderDetail children])

(defn table-body [& children]
  [:div.TableBody children])

(defn table-header [column-names]
  [-table-header
   (map (fn [x] [table-header-detail x]) column-names)])

(defn- present-val [[key fun] item]
  (fun (item key)))

(defn table-column-row
  ([column-keys item]
   (table-column-row {} column-keys item))
  ([params column-keys item]
   [table-row params (map (fn [x] [table-detail (present-val x item)]) column-keys)]))
