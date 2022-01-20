package dev.ogai.multinode.api

import dev.ogai.multinode.model.Types.GameId
import dev.ogai.multinode.model.game.Game.Game
import dev.ogai.multinode.model.kafka.Topic
import io.grpc.netty.NettyServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import scalapb.zio_grpc.{ Server, ServerLayer }
import zio.blocking.Blocking
import zio.kafka.consumer.ConsumerSettings
import zio.logging.Logging
import zio.random.Random
import zio.{ ExitCode, Has, RLayer, ULayer, URIO, ZEnv, ZLayer }

object Main extends zio.App {

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    server
      .build
      .useForever
      .provideCustomLayer(
        Blocking.any
          ++ Logger.live
          ++ settings
          >+> Kafka.liveTopic
      )
      .exitCode

  lazy val settings: ULayer[Has[Config] with Has[ConsumerSettings]] =
    Config.live >+> Kafka.consumerSettings

  lazy val server: RLayer[Has[Config] with Has[ConsumerSettings] with Has[
    Topic[GameId, Game]
  ] with Logging with Random with CB, Has[Server.Service]] =
    ZLayer
      .service[Config]
      .flatMap { cfg =>
        val grpcPort = cfg.get[Config].grpcPort

        ZLayer
          .fromEffect(Logging.info(s"Starting grpc server port=[$grpcPort]"))
          .flatMap { _ =>
            ServerLayer
              .fromServiceLayer(
                NettyServerBuilder
                  .forPort(grpcPort)
                  .addService(ProtoReflectionService.newInstance())
              )(
                Api.Service.live
              )
              .tap(_ => Logging.info("Grpc server started"))
          }
      }

}
