package org.burbokop.generators.configure

import org.burbokop.generators.cmake.CMakeBuilder
import org.burbokop.generators.{Generator, MaguraFile}
import org.burbokop.models.meta.RepositoryMetaData
import org.burbokop.virtualsystem.VirtualSystem

import java.io.File
object ConfigureBuilder {

  case class Paths(bin: String, include: String, lib: String)
  object Paths {
    def fromCache(cache: List[RepositoryMetaData]) = {
      cache
        .map(_.latestVersion)
        .filter(_.isDefined)
        .map(_.get).map { version =>
        if (version.builder == "configure") {
          Some(Paths(
            new File(s"${version.buildPath}${File.separator}bin").getAbsolutePath,
            new File(s"${version.buildPath}${File.separator}include").getAbsolutePath,
            new File(s"${version.buildPath}${File.separator}lib").getAbsolutePath
          ))
        } else {
          None
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


  def build(
             cache: List[RepositoryMetaData],
             virtualSystem: VirtualSystem,
             inputPath: String,
             outputPath: String
           ): Either[Throwable, Unit] = {
    virtualSystem.installLatestVersionRepositories(cache).fold(Left(_), { _ =>
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
          Left(CMakeBuilder.Error(s"error code: $r0, $r1"))
        }
      } else {
        Left(CMakeBuilder.Error("CMakeLists.txt not found"))
      }
    })
  }
}

class ConfigureBuilder(virtualSystem: VirtualSystem) extends Generator {
  override def proceed(
                        cache: List[RepositoryMetaData],
                        inputPath: String,
                        outputPath: String,
                        maguraFile: MaguraFile
                      ): Either[Throwable, Boolean] = {
    ConfigureBuilder.build(cache, virtualSystem, inputPath, outputPath).map(_ => true)
  }
}
