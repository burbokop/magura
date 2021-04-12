package org.burbokop.generators

import jdk.jfr.internal.Repository
import org.burbokop.repository.MaguraRepository
import scala.language.postfixOps

object CMakeConnector {
}

class CMakeConnector(builderDistributor: GeneratorDistributor, cacheFolder: String) extends Generator {
  override def proceed(inputPath: String, outputPath: String, maguraFile: MaguraFile): Either[Throwable, Unit] = {
    val metas = MaguraRepository.get(builderDistributor, maguraFile.dependencies, cacheFolder)
    println(s"proceed: $inputPath, $outputPath, $maguraFile, $metas")
    Right()
  }
}
