# sbt-dynver-ci

Cache-friendly [sbt-dynver](https://github.com/sbt/sbt-dynver) policy for early-effect builds.

Docs: [early-effect.github.io/sbt-dynver-ci](https://early-effect.github.io/sbt-dynver-ci/) (after first `v*` Docs deploy).

| Git state | Version |
| --- | --- |
| Clean on tag `v0.2.0` | `0.2.0` |
| Any commit after that tag | `0.2.0-ci` |
| No tags | `0.0.0-ci` |

Between tags, jar names stay stable so sbt 2 action-cache digests (`CompileInputs2`) match across CI pushes. Cutting the next tag intentionally starts a new cache generation. `version` itself is uncached and re-reads git, so a restored previous-tag action cache cannot republish that tag.

## Usage

```scala
// project/plugins.sbt
addSbtPlugin("rocks.earlyeffect" % "sbt-dynver-ci" % "<version>")
```

Do **not** set `version` in `build.sbt`. sbt-dynver is pulled in transitively.

Optional:

```scala
dynverCiSuffix := "-SNAPSHOT" // default is "-ci"
```

## Release this plugin

The meta-build uses stock sbt-dynver plus the same `-ci` version formula inlined in `build.sbt`
(so we never depend on a prior published copy of this plugin). Push a `v*` tag to publish via
the Release workflow (CI-only signing).

## License

Apache-2.0

## Development

```bash
./scripts/install-git-hooks  # once per clone: pre-commit runs scalafmtCheckAll
```
