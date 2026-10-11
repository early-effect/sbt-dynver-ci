import zipx.*

/** Typed catalog: every library and plugin this build may use. `zipxDepUpdate` rewrites constructors here. sbt-zipx is
  * not a row: generate emits it from the loaded plugin (`zipxSelfPlugins`). Action pins stay on jar defaults.
  */
object MyVersions extends ZipxVersions:
  val sbt: SbtVersion     = SbtVersion("2.1.0-M3")
  val scala: ScalaVersion = ScalaVersion("3.9.0")

  val release = ShipGroup("sbt-dynver-ci", "0.2.4")("root")

  val scalatest = Lib("org.scalatest", "scalatest", "3.2.20").test

  val specular        = Lib("rocks.earlyeffect", "specular-core", "0.20.0")
  val specularZioTest = specular.mod("specular-zio-test").test
  val specularSite    = specular.mod("specular-site").test
  val specularTheme   = specular.mod("early-effect-docs-theme").test

  /** Docs figures only. The published plugin does not select this. */
  val mermoid = Lib("rocks.earlyeffect", "mermoid", "0.2.0")

  val scalafmt       = Plugin("org.scalameta", "sbt-scalafmt", "2.6.2")
  val specularPlugin = Plugin("rocks.earlyeffect", "sbt-specular", "0.20.0")

  def pluginTest = library(scalatest)
  def docsTest   = library(specular.test, specularZioTest, specularSite, specularTheme, mermoid.test)
end MyVersions
