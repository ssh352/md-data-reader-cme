(ns cme-compile.md-incremental-refresh-book32
    (:use [pcap-common.macros]
          [pcap-common.parquet-util]
          [cme-compile.md-incremental-refresh-book])
    (:import [mktdata MDIncrementalRefreshBook32Decoder MDIncrementalRefreshBook46Decoder   MDIncrementalRefreshBook32Decoder$NoMDEntriesDecoder MDIncrementalRefreshBook46Decoder$NoMDEntriesDecoder MDIncrementalRefreshBook32Decoder$NoOrderIDEntriesDecoder MDIncrementalRefreshBook46Decoder$NoOrderIDEntriesDecoder]
             [java.util Map]
             [org.apache.parquet.io.api Binary RecordConsumer]
             [org.apache.parquet.hadoop ParquetRecordWriter]))

(defn- message-write [^RecordConsumer rconsumer #^long start-at ^MDIncrementalRefreshBook32Decoder top-level]

  (let [repetition-decoder (.noMDEntries top-level)]
    (when (> (.count repetition-decoder) 0)
      (.startField rconsumer "md_entries" (+ start-at 0))
      (while (.hasNext ^java.util.Iterator repetition-decoder)
        (let [^MDIncrementalRefreshBook32Decoder$NoMDEntriesDecoder repetition (.next ^java.util.Iterator repetition-decoder)]

          (.startGroup rconsumer)
          (let [src-value (.mantissa (.mDEntryPx repetition))]
            (when (not= src-value (. mktdata.PRICENULLDecoder mantissaNullValue))
              (with-field-new rconsumer "md_entry_px_mantissa" 0 (.addLong src-value))))

          (with-field-new rconsumer "md_entry_px_exponent" 1 (.addInteger -7))

          (let [src-value (.mDEntrySize repetition)]
            (when (not= src-value (. MDIncrementalRefreshBook32Decoder$NoMDEntriesDecoder mDEntrySizeNullValue))
              (with-field-new rconsumer "md_entry_size" 2 (.addInteger src-value))))

          (with-field-new rconsumer "security_id" 3 (.addInteger (.securityID repetition)))

          (with-field-new rconsumer "rpt_seq" 4 (.addInteger (.rptSeq repetition)))

          (let [src-value (.numberOfOrders repetition)]
            (when (not= src-value (. MDIncrementalRefreshBook32Decoder$NoMDEntriesDecoder numberOfOrdersNullValue))
              (with-field-new rconsumer "number_of_orders" 5 (.addInteger src-value))))

          (with-field-new rconsumer "md_price_level" 6 (.addInteger (.mDPriceLevel repetition)))

          (with-field-new rconsumer "md_update_action" 7 (.addInteger (.value (.mDUpdateAction repetition))))

          (with-field-new rconsumer "md_entry_type" 8 (.addInteger (.value (.mDEntryType repetition))))

          (.endGroup rconsumer))) (.endField rconsumer "md_entries" (+ start-at 0))))

  (do (adapt-order-id-entries MDIncrementalRefreshBook32Decoder$NoOrderIDEntriesDecoder (+ start-at 1))) (adapt-common (+ start-at 2) MDIncrementalRefreshBook32Decoder))
(defn ^ParquetRecordWriter make-record-writer [prefix]
  (make-parquet-integrated-writer (str prefix "-md-incremental-refresh-book32.parquet")
                                  (make-loopless-proxy message-write (compile-schema message-schema) {})))
(defn decode [subOffset actingBlockLength actingVersion buffer]
  (.wrap (new MDIncrementalRefreshBook32Decoder) buffer subOffset actingBlockLength actingVersion))
