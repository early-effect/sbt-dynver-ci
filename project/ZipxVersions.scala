import zipx.*

/** Typed catalog: every library and plugin this build may use. `zipxDepUpdate` rewrites constructors here. sbt-zipx is
  * not a row: generate emits it from the loaded plugin (`zipxSelfPlugins`). Action pins stay on jar defaults.
  */
object MyVersions extends ZipxVersions:
  val sbt: SbtVersion     = SbtVersion("2.0.6")
  val scala: ScalaVersion = ScalaVersion("3.8.4")

  val scalatest = Lib("org.scalatest", "scalatest", "3.2.20").test

  val specular        = Lib("rocks.earlyeffect", "specular-core", "0.12.0")
  val specularZioTest = specular.mod("specular-zio-test").test
  val specularTheme   = specular.mod("early-effect-docs-theme").test

  val scalafmt       = Plugin("org.scalameta", "sbt-scalafmt", "2.6.2")
  val dynver         = Plugin("com.github.sbt", "sbt-dynver", "5.1.1")
  val specularPlugin = Plugin("rocks.earlyeffect", "sbt-specular", "0.12.0")

  def pluginTest = library(scalatest)
  def docsTest   = library(specularZioTest, specularTheme)
end MyVersions
