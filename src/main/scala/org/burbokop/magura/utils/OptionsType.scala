package org.burbokop.magura.utils

import org.burbokop.magura.generators.Generator.Options
import play.api.libs.json.JsValue

import scala.annotation.Annotation

case class OptionsType(
                        ser: Options => JsValue,
                        des: JsValue => Options
                      ) extends Annotation
