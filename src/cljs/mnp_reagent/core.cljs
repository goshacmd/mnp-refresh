(ns mnp-reagent.core
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [mnp-reagent.country-desc :refer [country-desc]]
              [mnp-reagent.calculate :refer [calculate-items]]
              [mnp-reagent.table :refer [table table-header table-body table-column-row]]))

(def app-db (atom {:income 50000,
                   :sort-by :effective_total}))

(def income (reaction (get @app-db :income)))
(def -country-items (reaction (calculate-items country-desc @income)))
(def country-items
  (reaction (sort-by #(get % (get @app-db :sort-by)) @-country-items)))

(defn set-income [v]
  (->> v
       (assoc @app-db :income)
       (reset! app-db)))

;; ---
;; Components

(defn format-num
  ([x] (format-num x {}))
  ([x {:keys [precision prefix postfix] :or {precision 2 prefix "" postfix ""}}]
   (str prefix (.toFixed x 2) postfix)))

(def format-money #(format-num % {:prefix "$"}))
(def format-percent #(format-num % {:postfix "%"}))

(defn country-row [item column-keys]
  [:div.country-row
    [table-column-row column-keys item]])

(defn id [x] x)
(defn result-table [items]
  (let [columns [["Country" :country]
                 ["Eff income tax" :effective_tax format-money]
                 ["Eff soc sec" :effective_soc_sec format-money]
                 ["Eff rate" :effective_percent format-percent]
                 ["Eff total" :effective_total format-money]]
        column-names (map first columns)
        column-keys (map (fn [x] [(second x) (get x 2 id)]) columns)]
    [table
     [table-header column-names]
     [table-body
      (map (fn [x] [country-row x column-keys])
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
                                  (set-income))
                              (.preventDefault e))}
          [:input {:placeholder "Annual income, e.g. 50000" :default-value @income}]
          [:button "Calculate"]]
        [:p "Assuming self-employment (freelance or working remotely) and no deductibles."]
        ^{:key @income}[result-table @country-items]])))

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
