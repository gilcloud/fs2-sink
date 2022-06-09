package fs2.sink.source.kafka

import fs2.Stream

trait Source[F[_], X[_[`1`], _, _], K, V] {

  def setup: F[Stream[F, X[F, K, V]]]

  def close: Stream[F, X[F, K, V]]

}
