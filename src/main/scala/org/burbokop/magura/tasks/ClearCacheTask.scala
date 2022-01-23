package org.burbokop.magura.tasks

import org.burbokop.magura.plugins.CppBuildPlugin
import org.burbokop.magura.virtualsystem.VirtualSystem

import java.io.File
import scala.reflect.io.Directory

object ClearCacheTask extends Task {
  override def exec(args: Array[String]): Unit = {
    val plugin = new CppBuildPlugin()

    val directory = new Directory(new File(plugin.cacheFolder))
    if (directory.deleteRecursively()) {
      println("cache pruned")
    } else {
      System.err.println("cache prune failed")
      System.exit(1)
    }
    if(plugin.virtualSystem.clear()) {
      println("virtual system pruned")
    } else {
      System.err.println("virtual system prune failed")
      System.exit(1)
    }
  }
}
