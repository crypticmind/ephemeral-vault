package ar.com.crypticmind.ephemeralvault

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import ar.com.crypticmind.ephemeralvault.misc.{H2Database, DatabaseComponent, EnvironmentComponent}
import ar.com.crypticmind.ephemeralvault.repositories._
import ar.com.crypticmind.ephemeralvault.routes._
import ar.com.crypticmind.ephemeralvault.services._
import spray.can.Http
import spray.routing._

import scala.concurrent.duration._

object Main extends App {

  implicit val system = ActorSystem("ephemeral-vault")
  implicit val timeout = Timeout(5.seconds)

  class ServiceActor
    extends HttpServiceActor
    with EnvironmentComponent
    with OneTimeTokenRoutes
    with OneTimeTokenServiceComponent with DefaultOneTimeTokenService
    with OneTimeTokenRepositoryComponent with DatabaseOneTimeTokenRepository
    with DatabaseComponent with H2Database {
    implicit val system = context.system
    val configuration = standardConfiguration
    def receive = runRoute(routes)
  }

  val service = system.actorOf(Props[ServiceActor], "service")

  IO(Http) ? Http.Bind(service, interface = "localhost", port = 8080)
}
