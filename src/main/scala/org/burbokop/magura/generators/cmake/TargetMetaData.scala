package org.burbokop.magura.generators.cmake

import io.github.burbokop.magura.utils.JsonUtils.JsonParseException
import play.api.libs.json.{JsError, JsSuccess, Json}

import java.io.{File, FileInputStream, FileOutputStream, InputStream}


object TargetMetaData {
  implicit val jsonFormat = Json.format[TargetMetaData]

  def fromJsonStream(inputStream: InputStream): Either[Throwable, TargetMetaData] =
    Json.parse(inputStream).validate[TargetMetaData] match {
      case s: JsSuccess[TargetMetaData] => Right(s.get)
      case jsError: JsError => Left(JsonParseException(jsError.toString, jsError, inputStream.toString))
    }

  def fromJsonFile(path: String): Either[Throwable, TargetMetaData] =
    try {
      fromJsonStream(new FileInputStream(path))
    } catch {
      case e: Throwable => Left(e)
    }

  def fromJsonFile(file: File): Either[Throwable, TargetMetaData] =
    fromJsonFile(file.getPath)

  def fromJsonFileDefault(path: String): TargetMetaData =
    fromJsonFile(path)
      .fold[TargetMetaData](_ => TargetMetaData(Set()), d => d)

  def fromJsonFileDefault(file: File): TargetMetaData =
    fromJsonFileDefault(file.getPath)

  def insert(metaPath: String, inputPath: String, pretty: Boolean = false) =
    TargetMetaData(TargetMetaData.fromJsonFileDefault(metaPath).paths + inputPath)
      .writeJsonToFile(metaPath, pretty)
}

case class TargetMetaData(paths: Set[String]) {
  def toJson(pretty: Boolean = false): String =
    if (pretty) Json.prettyPrint(Json.toJson(this))
    else Json.stringify(Json.toJson(this))

  def writeJsonToFile(path: String, pretty: Boolean = false): Either[Throwable, TargetMetaData] =
    try {
      new File(path).getParentFile.mkdirs()
      new FileOutputStream(path).write(toJson(pretty).toArray.map(_.toByte))
      Right(this)
    } catch {
      case e: Throwable => Left(e)
    }
}