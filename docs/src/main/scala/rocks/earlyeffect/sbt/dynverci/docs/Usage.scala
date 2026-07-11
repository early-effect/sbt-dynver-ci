package rocks.earlyeffect.sbt.dynverci.docs

import specular.*
import zio.test.*

/** Install and configure sbt-dynver-ci. */
object Usage extends DocSpec:

  def doc = page("Usage")(
    md"""
One `addSbtPlugin` line is enough. The plugin depends on sbt-dynver transitively and enables
itself via `allRequirements`. Do **not** set `version` in `build.sbt`.
""",
    section("Install")(
      md"""
```scala
// project/plugins.sbt
addSbtPlugin("rocks.earlyeffect" % "sbt-dynver-ci" % "<version>")
```

Requires **sbt 2** (published as `_sbt2_3`).
"""
    ),
    section("Optional suffix")(
      md"""
The default CI suffix is `-ci`. Override if you want something else (for example
`-SNAPSHOT`):

```scala
ThisBuild / dynverCiSuffix := "-SNAPSHOT"
```
""",
      exampleValue {
        val base = "1.0.0"
        val suffix = "-SNAPSHOT"
        base + suffix
      }.assert(v => assertTrue(v == "1.0.0-SNAPSHOT"))
    ),
    section("CI tips")(
      md"""
- Keep `fetch-depth: 0` (or enough history) so dynver can see tags.
- Publish releases from a clean `v*` tag; the Release workflow should run on that tag.
- Do not also add stock sbt-dynver in `plugins.sbt` unless you need it for something else;
  this plugin already brings it in.
"""
    )
  )
end Usage
