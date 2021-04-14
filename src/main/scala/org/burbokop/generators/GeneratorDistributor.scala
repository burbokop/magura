package org.burbokop.generators

import java.io.File


object GeneratorDistributor{
  val maguraFileName = "magura.yaml"
}

class GeneratorDistributor(generators: Map[String, Generator], generatorField: MaguraFile => String) {
  private def proceedWithMaguraFile(inputFolder: String, outputFolder: String, maguraFile: MaguraFile) = {
    val generator = generatorField(maguraFile)
    generators.get(generator).map { generator =>
      generator.proceed(inputFolder, outputFolder, maguraFile)
    } getOrElse {
      Left(Generator.Error(s"generator $generator not found"))
    }
  }

  def proceed(inputFolder: String, outputFolder: String, maguraFile: Option[MaguraFile] = None): Either[Throwable, Boolean] =
    maguraFile.map { maguraFile =>
      proceedWithMaguraFile(inputFolder, outputFolder, maguraFile)
    } getOrElse {
      MaguraFile.fromYaml(s"$inputFolder${File.separator}${GeneratorDistributor.maguraFileName}")
        .fold[Either[Throwable, Boolean]](Left(_), { maguraFile =>
          proceedWithMaguraFile(inputFolder, outputFolder, maguraFile)
        })
    }
}
