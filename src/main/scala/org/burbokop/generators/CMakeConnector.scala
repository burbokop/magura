package org.burbokop.generators

import org.burbokop.generators.CMakeConnector.connectMetas
import org.burbokop.models.meta.RepositoryMetaData
import org.burbokop.repository.MaguraRepository

import scala.language.postfixOps

object CMakeConnector {
  case class Error(message: String) extends Exception(message)

  def connectMetas(metas: List[RepositoryMetaData], outputPath: String): Either[Error, Unit] = {
    println(s"connectMetas: $metas, $outputPath")
    Right()
  }
}

class CMakeConnector(builderDistributor: GeneratorDistributor, cacheFolder: String) extends Generator {
  override def proceed(inputPath: String, outputPath: String, maguraFile: MaguraFile): Either[Throwable, Unit] = {
    MaguraRepository.get(builderDistributor, maguraFile.dependencies, cacheFolder).fold(Left(_), { metas =>
      connectMetas(metas, outputPath)
    })
  }
}
