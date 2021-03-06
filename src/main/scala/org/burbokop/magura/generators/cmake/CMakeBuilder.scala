package org.burbokop.magura.generators.cmake

import io.github.burbokop.magura.api.{Generator, MaguraFile}
import io.github.burbokop.magura.api.Generator.Options
import io.github.burbokop.magura.models.meta
import io.github.burbokop.magura.models.meta.RepositoryMetaData
import io.github.burbokop.magura.utils.HashUtils.StringImplicits.apply
import io.github.burbokop.magura.utils.{FileUtils, JsonUtils}
import play.api.libs.json.{JsValue, Json}
import org.burbokop.magura.virtualsystem._

import java.io.File

object CMakeBuilder {

  @Options.FormatAttached(
    serialization = CMakeOptions.serialization,
    deserialization = CMakeOptions.deserialization
  )
  case class CMakeOptions(prefix: String) extends Options {
    override def hashName(): String = prefix.md5
  }

  object CMakeOptions {
    implicit val jsonFormat = Json.format[CMakeOptions]

    def deserialization(value: JsValue): Either[Throwable, Options] =
      JsonUtils.validateEitherThrowable[CMakeOptions](value)

    def serialization(options: Options): JsValue =
      Json.toJson(options.asInstanceOf[CMakeOptions])
  }

  case class Error(message: String) extends Exception(message)

  def buildCMake(
                  cache: List[RepositoryMetaData],
                  virtualSystem: VirtualSystem,
                  inputPath: String,
                  outputPath: String,
                  options: Options,
                ): Either[Throwable, Unit] =
    virtualSystem.update(cache).fold(Left(_), { _ =>
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
    FileUtils.recursiveListFiles(inputFolder).map { file =>
      val path: String = file.getPath
      val newPath = outputPath +
        File.separator +
        "include" +
        File.separator +
        Generator.repositoryName(inputPath) +
        File.separator +
        path.substring(inputPath.length, path.length)

      val a = if (path.endsWith(".h") || path.endsWith(".hpp")) {
        FileUtils.copyFile(path, newPath)
      } else {
        Right()
      }
      a
    }
      .find(_.isLeft).getOrElse(Right())
  }
}

class CMakeBuilder(virtualSystem: VirtualSystem) extends Generator {
  override def proceed(
                        cache: List[meta.RepositoryMetaData],
                        inputPath: String,
                        outputPath: String,
                        options: Options,
                        maguraFile: MaguraFile
                      ): Either[Throwable, Generator.Result] =
    CMakeBuilder
      .buildCMake(cache, virtualSystem, inputPath, outputPath, options)
      .fold[Either[Throwable, Generator.Result]](Left(_), { _ =>
        CMakeBuilder.copyHeaders(inputPath, outputPath).map(_ => Generator.Result(true))
      })
}
