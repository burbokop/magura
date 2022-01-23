package org.burbokop.magura.tasks

import io.github.burbokop.magura.api.Plugin
import io.github.burbokop.magura.models.meta.RepositoryCache
import io.github.burbokop.magura.utils.FileUtils.RichFile
import org.burbokop.magura.plugins.CppBuildPlugin
import org.burbokop.magura.virtualsystem.VirtualSystem

import java.io.File
import scala.reflect.io.Path

object CacheInfoTask extends Task {
  override def exec(args: Array[String]): Unit = {
    val plugin = new CppBuildPlugin()

    val metaFileName = "meta.json"
    val caches = RepositoryCache.fromFolder(new File(plugin.cacheFolder), metaFileName, 3)

    println("cache meta data:")
    for(cache <- caches) {
      println(s"${cache.user}.${cache.repository}:")
      for(version <- cache.meta.versions) {
        println(s"\tcommit: ${version.commit}, builder: ${version.builder} | ${if(version.commit == cache.meta.currentCommit) "latest" else ""}")
      }
    }
  }
}
