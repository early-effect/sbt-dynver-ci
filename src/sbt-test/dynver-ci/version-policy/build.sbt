scalaVersion := "3.8.4"

val assertOnTag    = taskKey[Unit]("assert version is the clean tag")
val assertRetag    = taskKey[Unit]("assert version is the newer tag on the same commit")
val assertAfterTag = taskKey[Unit]("assert version is tag-ci")

assertOnTag := {
  val v = version.value
  assert(v == "0.2.0", s"expected 0.2.0 on clean tag, got $v")
}

assertRetag := {
  val v = version.value
  assert(v == "0.2.1", s"expected 0.2.1 after same-commit retag, got $v")
}

assertAfterTag := {
  val v = version.value
  assert(v.endsWith("-ci"), s"expected a -ci version after a commit, got $v")
  assert(v != "0.2.1", s"must not still be the release version, got $v")
}
