(ns directionalsurvey.utils)

(defn gentabledata [len]
  (reduce #(conj %1 [%2 %2 0])
          []
          (map #(* % 5) (range 100 (+ 100 len)))))

(defn init-tableconfig []
  {:colHeaders ["MD" "TVD" "Deviation"]
   :data        (gentabledata 15)
   :rowHeaders  false
   :contextMenu true})
