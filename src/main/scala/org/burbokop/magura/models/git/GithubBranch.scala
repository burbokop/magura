package org.burbokop.magura.models.git

import play.api.libs.json.Json

case class GithubBranch(
                         commit: GithubCommit
                       )

object GithubBranch {
  implicit val format = Json.format[GithubBranch]
}