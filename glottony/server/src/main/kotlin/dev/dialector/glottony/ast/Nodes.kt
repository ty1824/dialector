package dev.dialector.glottony.ast

import dev.dialector.model.Child
import dev.dialector.model.Node
import dev.dialector.model.NodeDefinition
import dev.dialector.model.NodeReference
import dev.dialector.model.Property
import dev.dialector.model.Reference
import dev.dialector.typesystem.Type

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
interface FunctionDeclaration : TopLevelConstruct {
    @Property
    val name: String

    @Child
    val parameters: ParameterList

    @Child
    val type: GType

    @Child
    val body: Expression
}

@NodeDefinition
interface ParameterList : Node {
    @Child
    val parameters: List<Parameter>
}

@NodeDefinition
interface Parameter : Node {
    @Property
    val name: String

    @Child
    val type: GType
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

interface DotTarget : Node

@NodeDefinition
interface DotExpression : Expression {
    @Child
    val context: Expression

    @Child
    val target: DotTarget
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