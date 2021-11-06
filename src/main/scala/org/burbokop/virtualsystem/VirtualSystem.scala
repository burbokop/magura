package org.burbokop.virtualsystem

import org.burbokop.models.meta.{RepositoryMetaData, RepositoryVersion}
import org.burbokop.utils.FileUtils

import scala.reflect.io.Directory
//import org.apache.commons.io.FileUtils
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
  //FileUtils.copyDirectory(new File(metaData.buildPath), new File(path))

  def installRepository(metaData: RepositoryVersion): Either[Throwable, Unit] =
    metaData.defaultBuildPath()
      .map(bp => FileUtils.recursiveCopyDirectory(bp, path))
      .getOrElse(Left(new Exception("RepositoryVersion do not have any build")))

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
      .map(_.reduce((a, b) => new Exception(a.toString + " | " + b.toString)))
  }

  def clear(): Boolean = {
    val directory = new Directory(new File(path))
    directory.deleteRecursively()
  }
}
