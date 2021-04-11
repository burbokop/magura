package org.burbokop.generators

import jdk.jfr.internal.Repository
import org.burbokop.generators.CMakeConnector.solveDependencies
import org.burbokop.repository.MaguraRepository
import scala.language.postfixOps

object CMakeConnector {
}

class CMakeConnector extends Generator {
  override def proceed(inputPath: String, outputPath: String, maguraFile: MaguraFile): Either[Throwable, Unit] = {
    solveDependencies(maguraFile)
    println(s"proceed: $inputPath, $outputPath, $maguraFile")
    Right()
  }
}
