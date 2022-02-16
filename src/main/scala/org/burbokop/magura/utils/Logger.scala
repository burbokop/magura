package org.burbokop.magura.utils

object Logger {
  implicit class AnyImplicits[T](value: T) {
    def info: T = {
      val stackTrace = new Throwable()
        .getStackTrace
        .tail
        .headOption
        .map(st => s" in ${st.toString}")
        .getOrElse("")
      println(s"${Console.BLUE}[info]${Console.RESET} $value$stackTrace")
      value
    }
  }

}
