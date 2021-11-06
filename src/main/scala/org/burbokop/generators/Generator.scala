package org.burbokop.generators

import org.burbokop.generators.Generator.Options
import org.burbokop.models.meta.RepositoryMetaData
import play.api.libs.json.{Format, JsNull, JsObject, JsResult, JsString, JsValue, Json, Reads, Writes}

import java.io.File
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

object Generator {
  case class Error(message: String) extends Exception(message)

  abstract class Options {
    def hashName(): String
  }

  object Options {
    private var registry: Map[Class[_], (JsValue => Options, Options => JsValue)] = Map()

    val writes = new Writes[Options] {
      override def writes(options: Options): JsValue =
        JsObject(Map(
          "class" -> JsString(options.getClass.toString),
          "data" -> registry.get(options.getClass).map(_._2(options)).getOrElse(JsNull)
        ))
    }

    val reads = new Reads[Options] {
      override def reads(value: JsValue): JsResult[Options] =
        value.validate[JsObject].flatMap(obj => JsResult.fromTry(obj.value.get("class").flatMap(clazz => {
          obj.value.get("data").flatMap(data => registry.get(Class.forName(clazz.as[String])).map(_._1(data)))
        }).map(Success(_)).getOrElse(Failure(new Exception("AAAA")))))
    }

    implicit val jsonFormat = Format[Options](reads, writes)


    private val registry1 = mutable.HashMap.empty[Class[T] forSome {type T <: Options}, (JsValue => Options, Options => JsValue)]

    val a = (() => {
      println(s"registry1: $registry1")
    })()
    println(s"a: $a")

    def register(clazz: Class[_], read: JsValue => Options, write: Options => JsValue) = {
      registry = registry + (clazz -> (read, write))
      println(s"registry: $registry")
    }

  }

  case class DefaultOptions() extends Options {
    override def hashName(): String = "default"
  }

  object DefaultOptions {
    Options.register(classOf[DefaultOptions], _ => DefaultOptions(), _ => JsNull)
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

