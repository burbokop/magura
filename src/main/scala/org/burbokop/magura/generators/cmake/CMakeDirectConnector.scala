package org.burbokop.magura.generators.cmake

import io.github.burbokop.magura.api.Generator.Options
import io.github.burbokop.magura.api.{Generator, GeneratorDistributor, MaguraFile}
import io.github.burbokop.magura.models.meta.RepositoryMetaData
import io.github.burbokop.magura.repository.MaguraRepository
import io.github.burbokop.magura.utils.FileUtils
import org.burbokop.magura.generators.cmake.CMakeDirectConnector.connectMetas

import java.io.File
import scala.language.{implicitConversions, postfixOps}

object CMakeDirectConnector {
  case class Error(message: String) extends Exception(message)

  case class Library(name: String, folder: String)
  implicit def libraryAsPair(library: Library): (String, String) = (library.name, library.folder)
  case class Project(libraries: List[Library], path: String)


  def libraryFromList(list: List[String]) = if (list.length == 2) {
    Some(Library(list(1), list(0)))
  } else {
    None
  }

  def findLibraries(file: File): List[Library] = {
    FileUtils.recursiveListFiles(file).map { file =>
      List("\\.a")
        .map(suf => s"(.*)\\/lib([^\\/]+)$suf.*".r.findFirstMatchIn(file.getPath)
          .map(regMatch => libraryFromList(regMatch.subgroups))
        ).find(_.isDefined).getOrElse(None)
    }
      .filter(_.isDefined).map(_.get)
      .filter(_.isDefined).map(_.get)
      .toList
  }

  def generateCMake(projects: List[Project]): String = {
    val t = (for(p <- projects; lib <- p.libraries) yield lib).unzip
    val includeDirs = projects.map(p => s"${p.path}/headers").mkString("\n\t\t")
    val libs = t._1.mkString("\n\t\t")
    val libDirs = t._2.mkString("\n\t\t")
    if (libs.length > 0) {
      s"""
         |function(target_connect_magura TARGET)
         |\ttarget_include_directories($${TARGET} PRIVATE\n\t\t$includeDirs)
         |\ttarget_link_directories($${TARGET} PRIVATE\n\t\t$libDirs)
         |\ttarget_link_libraries($${TARGET}\n\t\t$libs)
         |endfunction()
         |""".stripMargin
    } else if(includeDirs.length > 0) {
      s"""
         |function(target_connect_magura TARGET)
         |\ttarget_include_directories($${TARGET} PRIVATE\n\t\t$includeDirs)
         |endfunction()
         |""".stripMargin
    } else {
      ""
    }
  }

  def connectMetas(
                    metas: List[RepositoryMetaData],
                    outputPath: String,
                  ): Either[Throwable, Boolean] = {
    val projects = (for(m <- metas) yield {
      m.latestVersion().flatMap { version =>
        version.defaultBuildPath().map { buildPath =>
          Project(findLibraries(new File(buildPath)), buildPath)
        }
      }
    })
      .filter(_.isDefined)
      .map(_.get)

    FileUtils.writeIfDifferent(
      s"$outputPath/magura_build_info.cmake",
      generateCMake(projects)
    )
  }
}

class CMakeDirectConnector(
                            builderDistributor: GeneratorDistributor,
                            cacheFolder: String,
                          ) extends Generator {
  override def proceed(
                        cache: List[RepositoryMetaData],
                        inputPath: String,
                        outputPath: String,
                        options: Options,
                        maguraFile: MaguraFile
                      ): Either[Throwable, Generator.Result] = {
    MaguraRepository.get(builderDistributor, maguraFile.dependencies, cacheFolder)
      ._2
      .fold(Left(_), { metas =>
        connectMetas(metas, outputPath).map(Generator.Result(_))
      })
  }
}
