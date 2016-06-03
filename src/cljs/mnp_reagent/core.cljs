(ns mnp-reagent.core
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [mnp-reagent.country-desc :refer [country-desc]]
              [mnp-reagent.calculate :refer [calculate-items]]
              [mnp-reagent.table :refer [table table-header table-header-detail table-body table-row table-detail ]]))

(defonce app-db (atom {:income 50000,
                   :sort-by [:effective_total, :asc]}))

(defn dir-sort-by [coll [f dir]]
  (let [sorted (sort-by #(get % f) coll)]
    (if (= dir :asc)
      sorted
      (reverse sorted))))

(def income (reaction (get @app-db :income)))
(def -sort-by (reaction (get @app-db :sort-by)))
(def -country-items (reaction (calculate-items country-desc @income)))
(def country-items
  (reaction (dir-sort-by @-country-items @-sort-by)))

(defn set-income [app-db [v]]
  (assoc app-db :income v))

(defn set-sort-by [app-db [prop dir]]
  (assoc app-db :sort-by [prop dir]))

(def handlers
  {:set-income set-income
   :set-sort-by set-sort-by})

(defn handle [[type & args]]
  (let [handler (handlers type)]
    (reset! app-db (handler @app-db args))))


;; ---
;; Components

(defn format-num
  ([x] (format-num x {}))
  ([x {:keys [precision prefix postfix] :or {precision 2 prefix "" postfix ""}}]
   (str prefix (.toFixed x 2) postfix)))

(def format-money #(format-num % {:prefix "$"}))
(def format-percent #(format-num % {:postfix "%"}))

(defn country-row [item]
  [:div.country-row
   [table-row
    [table-detail (get item :country)]
    [table-detail (format-money (get item :effective_tax))]
    [table-detail (format-money (get item :effective_soc_sec))]
    [table-detail (format-percent (get item :effective_percent))]
    [table-detail (format-money (get item :effective_total))]]])

(defn html-char [code]
  [:span {:dangerouslySetInnerHTML {:__html code}}])


(defn header-sortable-item [name property [sel dir]]
  (let [dir (if (= sel property) dir nil)
        click-dir (if (= dir :asc) :desc :asc)]
    [table-header-detail {:on-click #(handle [:set-sort-by property click-dir])}
                         name
                         [html-char (case dir
                                          :asc " &darr;"
                                          :desc " &uarr;"
                                          nil)]]))

(defn result-table [items]
  (fn []
    [table
      [table-header
        [header-sortable-item "Country" :country @-sort-by]
        [header-sortable-item "Eff income tax" :effective_tax @-sort-by]
        [header-sortable-item "Eff soc sec" :effective_soc_sec @-sort-by]
        [header-sortable-item "Eff rate" :effective_percent @-sort-by]
        [header-sortable-item "Eff total" :effective_total @-sort-by]]
      [table-body
        (map (fn [x] [country-row x])
              items)]]))

;; -------------------------
;; Views

(defn main-page []
  (let [_income (atom 20000)]
    (fn []
      [:div.Calculator
        [:h2 "Intl Tax Calculator"]
        [:form {:on-submit (fn [e]
                              (-> e
                                  (aget "target" "elements" 0 "value")
                                  (js/parseInt)
                                  ((fn [x] [:set-income x]))
                                  (handle))
                              (.preventDefault e))}
          [:input {:placeholder "Annual income, e.g. 50000" :default-value @income}]
          [:button "Calculate"]]
        [:p "Assuming self-employment (freelance or working remotely) and no deductibles."]
        ^{:key [@-sort-by @income]}[result-table @country-items]])))

(defn home-page []
  [:div [:h2 "Welcome to mnp-reagent"]
   [:div [:a {:href "/about"} "go to about page"]]])

(defn about-page []
  [:div [:h2 "About mnp-reagent"]
   [:div [:a {:href "/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'main-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
