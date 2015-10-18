package ar.com.crypticmind.ephemeralvault.model.api


import java.time.{Duration, ZonedDateTime}

import ar.com.crypticmind.ephemeralvault.misc.SnakifiedSprayJsonSupport
import spray.json.JsObject

object OneTimeTokenAPI extends SnakifiedSprayJsonSupport {

  import ar.com.crypticmind.ephemeralvault.misc.TimeJsonProtocol._

  case class CreateRQ(data: JsObject,
                      validFor: Option[Duration],
                      validUntil: Option[ZonedDateTime])

  case class CreateRS(token: String)

  implicit val createRQJF = jsonFormat3(CreateRQ)
  implicit val createRSJF = jsonFormat1(CreateRS)
  
}
