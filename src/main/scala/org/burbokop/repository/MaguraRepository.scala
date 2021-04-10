package org.burbokop.repository

import org.burbokop.generators.GeneratorDistributor
import org.burbokop.models.meta.{RepositoryMetaData, RepositoryVersion}
import org.burbokop.routes.git.GithubRoutes
import org.burbokop.utils.ZipUtils

import java.io.{ByteArrayInputStream, File}

object MaguraRepository {

  def install(generatorDistributor: GeneratorDistributor, user: String, repo: String, branchName: String, cacheFolder: String): Either[Throwable, String] = {
    val repoFolder = s"$cacheFolder${File.separator}$user${File.separator}$repo"
    val metaFile = s"$repoFolder${File.separator}meta.json"
    GithubRoutes.getBranch(user, repo, branchName).body.fold(e => Left(new RuntimeException(e)), { branch =>
      val meta = RepositoryMetaData.fromJsonDefault(metaFile)
      if(meta.currentCommit != branch.commit.sha) {
        GithubRoutes.downloadRepositoryZip(user, repo, branchName)
          .body.fold(Left(_), { data =>
          ZipUtils.unzipToFolder(new ByteArrayInputStream(data), repoFolder).fold(Left(_), { repoEntry =>
            val entryFolder = s"$repoFolder${File.separator}$repoEntry"
            val generatedFolder = s"$repoFolder${File.separator}generated_$repoEntry"
            generatorDistributor
              .proceed(entryFolder, generatedFolder)
              .fold(Left(_), { _ =>
                meta.withVersion(RepositoryVersion(
                  branch.commit.sha,
                  entryFolder,
                  generatedFolder
                )).writeJsonToFile(metaFile, true)
              })
          })
        })
        Right("")
      } else {
        Right(s"already up to date. branch: ${branch.commit.sha}")
      }
    })
  }

}
