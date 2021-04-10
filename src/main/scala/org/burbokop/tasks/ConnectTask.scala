package org.burbokop.tasks

import com.concurrentthought.cla._

import java.io.File

object ConnectTask extends Task {
  override def exec(args: Array[String]): Unit = {
    val parsedArgs: Args = """
                             |magura connect [options]
                             |Downloads packages.
                             |
                             |  -p  |  --project    string    Project root directory
                             |  -o  |  --output     string    Output directory
                             |
                             |""".stripMargin.toArgs.process(args)

    println(s"project: ${parsedArgs.get[File]("project")}")
    println(s"output: ${parsedArgs.get[File]("output")}")

  }
}
