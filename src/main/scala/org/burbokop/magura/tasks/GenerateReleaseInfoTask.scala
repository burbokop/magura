package org.burbokop.magura.tasks

import io.github.burbokop.magura.repository.MaguraRepository
import org.burbokop.magura.routes.git.GithubRoutes

import java.io.{File, FileOutputStream}

object GenerateReleaseInfoTask extends Task {
  override def exec(args: Array[String]): Unit = {
    if (args.length > 0) {
      val buildInfo = maguraApp.BuildInfo
      val currentReleaseTag = s"${buildInfo.name}-v${buildInfo.version}"
      val result = MaguraRepository
        .fromString(args(0))
        .fold(Left(_), { repo =>
          GithubRoutes.getRepositoryReleases(repo.user, repo.name).body
            .fold(Left(_), r => Right(!r.map(_.tag_name).contains(currentReleaseTag)))
        })
        .map { needRelease =>
          val releaseInfo = new File(s"${buildInfo.target.getPath}${File.separator}release.info")
          new FileOutputStream(releaseInfo.getPath).write(
            s"""|tag_name = $currentReleaseTag
                |need_release = $needRelease
                |deb = ${buildInfo.target.getPath}${File.separator}${buildInfo.packageName}_${buildInfo.version}_all.deb
             """.stripMargin.toArray.map(_.toByte))
        }
      println(result)
    }
  }
}
