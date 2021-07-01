package org.burbokop.tasks

import com.concurrentthought.cla._
import org.burbokop.generators.{CMakeBuilder, GeneratorDistributor}
import org.burbokop.repository.MaguraRepository

import java.io.File

object InstallTask extends Task {
  override def exec(args: Array[String]): Unit = {
    val parsedArgs: Args =
      """
        |magura install [github {user}.{repo}.{branch}:{builder(optional)} packages]
        |Downloads packages.
        |
        |""".stripMargin.toArgs.process(args)

    val result = for (p <- parsedArgs.remaining) yield {
      MaguraRepository.fromString(p).fold(Left(_), { repository =>
        println(s"installing: github.com/${repository.user}/${repository.name} (branch: ${repository.branchName}, builder: ${repository.builder.getOrElse("<default>")})")
        val cacheFolder = System.getenv("HOME") + File.separator + ".magura/repos"
        val builderDistributor = new GeneratorDistributor(Map("cmake" -> new CMakeBuilder()), _.builder)
        MaguraRepository.get(builderDistributor, repository, cacheFolder, None).fold({ err =>
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
