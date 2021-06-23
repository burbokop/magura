package org.burbokop.tasks

import org.burbokop.repository.MaguraRepository
import org.burbokop.routes.git.GithubRoutes

import java.io.{File, FileOutputStream}

object GenerateReleaseInfo extends Task {
  override def exec(args: Array[String]): Unit = {
    if (args.length > 0) {
      val buildInfo = maguraApp.BuildInfo
      val currentReleaseTag = s"${buildInfo.name}-v${buildInfo.version}"
      val result = MaguraRepository
        .fromString(args(0))
        .fold(Left(_), { repo =>
          GithubRoutes.getRepositoryReleases(repo.user, repo.name).body
            .fold(Left(_), r => Right(r.map(_.tag_name).find(_ == currentReleaseTag).isEmpty))
        })
        .map { needRelease =>
          val releaseInfo = new File(s"${buildInfo.target.getPath}${File.separator}release.info")
          new FileOutputStream(releaseInfo.getPath).write(
            s"""|tag_name = $currentReleaseTag
                |need_release = $needRelease
                |deb = ${buildInfo.target.getName}${File.separator}${buildInfo.packageName}_${buildInfo.version}_all.deb
             """.stripMargin.toArray.map(_.toByte))
        }
      println(result)
    }
  }
}
