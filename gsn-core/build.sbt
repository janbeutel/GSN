import NativePackagerHelper._

import com.typesafe.sbt.packager.archetypes.systemloader.ServerLoader

import com.typesafe.sbt.packager.archetypes.TemplateWriter

import com.typesafe.sbt.packager.linux._

name := "gsn-core"

Revolver.settings

libraryDependencies ++= Seq(
  //"com.typesafe" % "config" % "1.2.1",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.5",
  "com.h2database" % "h2" % "1.4.195",
  "com.typesafe.play" %% "play" % "2.6.0",
  //"mysql" % "mysql-connector-java" % "5.1.29",
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
  "junit" % "junit" % "4.11" %  "test",
  //"ch.epfl.gsn" % "gsn-tools" % "2.0.3",
  "org.easymock" % "easymockclassextension" % "3.2" % "test",
  "commons-fileupload" % "commons-fileupload" % "1.3.3",
  "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided",
  "com.jfinal" % "cos" % "2022.2",
  "org.eclipse.jetty" % "jetty-continuation" % "9.4.43.v20210629",
  "org.eclipse.jetty" % "jetty-io" % "9.4.43.v20210629",
  "org.jibx" % "jibx-run" % "1.3.1",
  "javax.media" % "jai_codec" % "1.1.3",
  "org.httpunit" % "httpunit" % "1.7.2" % "test" exclude("xerces","xercesImpl") exclude("xerces","xmlParserAPIs") exclude("javax.servlet","servlet-api")
)


mainClass := Some("ch.epfl.gsn.Main")

NativePackagerKeys.packageSummary in com.typesafe.sbt.SbtNativePackager.Linux := "GSN Server"

NativePackagerKeys.packageSummary in com.typesafe.sbt.SbtNativePackager.Windows := "GSN Server"

NativePackagerKeys.packageDescription := "Global Sensor Networks Core"

NativePackagerKeys.maintainer in com.typesafe.sbt.SbtNativePackager.Windows := "LSIR EPFL <gsn@epfl.ch>"

NativePackagerKeys.maintainer in com.typesafe.sbt.SbtNativePackager.Linux := "LSIR EPFL <gsn@epfl.ch>"

debianPackageDependencies in Debian += "java11-runtime"

debianPackageRecommends in Debian ++= Seq("postgresql", "munin-node", "gsn-services")

serverLoading in Debian := Some(ServerLoader.Systemd)

daemonUser in Linux := "gsn"

mappings in Universal += (sourceDirectory.value / "templates" / "gsn-core") -> "bin/gsn-core"

mappings in Universal += (sourceDirectory.value / "main" / "resources" / "log4j2.xml") -> "conf/log4j2.xml"

mappings in Universal += (baseDirectory.value / ".." / "conf" / "gsn.xml") -> "conf/gsn.xml"

mappings in Universal += (sourceDirectory.value / "main" / "resources" / "wrappers.properties") -> "conf/wrappers.properties"

linuxPackageMappings in Debian += packageMapping(
  (baseDirectory.value / ".." / "virtual-sensors" / "packaged") -> "/usr/share/gsn-core/conf/virtual-sensors"
) withUser "gsn" withGroup "root" withPerms "0775" withContents()

mappings in Universal ++= ((baseDirectory.value / ".." / "virtual-sensors" / "samples").***).pair(file => Some("virtual-sensors-samples/" + file.name))


linuxPackageMappings := {
    val mappings = linuxPackageMappings.value
    mappings map { 
        case linuxPackage if linuxPackage.fileData.config equals "true" =>
            val newFileData = linuxPackage.fileData.copy(
                user = "gsn"
            )
            linuxPackage.copy(
                fileData = newFileData
            )
        case linuxPackage => linuxPackage
    }
}
enablePlugins(SystemdPlugin)

scalacOptions += "-deprecation"

EclipseKeys.projectFlavor := EclipseProjectFlavor.Java

mainClass in Revolver.reStart := Some("ch.epfl.gsn.Main")

unmanagedJars in Compile += file("lib/tinyos-2.x.jar")
unmanagedJars in Compile += file("lib/tinyos-1.x-gsn-src-bin.jar")

Revolver.reStartArgs := Seq("../conf", "../virtual-sensors")

