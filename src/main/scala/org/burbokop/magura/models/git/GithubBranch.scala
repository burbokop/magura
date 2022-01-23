package org.burbokop.magura.models.git

import io.github.burbokop.magura.models.repository.RepositoryBranch
import play.api.libs.json.Json

case class GithubBranch(commit: GithubCommit) {
  def asApi = RepositoryBranch(commit.asApi)
}

object GithubBranch {
  implicit val format = Json.format[GithubBranch]
}