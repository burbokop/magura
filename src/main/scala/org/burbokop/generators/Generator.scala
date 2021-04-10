package org.burbokop.generators

object Generator {
  case class Error(message: String) extends Exception(message)
}

abstract class Generator {
  def proceed(inputPath: String, outputPath: String, maguraFile: MaguraFile): Either[Throwable, Unit]
}
