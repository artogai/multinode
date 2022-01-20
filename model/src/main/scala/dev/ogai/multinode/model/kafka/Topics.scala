package dev.ogai.multinode.model.kafka

import dev.ogai.multinode.model.Types.{ GameId, TimestampMs, UserName }
import dev.ogai.multinode.model.game.Game.Game

object Topics {
  import Format._

  val games: Topic[GameId, Game] =
    Topic(
      name = "games",
      partitions = 7,
      replicationFactor = 3,
      getKey = _.id,
      getTimestamp = _.createdAt,
      getPartitionKey = _.source.value,
      config = Map(
        "cleanup.policy"      -> "compact",
        "compression.type"    -> "zstd",
        "min.insync.replicas" -> "2",
      ),
    )

  val usersLatestGameTs: Topic[UserName, (UserName, TimestampMs)] =
    Topic(
      name = "users-latest-game-ts",
      partitions = 7,
      replicationFactor = 3,
      getKey = _._1,
      getTimestamp = _._2,
      getPartitionKey = _._1.value,
      config = Map(
        "cleanup.policy"      -> "compact",
        "compression.type"    -> "zstd",
        "min.insync.replicas" -> "2",
      ),
    )

}
