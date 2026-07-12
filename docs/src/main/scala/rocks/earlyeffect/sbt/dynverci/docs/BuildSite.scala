package rocks.earlyeffect.sbt.dynverci.docs

import earlyeffect.docs.EarlyEffectTheme
import specular.*
import specular.site.*
import zio.*

import java.nio.file.{Path, Paths}

/** Builds the docs site into `<repo>/target/site`. */
object BuildSite extends ZIOAppDefault:

  def run =
    val out  = SitePaths.outDir(repoRoot.resolve("target/site"))
    val base = SitePaths.basePath(".")
    val meta = ProjectMeta.fromSystemProperties
      .map(m =>
        m.copy(
          name = "sbt-dynver-ci",
          title = Some("sbt-dynver-ci"),
          description = Some(
            m.description.getOrElse(
              "Cache-friendly sbt-dynver policy for CI: stable jar names between tags."
            )
          ),
          language = Some("Scala"),
        )
      )
      .orElse(
        Some(
          ProjectMeta(
            name = "sbt-dynver-ci",
            organization = "rocks.earlyeffect",
            version = "0.1.0-SNAPSHOT",
            scalaVersion = "3.8.4",
            title = Some("sbt-dynver-ci"),
            description = Some(
              "Cache-friendly sbt-dynver policy for CI: stable jar names between tags."
            ),
            language = Some("Scala"),
          )
        )
      )
    val version = meta.map(_.version).getOrElse("0.1.0-SNAPSHOT")
    val org     = meta.map(_.organization).getOrElse("rocks.earlyeffect")
    val model   = SiteModel(
      title = "sbt-dynver-ci",
      basePath = base,
      pages = Vector(Overview.doc, Usage.doc),
      clientScript = None,
      meta = meta,
      description = meta.flatMap(_.description),
      logo = Some(EarlyEffectTheme.logoHref),
      logoLink = Some("https://www.earlyeffect.rocks/"),
      summaryMarkdown = Some(
        s"""**sbt-dynver-ci** is a thin sbt-dynver policy for early-effect CI builds.
On a clean version tag the version is the release itself (`0.2.0`). Everywhere else it is
`<last-tag>-ci`, so jar names and sbt 2 action-cache digests stay stable across commits
until the next tag.
"""
      ),
      installSnippets = Vector(
        CodeSnippet(
          "Install",
          s"""// project/plugins.sbt
addSbtPlugin("$org" % "sbt-dynver-ci" % "$version")""",
        ),
        CodeSnippet(
          "Optional suffix",
          """ThisBuild / dynverCiSuffix := "-SNAPSHOT" // default is "-ci"""",
        ),
      ),
    )
    ZIO
      .serviceWithZIO[SiteBuilder](_.buildSite(model, out))
      .flatMap { result =>
        EarlyEffectTheme.writeLogo(out) *>
          Console.printLine(s"Wrote ${result.pages.mkString(", ")}")
      }
      .provide(
        MarkdownRenderer.live,
        ExampleRunner.live,
        HtmlSsr.live,
        SiteWriter.live,
        NavBuilder.live,
        EarlyEffectTheme.live,
        PageTemplate.live,
        LandingTemplate.live,
        SiteBuilder.live,
      )
  end run

  private def repoRoot: Path =
    Iterator
      .iterate(Paths.get("").toAbsolutePath.nn)(p => Option(p.getParent).orNull)
      .takeWhile(_ != null)
      .find(p => java.nio.file.Files.exists(p.resolve("build.sbt")))
      .getOrElse(Paths.get("").toAbsolutePath.nn)
end BuildSite
