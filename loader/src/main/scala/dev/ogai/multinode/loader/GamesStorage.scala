package dev.ogai.multinode.loader

import cats.data.NonEmptySet
import cats.effect.Ref
import cats.effect.kernel.Async
import cats.syntax.flatMap._
import cats.syntax.functor._
import dev.ogai.multinode.model.Types.{ TimestampMs, UserName }
import dev.ogai.multinode.model.game.Game.Game
import dev.ogai.multinode.model.kafka.Topics
import fs2.Chunk
import fs2.kafka._
import org.apache.kafka.common.TopicPartition
import org.typelevel.log4cats.Logger

trait GamesStorage[F[_]] {
  def store(userName: UserName, games: Chunk[Game]): F[Unit]
  def latestGameTimestamp(userName: UserName): F[Option[TimestampMs]]
}

object GamesStorage {

  def apply[F[_]: Logger](cfg: Config.Kafka)(implicit F: Async[F]): fs2.Stream[F, GamesStorage[F]] = {
    val producerSettings =
      ProducerSettings(Serializer.identity, Serializer.identity)
        .withBootstrapServers(cfg.bootstrapServers)
        .withDeliveryTimeout(cfg.deliveryTimeout)
        .withRetries(Int.MaxValue)
        .withAcks(Acks.All)
        .withMaxInFlightRequestsPerConnection(1)

    val consumerSettings =
      ConsumerSettings(Deserializer.identity, Deserializer.identity)
        .withBootstrapServers(cfg.bootstrapServers)

    val initTimestampsRef: fs2.Stream[F, Ref[F, Map[UserName, TimestampMs]]] =
      fs2
        .Stream
        .eval(Ref.of(Map.empty[UserName, TimestampMs]))
        .flatMap { tsRef =>
          def readPartition(p: Int): fs2.Stream[F, Unit] =
            KafkaConsumer
              .stream(consumerSettings.withClientId(s"loader-timestamps-consumer-$p"))
              .evalMap { cons =>
                val tp = new TopicPartition(Topics.usersLatestGameTs.name, p)
                cons.assign(tp.topic(), NonEmptySet.one(p)) >>
                  cons.seekToBeginning >>
                  cons
                    .endOffsets(Set(tp))
                    .map(_.getOrElse(tp, 0L))
                    .map(cons -> _)
              }
              .flatMap { case (cons, lastOffset) =>
                if (lastOffset == 0) fs2.Stream.empty
                else
                  cons
                    .records
                    .takeThrough(_.record.offset < lastOffset)
                    .evalMap { rec =>
                      F.fromEither(Topics.usersLatestGameTs.decode(rec.record.asJavaKafka))
                        .flatMap { recDecoded =>
                          tsRef.update(_.updated(recDecoded.value._1, recDecoded.value()._2))
                        }
                    }
              }

          fs2
            .Stream
            .emits(0.until(Topics.usersLatestGameTs.partitions))
            .map(readPartition)
            .parJoinUnbounded
            .as(tsRef)
            .evalTap { tsRef =>
              tsRef.get.map(ts => Logger[F].info(s"Initialized timestamps: $ts"))
            }
        }

    KafkaProducer
      .stream(producerSettings)
      .zip(initTimestampsRef)
      .map { case (producer, timestampsRef) =>
        new GamesStorage[F] {
          override def store(userName: UserName, games: Chunk[Game]): F[Unit] =
            games.last.fold(F.unit) { lastGame =>
              // no need for transactions, due to idempotent consumer (api module)
              producer
                .produce_(ProducerRecords(games.map(game => Topics.games.encode(game).asFs2Kafka)))
                .flatten >>
                producer
                  .produceOne_(Topics.usersLatestGameTs.encode((lastGame.source, lastGame.createdAt)).asFs2Kafka)
                  .flatten >>
                timestampsRef.update(_.updated(userName, lastGame.createdAt))
            }

          override def latestGameTimestamp(userName: UserName): F[Option[TimestampMs]] =
            timestampsRef.get.map(_.get(userName))
        }
      }

  }
}
