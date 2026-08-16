import zipx.*

/** Typed catalog: every library, plugin, and Action this build may use. `zipxDepUpdate` / `zipxActionUpdate` rewrite
  * constructors here. sbt-zipx is not a row: generate emits it from the loaded plugin (`zipxSelfPlugins`).
  */
object MyVersions extends ZipxVersions:
  val sbt: SbtVersion     = SbtVersion("2.0.6")
  val scala: ScalaVersion = ScalaVersion("3.8.4")

  val scalatest = Lib("org.scalatest", "scalatest", "3.2.20").test

  val zio        = Lib("dev.zio", "zio", "2.1.26")
  val zioTest    = zio.mod("zio-test").test
  val zioTestSbt = zio.mod("zio-test-sbt").test

  val specular        = Lib("rocks.earlyeffect", "specular-core", "0.12.0")
  val specularZioTest = specular.mod("specular-zio-test").test
  val specularSite    = specular.mod("specular-site").test
  val specularTheme   = specular.mod("early-effect-docs-theme").test

  val scalafmt       = Plugin("org.scalameta", "sbt-scalafmt", "2.6.2")
  val dynver         = Plugin("com.github.sbt", "sbt-dynver", "5.1.1")
  val pgp            = Plugin("com.github.sbt", "sbt-pgp", "2.3.1")
  val specularPlugin = Plugin("rocks.earlyeffect", "sbt-specular", "0.12.0")

  val checkout       = Action("actions/checkout", "v7.0.1", sha = "3d3c42e5aac5ba805825da76410c181273ba90b1")
  val setupJava      = Action("actions/setup-java", "v5.7.0", sha = "b6effb05e454b25005698d916606bdc6ffcbf961")
  val setupSbt       = Action("sbt/setup-sbt", "v1.5.7", sha = "8feba82adc7f01ddcf8165b86f778bdb5b82cebc")
  val setupNode      = Action("actions/setup-node", "v7.0.0", sha = "820762786026740c76f36085b0efc47a31fe5020")
  val cache          = Action("actions/cache", "v6.1.0", sha = "55cc8345863c7cc4c66a329aec7e433d2d1c52a9")
  val uploadArtifact =
    Action("actions/upload-artifact", "v7.0.1", sha = "043fb46d1a93c77aae656e7c1c64a875d1fc6a0a")
  val downloadArtifact =
    Action("actions/download-artifact", "v8.0.1", sha = "3e5f45b2cfb9172054b4087a40e8e0b5a5461e7c")

  def pluginTest = library(scalatest)
  def docsTest   = library(specular.test, specularZioTest, specularSite, specularTheme, zioTest, zioTestSbt)
end MyVersions
