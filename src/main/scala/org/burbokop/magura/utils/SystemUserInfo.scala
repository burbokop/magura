package org.burbokop.magura.utils

import java.io.File
import scala.io.Source

case class SystemUserInfo(
                           name: String,
                           homeDirectory: File
                         )

trait SystemUserInfoProvider {
  def info(user: String): Option[SystemUserInfo]
}

object SystemUserInfoProvider {
  def get(): Option[SystemUserInfoProvider] =
    if(System.getProperty("os.name") == "linux") Some(new LinuxSystemUserInfoProvider())
    else None
}

object LinuxSystemUserInfoProvider {
  lazy val passwd = {
    val src = Source.fromFile("/etc/passwd")
    val lines = src.getLines()
    src.close()
    lines
  }
}

class LinuxSystemUserInfoProvider extends SystemUserInfoProvider {
  override def info(user: String): Option[SystemUserInfo] =
    LinuxSystemUserInfoProvider
      .passwd
      .find(_.startsWith(s"$user:"))
      .map(_.split(":"))
      .flatMap(line => if(line.length > 5) Some(SystemUserInfo(user, new File(line(5)))) else None)
}


