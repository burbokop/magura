package org.burbokop.magura

import org.burbokop.magura.generators.Generator.{DefaultOptions, Options}
import org.burbokop.magura.generators.cmake.CMakeBuilder.CMakeOptions
import org.burbokop.magura.tasks._
import org.burbokop.magura.utils.ReflectUtils._
import play.api.libs.json.{JsValue, Json}

import scala.reflect.runtime
import scala.reflect.runtime.{universe => ru}
import io.github.burbokop.magura.api.Plugin
import org.burbokop.magura.repository.MaguraRepository

import java.io.File

object Main extends App {
  val cacheFolder = System.getenv("HOME") + File.separator + ".magura/repos"

  val res = MaguraRepository.fromString("burbokop.magura_test_plugin.master")
    .fold(Left(_), repo => PluginLoader.load(repo, cacheFolder))

  println(s"load res: ${res}")

  val help =
    """
      |Usage: magura [command]
      |Commands:
      |  install  --  Installs package to cache
      |  connect  --  Connect project dependencies
      |  init     --  Init a project with magura.yaml
      |  build    --  Builds repo with multiple prefixes
      |  prune    --  Clear magura cache
      |  info     --  Display cache info
      |  version  --  Display magura version
      |""".stripMargin
  if (args.length > 0) {
    args(0) match {
      case "install" => InstallTask.exec(args.tail)
      case "connect" => ConnectTask.exec(args.tail)
      case "generate-release-info" => GenerateReleaseInfoTask.exec(args.tail)
      case "init" => InitTask.exec(args.tail)
      case "prune" => ClearCacheTask.exec(args.tail)
      case "info" => CacheInfoTask.exec(args.tail)
      case "build" => BuildTask.exec(args.tail)
      case "version" => println(s"${maguraApp.BuildInfo.name} ${maguraApp.BuildInfo.version}")
      case _ => println(help)
    }
  } else {
    println(help)
  }
}
