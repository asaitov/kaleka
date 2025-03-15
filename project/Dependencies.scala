import sbt._

object Dependencies {

  lazy val pekkoVersion = "1.1.0"

  lazy val akkaLibs = Seq(
    "org.apache.pekko" %% "pekko-actor-typed" % pekkoVersion,
    "org.apache.pekko" %% "pekko-http" % pekkoVersion,
    "org.apache.pekko" %% "pekko-stream-typed" % pekkoVersion
  )

  lazy val logLibs = Seq(
    "ch.qos.logback" % "logback-classic" % "1.5.11"
  )

  lazy val configLibs = Seq(
    "com.typesafe" % "config" % "1.4.3"
  )

  lazy val parserLibs = Seq(
    "org.scala-lang.modules" %% "scala-parser-combinators" % "2.4.0"
  )

  lazy val testLibs = Seq(
    "org.apache.pekko" %% "pekko-actor-testkit-typed" % pekkoVersion % Test,
    "org.scalatest" %% "scalatest-funsuite" % "3.2.19" % Test
  )
}
