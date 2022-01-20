package dev.ogai.multinode.model.kafka

import scala.jdk.CollectionConverters._
import scala.util.hashing.MurmurHash3

import dev.ogai.multinode.model.Types.TimestampMs
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord

case class Topic[K, V](
    name: String,
    partitions: Int,
    replicationFactor: Short,
    getKey: V => K,
    getTimestamp: V => TimestampMs,
    getPartition: V => Int,
    config: Map[String, String],
    encode: V => ProducerRecord[Array[Byte], Array[Byte]],
    decode: ConsumerRecord[Array[Byte], Array[Byte]] => Either[Throwable, ConsumerRecord[K, V]],
) {
  def asNewTopic: NewTopic = {
    val nt = new NewTopic(name, partitions, replicationFactor)
    if (config.nonEmpty) nt.configs(config.asJava) else nt
  }
}

object Topic {
  def apply[K: Format, V: Format](
      name: String,
      partitions: Int,
      replicationFactor: Short,
      getKey: V => K,
      getPartitionKey: V => String,
      getTimestamp: V => TimestampMs,
      config: Map[String, String],
  ): Topic[K, V] = {
    val getPartition = (v: V) => MurmurHash3.stringHash(getPartitionKey(v)) % partitions

    new Topic[K, V](
      name = name,
      partitions = partitions,
      replicationFactor,
      getKey = getKey,
      getTimestamp = getTimestamp,
      getPartition = getPartition,
      config = config,
      encode = (v: V) =>
        new ProducerRecord(
          name,
          getPartition(v),
          getTimestamp(v),
          Format.encode(getKey(v)),
          Format.encode(v),
        ),
      decode = (rec: ConsumerRecord[Array[Byte], Array[Byte]]) =>
        for {
          key   <- Format.decode[K](rec.key())
          value <- Format.decode[V](rec.value())
        } yield new ConsumerRecord[K, V](
          rec.topic(),
          rec.partition(),
          rec.offset(),
          rec.timestamp(),
          rec.timestampType(),
          0,
          rec.serializedKeySize(),
          rec.serializedValueSize(),
          key,
          value,
          rec.headers(),
        ),
    )
  }
}
