package dev.ogai.multinode.api

import zio.ULayer
import zio.logging._
import zio.logging.slf4j.Slf4jLogger

object Logger {
  lazy val live: ULayer[Logging] =
    Slf4jLogger.make((_, message) => message)
}
