package dev.ogai.multinode.model

import java.nio.file.{ Files, Path }
import java.util.Comparator

import scala.util.Try

object FileUtils {

  def delete(root: Path): Unit = {
    val walkTry = Try(Files.walk(root))
    try walkTry.foreach { walk =>
      walk.sorted(Comparator.reverseOrder).map(_.toFile).forEach { file => file.delete; () }
    } finally walkTry.foreach(_.close())
  }

}
