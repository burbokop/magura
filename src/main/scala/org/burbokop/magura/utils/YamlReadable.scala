package org.burbokop.magura.utils

import org.yaml.snakeyaml.Yaml
import scala.jdk.CollectionConverters.{CollectionHasAsScala, MapHasAsScala}

import java.io.{FileInputStream, IOException, InputStream}
import scala.collection.mutable

object YamlReadable {
  case class Error(message: String) extends Exception(message)
}

trait YamlReadable[T] {
  def fromMap(map: mutable.Map[String, Any]): Either[Throwable, T]

  def fromYaml(inputStream: InputStream): Either[Throwable, T] =
    fromMap(Option(new Yaml().load(inputStream).asInstanceOf[java.util.Map[String, Any]].asScala).getOrElse(mutable.Map()))

  def fromYaml(path: String): Either[Throwable, T] = try {
    fromYaml(new FileInputStream(path))
  } catch {
    case e: IOException => Left(YamlReadable.Error(e.getMessage))
  }
}
