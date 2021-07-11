package org.burbokop.tasks

import com.concurrentthought.cla._
import org.burbokop.generators.{CMakeBuilder, CMakeConnector, ConfigureBuilder, GeneratorDistributor}
import org.burbokop.utils.ErrorUtils.ThrowableImplicits.apply
import org.burbokop.utils.FileUtils
import org.burbokop.virtualsystem.VirtualSystem

import java.io.File
import scala.Console._

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

    println(s"${GREEN}project folder: $input$RESET")
    println(s"${GREEN}output folder: $output$RESET")

    val cacheFolder = System.getenv("HOME") + File.separator + ".magura/repos"
    val mainVirtualSystem = new VirtualSystem(System.getenv("HOME") + File.separator + ".magura/vsys")

    val builderDistributor = new GeneratorDistributor(Map(
      "cmake" -> new CMakeBuilder(mainVirtualSystem),
      "configure" -> new ConfigureBuilder(mainVirtualSystem)
    ), _.builder)

    val connectorDistributor = new GeneratorDistributor(Map(
      "cmake" -> new CMakeConnector(builderDistributor, cacheFolder, mainVirtualSystem)
    ), _.connector)

    connectorDistributor.proceed(List(), input, output, None).fold({ error =>
      error.print(true)
    }, { generatorName =>
      generatorName.map { generatorName =>
        println(s"magura connected with generator '$generatorName'")
      } getOrElse {
        println(s"magura already connected")
      }
    })
  }
}
