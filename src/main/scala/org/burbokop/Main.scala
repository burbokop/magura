package org.burbokop

import org.burbokop.generators.Generator.Options
import org.burbokop.generators.cmake.CMakeBuilder.CMakeOptions
import org.burbokop.tasks._
import org.burbokop.utils.java.OptionsType
import play.api.libs.json.JsValue

import scala.reflect.runtime.{universe => ru}
import ru._

object Main extends App {

  //val reflections = new Reflections(new ConfigurationBuilder()
  //  .filterInputsBy(new FilterBuilder().includePackage("my.project.prefix"))
  //  .setUrls(ClasspathHelper.forPackage("my.project.prefix"))
  //  .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner().filterResultsBy(optionalFilter), ...));

  //ru.Type


  //typeOf[CMakeOptions].typeSymbol.name

  //TermName("").

  val a = OptionsType.annotations("org.burbokop.generators.cmake.CMakeBuilder.CMakeOptions")

  println(s"AAA: $a")

  val mmm = OptionsType.findAnnotationMethod("org.burbokop.generators.cmake.CMakeBuilder.CMakeOptions", typeOf[Options], typeOf[JsValue])

  println(s"mmm: $mmm")
  val mmm2 = OptionsType.findAnnotationMethod("org.burbokop.generators.cmake.CMakeBuilder.CMakeOptions", typeOf[JsValue], typeOf[Options])
  println(s"mmm2: $mmm2")

  println(s"serialized: ${OptionsType.serialize("org.burbokop.generators.cmake.CMakeBuilder.CMakeOptions", CMakeOptions("gogadoda"))}")

  def annots = typeOf[CMakeOptions]
    .typeSymbol
    //.asClass
    .annotations

  println(s"annots: $annots")

  annots.foreach({ a =>
    println(s"${a.tree} -> ${a.tree.children} -> ${a.tree.symbol.typeSignature}")

    println(s"${a.getClass.getTypeName} \ta.tree.symbol { ${a.tree.symbol.name} }: ${a.tree.symbol}")

    println(s"\tannot { ${a.getClass} }: ${a}")
    a.tree.children.foreach({ c =>
      println(s"c.children: ${c.children}")

      c.children.foreach(cc => {
        println(s"\t\t\tcc.symbol.isMethod: ${Option(cc.symbol).map(_.isMethod)}")
      })
      println(s"c.symbol.isMethod: ${c.symbol.isMethod}")
      if(c.symbol.isMethod) {
        //c.symbol.name match {
        //  case "ser" => rootMirror.reflect().reflectMethod(c.symbol.asMethod)
        //  case "des" => rootMirror.reflect().reflectMethod(c.symbol.asMethod)
        //}
      }

      println(s"\tc.symbol { ${c.symbol.getClass} }: ${c.symbol}")


      if(c.isInstanceOf[scala.reflect.runtime.universe.Function]) {
        def f = c.asInstanceOf[scala.reflect.runtime.universe.Function]
        f.symbol.isMethod
        println(s"\t\tf.isInstanceOf f.symbol.isMethod: ${f.children}")
      } else if(c.isInstanceOf[scala.reflect.runtime.universe.Select]) {
        def s = c.asInstanceOf[scala.reflect.runtime.universe.Select]
        println(s"\t\ts.name: ${s.name}")
      }

      println(s"\t\tchild { ${c.getClass} }: ${c}, ${}")
    })
  })


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
      |""".stripMargin
  if (args.length > 0) {
    args(0) match {
      case "install" => InstallTask.exec(args.tail)
      case "connect" => ConnectTask.exec(args.tail)
      case "generate-release-info" => GenerateReleaseInfoTask.exec(args.tail)
      case "init" => InitTask.exec(args.tail)
      case "prune" => ClearCacheTask.exec(args.tail)
      case "info" => CacheInfoTask.exec(args.tail)
      case "version" => println(s"${maguraApp.BuildInfo.name} ${maguraApp.BuildInfo.version}")
      case "build" => BuildTask.exec(args.tail)
      case _ => println(help)
    }
  } else {
    println(help)
  }
}
