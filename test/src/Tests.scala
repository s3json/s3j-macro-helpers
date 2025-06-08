package s3j.test

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Tests extends AnyFlatSpec with Matchers {
  it should "flush quotes cache" in {
    Macros.flushQuotesCache()
  }

  it should "build classes via ClassGenerator" in {
    Macros.testClassGenerator() shouldBe 6
  }

  it should "parse macro settings" in {
    Macros.macroSettings() shouldBe Seq("hello-world")
  }

  it should "dealias keeping annotations" in {
    type Test1 = Int @annot1
    type Test2 = Test1 @annot2

    Macros.dealias[Test2] shouldBe "scala.Int @s3j.test.annot1 @s3j.test.annot2"
  }

  it should "resolve implicits" in {
    Macros.findImplicit[CanMeow[Foo]] shouldBe "s3j.test.CanMeow.fooCanMeow"
    Macros.findImplicit[CanMeow[Bar]] shouldBe "s3j.test.ImplicitHelpers.ExtraLocation.barCanMeow"
    Macros.findImplicit[CanMeow[Moo]] shouldBe "s3j.test.ImplicitHelpers.Assisted.assistedMeow[s3j.test.Moo]"

    val iterable = Macros.findImplicit[CanMeow[Seq[Moo]]]
    iterable should include("s3j.test.CanMeow.iterableCanMeow")
    iterable should include("s3j.test.ImplicitHelpers.Assisted.assistedMeow")
  }
}
