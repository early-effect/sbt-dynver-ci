package rocks.earlyeffect.sbt.dynverci.docs

import rocks.earlyeffect.sbt.dynverci.DynverCiVersion
import specular.*
import specular.ziotest.DocSpecSuite
import zio.test.*

/** The version string, the suffix, and what CI has to give git. */
object Usage extends DocSpecSuite:

  def doc = page("Usage")(
    md"""
One `addSbtPlugin` line is enough. The plugin depends on sbt-dynver transitively and enables itself via
`allRequirements`. It is published for **sbt 2** only. Do **not** set `version` in `build.sbt`.
""",
    section("The string")(
      md"""
`DynverCiVersion.format` reads sbt-dynver's last tag. `isCleanAfterTag` is true only on a clean checkout of that tag:
the version is the tag with the `v` prefix dropped (`0.2.3`). A dirty tree and a commit after the tag are the same
branch. Both append `dynverCiSuffix` (default `-ci`), and neither the sha nor the distance is included, so the jar
name stays. No git describe yields `0.0.0` plus the suffix.

`version` and `dynver` are `Def.uncached`, so a restored LocalDir cache cannot republish the last tag. You do not
need `cleanFull` on publish to bust a stale version string.
""",
      exampleValue {
        (
          DynverCiVersion.format(isCleanAfterTag = true, versionWithoutPrefix = "0.2.3", suffix = "-ci"),
          DynverCiVersion.format(isCleanAfterTag = false, versionWithoutPrefix = "0.2.3", suffix = "-ci"),
          DynverCiVersion.format(isCleanAfterTag = true, versionWithoutPrefix = "0.2.3", suffix = "-SNAPSHOT"),
          DynverCiVersion.fallback(),
        )
      }.assert { case (release, held, customOnTag, untagged) =>
        assertTrue(
          release == "0.2.3",
          held == "0.2.3-ci",
          customOnTag == "0.2.3",
          untagged == "0.0.0-ci",
        )
      },
    ),
    section("Suffix")(
      md"""
The default suffix is `-ci`. Override it when you want a different marker between tags:

```scala
dynverCiSuffix := "-SNAPSHOT"
```

A clean version tag still yields the release version. Every other state appends the configured suffix.
"""
    ),
    section("CI")(
      md"""
Give dynver enough history to see tags: set `fetch-depth: 0` (or a depth that includes the latest `v*` tags).
Publish releases from a clean checkout of a `v*` tag so the release workflow sees the release version rather than
the suffix.
"""
    ),
  )
end Usage
