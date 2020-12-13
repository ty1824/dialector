package dev.dialector.glottony.ast

import dev.dialector.model.Child
import dev.dialector.model.Node
import dev.dialector.model.NodeDefinition
import dev.dialector.model.NodeReference
import dev.dialector.model.Property
import dev.dialector.model.Reference

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
    val type: Type

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
    val type: Type
}

@NodeDefinition
interface Type : Node

@NodeDefinition
interface IntegerType : Type

@NodeDefinition
interface NumberType : Type

@NodeDefinition
interface StringType : Type

@NodeDefinition
interface StructType : Type {
    @Reference
    val ofStruct: NodeReference<StructDeclaration>
}

interface Expression : Node

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