package org.burbokop.models.meta

import org.burbokop.models.meta.RepositoryMetaData.fromJson
import org.burbokop.utils.FileUtils

import java.io.File

case class RepositoryCache(
                            meta: RepositoryMetaData,
                            user: String,
                            repository: String,
                          )

object RepositoryCache {
  def fromFolder(f: File, name: String, maxLevel: Int = Int.MaxValue): List[RepositoryCache] =
    FileUtils.recursiveListFiles(f, maxLevel).filter(item => item.isFile && item.getName == name).map { item =>
      fromJson(item.getPath).toOption.map(meta => RepositoryCache(
        meta = meta,
        user = item.getParentFile.getParentFile.getName,
        repository = item.getParentFile.getName
      ))
    }
      .filter(_.isDefined)
      .map(_.get)
      .toList
}
