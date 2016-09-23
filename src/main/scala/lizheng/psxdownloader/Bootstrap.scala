package lizheng.psxdownloader

import java.io.File
import java.net.{InetAddress, ServerSocket}
import java.util.concurrent.Executors
import java.util.regex.Pattern

import lizheng.psxdownloader.model.Argument
import lizheng.psxdownloader.util.Logger

import scala.concurrent.{ExecutionContext, Future}

object Bootstrap extends App with Logger {

  implicit val context = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(100))

  val argument = parseArguments(args)
  val resource2Dir = mapResourceDir(argument.resourceDir)
  val server = new ServerSocket(argument.port)
  info(s"PSX Proxy server (${InetAddress.getLocalHost.getHostAddress}:${argument.port}) started")
  while (true) {
    val request = server.accept
    Future {
      new TaskProcessor(request, resource2Dir).process()
    }
  }

  private def parseArguments(args: Array[String]): Argument = {
    val port = args.headOption.getOrElse(handleError("port is required")).toString match {
      case str =>
        if (!Pattern.compile("[0-9]*").matcher(str).matches()) {
          handleError(s"invalid port: [$str]")
        }
        str.toInt
    }
    val resourceDir = new File(args.tail.headOption.getOrElse(handleError("resource dir is required")).toString)
    if (!resourceDir.isDirectory) {
      handleError(s"invalid resource dir: [$resourceDir]")
    }
    Argument(port, resourceDir)
  }

  private def mapResourceDir(resourceDir: File): Map[String, File] = {
    var result = Map.empty[String, File]
    def scan(dir: File): Unit = {
      dir.listFiles.foreach({
        case f if f.isFile =>
          result += f.getName -> f.getParentFile
          debug(s"mapped resource [${f.getName} ----> ${f.getParentFile}]")
        case f if f.isDirectory => scan(f)
      })
    }
    scan(resourceDir)
    result
  }

  @inline
  private def handleError(message: String) = {
    error(message)
    System.exit(0)
  }

}
