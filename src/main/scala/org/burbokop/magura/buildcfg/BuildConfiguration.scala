package org.burbokop.magura.buildcfg

import org.burbokop.magura.repository.MaguraRepository
import org.burbokop.magura.utils.YamlReadable

import scala.collection.mutable
import scala.jdk.CollectionConverters.CollectionHasAsScala


case class BuildConfiguration(
                               repository: MaguraRepository,
                               prefixes: List[String]
                             )

object BuildConfiguration extends YamlReadable[BuildConfiguration] {
  case class Error(message: String, throwable: Option[Throwable]) extends Exception(message, throwable.orNull)

  override def fromMap(map: mutable.Map[String, Any]): Either[Throwable, BuildConfiguration] =
    map.get("repository")
      .map(repository => {
        MaguraRepository.fromString(repository.toString)
          .fold(e => Left(BuildConfiguration.Error("can not parse repository", Some(e))), repository => {
            Right(BuildConfiguration(repository, map.get("prefixes")
              .map(
                _.asInstanceOf[java.util.ArrayList[String]].asScala.toList
              ).getOrElse(List())))
          })
      })
      .getOrElse(Left(BuildConfiguration.Error("repository must be set", None)))

}