package dev.ogai.multinode.loader

import cats.effect.Async
import dev.ogai.multinode.model.Types.UserName
import fs2.Stream.eval
import org.typelevel.log4cats.Logger

object App {

  def loadGames[F[_]: Async: Logger](cfg: Config): fs2.Stream[F, Unit] =
    LichessClient[F](cfg.lichess)
      .zip(GamesStorage[F](cfg.kafka))
      .flatMap { case (client, storage) =>
        loadGames(cfg.lichess.users, client, storage)
      }

  def loadGames[F[_]: Async](
      users: Set[UserName],
      client: LichessClient[F],
      storage: GamesStorage[F],
  ): fs2.Stream[F, Unit] =
    fs2
      .Stream
      .emits(users.toSeq)
      // lichess api allows only sequential requests for unregistered clients
      .flatMap(loadUserGames(_, client, storage)) >>
      loadGames(users, client, storage)

  def loadUserGames[F[_]](
      user: UserName,
      client: LichessClient[F],
      storage: GamesStorage[F],
  ): fs2.Stream[F, Unit] =
    eval(storage.latestGameTimestamp(user))
      .flatMap { tsOpt =>
        client
          .games(user, tsOpt)
          .chunks
          .evalMap(storage.store(user, _))
      }

}
