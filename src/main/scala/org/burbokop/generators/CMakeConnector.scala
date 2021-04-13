package org.burbokop.generators

import org.burbokop.generators.CMakeConnector.connectMetas
import org.burbokop.models.meta.RepositoryMetaData
import org.burbokop.repository.MaguraRepository
import org.burbokop.utils.FileUtils

import java.io.{File, FileOutputStream}
import scala.language.postfixOps

object CMakeConnector {
  case class Error(message: String) extends Exception(message)

  case class Library(name: String, folder: String)
  def libraryFromList(list: List[String]) = if (list.length == 2) {
    Some(Library(list(1), list(0)))
  } else {
    None
  }

  def findLibraries(file: File): Array[Library] = {
    FileUtils.recursiveListFiles(file).map { file =>
      List(".so", ".a")
        .map(suf => s"(.*)\\/lib([^\\/]+)$suf.*".r.findFirstMatchIn(file.getPath)
          .map(regMatch => libraryFromList(regMatch.subgroups))
        ).find(_.isDefined).getOrElse(None)
    }
      .filter(_.isDefined).map(_.get)
      .filter(_.isDefined).map(_.get)
  }

  def connectMetas(metas: List[RepositoryMetaData], outputPath: String): Either[Error, Unit] = {
    val str = (for(m <- metas) yield {
      m.versions.find(_.commit == m.currentCommit).map { version =>
        println(s"build: ${version.buildPath}")
        findLibraries(new File(version.buildPath)).map { library =>
          s"set(MAGURA_LIBS ${'"'}$${MAGURA_LIBS} ${library.name}${'"'})\n" +
            s"link_directories(${'"'}${library.folder}${'"'})"
        }.toList :+ s"\ninclude_directories(${'"'}${version.buildPath}/headers${'"'})"
      } getOrElse {
        List()
      }
    }).flatten.reduce((a, b) => a + "\n" + b)

    println(s"libs:\n$str")

    new FileOutputStream(s"$outputPath/magura_build_info.cmake").write(str.toArray.map(_.toByte))

    println(s"connectMetas: $metas, $outputPath")
    Right()
  }
}

class CMakeConnector(builderDistributor: GeneratorDistributor, cacheFolder: String) extends Generator {
  override def proceed(inputPath: String, outputPath: String, maguraFile: MaguraFile): Either[Throwable, Unit] = {
    MaguraRepository.get(builderDistributor, maguraFile.dependencies, cacheFolder).fold(Left(_), { metas =>
      connectMetas(metas, outputPath)
    })
  }
}
