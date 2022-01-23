package org.burbokop.magura.plugins

import io.github.burbokop.magura.api.{Generator, MaguraFile, Plugin, PluginLoader}
import io.github.burbokop.magura.repository.RepositoryProvider
import org.burbokop.magura.generators.cmake.CMakeBuilder
import org.burbokop.magura.generators.configure.ConfigureBuilder
import org.burbokop.magura.repository._
import org.burbokop.magura.virtualsystem.VirtualSystem

import java.io.File


class CppBuildPlugin extends Plugin {
  val cacheFolder = System.getenv("HOME") + File.separator + ".magura/repos"
  val virtualSystem = new VirtualSystem(System.getenv("HOME") + File.separator + ".magura/vsys")

  override def name(): String = "cpp-build"

  override def repositoryProviders(): Map[String, RepositoryProvider] = Map(
    "github.com" -> new GithubRepositoryProvider(),
    "local" -> new LocalRepositoryProvider()
  )

  override def generators(): Map[String, Generator] = Map(
    "plugin" -> new PluginLoader,
    "cmake" -> new CMakeBuilder(virtualSystem),
    "configure" -> new ConfigureBuilder(virtualSystem)
  )

  override def generatorField(maguraFile: MaguraFile): String = maguraFile.builder
}
