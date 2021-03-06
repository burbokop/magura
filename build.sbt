import com.typesafe.sbt.packager.linux.LinuxPlugin.autoImport.packageMapping
import com.typesafe.sbt.packager.linux.LinuxSymlink

name := "magura"

version := "0.0.3-beta3"

maintainer := "Borys Boiko <burbokop@gmail.com>"
packageSummary := "Package for downloading dependencies from github"
packageDescription := "Description"

scalaVersion := "2.12.2"


enablePlugins(DebianPlugin)
debianPackageDependencies := Seq("cmake", "java9-runtime-headless")

mainClass in assembly := Some("org.burbokop.magura.Main")

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, target, packageName),
    buildInfoPackage := "maguraApp"
  )


linuxPackageMappings := {
  import java.io.{File, FileOutputStream}
  val jar = (assemblyOutputPath in assembly).value.file
  val bin = new File(s"${target.value.getPath}${File.separator}${jar.name}.sh")
  new FileOutputStream(bin.getPath).write(
    s"""|#!/bin/sh
        |java -jar /usr/share/magura/lib/${jar.name} $$@
     """.stripMargin.toArray.map(_.toByte))

  val cmakeConfig = sourceDirectory.value / "main" / "resources" / "magura-config.cmake"
  Seq(
    packageMapping((jar, s"/usr/share/magura/lib/${jar.name}")),
    packageMapping((bin, s"/usr/share/magura/bin/magura")),
    packageMapping((cmakeConfig, s"/opt/magura/magura-config.cmake"))
  )
}

linuxPackageSymlinks += LinuxSymlink("/usr/bin/magura" ,"/usr/share/magura/bin/magura")


val aaa = new File("fff") / "ddfdf"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value
libraryDependencies += "org.scala-lang" % "scala-library" % scalaVersion.value
libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value

libraryDependencies += "com.softwaremill.sttp.client3" %% "core" % "3.1.7"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.2"
libraryDependencies ++= Seq("org.yaml" % "snakeyaml" % "1.16")
//libraryDependencies += "io.github.burbokop" %% "magura_api" % "0.1.9"
libraryDependencies += "io.github.burbokop" %% "magura_api" % "0.1.11" from "file:///home/borys/projects/scala/magura_api/target/sonatype-staging/0.1.0-SNAPSHOT/io/github/burbokop/magura_api_2.12/0.1.10/magura_api_2.12-0.1.10.jar"


libraryDependencies ++= Seq(
  "com.concurrentthought.cla" %% "command-line-arguments"          % "0.6.0",
  "com.concurrentthought.cla" %% "command-line-arguments-examples" % "0.6.0"
)
