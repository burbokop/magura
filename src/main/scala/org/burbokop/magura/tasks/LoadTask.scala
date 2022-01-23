package org.burbokop.magura.tasks

import io.github.burbokop.magura.api.PluginLoader

import java.io.File

object LoadTask extends Task {
  override def exec(args: Array[String]): Unit = {
    if(args.length > 0) {
      (new PluginLoader).load(new File(args(0))).foreach { plugin =>
        println(s"plugin loaded: ${plugin.getClass.getCanonicalName}, name: ${plugin.name()}")
      }
    } else {
      println("no dir specified")
    }
  }
}
