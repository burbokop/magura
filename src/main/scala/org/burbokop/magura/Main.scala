package org.burbokop.magura

import org.burbokop.magura.generators.Generator.{DefaultOptions, Options}
import org.burbokop.magura.generators.cmake.CMakeBuilder.CMakeOptions
import org.burbokop.magura.tasks._
import org.burbokop.magura.utils.ReflectUtils._
import play.api.libs.json.{JsValue, Json}

import scala.reflect.runtime
import scala.reflect.runtime.{universe => ru}


object Main extends App {


  def debugOptions(opt: Options) = {
    val optJson = Json.stringify(Json.toJson[Options](opt))
    val desOpt = Json.parse(optJson).validate[Options]

    println(s"opt: $opt")
    println(s"optJson: $optJson")
    println(s"desOpt: $desOpt")

  }

  debugOptions(CMakeOptions("gog"))
  debugOptions(DefaultOptions())


  val it = runtime.currentMirror.instanceType(CMakeOptions("ddd"))
  println(s"SSS2: $it")

  println(s"annot0: ${runtime.currentMirror.annotations(it.toString)}")

  println(s"serialized: ${runtime.currentMirror.invokeAttachedMethod[Options, JsValue](it.toString, CMakeOptions("gogadoda"))}")

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
