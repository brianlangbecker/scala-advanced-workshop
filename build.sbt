name := "play-pekko-otel-workshop"
version := "1.0-SNAPSHOT"
scalaVersion := "2.13.12"

// Resolve scala-xml version conflict
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % "always"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies ++= Seq(
      guice,
      
      // Apache Pekko (Akka fork)
      "org.apache.pekko" %% "pekko-actor-typed" % "1.0.2",
      "org.apache.pekko" %% "pekko-stream" % "1.0.2",
      
      //OTEL libraries 
      "io.opentelemetry" % "opentelemetry-api" % "1.54.0",
      "io.opentelemetry" % "opentelemetry-sdk" % "1.54.0",
      "io.opentelemetry" % "opentelemetry-exporter-otlp" % "1.54.0",
      "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % "1.54.0",
      "io.opentelemetry.instrumentation" % "opentelemetry-instrumentation-annotations" % "2.20.1",

      // Logback - use version compatible with Play 2.8
      "ch.qos.logback" % "logback-classic" % "1.2.11"
    ),
    fork := true,
  )