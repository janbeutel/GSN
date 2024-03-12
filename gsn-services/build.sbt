//import com.typesafe.sbt.packager.archetypes.ServerLoader

name := "gsn-services"


val buildSettings = Seq(
   javaOptions += "-Xmx128m",
   javaOptions += "-Xms64m"
)

sources in (Compile,doc) := Seq.empty

libraryDependencies ++= Seq(
  "be.objectify"  %% "deadbolt-java"     % "2.6.4",
  "be.objectify"  %% "deadbolt-scala"     % "2.6.0",
  "be.objectify" %% "deadbolt-java-gs" % "2.6.0",
  // Comment the next line for local development of the Play Authentication core:
  // Use the latest release version when copying this code, e.g. "0.9.0"
  "com.feth"      %% "play-authenticate" % "0.9.0",
  "org.postgresql" % "postgresql" % "42.3.0",
  "com.h2database" % "h2" % "1.4.195",
  cacheApi,
  ehcache,
  "com.google.inject" % "guice" % "4.2.1",
  evolutions,
  javaWs,
  javaJdbc,
  "org.webjars" % "bootstrap" % "3.2.0",
  "org.easytesting" % "fest-assert" % "1.4" % "test",
  "org.fluentlenium" % "fluentlenium-core" % "3.6.1" % "test",
  "org.seleniumhq.selenium" % "selenium-java" % "3.14.0" % "test",
  "org.seleniumhq.selenium" % "selenium-api" % "3.14.0" % "test",
  "org.seleniumhq.selenium" % "selenium-firefox-driver" % "3.14.0" % "test",
  "org.seleniumhq.selenium" % "selenium-support" % "3.14.0" % "test",
  "javax.xml.bind" % "jaxb-api" % "2.3.1",
  "org.glassfish.jaxb" % "jaxb-runtime" % "2.3.1",
  "com.esotericsoftware.kryo" % "kryo" % "2.23.0",
  "com.nulab-inc" %% "scala-oauth2-core" % "1.3.0",
  "com.nulab-inc" %% "play2-oauth2-provider" % "1.3.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.3" % "test",
  "org.seleniumhq.selenium" % "selenium-java" % "3.141.59" % "test"
  //"ch.epfl.gsn" % "gsn-core" % "2.0.3" exclude("org.apache.logging.log4j", "log4j-slf4j-impl") exclude("org.scala-lang.modules", "scala-xml_2.11")
)

//libraryDependencies := libraryDependencies.value.map(_.exclude("ch.qos.logback", "logback-classic").exclude("ch.qos.logback", "logback-core"))

excludeDependencies ++= Seq(
  SbtExclusionRule("org.apache.logging.log4j", "log4j-slf4j-impl"),
  SbtExclusionRule("org.hibernate", "hibernate-core")
)

NativePackagerKeys.packageSummary in com.typesafe.sbt.SbtNativePackager.Linux := "GSN Services"

NativePackagerKeys.packageDescription := "Global Sensor Networks Services"

NativePackagerKeys.maintainer in com.typesafe.sbt.SbtNativePackager.Linux := "LSIR EPFL <gsn@epfl.ch>"

debianPackageDependencies in Debian += "java11-runtime"

debianPackageRecommends in Debian ++= Seq("postgresql", "gsn-core", "nginx")

serverLoading in Debian := Some(ServerLoader.Systemd)

enablePlugins(DebianPlugin)

enablePlugins(SystemdPlugin)

daemonUser in Linux := "gsn"

javaOptions in Test += "-Dconfig.file=conf/test.conf"
