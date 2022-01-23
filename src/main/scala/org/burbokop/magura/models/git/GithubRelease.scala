package org.burbokop.magura.models.git

import io.github.burbokop.magura.models.repository.RepositoryRelease
import play.api.libs.json.Json

case class GithubRelease(
                          tag_name: String,
                          name: Option[String]
                        ) {
  def asApi = RepositoryRelease(tag_name, name)
}

object GithubRelease {
  implicit val format = Json.format[GithubRelease]
}