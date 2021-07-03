package org.burbokop.generators

import org.burbokop.models.meta.RepositoryMetaData
import org.burbokop.virtualsystem.VirtualSystem

import java.io.File

object Generator {
  case class Error(message: String) extends Exception(message)

  def repositoryName(inputPath: String): String =
    new File(inputPath).getParentFile.getName
}

abstract class Generator {
  def proceed(
               cache: List[RepositoryMetaData],
               inputPath: String,
               outputPath: String,
               maguraFile: MaguraFile
             ): Either[Throwable, Boolean]

}

