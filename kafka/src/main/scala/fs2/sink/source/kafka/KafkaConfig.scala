package fs2.sink.source.kafka

import org.apache.kafka.common.config.SaslConfigs.{SASL_JAAS_CONFIG, SASL_MECHANISM}

case class KafkaConfig(bootstraps: String, clusterKey: String, clusterSecret: String, appId: String, inputTopic: String) {

  def properties: Map[String, String] =
    Map(
      "security.protocol" -> "SASL_SSL",
      SASL_MECHANISM -> "PLAIN",
      SASL_JAAS_CONFIG -> s"""org.apache.kafka.common.security.plain.PlainLoginModule   required username='$clusterKey'   password='$clusterSecret';"""
    )
}
