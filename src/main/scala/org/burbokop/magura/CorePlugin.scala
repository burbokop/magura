package org.burbokop.magura

import io.github.burbokop.magura.api.{Generator, Plugin, PluginLoader}
import io.github.burbokop.magura.repository.RepositoryProvider
import org.burbokop.magura.generators.cmake.{CMakeBuilder, CMakeConnector, CMakeDirectConnector}
import org.burbokop.magura.generators.configure.ConfigureBuilder
import org.burbokop.magura.repository._

class CorePlugin extends Plugin {
  override def name(): String = "core"

  override def repositoryProviders(): Map[String, RepositoryProvider] = Map(
    "github.com" -> new GithubRepositoryProvider(),
    "local" -> new LocalRepositoryProvider()
  )

  override def generators(): Map[String, Generator] = Map(
    "plugin" -> new PluginLoader,
    "cmake" -> new CMakeBuilder(),
    "configure" -> new ConfigureBuilder(),
    "cmake-connector" -> new CMakeConnector(),
    "cmake-dc" -> new CMakeDirectConnector()
  )
}
