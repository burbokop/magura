package org.burbokop.tasks

import com.concurrentthought.cla._
import org.burbokop.generators.{CMakeConnector, GeneratorDistributor, MaguraFile}
import org.burbokop.utils.FileUtils

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


    val input = FileUtils.normalizeFolder(parsedArgs.get[String]("project").get)
    val output = FileUtils.normalizeFolder(parsedArgs.get[String]("output").get)


    val connectDistributor = new GeneratorDistributor(Map("cmake" -> new CMakeConnector()), _.connector)

    val result = connectDistributor.proceed(input, output)
    println(s"result: $result")
  }
}
