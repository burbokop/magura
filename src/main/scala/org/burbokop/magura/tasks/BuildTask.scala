package org.burbokop.magura.tasks

import io.github.burbokop.magura.api.Generator.{DefaultOptions, Options}
import io.github.burbokop.magura.api.GeneratorDistributor
import io.github.burbokop.magura.repository.MaguraRepository
import io.github.burbokop.magura.utils.FileUtils
import org.burbokop.magura.buildcfg.{BuildConfiguration, BuildPrefix}
import org.burbokop.magura.generators.cmake.CMakeBuilder
import org.burbokop.magura.generators.cmake.CMakeBuilder.CMakeOptions
import org.burbokop.magura.plugins.CppBuildPlugin
import org.burbokop.magura.virtualsystem.VirtualSystem

import java.io.File

object BuildTask extends Task {
  override def exec(args: Array[String]): Unit = {
    val mainVirtualSystem = new VirtualSystem(System.getenv("HOME") + File.separator + ".magura/vsys")

    val result = BuildConfiguration.fromYaml(s"${FileUtils.pwd()}/build.yaml")
      .fold(Left(_), conf => {
        println(s"conf: $conf")

        val plugin = new CppBuildPlugin()

        val opts: Set[Options] = conf.prefixes.toSet.map({ prefix: BuildPrefix => if(prefix.isMain) DefaultOptions() else CMakeOptions(prefix.path) })
        val defaultedOpts: Set[Options] = if(opts.size > 0) opts else Set(DefaultOptions())

        MaguraRepository.get(plugin.newDistributor(), conf.repository, plugin.cacheFolder, defaultedOpts)
          ._2
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
