//package fs2.sink.source.kafka
//
//import cats.effect.kernel.Async
//import cats.effect.{Concurrent, IO, Resource}
//import cats.implicits.{catsSyntaxFlatMapOps, toFunctorOps}
//import fs2.kafka._
//import fs2.kafka.{CommittableConsumerRecord, KafkaConsumer}
//
//class KafkaSource[K, V](kafkaConfig: KafkaConfig, kafkaConsumer: KafkaConsumer[IO, K, V]) extends Source[IO, CommittableConsumerRecord, K, V] {
//  override def setup: IO[fs2.Stream[IO, CommittableConsumerRecord[IO, K, V]]] =
//    kafkaConsumer.subscribeTo(kafkaConfig.inputTopic) >> IO.delay(
//      kafkaConsumer.stream
//    )
//
//  override def close: fs2.Stream[F, CommittableConsumerRecord[F, K, V]] = ???
//}
