name := "gsn-tools"

libraryDependencies ++= Seq(
//  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
//  "org.scala-lang.modules" %% "scala-xml" % "1.0.5",
  "org.scala-lang.modules" %% "scala-xml" % "1.3.0",
//  "org.scala-lang.modules" %% "scala-xml" % "2.1.0",
//  "com.typesafe.play" %% "play-json" % "2.6.0",
  "com.typesafe.play" %% "play-json" % "2.9.4",
//  "com.typesafe.akka" %% "akka-actor" % "2.5.3",
//  "com.typesafe.akka" %% "akka-actor" % "2.6.20",
  "edu.ucar" % "netcdf" % "4.3.22",
  "org.geotools" % "gt-shapefile" % "13.2",
  "org.geotools" % "gt-geojson" % "13.2",
  "org.geotools" % "gt-epsg-hsql" % "13.2",
  "javax.media" % "jai_core" % "1.1.3",
  "org.apache.jena" % "jena-core" % "2.11.2" exclude("log4j","log4j") exclude("org.slf4j","slf4j-log4j12"),
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "com.mchange" % "c3p0" % "0.9.5-pre10",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.12.4",
  "ch.qos.logback" % "logback-classic" % "1.1.1" % "test",
  "org.scalatest" %% "scalatest" % "3.2.9" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.4.1" % "test",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.3" % "test",
//  "org.mindrot" % "jbcrypt" % "0.3m"
)

scalacOptions += "-deprecation"
