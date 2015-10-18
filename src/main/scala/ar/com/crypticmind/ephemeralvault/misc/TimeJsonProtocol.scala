package ar.com.crypticmind.ephemeralvault.misc


import java.time.{ZonedDateTime, Duration}

import spray.json._

import scala.reflect.ClassTag

object TimeJsonProtocol extends DefaultJsonProtocol {

  private def requireString[T : ClassTag](v: JsValue): String = v match {
    case JsString(string) => string
    case _ => deserializationError(s"Expected JSON string to parse as ${implicitly[ClassTag[T]].runtimeClass.getSimpleName}")
  }

  implicit val durationParser: String => Duration = Duration.parse
  implicit val zonedDateTimeParser: String => ZonedDateTime = ZonedDateTime.parse

  implicit def timeJF[T : ClassTag](implicit parser: String => T) = new RootJsonFormat[T] {
    override def read(json: JsValue): T = parser(requireString[T](json))
    override def write(obj: T): JsValue = JsString(obj.toString)
  }
}
