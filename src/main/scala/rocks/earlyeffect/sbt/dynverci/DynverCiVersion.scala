package rocks.earlyeffect.sbt.dynverci

import sbtdynver.GitDescribeOutput

/** Pure version formatting for cache-friendly dynver.
  *
  * Clean after a version tag → release version (`0.2.0`). Otherwise → last tag + suffix (`0.2.0-ci`).
  */
object DynverCiVersion:

  val DefaultSuffix = "-ci"
  val FallbackBase  = "0.0.0"

  /** Format from dynver's structured git describe output. */
  def format(out: GitDescribeOutput, suffix: String = DefaultSuffix): String =
    format(out.isCleanAfterTag, out.ref.dropPrefix, suffix)

  /** Testable core: no git types required. */
  def format(isCleanAfterTag: Boolean, versionWithoutPrefix: String, suffix: String): String =
    if isCleanAfterTag then versionWithoutPrefix
    else versionWithoutPrefix + suffix

  def fallback(suffix: String = DefaultSuffix): String =
    FallbackBase + suffix
end DynverCiVersion
