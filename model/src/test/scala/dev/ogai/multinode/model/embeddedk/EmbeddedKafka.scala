package dev.ogai.multinode.model.embeddedk

import java.net.InetSocketAddress
import java.nio.file.{ Files, Path }
import java.util.UUID

import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

import kafka.server.{ KafkaConfig, KafkaServer }
import org.apache.zookeeper.server.{ ServerCnxnFactory, ZooKeeperServer }

object EmbeddedKafka {

  def startZooKeeper(zkPort: Int): (ServerCnxnFactory, Path) = {
    val zkUniqueId = UUID.randomUUID()
    val logsDir    = Files.createTempDirectory(s"zookeeper-logs-$zkUniqueId")

    val zkServer = new ZooKeeperServer(
      logsDir.toFile,
      logsDir.toFile,
      2000,
    )

    val factory = ServerCnxnFactory.createFactory
    factory.configure(new InetSocketAddress(zkPort), 1024)
    factory.startup(zkServer)
    (factory, logsDir)
  }

  def startKafkaBroker(zkPort: Int, brokerPort: Int): (KafkaServer, Path) = {
    val brokerUniqueId = UUID.randomUUID()
    val logsDir        = Files.createTempDirectory(s"kafka-logs-$brokerUniqueId")

    val properties = Map(
      KafkaConfig.ZkConnectProp                          -> s"localhost:$zkPort",
      KafkaConfig.ZkConnectionTimeoutMsProp              -> 10.seconds.toMillis.toString,
      KafkaConfig.BrokerIdProp                           -> -1.toString,
      KafkaConfig.ListenersProp                          -> s"PLAINTEXT://localhost:$brokerPort",
      KafkaConfig.AdvertisedListenersProp                -> s"PLAINTEXT://localhost:$brokerPort",
      KafkaConfig.AutoCreateTopicsEnableProp             -> false.toString,
      KafkaConfig.LogDirProp                             -> logsDir.toAbsolutePath.toString,
      KafkaConfig.LogFlushIntervalMessagesProp           -> 1.toString,
      KafkaConfig.OffsetsTopicReplicationFactorProp      -> 1.toString,
      KafkaConfig.OffsetsTopicPartitionsProp             -> 1.toString,
      KafkaConfig.TransactionsTopicReplicationFactorProp -> 1.toString,
      KafkaConfig.TransactionsTopicMinISRProp            -> 1.toString,
      KafkaConfig.LogCleanerDedupeBufferSizeProp         -> (1 * 1024 * 1024 + 1).toString,
      KafkaConfig.ControlledShutdownEnableProp           -> false.toString,
    )

    val broker = new KafkaServer(new KafkaConfig(properties.asJava))
    broker.startup()

    (broker, logsDir)
  }

}
