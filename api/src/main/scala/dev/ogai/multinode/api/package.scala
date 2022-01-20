package dev.ogai.multinode

import dev.ogai.multinode.model.kafka.Topic
import zio.blocking.Blocking
import zio.clock.Clock
import zio.kafka.admin.AdminClient.NewTopic

package object api {
  type CB = Clock with Blocking

  implicit class TopicExt[K, V](private val topic: Topic[K, V]) extends AnyVal {
    def asZioNewTopic: NewTopic =
      NewTopic(
        topic.name,
        topic.partitions,
        topic.replicationFactor,
        topic.config,
      )
  }
}
