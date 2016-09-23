package lizheng.psxdownloader.util

import org.slf4j.LoggerFactory

trait Logger {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def debug(message: String): Unit = {
    logger.debug(message)
  }

  def info(message: String): Unit = {
    logger.info(message)
  }

  def warn(message: String): Unit = {
    logger.warn(message)
  }

  def error(message: String): Unit = {
    logger.error(message)
  }

  def error(message: String, throwable: Throwable): Unit = {
    logger.error(message, throwable)
  }

}
