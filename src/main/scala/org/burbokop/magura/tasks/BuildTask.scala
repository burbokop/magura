package org.burbokop.magura.tasks

import org.burbokop.magura.buildcfg.BuildConfiguration
import org.burbokop.magura.generators.Generator.{DefaultOptions, Options}
import org.burbokop.magura.generators.GeneratorDistributor
import org.burbokop.magura.generators.cmake.CMakeBuilder
import org.burbokop.magura.generators.cmake.CMakeBuilder.CMakeOptions
import org.burbokop.magura.repository.MaguraRepository
import org.burbokop.magura.utils.FileUtils
import org.burbokop.magura.virtualsystem.VirtualSystem

import java.io.File

object BuildTask extends Task {
  override def exec(args: Array[String]): Unit = {
    val mainVirtualSystem = new VirtualSystem(System.getenv("HOME") + File.separator + ".magura/vsys")

    val result = BuildConfiguration.fromYaml(s"${FileUtils.pwd()}/build.yaml")
      .fold(Left(_), conf => {
        println(s"conf: $conf")
        val cacheFolder = System.getenv("HOME") + File.separator + ".magura/repos"
        val builderDistributor = new GeneratorDistributor(Map("cmake" -> new CMakeBuilder(mainVirtualSystem)), _.builder)

        val opts: Set[Options] = conf.prefixes.toSet.map(p => CMakeOptions(p))

        val a: Set[Options] = if(opts.size > 0) opts else Set(DefaultOptions())

        MaguraRepository.get(builderDistributor, conf.repository, cacheFolder, a)
          .fold({ err =>
            println(s"error: $err")
            Left(err)
          }, { metaData =>
            println(s"metaData: $metaData")
            metaData.latestVersion().map(version => {
              println(s"version: $version")
              Right(version)
            }).getOrElse(Left(new Exception("No repo version")))
          })
      })

    result.left.map(_.printStackTrace())
  }
}
