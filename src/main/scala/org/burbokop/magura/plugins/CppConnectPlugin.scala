package org.burbokop.magura.plugins

import io.github.burbokop.magura.api.{Generator, MaguraFile, Plugin}
import io.github.burbokop.magura.repository.RepositoryProvider
import org.burbokop.magura.generators.cmake.{CMakeConnector, CMakeDirectConnector}

class CppConnectPlugin extends Plugin {
  val buildPlugin = new CppBuildPlugin()

  override def name(): String = "cpp-connect"
  override def repositoryProviders(): Map[String, RepositoryProvider] = Map()
  override def generators(): Map[String, Generator] = Map(
    "cmake" -> new CMakeConnector(buildPlugin.newDistributor(), buildPlugin.cacheFolder, buildPlugin.virtualSystem),
    "cmake-dc" -> new CMakeDirectConnector(buildPlugin.newDistributor(), buildPlugin.cacheFolder)
  )
  override def generatorField(maguraFile: MaguraFile): String = maguraFile.connector
}
