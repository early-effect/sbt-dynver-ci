package rocks.earlyeffect.sbt.dynverci.docs

import specular.*
import specular.ziotest.DocSpecSuite

/** Install and configure sbt-dynver-ci. */
object Usage extends DocSpecSuite:

  def doc = page("Usage")(
    md"""
One `addSbtPlugin` line is enough. The plugin depends on sbt-dynver transitively and enables
itself via `allRequirements`. Do **not** set `version` in `build.sbt`; dynver owns that key.
""",
    section("Install")(
      md"""
Add the plugin from Maven Central. It is published for **sbt 2** only (the `_sbt2_3`
coordinate):

```scala
// project/plugins.sbt
addSbtPlugin("rocks.earlyeffect" % "sbt-dynver-ci" % "<version>")
```

You do not need a separate `addSbtPlugin` for stock sbt-dynver unless you rely on it for
something else; this plugin already brings it in.
"""
    ),
    section("Optional suffix")(
      md"""
The default CI suffix is `-ci`. Override it when you want a different marker between tags,
for example `-SNAPSHOT`:

```scala
ThisBuild / dynverCiSuffix := "-SNAPSHOT"
```

The same rule applies: a clean version tag still yields the release version; every other
state appends the configured suffix.
"""
    ),
    section("CI tips")(
      md"""
Give dynver enough history to see tags: set `fetch-depth: 0` (or a depth that includes the
latest `v*` tags). Publish releases from a clean checkout of a `v*` tag so the Release
workflow sees the release version rather than the CI suffix.
"""
    ),
  )
end Usage
