package org.burbokop.models.meta

import play.api.libs.json.Json

case class RepositoryVersion(
                              commit: String,
                              entryPath: String,
                              generatedPath: String
                            )

object RepositoryVersion {
  implicit val format = Json.format[RepositoryVersion]
}