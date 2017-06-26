(ns directionalsurvey.utils
  (:require [reagent.core :as reagent]
            [cognitect.transit :as t]
            [precept.core :refer [subscribe then]]))

(defn gentabledata [len]
  (reduce #(conj %1 [%2 %2 0])
          []
          (map #(* % 5) (range 100 (+ 100 len)))))

(defn init-tableconfig []
  {:colHeaders ["MD" "TVD" "Deviation"]
   :data        (gentabledata 3)
   :rowHeaders  false
   :contextMenu true})

(defn gen-chart-config-handson
  [tableconfig]
  (let [currenttableconfig  tableconfig
        ret (atom {
                   :chart    {:type     "line"
                              :zoomType "xy"}
                   :title    {:text "Directional Survey"}
                   :subtitle {:text "An experiment"}
                   :xAxis    {:title      {:text "X"}}
                   :yAxis    {:title      {:text "Y"}
                              :reversed true}
                   :credits  {:enabled false}})]
    (let [tabledata (:data currenttableconfig)
          tmptabledata (into [[0 0 0]] tabledata)
          tmptabledata1 (mapv (fn [in]
                                (let [md (get in 0)
                                      tvd (get in 1)
                                      dev (get in 2)]
                                  [md tvd dev 0]))
                              tmptabledata)
          tmptable (reduce (fn [data rowIdx]
                             (let [md1 (get-in data [(- rowIdx 1) 0])
                                   md2 (get-in data [rowIdx 0])
                                   x1 (get-in data [(- rowIdx 1) 3])
                                   dev2 (get-in data [rowIdx 2])
                                   x2 (+ x1 (* (- md2 md1) (js/Math.sin (* (/ dev2 180.0) js/Math.PI))))]
                               (assoc-in data [rowIdx 3] x2)))
                           tmptabledata1
                           (range 1 (count tmptabledata1)))
          tmptable1 (rest tmptable)
          gendata (mapv (fn [data]
                          (let [y (get data 1)
                                x (get data 3)]
                            [x y]))
                        tmptable1)
          mydata [{:name "Directional survey" :data gendata}]]
      (swap! ret assoc-in [:series] mydata))
    ret))

(defn handle-user-change-MD [tableconfig action]
  (let [dataTable (:data tableconfig)
        rowIdx (:row action)
        colIdx (:col action)
        newMD (:val action)
        tmpDataTable1 (assoc-in dataTable [rowIdx colIdx] newMD)
        tmpDataTable1 (vec (sort #(compare (get %1 0) (get %2 0)) tmpDataTable1))
        tmpDataTable2 (assoc-in tmpDataTable1 [0 2] (* 180.0
                                                       (/
                                                         (js/Math.acos
                                                           (/ (double (get-in tmpDataTable1 [0 1])) (double (get-in tmpDataTable1 [0 0]))))
                                                         js/Math.PI)))
        newDataTable (reduce (fn [data rowIdx]
                               (let [md1 (get-in data [(- rowIdx 1) 0])
                                     md2 (get-in data [rowIdx 0])
                                     tvd1 (get-in data [(- rowIdx 1) 1])
                                     tvd2 (get-in data [rowIdx 1])
                                     dev3 (* 180.0
                                             (/
                                               (js/Math.acos (/ (double (- tvd1 tvd2)) (double (- md1 md2))))
                                               js/Math.PI))]
                                 (assoc-in data [rowIdx 2] dev3)))
                             tmpDataTable2
                             (range 1 (count tmpDataTable2)))
        newtableconfig (assoc tableconfig :data newDataTable)]
    newtableconfig))

(defn handle-user-change-TVD [tableconfig action]
  (let [dataTable (:data tableconfig)
        rowIdx (:row action)
        colIdx (:col action)
        newTVD (:val action)
        tmpDataTable1 (assoc-in dataTable [rowIdx colIdx] newTVD)
        tmpDataTable2 (assoc-in tmpDataTable1 [0 2] (* 180.0
                                                       (/
                                                         (js/Math.acos
                                                           (/ (double (get-in tmpDataTable1 [0 1])) (double (get-in tmpDataTable1 [0 0]))))
                                                         js/Math.PI)))
        newDataTable (reduce (fn [data rowIdx]
                               (let [md1 (get-in data [(- rowIdx 1) 0])
                                     md2 (get-in data [rowIdx 0])
                                     tvd1 (get-in data [(- rowIdx 1) 1])
                                     tvd2 (get-in data [rowIdx 1])
                                     dev3 (* 180.0
                                             (/
                                               (js/Math.acos (/ (double (- tvd1 tvd2)) (double (- md1 md2))))
                                               js/Math.PI))]
                                 (assoc-in data [rowIdx 2] dev3)))
                             tmpDataTable2
                             (range 1 (count tmpDataTable2)))
        newtableconfig (assoc tableconfig :data newDataTable)]
    newtableconfig))

(defn handle-user-change-Deviation [tableconfig action]
  (let [dataTable (:data tableconfig)
        rowIdx (:row action)
        colIdx (:col action)
        newDeviation (:val action)
        tmpDataTable1 (assoc-in dataTable [rowIdx colIdx] newDeviation)
        tmpDataTable2 (assoc-in tmpDataTable1 [0 1] (* (get-in tmpDataTable1 [0 0]) (Math/cos (* (/ (get-in tmpDataTable1 [0 2]) 180.0) Math/PI))))
        newDataTable (reduce (fn [data rowIdx]
                               (let [md1 (get-in data [(- rowIdx 1) 0])
                                     md2 (get-in data [rowIdx 0])
                                     tvd1 (get-in data [(- rowIdx 1) 1])
                                     dev2 (get-in data [rowIdx 2])
                                     tvd2 (+ tvd1 (* (- md2 md1) (js/Math.cos (* (/ dev2 180.0) js/Math.PI))))]
                                 (assoc-in data [rowIdx 1] tvd2)))
                             tmpDataTable2
                             (range 1 (count tmpDataTable2)))
        newtableconfig (assoc tableconfig :data newDataTable)]
    newtableconfig))

(defn handle-table-actions [tableconfig action]
  (let [colIdx (:col action)]
    ;(.log js/console "action: " action)
    (cond
      (= 0 colIdx) (handle-user-change-MD tableconfig action)
      (= 1 colIdx) (handle-user-change-TVD tableconfig action)
      (= 2 colIdx) (handle-user-change-Deviation tableconfig action))))

(defn handle-global-table [myinitconfig data]
  ;(.log js/console (str "myinitconfig: " myinitconfig))
  ;(.log js/console (str "data: " data))
  (let [newtableconfig (reduce (fn [indata action]
                                 (handle-table-actions indata action)) myinitconfig data)]
    ;(.log js/console (str "newconfig: " newtableconfig))
    (then [:global :globaltableconfig (let [w (t/writer :json)]
                                        (t/write w newtableconfig))])))

(defn handle-local-table [myinitconfig data]
  (let [newtableconfig (reduce (fn [indata action]
                                 (handle-table-actions indata action)) myinitconfig data)]
    (then [:global :localtableconfig (let [w (t/writer :json)]
                                       (t/write w newtableconfig))])))

