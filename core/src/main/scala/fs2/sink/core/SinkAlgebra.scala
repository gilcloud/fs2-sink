package fs2.sink.core

trait SinkAlgebra[F[_], K, V, O] {

  def keyExtractor: Option[KeyExtract[V, K]]

  def key(inputRecord: InputRecord[K, V]): K = keyExtractor.fold(inputRecord.key)(_.extract(inputRecord.value))

  def payloadTransform: Transformer[V, O]

  def updateRecord(record: InputRecord[K, V]): F[String]

  def getRecord(record: InputRecord[K, V]): F[String]

}
