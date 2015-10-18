package ar.com.crypticmind.ephemeralvault.misc

import java.io.File

import com.typesafe.config.{ConfigFactory, Config}

trait EnvironmentComponent {

  def configuration: Config

  lazy val defaultConfig = ConfigFactory.load()

  lazy val environmentId = Option(System.getProperty("environment")).getOrElse("dev")

  def standardConfiguration = {
    val overrideFile = new File(System.getProperty("user.home") + File.separator + "environment-override.conf")
    val overrideConfig = ConfigFactory.parseFile(overrideFile)
    val conf = overrideConfig.withFallback(defaultConfig.getConfig(environmentId).withFallback(defaultConfig))
    conf
  }
}
