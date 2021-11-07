package org.burbokop.magura.generators

import org.burbokop.magura.generators.Generator.Options
import org.burbokop.magura.generators.cmake.CMakeBuilder.CMakeOptions
import org.burbokop.magura.models.meta.RepositoryMetaData
import org.burbokop.magura.utils.OptionsType
import org.burbokop.magura.utils.ReflectUtils._
import play.api.libs.json.{Format, JsNull, JsObject, JsResult, JsString, JsValue, Json, Reads, Writes}

import java.io.File
import scala.collection.mutable
import scala.reflect.runtime
import scala.util.{Failure, Success, Try}

object Generator {
  case class Error(message: String) extends Exception(message)

  abstract class Options {
    def hashName(): String
  }

  object Options {
    val writes = new Writes[Options] {
      override def writes(options: Options): JsValue = {
        val clazz = runtime.currentMirror.instanceType(options).toString
        val data = runtime.currentMirror.invokeAttachedMethod[Options, JsValue](clazz, options).getOrElse(JsNull)
        JsObject(
          Map("class" -> JsString(clazz))
            ++ (if(data == JsNull) Map() else Map("data" -> data))
        )
      }
    }

    val reads = new Reads[Options] {
      override def reads(value: JsValue): JsResult[Options] =
        JsResult.fromTry(
          value.validate[JsObject].map(obj =>
            obj.value.get("class").map(clazz =>
              runtime.currentMirror.invokeAttachedMethod[JsValue, Options](clazz.as[String], obj.value.getOrElse("data", JsNull))
                .map(Right(_))
                .getOrElse(Left(Generator.Error(s"Deserialization function not registered for: $clazz")))
            )
              .getOrElse(Left(Generator.Error(s"'class' field not found")))
          )
            .getOrElse(Left(Generator.Error(s"Json value must be an object")))
            .toTry
        )
    }

    implicit val jsonFormat = Format[Options](reads, writes)
  }

  @OptionsType(ser = DefaultOptions.ser, des = DefaultOptions.des)
  case class DefaultOptions() extends Options {
    override def hashName(): String = "default"
  }

  object DefaultOptions {
    def des(value: JsValue): Options = DefaultOptions()
    def ser(options: Options): JsValue = JsNull
  }

  def repositoryName(inputPath: String): String =
    new File(inputPath).getParentFile.getName
}

abstract class Generator {
  def proceed(
               cache: List[RepositoryMetaData],
               inputPath: String,
               outputPath: String,
               options: Options,
               maguraFile: MaguraFile,
             ): Either[Throwable, Boolean]

}

