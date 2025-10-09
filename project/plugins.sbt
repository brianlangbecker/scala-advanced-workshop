// Fix scala-xml version conflict in the sbt build itself
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % "always"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.20")