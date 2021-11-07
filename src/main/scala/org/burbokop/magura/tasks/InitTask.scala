package org.burbokop.magura.tasks

import org.burbokop.magura.generators.GeneratorDistributor
import org.burbokop.magura.utils.FileUtils

object InitTask extends Task {
  override def exec(args: Array[String]): Unit = {
    FileUtils.writeIfNotExist(s"./${GeneratorDistributor.maguraFileName}",
      """
        |# Name build strategy (now only 'cmake' available)
        |builder: "cmake"
        |
        |# Name of connect strategy (now only 'cmake' available)
        |connector: "cmake"
        |
        |# List of github repositories witch will be connected
        |dependencies:
        |  - "{github user}.{github repo}.{branch}:{builder (optional)}"
        |
        |""".stripMargin)
  }
}
