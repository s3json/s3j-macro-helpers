package s3j.internal.macros

import s3j.internal.macros.ClassGenerator.FieldHandle

import scala.quoted.{Expr, Quotes, Type}

private[s3j] object ClassGenerator {
  abstract class FieldHandle[T] {
    given staticType: Type[T]

    /** @return A symbol for the field */
    def symbol(using q: Quotes): q.reflect.Symbol

    /** @return A nested [[Quotes]] object for code splicing */
    def nestedQuotes: Quotes

    /** @return An expression to reference this field */
    def reference(using q: Quotes): Expr[T]

    /** @return An expression to reference this field from the [[ClassGenerator.build]] call argument */
    def externalReference(using q: Quotes): Expr[T]

    /** Set an initializer code block */
    def setInitializer(expr: Expr[T]): Unit

    /**
     * Set the position of this field in the generated class.
     * Fields with negative positions are excluded from generation.
     */
    def setPosition(pos: Int): Unit
  }

  /** Create new instance of the class generator. Symbols will be owned by the provided quote object. */
  def apply(className: String)(using Quotes): ClassGenerator =
    CompilerBridge.current.classGenerator(className)
}

/**
 * An incremental class generator, which allows classes to be built in more freeform ways than it is normally possible
 * with official macro API.
 */
private[s3j] trait ClassGenerator {
  /** @return Symbol for the defined class */
  def classSymbol(using q: Quotes): q.reflect.Symbol

  /** @return Symbol for the variable where created instance is stored */
  def resultSymbol(using q: Quotes): q.reflect.Symbol

  /**
   * Define a new field. Field is immediately assigned a symbol and so could be referenced.
   * Actual initializer for the field must be provided via returned `FieldHandle` object.
   */
  def defineField[T](name: String)(using Type[T]): FieldHandle[T]

  /**
   * Build the class and return an expression that constructs an instance of it. Use [[FieldHandle.externalReference]]
   * method to obtain references to the fields in this class.
   */
  def build[R](resultExpr: Expr[R])(using Quotes, Type[R]): Expr[R]
}
