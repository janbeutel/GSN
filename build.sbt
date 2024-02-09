lazy val commonSettings = Seq(
  organization := "ch.epfl.gsn",
  version := "2.0.4",
  scalaVersion := "2.12.18",
  // sbt 1.4.0 and up need scala-xml 2.1.0 and a newer play version
  Compile / compile / javacOptions ++= Seq("-source", "11", "-target", "11"),
  resolvers ++= Seq(
    DefaultMavenRepository,
    "Typesafe Repository" at "https://repo.maven.apache.org/maven2/",
    "osgeo" at "https://repo.osgeo.org/repository/release/",
    "play-authenticate (release)" at "https://oss.sonatype.org/content/repositories/releases/",
    "play-authenticate (snapshot)" at "https://oss.sonatype.org/content/repositories/snapshots/",
    "Local ivy Repository" at ""+Path.userHome.asFile.toURI.toURL+"/.ivy2/local",
    "Local cache" at ""+file(".").toURI.toURL+"lib/cache"
  ),

  Compile / publishArtifact := false,
  Compile / packageBin / publishArtifact := true,
  Compile / packageSrc / publishArtifact := true,
  Compile / packageDoc / publishArtifact := false,
  Test / publishArtifact := false,
  pomIncludeRepository := { x => false },
  pomExtra :=
  <url>http://gsn.epfl.ch</url>
  <licenses>
    <license>
      <name>GPL-3.0+</name>
      <url>https://opensource.org/licenses/GPL-3.0</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:LSIR/gsn.git</url>
    <connection>scm:git:git@github.com:LSIR/gsn.git</connection>
  </scm>
  <developers>
    <developer>
      <id>EPFL-LSIR</id>
      <name>The GSN Team</name>
      <url>http://gsn.epfl.ch</url>
    </developer>
  </developers>,
  crossPaths := false,
//  useGpg := true,
  Test / parallelExecution := false,
  EclipseKeys.createSrc := EclipseCreateSrc.Default
)

usePgpKeyHex("DC900B5F")

lazy val gsn2 = (project in file(".")).
  aggregate(core, tools, services)


lazy val core = (project in file("gsn-core")).
  dependsOn(tools).
  settings(commonSettings: _*).
  enablePlugins(JavaServerAppPackaging, DebianPlugin, SystemdPlugin)

//lazy val extra = (project in file("gsn-extra")).
//  dependsOn(core).
//  settings(commonSettings: _*)

lazy val services = (project in file("gsn-services")).
  dependsOn(tools, core).
  settings(
    coverageEnabled := true,
    coverageExcludedPackages := "<empty>;views.*;router.*;models.gsn.data"
  ).
  settings(commonSettings: _*).
  enablePlugins(PlayJava, PlayEbean, DebianPlugin, SystemdPlugin)

lazy val tools = (project in file("gsn-tools")).
  settings(commonSettings: _*)

lazy val webui = (project in file("gsn-webui")).
  enablePlugins(JavaServerAppPackaging, DebianPlugin)

// lazy val startAll = taskKey[Unit]("Start all the GSN modules")


//startAll := {
  //(webui/startDjango in webui).value
//  (re-start in core).value
//  (run in services).value
//}

