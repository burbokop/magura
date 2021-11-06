package org.burbokop.utils.java

import org.burbokop.generators.Generator.Options
import play.api.libs.json.JsValue

import scala.annotation.{Annotation, StaticAnnotation}

case class OptionsType(
                        ser: Options => JsValue,
                        des: JsValue => Options
                      ) extends Annotation
