package com.goamegah.flowtrack.common

import org.slf4j.{Logger, LoggerFactory}

object LoggerHelper {
  private val RESET = "\u001b[0m"
  private val RED = "\u001b[31m"
  private val GREEN = "\u001b[32m"
  private val YELLOW = "\u001b[33m"
  private val CYAN = "\u001b[36m"
  private val BOLD = "\u001b[1m"

  def getLogger(name: String): ColoredLogger = {
    new ColoredLogger(LoggerFactory.getLogger(name), name)
  }

  class ColoredLogger(private val logger: Logger, prefix: String) {

    private def wrap(color: String, msg: String): String =
      s"$color[$prefix] $msg$RESET"

    def info(msg: String): Unit = logger.info(wrap(GREEN, msg))
    def warn(msg: String): Unit = logger.warn(wrap(YELLOW, msg))
    def error(msg: String): Unit = logger.error(wrap(RED, msg))
    def debug(msg: String): Unit = logger.debug(wrap(CYAN, msg))
  }
}
