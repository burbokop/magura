package org.burbokop.repository

import org.burbokop.generators.GeneratorDistributor
import org.burbokop.models.meta.{RepositoryMetaData, RepositoryVersion}
import org.burbokop.routes.git.GithubRoutes
import org.burbokop.utils.ZipUtils

import java.io.{ByteArrayInputStream, File}

case class MaguraRepository(
                           user: String,
                           repo: String,
                           branchName: String
                           )

object MaguraRepository {
  case class Error(message: String) extends Exception(message)

  def fromString(string: String): Either[Throwable, MaguraRepository] = {
    val parts = string.split('.')
    if (parts.length == 3) {
      Right(MaguraRepository(parts(0), parts(1), parts(2)))
    } else {
      Left(MaguraRepository.Error(s"repo should be {user}.{repo}.{branch} but got '$string'"))
    }
  }

  def get(builderDistributor: GeneratorDistributor, repository: MaguraRepository, cacheFolder: String): Either[Throwable, RepositoryMetaData] = {
    val repoFolder = s"$cacheFolder${File.separator}${repository.user}${File.separator}${repository.repo}"
    val metaFile = s"$repoFolder${File.separator}meta.json"
    GithubRoutes.getBranch(repository.user, repository.repo, repository.branchName).body.fold(e => Left(new RuntimeException(e)), { branch =>
      val meta = RepositoryMetaData.fromJsonDefault(metaFile)
      if(meta.currentCommit != branch.commit.sha) {
        GithubRoutes.downloadRepositoryZip(repository.user, repository.repo, repository.branchName)
          .body.fold(Left(_), { data =>
          ZipUtils.unzipToFolder(new ByteArrayInputStream(data), repoFolder).fold(Left(_), { repoEntry =>
            val entryFolder = s"$repoFolder${File.separator}$repoEntry"
            val buildFolder = s"$repoFolder${File.separator}build_$repoEntry"
            builderDistributor
              .proceed(entryFolder, buildFolder)
              .fold(Left(_), { _ =>
                meta.withVersion(RepositoryVersion(
                  branch.commit.sha,
                  entryFolder,
                  buildFolder
                )).writeJsonToFile(metaFile, true)
              })
          })
        })
        Right(meta)
      } else {
        Right(meta)
      }
    })
  }

  def get(builderDistributor: GeneratorDistributor, repos: List[MaguraRepository], cacheFolder: String): List[Either[Throwable, RepositoryMetaData]] =
    repos.map(repo => MaguraRepository.get(builderDistributor, repo, cacheFolder))
}
