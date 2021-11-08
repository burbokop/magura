package org.burbokop.magura.generators.cmake

import org.burbokop.magura.generators.Generator.Options
import org.burbokop.magura.generators.cmake.CMakeConnector.connectMetas
import org.burbokop.magura.generators.{Generator, GeneratorDistributor, MaguraFile}
import org.burbokop.magura.models.meta.RepositoryMetaData
import org.burbokop.magura.repository.MaguraRepository
import org.burbokop.magura.utils.FileUtils
import org.burbokop.magura.utils.HashUtils.StringImplicits.apply
import org.burbokop.magura.virtualsystem.VirtualSystem
import play.api.libs.json.Json

import java.io.File
import scala.Console._
import scala.io.AnsiColor.RESET
import scala.language.{implicitConversions, postfixOps}

object CMakeConnector {
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

  def generateCMake(md5: String, virtualSystem: VirtualSystem, projects: List[Project]): String = {
    val t = (for(p <- projects; lib <- p.libraries) yield lib).unzip
    val includeDirs = virtualSystem.include
    val libs = t._1.reverse.mkString("\n    ")
    val libDirs = virtualSystem.lib
    if (libs.length > 0) {
      s"""
         |function(target_connect_magura_$md5 TARGET)
         |  message("connection $md5 on target $${TARGET}")
         |  target_include_directories($${TARGET} PRIVATE\n    $includeDirs)
         |  target_link_directories($${TARGET} PUBLIC\n    $libDirs)
         |  # Here may be error (undefined ref) depends on order of linked libs. This error appears from ld linker
         |  target_link_libraries($${TARGET}\n    $libs)
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

  def generateMasterCMake(targetMetaData: TargetMetaData): String =
    (for(p <- targetMetaData.paths) yield {
      s"""include($${CMAKE_CURRENT_LIST_DIR}/${p.md5}.cmake)
         |""".stripMargin
    }).reduce(_ + _) +
      s"""
         |function(target_connect_magura TARGET)
         |""".stripMargin +
      (for(p <- targetMetaData.paths) yield {
        s"""${if(targetMetaData.paths.head == p) "  if" else "  elseif"}("$${CMAKE_PARENT_LIST_FILE}" STREQUAL "$p")
           |    target_connect_magura_${p.md5}($${TARGET})
           |""".stripMargin
      }).reduce(_ + _) +
      """  else()
        |    message(FATAL_ERROR "error: undefined parent cmake")
        |  endif()
        |endfunction()
        |""".stripMargin

  def connectMetas(
                    metas: List[RepositoryMetaData],
                    inputPath: String,
                    outputPath: String,
                    virtualSystem: VirtualSystem,
                    projectFile: String
                  ): Either[Throwable, Boolean] = {
    virtualSystem.update(metas).fold(Left(_), { oks =>
      if(oks.forall(b => b)) {
        val projects = (for(m <- metas) yield {
          m.latestVersion().flatMap { version =>
            version.defaultBuildPath().map(buildPath => {
              Project(findLibraries(new File(buildPath)), buildPath)
            })
          }
        })
          .filter(_.isDefined)
          .map(_.get)

        val projectFileMd5 = projectFile.md5

        TargetMetaData.insert(s"$outputPath/magura_build_info.d/target_meta.json", projectFile, true)
          .fold(Left(_), { targetMetaData =>
            val masterCMake = generateMasterCMake(targetMetaData)
            println(s"${CYAN}Master cmake:\n$masterCMake$RESET")
            FileUtils.writeIfDifferent(
              s"$outputPath/magura_build_info.d/master.cmake",
              masterCMake
            ).fold(Left(_), { _ =>
              FileUtils.writeIfDifferent(
                s"$outputPath/magura_build_info.d/$projectFileMd5.cmake",
                {
                  val cmake = generateCMake(projectFileMd5, virtualSystem, projects)
                  println(s"${MAGENTA}Generated build info:\n$cmake$RESET")
                  cmake
                }
              )
            })
          })
      } else {
        Left(new Exception("not all repositories have latest version"))
      }
    })
  }
}

class CMakeConnector(
                      builderDistributor: GeneratorDistributor,
                      cacheFolder: String,
                      virtualSystem: VirtualSystem,
                      projectFile: String
                    ) extends Generator {
  override def proceed(
                        cache: List[RepositoryMetaData],
                        inputPath: String,
                        outputPath: String,
                        options: Options,
                        maguraFile: MaguraFile
                      ): Either[Throwable, Boolean] =
    MaguraRepository.get(builderDistributor, maguraFile.dependencies, cacheFolder)
      .fold(Left(_), { metas =>
        connectMetas(metas, inputPath, outputPath, virtualSystem, projectFile)
      })
}
