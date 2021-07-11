package org.burbokop.utils

import scala.Console.{CYAN, RED, RESET, YELLOW, println}

object ErrorUtils {
  class ThrowableImplicits(throwable: Throwable) {
    def print(verbose: Boolean): Unit = {
      System.err.println(s"${RED}magura connection error: $throwable$RESET")
      if(verbose) {
        throwable.getStackTrace.headOption.map { element =>
          println(s"appearance point: \t${YELLOW}${element.getClassName}.${element.getMethodName}$RESET in ${CYAN}${element.getFileName}:${element.getLineNumber}$RESET")
        } getOrElse {
          println(s"appearance point not available")
        }
        if(throwable.isInstanceOf[ReducedError]) {
          println(s"error is reduced. parts:")
          for (err <- throwable.asInstanceOf[ReducedError].errors) {
            new ThrowableImplicits(err).print(verbose)
          }
        }
      }
    }
  }
  object ThrowableImplicits {
    implicit def apply(throwable: Throwable) = new ThrowableImplicits(throwable)
  }
}
