package rocks.earlyeffect.sbt.dynverci

import org.scalatest.funsuite.AnyFunSuite

class DynverCiVersionSpec extends AnyFunSuite:

  test("clean after tag keeps the release version") {
    assert(DynverCiVersion.format(isCleanAfterTag = true, "0.2.0", "-ci") == "0.2.0")
  }

  test("commits after a tag append the ci suffix") {
    assert(DynverCiVersion.format(isCleanAfterTag = false, "0.2.0", "-ci") == "0.2.0-ci")
  }

  test("custom suffix is honored") {
    assert(DynverCiVersion.format(isCleanAfterTag = false, "1.0.0", "-SNAPSHOT") == "1.0.0-SNAPSHOT")
  }

  test("fallback is 0.0.0 plus suffix") {
    assert(DynverCiVersion.fallback() == "0.0.0-ci")
    assert(DynverCiVersion.fallback("-x") == "0.0.0-x")
  }

  test("synthetic untaged base 0.0.0 becomes 0.0.0-ci") {
    assert(DynverCiVersion.format(isCleanAfterTag = false, "0.0.0", "-ci") == "0.0.0-ci")
  }
end DynverCiVersionSpec
