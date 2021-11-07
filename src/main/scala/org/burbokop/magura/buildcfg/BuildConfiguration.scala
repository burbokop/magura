package org.burbokop.magura.buildcfg

import org.burbokop.magura.buildcfg
import org.burbokop.magura.repository.MaguraRepository
import org.burbokop.magura.utils.EitherUtils.ThrowableListImplicits
import org.burbokop.magura.utils.YamlReadable
import play.api.libs.json.JsObject

import scala.jdk.CollectionConverters.{CollectionHasAsScala, MapHasAsScala}
import org.burbokop.magura.utils.EitherUtils.ThrowableListImplicits._

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
            ThrowableListImplicits(p).reducedPartitionEither.map(BuildConfiguration(repository, _))
          })
      })
      .getOrElse(Left(BuildConfiguration.Error("repository must be set", None)))

}