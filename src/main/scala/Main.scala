import org.burbokop.generators.{CMakeGenerator, GeneratorDistributor}
import org.burbokop.routes.GithubRoutes
import org.burbokop.utils.ZipUtils

import java.io.{ByteArrayInputStream, File, FileOutputStream}

object Main extends App {
  val generatorDistributor = new GeneratorDistributor(Map("cmake" -> new CMakeGenerator()))

  def proceedRepository(user: String, repo: String, cacheFolder: String) =
    GithubRoutes.downloadRepositoryZip(user, repo, "")
      .body.fold(
      Left(_),
      { data =>
        ZipUtils.unzipToFolder(new ByteArrayInputStream(data), s"$cacheFolder${File.separator}$user${File.separator}$repo").fold(
          Left(_),
          { repoEntry =>
            generatorDistributor
              .proceed(
                s"$cacheFolder${File.separator}$user${File.separator}$repo${File.separator}$repoEntry",
                s"$cacheFolder${File.separator}$user${File.separator}$repo${File.separator}build_$repoEntry")
              .map(Left(_))
              .getOrElse(Right())
          }
        )
      }
    )

  val cacheFolder = System.getenv("HOME") + File.separator + ".magura/repos"

  val result = proceedRepository("burbokop", "SPM", cacheFolder)
  println(s"$result")
}
