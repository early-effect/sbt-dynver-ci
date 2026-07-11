# sbt-dynver-ci

Cache-friendly [sbt-dynver](https://github.com/sbt/sbt-dynver) policy for early-effect builds.

| Git state | Version |
| --- | --- |
| Clean on tag `v0.2.0` | `0.2.0` |
| Any commit after that tag | `0.2.0-ci` |
| No tags | `0.0.0-ci` |

Between tags, jar names stay stable so sbt 2 action-cache digests (`CompileInputs2`) match across CI pushes. Cutting the next tag intentionally starts a new cache generation.

## Usage

```scala
// project/plugins.sbt
addSbtPlugin("rocks.earlyeffect" % "sbt-dynver-ci" % "<version>")
```

Do **not** set `version` in `build.sbt`. sbt-dynver is pulled in transitively.

Optional:

```scala
ThisBuild / dynverCiSuffix := "-SNAPSHOT" // default is "-ci"
```

## Release this plugin

Stock dynver versions this repo from tags. Push `v0.1.0` to publish via the Release workflow (CI-only signing).

## License

Apache-2.0
