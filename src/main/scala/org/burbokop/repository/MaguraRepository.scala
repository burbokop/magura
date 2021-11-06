package org.burbokop.repository

import org.burbokop.generators.Generator.{DefaultOptions, Options}
import org.burbokop.generators.{GeneratorDistributor, MaguraFile}
import org.burbokop.models.meta.{RepositoryMetaData, RepositoryVersion}
import org.burbokop.routes.git.GithubRoutes
import org.burbokop.utils.{ReducedError, ZipUtils}
import org.burbokop.utils.FileUtils./

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
           optionsSet: Set[Options] = Set(new DefaultOptions())
         ): Either[Throwable, RepositoryMetaData] = {
    val repoFolder = s"$cacheFolder${/}${repository.user}${/}${repository.name}"
    val metaFile = s"$repoFolder${/}$metaFileName"
    GithubRoutes.getBranch(repository.user, repository.name, repository.branchName).body.fold(Left(_), { branch =>
      val meta = RepositoryMetaData.fromJsonFileDefault(metaFile)
      if(meta.currentCommit != branch.commit.sha) {
        GithubRoutes.downloadRepositoryZip(repository.user, repository.name, repository.branchName)
          .body.fold(e => Left(MaguraRepository.Error(e)), { data =>
          ZipUtils.unzipToFolder(new ByteArrayInputStream(data), repoFolder).fold(Left(_), { repoEntry =>
            val entryFolder = s"$repoFolder${/}$repoEntry"
            val buildPaths: Map[String, Options] =
              optionsSet.map(options => (s"$repoFolder${/}build_${options.hashName()}_$repoEntry", options)).toMap

            println(s"optionsSet: $optionsSet")
            println(s"entryFolder: $entryFolder")
            println(s"buildPaths: $buildPaths")

            builderDistributor
              .proceed(
                RepositoryMetaData.fromFolder(new File(cacheFolder), metaFileName, 3),
                entryFolder,
                buildPaths,
                repository.builder.map(MaguraFile.fromBuilder(_))
              )
              .fold[Either[Throwable, RepositoryMetaData]](Left(_), generatorName => {
                generatorName.map { generatorName =>
                  meta.withVersion(RepositoryVersion(
                    branch.commit.sha,
                    entryFolder,
                    buildPaths,
                    generatorName
                  )).writeJsonToFile(metaFile, true)
                } getOrElse {
                  Right(meta)
                }
              })
          })
        })
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
