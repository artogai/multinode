package dev.ogai.multinode

import cats.effect.Sync
import dev.ogai.multinode.model.Types.{ TimestampMs, UserName }
import fs2.kafka.{ Deserializer, ProducerRecord => Fs2ProducerRecord }
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord

package object loader {
  implicit class Deserializers(private val obj: Deserializer.type) extends AnyVal {
    def userName[F[_]: Sync]: Deserializer[F, UserName] =
      Deserializer.string.map(UserName.apply)

    def timestampMs[F[_]: Sync]: Deserializer[F, TimestampMs] =
      Deserializer.long.map(TimestampMs.apply)
  }

  implicit class Fs2KafkaConsumerRecordExt[K, V](private val rec: fs2.kafka.ConsumerRecord[K, V]) extends AnyVal {

    // ignoring part of the fields for simplicity
    def asJavaKafka: ConsumerRecord[K, V] = new ConsumerRecord[K, V](
      rec.topic,
      rec.partition,
      rec.offset,
      rec.key,
      rec.value,
    )
  }

  implicit class ProducerRecordExt[K, V](private val rec: ProducerRecord[K, V]) extends AnyVal {

    // ignoring part of the fields for simplicity
    def asFs2Kafka: Fs2ProducerRecord[K, V] = {
      val key     = Option(rec.key()).getOrElse(throw new IllegalArgumentException("Key must be set"))
      val partOpt = Option(rec.partition())
      val tsOpt   = Option(rec.timestamp())

      val fs2Rec         = Fs2ProducerRecord[K, V](rec.topic(), key, rec.value())
      val fs2RecWithPart = partOpt.fold(fs2Rec)(fs2Rec.withPartition(_))
      val fs2RecWithTs   = tsOpt.fold(fs2RecWithPart)(fs2Rec.withTimestamp(_))

      fs2RecWithTs
    }
  }
}
