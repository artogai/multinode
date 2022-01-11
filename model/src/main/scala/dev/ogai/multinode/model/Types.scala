package dev.ogai.multinode.model

import io.estatico.newtype.macros.newsubtype
import scalapb.TypeMapper

object Types {
  @newsubtype case class UserName(value: String)
  object UserName {
    val empty: UserName = UserName("")
    implicit val typeMapper: TypeMapper[String, UserName] =
      TypeMapper(UserName.apply)(_.value)
  }

  @newsubtype case class GameId(value: String)
  object GameId {
    val empty: GameId = GameId("")
    implicit val typeMapper: TypeMapper[String, GameId] =
      TypeMapper(GameId.apply)(_.value)
    implicit val ordering: Ordering[GameId] =
      Ordering[String].on[GameId](_.value)
  }

  @newsubtype case class TimestampMs(value: Long)
  object TimestampMs {
    val empty: TimestampMs = TimestampMs(0)
    val epoch: TimestampMs = empty
    implicit val typeMapper: TypeMapper[Long, TimestampMs] =
      TypeMapper(TimestampMs.apply)(_.value)
    implicit val ordering: Ordering[TimestampMs] =
      Ordering[Long].on[TimestampMs](_.value)
  }
}
