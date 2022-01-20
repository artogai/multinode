package dev.ogai.multinode.api

import dev.ogai.multinode.model.Sockets
import zio.{ Task, ZIO, ZManaged }

object Utils {

  def freePort: Task[Int] =
    ZManaged
      .fromAutoCloseable(ZIO(Sockets.getServerSocket))
      .use(socket => ZIO(socket.getLocalPort))

  def freePorts(n: Int): Task[List[Int]] =
    ZIO.foreach(0.until(n).toList)(_ => freePort)

}
