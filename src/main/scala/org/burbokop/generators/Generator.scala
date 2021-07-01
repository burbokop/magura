package org.burbokop.generators

import org.burbokop.models.meta.RepositoryMetaData
import org.burbokop.virtualsystem.VirtualSystem

object Generator {
  case class Error(message: String) extends Exception(message)
}

abstract class Generator {
  def proceed(
               cache: List[RepositoryMetaData],
               virtualSystem: Option[VirtualSystem],
               inputPath: String,
               outputPath: String,
               maguraFile: MaguraFile
             ): Either[Throwable, Boolean]
}
