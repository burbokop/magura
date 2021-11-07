package org.burbokop.magura.models.git

import play.api.libs.json.Json

case class GithubRelease(
                          tag_name: String,
                          name: Option[String]
                        )

object GithubRelease {
  implicit val format = Json.format[GithubRelease]
}