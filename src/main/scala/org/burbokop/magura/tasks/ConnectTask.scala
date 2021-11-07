package org.burbokop.magura.tasks

import com.concurrentthought.cla._
import org.burbokop.magura.generators.Generator.DefaultOptions
import org.burbokop.magura.generators.cmake.{CMakeBuilder, CMakeConnector}
import org.burbokop.magura.generators.GeneratorDistributor
import org.burbokop.magura.generators.configure.ConfigureBuilder
import org.burbokop.magura.utils.ErrorUtils.ThrowableImplicits.apply
import org.burbokop.magura.utils.FileUtils
import org.burbokop.magura.virtualsystem.VirtualSystem

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


    val projectFile = parsedArgs.get[String]("project").get
    val input = FileUtils.normalizeFolder(projectFile)
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
      "cmake" -> new CMakeConnector(builderDistributor, cacheFolder, mainVirtualSystem, projectFile)
    ), _.connector)

    connectorDistributor.proceed(List(), input, Map(output -> DefaultOptions()), None).fold({ error =>
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
