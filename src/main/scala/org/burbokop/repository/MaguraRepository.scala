package org.burbokop.repository

import org.burbokop.generators.{GeneratorDistributor, MaguraFile}
import org.burbokop.models.meta.{RepositoryMetaData, RepositoryVersion}
import org.burbokop.routes.git.GithubRoutes
import org.burbokop.utils.{ReducedError, ZipUtils}
import org.burbokop.virtualsystem.VirtualSystem

import java.io.{ByteArrayInputStream, File}

case class MaguraRepository(
                             user: String,
                             name: String,
                             branchName: String,
                             builder: Option[String]
                           )

object MaguraRepository {
  case class Error(message: String) extends Exception(message)

  def fromString(string: String): Either[Throwable, MaguraRepository] = {
    val parts = string.split('.')
    if (parts.length == 3) {
      val parts2 = parts(2).split(':')
      if (parts2.length == 2) {
        Right(MaguraRepository(parts(0), parts(1), parts2(0), Some(parts2(1))))
      } else if(parts2.length == 1) {
        Right(MaguraRepository(parts(0), parts(1), parts2(0), None))
      } else {
        Left(MaguraRepository.Error(s"repo should be {user}.{repo}.{branch(default = master)}:{builder(optional)} but got '$string'"))
      }
    } else if(parts.length == 2) {
      Right(MaguraRepository(parts(0), parts(1), "master", None))
    } else {
      Left(MaguraRepository.Error(s"repo should be {user}.{repo}.{branch}:{builder(optional)} but got '$string'"))
    }
  }

  val metaFileName = "meta.json"

  def get(
           builderDistributor: GeneratorDistributor,
           repository: MaguraRepository,
           cacheFolder: String,
         ): Either[Throwable, RepositoryMetaData] = {
    val repoFolder = s"$cacheFolder${File.separator}${repository.user}${File.separator}${repository.name}"
    val metaFile = s"$repoFolder${File.separator}$metaFileName"
    GithubRoutes.getBranch(repository.user, repository.name, repository.branchName).body.fold(Left(_), { branch =>
      val meta = RepositoryMetaData.fromJsonFileDefault(metaFile)
      if(meta.currentCommit != branch.commit.sha) {
        val e = GithubRoutes.downloadRepositoryZip(repository.user, repository.name, repository.branchName)
          .body.fold(e => Left(MaguraRepository.Error(e)), { data =>
          ZipUtils.unzipToFolder(new ByteArrayInputStream(data), repoFolder).fold(Left(_), { repoEntry =>
            val entryFolder = s"$repoFolder${File.separator}$repoEntry"
            val buildFolder = s"$repoFolder${File.separator}build_$repoEntry"
            builderDistributor
              .proceed(
                RepositoryMetaData.fromFolder(new File(cacheFolder), metaFileName, 3),
                entryFolder,
                buildFolder,
                repository.builder.map(MaguraFile.fromBuilder(_))
              )
              .fold(Left(_), { generatorName =>
                generatorName.map { generatorName =>
                  meta.withVersion(RepositoryVersion(
                    branch.commit.sha,
                    entryFolder,
                    buildFolder,
                    generatorName
                  )).writeJsonToFile(metaFile, true)
                } getOrElse {
                  Right(meta)
                }
              })
          })
        })
        println(s"ee: $e")
        e
      } else {
        Right(meta)
      }
    })
  }

  def get(
           builderDistributor: GeneratorDistributor,
           repos: List[MaguraRepository],
           cacheFolder: String,
         ): Either[Throwable, List[RepositoryMetaData]] =
    (repos
      .map(repo => MaguraRepository.get(builderDistributor, repo, cacheFolder))
      .partition(_.isLeft) match {
      case (Nil,  ints) => Right(for(Right(i) <- ints) yield i)
      case (strings, _) => Left(for(Left(s) <- strings) yield s)
    })
      .left
      .map(e => ReducedError(e))
}
