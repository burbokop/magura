package org.burbokop.generators

import org.burbokop.utils.FileUtils

import java.io.{File, FileInputStream, FileOutputStream}

object CMakeBuilder {
  case class Error(message: String) extends Exception(message)

  def buildCMake(inputPath: String, outputPath: String): Either[Throwable, Unit] = {
    val cmakePath = s"$inputPath${File.separator}CMakeLists.txt"
    if (new File(cmakePath).isFile) {
      val outputFolder = new File(outputPath)
      if (!outputFolder.exists()) {
        outputFolder.mkdirs();
      }
      val inputFolder = new File(inputPath)
      val r0 = sys.process.Process(Seq("cmake", inputFolder.getAbsolutePath), outputFolder).!
      val r1 = sys.process.Process(Seq("make"), outputFolder).!
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
    val inputFolder = new File(inputPath)
    FileUtils.recursiveListFiles(inputFolder).map[Either[Throwable, Unit]] { file =>
      val path: String = file.getPath
      val newPath = outputPath +
        File.separator +
        "headers" +
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
  override def proceed(inputPath: String, outputPath: String, maguraFile: MaguraFile): Either[Throwable, Boolean] = {
    CMakeBuilder.buildCMake(inputPath, outputPath).fold[Either[Throwable, Boolean]](Left(_), { _ =>
      CMakeBuilder.copyHeaders(inputPath, outputPath).map(_ => true)
    })
  }
}
