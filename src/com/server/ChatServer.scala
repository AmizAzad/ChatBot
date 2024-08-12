package com.server

import java.io._
import java.net.{ServerSocket, Socket, SocketException}
import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.mutable
import java.util.concurrent.Executors

class ChatServer(port: Int) {
  private val serverSocket = new ServerSocket(port)
  private val clients = mutable.Map[String, ClientHandler]()
  private val logger = new PrintWriter(new BufferedWriter(new FileWriter("chat_app.log", true)))

  def start(): Unit = {
    log(s"Server started on port: [$port]")
    val executor = Executors.newCachedThreadPool()

    while (!serverSocket.isClosed) {
      try {
        val socket = serverSocket.accept()
        val clientHandler = new ClientHandler(socket, this)
        executor.execute(clientHandler)
      } catch {
        case _: SocketException if serverSocket.isClosed =>
          // Expected exception when server is shut down. Do nothing.
        case ex: IOException =>
          log("Error accepting client connection: " + ex.getMessage)
      }
    }
    executor.shutdown()
  }

  def broadcastMessage(message: String, sender: String): Unit = {
    clients.filter(i => !i._1.equals(sender)).values.foreach(_.sendMessage(s"$sender: $message"))
    log(s"Broadcast message from [$sender]: $message")
  }

  def privateMessage(sender: String, receiver: String, message: String): Unit = {
    clients.get(receiver) match {
      case Some(clientHandler) =>
        clientHandler.sendMessage(s"$sender [Private]: $message")
        log(s"Private message from [$sender] to [$receiver]: $message")
      case None =>
        clients.get(sender).foreach(_.sendMessage(s"User [$receiver] not found"))
        log(s"User [$receiver] not found for sending private message [$message] from [$sender]")
    }
  }

  def addClient(clientUserName: String, clientHandler: ClientHandler): Boolean = {
    if (clients.contains(clientUserName)) {
      clientHandler.sendMessage("SYSTEM|Username already taken")
      false
    } else {
      clients += (clientUserName -> clientHandler)
      log(s"User [$clientUserName] connected")
      clients.filter(i => i._1.equals(clientUserName)).values.foreach(_.sendMessage("You are logged in to the chat application. To send a private message, use format @receiver_name message"))
      clients.filter(i => !i._1.equals(clientUserName)).values.foreach(_.sendMessage(s"User [$clientUserName] has joined the chat"))
      sendActiveClientsList()
      true
    }
  }

  def removeClient(clientUserName: String): Unit = {
    if (clients.contains(clientUserName)) {
      clients -= clientUserName
      log(s"User [$clientUserName] disconnected")
      clients.values.foreach(_.sendMessage(s"User [$clientUserName] has left the chat"))
      sendActiveClientsList()
    }
  }

  private def sendActiveClientsList(): Unit = {
    val clientsList = clients.keys.mkString(", ")
    clients.values.foreach(_.sendMessage(s"SYSTEM|Currently active users: [$clientsList]"))
  }

  def log(message: String): Unit = {
    println(s"$message")
    val date = new Date
    val dateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm:sss aa")

    logger.println(s"${dateFormat.format(date)} | $message")
    logger.flush()
  }

  def shutdown(): Unit = {
    clients.values.foreach(_.sendMessage("SYSTEM|Server is shutting down. You will be disconnected."))
    clients.values.foreach(_.closeConnection())
    if (!serverSocket.isClosed)
      serverSocket.close()

    log("Server shutdown")
    logger.close()
  }
}

class ClientHandler(socket: Socket, server: ChatServer) extends Runnable {
  private val bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream))
  private val bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream))
  private var clientUserName: String = _

  def run(): Unit = {
    try {
      var validUsername = false
      while (!validUsername) {
        clientUserName = bufferedReader.readLine()
        validUsername = server.addClient(clientUserName, this)
      }

      var message: String = null
      while ({ message = bufferedReader.readLine(); message != null }) {
        val tokens = message.split('|')
        tokens(0) match {
          case "BROADCAST" =>
            server.broadcastMessage(tokens(2), clientUserName)
          case "PRIVATE" =>
            server.privateMessage(tokens(1), tokens(2), tokens(3))
          case "SYSTEM" if tokens(2) == "Server is shutting down. You will be disconnected." =>
            sendMessage("SYSTEM|Disconnecting...")
            // closeConnection()
            return
          case _ =>
            sendMessage(s"SYSTEM|Invalid message format")
        }
      }
    } catch {
      case ex: IOException =>
        server.log(s"Connection error with client [$clientUserName]: ${ex.getMessage}")
    } finally {
      closeConnection()
    }
  }

  def sendMessage(message: String): Unit = {
    try {
      bufferedWriter.write(message)
      bufferedWriter.newLine()
      bufferedWriter.flush()
    } catch {
      case ex: IOException =>
        server.log(s"Error sending message to [$clientUserName]: ${ex.getMessage}")
    }
  }

  def closeConnection(): Unit = {
    try {
      if (bufferedReader != null) bufferedReader.close()
      if (bufferedWriter != null) bufferedWriter.close()
      if (socket != null) socket.close()
    } catch {
      case ex: IOException =>
        server.log(s"Error closing connection for [$clientUserName]: ${ex.getMessage}")
    } finally {
      server.removeClient(clientUserName)
    }
  }
}

object ChatServerApp extends App {
  private val chatServer = new ChatServer(9001)

  Runtime.getRuntime.addShutdownHook(
    new Thread(new Runnable {
      override def run(): Unit = {
        chatServer.shutdown()
      }
    })
  )

  chatServer.start()
}