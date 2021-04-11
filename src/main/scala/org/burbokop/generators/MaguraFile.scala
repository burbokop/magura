package org.burbokop.generators

import org.burbokop.repository.MaguraRepository
import org.yaml.snakeyaml.Yaml

import java.io.{FileInputStream, InputStream}
import scala.collection.mutable
import scala.jdk.CollectionConverters.{CollectionHasAsScala, MapHasAsScala}

case class MaguraFile(
                       builder: String,
                       connector: String,
                       dependencies: List[MaguraRepository]
                     )



object MaguraFile {
  case class Error(message: String) extends Exception(message)

  def fromMap(map: mutable.Map[String, Any]): Either[Error, MaguraFile] = {
    val builder = map.get("builder").map(_.toString)
    val connector = map.get("connector").map(_.toString)
    val dependencies = map.get("dependencies").map(
      _.asInstanceOf[java.util.ArrayList[String]].asScala.toList.map(
        MaguraRepository.fromString(_)
      )
    )
    if(builder.isEmpty && connector.isEmpty) {
      Left(MaguraFile.Error("builder or/and connector must be set"))
    } else {
      val left = dependencies.getOrElse(List()).find(_.isLeft)
      Right(MaguraFile(
        builder.getOrElse(""),
        connector.getOrElse(""),

      ))
    }
  }

  def fromYaml(inputStream: InputStream): Either[Error, MaguraFile] =
    fromMap(new Yaml().load(inputStream).asInstanceOf[java.util.Map[String, Any]].asScala)

  def fromYaml(path: String): Either[Error, MaguraFile] = try {
    fromYaml(new FileInputStream(path))
  } catch {
    case e => Left(MaguraFile.Error(e.getMessage))
  }
}