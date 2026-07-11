val assertOnTag = taskKey[Unit]("assert version is the clean tag")
val assertAfterTag = taskKey[Unit]("assert version is tag-ci")

assertOnTag := {
  val v = version.value
  assert(v == "0.2.0", s"expected 0.2.0 on clean tag, got $v")
}

assertAfterTag := {
  val v = version.value
  assert(v == "0.2.0-ci", s"expected 0.2.0-ci after tag, got $v")
}
