package dev.ogai.multinode.loader

import cats.Applicative
import cats.effect.kernel.Async
import cats.syntax.applicative._
import cats.syntax.functor._
import cats.syntax.option._
import dev.ogai.multinode.model.Types.{ TimestampMs, UserName }
import dev.ogai.multinode.model.game.Game.Game
import fs2.RaiseThrowable
import io.circe.Decoder
import io.circe.parser.parse
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client
import org.http4s.headers.Accept
import org.http4s.{ Headers, MediaType, Request }
import org.typelevel.log4cats.Logger

trait LichessClient[F[_]] {
  def games(userName: UserName, sinceOpt: Option[TimestampMs]): fs2.Stream[F, Game]
}

object LichessClient {

  def apply[F[_]: Async: Logger](cfg: Config.Lichess): fs2.Stream[F, LichessClient[F]] =
    BlazeClientBuilder[F]
      .stream
      .map(LichessClient(cfg, _))

  def apply[F[_]: Applicative: RaiseThrowable: Logger](
      cfg: Config.Lichess,
      httpClient: Client[F],
  ): LichessClient[F] =
    (userName: UserName, sinceOpt: Option[TimestampMs]) => {
      implicit val gamesDecoder: Decoder[Game] = Decoders.gameDecoder(userName)
      httpClient
        .stream(
          Request(
            uri = (cfg.api / "games" / "user" / userName.value)
              .withOptionQueryParam("since", sinceOpt.map(_.value)),
            headers = Headers(
              Accept(MediaType.unsafeParse("application/x-ndjson"))
            ),
          )
        )
        .flatMap { resp =>
          resp
            .bodyText
            .evalMap { str =>
              parse(str)
                .flatMap(_.as[Game])
                .fold(
                  err => Logger[F].error(s"Failed to parse game=${str} err=${err}").as(none[Game]),
                  game => game.some.pure[F],
                )
            }
            .unNone
        }
    }

}
