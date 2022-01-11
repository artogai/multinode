package dev.ogai.multinode.api

import dev.ogai.multinode.api.GamesStorage.GamesStorage
import dev.ogai.multinode.grpc.Api.{ ListGamesReq, ListGamesResp, Paging, ZioApi }
import io.grpc.Status
import zio.kafka.consumer.ConsumerSettings
import zio.logging.Logging
import zio.{ Has, RLayer, URLayer, ZLayer }

object Api {
  type Api = Has[ZioApi.Api]

  object Service {

    val live: RLayer[Has[ConsumerSettings] with Logging with CB, Api] =
      GamesStream.Service.kafka >>>
        GamesStorage.Service.inMemory >>>
        Api.Service.impl

    val impl: URLayer[GamesStorage, Api] =
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
