package org.burbokop.utils

import org.burbokop.generators.cmake.CMakeBuilder.CMakeOptions

import scala.reflect.ClassTag
import scala.reflect.runtime.universe

object ReflectUtils {
  def instanceType[T: ClassTag](instance: T) =
    universe.rootMirror.reflect(instance).symbol.toType

  def annotations(`class`: String) = {
    universe.rootMirror.staticClass(`class`).annotations
  }

  def invokeAttachedMethod[A, R](`class`: String, arg: A)(implicit aTag: universe.TypeTag[A], rTag: universe.TypeTag[R]) =
    findAnnotationMethod(`class`, universe.typeOf[A], universe.typeOf[R]).flatMap(method => {
      getModuleFromMethod(method).map(module => {
        universe.rootMirror.reflect(universe.rootMirror.reflectModule(module.symbol.asModule).instance)
          .reflectMethod(method.symbol.asMethod)(arg).asInstanceOf[R]

      })
    })

  def getModuleFromMethod(method: universe.Tree) = {
    var result: Option[universe.Tree] = None
    method.children.exists(child => {
      child.children.exists(childOfChild => {
        if (childOfChild.symbol.isModule) {
          result = Some(childOfChild)
          true
        } else false
      })
    })
    result
  }

  def findAnnotationMethod(`class`: String, argType: universe.Type, resType: universe.Type) = {
    var result: Option[universe.Tree] = None
    annotations(`class`).find(annotation => {
      annotation.tree.children.find(child => {
        child.children.find(childOfChild => {
          if(Option(childOfChild.symbol).exists(_.isMethod)) {
            val method = childOfChild.symbol.asMethod
            if(method.paramLists.exists(paramList => paramList.length == 1 && paramList.head.typeSignature <:< argType) && method.typeSignature.resultType <:< resType) {
              result = Some(childOfChild)
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
