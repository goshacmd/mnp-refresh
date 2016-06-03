(ns mnp-reagent.table)

(defn table [& children]
  [:div.Table children])

(defn table-row [& children]
  (into [:div.TableRow] children))

(defn table-detail [& children]
  [:div.TableDetail children])

(defn table-header [& children]
  [:div.TableRow children])

(defn table-header-detail [& children]
  (into [:div.TableHeaderDetail] children))

(defn table-body [& children]
  [:div.TableBody children])
