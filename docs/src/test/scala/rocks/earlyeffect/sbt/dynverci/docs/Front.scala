package rocks.earlyeffect.sbt.dynverci.docs

import ascent.ast.{Attr, UI}
import ascent.domtypes.AttrValue
import mermoid.{LayoutConfig, Mermaid, RenderConfig, SvgNode, SvgRenderer, Viewport}
import rocks.earlyeffect.sbt.dynverci.DynverCiVersion
import specular.*
import specular.ziotest.DocSpecSuite
import zio.test.*

/** Install coordinate. Judge the raw build version. Do not trust a version after `-ci` was stripped. */
object DocsVersion:
  /** Last published tag (`v0.2.3`). ShipGroup is the next line and is not published. */
  val published = "0.2.3"

  /** A dynver distance (`-ci`, `+`, `SNAPSHOT`) is not a coordinate to copy. Anything else is advertised as written. */
  def advertise(raw: String): String =
    val trimmed  = raw.trim
    val distance = trimmed.contains("-ci") || trimmed.contains("+") || trimmed.contains("SNAPSHOT")
    if trimmed.nonEmpty && !distance then trimmed else published

  /** `specular.meta.version` is the raw build version. Absent in unit tests, so those use [[published]]. */
  def version: String =
    sys.props.get("specular.meta.version").map(advertise).getOrElse(published)
end DocsVersion

/** The site front. Specular still emits a summary index; [[BuildSite]] copies this page over it.
  *
  * Not a sidebar item.
  */
object Front extends DocSpecSuite:

  private val coordinate: String =
    s"""addSbtPlugin("rocks.earlyeffect" % "sbt-dynver-ci" % "${DocsVersion.version}")"""

  /** Phone column is about 390px. Three short nodes on the first rank stay inside that. */
  private val figure =
    RenderConfig(layout =
      LayoutConfig(
        minNodeWidth = 72,
        nodeHeight = 36,
        nodePaddingH = 8,
        charWidthEstimate = 7.0,
        hSpacing = 12,
        vSpacing = 22,
        padding = 8,
        fontSize = 12,
        edgeLabelFontSize = 11,
        lineHeight = 14,
        arrowSize = 8,
      )
    )

  /** Clean tag keeps `dropPrefix`. Dirty tree and a later commit are the same branch: `dropPrefix` plus the suffix.
    * That string is the jar name that stays.
    */
  private val pipeline =
    Mermaid("""flowchart TB
              |    tag["clean tag"] --> rel["0.2.3"]
              |    dirty["dirty tree"] --> ci["0.2.3-ci"]
              |    push["CI commit"] --> ci
              |    ci --> jar["jar name stays"]
              |""".stripMargin)

  private val painted = SvgRenderer.renderTree(pipeline.diagram, figure, Some(Viewport(340)))

  private def plainText(ui: UI[Any]): String = ui match
    case UI.Text(value)            => value
    case element: UI.Element[Any]  => element.children.map(plainText).mkString
    case UI.Empty                  => ""
    case UI.Fragment(children)     => children.map(plainText).mkString
    case UI.ReactiveText(_)        => ""
    case UI.ReactiveChild(_)       => ""
    case UI.When(_, _)             => ""
    case UI.ForEach(_, _, _)       => ""
    case UI.ForEachSignal(_, _, _) => ""
    case UI.Scoped(_)              => ""
    case UI.ServerRegion(_, _)     => ""

  private def attr(ui: UI[Any], tag: String, name: String): Option[String] = ui match
    case element: UI.Element[Any] if element.tag == tag =>
      element.attrs.collectFirst { case Attr.StaticAttr(`name`, AttrValue.Str(raw)) => raw }
    case element: UI.Element[Any] =>
      element.children.iterator.map(node => attr(node, tag, name)).collectFirst { case Some(raw) => raw }
    case UI.Fragment(children) =>
      children.iterator.map(node => attr(node, tag, name)).collectFirst { case Some(raw) => raw }
    case UI.Text(_) | UI.Empty | UI.ReactiveText(_) | UI.ReactiveChild(_) | UI.When(_, _) | UI.ForEach(_, _, _) |
        UI.ForEachSignal(_, _, _) | UI.Scoped(_) | UI.ServerRegion(_, _) =>
      None

  /** Third number of `viewBox="0 0 W H"`. */
  private def viewBoxWidth(raw: String): Option[Double] =
    raw.split("\\s+").toList.filter(_.nonEmpty) match
      case _ :: _ :: width :: _ => width.toDoubleOption
      case _                    => None

  private def diagram: UI[Any] =
    SvgEmbed.root(painted)

  def doc = page("The jar name stays put")(
    md"""
Between tags, sbt-dynver-ci keeps one jar name, so an sbt 2 action-cache hit survives the next push.
""",
    illustration(diagram).assert { ui =>
      val text  = plainText(ui)
      val box   = attr(ui, "svg", "viewBox")
      val width = box.flatMap(viewBoxWidth)
      val style = attr(ui, "svg", "style")
      assertTrue(
        text.contains("clean tag"),
        text.contains("dirty tree"),
        text.contains("CI commit"),
        text.contains("jar name stays"),
        text.contains("0.2.3-ci"),
        pipeline.source.contains("""["0.2.3"]"""),
        pipeline.source.contains("dirty tree"),
        pipeline.source.contains("CI commit"),
        attr(ui, "svg", "width").contains("100%"),
        style.exists(_.contains("max-width:100%")),
        style.exists(_.contains("height:auto")),
        width.exists(_ > 0),
        width.exists(_ <= 360),
      ).label(s"viewBox $box")
    },
    section("Install")(
      md"""
The coordinate is the last published release. A build whose version contains `-ci`, `+`, or `SNAPSHOT` still shows
that release. The artifact is `sbt-dynver-ci`: one `%`, not `%%`.

```scala
${coordinate}
```
"""
    ),
    section("format")(
      md"""
`format` is the three-argument method `DynverCiVersion.format`. A clean tag keeps the tag with its prefix dropped.
Anything else appends the suffix, so the jar name stays on that string. The cite is
`cite(DynverCiVersion.format(_, _, _))`. The panel is that method. The footer is the definition.
""",
      cite(DynverCiVersion.format(_, _, _)),
    ),
  )

  override def spec =
    suite("The jar name stays put")(
      super.spec,
      test("install coordinate is the published release") {
        val version = DocsVersion.version
        assertTrue(
          coordinate.contains(version),
          coordinate.contains("addSbtPlugin"),
          coordinate.contains("rocks.earlyeffect"),
          coordinate.contains("% \"sbt-dynver-ci\""),
          coordinate.contains(s"% \"$version\""),
          !coordinate.contains("%%"),
          !coordinate.contains("<version>"),
          !version.contains("-ci"),
          !version.contains("+"),
          !version.contains("SNAPSHOT"),
          // The artifact id is sbt-dynver-ci, so the line contains "-ci". The version token must not.
          !coordinate.contains(s"% \"$version-ci\""),
          !coordinate.contains("SNAPSHOT"),
          !coordinate.contains("0.2.4"),
          DocsVersion.advertise("0.2.3-ci") == DocsVersion.published,
          DocsVersion.advertise("0.2.4-ci") == DocsVersion.published,
          DocsVersion.advertise("0.2.3+10-g2c84f47") == DocsVersion.published,
          DocsVersion.advertise("0.2.4-SNAPSHOT") == DocsVersion.published,
          DocsVersion.advertise("0.2.3") == "0.2.3",
        )
      },
    )
end Front

/** Root SVG scaled to the content column. Nested shapes keep the sizes mermoid painted. */
private object SvgEmbed:

  def root(node: SvgNode): UI[Any] = node match
    case SvgNode.Element("svg", attrs, children) =>
      element("svg", fit(attrs), children)
    case other =>
      paint(other)

  private def paint(node: SvgNode): UI[Any] = node match
    case SvgNode.Text(value) =>
      UI.Text(value)
    case SvgNode.Raw(content) =>
      UI.Text(content)
    case SvgNode.Element(tag, attrs, children) =>
      element(tag, attrs, children)

  private def element(tag: String, attrs: List[(String, String)], children: List[SvgNode]): UI[Any] =
    UI.Element(
      tag,
      attrs.map((name, value) => Attr.StaticAttr(name, AttrValue.Str(value))).toVector,
      children.map(paint).toVector,
    )

  /** Drop the painted pixel box. `viewBox` keeps the aspect; the column width does the rest. */
  private def fit(attrs: List[(String, String)]): List[(String, String)] =
    val kept = attrs.filter((name, _) => name != "width" && name != "height" && name != "style")
    kept ++ List(
      "width" -> "100%",
      "style" -> "height:auto;max-width:100%;display:block",
    )
end SvgEmbed
