package org.burbokop.magura.utils

import io.github.burbokop.magura.utils.JsonUtils
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json, Reads}
import sttp.client3.{UriContext, asString}
import sttp.model.Uri

object SttpUtils {
  def asThrowable[A](uri: Uri = uri"")(implicit jsonReads: Reads[A]) =
    asString.map(data => JsonUtils.deserializeEitherThrowable[A](uri.toString, data))

  def as[A](uri: Uri = uri"")(implicit jsonReads: Reads[A]) =
    asString.map(data => JsonUtils.deserializeEither[A](uri.toString, data))
}
