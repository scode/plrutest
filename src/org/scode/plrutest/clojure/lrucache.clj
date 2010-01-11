(ns org.scode.plrutest.clojure.lrucache)

(defn- asserting
  ([ok? val reason]
     (asserting ok? val))
  ([ok? val]
     (assert (ok? val))
     val))

(defn lru-make
  [max-size]
  { :kvmap {}                ; key/value map of actual items
    :rkmap (sorted-map)      ; recenticity -> key
    :krmap {}                ; key -> recenticity
    :size 0
    :max-size (asserting #(> %1 1) max-size "implementation breaks if less than 2"),
    :mutation-counter 0 })

(defn- remove-oldest
  [cache]
  (let [[recenticity key] (first (:rkmap cache))]
    (conj cache
          [:kvmap (dissoc (:kvmap cache) key)]
          [:rkmap (dissoc (:rkmap cache) recenticity)]
          [:krmap (dissoc (:krmap cache) key)]
          [:size (- (:size cache) 1)])))

(defn lru-put
  [cache key value]
  (assert (not (= nil value))) ; nil is not supported

  (let [had-key (contains? (:kvmap cache) key)
        should-remove (and (not had-key) (>= (:size cache) (:max-size cache)))
        new-kvmap (conj (:kvmap cache) [key value])
        new-size (if had-key (:size cache) (+ 1 (:size cache)))
        old-r (if had-key ((:krmap cache) key) nil)
        new-rkmap (let [with-new-added (conj (:rkmap cache) [(:mutation-counter cache) key])]
                    (if old-r
                      (dissoc with-new-added old-r)
                      with-new-added))
        new-krmap (conj (:krmap cache) [key (:mutation-counter cache)])
        new-mutation-counter (+ 1 (:mutation-counter cache))]
    (let [new-cache (conj cache
                          [:kvmap new-kvmap]
                          [:rkmap new-rkmap]
                          [:krmap new-krmap]
                          [:size new-size]
                          [:mutation-counter new-mutation-counter])]
      (if should-remove
        (remove-oldest new-cache)
        new-cache))))

(defn lru-get
  ([cache key]
     (lru-get cache key nil))
  ([cache key not-found]
     (let [not-found-sym (gensym)
           value ((get cache :kvmap key not-found-sym))]
       (if (= value not-found-sym)
         not-found
         (let [new-rkmap (conj (dissoc (:rkmap cache) ((:krmap cache) key)) [(:mutation-counter cache) key])
               new-krmap (conj (dissoc (:krmap cache) key) [key (:mutation-counter cache)])
               new-mutation-counter (+ 1 (:mutation-counter cache))]
           [value (conj cache
                        [:rkmap new-rkmap]
                        [:krmap new-krmap]
                        [:mutation-counter new-mutation-counter])])))))

