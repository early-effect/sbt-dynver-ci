MyVersions.settings

organization         := "rocks.earlyeffect"
organizationName     := "Early Effect"
organizationHomepage := Some(url("https://www.earlyeffect.rocks"))
versionScheme        := Some("early-semver")

// Mirror DynverCiPlugin.buildSettings here (stock dynver only in project/). Do not addSbtPlugin
// this artifact: the meta-build must not depend on a previously published version of itself.
// Must be ThisBuild-scoped: DynVerPlugin sets ThisBuild / version; a bare `version :=` only
// overrides the root project and leaves stock dynver (e.g. 0.2.2+3-hash) on ThisBuild — which
// breaks specularDisplayVersion (and anything else that reads ThisBuild / version).
def metaCiVersion: String =
  val suffix = "-ci"
  sbtdynver.DynVer
    .getGitDescribeOutput(new java.util.Date)
    .mkVersion(
      out => if out.isCleanAfterTag then out.ref.dropPrefix else out.ref.dropPrefix + suffix,
      "0.0.0" + suffix,
    )

ThisBuild / version := Def.uncached(metaCiVersion)
ThisBuild / dynver  := Def.uncached(metaCiVersion)

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

// zipx: Aggregate verify (testFull + scripted) + Central publish + Specular Pages + catalog PRs.
// Builtin fmt / workflow-check / advisories stay parallel; do not make test wait on fmt.
zipxJavaVersion      := JdkVersion("25")
zipxWorkflowDispatch := true
zipxTestTask         := zipxTasks.session(testFull, scripted)
zipxCapabilities += ZipxCentral.releaseRoot
zipxCapabilities += ZipxDocs.pages()

lazy val root = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .aggregate(docs)
  .settings(MyVersions.pluginTest)
  .settings(
    name := "sbt-dynver-ci",
    description :=
      "Cache-friendly sbt-dynver policy for CI: stable jar names between tags.",
    scalacOptions ++= Seq("-deprecation", "-feature", "-Wunused:all"),
    // Pull sbt-dynver transitively so consumers need one addSbtPlugin line.
    addSbtPlugin(MyVersions.moduleID(MyVersions.dynver)),
    scriptedLaunchOpts ++= Seq("-Xmx512m", s"-Dplugin.version=${version.value}"),
    scriptedBufferLog := false,
    publishMavenStyle := true,
    pomIncludeRepository := { _ => false },
  )

lazy val docs = project
  .in(file("docs"))
  .enablePlugins(SpecularPlugin)
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
    // Docs-only (workflow_dispatch) builds are dynver `-ci`; don't advertise that as a Central coord.
    // Empty string → Specular uses build version (clean v* tags).
    specularDisplayVersion := {
      val v = (ThisBuild / version).value
      if v.endsWith("-ci") || v.endsWith("-SNAPSHOT") then
        previousStableVersion.value.getOrElse("<version>")
      else ""
    },
  )

addCommandAlias("release", "; publishSigned; sonaRelease")
