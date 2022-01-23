package org.burbokop.magura.repository

import io.github.burbokop.magura.models.repository.{RepositoryBranch, RepositoryRelease}
import io.github.burbokop.magura.repository.RepositoryProvider
import org.burbokop.magura.routes.git.GithubRoutes

class GithubRepositoryProvider extends RepositoryProvider {
  override def defaultBranchName(): String = "master"

  override def branch(user: String, repo: String, branch: String): Either[Throwable, RepositoryBranch] =
    GithubRoutes.getBranch(user, repo, branch)
      .body
      .map(_.asApi)

  override def repositoryReleases(user: String, repo: String): Either[Throwable, List[RepositoryRelease]] =
    GithubRoutes.getRepositoryReleases(user, repo)
      .body
      .map(_.map(_.asApi))

  override def downloadZip(user: String, repo: String, branch: String): Either[Throwable, Array[Byte]] =
    GithubRoutes.downloadRepositoryZip(user, repo, branch)
      .body
      .left
      .map(new Exception(_))
}
