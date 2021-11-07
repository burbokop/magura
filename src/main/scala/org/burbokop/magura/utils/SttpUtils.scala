package org.burbokop.magura.utils

import play.api.libs.json.{JsError, JsSuccess, Json, Reads}
import sttp.client3.{UriContext, asString}
import sttp.model.Uri

object SttpUtils {
  final case class HttpStatusException(statusCode: Int, message: String) extends Exception(message)

  final case class EmptyBodyException(message: String) extends Exception(message)

  final case class UnsuccessfulResponseException(uri: Uri, message: String) extends Exception(s"url: $uri, massage: $message")

  final case class JsonParseException(message: String, jsError: JsError, body: String) extends Exception(message)

  def deserializeEither[A](uri: Uri, data: Either[String, String])(implicit jsonReads: Reads[A]): Either[String, A] =
    deserializeEitherThrowable(uri, data).left.map(_.getMessage)

  def deserializeEitherThrowable[A](uri: Uri, data: Either[String, String])(implicit jsonReads: Reads[A]): Either[Throwable, A] =
    data.fold(
      error =>
        Left(UnsuccessfulResponseException(uri, error)),
      data =>
        if(data.isEmpty) {
          Left(EmptyBodyException("data is empty"))
        } else {
          Json.parse(data).validate[A] match {
            case s: JsSuccess[A] => Right(s.get)
            case jsError: JsError => Left(JsonParseException(jsError.toString, jsError, data))
          }
        }
    )

  def asThrowable[A](uri: Uri = uri"")(implicit jsonReads: Reads[A]) =
    asString.map(data => deserializeEitherThrowable[A](uri, data))

  def as[A](uri: Uri = uri"")(implicit jsonReads: Reads[A]) =
    asString.map(data => deserializeEither[A](uri, data))
}
