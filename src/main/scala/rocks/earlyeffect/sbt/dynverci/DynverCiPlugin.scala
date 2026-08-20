package rocks.earlyeffect.sbt.dynverci

import sbt.*
import sbt.Keys.*
import sbtdynver.DynVerPlugin
import sbtdynver.DynVerPlugin.autoImport.dynver

/** Cache-friendly dynver policy for early-effect builds.
  *
  * On a clean version tag (`v0.2.0`): version is `0.2.0` (publish). Otherwise: version is `<last-tag>-ci` (e.g.
  * `0.2.0-ci`), so jar names and sbt 2 action-cache digests stay stable across commits until the next tag.
  *
  * Git state comes from sbt-dynver (`DynVer.getGitDescribeOutput` / `isCleanAfterTag`). This plugin only appends the
  * suffix. `version` / `dynver` are `Def.uncached` so a restored LocalDir cache cannot republish the last tag.
  *
  * Requires DynVerPlugin. Depends on sbt-dynver transitively; consumers only need:
  * {{{
  * addSbtPlugin("rocks.earlyeffect" % "sbt-dynver-ci" % "<version>")
  * }}}
  */
object DynverCiPlugin extends AutoPlugin:

  object autoImport:
    val dynverCiSuffix = settingKey[String](
      "Suffix appended when not cleanly on a version tag (default: \"-ci\")"
    )

  import autoImport.*

  override def requires: Plugins      = DynVerPlugin
  override def trigger: PluginTrigger = allRequirements

  override def buildSettings: Seq[Setting[?]] = Seq(
    dynverCiSuffix := DynverCiVersion.DefaultSuffix,
    version        := Def.uncached(ciVersion(dynverCiSuffix.value)),
    dynver         := Def.uncached(ciVersion(dynverCiSuffix.value)),
  )

  /** Last tagged version from sbt-dynver; append the suffix when the tree is not clean on that tag. */
  private def ciVersion(suffix: String): String =
    sbtdynver.DynVer
      .getGitDescribeOutput(new java.util.Date)
      .mkVersion(DynverCiVersion.format(_, suffix), DynverCiVersion.fallback(suffix))
end DynverCiPlugin
