package rocks.earlyeffect.sbt.dynverci.docs

import specular.*
import specular.ziotest.DocTestInterpreter
import zio.test.*

object OverviewSpec extends ZIOSpecDefault:
  def spec =
    DocTestInterpreter.specOf(Overview).provideLayer(ExampleRunner.live)
end OverviewSpec
