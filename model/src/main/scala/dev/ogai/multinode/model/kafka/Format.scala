package dev.ogai.multinode.model.kafka

import java.io.ByteArrayOutputStream

import scala.util.Try

import com.google.protobuf.{ CodedInputStream, CodedOutputStream }
import dev.ogai.multinode.model.Types.{ GameId, TimestampMs, UserName }
import org.apache.kafka.common.serialization.{ LongDeserializer, LongSerializer, StringDeserializer, StringSerializer }
import scalapb.{ GeneratedMessage, GeneratedMessageCompanion }

trait Format[A] {
  def encode(v: A): Array[Byte]
  def decode(bytes: Array[Byte]): Either[Throwable, A]

  def inmap[B](f: A => B)(g: B => A): Format[B] =
    Format.from(b => encode(g(b)), bytes => decode(bytes).map(f))
}

object Format {
  def apply[A: Format]: Format[A] =
    implicitly[Format[A]]

  def from[A](_encode: A => Array[Byte], _decode: Array[Byte] => Either[Throwable, A]): Format[A] =
    new Format[A] {
      override def encode(v: A): Array[Byte]                        = _encode(v)
      override def decode(bytes: Array[Byte]): Either[Throwable, A] = _decode(bytes)
    }

  def encode[A](value: A)(implicit f: Format[A]): Array[Byte] =
    f.encode(value)

  def decode[A](value: Array[Byte])(implicit f: Format[A]): Either[Throwable, A] =
    f.decode(value)

  implicit val stringFormat: Format[String] = {
    val ser = new StringSerializer()
    val de  = new StringDeserializer()
    Format.from(s => ser.serialize(null, s), bytes => Try(de.deserialize(null, bytes)).toEither)
  }

  implicit val longFormat: Format[Long] = {
    val ser = new LongSerializer()
    val de  = new LongDeserializer()
    Format.from(l => ser.serialize(null, l), bytes => Try(de.deserialize(null, bytes).toLong).toEither)
  }

  implicit val gameIdFormat: Format[GameId] =
    Format[String].inmap(GameId.apply)(_.value)

  implicit val userNameFormat: Format[UserName] =
    Format[String].inmap(UserName.apply)(_.value)

  implicit val timestampMsFormat: Format[TimestampMs] =
    Format[Long].inmap(TimestampMs.apply)(_.value)

  implicit def tupleFormat[A, B](implicit f1: Format[A], f2: Format[B]): Format[(A, B)] =
    Format.from[(A, B)](
      _encode = { case (a, b) =>
        val out = new ByteArrayOutputStream()
        val cos = CodedOutputStream.newInstance(out)
        cos.writeByteArrayNoTag(f1.encode(a))
        cos.writeByteArrayNoTag(f2.encode(b))
        cos.flush()
        out.toByteArray
      },
      _decode = bytes => {
        val cos  = CodedInputStream.newInstance(bytes)
        val arr1 = cos.readByteArray()
        val arr2 = cos.readByteArray()
        for {
          _1 <- f1.decode(arr1)
          _2 <- f2.decode(arr2)
        } yield (_1, _2)
      },
    )

  implicit def protobufFormat[A <: GeneratedMessage](implicit gmc: GeneratedMessageCompanion[A]): Format[A] =
    new Format[A] {
      override def encode(v: A): Array[Byte]                        = gmc.toByteArray(v)
      override def decode(bytes: Array[Byte]): Either[Throwable, A] = Try(gmc.parseFrom(bytes)).toEither
    }

}
