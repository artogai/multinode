package dev.ogai.multinode.api

import scala.collection.immutable.SortedMap

import dev.ogai.multinode.api.GamesStream.GamesStream
import dev.ogai.multinode.grpc.Api.Paging
import dev.ogai.multinode.model.Types.{ GameId, TimestampMs, UserName }
import dev.ogai.multinode.model.game.Game.Game
import zio.stream.ZStream
import zio.{ Has, RIO, RLayer, Ref, Task, ZIO }

object GamesStorage {
  type GamesStorage = Has[GamesStorage.Service]

  def listGames(userName: UserName, paging: Paging): RIO[GamesStorage, Seq[Game]] =
    ZIO.serviceWith[Service](_.listGames(userName, paging))

  trait Service {
    def listGames(userName: UserName, paging: Paging): Task[Seq[Game]]
  }

  object Service {

    val inMemory: RLayer[GamesStream, GamesStorage] = {
      def getPage[K, V](xs: SortedMap[K, V], p: Paging): Seq[V] =
        xs.iterator
          .map(_._2)
          .grouped(p.size)
          .drop(p.number - 1)
          .nextOption()
          .getOrElse(Seq.empty)

      // not ideal for large number of games per user, but for example will do
      Ref
        .make(Map.empty[UserName, SortedMap[(TimestampMs, GameId), Game]])
        .flatMap { storageRef =>
          val service =
            new Service {
              override def listGames(userName: UserName, paging: Paging): Task[Seq[Game]] =
                storageRef
                  .get
                  .map { storage =>
                    getPage(
                      storage.getOrElse(userName, SortedMap.empty[(TimestampMs, GameId), Game]),
                      paging,
                    )
                  }
            }

          val subscribeUpdates =
            GamesStream
              .games
              .mapM { game =>
                storageRef
                  .update { storage =>
                    storage
                      .updated(
                        game.source,
                        storage
                          .getOrElse(game.source, SortedMap.empty[(TimestampMs, GameId), Game])
                          .updated((game.createdAt, game.id), game),
                      )
                  }
              }

          ZStream
            .succeed(service)
            .drainFork(subscribeUpdates)
            .runHead
            .someOrFailException
        }
        .toLayer
    }

  }

}
