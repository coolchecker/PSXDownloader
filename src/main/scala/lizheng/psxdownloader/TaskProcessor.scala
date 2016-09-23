package lizheng.psxdownloader

import java.io.{File, FileInputStream, OutputStream}
import java.net.{Socket, URL}

import lizheng.psxdownloader.model.{DownloadRange, HttpMethod, Request}
import lizheng.psxdownloader.util.Logger

class TaskProcessor(socket: Socket, resourceLocation: Map[String, File]) extends Logger {

  def process() = {
    info("----------------------------------------------------------------->")
    val is = socket.getInputStream
    val os = socket.getOutputStream
    val buffer = new Array[Byte](1024)
    is.read(buffer)
    val request = parseRequest(new String(buffer, "UTF-8"))
    HttpMethod.withName(request.method.toUpperCase) match {
      case HttpMethod.CONNECT =>
        writeEstablishedResponse(os)
      case HttpMethod.GET =>
        val path = new URL(request.url).getPath
        val resourceName = path.substring(path.lastIndexOf("/") + 1)
        resourceLocation.get(resourceName) match {
          case None =>
            info(s"no matched resource [$resourceName], ignore")
          case Some(dir) =>
            val localResource = new File(s"${dir.getAbsolutePath}${File.separatorChar}$resourceName")
            writeResourceResponse(localResource, request, os)
        }
    }
    os.flush()
    is.close()
    os.close()
    info("<-----------------------------------------------------------------")
  }

  private def parseRequest(content: String): Request = {
    debug(s"raw request:\n$content")
    val contentArray = content.split("\r")
    val firstLineArray = contentArray.head.split(" ")
    val method = firstLineArray(0)
    val url = firstLineArray(1)
    val range = contentArray.find(_.indexOf("Range:") != -1) match {
      case None =>
        Option.empty[DownloadRange]
      case Some(v) =>
        val exp = v.split(":")(1).trim.split("=")(1)
        val expArray = exp.split("-")
        Option(DownloadRange(expArray(0).toLong, if (expArray.length == 1) Option.empty[Long] else Option(expArray(1).toLong)))
    }
    val result = Request(method, url, range)
    info(s"parsed request: $result")
    result
  }

  private def writeEstablishedResponse(os: OutputStream): Unit = {
    info(s"writing response: [Connection Established]")
    os.write(new StringBuilder()
      .append("HTTP/1.1 200 Connection Established\r\n")
      .append("\r\n")
      .toString.toCharArray.map(_.toByte)
    )
  }

  private def writeResourceResponse(localResource: File, request: Request, os: OutputStream): Unit = {
    val resourceLength = localResource.length
    val fis = new FileInputStream(localResource)
    val (beginRange, endRange) = request.range match {
      case None => (0L, resourceLength - 1)
      case Some(r) => (r.begin, r.end.getOrElse(resourceLength - 1))
    }
    fis.skip(beginRange)
    val contentLength = endRange - beginRange + 1
    val response = new StringBuilder()
      .append(s"HTTP/1.1 ${if (beginRange == 0) "200 OK" else "206 Partial Content"}\r\n")
      .append("Accept-Ranges: bytes\r\n")
      .append("Content-Type: application/octet-stream\r\n")
      .append(if (beginRange > 0) s"Content-Range: bytes $beginRange-$endRange/$resourceLength\r\n" else "")
      .append(s"Content-Length: $contentLength\r\n")
      .append("\r\n")
    info(s"writing response: [${localResource.getAbsolutePath}]")
    os.write(response.toString.toCharArray.map(_.toByte))
    val buffer = new Array[Byte](8192)
    var rd = 0
    do {
      rd = fis.read(buffer, 0, Math.min(buffer.length, fis.available()))
      os.write(buffer, 0, rd)
    }
    while (rd > 0)
    fis.close()
  }

}