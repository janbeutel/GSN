import com.typesafe.sbt.packager.linux._ 

enablePlugins(JavaServerAppPackaging, SystemdPlugin)

// --- MIGRATED GLOBAL SETTINGS ---
organization := "ch.epfl.gsn"
name := "gsn-core"
version := "2.0.4"
scalaVersion := "2.12.18" // Crucial for Java 17 compatibility

Compile / javacOptions ++= Seq("-source", "17", "-target", "17")
scalacOptions += "-deprecation"
EclipseKeys.projectFlavor := EclipseProjectFlavor.Java

resolvers ++= Seq(
  DefaultMavenRepository,
  "Typesafe Repository" at "https://repo.maven.apache.org/maven2/",
  "osgeo" at "https://repo.osgeo.org/repository/release/",
  "play-authenticate (release)" at "https://oss.sonatype.org/content/repositories/releases/",
  "play-authenticate (snapshot)" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Local ivy Repository" at ""+Path.userHome.asFile.toURI.toURL+"/.ivy2/local",
  "Local cache" at ""+file(".").toURI.toURL+"lib/cache"
)
// --------------------------------

// --- DEPENDENCIES ---
libraryDependencies ++= Seq(
  "org.projectlombok" % "lombok" % "1.18.44" % Provided,
  "com.h2database" % "h2" % "1.4.195",
  "com.mchange" % "c3p0" % "0.9.5-pre10",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % "2.17.0",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.17.0",
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-xml" % "2.17.0",
  "com.vividsolutions" % "jts-core" % "1.14.0",
  "mysql" % "mysql-connector-java" % "8.0.28",
  "org.postgresql" % "postgresql" % "42.3.0",
  "org.apache.commons" % "commons-dbcp2" % "2.0",
  "org.hibernate" % "hibernate-core" % "3.6.10.Final",
  "org.apache.httpcomponents" % "httpclient" % "4.3.2",
  "org.apache.commons" % "commons-email" % "1.3.2",
  "commons-collections" % "commons-collections" % "3.2.1",
  "commons-io" % "commons-io" % "2.4",
  "org.apache.logging.log4j" % "log4j-api" % "2.3",
  "org.apache.logging.log4j" % "log4j-core" % "2.3",
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.3",
  "org.apache.logging.log4j" % "log4j-web" % "2.3",
  "net.sf.opencsv" % "opencsv" % "2.3",
  "com.thoughtworks.xstream" % "xstream" % "1.4.5",
  "org.antlr" % "stringtemplate" % "3.0",
  "commons-lang" % "commons-lang" % "2.6",
  "rome" % "rome" % "1.0",
  "org.jfree" % "jfreechart" % "1.0.19", 
  "org.jfree" % "jcommon" % "1.0.23",
  "org.codehaus.groovy" % "groovy-all" % "2.2.2",
  "net.rforge" % "REngine" % "0.6-8.1",
  "net.rforge" % "Rserve" % "0.6-8.1",
  "org.rxtx" % "rxtx" % "2.1.7",
  "com.esotericsoftware.kryo" % "kryo" % "2.23.0",
  "org.zeromq" % "jeromq" % "0.3.5",
  "org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "1.1.0",
  "org.eclipse.californium" % "californium-core" % "1.0.4",
  "junit" % "junit" % "4.11" %  Test,
  "org.easymock" % "easymockclassextension" % "3.2" % Test,
  "commons-fileupload" % "commons-fileupload" % "1.3.3",
  "javax.servlet" % "javax.servlet-api" % "3.0.1" % Provided,
  "com.jfinal" % "cos" % "2022.2",
  "org.eclipse.jetty" % "jetty-continuation" % "9.4.43.v20210629",
  "org.eclipse.jetty" % "jetty-io" % "9.4.43.v20210629",
  "org.jibx" % "jibx-run" % "1.3.1",
  "org.httpunit" % "httpunit" % "1.7.2" % Test exclude("xerces","xercesImpl") exclude("xerces","xmlParserAPIs") exclude("javax.servlet","servlet-api")
)

Compile / unmanagedJars += file("lib/tinyos-2.x.jar")
Compile / unmanagedJars += file("lib/tinyos-1.x-gsn-src-bin.jar")
Compile / unmanagedJars += file("lib/jai_codec-1.1.3.jar")
Compile / unmanagedJars += file("lib/jai_core-1.1.3.jar")


mainClass := Some("ch.epfl.gsn.Main")

// --- NATIVE PACKAGER ---
Linux / packageSummary := "GSN Server"
Windows / packageSummary := "GSN Server"
packageDescription := "Global Sensor Networks Core"
Windows / maintainer := "LSIR EPFL <gsn@epfl.ch>"
Linux / maintainer := "LSIR EPFL <gsn@epfl.ch>"

Debian / debianPackageDependencies += "java17-runtime"
Debian / debianPackageRecommends ++= Seq("postgresql", "munin-node", "gsn-services")
Linux / daemonUser := "gsn"

// --- MAPPINGS ---
Universal / mappings += (Compile / sourceDirectory).value / "templates" / "gsn-core" -> "bin/gsn-core"
Universal / mappings += (Compile / sourceDirectory).value / "main" / "resources" / "log4j2.xml" -> "conf/log4j2.xml"
Universal / mappings += baseDirectory.value / ".." / "conf" / "gsn.xml" -> "conf/gsn.xml"
Universal / mappings += (Compile / sourceDirectory).value / "main" / "resources" / "wrappers.properties" -> "conf/wrappers.properties"

Debian / linuxPackageMappings += packageMapping(
  (baseDirectory.value / ".." / "virtual-sensors" / "packaged") -> "/usr/share/gsn-core/conf/virtual-sensors"
) withUser "gsn" withGroup "root" withPerms "0775" withContents()

Universal / mappings ++= {
  val samplesDir = baseDirectory.value / ".." / "virtual-sensors" / "samples"
  // Get all files, filter out directories, and map them to the new folder
  samplesDir.allPaths.get.filter(_.isFile).map { file =>
    file -> s"virtual-sensors-samples/${file.getName}"
  }
}

Linux / linuxPackageMappings := {
    val mappings = (Linux / linuxPackageMappings).value
    mappings map { 
        case linuxPackage if linuxPackage.fileData.config equals "true" =>
            val newFileData = linuxPackage.fileData.copy(user = "gsn")
            linuxPackage.copy(fileData = newFileData)
        case linuxPackage => linuxPackage
    }
}

// --- REVOLVER ---
reStart / mainClass := Some("ch.epfl.gsn.Main")
reStart / reStartArgs := Seq("../conf", "../virtual-sensors")