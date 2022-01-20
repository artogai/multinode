package dev.ogai.multinode.model

import java.net.{ InetSocketAddress, ServerSocket }

object Sockets {

  def getServerSocket: ServerSocket = {
    val socket = new ServerSocket()
    socket.setReuseAddress(true)
    socket.bind(new InetSocketAddress(0))
    socket
  }

}
