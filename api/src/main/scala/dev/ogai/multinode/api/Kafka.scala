package dev.ogai.multinode.api

import dev.ogai.multinode.model.Types.GameId
import dev.ogai.multinode.model.game.Game.Game
import dev.ogai.multinode.model.kafka.{ Topic, Topics }
import zio.blocking.Blocking
import zio.kafka.admin.{ AdminClient, AdminClientSettings }
import zio.kafka.consumer.Consumer.{ AutoOffsetStrategy, OffsetRetrieval }
import zio.kafka.consumer.ConsumerSettings
import zio.{ Has, RIO, RLayer, URLayer, ZIO, ZLayer }

object Kafka {

  lazy val consumerSettings: URLayer[Has[Config], Has[ConsumerSettings]] =
    ZIO
      .service[Config]
      .map { cfg =>
        ConsumerSettings(cfg.bootstrapServers)
          .withOffsetRetrieval(OffsetRetrieval.Auto(AutoOffsetStrategy.Earliest))
      }
      .toLayer

  lazy val liveTopic: RLayer[Has[Config] with Blocking, Has[Topic[GameId, Game]]] =
    ZLayer
      .fromServiceM { cfg =>
        createTopic(cfg.bootstrapServers, Topics.games)
          .as(Topics.games)
      }

  def createTopic[K, V](bootstrap: List[String], topic: Topic[K, V]): RIO[Blocking, Unit] =
    AdminClient
      .make(AdminClientSettings(bootstrap))
      .use(_.createTopic(topic.asZioNewTopic))

}
