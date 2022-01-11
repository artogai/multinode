package dev.ogai.multinode.loader

import scala.concurrent.duration._

import cats.effect.Sync
import dev.ogai.multinode.model.Types.UserName
import org.http4s.Uri
import org.http4s.syntax.literals._

case class Config(kafka: Config.Kafka, lichess: Config.Lichess)

object Config {
  case class Kafka(bootstrapServers: String, deliveryTimeout: FiniteDuration)
  case class Lichess(api: Uri, users: Set[UserName])

  def load[F[_]](implicit F: Sync[F]): F[Config] =
    F.pure {
      val kafka = Kafka(
        bootstrapServers = "kafka:8082",
        deliveryTimeout = 1.minute,
      )

      val lichess = Lichess(
        api = uri"https://lichess.org/api",
        users = Set(
          UserName("tyoma92")
        ),
      )

      Config(kafka, lichess)
    }
}
