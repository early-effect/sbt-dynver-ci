val scala3Version   = "3.8.4"
val specularVersion = "0.4.0"

ThisBuild / scalaVersion         := scala3Version
ThisBuild / organization         := "rocks.earlyeffect"
ThisBuild / organizationName     := "Early Effect"
ThisBuild / organizationHomepage := Some(url("https://www.earlyeffect.rocks"))
ThisBuild / versionScheme        := Some("early-semver")
// This plugin's own version uses stock sbt-dynver (tag v0.1.0 -> 0.1.0).

ThisBuild / homepage := Some(url("https://github.com/early-effect/sbt-dynver-ci"))
ThisBuild / licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / scmInfo  := Some(
  ScmInfo(
    url("https://github.com/early-effect/sbt-dynver-ci"),
    "scm:git@github.com:early-effect/sbt-dynver-ci.git",
  )
)
ThisBuild / developers := List(
  Developer(
    id = "russwyte",
    name = "Russ White",
    email = "356303+russwyte@users.noreply.github.com",
    url = url("https://github.com/russwyte"),
  )
)

// Sonatype Central Portal. sbt 2 has localStaging / publishSigned / sonaRelease.
ThisBuild / publishTo := {
  val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
  if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
  else localStaging.value
}

// CI-only publishing: key hex from PGP_KEY_HEX (org secret). Sentinel keeps local loads working.
usePgpKeyHex(sys.env.getOrElse("PGP_KEY_HEX", "MISSING_KEY_HEX"))

lazy val root = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .aggregate(docs)
  .settings(
    name := "sbt-dynver-ci",
    description :=
      "Cache-friendly sbt-dynver policy for CI: stable jar names between tags.",
    scalacOptions ++= Seq("-deprecation", "-feature", "-Wunused:all"),
    // Pull sbt-dynver transitively so consumers need one addSbtPlugin line.
    addSbtPlugin("com.github.sbt" % "sbt-dynver" % "5.1.1"),
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test,
    scriptedLaunchOpts ++= Seq("-Xmx512m", s"-Dplugin.version=${version.value}"),
    scriptedBufferLog := false,
    publishMavenStyle := true,
    pomIncludeRepository := { _ => false },
  )

lazy val docs = project
  .in(file("docs"))
  .enablePlugins(SpecularPlugin)
  .settings(
    name           := "sbt-dynver-ci-docs",
    publish / skip := true,
    scalacOptions ++= Seq("-deprecation", "-feature", "-Wunused:all"),
    libraryDependencies ++= Seq(
      "rocks.earlyeffect" %% "specular-core"           % specularVersion % Test,
      "rocks.earlyeffect" %% "specular-zio-test"       % specularVersion % Test,
      "rocks.earlyeffect" %% "specular-site"           % specularVersion % Test,
      "rocks.earlyeffect" %% "early-effect-docs-theme" % specularVersion % Test,
      "dev.zio"           %% "zio-test"                % "2.1.26"        % Test,
      "dev.zio"           %% "zio-test-sbt"            % "2.1.26"        % Test,
    ),
    Test / mainClass      := Some("specular.site.DocsServe"),
    specularBuildMain     := "rocks.earlyeffect.sbt.dynverci.docs.BuildSite",
    specularMetaProject   := Some(LocalProject("root")),
    specularArtifactKind  := "plugin",
    specularSiteDirectory := (ThisBuild / baseDirectory).value / "target" / "site",
  )

addCommandAlias("release", "; publishSigned; sonaRelease")
