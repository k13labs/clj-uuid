(ns clj-uuid.v1-test
  "Time based UUIDs tests"
  (:require [clojure.test   :refer :all]
            [clojure.set]
            [clj-uuid :as uuid :refer [v7 v6 v1 get-timestamp]]
            [clj-uuid.clock :as clock]))

(deftest check-v1-concurrency
  (doseq [concur (range 5 9)]
    (let [extent    100000
          agents    (map agent (repeat concur nil))
          working   (map #(send-off %
                            (fn [state]
                              (repeatedly extent v1)))
                      agents)
          _         (apply await working)
          answers   (map deref working)]
      (testing "single-thread v1 uuid uniqueness..."
        (is (=
              (* concur extent)
              (apply + (map (comp count set) answers)))))
      (testing "concurrent v1 uuid uniqueness..."
        (is (=
              (* concur extent)
              (count (apply clojure.set/union (map set answers))))))
      (testing "concurrent v1 monotonic increasing..."
        (is (every? identity
              (map #(apply < (map get-timestamp %)) answers)))))))

(deftest check-v6-concurrency
  (doseq [concur (range 5 9)]
    (let [extent    100000
          agents    (map agent (repeat concur nil))
          working   (map #(send-off %
                            (fn [state]
                              (repeatedly extent v6)))
                      agents)
          _         (apply await working)
          answers   (map deref working)]
      (testing "single-thread v6 uuid uniqueness..."
        (is (=
              (* concur extent)
              (apply + (map (comp count set) answers)))))
      (testing "concurrent v6 uuid uniqueness..."
        (is (=
              (* concur extent)
              (count (apply clojure.set/union (map set answers))))))
      (testing "concurrent v6 monotonic increasing..."
        (is (every? identity (map (partial apply uuid/<) answers)))))))

(deftest check-get-timestamp
  (let [time (clock/monotonic-time)]
    (with-redefs [clock/monotonic-time (constantly time)]
      (is (= time (uuid/get-timestamp (v1)))
          "Timestamp should be retrievable from v1 UUID")
      (is (= time (uuid/get-timestamp (v6)))
          "Timestamp should be retrievable from v6 UUID"))))

(deftest check-v7-concurrency
  (doseq [concur (range 5 9)]
    (let [extent    100000
          agents    (map agent (repeat concur nil))
          working   (map #(send-off %
                            (fn [state]
                              (repeatedly extent v7)))
                      agents)
          _         (apply await working)
          answers   (map deref working)]
      (testing "single-thread v7 uuid uniqueness..."
        (is (=
              (* concur extent)
              (apply + (map (comp count set) answers)))))
      (testing "concurrent v7 uuid uniqueness..."
        (is (=
              (* concur extent)
              (count (apply clojure.set/union (map set answers))))))
      (testing "concurrent v7 monotonic increasing..."
        (is (every? identity
              (map (partial apply uuid/<) answers)))))))
