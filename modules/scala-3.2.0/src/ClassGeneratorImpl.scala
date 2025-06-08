package s3j.internal.scala3_2_0

import s3j.internal.macros.ClassGenerator
import s3j.internal.macros.ClassGenerator.FieldHandle

import scala.collection.mutable
import scala.quoted.{Expr, Quotes, Type}

private class ClassGeneratorImpl(className: String)(using q: Quotes, ci: ClassGeneratorOps)
extends ClassGenerator {
  import q.reflect.{*, given}

  private final class Field[T](val name: String)(using val staticType: Type[T]) extends FieldHandle[T] {
    val _typeRepr: TypeRepr = TypeRepr.of[T]
    val _symbol: Symbol = Symbol.newVal(_classSymbol, name, _typeRepr, Flags.Final, Symbol.noSymbol)
    var _init: Option[Term] = None
    var position: Int = 0

    override def symbol(using qx: Quotes): qx.reflect.Symbol = _symbol.asInstanceOf[qx.reflect.Symbol]
    override def nestedQuotes: Quotes = _symbol.asQuotes
    override def setPosition(pos: Int): Unit = position = pos
    override def setInitializer(expr: Expr[T]): Unit = _init = Some(expr.asTerm.changeOwner(symbol))

    override def reference(using q: Quotes): Expr[T] = {
      import q.reflect.*
      Ref(symbol).asExprOf[T]
    }

    override def externalReference(using q: Quotes): Expr[T] = {
      import q.reflect.*
      Typed(Select(Ref(resultSymbol), symbol), TypeTree.of[T]).asExprOf[T]
    }

    override def toString: String = s"Field($name, ${TypeRepr.of[T].show})"
  }

  private val _fields: mutable.ArrayBuffer[Field[?]] = mutable.ArrayBuffer.empty
  private val _classSymbol: Symbol = ci.newClass(Symbol.spliceOwner, className)

  private val _resultSymbol: Symbol =
    Symbol.newVal(Symbol.spliceOwner, className + "$inst", classSymbol.typeRef, Flags.EmptyFlags, Symbol.noSymbol)

  inline def classSymbol(using qx: Quotes): qx.reflect.Symbol = _classSymbol.asInstanceOf[qx.reflect.Symbol]
  inline def resultSymbol(using qx: Quotes): qx.reflect.Symbol = _resultSymbol.asInstanceOf[qx.reflect.Symbol]

  def defineField[T](name: String)(using Type[T]): FieldHandle[T] = {
    val field = new Field(name)
    _fields += field
    field
  }

  def build[R](resultExpr: Expr[R])(using qx: Quotes, t: Type[R]): Expr[R] = {
    val spliceOwner = qx.reflect.Symbol.spliceOwner
    buildInner(spliceOwner.asInstanceOf[q.reflect.Symbol], resultExpr.asTerm).asExprOf[R]
  }

  private def buildInner(spliceOwner: Symbol, resultExpr: Term): Term = {
    if (spliceOwner != _classSymbol.owner) {
      throw new IllegalStateException("ClassGenerator.build() was called in a different context than it was created. " +
        "Splice owners should match between creation and building, otherwise a corrupted tree would be generated.")
    }

    val classBody = Vector.newBuilder[Statement]

    for (f <- _fields.toSeq.sortBy(_.position) if f.position >= 0) {
      if (f._init.isEmpty) {
        throw new IllegalStateException(s"Initializer is not defined for the field ${f.name}")
      }

      _classSymbol.enter(f._symbol)
      classBody += ValDef(f._symbol, f._init)
    }

    Block(
      List(
        ci.classDef(classSymbol, classBody.result().toList),
        ValDef(resultSymbol, Some(
          Typed(
            Apply(Select(New(TypeIdent(classSymbol)), classSymbol.primaryConstructor), Nil),
            TypeIdent(classSymbol)
          )
        )),
      ),
      resultExpr
    )
  }
}
