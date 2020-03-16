package be.reaktika.foodordering.api

import java.util.UUID

import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._

object Formats {

  implicit val mapReads: Reads[Map[UUID, Int]] = new Reads[Map[UUID, Int]] {
    def reads(jv: JsValue): JsResult[Map[UUID, Int]] =
      JsSuccess(jv.as[Map[String, String]].map{case (k, v) =>
        UUID.fromString(k) -> Integer.parseInt(v)
      })
  }

  implicit val mapWrites: Writes[Map[UUID, Int]] = new Writes[Map[UUID, Int]] {
    def writes(map: Map[UUID, Int]): JsValue =
      Json.obj(map.map{case (s, o) =>
        val ret: (String, JsValueWrapper) = s.toString -> JsNumber(o)
        ret
      }.toSeq:_*)
  }

  implicit val mapFormat: Format[Map[UUID, Int]] = Format(mapReads, mapWrites)

}
