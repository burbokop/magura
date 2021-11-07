package org.burbokop.utils.java

import org.burbokop.generators.Generator.Options
import org.burbokop.utils.ReflectUtils
import play.api.libs.json.JsValue

import scala.annotation.Annotation
import scala.reflect.runtime.universe

case class OptionsType(
                        ser: Options => JsValue,
                        des: JsValue => Options
                      ) extends Annotation

object OptionsType {

  def serialize(`class`: String, options: Options) =
    ReflectUtils.invokeAttachedMethod[Options, JsValue](`class`, options)

  def deserialize(`class`: String, value: JsValue) =
    ReflectUtils.invokeAttachedMethod[JsValue, Options](`class`, value)


}