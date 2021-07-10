package org.burbokop.tasks

import org.burbokop.models.meta.{RepositoryCache, RepositoryMetaData}
import org.burbokop.repository.MaguraRepository.metaFileName
import org.burbokop.virtualsystem.VirtualSystem

import java.io.File

object CacheInfoTask extends Task {
  override def exec(args: Array[String]): Unit = {
    val cacheFolder = System.getenv("HOME") + File.separator + ".magura/repos"
    val metaFileName = "meta.json"
    val caches = RepositoryCache.fromFolder(new File(cacheFolder), metaFileName, 3)
    println("cache meta data:")
    for(cache <- caches) {
      println(s"${cache.user}.${cache.repository}:")
      for(version <- cache.meta.versions) {
        println(s"\tcommit: ${version.commit}, builder: ${version.builder} | ${if(version.commit == cache.meta.currentCommit) "latest" else ""}")
      }
    }
  }
}
