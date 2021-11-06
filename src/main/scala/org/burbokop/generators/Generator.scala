package org.burbokop.generators

import org.burbokop.generators.Generator.Options
import org.burbokop.models.meta.RepositoryMetaData
import org.burbokop.virtualsystem.VirtualSystem

import java.io.File

object Generator {
  case class Error(message: String) extends Exception(message)

  abstract class Options {
    def hashName(): String
  }

  object Options {
    def default: Options = () => "default"
    def defaultList: List[Options] = List(default)
  }

  def repositoryName(inputPath: String): String =
    new File(inputPath).getParentFile.getName
}

abstract class Generator {
  def proceed(
               cache: List[RepositoryMetaData],
               inputPath: String,
               outputPath: String,
               options: Options,
               maguraFile: MaguraFile,
             ): Either[Throwable, Boolean]

}

