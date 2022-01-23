package org.burbokop.magura.buildcfg

import io.github.burbokop.magura.repository.MaguraRepository
import io.github.burbokop.magura.utils.EitherUtils.ThrowableListImplicits.apply
import io.github.burbokop.magura.utils.YamlReadable

import scala.collection.JavaConverters.{collectionAsScalaIterableConverter, mapAsScalaMapConverter}
/*
 * FOR SCALA 1.13 USE THIS
 * import scala.jdk.CollectionConverters.{CollectionHasAsScala, MapHasAsScala}
 */


case class BuildPrefix(path: String, isMain: Boolean)

object BuildPrefix extends YamlReadable[BuildPrefix] {
  override def fromObject(map: YamlObject[Any]): Either[Throwable, BuildPrefix] = {
    map.get("path").map(path =>
      map.get("isMain").map(isMain =>
        Option(isMain.asInstanceOf[Boolean])
          .map(isMain => Right(BuildPrefix(path.toString, isMain)))
          .getOrElse(Left(new Exception("isMain field is not boolean")))
      ).getOrElse(Right(BuildPrefix(path.toString, false)))
    ).getOrElse(Left(new Exception("path field not found")))
  }
}

case class BuildConfiguration(
                               repository: MaguraRepository,
                               prefixes: List[BuildPrefix]
                             )

object BuildConfiguration extends YamlReadable[BuildConfiguration] {
  case class Error(message: String, throwable: Option[Throwable]) extends Exception(message, throwable.orNull)

  override def fromObject(map: YamlObject[Any]): Either[Throwable, BuildConfiguration] =
    map.get("repository")
      .map(repository => {
        MaguraRepository.fromString(repository.toString)
          .fold(e => Left(BuildConfiguration.Error("can not parse repository", Some(e))), repository => {
            val p = map.get("prefixes")
              .map(_.asInstanceOf[JList[Any]].asScala.toList)
              .getOrElse(List())
              .map {
                case obj: JObject[Any] => BuildPrefix.fromObject(obj.asScala)
                case str: String => Right(BuildPrefix(str, isMain = false))
                case _ => Left(Error("null prefix", None))
              }
            Left(new Exception("not implemented"))
            //p.reducedPartitionEither.map(BuildConfiguration(repository, _))
          })
      })
      .getOrElse(Left(BuildConfiguration.Error("repository must be set", None)))

}