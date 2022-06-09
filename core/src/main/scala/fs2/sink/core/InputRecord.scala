package fs2.sink.core

case class InputRecord[K, V](key: K, value: V)
