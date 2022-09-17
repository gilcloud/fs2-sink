package fs2.sink

package object core {

  type Transform[A] = Transformer[A, _]
  type KeyEx[V] = Option[KeyExtract[V, Option[String]]]

}
