package fs2.sink.source

import fs2.kafka.ConsumerRecord
import fs2.sink.core.InputRecord

package object kafka {

  implicit class ConsumerRecordOps[K, V](cons: ConsumerRecord[K, V]) {
    def toInputRecord: InputRecord[K, V] = InputRecord(cons.key, cons.value)
  }
}
