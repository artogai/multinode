package dev.ogai.multinode.api

import dev.ogai.multinode.api.EmbeddedKafkaZ.EmbeddedKafkaZ
import dev.ogai.multinode.api.Utils.{ freePort, freePorts }
import dev.ogai.multinode.grpc.Api.ZioApi.ApiClient
import dev.ogai.multinode.grpc.Api.{ ListGamesReq, ListGamesResp, ZioApi }
import dev.ogai.multinode.model.Types.{ GameId, UserName }
import dev.ogai.multinode.model.game.Game.Game
import dev.ogai.multinode.model.kafka.{ Topic, Topics }
import io.grpc.ManagedChannelBuilder
import scalapb.zio_grpc.ZManagedChannel
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.Console
import zio.logging.Logging
import zio.random.Random
import zio.test.Assertion.equalTo
import zio.test._
import zio.{ Has, RLayer, ZIO, ZLayer, ZManaged }

import java.util.UUID

object AppISpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("App spec")(
      testM("should-startup") {
        ZIO
          .service[ApiClient.Service]
          .flatMap { client =>
            client
              .listGames(ListGamesReq(UserName("tyoma92")))
              .mapError(_.asRuntimeException())
              .map(assert(_)(equalTo(ListGamesResp())))
          }
      }
    ).provideSomeLayer(testLayer)
      .provideCustomLayerShared(suiteLayer)
      .mapError(TestFailure.fail)

  lazy val testLayer =
    (Clock.any ++ Blocking.any ++ Random.any ++ Logging.any ++ EmbeddedKafkaZ.any) >+>
      configLayer >+>
      Kafka.consumerSettings >+>
      topicLayer >+>
      clientLayer >+>
      Main.server

  lazy val suiteLayer =
    (Clock.live ++ Random.live ++ Blocking.any ++ Console.any) >+>
      Logging.console() >+>
      kafkaLayer

  lazy val configLayer: RLayer[EmbeddedKafkaZ, Has[Config]] =
    (for {
      embKafka         <- ZIO.service[EmbeddedKafkaZ.Service]
      grpcPort         <- freePort
      bootstrapServers <- embKafka.bootstrapServers
    } yield Config(bootstrapServers, grpcPort)).toLayer

  lazy val topicLayer: RLayer[EmbeddedKafkaZ with Blocking, Has[Topic[GameId, Game]]] =
    (for {
      embKafka <- ZIO.service[EmbeddedKafkaZ.Service]
      topicName = Topics.games.name + "_" + UUID.randomUUID().toString
      topic     = Topics.games.copy(name = topicName)
      bootstrapServers <- embKafka.bootstrapServers
      _                <- Kafka.createTopic(bootstrapServers, topic)
    } yield topic).toLayer

  lazy val kafkaLayer: RLayer[Blocking with Logging, EmbeddedKafkaZ] =
    (for {
      zkPort      <- ZLayer.fromEffect(freePort)
      brokerPorts <- ZLayer.fromEffect(freePorts(3))
      embKafka    <- EmbeddedKafkaZ.live(zkPort.get[Int], brokerPorts.get[List[Int]])
    } yield embKafka)

  lazy val clientLayer: RLayer[Has[Config], ApiClient] =
    (for {
      cfg <- ZManaged.service[Config]
      builder = ManagedChannelBuilder
                  .forAddress("localhost", cfg.grpcPort)
                  .usePlaintext()
      channel = ZManagedChannel(builder)
      client <- ZioApi.ApiClient.managed(channel)
    } yield client).toLayer

}
