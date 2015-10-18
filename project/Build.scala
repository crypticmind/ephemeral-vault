import sbt.Keys._
import sbt._
import spray.revolver.RevolverPlugin.Revolver

object Build extends Build {

  lazy val main =
    Project("ephemeral-vault", file("."))
      .settings(
        organization := "ar.com.crypticmind",
        version := "0.1-SNAPSHOT",
        scalaVersion := "2.11.6",
        scalacOptions := Seq(
          "-unchecked",
          "-deprecation",
          "-encoding",
          "utf8"
        )
      )
      .settings(Revolver.settings)
      .settings(
        libraryDependencies ++= {
          val akkaV = "2.3.9"
          val sprayV = "1.3.3"
          Seq(
            "com.typesafe"            %   "config"                    % "1.2.1",
            "io.spray"                %%  "spray-can"                 % sprayV,
            "io.spray"                %%  "spray-routing-shapeless2"  % sprayV,
            "io.spray"                %%  "spray-json"                % "1.3.2",
            "com.typesafe.akka"       %%  "akka-actor"                % akkaV,
            "com.typesafe.play"       %%  "anorm"                     % "2.4.0",
            "com.zaxxer"              %   "HikariCP"                  % "2.4.1",
            "com.h2database"          %   "h2"                        % "1.3.176",
            "org.scala-lang.modules"  %%  "scala-parser-combinators"  % "1.0.3",
            "io.spray"                %%  "spray-testkit"             % sprayV    % "test",
            "com.typesafe.akka"       %%  "akka-testkit"              % akkaV     % "test"
          )
        }
      )

}
