import com.typesafe.sbt.packager.linux.LinuxPlugin.autoImport.packageMapping
import com.typesafe.sbt.packager.linux.LinuxSymlink

name := "magura"

version := "0.1.2"

maintainer := "Borys Boiko <burbokop@gmail.com>"
packageSummary := "Package for downloading dependencies from github"
packageDescription := "Description"

scalaVersion := "2.13.5"

enablePlugins(DebianPlugin)
debianPackageDependencies := Seq(/*"cmake",*/ "java8-runtime-headless")

mainClass in assembly := Some("org.burbokop.Main")

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "maguraApp"
  )



linuxPackageMappings := {
  def getRepositoryReleaseTags(user: String, repo: String): Either[String, List[String]] = {
    import play.api.libs.json.{JsError, JsObject, JsSuccess, Json}
    import sttp.client3.{HttpURLConnectionBackend, UriContext, asString, basicRequest}
    basicRequest
      .header("Accept", "application/vnd.github.v3+json")
      .get(uri"https://api.github.com/repos/$user/$repo/releases")
      .response(asString).send(HttpURLConnectionBackend())
      .body.fold[Either[String, List[String]]](Left(_), data =>
      Json.parse(data).validate[List[JsObject]] match {
        case JsError(errors) => Left(errors.toString)
        case JsSuccess(value, _) => Right(value.map(_.value("tag_name").toString))
      }
    )
  }

  val currentReleaseTag = s"v${version.value.toString}"
  val needRelease = getRepositoryReleaseTags("burbokop", "magura")
    .fold(_ => false, _.find(_ == currentReleaseTag).isEmpty)

  System.setProperty("PROP0", needRelease.toString)


  import java.io.{File, FileOutputStream}
  val jar = (assemblyOutputPath in assembly).value.file
  val bin = new File(s"${target.value.getPath}${File.separator}${jar.name}.sh")
  val releaseInfo = new File(s"${target.value.getPath}${File.separator}release.info")
  new FileOutputStream(bin.getPath).write(
    s"""|#!/bin/sh
        |java -jar /usr/share/magura/lib/${jar.name} $$@
     """.stripMargin.toArray.map(_.toByte))

  new FileOutputStream(releaseInfo.getPath).write(
    s"""|tag_name = $currentReleaseTag
        |need_release = $needRelease
        |deb = ./target/${packageName.value}_${version.value}_all.deb
     """.stripMargin.toArray.map(_.toByte))

  val cmakeConfig = sourceDirectory.value / "main" / "resources" / "magura-config.cmake"
  Seq(
    packageMapping((jar, s"/usr/share/magura/lib/${jar.name}")),
    packageMapping((bin, s"/usr/share/magura/bin/magura")),
    packageMapping((cmakeConfig, s"/opt/magura/magura-config.cmake"))
  )
}

linuxPackageSymlinks += LinuxSymlink("/usr/bin/magura" ,"/usr/share/magura/bin/magura")


resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies += "com.softwaremill.sttp.client3" %% "core" % "3.1.7"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.2"
libraryDependencies ++= Seq("org.yaml" % "snakeyaml" % "1.16")

libraryDependencies ++= Seq(
  "com.concurrentthought.cla" %% "command-line-arguments"          % "0.6.0",
  "com.concurrentthought.cla" %% "command-line-arguments-examples" % "0.6.0"
)