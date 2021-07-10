package org.burbokop

import org.burbokop.tasks.{CacheInfoTask, ClearCacheTask, ConnectTask, GenerateReleaseInfoTask, InitTask, InstallTask}


object Main extends App {
  val help = "Usage: magura [command]\n" +
    "Commands:\n" +
    "\tinstall  --  Installs package to cache\n" +
    "\tconnect  --  Connect project dependencies\n" +
    "\tinit     --  Init a project with magura.yaml" +
    "\tprune    --  Clear magura cache" +
    "\tinfo     --  Display cache info"
  if (args.length > 0) {
    args(0) match {
      case "install" => InstallTask.exec(args.tail)
      case "connect" => ConnectTask.exec(args.tail)
      case "generate-release-info" => GenerateReleaseInfoTask.exec(args.tail)
      case "init" => InitTask.exec(args.tail)
      case "prune" => ClearCacheTask.exec(args.tail)
      case "info" => CacheInfoTask.exec(args.tail)
      case "version" => println(s"${maguraApp.BuildInfo.name} ${maguraApp.BuildInfo.version}")
      case _ => println(help)
    }
  } else {
    println(help)
  }
}
