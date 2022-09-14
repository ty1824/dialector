package dev.dialector.glottony.ast

import dev.dialector.syntax.Child
import dev.dialector.syntax.Node
import dev.dialector.syntax.NodeDefinition
import dev.dialector.syntax.NodeReference
import dev.dialector.syntax.Property
import dev.dialector.syntax.Reference

@NodeDefinition
interface TopLevelConstruct : Node

@NodeDefinition
interface File : Node {
    @Child
    val contents: List<TopLevelConstruct>
}

@NodeDefinition
interface StructDeclaration : TopLevelConstruct {
    @Property
    val name: String
    /* TODO: Add more */
}

@NodeDefinition
interface StructField : Node {
    @Property
    val name: String

    @Child
    val type: GType
}

@NodeDefinition
interface FunctionDeclaration : TopLevelConstruct {
    @Property
    val name: String

    @Child
    val parameters: List<Parameter>

    @Child
    val type: GType

    @Child
    val body: Expression
}

@NodeDefinition
interface Parameter : Node {
    @Property
    val name: String

    @Child
    val type: GType?
}

@NodeDefinition
interface GType : Node

@NodeDefinition
interface IntegerType : GType

@NodeDefinition
interface NumberType : GType

@NodeDefinition
interface StringType : GType

@NodeDefinition
interface StructType : GType {
    @Reference
    val ofStruct: NodeReference<StructDeclaration>
}

@NodeDefinition
interface ParameterTypeDeclaration : Node {
    @Property
    val name: String?

    @Child
    val type: GType
}

@NodeDefinition
interface FunctionType : GType {
    @Child
    val parameterTypes: List<ParameterTypeDeclaration>

    @Child
    val returnType: GType
}

interface Expression : Node

/**
 * Represents a Statement inside a [Block]
 */
interface Statement : Node

@NodeDefinition
interface Block : Node {
    @Child
    val statements: List<Statement>
}

@NodeDefinition
interface ValStatement : Statement {
    @Property
    val name: String

    @Child
    val expression: Expression

    @Child
    val type: GType?
}

@NodeDefinition
interface ReturnStatement : Statement {
    @Child
    val expression: Expression
}

@NodeDefinition
interface BlockExpression : Expression {
    @Child
    val block: Block
}

abstract class BinaryOperator(val symbol: String)

object BinaryOperators {
    object Plus : BinaryOperator("+")
    object Minus : BinaryOperator("-")
    object Multiply : BinaryOperator("*")
    object Divide : BinaryOperator("/")
}

@NodeDefinition
interface BinaryExpression : Expression {
    @Child
    val left: Expression

    @Property
    val operator: BinaryOperator

    @Child
    val right: Expression
}

@NodeDefinition
interface ArgumentList : Node {
    @Child
    val arguments: List<Argument>
}

@NodeDefinition
interface Argument : Node {
    @Child
    val value: Expression
}

@NodeDefinition
interface FunctionCall : Expression {
    @Child
    val functionExpression: Expression

    @Child
    val argumentList: ArgumentList
}

@NodeDefinition
interface MemberAccessExpression : Expression {
    @Child
    val context: Expression

    @Reference
    val member: NodeReference<Node>
}

@NodeDefinition
interface ReferenceExpression : Expression {
    @Reference
    val target: NodeReference<Node>
}

interface Literal : Expression

@NodeDefinition
interface IntegerLiteral : Literal {
    @Property
    val value: String
}


@NodeDefinition
interface NumberLiteral : Literal {
    @Property
    val value: String
}


@NodeDefinition
interface StringLiteral : Literal {
    @Property
    val value: String
}

@NodeDefinition
interface LambdaLiteral : Expression {
    @Child
    val parameters: List<Parameter>

    @Child
    val body: Expression

}