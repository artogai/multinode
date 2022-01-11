package dev.ogai.multinode.model.kafka

import scala.jdk.CollectionConverters._
import scala.util.hashing.MurmurHash3

import dev.ogai.multinode.model.Types.TimestampMs
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord

trait Topic[V] {
  type Key

  def name: String
  def partitions: Int
  def replicationFactor: Short
  def getKey: V => Key
  def getTimestamp: V => TimestampMs
  def getPartition: V => Int
  def config: Map[String, String]

  def encode(v: V): ProducerRecord[Array[Byte], Array[Byte]]
  def decode(rec: ConsumerRecord[Array[Byte], Array[Byte]]): Either[Throwable, ConsumerRecord[Key, V]]

  def asNewTopic: NewTopic = {
    val nt = new NewTopic(name, partitions, replicationFactor)
    if (config.nonEmpty) nt.configs(config.asJava) else nt
  }
}

object Topic {
  def apply[K: Format, V: Format](
      _name: String,
      _partitions: Int,
      _replicationFactor: Short,
      _getKey: V => K,
      _getPartitionKey: V => String,
      _getTimestamp: V => TimestampMs,
      _config: Map[String, String],
  ): Topic[V] = new Topic[V] {
    type Key = K

    override def name: String                   = _name
    override def partitions: Int                = _partitions
    override def replicationFactor: Short       = _replicationFactor
    override def getKey: V => K                 = _getKey
    override def getTimestamp: V => TimestampMs = _getTimestamp
    override def getPartition: V => Int         = v => MurmurHash3.stringHash(_getPartitionKey(v)) % _partitions
    override def config: Map[String, String]    = _config

    override def encode(v: V): ProducerRecord[Array[Byte], Array[Byte]] =
      new ProducerRecord(
        _name,
        getPartition(v),
        _getTimestamp(v),
        Format.encode(_getKey(v)),
        Format.encode(v),
      )

    override def decode(rec: ConsumerRecord[Array[Byte], Array[Byte]]): Either[Throwable, ConsumerRecord[K, V]] =
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
      )
  }
}
