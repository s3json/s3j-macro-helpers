package s3j.test

import scala.annotation.StaticAnnotation

case class annot1() extends StaticAnnotation
case class annot2() extends StaticAnnotation

// Implicit resolution testing:
class Foo
class Bar
class Moo
