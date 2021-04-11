package org.burbokop.generators

import java.io.{File, FileInputStream, FileOutputStream}

object CMakeBuilder {
  case class Error(message: String) extends Exception(message)

  def recursiveListFiles(f: File): Array[File] = {
    val these = f.listFiles
    these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
  }

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

  def copyFile(src: String, dest: String): Either[Throwable, Unit] = {
    try {
      val destDir = new File(dest).getParentFile
      if (!destDir.exists()) {
        destDir.mkdirs()
      }
      val inputChannel = new FileInputStream(src).getChannel
      val outputChannel = new FileOutputStream(dest).getChannel
      outputChannel.transferFrom(inputChannel, 0, inputChannel.size)
      inputChannel.close()
      Right()
    } catch {
      case e => Left(e)
    }
  }

  def copyHeaders(inputPath: String, outputPath: String): Either[Throwable, Unit] = {
    val outputFolder = new File(outputPath)
    if (!outputFolder.exists()) {
      outputFolder.mkdirs();
    }
    val inputFolder = new File(inputPath)
    recursiveListFiles(inputFolder).map[Either[Throwable, Unit]] { file =>
      val path: String = file.getPath
      val newPath = outputPath +
        File.separator +
        "headers" +
        File.separator +
        path.substring(inputPath.length, path.length)

      if (path.endsWith(".h") || path.endsWith(".hpp")) {
        copyFile(path, newPath)
      } else {
        Right()
      }
    }
      .find(_.isLeft).getOrElse(Right())
  }
}

class CMakeBuilder extends Generator {
  override def proceed(inputPath: String, outputPath: String, maguraFile: MaguraFile): Either[Throwable, Unit] = {
    CMakeBuilder.buildCMake(inputPath, outputPath).fold[Either[Throwable, Unit]](Left(_), { _ =>
      CMakeBuilder.copyHeaders(inputPath, outputPath)
    })
  }
}
