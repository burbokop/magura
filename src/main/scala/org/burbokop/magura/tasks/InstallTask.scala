package org.burbokop.magura.tasks

import com.concurrentthought.cla._
import io.github.burbokop.magura.api.GeneratorDistributor
import io.github.burbokop.magura.repository.MaguraRepository
import org.burbokop.magura.generators.cmake.CMakeBuilder
import org.burbokop.magura.virtualsystem.VirtualSystem

import java.io.File

object InstallTask extends Task {
  override def exec(args: Array[String]): Unit = {
    val parsedArgs: Args =
      """
        |magura install [github {user}.{repo}.{branch}:{builder(optional)} packages]
        |Downloads packages.
        |
        |""".stripMargin.toArgs.process(args)
    val mainVirtualSystem = new VirtualSystem(System.getenv("HOME") + File.separator + ".magura/vsys")
    val result = for (p <- parsedArgs.remaining) yield {
      MaguraRepository.fromString(p).fold(Left(_), { repository =>
        println(s"installing: github.com/${repository.user}/${repository.name} (branch: ${repository.branchName}, builder: ${repository.builder.getOrElse("<default>")})")
        val cacheFolder = System.getenv("HOME") + File.separator + ".magura/repos"
        val builderDistributor = new GeneratorDistributor(
          Map("cmake" -> new CMakeBuilder(mainVirtualSystem)),
          Map(),
          _.builder
        )
        MaguraRepository.get(builderDistributor, repository, cacheFolder)
          ._2
          .fold({ err =>
            println(s"error: $err")
            Left(err)
          }, { message =>
            println(message)
            Right(message)
          })
      })
    }
    println(result)
  }
}
