package org.burbokop.magura.tasks

import io.github.burbokop.magura.api.Plugin
import org.burbokop.magura.models.meta.{RepositoryCache, RepositoryMetaData}
import org.burbokop.magura.repository.MaguraRepository.metaFileName
import org.burbokop.magura.utils.FileUtils.RichFile
import org.burbokop.magura.virtualsystem.VirtualSystem

import java.io.File
import scala.reflect.io.Path

class Aaaaa extends Plugin {
  override def init(): Int = 0
  override def name(): String = "AAAAAAAA"
}

object CacheInfoTask extends Task {
  override def exec(args: Array[String]): Unit = {

    println("LLLLLLLLLLLLLLLLLLLLLLLLL")

    val cacheFolder = new File(System.getenv("HOME")) / ".magura" / "repos"

    val metaFileName = "meta.json"
    val caches = RepositoryCache.fromFolder(cacheFolder, metaFileName, 3)

    val ps = Plugin.loadFromPath((cacheFolder / "plugins").getPath)
    for(p <- ps) {
      println(s"plugin: ${p.name()}")
    }

    println("cache meta data:")
    for(cache <- caches) {
      println(s"${cache.user}.${cache.repository}:")
      for(version <- cache.meta.versions) {
        println(s"\tcommit: ${version.commit}, builder: ${version.builder} | ${if(version.commit == cache.meta.currentCommit) "latest" else ""}")
      }
    }
  }
}
