# sbt-dynver-ci

Between tags, sbt-dynver-ci keeps one jar name, so an sbt 2 action-cache hit survives the next push.

Docs: [early-effect.github.io/sbt-dynver-ci](https://early-effect.github.io/sbt-dynver-ci/)

| Git state | Version |
| --- | --- |
| Clean on tag `v0.2.3` | `0.2.3` |
| Dirty tree, or any commit after that tag | `0.2.3-ci` |
| No tags | `0.0.0-ci` |

Cutting the next tag intentionally starts a new cache generation. `version` itself is uncached and re-reads git, so a restored previous-tag action cache cannot republish that tag.

## Usage

```scala
// project/plugins.sbt
addSbtPlugin("rocks.earlyeffect" % "sbt-dynver-ci" % "0.2.3")
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
