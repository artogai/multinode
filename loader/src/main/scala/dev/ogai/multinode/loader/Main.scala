package dev.ogai.multinode.loader

import cats.effect.{ Async, IO, IOApp }
import dev.ogai.multinode.model.kafka.Topic
import fs2.Stream.eval
import fs2.kafka.{ AdminClientSettings, KafkaAdminClient }
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp.Simple {

  override def run: IO[Unit] =
    (for {
      implicit0(log: Logger[IO]) <- eval(Slf4jLogger.create[IO])
      cfg                        <- eval(Config.load[IO])
      _                          <- App.loadGames[IO](cfg)
    } yield ())
      .compile
      .drain

  def createTopic[F[_]: Async, V](topic: Topic[V], cfg: Config.Kafka): F[Unit] =
    KafkaAdminClient
      .resource[F](AdminClientSettings(cfg.bootstrapServers))
      .use(_.createTopic(topic.asNewTopic))

}
