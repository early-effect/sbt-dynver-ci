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
  * Same formula as before, but `version` / `dynver` call live `DynVer.getGitDescribeOutput` under `Def.uncached` so a
  * restored previous-tag LocalDir cache cannot republish that tag. "On the tag" is sbt-dynver's `isCleanAfterTag`.
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
    version        := Def.uncached(DynverCiVersion.fromGit(dynverCiSuffix.value)),
    dynver         := Def.uncached(DynverCiVersion.fromGit(dynverCiSuffix.value)),
  )
end DynverCiPlugin
