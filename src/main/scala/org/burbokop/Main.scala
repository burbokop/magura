package org.burbokop

import org.burbokop.tasks._

object Main extends App {
  val reflections = new Reflections(new ConfigurationBuilder()
    .filterInputsBy(new FilterBuilder().includePackage("my.project.prefix"))
    .setUrls(ClasspathHelper.forPackage("my.project.prefix"))
    .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner().filterResultsBy(optionalFilter), ...));


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
