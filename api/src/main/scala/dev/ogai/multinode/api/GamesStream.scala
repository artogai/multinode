package dev.ogai.multinode.api

import dev.ogai.multinode.model.game.Game.Game
import dev.ogai.multinode.model.kafka.Topics
import zio.kafka.consumer.{ Consumer, ConsumerSettings, Subscription }
import zio.kafka.serde.Deserializer
import zio.logging.{ Logger, Logging }
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

    val kafka: RLayer[Has[ConsumerSettings] with Logging with CB, GamesStream] = {

      val consumersM =
        ZManaged
          .service[ConsumerSettings]
          .flatMap { settings =>
            ZManaged
              .foreachPar(0.until(Topics.games.partitions).toList) { part =>
                Consumer
                  .make(settings.withClientId(s"c-api-games-${part}"))
                  .map(_ -> part)
              }
          }

      val serviceM =
        ZManaged
          .service[Logger[String]]
          .flatMap { log =>
            consumersM
              .map { consumers =>
                new Service {
                  val games: ZStream[Any, Throwable, Game] =
                    ZStream.mergeAllUnbounded()(
                      consumers.map { case (cons, partition) =>
                        cons
                          .subscribeAnd(Subscription.manual(Topics.games.name, partition))
                          .plainStream(Deserializer.byteArray, Deserializer.byteArray)
                          .mapM { rec =>
                            Topics.games.decode(rec.record) match {
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
