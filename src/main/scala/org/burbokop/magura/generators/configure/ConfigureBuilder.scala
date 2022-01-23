package org.burbokop.magura.generators.configure

import io.github.burbokop.magura.api.{Generator, MaguraFile}
import io.github.burbokop.magura.api.Generator.Options
import io.github.burbokop.magura.models.meta.RepositoryMetaData
import org.burbokop.magura.virtualsystem.VirtualSystem

import java.io.File
object ConfigureBuilder {

  case class Paths(bin: String, include: String, lib: String)
  object Paths {
    def fromCache(cache: List[RepositoryMetaData]) = {
      cache
        .map(_.latestVersion)
        .filter(_.isDefined)
        .map(_.get).map { version =>
        version.defaultBuildPath().map { buildPath =>
          if (version.builder == "configure") {
            Some(Paths(
              new File(s"$buildPath${File.separator}bin").getAbsolutePath,
              new File(s"$buildPath${File.separator}include").getAbsolutePath,
              new File(s"$buildPath${File.separator}lib").getAbsolutePath
            ))
          } else {
            None
          }
        }
      }
        .filter(_.isDefined)
        .map(_.get)
    }
  }

  def createEnvironment(paths: List[Paths], records: (String, Paths => String)*): Seq[(String, String)] =
    records.map { r =>
      (
        r._1, sys.env.get(r._1)
        .map(_ :: paths.map(r._2))
        .getOrElse(paths.map(r._2))
        .mkString(":")
      )
    }

  case class Error(message: String) extends Exception(message)

  def build(
             cache: List[RepositoryMetaData],
             virtualSystem: VirtualSystem,
             inputPath: String,
             outputPath: String
           ): Either[Throwable, Unit] = {
    virtualSystem.update(cache).fold(Left(_), { _ =>
      val env = virtualSystem.env
      val configPath = s"$inputPath${File.separator}configure"
      if (new File(configPath).isFile) {
        val outputFolder = new File(outputPath)
        if (!outputFolder.exists()) {
          outputFolder.mkdirs();
        }
        val inputFolder = new File(inputPath)
        val permissionsChangeResult = sys.process.Process(Seq("chmod", "+x", "./configure"), inputFolder, env:_*).!
        val configResult = sys.process.Process(Seq("./configure", s"--prefix=$outputPath"), inputFolder, env:_*).!

        val r0 = sys.process.Process(Seq("make"), inputFolder, env:_*).!
        val r1 = sys.process.Process(Seq("make", "install"), inputFolder, env:_*).!
        if (r0 == 0 && r1 == 0) {
          Right()
        } else {
          Left(ConfigureBuilder.Error(s"error code: $r0, $r1"))
        }
      } else {
        Left(ConfigureBuilder.Error("CMakeLists.txt not found"))
      }
    })
  }
}

class ConfigureBuilder(virtualSystem: VirtualSystem) extends Generator {
  override def proceed(
                        cache: List[RepositoryMetaData],
                        inputPath: String,
                        outputPath: String,
                        options: Options,
                        maguraFile: MaguraFile
                      ): Either[Throwable, Generator.Result] = {
    ConfigureBuilder.build(cache, virtualSystem, inputPath, outputPath).map(_ => Generator.Result(true))
  }
}
