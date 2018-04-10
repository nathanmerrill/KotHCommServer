name := """kothcommserver"""
organization := "com.nathanm"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, PlayEbean)

scalaVersion := "2.12.4"

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies += guice
libraryDependencies += jdbc
libraryDependencies += evolutions

libraryDependencies += "com.h2database" % "h2" % "1.4.196"
libraryDependencies += "com.github.dakatsuka" %% "akka-http-oauth2-client" % "0.1.0"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.nathanm.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.nathanm.binders._"
