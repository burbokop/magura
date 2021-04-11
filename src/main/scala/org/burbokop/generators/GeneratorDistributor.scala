package org.burbokop.generators

import java.io.File


object GeneratorDistributor{
  val maguraFileName = "magura.yaml"
}

class GeneratorDistributor(generators: Map[String, Generator], generatorField: MaguraFile => String) {
  def proceed(inputFolder: String, outputFolder: String): Either[Throwable, Unit] = {
    MaguraFile.fromYaml(s"$inputFolder${File.separator}${GeneratorDistributor.maguraFileName}")
      .fold[Either[Throwable, Unit]](Left(_), { maguraFile =>
        val generator = generatorField(maguraFile)
        generators.get(generator).map { generator =>
          generator.proceed(inputFolder, outputFolder, maguraFile)
        } getOrElse {
          Left(Generator.Error(s"generator $generator not found"))
        }
      }
    )
  }
}
