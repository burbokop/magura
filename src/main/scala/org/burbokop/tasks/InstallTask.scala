package org.burbokop.tasks

import com.concurrentthought.cla._
import org.burbokop.generators.{CMakeBuilder, GeneratorDistributor}
import org.burbokop.repository.MaguraRepository

import java.io.File

object InstallTask extends Task {
  override def exec(args: Array[String]): Unit = {
    val parsedArgs: Args = """
                             |magura install [github {user}/{repo} packages]
                             |Downloads packages.
                             |
                             |""".stripMargin.toArgs.process(args)

    for(p <- parsedArgs.remaining) {
      val parts = p.split('/')
      if (parts.length > 1) {
        println(s"installing: github.com/${parts(0)}/${parts(1)}")
        val cacheFolder = System.getenv("HOME") + File.separator + ".magura/repos"
        val builderDistributor = new GeneratorDistributor(Map("cmake" -> new CMakeBuilder()), _.builder)
        MaguraRepository.get(builderDistributor, parts(0), parts(1), "master", cacheFolder).fold({ err =>
          println(s"error: $err")
        }, { message =>
          println(message)
        })
      }
    }
  }
}
