package org.burbokop.generators.cmake

import org.burbokop.generators.Generator.{DefaultOptions, Options}
import org.burbokop.generators.{Generator, MaguraFile}
import org.burbokop.models.meta.RepositoryMetaData
import org.burbokop.utils.FileUtils
import org.burbokop.utils.HashUtils.StringImplicits.apply
import org.burbokop.virtualsystem.VirtualSystem
import play.api.libs.json.{JsNull, JsString, JsValue}

import java.io.File
import scala.util.{Failure, Success}

object CMakeBuilder {
  case class CMakeOptions(prefix: String) extends Options {
    override def hashName(): String =  prefix.md5
  }

  object CMakeOptions {
    Options.register(
      classOf[CMakeOptions],
      value => CMakeOptions(value.as[String]),
      options => Option(options.asInstanceOf[CMakeOptions]).map(o => JsString(o.prefix)).getOrElse(JsNull)
    )
  }

  case class Error(message: String) extends Exception(message)

  def buildCMake(
                  cache: List[RepositoryMetaData],
                  virtualSystem: VirtualSystem,
                  inputPath: String,
                  outputPath: String,
                  options: Options,
                ): Either[Throwable, Unit] =
    virtualSystem.installLatestVersionRepositories(cache).fold(Left(_), { _ =>
      val env = virtualSystem.env
      val cmakePath = s"$inputPath${File.separator}CMakeLists.txt"
      if (new File(cmakePath).isFile) {
        val prefix: Option[String] = options match {
          case CMakeOptions(prefix) => Some(prefix)
          case _ => None
        }
        println(s"prefix: $prefix")

        val cmakeVariables: Map[String, String] = prefix
          .map(p => Map("CMAKE_PREFIX_PATH" -> p))
          .getOrElse(Map())

        val libOutputFolder = new File(outputPath + File.separator + "lib")
        if (!libOutputFolder.exists()) {
          libOutputFolder.mkdirs();
        }
        val inputFolder = new File(inputPath)
        val r0 = sys.process.Process(Seq("cmake", inputFolder.getAbsolutePath) ++ cmakeVariables.map(arg => s"-D${arg._1}=${arg._2}"), libOutputFolder, env:_*).!
        val r1 = sys.process.Process(Seq("make"), libOutputFolder, env:_*).!
        if (r0 == 0 && r1 == 0) {
          Right()
        } else {
          Left(CMakeBuilder.Error(s"error cmake code: $r0, $r1"))
        }
      } else {
        Left(CMakeBuilder.Error("CMakeLists.txt not found"))
      }
    })


  def copyHeaders(inputPath: String, outputPath: String): Either[Throwable, Unit] = {
    val outputFolder = new File(outputPath)
    if (!outputFolder.exists()) {
      outputFolder.mkdirs();
    }
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

class CMakeBuilder(virtualSystem: VirtualSystem) extends Generator {
  override def proceed(
                        cache: List[RepositoryMetaData],
                        inputPath: String,
                        outputPath: String,
                        options: Options,
                        maguraFile: MaguraFile
                      ): Either[Throwable, Boolean] = {
    CMakeBuilder.buildCMake(cache, virtualSystem, inputPath, outputPath, options).fold[Either[Throwable, Boolean]](Left(_), { _ =>
      CMakeBuilder.copyHeaders(inputPath, outputPath).map(_ => true)
    })
  }
}
