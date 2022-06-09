package fs2.sink.source.kafka

import _root_.vulcan.Codec
import cats.effect.{IO, Resource}
import fs2.kafka._
import fs2.kafka.vulcan.{Auth, AvroSettings, SchemaRegistryClientSettings, avroDeserializer}
import fs2.sink.core.{KeyExtract, SinkAlgebra}

import scala.concurrent.duration.DurationInt

object KafkaRunner {

  type KeyEx[V] = Option[KeyExtract[V, Option[String]]]

  def runner[V: KeyEx: Codec, O](
      kafkaConfig: KafkaConfig,
      schemaRegistryConfig: SchemaRegistryConfig,
      sinkResource: Resource[IO, SinkAlgebra[IO, Option[String], V, O]]
  ): IO[Unit] =
    for {
      kafkaResource <- kafkaResource(kafkaConfig, schemaRegistryConfig)
      _ <- (for {
        kafkaCon <- kafkaResource
        sink <- sinkResource
      } yield (kafkaCon, sink))
        .use(rune[V, O])
    } yield ()

  def rune[V, O](resource: (KafkaConsumer[IO, Option[String], V], SinkAlgebra[IO, Option[String], V, O])): IO[Unit] = resource match {
    case (consumer, client) =>
      consumer.subscribeTo("") >>
        consumer.stream
          .mapAsync(8) { committable =>
            client
              .getRecord(committable.record.toInputRecord)
              .flatMap(IO.println)
              .as(committable.offset)
          }
          .through(commitBatchWithin(500, 10.seconds))
          .metered(25.milliseconds)
          .take(2)
          .compile
          .drain
  }

  def setupConsumer[V](kafkaConfig: KafkaConfig)(implicit recDe: RecordDeserializer[IO, V]): IO[ConsumerSettings[IO, Option[String], V]] = IO.apply {
    ConsumerSettings[IO, Option[String], V]
      .withAutoOffsetReset(AutoOffsetReset.Latest)
      .withBootstrapServers(kafkaConfig.bootstraps)
      .withProperties(kafkaConfig.properties)
      .withGroupId(kafkaConfig.appId)
  }

  def setupAvro(schemaRegistryConfig: SchemaRegistryConfig): IO[AvroSettings[IO]] = IO.apply {
    AvroSettings {
      SchemaRegistryClientSettings[IO](schemaRegistryConfig.url)
        .withAuth(Auth.Basic(schemaRegistryConfig.user, schemaRegistryConfig.password))
    }
  }

  def kafkaResource[V: Codec](kafkaConfig: KafkaConfig, schemaRegistryConfig: SchemaRegistryConfig): IO[Resource[IO, KafkaConsumer[IO, Option[String], V]]] = for {
    implicit0(a: AvroSettings[IO]) <- setupAvro(schemaRegistryConfig)
    implicit0(rec: RecordDeserializer[IO, V]) <- IO.apply { avroDeserializer[V].using(a) }
    settings <- setupConsumer(kafkaConfig)
  } yield KafkaConsumer.resource(settings)

}
