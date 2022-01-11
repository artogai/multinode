package dev.ogai.multinode.api

import dev.ogai.multinode.model.kafka.{ Topic, Topics }
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import scalapb.zio_grpc.{ Server, ServerLayer }
import zio.blocking.Blocking
import zio.kafka.admin.{ AdminClient, AdminClientSettings }
import zio.kafka.consumer.ConsumerSettings
import zio.logging.Logging
import zio.{ ExitCode, Has, RIO, RLayer, ULayer, URIO, ZEnv, ZIO, ZLayer }

object Main extends zio.App {

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    (createTopic(Topics.games) *>
      server.build.useForever)
      .provideCustomLayer(settings)
      .exitCode

  val settings: ULayer[Has[Config] with Has[ConsumerSettings] with Logging] =
    Logger.live ++
      Config.live >+> KafkaSettings.consumer

  val server: RLayer[Has[Config] with Has[ConsumerSettings] with Logging with CB, Has[Server.Service]] =
    ZLayer
      .service[Config]
      .flatMap { cfg =>
        ServerLayer
          .fromServiceLayer(
            ServerBuilder
              .forPort(cfg.get[Config].grpcPort)
              .addService(ProtoReflectionService.newInstance())
          )(
            Api.Service.live
          )
      }

  def createTopic[V](topic: Topic[V]): RIO[Has[Config] with Blocking, Unit] =
    ZIO
      .service[Config]
      .flatMap { cfg =>
        AdminClient
          .make(AdminClientSettings(cfg.bootstrapServers))
          .use(_.createTopic(topic.asZioNewTopic))
      }

}
