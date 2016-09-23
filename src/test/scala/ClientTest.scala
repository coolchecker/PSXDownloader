import java.io.{InputStream, OutputStreamWriter}
import java.net.Socket

object ClientTest {

  def main(args: Array[String]) {
    val host = "127.0.0.1"
    val port = 8080
    val client = new Socket(host, port)
    val in = client.getInputStream
    val out = new OutputStreamWriter(client.getOutputStream)

//    sendConnect(out)
    sendGet(out)
    getResponse(in)

    in.close()
    out.close()
    client.close()
  }

  private def sendConnect(out: OutputStreamWriter) = {
    val request = new StringBuilder()
      .append("CONNECT cn-prof.np.community.playstation.net:443 HTTP/1.1\r\n")
      .append("Host: cn-prof.np.community.playstation.net\r\n")
      .append("Proxy-Connection: Keep-Alive\r\n")
      .append("\r\n")
    println("----> send connection")
    out.write(request.toString)
    out.flush()
  }

  private def sendGet(out: OutputStreamWriter) = {
    val request = new StringBuilder()
      .append("GET http://cn-prof.np.community.playstation.net:443/UP9000-CUSA01623_00-0000GODOFWAR3PS4-A0101-V0100.pkg HTTP/1.1\r\n")
      .append("\r\n")
    println("----> test get")
    out.write(request.toString)
    out.flush()
  }

  private def getResponse(in: InputStream) = {
    val buffer = new Array[Byte](8192)
    var readLength, totalLength = 0
    var readProtocol = false
    do {
      readLength = in.read(buffer)
      if (readLength > 0) {
        if (!readProtocol) {
          println(new String(buffer, 0, readLength))
          readProtocol = true
        }
        totalLength += readLength
      }
    }
    while (readLength > 0)
    println(s"<---- response: total $totalLength byte(s)")
  }

}
