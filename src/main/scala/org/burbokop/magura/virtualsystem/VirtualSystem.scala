package org.burbokop.magura.virtualsystem

import org.burbokop.magura.models.meta.{RepositoryMetaData, RepositoryVersion}
import org.burbokop.magura.utils.FileUtils
import org.burbokop.magura.virtualsystem.VirtualSystem.{Installer, defaultInstaller}

import scala.reflect.io.Directory
import org.burbokop.magura.utils.EitherUtils.ListImplicits.apply
import org.burbokop.magura.virtualsystem.VirtualSystem.createEnvironment

import java.io.File

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


  type Installer = (String, String) => Either[Throwable, Unit]
  def defaultInstaller: Installer = FileUtils.recursiveCopyDirectory
}

class VirtualSystem(path: String, installers: Map[String, Installer] = Map()) {
  def root(): String = path
  def bin(): String = path + File.separator + "bin"
  def include(): String = path + File.separator + "include"
  def lib(): String = path + File.separator + "lib"
  def env(): Seq[(String, String)] = createEnvironment(
    "PATH" -> bin,
    "CPATH" -> include,
    "LD_LIBRARY_PATH" -> lib
  )

  def installRepository(metaData: RepositoryVersion, installer: Installer = defaultInstaller): Either[Throwable, Boolean] =
    metaData.activeBuildPath
      .map(installer(_, path).map(_ => true))
      .getOrElse(Right(false))

  def installLatestVersionRepository(repos: RepositoryMetaData): Either[Throwable, Boolean] =
    repos
      .latestVersion
      .map(version => installRepository(version, installers.getOrElse(version.builder, defaultInstaller)))
      .getOrElse(Right(false))

  def installLatestVersionRepositories(repos: List[RepositoryMetaData]): Either[Throwable, List[Boolean]] = {
    repos
      .map(repo => installLatestVersionRepository(repo))
      .partitionEither
      .left
      .map(_.reduce((a, b) => new Exception(a.toString + " | " + b.toString)))
  }

  def update(repos: List[RepositoryMetaData]): Either[Throwable, List[Boolean]] = {
    clear()
    installLatestVersionRepositories(repos)
  }

  def clear(): Boolean = {
    val directory = new Directory(new File(path))
    directory.deleteRecursively()
  }
}
