package org.burbokop.tasks

import com.concurrentthought.cla._
import org.burbokop.generators.{CMakeBuilder, CMakeConnector, GeneratorDistributor}
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

    val cacheFolder = System.getenv("HOME") + File.separator + ".magura/repos"

    val builderDistributor = new GeneratorDistributor(Map(
      "cmake" -> new CMakeBuilder()
    ), _.builder)

    val connectorDistributor = new GeneratorDistributor(Map(
      "cmake" -> new CMakeConnector(builderDistributor, cacheFolder)
    ), _.connector)

    connectorDistributor.proceed(input, output).fold({ error =>
      println(s"magura connection error: ${error.getMessage}")
    }, {
      if(_) {
        println(s"magura connected")
      } else {
        println(s"magura already connected")
      }
    })
  }
}
