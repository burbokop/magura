package org.burbokop.generators

import org.burbokop.generators.CMakeDirectConnector.connectMetas
import org.burbokop.models.meta.RepositoryMetaData
import org.burbokop.repository.MaguraRepository
import org.burbokop.utils.FileUtils
import org.burbokop.virtualsystem.VirtualSystem

import java.io.File
import scala.Console.{GREEN, YELLOW}
import scala.io.AnsiColor.RESET
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
      m.versions.find(_.commit == m.currentCommit).map { version =>
        Project(findLibraries(new File(version.buildPath)), version.buildPath)
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
                        maguraFile: MaguraFile
                      ): Either[Throwable, Boolean] = {
    MaguraRepository.get(builderDistributor, maguraFile.dependencies, cacheFolder)
      .fold(Left(_), { metas =>
        connectMetas(metas, outputPath)
      })
  }
}
