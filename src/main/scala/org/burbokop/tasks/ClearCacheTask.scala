package org.burbokop.tasks

import org.burbokop.virtualsystem.VirtualSystem

import java.io.File
import scala.reflect.io.Directory

object ClearCacheTask extends Task {
  override def exec(args: Array[String]): Unit = {
    val cacheFolder = System.getenv("HOME") + File.separator + ".magura/repos"
    val mainVirtualSystem = new VirtualSystem(System.getenv("HOME") + File.separator + ".magura/vsys")
    val directory = new Directory(new File(cacheFolder))
    if (directory.deleteRecursively()) {
      println("cache pruned")
    } else {
      System.err.println("cache prune failed")
      System.exit(1)
    }
    if(mainVirtualSystem.clear()) {
      println("virtual system pruned")
    } else {
      System.err.println("virtual system prune failed")
      System.exit(1)
    }
  }
}
