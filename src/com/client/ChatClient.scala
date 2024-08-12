package com.client

import java.io._
import java.net.Socket
import java.util.Scanner

class ChatClient(host: String, port: Int, userName: String) {
  private val socket = new Socket(host, port)
  private val bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream))
  private val bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream))

  def start(): Unit = {
    sendUserName()

    val listenerThread = new Thread(new Runnable {
      override def run(): Unit = {
        try {
          var message: String = null
          while ({ message = bufferedReader.readLine(); message != null }) {
            if (message.contains("Server is shutting down")) {
              println(message)
              closeConnection()
              return
            }
            println(message)
          }
        } catch {
          case ex: IOException =>
            println(s"Error reading messages: ${ex.getMessage}")
            closeConnection()
        }
      }
    })

    listenerThread.start()

    val scanner = new Scanner(System.in)
    while (socket.isConnected) {
      val message = scanner.nextLine()

      if (message.startsWith("@")) {
        val splitMessage = message.split(" ", 2)
        if (splitMessage.length == 2) {
          sendMessage(s"PRIVATE|$userName|${splitMessage(0).substring(1)}|${splitMessage(1)}")
        } else {
          println("Invalid private message format. Use format @receiver_name message")
        }
      } else {
        sendMessage(s"BROADCAST|$userName|$message")
      }
    }
  }

  private def sendUserName(): Unit = {
    try {
      bufferedWriter.write(userName)
      bufferedWriter.newLine()
      bufferedWriter.flush()
    } catch {
      case ex: IOException =>
        println(s"Error sending user name: ${ex.getMessage}")
        closeConnection()
    }
  }

  private def sendMessage(message: String): Unit = {
    try {
      bufferedWriter.write(message)
      bufferedWriter.newLine()
      bufferedWriter.flush()
    } catch {
      case ex: IOException =>
        println(s"Error sending message: ${ex.getMessage}")
        closeConnection()
    }
  }

  private def closeConnection(): Unit = {
    try {
      if (bufferedReader != null) bufferedReader.close()
      if (bufferedWriter != null) bufferedWriter.close()
      if (socket != null) socket.close()
    } catch {
      case ex: IOException =>
        println(s"Error closing connection: ${ex.getMessage}")
    } finally {
      println("Connection closed. Exiting.")
      System.exit(0)
    }
  }
}

object ChatClientApp extends App {
  private val scanner: Scanner = new Scanner(System.in)
  println("Enter server host: ")
  private val host = scanner.nextLine()
  println("Enter server port: ")
  val port = scanner.nextInt()
  scanner.nextLine()
  println("Enter your user name: ")
  private val userName = scanner.nextLine()

  private val chatClient = new ChatClient(host, port, userName)
  chatClient.start()
}