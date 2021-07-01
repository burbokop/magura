package org.burbokop.virtualsystem

import org.burbokop.models.meta.{RepositoryMetaData, RepositoryVersion}
import org.apache.commons.io.FileUtils
import org.burbokop.utils.EitherUtils.ListImplicits.apply
import org.burbokop.virtualsystem.VirtualSystem.createEnvironment

import java.io.{File, IOException}

object VirtualSystem {
  def createEnvironment(records: (String, String)*): Seq[(String, String)] =
    records.map { r =>
      (
        r._1, sys.env.get(r._1)
        .map(List(_, r._2))
        .getOrElse(List(r._2))
        .mkString(":")
      )
    }
}

class VirtualSystem(path: String) {
  def root(): String = path
  def bin(): String = path + File.separator + "bin"
  def include(): String = path + File.separator + "include"
  def lib(): String = path + File.separator + "lib"
  def env(): Seq[(String, String)] = createEnvironment(
    "PATH" -> bin,
    "CPATH" -> include,
    "LD_LIBRARY_PATH" -> lib
  )

  def installRepository(metaData: RepositoryVersion): Either[Throwable, Unit] =
    try {
      FileUtils.copyDirectory(new File(metaData.buildPath), new File(path))
      Right()
    } catch {
      case e: IOException => Left(e)
    }

  def installLatestVersionRepository(repos: RepositoryMetaData): Either[Throwable, Boolean] =
    repos
      .latestVersion
      .map(version => installRepository(version).map(_ => true))
      .getOrElse(Right(false))

  def installLatestVersionRepositories(repos: List[RepositoryMetaData]): Either[Throwable, List[Boolean]] = {
    repos
      .map(repo => installLatestVersionRepository(repo))
      .partitionEither
      .left
      .map(_.reduce((a, b) => new Exception(a.getMessage + b.getMessage)))
  }
}
