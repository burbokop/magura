package org.burbokop.magura.repository

import io.github.burbokop.magura.models.repository.{RepositoryBranch, RepositoryCommit, RepositoryRelease}
import io.github.burbokop.magura.repository.RepositoryProvider
import org.burbokop.magura.routes.git.GithubRoutes
import org.burbokop.magura.utils.Logger.AnyImplicits

class GithubRepositoryProvider extends RepositoryProvider {
  override def defaultBranchName(): String = "master"

  override def branch(user: String, repo: String, branch: String): Either[Throwable, RepositoryBranch] =
    GithubRoutes.getBranch(user, repo, branch)
      .body
      .info
      .map(_.asApi)

  override def repositoryReleases(user: String, repo: String): Either[Throwable, List[RepositoryRelease]] =
    GithubRoutes.getRepositoryReleases(user, repo)
      .body
      .map(_.map(_.asApi))

  override def downloadZip(user: String, repo: String, branch: String): Either[Throwable, Array[Byte]] =
    GithubRoutes.downloadRepositoryZip(user, repo, branch)
      .body
      .info
      .left
      .map(new Exception(_))

  override def commit(user: String, repo: String, hash: String): Either[Throwable, RepositoryCommit] =
    GithubRoutes.getCommit(user, repo, hash)
      .body
      .info
      .map(_.asApi)
}
