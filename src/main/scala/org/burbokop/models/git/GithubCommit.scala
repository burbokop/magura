package org.burbokop.models.git

import play.api.libs.json.Json

case class GithubCommit(
                       sha: String
                       )

object GithubCommit {
  implicit val format = Json.format[GithubCommit]
}