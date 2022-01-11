package dev.ogai.multinode.model.kafka

import dev.ogai.multinode.model.Types.{ TimestampMs, UserName }
import dev.ogai.multinode.model.game.Game.Game

object Topics {
  import Format._

  val games: Topic[Game] =
    Topic(
      _name = "games",
      _partitions = 7,
      _replicationFactor = 3,
      _getKey = _.id,
      _getTimestamp = _.createdAt,
      _getPartitionKey = _.source.value,
      _config = Map(
        "cleanup.policy"      -> "compact",
        "compression.type"    -> "Zstd",
        "min.insync.replicas" -> "2",
      ),
    )

  val usersLatestGameTs: Topic[(UserName, TimestampMs)] =
    Topic(
      _name = "users-latest-game-ts",
      _partitions = 7,
      _replicationFactor = 3,
      _getKey = _._1,
      _getTimestamp = _._2,
      _getPartitionKey = _._1.value,
      _config = Map(
        "cleanup.policy"      -> "compact",
        "compression.type"    -> "Zstd",
        "min.insync.replicas" -> "2",
      ),
    )

}
