package org.burbokop.generators
import org.burbokop.models.meta.RepositoryMetaData

import java.io.File

object ConfigureBuilder {

  case class Paths(bin: String, include: String, lib: String)
  object Paths {
    def fromCache(cache: List[RepositoryMetaData]) = {
      cache
        .map(_.currentVersion)
        .filter(_.isDefined)
        .map(_.get).map { version =>
        if (version.builder == "configure") {
          Paths(
            s"${version.buildPath}${File.separator}bin",
            s"${version.buildPath}${File.separator}include",
            s"${version.buildPath}${File.separator}lib"
          )
        } else {
          Paths("", "", "")
        }
      }
    }
  }

  def build(cache: List[RepositoryMetaData], inputPath: String, outputPath: String): Either[Throwable, Unit] = {
    val paths = Paths.fromCache(cache)
    println(s"build ($inputPath) paths: $paths")

    def env = Seq(
      "PATH" -> paths.map(_.bin).mkString(":"),
      "CPATH" -> paths.map(_.include).mkString(":"),
      "LD_LIBRARY_PATH" -> paths.map(_.lib).mkString(":")
    )

    println(s"-> env: $env")

    val configPath = s"$inputPath${File.separator}configure"
    if (new File(configPath).isFile) {
      val outputFolder = new File(outputPath)
      if (!outputFolder.exists()) {
        outputFolder.mkdirs();
      }
      val inputFolder = new File(inputPath)
      val permissionsChangeResult = sys.process.Process(Seq("chmod", "+x", "./configure"), inputFolder, env:_*).!
      val configResult = sys.process.Process(Seq("./configure", s"--prefix=$outputPath"), inputFolder, env:_*).!
      println(s"config result: $configResult (perm: $permissionsChangeResult)")

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
  }
}

class ConfigureBuilder extends Generator {
  override def proceed(
                        cache: List[RepositoryMetaData],
                        inputPath: String,
                        outputPath: String,
                        maguraFile: MaguraFile
                      ): Either[Throwable, Boolean] = {
    ConfigureBuilder.build(cache, inputPath, outputPath).map(_ => true)
  }
}
