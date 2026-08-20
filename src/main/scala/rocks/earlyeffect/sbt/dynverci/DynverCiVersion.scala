package rocks.earlyeffect.sbt.dynverci

import scala.sys.process.*
import scala.util.Try

import sbtdynver.GitDescribeOutput

/** Pure version formatting for cache-friendly dynver.
  *
  * Clean after a version tag → release version (`0.2.0`). Otherwise → last tag + suffix (`0.2.0-ci`). "Clean after tag"
  * is sbt-dynver's `isCleanAfterTag` (`git describe --long`: on a tag, distance zero, tree not dirty).
  */
object DynverCiVersion:

  val DefaultSuffix = "-ci"
  val FallbackBase  = "0.0.0"

  /** Live `git describe` via sbt-dynver. When already on a tag, pick the highest `v*` tag on HEAD so a same-commit
    * retag is not stuck on the older name describe chose.
    */
  def fromGit(suffix: String = DefaultSuffix, now: java.util.Date = new java.util.Date): String =
    sbtdynver.DynVer.getGitDescribeOutput(now) match
      case None      => fallback(suffix)
      case Some(out) =>
        val release =
          if out.isCleanAfterTag then highestOnHead(out.ref.dropPrefix, gitTagsPointingAtHead())
          else out.ref.dropPrefix
        format(out.isCleanAfterTag, release, suffix)

  def highestOnHead(describedWithoutPrefix: String, tagsOnHead: Seq[String]): String =
    val names = describedWithoutPrefix +: tagsOnHead.map(_.stripPrefix("v")).filter(_.nonEmpty)
    names.maxBy(releaseOrder)

  /** Format from dynver's structured git describe output. */
  def format(out: GitDescribeOutput, suffix: String = DefaultSuffix): String =
    format(out.isCleanAfterTag, out.ref.dropPrefix, suffix)

  /** Testable core: no git types required. */
  def format(isCleanAfterTag: Boolean, versionWithoutPrefix: String, suffix: String): String =
    if isCleanAfterTag then versionWithoutPrefix
    else versionWithoutPrefix + suffix

  def fallback(suffix: String = DefaultSuffix): String =
    FallbackBase + suffix

  private def gitTagsPointingAtHead(): Seq[String] =
    Try(Process(Seq("git", "tag", "--points-at", "HEAD", "--list", "v*")).!!.trim).toOption.toSeq
      .flatMap(_.split('\n').map(_.trim).filter(_.nonEmpty))

  private def releaseOrder(v: String): (Int, Int, Int, String) =
    val (core, rest) = v.span(c => c.isDigit || c == '.')
    val nums         = core.split('.').toList.filter(_.nonEmpty).map(_.toIntOption.getOrElse(0)).padTo(3, 0)
    (nums(0), nums(1), nums(2), rest)
end DynverCiVersion
