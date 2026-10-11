MyVersions.settings

organization         := "rocks.earlyeffect"
organizationName     := "Early Effect"
organizationHomepage := Some(url("https://www.earlyeffect.rocks"))
versionScheme        := Some("early-semver")

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

// zipx: Aggregate verify + Central publish + Specular Pages + catalog PRs.
// Builtin fmt / workflow-check / advisories stay parallel; do not make test wait on fmt.
// scripted lives on the plugin project. A build-wide session would send docs/scripted when a
// pull request only affects docs.
zipxJavaVersion      := JdkVersion("25")
zipxWorkflowDispatch := true
zipxCapabilities += ZipxCentral.snapshots
zipxCapabilities += ZipxCentral.pullRequestSnapshots("snapshots")
zipxCapabilities += ZipxDocs.pages()
zipxReleaseWorkflow := Some(ZipxCentral.releases)

lazy val root: Project = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .aggregate(docs)
  .settings(MyVersions.pluginTest)
  .settings(
    // docs/compile reads this project's classes (cite). Aggregating compile would cycle.
    Compile / compile / aggregate := false,
  )
  .settings(
    zipxPublish  := zipxOn,
    zipxTestTask := zipxTasks.session(testFull, scripted),
    name := "sbt-dynver-ci",
    description :=
      "Cache-friendly sbt-dynver policy for CI: stable jar names between tags.",
    scalacOptions ++= Seq("-deprecation", "-feature", "-Wunused:all"),
    // Pull sbt-dynver transitively so consumers need one addSbtPlugin line.
    addSbtPlugin("com.github.sbt" % "sbt-dynver" % "5.1.1"),
    scriptedLaunchOpts ++= Seq("-Xmx512m", s"-Dplugin.version=${version.value}"),
    scriptedBufferLog := false,
    publishMavenStyle := true,
    pomIncludeRepository := { _ => false },
  )

lazy val docs: Project = project
  .in(file("docs"))
  .enablePlugins(SpecularPlugin)
  // Cite reads DynverCiVersion.format from this build. The published plugin does not depend on docs.
  // LocalProject, not `root`: root aggregates docs, so a direct reference deadlocks project init.
  // sbt is provided, so dependsOn alone does not put sbt-dynver on the docs classpath.
  .dependsOn(LocalProject("root"))
  .settings(
    Test / unmanagedClasspath ++= (LocalProject("root") / Compile / dependencyClasspath).value
  )
  .settings(MyVersions.docsTest)
  .settings(
    name           := "sbt-dynver-ci-docs",
    publish / skip := true,
    scalacOptions ++= Seq("-deprecation", "-feature", "-Wunused:all"),
    Test / mainClass      := Some("specular.site.DocsServe"),
    specularBuildMain     := "rocks.earlyeffect.sbt.dynverci.docs.BuildSite",
    specularMetaProject   := Some(LocalProject("root")),
    specularArtifactKind  := "plugin",
    specularSiteDirectory := (LocalRootProject / baseDirectory).value / "target" / "site",
    // Do not call stripCi. It only drops a trailing -ci, so a `+` build or a SNAPSHOT would
    // still be advertised, and stripping a next-line -ci would publish that unpublished version.
    // Any raw version containing -ci, +, or SNAPSHOT advertises the last published tag.
    specularDisplayVersion := { raw =>
      val distance = raw.contains("-ci") || raw.contains("+") || raw.contains("SNAPSHOT")
      if raw.nonEmpty && !distance then raw else "0.2.3"
    },
  )

addCommandAlias("release", "; publishSigned; sonaRelease")
