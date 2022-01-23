package org.burbokop.magura.models.git

import io.github.burbokop.magura.models.repository.RepositoryCommit
import play.api.libs.json.Json

case class GithubCommit(sha: String) {
  def asApi = RepositoryCommit(sha)
}

object GithubCommit {
  implicit val format = Json.format[GithubCommit]
}