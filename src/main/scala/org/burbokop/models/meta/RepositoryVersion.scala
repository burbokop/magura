package org.burbokop.models.meta

import org.burbokop.generators.Generator.{DefaultOptions, Options}
import play.api.libs.json.Json

case class RepositoryVersion(
                              commit: String,
                              entryPath: String,
                              buildPaths: Map[String, Options],
                              builder: String
                            ) {
  def defaultBuildPath() =
    buildPaths
      .find(arg => arg._2.isInstanceOf[DefaultOptions])
      .map(_._1)
      .orElse(buildPaths.headOption.map(_._1))
}

object RepositoryVersion {
  implicit val format = Json.format[RepositoryVersion]
}