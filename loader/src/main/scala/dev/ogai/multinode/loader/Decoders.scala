package dev.ogai.multinode.loader

import cats.syntax.option._
import dev.ogai.multinode.model.Types
import dev.ogai.multinode.model.Types.{ GameId, UserName }
import dev.ogai.multinode.model.game.Clock.Clock
import dev.ogai.multinode.model.game.Color.Color
import dev.ogai.multinode.model.game.Game.Game
import dev.ogai.multinode.model.game.MoveEval.MoveEval
import dev.ogai.multinode.model.game.MoveEval.MoveEval.Judgment
import dev.ogai.multinode.model.game.Opening.Opening
import dev.ogai.multinode.model.game.Player.Player
import dev.ogai.multinode.model.game.Player.Player.Analysis
import dev.ogai.multinode.model.game.Speed.Speed
import dev.ogai.multinode.model.game.Status.Status
import dev.ogai.multinode.model.game.Variant.Variant
import dev.ogai.multinode.model.user.Title.Title
import dev.ogai.multinode.model.user.User.User
import io.circe.{ Decoder, HCursor }

object Decoders {
  implicit val gameIdDecoder: Decoder[GameId] =
    Decoder[String].map(GameId.apply)

  implicit val userNameDecoder: Decoder[UserName] =
    Decoder[String].map(UserName.apply)

  implicit val variantDecoder: Decoder[Variant] =
    Decoder.decodeString.map {
      case "standard"      => Variant.STANDARD
      case "chess960"      => Variant.CHESS960
      case "crazyhouse"    => Variant.CRAZYHOUSE
      case "antichess"     => Variant.ANTICHESS
      case "atomic"        => Variant.ATOMIC
      case "horde"         => Variant.HORDE
      case "kingOfTheHill" => Variant.KING_OF_THE_HILL
      case "racingKings"   => Variant.RACING_KINGS
      case "threeCheck"    => Variant.THREE_CHECK
      case _               => Variant.UNKNWON
    }

  implicit val speedDecoder: Decoder[Speed] =
    Decoder.decodeString.map {
      case "ultraBullet"    => Speed.ULTRA_BULLET
      case "bullet"         => Speed.BULLET
      case "blitz"          => Speed.BLITZ
      case "rapid"          => Speed.RAPID
      case "classical"      => Speed.CLASSICAL
      case "correspondence" => Speed.CORRESPONDENCE
      case _                => Speed.UNKNOWN
    }

  implicit val statusDecoder: Decoder[Status] =
    Decoder.decodeString.map {
      case "created"       => Status.CREATED
      case "started"       => Status.STARTED
      case "aborted"       => Status.ABORTED
      case "mate"          => Status.MATE
      case "resign"        => Status.RESIGN
      case "stalemate"     => Status.STALEMATE
      case "timeout"       => Status.TIMEOUT
      case "draw"          => Status.DRAW
      case "outoftime"     => Status.OUT_OF_TIME
      case "cheat"         => Status.CHEAT
      case "noStart"       => Status.NO_START
      case "unknownFinish" => Status.UNKNOWN_FINISH
      case "variantEnd"    => Status.VARIANT_END
      case _               => Status.UNKNOWN
    }

  implicit val colorDecoder: Decoder[Color] =
    Decoder.decodeString.map {
      case "white" => Color.WHITE
      case "black" => Color.BLACK
      case _       => Color.UNKNOWN
    }

  implicit val titleDecoder: Decoder[Title] =
    Decoder.decodeString.map {
      case "GM"  => Title.GM
      case "WGM" => Title.WGM
      case "IM"  => Title.IM
      case "WIM" => Title.WIM
      case "FM"  => Title.FM
      case "WFM" => Title.WFM
      case "NM"  => Title.NM
      case "CM"  => Title.CM
      case "WCM" => Title.WCM
      case "WNM" => Title.WNM
      case "LM"  => Title.LM
      case "BOT" => Title.BOT
      case _     => Title.UNKNOWN
    }

  implicit val userDecoder: Decoder[User] =
    (c: HCursor) =>
      for {
        id     <- c.getOrElse("id")("")
        name   <- c.getOrElse("name")(UserName.empty)
        title  <- c.getOrElse("title")(Title.UNKNOWN: Title)
        patron <- c.getOrElse("patron")(false)
      } yield User(
        id,
        name,
        title,
        patron,
      )

  implicit val analysisDecoder: Decoder[Analysis] =
    (c: HCursor) =>
      for {
        inaccuracy <- c.getOrElse("inaccuracy")(0)
        mistake    <- c.getOrElse("mistake")(0)
        blunder    <- c.getOrElse("blunder")(0)
        acpl       <- c.getOrElse("acpl")(0)
      } yield Analysis(
        inaccuracy,
        mistake,
        blunder,
        acpl,
      )

  implicit val playerDecoder: Decoder[Player] =
    (c: HCursor) =>
      for {
        user        <- c.getOrElse("user")(none[User])
        rating      <- c.getOrElse("rating")(0)
        ratingDiff  <- c.getOrElse("ratingDiff")(0)
        name        <- c.getOrElse("name")(UserName.empty)
        provisional <- c.getOrElse("provisional")(false)
        aiLevel     <- c.getOrElse("aiLevel")(0)
        analysis    <- c.getOrElse("analysis")(none[Analysis])
        team        <- c.getOrElse("team")("")
      } yield Player(
        user,
        rating,
        ratingDiff,
        name,
        provisional,
        aiLevel,
        analysis,
        team,
      )

  implicit val openingDecoder: Decoder[Opening] =
    (c: HCursor) =>
      for {
        eco  <- c.getOrElse("eco")("")
        name <- c.getOrElse("name")("")
        ply  <- c.getOrElse("ply")(0)
      } yield Opening(
        eco,
        name,
        ply,
      )

  implicit val judgmentDecoder: Decoder[Judgment] =
    (c: HCursor) =>
      for {
        name    <- c.getOrElse("name")("")
        comment <- c.getOrElse("comment")("")
      } yield Judgment(
        name,
        comment,
      )

  implicit val moveEvalDecoder: Decoder[MoveEval] =
    (c: HCursor) =>
      for {
        eval      <- c.get[Int]("eval")
        best      <- c.getOrElse("best")("")
        variation <- c.getOrElse("variation")("")
        judgment  <- c.getOrElse("judgment")(none[MoveEval.Judgment])
      } yield MoveEval(
        eval,
        best,
        variation,
        judgment,
      )

  implicit val clockDecoder: Decoder[Clock] =
    (c: HCursor) =>
      for {
        intital   <- c.getOrElse("intital")(0)
        increment <- c.getOrElse("increment")(0)
        totalTime <- c.getOrElse("totalTime")(0)
      } yield Clock(
        intital,
        increment,
        totalTime,
      )

  def gameDecoder(userName: UserName): Decoder[Game] =
    (c: HCursor) =>
      for {
        id          <- c.get[GameId]("id")
        rated       <- c.get[Boolean]("rated")
        variant     <- c.get[Variant]("variant")
        speed       <- c.get[Speed]("speed")
        perf        <- c.get[String]("perf")
        createdAt   <- c.get[Long]("createdAt")
        lastMoveAt  <- c.get[Long]("lastMoveAt")
        status      <- c.get[Status]("status")
        white       <- c.downField("players").get[Player]("white")
        black       <- c.downField("players").get[Player]("black")
        initialFen  <- c.getOrElse("initialFen")("")
        winner      <- c.getOrElse("winner")(Color.UNKNOWN: Color)
        opening     <- c.getOrElse("opening")(none[Opening])
        moves       <- c.getOrElse("moves")("")
        pgn         <- c.getOrElse("pgn")("")
        daysPerTurn <- c.getOrElse("daysPerTurn")(0)
        analysis    <- c.getOrElse("analysis")(Seq.empty[MoveEval])
        tournament  <- c.getOrElse("tournament")("")
        swiss       <- c.getOrElse("swiss")("")
        clock       <- c.getOrElse("clock")(none[Clock])
      } yield Game(
        id,
        rated,
        variant,
        speed,
        perf,
        Types.TimestampMs(createdAt),
        Types.TimestampMs(lastMoveAt),
        status,
        Some(white),
        Some(black),
        initialFen,
        winner,
        opening,
        moves,
        pgn,
        daysPerTurn,
        analysis,
        tournament,
        swiss,
        clock,
        userName,
      )

}
