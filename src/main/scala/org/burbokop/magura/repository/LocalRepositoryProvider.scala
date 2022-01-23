package org.burbokop.magura.repository

import com.github.benmanes.caffeine.cache.Cache
import io.github.burbokop.magura.models.repository.{RepositoryBranch, RepositoryCommit, RepositoryRelease}
import io.github.burbokop.magura.repository.RepositoryProvider
import io.github.burbokop.magura.utils.FileUtils
import io.github.burbokop.magura.utils.FileUtils.RichFile
import io.github.burbokop.magura.utils.HashUtils.FileImplicits.apply
import io.github.burbokop.magura.utils.HashUtils.StringImplicits.{apply => apply2}
import org.burbokop.magura.repository.LocalRepositoryProvider.rootUser
import org.burbokop.magura.utils.{SystemUserInfo, SystemUserInfoProvider}

import java.io.File
import scala.collection.mutable


object LocalRepositoryProvider {
  val rootUser = "sysroot"
}

class LocalRepositoryProvider extends RepositoryProvider {
  def repoDirectory(user: String, repo: String): Option[File] =
    if(user == rootUser) {
      val file = new File(repo)
      if(file.isDirectory) Some(file)
      else None
    } else {
      SystemUserInfoProvider.get().flatMap(_.info(user)).map { info =>
        info.homeDirectory / repo
      }
    }


  private var directoryMd5Cache: mutable.Map[File, String] = mutable.Map()
  def directoryMd5(dir: File): String =
    directoryMd5Cache.getOrElse(dir, {
      val md5 = FileUtils.recursiveListFiles(dir)
        .map(_.contentMd5)
        .reduce(_ + _)
        .md5
      directoryMd5Cache.put(dir, md5)
      md5
    })

  override def defaultBranchName(): String = "default"

  override def branch(user: String, repo: String, branch: String): Either[Throwable, RepositoryBranch] = {
    repoDirectory(user, repo).map(dir => Right({
      RepositoryBranch(RepositoryCommit(directoryMd5(dir)))
    }))
      .getOrElse(Left(new Exception(s"repo $user.$repo not found on local machine")))
  }

  override def repositoryReleases(user: String, repo: String): Either[Throwable, List[RepositoryRelease]] =
    Left(new Exception("not implemented"))

  override def download(user: String, repo: String, branch: String, dst: File): Either[Throwable, String] = {
    if(user == "sysroot") {
      FileUtils.recursiveCopyDirectory(repo, dst.getAbsolutePath)
        .map(_ => directoryMd5(new File(repo)))
    } else {
      SystemUserInfoProvider.get().flatMap(_.info(user)).map { info =>
        FileUtils.recursiveCopyDirectory((info.homeDirectory / repo).getAbsolutePath, dst.getAbsolutePath)
          .map(_ => directoryMd5(new File(repo)))
      } getOrElse {
        Left(new Exception("user not found or unsupported os"))
      }
    }
  }

  override def downloadZip(user: String, repo: String, branch: String): Either[Throwable, Array[Byte]] =
    Left(new Exception("not implemented"))
}
