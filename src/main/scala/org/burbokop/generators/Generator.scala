package org.burbokop.generators

import org.burbokop.models.meta.RepositoryMetaData

object Generator {
  case class Error(message: String) extends Exception(message)
}

abstract class Generator {
  def proceed(
               cache: List[RepositoryMetaData],
               inputPath: String,
               outputPath: String,
               maguraFile: MaguraFile
             ): Either[Throwable, Boolean]
}
