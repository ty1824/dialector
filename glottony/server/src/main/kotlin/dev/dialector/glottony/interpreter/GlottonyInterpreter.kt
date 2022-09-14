package dev.dialector.glottony.interpreter

import dev.dialector.glottony.ast.BinaryExpression
import dev.dialector.glottony.ast.BinaryOperator
import dev.dialector.glottony.ast.BinaryOperators
import dev.dialector.glottony.ast.Expression
import dev.dialector.glottony.ast.IntegerLiteral
import dev.dialector.glottony.ast.MemberAccessExpression
import dev.dialector.glottony.ast.NumberLiteral
import dev.dialector.glottony.ast.StringLiteral

interface ExpressionVisitor<C, R> {
    fun visit(expression: Expression, context: C): R

    fun visitBinaryExpression(expression: BinaryExpression, context: C): R

    fun visitMemberAccessExpression(expression: MemberAccessExpression, context: C): R

    fun visitIntegerLiteral(expression: IntegerLiteral, context: C): R

    fun visitNumberLiteral(expression: NumberLiteral, context: C): R

    fun visitStringLiteral(expression: StringLiteral, context: C): R
}

interface InterpreterContext

interface Overload<R> {
    fun predicate(left: R, right: R): Boolean
    fun evaluate(left: R, right: R): R
}

class OverloadImpl<R>(
    val operator: BinaryOperator,
    val leftPredicate: (R) -> Boolean,
    val rightPredicate: (R) -> Boolean,
    val evaluate: (R, R) -> R,
    val reversible: Boolean = true
) : Overload<R> {
    override fun predicate(left: R, right: R): Boolean =
        (leftPredicate(left) && rightPredicate(right)) ||
            (reversible && rightPredicate(left) && leftPredicate(right))

    override fun evaluate(left: R, right: R): R =
        this.evaluate.invoke(left, right)
}

private fun overload(
    operator: BinaryOperator,
    leftPredicate: (Any) -> Boolean,
    rightPredicate: (Any) -> Boolean,
    reversible: Boolean = true,
    evaluate: (Any, Any) -> Any,
): Overload<Any> = OverloadImpl(operator, leftPredicate, rightPredicate, evaluate, reversible)

object GlottonyInterpreter : ExpressionVisitor<InterpreterContext, Any> {
    private val binaryOverloads: List<Overload<Any>> = listOf(
        overload(BinaryOperators.Plus, { it is String }, { true }) { left, right ->
            left as String + right as String
        },
        overload(BinaryOperators.Plus, { it is Double }, { it is Double }, false) { left, right ->
            left as Double + right as Double
        },
        overload(BinaryOperators.Minus, { it is Double }, { it is Double }, false) { left, right ->
            left as Double - right as Double
        },
        overload(BinaryOperators.Multiply, { it is Double }, { it is Double }, false) { left, right ->
            left as Double * right as Double
        },
        overload(BinaryOperators.Divide, { it is Double }, { it is Double }, false) { left, right ->
            left as Double / right as Double
        },
        overload(BinaryOperators.Plus, { it is Int }, { it is Int }, false) { left, right ->
            left as Int + right as Int
        },
        overload(BinaryOperators.Minus, { it is Int }, { it is Int }, false) { left, right ->
            left as Int - right as Int
        },
        overload(BinaryOperators.Multiply, { it is Int }, { it is Int }, false) { left, right ->
            left as Int * right as Int
        },
        overload(BinaryOperators.Divide, { it is Int }, { it is Int }, false) { left, right ->
            left as Int / right as Int
        }
    )

    override fun visit(expression: Expression, context: InterpreterContext): Any {
        return when (expression) {
            is BinaryExpression -> visitBinaryExpression(expression, context)
            is MemberAccessExpression -> visitMemberAccessExpression(expression, context)
            is IntegerLiteral -> visitIntegerLiteral(expression, context)
            is NumberLiteral -> visitNumberLiteral(expression, context)
            is StringLiteral -> visitStringLiteral(expression, context)
            else -> throw RuntimeException("Unknown expression type: $expression")
        }
    }

    override fun visitBinaryExpression(expression: BinaryExpression, context: InterpreterContext): Any {
        val left = visit(expression.left, context)
        val right = visit(expression.right, context)

        return binaryOverloads.first { it.predicate(left, right) }.evaluate(left, right)
    }

    override fun visitMemberAccessExpression(expression: MemberAccessExpression, context: InterpreterContext): Any {
        TODO("Not yet implemented")
    }

    override fun visitIntegerLiteral(expression: IntegerLiteral, context: InterpreterContext): Any {
        return expression.value.toInt()
    }

    override fun visitNumberLiteral(expression: NumberLiteral, context: InterpreterContext): Any {
        return expression.value.toDouble()
    }

    override fun visitStringLiteral(expression: StringLiteral, context: InterpreterContext): Any {
        return expression.value
    }

}