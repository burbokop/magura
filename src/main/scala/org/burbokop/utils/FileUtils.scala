package org.burbokop.utils

import java.io.{File, FileInputStream, FileOutputStream}
import scala.annotation.tailrec
import scala.io.Source

object FileUtils {
  @tailrec
  def normalizeFolder(folder: File): File =
    if (folder.isDirectory) folder
    else normalizeFolder(folder.getParentFile)

  def normalizeFolder(folder: String): String =
    normalizeFolder(new File(folder)).getAbsolutePath

  def recursiveListFiles(f: File): Array[File] = {
    val these = f.listFiles
    these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
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

  def writeIfDifferent(path: String, data: String): Either[Throwable, Boolean] = {
    try {
      if (Source.fromFile(path).mkString != data) {
        new FileOutputStream(path).write(data.toArray.map(_.toByte))
        Right(true)
      } else {
        Right(false)
      }
    } catch {
      case e => Left(e)
    }
  }
}
