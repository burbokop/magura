import org.burbokop.generators.{CMakeGenerator, GeneratorDistributor}
import org.burbokop.models.meta.{RepositoryMetaData, RepositoryVersion}
import org.burbokop.repository.MaguraRepository
import org.burbokop.routes.git.GithubRoutes
import org.burbokop.utils.ZipUtils
import sttp.client3.SttpClientException

import java.io.{ByteArrayInputStream, File}

object Main extends App {
  val cacheFolder = System.getenv("HOME") + File.separator + ".magura/repos"
  val generatorDistributor = new GeneratorDistributor(Map("cmake" -> new CMakeGenerator()))
  MaguraRepository.download(generatorDistributor, "burbokop", "SPM", "master", cacheFolder).fold({ err =>
    println(s"error: $err")
  }, { message =>
    println(message)
  })
}
