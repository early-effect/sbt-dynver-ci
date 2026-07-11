package rocks.earlyeffect.sbt.dynverci

import sbt.*
import sbt.Keys.*
import sbtdynver.DynVerPlugin
import sbtdynver.DynVerPlugin.autoImport.*

/** Cache-friendly dynver policy for early-effect builds.
  *
  * On a clean version tag (`v0.2.0`): version is `0.2.0` (publish).
  * Otherwise: version is `<last-tag>-ci` (e.g. `0.2.0-ci`), so jar names and
  * sbt 2 action-cache digests stay stable across commits until the next tag.
  *
  * Requires [[DynVerPlugin]]. Depends on sbt-dynver transitively; consumers only need:
  * {{{
  * addSbtPlugin("rocks.earlyeffect" % "sbt-dynver-ci" % "<version>")
  * }}}
  */
object DynverCiPlugin extends AutoPlugin:

  object autoImport:
    val dynverCiSuffix = settingKey[String](
      "Suffix appended when not cleanly on a version tag (default: \"-ci\")"
    )
  end autoImport

  import autoImport.*

  override def requires: Plugins = DynVerPlugin
  override def trigger: PluginTrigger = allRequirements

  override def buildSettings: Seq[Setting[?]] = Seq(
    dynverCiSuffix := DynverCiVersion.DefaultSuffix,
    // Override DynVerPlugin's version / dynver with the cache-friendly formula.
    version := {
      val suffix = dynverCiSuffix.value
      dynverGitDescribeOutput.value.mkVersion(
        DynverCiVersion.format(_, suffix),
        DynverCiVersion.fallback(suffix),
      )
    },
    dynver := {
      val suffix = dynverCiSuffix.value
      val d = new java.util.Date
      sbtdynver.DynVer.getGitDescribeOutput(d).mkVersion(
        DynverCiVersion.format(_, suffix),
        DynverCiVersion.fallback(suffix),
      )
    },
  )
end DynverCiPlugin
