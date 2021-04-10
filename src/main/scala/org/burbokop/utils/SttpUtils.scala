package org.burbokop.utils

import play.api.libs.json.{JsError, JsSuccess, Json, Reads}
import sttp.client3.asString

object SttpUtils {
  final case class HttpStatusException(statusCode: Int, message: String) extends Exception(message)

  final case class EmptyBodyException(message: String) extends Exception(message)

  final case class UnsuccessfulResponseException(message: String) extends Exception(message)

  final case class JsonParseException(message: String, jsError: JsError, body: String) extends Exception(message)

  def deserializeEither[A](data: Either[String, String])(implicit jsonReads: Reads[A]): Either[String, A] =
    deserializeEitherThrowable(data).left.map(_.getMessage)

  def deserializeEitherThrowable[A](data: Either[String, String])(implicit jsonReads: Reads[A]): Either[Throwable, A] =
    data.fold(
      error =>
        Left(UnsuccessfulResponseException(error)),
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

  def asThrowable[A](implicit jsonReads: Reads[A]) = asString.map(deserializeEitherThrowable[A])

  def as[A](implicit jsonReads: Reads[A]) = asString.map(deserializeEither[A])
}
