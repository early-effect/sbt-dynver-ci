package rocks.earlyeffect.sbt.dynverci.docs

import specular.*
import zio.test.*

/** Why a stable CI version matters for sbt 2 action cache. */
object Overview extends DocSpec:

  def doc = page("Overview")(
    md"""
**sbt-dynver-ci** is a thin [sbt-dynver](https://github.com/sbt/sbt-dynver) policy for
early-effect builds. It keeps jar names stable across CI commits so sbt 2 action-cache digests
(`CompileInputs2`) hit instead of miss.
""",
    section("The problem")(
      md"""
Stock dynver encodes the current commit into the version (for example `0.0.0+1-<sha>`).
Those version strings land in jar paths, which are hashed into compile input digests. Every
push produces a new digest, so restoring `~/.cache/sbt` does not help: every compile looks
like a miss.
"""
    ),
    section("The policy")(
      md"""
On a clean checkout of a version tag such as `v0.2.0`, the version is the release itself:
`0.2.0`. Anywhere else after that tag, including dirty trees and commits that are not the
tag tip, the version is the last release with a fixed suffix: `0.2.0-ci`. If the repository
has no tags yet, the version is `0.0.0-ci`.

Between tags, jar names stay fixed, so action-cache digests can match across CI pushes.
Cutting the next tag starts a new cache generation on purpose.
""",
      exampleValue {
        def format(isCleanAfterTag: Boolean, version: String, suffix: String): String =
          if isCleanAfterTag then version else version + suffix

        (
          format(true, "0.2.0", "-ci"),
          format(false, "0.2.0", "-ci"),
          format(false, "0.0.0", "-ci"),
        )
      }.assert { case (release, ci, untagged) =>
        assertTrue(release == "0.2.0", ci == "0.2.0-ci", untagged == "0.0.0-ci")
      },
    ),
  )
end Overview
