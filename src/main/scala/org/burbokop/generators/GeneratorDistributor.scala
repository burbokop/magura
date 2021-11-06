package org.burbokop.generators

import org.burbokop.generators.Generator.Options
import org.burbokop.models.meta.RepositoryMetaData
import org.burbokop.utils.EitherUtils.ThrowableListImplicits._

import java.io.File


object GeneratorDistributor{
  val maguraFileName = "magura.yaml"
}

class GeneratorDistributor(generators: Map[String, Generator], generatorField: MaguraFile => String) {
  private def proceedWithMaguraFile(
                                     cache: List[RepositoryMetaData],
                                     inputFolder: String,
                                     outputWithOptions: Map[String, Options],
                                     maguraFile: MaguraFile,
  ): Either[Throwable, Option[String]] = {
    val generatorName = generatorField(maguraFile)
    generators.get(generatorName).map { generator =>
      outputWithOptions.map(arg => { val (path, options) = arg
        generator.proceed(cache, inputFolder, path, options, maguraFile)
      })
        .toList
        .reducesPartitionEither
        .map(_.find(b => b).map(_ => generatorName))
    } getOrElse {
      Left(Generator.Error(s"generator $generatorName not found"))
    }
  }

  def proceed(
               cache: List[RepositoryMetaData],
               inputFolder: String,
               outputWithOptions: Map[String, Options],
               maguraFile: Option[MaguraFile] = None
             ): Either[Throwable, Option[String]] =
    maguraFile.map { maguraFile =>
      proceedWithMaguraFile(cache, inputFolder, outputWithOptions, maguraFile)
    } getOrElse {
      MaguraFile.fromYaml(s"$inputFolder${File.separator}${GeneratorDistributor.maguraFileName}")
        .fold[Either[Throwable, Option[String]]](Left(_), { maguraFile =>
          proceedWithMaguraFile(cache, inputFolder, outputWithOptions, maguraFile)
        })
    }
}
