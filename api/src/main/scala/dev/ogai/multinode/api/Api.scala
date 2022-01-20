package dev.ogai.multinode.api

import dev.ogai.multinode.api.GamesStorage.GamesStorage
import dev.ogai.multinode.grpc.Api.{ ListGamesReq, ListGamesResp, Paging, ZioApi }
import dev.ogai.multinode.model.Types.GameId
import dev.ogai.multinode.model.game.Game.Game
import dev.ogai.multinode.model.kafka.Topic
import io.grpc.Status
import zio.kafka.consumer.ConsumerSettings
import zio.logging.Logging
import zio.random.Random
import zio.{ Has, RLayer, URLayer, ZLayer }

object Api {
  type Api = Has[ZioApi.Api]

  object Service {

    lazy val live: RLayer[Has[ConsumerSettings] with Has[Topic[GameId, Game]] with Logging with Random with CB, Api] =
      GamesStream.Service.kafka >>>
        GamesStorage.Service.inMemory >>>
        Api.Service.impl

    lazy val impl: URLayer[GamesStorage, Api] =
      ZLayer.fromService { gamesStorage: GamesStorage.Service => (req: ListGamesReq) =>
        gamesStorage
          .listGames(req.userName, req.paging.getOrElse(defaultPaging))
          .mapBoth(
            ex => Status.INTERNAL.withCause(ex),
            games => ListGamesResp(games),
          )
      }

    val defaultPaging = Paging(size = 10, number = 0)
  }

}
