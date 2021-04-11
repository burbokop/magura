package org.burbokop.utils

import java.io.File
import scala.annotation.tailrec

object FileUtils {
  @tailrec
  def normalizeFolder(folder: File): File =
    if (folder.isDirectory) folder
    else normalizeFolder(folder.getParentFile)

  def normalizeFolder(folder: String): String =
    normalizeFolder(new File(folder)).getAbsolutePath
}
