package org.burbokop.generators

import org.yaml.snakeyaml.Yaml

import java.io.{FileInputStream, InputStream}
import java.util

case class MaguraFile(generator: String)



object MaguraFile {
  case class Error(message: String) extends Exception(message)

  def fromMap(map: util.LinkedHashMap[String, String]): Either[Error, MaguraFile] = {
    val generator = map.get("generator")
    if (generator.isEmpty) Left(Error("generator not exist or empty"))
    else Right(MaguraFile(generator))
  }

  def fromYaml(inputStream: InputStream): Either[Error, MaguraFile] =
    fromMap(new Yaml()
      .load(inputStream)
      .asInstanceOf[util.LinkedHashMap[String, String]])

  def fromYaml(path: String): Either[Error, MaguraFile] = try {
    fromYaml(new FileInputStream(path))
  } catch {
    case e => Left(Error(e.getMessage))
  }
}