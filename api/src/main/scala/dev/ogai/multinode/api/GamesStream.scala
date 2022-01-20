package dev.ogai.multinode.api

import dev.ogai.multinode.model.Types.GameId
import dev.ogai.multinode.model.game.Game.Game
import dev.ogai.multinode.model.kafka.Topic
import zio.kafka.consumer.{ Consumer, ConsumerSettings, Subscription }
import zio.kafka.serde.Deserializer
import zio.logging.{ Logger, Logging }
import zio.random.{ Random, nextUUID }
import zio.stream.ZStream
import zio.{ Has, RLayer, ZIO, ZManaged }

object GamesStream {
  type GamesStream = Has[GamesStream.Service]

  val games: ZStream[GamesStream, Throwable, Game] =
    ZStream.serviceWithStream[Service](_.games)

  trait Service {
    def games: ZStream[Any, Throwable, Game]
  }

  object Service {

    lazy val kafka
        : RLayer[Has[ConsumerSettings] with Has[Topic[GameId, Game]] with Logging with Random with CB, GamesStream] = {

      val consumersM =
        ZManaged
          .services[ConsumerSettings, Topic[GameId, Game]]
          .flatMap { case (settings, topic) =>
            ZManaged
              .fromEffect(nextUUID)
              .flatMap { consumerBaseId =>
                ZManaged
                  .foreachPar(0.until(topic.partitions).toList) { part =>
                    Consumer
                      .make(settings.withClientId(s"c-api-games-$consumerBaseId-${part}"))
                      .map(_ -> part)
                  }
              }
          }

      val serviceM =
        ZManaged
          .services[Topic[GameId, Game], Logger[String]]
          .flatMap { case (topic, log) =>
            consumersM
              .map { consumers =>
                new Service {
                  val games: ZStream[Any, Throwable, Game] =
                    ZStream.mergeAllUnbounded()(
                      consumers.map { case (cons, partition) =>
                        cons
                          .subscribeAnd(Subscription.manual(topic.name, partition))
                          .plainStream(Deserializer.byteArray, Deserializer.byteArray)
                          .mapM { rec =>
                            topic.decode(rec.record) match {
                              case Right(v) => ZIO.succeed(Some(v.value()))
                              case Left(ex) => log.error(s"Failed to decode msg: ${rec} err:${ex}").as(None)
                            }
                          }
                          .collectSome
                      }: _*
                    )
                }
              }
          }

      serviceM.toLayer
    }

  }

}
