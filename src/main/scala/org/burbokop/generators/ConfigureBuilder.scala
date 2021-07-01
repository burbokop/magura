package org.burbokop.generators
import org.burbokop.models.meta.RepositoryMetaData
import org.burbokop.virtualsystem.VirtualSystem

import java.io.File
import io.AnsiColor._
import scala.Console.GREEN
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
             virtualSystem: Option[VirtualSystem],
             inputPath: String,
             outputPath: String
           ): Either[Throwable, Unit] = {

    virtualSystem.map { vs =>
      println(s"vs.installLatestVersionRepositories(cache): ${vs.installLatestVersionRepositories(cache)}")
    }

    println(s"${GREEN}configure: $inputPath, cache: $cache$RESET")

    val paths = Paths.fromCache(cache)

    println(s"${MAGENTA}build ($inputPath) paths: $paths$RESET")

    val env = virtualSystem.map { vs =>
      vs.env
    } getOrElse {
      createEnvironment(paths,
        "PATH" -> (_.bin),
        "CPATH" -> (_.include),
        "LD_LIBRARY_PATH" -> (_.lib)
      )
    }

    val a = createEnvironment(paths,
      "PATH" -> (_.bin),
      "CPATH" -> (_.include),
      "LD_LIBRARY_PATH" -> (_.lib)
    )
    println(s"-> prev env: $a")
    println(s"-> prev env: $a")

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
                        virtualSystem: Option[VirtualSystem],
                        inputPath: String,
                        outputPath: String,
                        maguraFile: MaguraFile
                      ): Either[Throwable, Boolean] = {
    ConfigureBuilder.build(cache, virtualSystem, inputPath, outputPath).map(_ => true)
  }
}
