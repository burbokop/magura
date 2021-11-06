package org.burbokop.utils.java

import org.burbokop.generators.Generator.Options
import play.api.libs.json.JsValue

import scala.annotation.{Annotation, StaticAnnotation}
import scala.reflect.runtime.universe

case class OptionsType(
                        ser: Options => JsValue,
                        des: JsValue => Options
                      ) extends Annotation

object OptionsType {
  def annotations(`class`: String) = {
    universe.rootMirror.staticClass(`class`).annotations
  }

  def serialize(`class`: String, options: Options) =
    findAnnotationMethod(`class`, universe.typeOf[Options], universe.typeOf[JsValue]).map(method => {
      universe.rootMirror.reflect()
      universe.rootMirror.reflectMethod(method).apply(options)
    })

  def deserialize(`class`: String, value: JsValue) =
    findAnnotationMethod(`class`, universe.typeOf[JsValue], universe.typeOf[Options]).map(method => {
      universe.rootMirror.reflect().reflectMethod(method).apply(value)
    })

  def findAnnotationMethod(`class`: String, argType: universe.Type, resType: universe.Type) = {
    var result: Option[universe.MethodSymbol] = None
    annotations(`class`).find(annotation => {
      annotation.tree.children.find(child => {
        child.children.find(childOfChild => {
          if(Option(childOfChild.symbol).exists(_.isMethod)) {
            val method = childOfChild.symbol.asMethod
            if(method.paramLists.exists(paramList => paramList.length == 1 && paramList.head.typeSignature <:< argType) && method.typeSignature.resultType <:< resType) {
              result = Some(method)
              true
            } else false
          } else false
        })
        result.isDefined
      })
      result.isDefined
    })
    result
  }

}