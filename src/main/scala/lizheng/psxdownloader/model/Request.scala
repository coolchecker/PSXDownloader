package lizheng.psxdownloader.model

case class Request(method: String, url: String, range: Option[DownloadRange])

object HttpMethod extends Enumeration {
  val CONNECT, GET = Value
}