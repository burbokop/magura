package org.burbokop.magura

import org.burbokop.magura.generators.GeneratorDistributor
import org.burbokop.magura.generators.cmake.CMakeBuilder
import org.burbokop.magura.generators.sbt.SbtBuilder
import org.burbokop.magura.repository.MaguraRepository

object PluginLoader {
  def load(repository: MaguraRepository, cacheFolder: String) = {
    val builderDistributor = new GeneratorDistributor(Map("sbt" -> new SbtBuilder()), _.builder)
    MaguraRepository.get(builderDistributor, repository, cacheFolder).fold({ err =>
      println(s"error: $err")
      Left(err)
    }, { message =>
      message.latestVersion().map(vers => vers.defaultBuildPath().map(bp => {
        println(s">>>>>>>>>>>>>>>>>>>>>>>>>>>> $bp")
      }))
      Right(message)
    })
  }
}
