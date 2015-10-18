package ar.com.crypticmind.ephemeralvault.misc

import spray.routing._

trait Routes extends Directives {
  def routes: Route = reject
}
