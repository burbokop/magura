package org.burbokop.tasks

import org.burbokop.build.BuildConfiguration
import org.burbokop.generators.GeneratorDistributor
import org.burbokop.generators.cmake.CMakeBuilder
import org.burbokop.repository.MaguraRepository
import org.burbokop.utils.FileUtils
import org.burbokop.virtualsystem.VirtualSystem

import java.io.File

object BuildTask extends Task {
  override def exec(args: Array[String]): Unit = {
    val mainVirtualSystem = new VirtualSystem(System.getenv("HOME") + File.separator + ".magura/vsys")

    val result = BuildConfiguration.fromYaml(s"${FileUtils.pwd()}/build.yaml")
      .fold(Left(_), conf => {
        val cacheFolder = System.getenv("HOME") + File.separator + ".magura/repos"
        val builderDistributor = new GeneratorDistributor(Map("cmake" -> new CMakeBuilder(mainVirtualSystem)), _.builder)
        MaguraRepository.get(builderDistributor, conf.repository, cacheFolder)
          .fold({ err =>
            println(s"error: $err")
            Left(err)
          }, { metaData =>
            metaData.latestVersion().map(version => {
              println(s"version: $version")
              Right(version)
            }).getOrElse(Left(new Exception("No repo version")))
          })
      })

    result.left.map(_.printStackTrace())
  }
}
