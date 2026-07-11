package rocks.earlyeffect.sbt.dynverci.docs

import specular.*
import specular.ziotest.DocTestInterpreter
import zio.test.*

object UsageSpec extends ZIOSpecDefault:
  def spec =
    DocTestInterpreter.specOf(Usage).provideLayer(ExampleRunner.live)
end UsageSpec
