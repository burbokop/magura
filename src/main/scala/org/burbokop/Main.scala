package org.burbokop

import org.burbokop.tasks.{ConnectTask, InstallTask}

object Main extends App {
  val help = "Usage: magura [command]\n" +
    "Commands:\n" +
    "\tinstall  --  Installs package to cache\n" +
    "\tconnect  --  Connect project dependencies"
  if (args.length > 0) {
    args(0) match {
      case "install" => InstallTask.exec(args.tail)
      case "connect" => ConnectTask.exec(args.tail)
      case "version" => println(s"${maguraApp.BuildInfo.name} ${maguraApp.BuildInfo.version}")
      case _ => println(help)
    }
  } else {
    println(help)
  }
}