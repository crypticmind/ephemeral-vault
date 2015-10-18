package ar.com.crypticmind.ephemeralvault.routes

import ar.com.crypticmind.ephemeralvault.misc.{EnvironmentComponent, Routes}
import ar.com.crypticmind.ephemeralvault.services.OneTimeTokenServiceComponent
import spray.http.ContentTypes._
import spray.http.HttpEntity
import spray.http.StatusCodes._

trait OneTimeTokenRoutes extends Routes { self: OneTimeTokenServiceComponent with EnvironmentComponent =>

  import ar.com.crypticmind.ephemeralvault.model.api.OneTimeTokenAPI._
  import spray.httpx.SprayJsonSupport._

  lazy val apiVersion = configuration.getString("ephemeral-vault.api-version")

  abstract override def routes =
    pathPrefix(apiVersion / "one-time-token") {
      path(Segment) { key =>
        put {
          entity(as[CreateRQ]) { rq =>
            complete(oneTimeTokenService.create(key, rq))
          }
        }
      } ~
      path(Segment / Segment) { (key, token) =>
        get {
          complete {
            oneTimeTokenService.get(key, token) match {
              case Some(Right(oneTimeToken)) => HttpEntity(`application/json`, oneTimeToken.data)
              case Some(Left(_)) => Gone
              case None => NotFound
            }
          }
        }
      }
    } ~ super.routes

}
