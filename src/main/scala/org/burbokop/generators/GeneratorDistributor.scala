package org.burbokop.generators

import org.burbokop.models.meta.RepositoryMetaData
import org.burbokop.virtualsystem.VirtualSystem

import java.io.File


object GeneratorDistributor{
  val maguraFileName = "magura.yaml"
}

class GeneratorDistributor(generators: Map[String, Generator], generatorField: MaguraFile => String) {
  private def proceedWithMaguraFile(
                                     cache: List[RepositoryMetaData],
                                     inputFolder: String,
                                     outputFolder: String,
                                     maguraFile: MaguraFile,
                                     virtualSystem: Option[VirtualSystem]
  ): Either[Throwable, Option[String]] = {
    val generatorName = generatorField(maguraFile)
    generators.get(generatorName).map { generator =>
      generator.proceed(cache, virtualSystem, inputFolder, outputFolder, maguraFile).map(if(_) Some(generatorName) else None)
    } getOrElse {
      Left(Generator.Error(s"generator $generatorName not found"))
    }
  }

  def proceed(
               cache: List[RepositoryMetaData],
               virtualSystem: Option[VirtualSystem],
               inputFolder: String,
               outputFolder: String,
               maguraFile: Option[MaguraFile] = None
             ): Either[Throwable, Option[String]] =
    maguraFile.map { maguraFile =>
      proceedWithMaguraFile(cache, inputFolder, outputFolder, maguraFile, virtualSystem)
    } getOrElse {
      MaguraFile.fromYaml(s"$inputFolder${File.separator}${GeneratorDistributor.maguraFileName}")
        .fold[Either[Throwable, Option[String]]](Left(_), { maguraFile =>
          proceedWithMaguraFile(cache, inputFolder, outputFolder, maguraFile, virtualSystem)
        })
    }
}
