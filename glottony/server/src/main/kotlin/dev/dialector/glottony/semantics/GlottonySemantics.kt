package dev.dialector.glottony.semantics

import dev.dialector.glottony.ast.BinaryExpression
import dev.dialector.glottony.ast.BinaryOperators
import dev.dialector.glottony.ast.Block
import dev.dialector.glottony.ast.BlockExpression
import dev.dialector.glottony.ast.FunctionCall
import dev.dialector.glottony.ast.FunctionDeclaration
import dev.dialector.glottony.ast.IntegerLiteral
import dev.dialector.glottony.ast.LambdaLiteral
import dev.dialector.glottony.ast.NumberLiteral
import dev.dialector.glottony.ast.ReferenceExpression
import dev.dialector.glottony.ast.ReturnStatement
import dev.dialector.glottony.ast.StringLiteral
import dev.dialector.glottony.ast.ValStatement
import dev.dialector.glottony.typesystem.FunType
import dev.dialector.glottony.typesystem.IntType
import dev.dialector.glottony.typesystem.NumType
import dev.dialector.glottony.typesystem.ParameterType
import dev.dialector.glottony.typesystem.StrType
import dev.dialector.glottony.typesystem.asType
import dev.dialector.semantic.PropagationType
import dev.dialector.semantic.Scopes
import dev.dialector.semantic.SemanticRule
import dev.dialector.semantic.TypeScopes
import dev.dialector.semantic.Types
import dev.dialector.semantic.evaluateSemantics
import dev.dialector.semantic.SimpleNamespace
import dev.dialector.semantic.type.lattice.OrType
import dev.dialector.syntax.Node
import dev.dialector.syntax.given

val Unqualified = SimpleNamespace("unqualified")
val Declarations = SimpleNamespace("declarations")

object OutgoingScope : PropagationType

val semanticRules: List<SemanticRule<out Node>> = listOf(
    given<FunctionDeclaration>().evaluateSemantics("functionDeclaration") {
        val parentScope = receiveScope()
        constraint(Scopes) { parentScope.declare(Declarations, it, it.name) }
        val functionScope = scope("function")
        constraint(Scopes) { functionScope.inherit(parentScope, "parent") }
        it.parameters.forEach { parameter ->
            constraint(Scopes) { functionScope.declare(Declarations, parameter, parameter.name) }
            constraint(TypeScopes) { functionScope.declareTypeElement(Declarations, parameter.name, parameter.type!!.asType()) }
        }
        propagateScope(functionScope, it.body)
        constraint(Types) { typeOf(it.body) subtype it.type.asType() }
    },
    given<BlockExpression>().evaluateSemantics("blockExpression") {
        propagateScope(receiveScope(), it.block)
        constraint(Types) {
            typeOf(it) equal typeOf((it.block.statements.last { statement -> statement is ReturnStatement } as ReturnStatement).expression)
        }
    },
    given<Block>().evaluateSemantics("block") {
        // For each statement, propagate the incoming and outgoing scopes. Incoming scopes should
        // be used to perform name resolution within the statement whereas outgoing scopes include
        // new names visible to later statements
        var incomingScope = receiveScope()
        it.statements.forEach { statement ->
            val outgoingScope = scope("statementResult")
            constraint(Scopes) { outgoingScope.inherit(incomingScope, "parent") }
            propagateScope(incomingScope, statement)
            propagateScope(outgoingScope, statement, OutgoingScope)
            incomingScope = outgoingScope

        }
    },
    given<ValStatement>().evaluateSemantics("valStatement") {
        val parentScope = receiveScope()
        propagateScope(parentScope, it.expression)
        val expressionScope = scope("var")
        constraint(Scopes) { expressionScope.inherit(parentScope, "parent") }
        val explicitType = it.type
        val variableType = typeVar()
        if (explicitType != null) {
            constraint(Types) { typeOf(it.expression) subtype explicitType.asType() }
            constraint(Types) { variableType equal explicitType.asType() }
        } else {
            constraint(Types) { variableType equal typeOf(it.expression) }
        }

        constraint(Scopes) { receiveScope(OutgoingScope).declare(Declarations, it, it.name) }
        constraint(TypeScopes) { receiveScope(OutgoingScope).declareTypeElement(Declarations, it.name, variableType) }
    },
    given<ReferenceExpression>().evaluateSemantics("referenceExpression") {
        val scope = receiveScope()
        constraint(Scopes) { scope.reference(Declarations, it.target) }
        constraint(TypeScopes) { scope.referenceType(Declarations, it.target, typeOf(it)) }
    },
    given<StringLiteral>().evaluateSemantics("stringLiteral") {
        constraint(Types) { typeOf(it) equal StrType }
    },
    given<NumberLiteral>().evaluateSemantics("numberLiteral") {
        constraint(Types) { typeOf(it) equal NumType }
    },
    given<IntegerLiteral>().evaluateSemantics("integerLiteral") {
        constraint(Types) { typeOf(it) equal IntType }
    },
    given<BinaryExpression> { it.operator == BinaryOperators.Plus }.evaluateSemantics("plusExpression") {
        constraint(Types) { typeOf(it) supertype typeOf(it.left) }
        constraint(Types) { typeOf(it) supertype typeOf(it.right) }
        constraint(Types) { typeOf(it.left) subtype OrType(setOf(NumType, StrType)) }
        constraint(Types) { typeOf(it.right) subtype OrType(setOf(NumType, StrType)) }
    },
    given<BinaryExpression> { it.operator == BinaryOperators.Minus }.evaluateSemantics("minusExpression") {
        constraint(Types) { typeOf(it) supertype typeOf(it.left) }
        constraint(Types) { typeOf(it) supertype typeOf(it.right) }
        constraint(Types) { typeOf(it.left) subtype NumType }
        constraint(Types) { typeOf(it.right) subtype NumType }
    },
    given<BinaryExpression> { it.operator == BinaryOperators.Multiply }.evaluateSemantics("multiplyExpression") {
        constraint(Types) { typeOf(it) supertype typeOf(it.left) }
        constraint(Types) { typeOf(it) supertype typeOf(it.right) }
        constraint(Types) { typeOf(it.left) subtype NumType }
        constraint(Types) { typeOf(it.right) subtype NumType }
    },
    given<BinaryExpression> { it.operator == BinaryOperators.Divide }.evaluateSemantics("divideExpression") {
        constraint(Types) { typeOf(it) supertype typeOf(it.left) }
        constraint(Types) { typeOf(it) supertype typeOf(it.right) }
        constraint(Types) { typeOf(it.left) subtype NumType }
        constraint(Types) { typeOf(it.right) subtype NumType }
    },
    given<ReturnStatement>().evaluateSemantics("returnExpression") {
        constraint(Types) { typeOf(it) equal typeOf(it.expression) }
    },
    given<LambdaLiteral>().evaluateSemantics("lambdaLiteral") {
        val parentScope = receiveScope()
        val lambdaScope = scope("lambda")
        constraint(Scopes) { lambdaScope.inherit(parentScope, "lambdaContext")}

        it.parameters.forEach { parameter ->
            constraint(Scopes) { lambdaScope.declare(Declarations, parameter, parameter.name) }
            constraint(TypeScopes) { lambdaScope.declareTypeElement(Declarations, parameter.name, typeOf(parameter))}
        }
        propagateScope(lambdaScope, it.body)

        val parameterTypes = it.parameters.map {
            parameter -> ParameterType(typeOf(parameter), parameter.name)
        }
        constraint(Types) { typeOf(it) equal FunType(parameterTypes, typeOf(it.body)) }
    },
    given<FunctionCall>().evaluateSemantics("functionCall") {
        val callType = FunType(it.argumentList.arguments.map { arg -> ParameterType(typeOf(arg)) }, typeVar())
        constraint(Types) { typeOf(it.functionExpression) equal callType }

        constraint(Types) { typeOf(it) equal callType.returnType}
    }
)