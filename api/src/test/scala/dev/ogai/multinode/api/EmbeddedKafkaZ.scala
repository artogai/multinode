package dev.ogai.multinode.api

import dev.ogai.multinode.api.EmbeddedKafkaZ.EmbZooKeeper.EmbZooKeeper
import dev.ogai.multinode.model.FileUtils
import dev.ogai.multinode.model.embeddedk.EmbeddedKafka
import zio.blocking.{ Blocking, effectBlocking }
import zio.{ Has, RLayer, RManaged, Task, TaskManaged, ZIO, ZLayer, ZManaged }

object EmbeddedKafkaZ {
  type EmbeddedKafkaZ = Has[EmbeddedKafkaZ.Service]

  lazy val any: RLayer[EmbeddedKafkaZ, EmbeddedKafkaZ] =
    ZLayer.requires[EmbeddedKafkaZ]

  def live(zkPort: Int, brokersPorts: List[Int]): RLayer[Blocking, EmbeddedKafkaZ] =
    (ZLayer.requires[Blocking] ++ EmbZooKeeper.managed(zkPort).toLayer) >>>
      ZManaged
        .foreachPar(brokersPorts)(EmbKafkaBroker.managed)
        .map { _ =>
          val _zkPort       = zkPort
          val _brokersPorts = brokersPorts
          new EmbeddedKafkaZ.Service {
            override def zkPort: Task[Int]             = Task.effectTotal(_zkPort)
            override def brokersPorts: Task[List[Int]] = Task.effectTotal(_brokersPorts)
          }
        }
        .toLayer

  trait Service {
    def zkPort: Task[Int]
    def brokersPorts: Task[List[Int]]
    def bootstrapServers: Task[List[String]] =
      brokersPorts.map(_.map(p => s"localhost:$p"))
  }

  object EmbZooKeeper {
    type EmbZooKeeper = Has[EmbZooKeeper.Service]

    trait Service {
      def port: Task[Int]
    }

    def managed(port: Int): TaskManaged[EmbZooKeeper.Service] =
      ZManaged
        .make {
          ZIO {
            EmbeddedKafka.startZooKeeper(port)
          }
        } { case (server, logsDir) =>
          ZIO(server.shutdown())
            .ignore
            .ensuring(ZIO(FileUtils.delete(logsDir)).ignore)
        }
        .as({
          val _p = port
          new Service {
            override def port: Task[Int] = Task.effectTotal(_p)
          }
        })
  }

  object EmbKafkaBroker {
    type EmbKafkaBroker = Has[EmbKafkaBroker.Service]

    trait Service {
      def port: Task[Int]
    }

    def managed(port: Int): RManaged[EmbZooKeeper with Blocking, EmbKafkaBroker.Service] =
      ZManaged
        .service[EmbZooKeeper.Service]
        .flatMap { zooKeeper =>
          ZManaged
            .make {
              zooKeeper
                .port
                .flatMap { zkPort =>
                  ZIO(EmbeddedKafka.startKafkaBroker(zkPort, port))
                }
            } { case (server, logsDir) =>
              (ZIO(server.shutdown()) *>
                effectBlocking(server.awaitShutdown()))
                .ignore
                .ensuring(ZIO(FileUtils.delete(logsDir)).ignore)
            }
            .as({
              val _p = port
              new Service {
                override def port: Task[Int] = Task.effectTotal(_p)
              }
            })
        }
  }

}
