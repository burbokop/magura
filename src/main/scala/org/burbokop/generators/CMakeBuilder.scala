package org.burbokop.generators

import org.burbokop.generators.ConfigureBuilder.{Paths, createEnvironment}
import org.burbokop.models.meta.RepositoryMetaData
import org.burbokop.utils.FileUtils
import org.burbokop.virtualsystem.VirtualSystem

import java.io.File
import scala.Console.{GREEN, YELLOW}
import scala.io.AnsiColor.{MAGENTA, RESET}

object CMakeBuilder {
  case class Error(message: String) extends Exception(message)

  def buildCMake(
                  cache: List[RepositoryMetaData],
                  virtualSystem: Option[VirtualSystem],
                  inputPath: String,
                  outputPath: String,
                ): Either[Throwable, Unit] = {

    virtualSystem.map { vs =>
      println(s"${YELLOW}vs.installLatestVersionRepositories(cache): ${vs.installLatestVersionRepositories(cache)}$RESET")
    }

    println(s"${GREEN}build: $inputPath, cache: $cache$RESET")

    val paths = Paths.fromCache(cache)
    val env = virtualSystem.map { vs =>
      vs.env
    } getOrElse {
      createEnvironment(paths,
        "PATH" -> (_.bin),
        "CPATH" -> (_.include),
        "LD_LIBRARY_PATH" -> (_.lib)
      )
    }
    println(s"buildCMake ($inputPath) activeVersions: $env")

    val cmakePath = s"$inputPath${File.separator}CMakeLists.txt"
    if (new File(cmakePath).isFile) {
      val libOutputFolder = new File(outputPath + File.separator + "lib")
      if (!libOutputFolder.exists()) {
        libOutputFolder.mkdirs();
      }
      val inputFolder = new File(inputPath)
      val r0 = sys.process.Process(Seq("cmake", inputFolder.getAbsolutePath), libOutputFolder, env:_*).!
      val r1 = sys.process.Process(Seq("make"), libOutputFolder, env:_*).!
      if (r0 == 0 && r1 == 0) {
        Right()
      } else {
        Left(CMakeBuilder.Error(s"error cmake code: $r0, $r1"))
      }
    } else {
      Left(CMakeBuilder.Error("CMakeLists.txt not found"))
    }
  }

  def copyHeaders(inputPath: String, outputPath: String): Either[Throwable, Unit] = {
    val outputFolder = new File(outputPath)
    if (!outputFolder.exists()) {
      outputFolder.mkdirs();
    }
    println(s"copy header: repo name: ${Generator.repositoryName(inputPath)}")
    val inputFolder = new File(inputPath)
    FileUtils.recursiveListFiles(inputFolder).map[Either[Throwable, Unit]] { file =>
      val path: String = file.getPath
      val newPath = outputPath +
        File.separator +
        "include" +
        File.separator +
        Generator.repositoryName(inputPath) +
        File.separator +
        path.substring(inputPath.length, path.length)

      if (path.endsWith(".h") || path.endsWith(".hpp")) {
        FileUtils.copyFile(path, newPath)
      } else {
        Right()
      }
    }
      .find(_.isLeft).getOrElse(Right())
  }
}

class CMakeBuilder extends Generator {
  override def proceed(
                        cache: List[RepositoryMetaData],
                        virtualSystem: Option[VirtualSystem],
                        inputPath: String,
                        outputPath: String,
                        maguraFile: MaguraFile
                      ): Either[Throwable, Boolean] = {
    CMakeBuilder.buildCMake(cache, virtualSystem, inputPath, outputPath).fold[Either[Throwable, Boolean]](Left(_), { _ =>
      CMakeBuilder.copyHeaders(inputPath, outputPath).map(_ => true)
    })
  }
}
