package dev.ogai.multinode.api

import zio.kafka.consumer.Consumer.{ AutoOffsetStrategy, OffsetRetrieval }
import zio.kafka.consumer.ConsumerSettings
import zio.{ Has, URLayer, ZIO }

object KafkaSettings {

  val consumer: URLayer[Has[Config], Has[ConsumerSettings]] =
    ZIO
      .service[Config]
      .map { cfg =>
        ConsumerSettings(cfg.bootstrapServers)
          .withOffsetRetrieval(OffsetRetrieval.Auto(AutoOffsetStrategy.Earliest))
      }
      .toLayer
}
