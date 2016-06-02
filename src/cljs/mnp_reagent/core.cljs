(ns mnp-reagent.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [mnp-reagent.country-desc :refer [country-desc]]
              [mnp-reagent.calculate :refer [calculate-items]]
              [mnp-reagent.table :refer [data-table]]))

;; ---
;; Components

(defn format-num
  ([x] (format-num x {}))
  ([x {:keys [precision prefix postfix] :or {precision 2 prefix "" postfix ""}}]
   (str prefix (.toFixed x 2) postfix)))

(def format-money #(format-num % {:prefix "$"}))
(def format-percent #(format-num % {:postfix "%"}))

(defn result-table [items]
  (data-table {:columns [["Country" :country]
                         ["Eff income tax" :effective_tax format-money]
                         ["Eff soc sec" :effective_soc_sec format-money]
                         ["Eff rate" :effective_percent format-percent]
                         ["Eff total" :effective_total format-money]]
               :data items}))

;; -------------------------
;; Views

(defn main-page []
  (let [income (atom 20000)]
    (fn []
      [:div
      [:h2 "Cal"]
       [:form {:on-submit (fn [e]
                            (reset! income (js/parseInt (aget e "target" "elements" 0 "value")))
                            (.preventDefault e))}
        [:input {:placeholder "Annual income, e.g. 50000" :default-value @income}]
        [:button "Calculate"]]
      [:p "Assuming self-employment (freelance or working remotely) and no deductibles."]
      (if @income [result-table (calculate-items country-desc @income)] nil)])))

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
