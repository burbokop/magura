package org.burbokop.generators

import org.burbokop.generators.CMakeConnector.connectMetas
import org.burbokop.models.meta.RepositoryMetaData
import org.burbokop.repository.MaguraRepository
import org.burbokop.utils.FileUtils
import org.burbokop.virtualsystem.VirtualSystem

import java.io.File
import scala.Console.{GREEN, YELLOW}
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
      List(".so", ".a")
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
                    virtualSystem: Option[VirtualSystem]
                  ): Either[Throwable, Boolean] = {
    virtualSystem.map { vs =>
      println(s"${YELLOW}vs.installLatestVersionRepositories(cache): ${vs.installLatestVersionRepositories(metas)}$RESET")
    }

    println(s"${GREEN}connect: $outputPath, cache: $metas$RESET")

    // configure: /home/borys/.magura/repos/libsdl-org/SDL/libsdl-org-SDL-39302c9/
    // cache: Nil

    // configure: /home/borys/.magura/repos/libsdl-org/SDL_ttf/libsdl-org-SDL_ttf-9363bd0/
    // cache:
    //    RepositoryMetaData(39302c921445e9f695a72469be17ba041f6f09db,List(
    //      RepositoryVersion(39302c921445e9f695a72469be17ba041f6f09db,/home/borys/.magura/repos/libsdl-org/SDL/libsdl-org-SDL-39302c9/,/home/borys/.magura/repos/libsdl-org/SDL/build_libsdl-org-SDL-39302c9/,configure))
    //    ))

    // configure: /home/borys/.magura/repos/ferzkopp/SDL_gfx/ferzkopp-SDL_gfx-0df23ee/
    // cache:
    //    RepositoryMetaData(39302c921445e9f695a72469be17ba041f6f09db,List(
    //      RepositoryVersion(39302c921445e9f695a72469be17ba041f6f09db,/home/borys/.magura/repos/libsdl-org/SDL/libsdl-org-SDL-39302c9/,/home/borys/.magura/repos/libsdl-org/SDL/build_libsdl-org-SDL-39302c9/,configure)
    //    )),
    //    RepositoryMetaData(9363bd0f3b10aad5aaf73a63b9d085aba7ef7098,List(
    //      RepositoryVersion(9363bd0f3b10aad5aaf73a63b9d085aba7ef7098,/home/borys/.magura/repos/libsdl-org/SDL_ttf/libsdl-org-SDL_ttf-9363bd0/,/home/borys/.magura/repos/libsdl-org/SDL_ttf/build_libsdl-org-SDL_ttf-9363bd0/,configure))
    //    ))

    // build: /home/borys/.magura/repos/burbokop/SPM/burbokop-SPM-d3c51f5/
    // cache:
    //    RepositoryMetaData(39302c921445e9f695a72469be17ba041f6f09db,List(
    //      RepositoryVersion(39302c921445e9f695a72469be17ba041f6f09db,/home/borys/.magura/repos/libsdl-org/SDL/libsdl-org-SDL-39302c9/,/home/borys/.magura/repos/libsdl-org/SDL/build_libsdl-org-SDL-39302c9/,configure)
    //    )),
    //    RepositoryMetaData(9363bd0f3b10aad5aaf73a63b9d085aba7ef7098,List(
    //      RepositoryVersion(9363bd0f3b10aad5aaf73a63b9d085aba7ef7098,/home/borys/.magura/repos/libsdl-org/SDL_ttf/libsdl-org-SDL_ttf-9363bd0/,/home/borys/.magura/repos/libsdl-org/SDL_ttf/build_libsdl-org-SDL_ttf-9363bd0/,configure))
    //    ))

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

class CMakeConnector(
                      builderDistributor: GeneratorDistributor,
                      cacheFolder: String,
                    ) extends Generator {
  override def proceed(
                        cache: List[RepositoryMetaData],
                        virtualSystem: Option[VirtualSystem],
                        inputPath: String,
                        outputPath: String,
                        maguraFile: MaguraFile
                      ): Either[Throwable, Boolean] = {
    MaguraRepository.get(builderDistributor, maguraFile.dependencies, cacheFolder, virtualSystem)
      .fold(Left(_), { metas =>
        connectMetas(metas, outputPath, virtualSystem)
      })
  }
}
