package dev.ogai.multinode.api

import zio.{ Has, ULayer, ZLayer }

case class Config(bootstrapServers: List[String], grpcPort: Int)

object Config {

  val live: ULayer[Has[Config]] = ZLayer.succeed(
    Config(
      bootstrapServers = List("kafka:9092"),
      grpcPort = 9000,
    )
  )

}
