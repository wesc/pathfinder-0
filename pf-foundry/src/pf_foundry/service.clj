(ns pf-foundry.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [ns-tracker.core :refer [ns-tracker]]
            [hikari-cp.core :as hikari]
            [clojure.java.jdbc :as jdbc]
            [clojure.data.json :as json]
            [selmer.parser :as selmer]
            [ring.util.response :as ring-resp]))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (route/url-for ::about-page))))

(defn home-page
  [request]
  (ring-resp/response "Hello World!"))


(def datasource-options {:auto-commit        true
                         :read-only          false
                         :connection-timeout 30000
                         :validation-timeout 5000
                         :idle-timeout       600000
                         :max-lifetime       1800000
                         :minimum-idle       10
                         :maximum-pool-size  10
                         :pool-name          "db-pool"
                         :adapter            "postgresql"
                         :username           "postgres"
                         :password           "postgres"
                         :database-name      "postgres"
                         ;;:server-name        "localhost"
                         :server-name "postgres"
                         :port-number        5432
                         :register-mbeans    false})

(defonce datasource
  (delay (hikari/make-datasource datasource-options)))

;; fixme: how do we close the datasource for real?
;;  (hikari/close-datasource @datasource))


(defmacro make-objects-api [params sql]
  `(fn [request#]
     (let [path-params# (select-keys (:path-params request#) ~params)
           sql-text# (selmer.parser/render ~sql path-params#)]
       (jdbc/with-db-connection [conn# {:datasource @datasource}]
         (->>
          sql-text#
          (jdbc/query conn#)
          (map (fn [r#] (assoc r#
                               :version (json/read-str (:version r#))
                               :value (json/read-str (:value r#)))))
          (json/write-str)
          (ring-resp/response))))))

(defn api-catalog
  [request]
  (let [params (:path-params request)]
    (jdbc/with-db-connection [conn {:datasource @datasource}]
      (let [sql (format "select id, value from subtrees")
            rows (jdbc/query conn sql)
            ids (map :id rows)
            values (map #(->> % :value json/read-str) rows)
            results (into {} (map (fn [r] [(r "id") r]) values))]
        (ring-resp/response (json/write-str results))))))

(defn api-write-subtree
  [request]
  (jdbc/with-db-connection [conn {:datasource @datasource}]
    (let [params (:json-params request)
          id (:id params)
          value (json/write-str params)
          sql "insert into subtrees values (?, '', ?) on conflict (id) do update set value = EXCLUDED.value"]
      (jdbc/delete! conn :subtrees ["id = ?" id])
      (jdbc/insert! conn :subtrees {:id id :version "" :value value})
      (ring-resp/response (json/write-str {:write "ok"})))))

(def api-subtrees
  (make-objects-api
   [:id]
   "select * from subtrees where id = '{{id}}'"))


;; Defines "/" and "/about" routes with their associated :get handlers.
;; The interceptors defined after the verb map (e.g., {:get home-page}
;; apply to / and its children (/about).
(def common-interceptors [(body-params/body-params) http/html-body])

;; Tabular routes
(def routes #{["/" :get (conj common-interceptors `home-page)]
              ["/about" :get (conj common-interceptors `about-page)]
              ["/api/subtrees/:id" :get (conj common-interceptors `api-subtrees)]
              ["/api/subtrees" :post (conj common-interceptors `api-write-subtree)]
              ["/api/catalog" :get (conj common-interceptors `api-catalog)]})

;; Map-based routes
;(def routes `{"/" {:interceptors [(body-params/body-params) http/html-body]
;                   :get home-page
;                   "/about" {:get about-page}}})

;; Terse/Vector-based routes
;(def routes
;  `[[["/" {:get home-page}
;      ^:interceptors [(body-params/body-params) http/html-body]
;      ["/about" {:get about-page}]]]])

;; Consumed by pf-foundry.server/create-server
;; See http/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::http/interceptors []
              ::http/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::http/allowed-origins ["scheme://host:port"]
              ::http/allowed-origins ["http://localhost:8280"]

              ;; Tune the Secure Headers
              ;; and specifically the Content Security Policy appropriate to your service/application
              ;; For more information, see: https://content-security-policy.com/
              ;;   See also: https://github.com/pedestal/pedestal/issues/499
              ;;::http/secure-headers {:content-security-policy-settings {:object-src "'none'"
              ;;                                                          :script-src "'unsafe-inline' 'unsafe-eval' 'strict-dynamic' https: http:"
              ;;                                                          :frame-ancestors "'none'"}}

              ;; Root for resource interceptor that is available by default.
              ::http/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ;;  This can also be your own chain provider/server-fn -- http://pedestal.io/reference/architecture-overview#_chain_provider
              ::http/type :jetty
              ::http/host "0.0.0.0"
              ::http/port 8080
              ;; Options to pass to the container (Jetty)
              ::http/container-options {:h2c? true
                                        :h2? false
                                        ;:keystore "test/hp/keystore.jks"
                                        ;:key-password "password"
                                        ;:ssl-port 8443
                                        :ssl? false
                                        ;; Alternatively, You can specify you're own Jetty HTTPConfiguration
                                        ;; via the `:io.pedestal.http.jetty/http-configuration` container option.
                                        ;:io.pedestal.http.jetty/http-configuration (org.eclipse.jetty.server.HttpConfiguration.)
                                        }})
