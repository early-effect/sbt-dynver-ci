val scala3Version   = "3.8.4"
val specularVersion = "0.7.1"

scalaVersion         := scala3Version
organization         := "rocks.earlyeffect"
organizationName     := "Early Effect"
organizationHomepage := Some(url("https://www.earlyeffect.rocks"))
versionScheme        := Some("early-semver")

// Mirror DynverCiPlugin here (stock dynver only in project/). Do not addSbtPlugin this
// artifact: the meta-build must not depend on a previously published version of itself.
version := {
  val suffix = "-ci"
  sbtdynver.DynVerPlugin.autoImport.dynverGitDescribeOutput.value.mkVersion(
    out => if out.isCleanAfterTag then out.ref.dropPrefix else out.ref.dropPrefix + suffix,
    "0.0.0" + suffix,
  )
}
dynver := {
  val suffix = "-ci"
  sbtdynver.DynVer
    .getGitDescribeOutput(new java.util.Date)
    .mkVersion(
      out => if out.isCleanAfterTag then out.ref.dropPrefix else out.ref.dropPrefix + suffix,
      "0.0.0" + suffix,
    )
}

homepage := Some(url("https://github.com/early-effect/sbt-dynver-ci"))
licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
scmInfo  := Some(
  ScmInfo(
    url("https://github.com/early-effect/sbt-dynver-ci"),
    "scm:git@github.com:early-effect/sbt-dynver-ci.git",
  )
)
developers := List(
  Developer(
    id = "russwyte",
    name = "Russ White",
    email = "356303+russwyte@users.noreply.github.com",
    url = url("https://github.com/russwyte"),
  )
)

// Sonatype Central Portal. sbt 2 has localStaging / publishSigned / sonaRelease.
publishTo := {
  val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
  if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
  else localStaging.value
}

// CI-only publishing: key hex from PGP_KEY_HEX (org secret). Sentinel keeps local loads working.
usePgpKeyHex(sys.env.getOrElse("PGP_KEY_HEX", "MISSING_KEY_HEX"))

// zipx: Aggregate verify (test + scripted) + Central publish + Specular Pages + Steward.
zipxJavaVersion      := "25"
zipxWorkflowDispatch := true
zipxScalaSteward     := true
zipxCapabilities += Capability.once("fmt", "scalafmtCheckAll")
zipxCapabilities += Capability.once(
  name = "test",
  command = "test; scripted",
  needsCapabilities = List("fmt"),
)
zipxCapabilities += ZipxCentral.release
zipxCapabilities += ZipxDocs.pages()

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
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.20" % Test,
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
    specularSiteDirectory := (LocalRootProject / baseDirectory).value / "target" / "site",
  )

addCommandAlias("release", "; publishSigned; sonaRelease")
