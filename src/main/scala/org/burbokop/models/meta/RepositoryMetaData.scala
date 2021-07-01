package org.burbokop.models.meta

import org.burbokop.models.meta.RepositoryMetaData.fromJson
import org.burbokop.utils.FileUtils
import org.burbokop.utils.SttpUtils.JsonParseException
import play.api.libs.json.{JsError, JsSuccess, Json}

import java.io.{File, FileInputStream, FileOutputStream, InputStream}


object RepositoryMetaData {
  implicit val format = Json.format[RepositoryMetaData]

  def fromFolder(f: File, name: String, maxLevel: Int = Int.MaxValue): List[RepositoryMetaData] =
    FileUtils.recursiveListFiles(f, maxLevel).filter(item => item.isFile && item.getName == name).map { item =>
      fromJson(item.getPath).toOption
    }
      .filter(_.isDefined)
      .map(_.get)
      .toList

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

  def writeJsonToFile(path: String, pretty: Boolean = false): Either[Throwable, RepositoryMetaData] =
    try {
      new FileOutputStream(path).write(toJson(pretty).toArray.map(_.toByte))
      Right(this)
    } catch {
      case e => Left(e)
    }

  def latestVersion(): Option[RepositoryVersion] =
    versions.find(_.commit == currentCommit)

  def withVersion(version: RepositoryVersion): RepositoryMetaData =
    RepositoryMetaData(version.commit, this.versions :+ version)
}
