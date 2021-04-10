package org.burbokop.models.meta

import org.burbokop.models.meta.RepositoryMetaData.fromJson
import org.burbokop.utils.SttpUtils.JsonParseException
import play.api.libs.json.{JsError, JsSuccess, Json}

import java.io.{FileInputStream, FileOutputStream, InputStream}


object RepositoryMetaData {
  implicit val format = Json.format[RepositoryMetaData]

  def toJson() = {

  }

  def fromJson(inputStream: InputStream): Either[Throwable, RepositoryMetaData] = {
    Json.parse(inputStream).validate[RepositoryMetaData] match {
      case s: JsSuccess[RepositoryMetaData] => Right(s.get)
      case jsError: JsError => Left(JsonParseException(jsError.toString, jsError, inputStream.toString))
    }
  }

  def fromJson(path: String): Either[Throwable, RepositoryMetaData] = try {
    fromJson(new FileInputStream(path))
  } catch {
    case e => Left(e)
  }

  def fromJsonDefault(path: String): RepositoryMetaData =
    fromJson(path)
      .fold[RepositoryMetaData](_ => RepositoryMetaData("", List()), d => d)

}

case class RepositoryMetaData(
                               currentCommit: String,
                               versions: List[RepositoryVersion]
                             ) {
  def toJson(pretty: Boolean = false) =
    if (pretty) Json.prettyPrint(Json.toJson(this))
    else Json.stringify(Json.toJson(this))

  def writeJsonToFile(path: String, pretty: Boolean = false): Either[Throwable, Unit] =
    try {
      Right(new FileOutputStream(path).write(toJson(pretty).toArray.map(_.toByte)))
    } catch {
      case e => Left(e)
    }

  def withVersion(version: RepositoryVersion): RepositoryMetaData =
    RepositoryMetaData(version.commit, this.versions :+ version)
}
