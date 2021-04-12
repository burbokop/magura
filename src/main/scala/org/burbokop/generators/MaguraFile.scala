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

  def fromMap(map: mutable.Map[String, Any]): Either[Throwable, MaguraFile] = {
    val builder = map.get("builder").map(_.toString)
    val connector = map.get("connector").map(_.toString)
    val dependencies = map.get("dependencies").map(
      _.asInstanceOf[java.util.ArrayList[String]].asScala.toList.map(
        MaguraRepository.fromString(_)
      )
    ).getOrElse(List())
    if(builder.isEmpty && connector.isEmpty) {
      Left(MaguraFile.Error("builder or/and connector must be set"))
    } else {
      (dependencies.partition(_.isLeft) match {
        case (Nil,  ints) => Right(for(Right(i) <- ints) yield i)
        case (strings, _) => Left(for(Left(s) <- strings) yield s)
      }).fold(e => Left(e.reduce((a: Throwable, b: Throwable) => MaguraFile.Error(a.getMessage + ", " + b.getMessage))), { repos =>
        Right(MaguraFile(
          builder.getOrElse(""),
          connector.getOrElse(""),
          repos
        ))
      })
    }
  }

  def fromYaml(inputStream: InputStream): Either[Throwable, MaguraFile] =
    fromMap(new Yaml().load(inputStream).asInstanceOf[java.util.Map[String, Any]].asScala)

  def fromYaml(path: String): Either[Throwable, MaguraFile] = try {
    fromYaml(new FileInputStream(path))
  } catch {
    case e => Left(MaguraFile.Error(e.getMessage))
  }
}