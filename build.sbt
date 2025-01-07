val scalaVersion_3 = "3.3.3"

ThisBuild / organization := "com.github.tototoshi"
ThisBuild / version := "0.1.0"
ThisBuild / scalaVersion := scalaVersion_3

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "com.h2database" % "h2" % "2.2.224" % Test,
    "org.scalameta" %% "munit" % "1.0.4" % Test
  ),
  publishTo := _publishTo(version.value)
)

lazy val module = project
  .in(file("module"))
  .settings(
    commonSettings,
    publishingSettings,
    name := "nyanda",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.5.4"
    )
  )

lazy val example = project
  .in(file("example"))
  .settings(
    commonSettings,
    nonPublishSettings,
    name := "nyanda-example",
    libraryDependencies ++= Seq(
      "com.h2database" % "h2" % "2.2.224"
    ),
    run / fork := true
  )
  .dependsOn(module)

lazy val root = project
  .in(file("."))
  .settings(
    commonSettings,
    nonPublishSettings
  )
  .aggregate(module, example)

val publishingSettings = Seq(
  publishMavenStyle := true,
  Test / publishArtifact := false,
  pomExtra := _pomExtra
)

val nonPublishSettings = Seq(
  publishArtifact := false,
  publish := {},
  publishLocal := {},
  Test / parallelExecution := false
)

def _publishTo(v: String) = {
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

val _pomExtra =
  <url>https://github.com/tototoshi/nyanda</url>
  <licenses>
    <license>
      <name>Apache License, Version 2.0</name>
      <url>https://www.apache.org/licenses/LICENSE-2.0.html</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:tototoshi/nyanda.git</url>
    <connection>scm:git:git@github.com:tototoshi/nyanda.git</connection>
  </scm>
  <developers>
    <developer>
      <id>tototoshi</id>
      <name>Toshiyuki Takahashi</name>
      <url>https://tototoshi.github.io</url>
    </developer>
  </developers>
