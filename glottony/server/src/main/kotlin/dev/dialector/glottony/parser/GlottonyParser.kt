package dev.dialector.glottony.parser

import dev.dialector.glottony.ast.ArgumentList
import dev.dialector.glottony.ast.BinaryOperator
import dev.dialector.glottony.ast.BinaryOperators
import dev.dialector.glottony.ast.BlockExpression
import dev.dialector.glottony.ast.Expression
import dev.dialector.glottony.ast.File
import dev.dialector.glottony.ast.FunctionDeclaration
import dev.dialector.glottony.ast.FunctionType
import dev.dialector.glottony.ast.GType
import dev.dialector.glottony.ast.LambdaLiteral
import dev.dialector.glottony.ast.Parameter
import dev.dialector.glottony.ast.ParameterTypeDeclaration
import dev.dialector.glottony.ast.ReferenceExpression
import dev.dialector.glottony.ast.ReturnStatement
import dev.dialector.glottony.ast.Statement
import dev.dialector.glottony.ast.TopLevelConstruct
import dev.dialector.glottony.ast.ValStatement
import dev.dialector.glottony.ast.argument
import dev.dialector.glottony.ast.argumentList
import dev.dialector.glottony.ast.binaryExpression
import dev.dialector.glottony.ast.block
import dev.dialector.glottony.ast.blockExpression
import dev.dialector.glottony.ast.file
import dev.dialector.glottony.ast.functionDeclaration
import dev.dialector.glottony.ast.functionType
import dev.dialector.glottony.ast.integerLiteral
import dev.dialector.glottony.ast.integerType
import dev.dialector.glottony.ast.lambdaLiteral
import dev.dialector.glottony.ast.memberAccessExpression
import dev.dialector.glottony.ast.numberLiteral
import dev.dialector.glottony.ast.numberType
import dev.dialector.glottony.ast.parameter
import dev.dialector.glottony.ast.parameterTypeDeclaration
import dev.dialector.glottony.ast.referenceExpression
import dev.dialector.glottony.ast.returnStatement
import dev.dialector.glottony.ast.stringLiteral
import dev.dialector.glottony.ast.stringType
import dev.dialector.glottony.ast.structDeclaration
import dev.dialector.glottony.ast.valStatement
import dev.dialector.model.Node
import dev.dialector.model.nodeReference
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode
import java.nio.file.Path

data class SourceLocation(val line: Int, val character: Int)

operator fun SourceLocation.compareTo(other: SourceLocation): Int = when {
    other.line < line -> 1
    other.line > line -> -1
    other.character < character -> 1
    other.character > character -> -1
    else -> 0
}

data class SourceRange(val start: SourceLocation, val end: SourceLocation)

operator fun SourceRange.compareTo(other: SourceRange): Int {
    // If it starts first, it has lower priority
    val start = this.start.compareTo(other.start)
    return if (start != 0) {
        start
    } else {
        // If it starts at the same point and ends first, it has higher priority
        other.end.compareTo(this.end)
    }
}

interface SourceMap {
    fun getNodeAtLocation(location: SourceLocation): Node?

    fun getNodesInRange(range: SourceRange): List<Node>

    fun getRangeForNode(node: Node): SourceRange?
}

class SourceMapImpl(private val rangeMap: Map<Node, SourceRange>) : SourceMap {
    private val orderedRangeList: List<Pair<Node, SourceRange>> = rangeMap.entries.sortedWith { a, b ->
        a.value.compareTo(b.value)
    }.map { it.key to it.value }.toList()

    override fun getNodeAtLocation(location: SourceLocation): Node? =
        orderedRangeList.lastOrNull { it.second.contains(location) }?.first

    override fun getNodesInRange(range: SourceRange): List<Node> =
        orderedRangeList.filter { range.contains(it.second.start) && range.contains(it.second.end) }.map { it.first }

    override fun getRangeForNode(node: Node): SourceRange? = rangeMap[node]
}


fun SourceRange.contains(location: SourceLocation): Boolean =
    this.start <= location && this.end >= location

object GlottonyParser {
    fun parseFile(path: Path): File = parseFile(CharStreams.fromPath(path))

    fun parseString(string: String): File = parseFile(CharStreams.fromString(string))

    private fun parseFile(charStream: CharStream): File {
        val tokens = CommonTokenStream(GlottonyLexer(charStream))
        return ParserVisitor().visit(GlottonyGrammar(tokens).file()) as File
    }

    fun parseStringWithSourceMap(string: String): Pair<File, SourceMap> =
        parseFileWithSourceMap(CharStreams.fromString(string))

    private fun parseFileWithSourceMap(charStream: CharStream): Pair<File, SourceMap> {
        val tokens = CommonTokenStream(GlottonyLexer(charStream))
        val mappingVisitor = MappingVisitor()
        val result = mappingVisitor.visit(GlottonyGrammar(tokens).file()) as File
        return result to SourceMapImpl(mappingVisitor.sourceMapping.toMap())
    }
}

fun ParserRuleContext.toSourceRange(): SourceRange {
    val lineCount = this.stop.text.split('\n').count() - 1
    return SourceRange(
        SourceLocation(this.start.line-1, this.start.charPositionInLine),
        SourceLocation(
            this.stop.line-1 + lineCount,
            this.stop.charPositionInLine + this.stop.text.length - lineCount
        )
    )
}

class MappingVisitor : ParserVisitor() {
    val sourceMapping: MutableMap<Node, SourceRange> = mutableMapOf()

    override fun visit(tree: ParseTree?): Any? {
        val result = super.visit(tree)
        if (result is Node && tree is ParserRuleContext && !sourceMapping.contains(result)) {
            sourceMapping[result] = tree.toSourceRange()
        }
        return result
    }
}

open class ParserVisitor : GlottonyGrammarBaseVisitor<Any?>() {
    override fun visitFile(ctx: GlottonyGrammar.FileContext): File =
        file {
            contents += ctx.topLevelConstruct().map { visit(it) as TopLevelConstruct }
        }

    override fun visitTopLevelConstruct(ctx: GlottonyGrammar.TopLevelConstructContext): TopLevelConstruct =
        visit(ctx.getChild(0)) as TopLevelConstruct

    override fun visitStructDeclaration(ctx: GlottonyGrammar.StructDeclarationContext): Any? =
        structDeclaration {
            name = ctx.IDENTIFIER().text
        }

    override fun visitFieldDeclaration(ctx: GlottonyGrammar.FieldDeclarationContext): Any? {
        return super.visitFieldDeclaration(ctx)
    }

    override fun visitFunctionDeclaration(ctx: GlottonyGrammar.FunctionDeclarationContext): FunctionDeclaration =
        functionDeclaration {
            name = ctx.name.text
            parameters += visit(ctx.parameters) as List<Parameter>
            type = visit(ctx.returnType) as GType
            body = visit(ctx.body()) as Expression
        }

    override fun visitFunctionParameters(ctx: GlottonyGrammar.FunctionParametersContext): List<Parameter> {
        return ctx.parameterDeclaration().map { visit(it) as Parameter}
    }

    override fun visitParameterDeclaration(ctx: GlottonyGrammar.ParameterDeclarationContext): Parameter {
        return parameter {
            name = ctx.name.text
            type = visit(ctx.type()) as GType
        }
    }

    override fun visitBody(ctx: GlottonyGrammar.BodyContext): Expression {
        return visit(ctx.expression()) as Expression
    }

    override fun visitExpression(ctx: GlottonyGrammar.ExpressionContext): Expression {
        return visit(ctx.additiveExpression()) as Expression
    }

    override fun visitAdditiveExpression(ctx: GlottonyGrammar.AdditiveExpressionContext): Expression {
        val left = visit(ctx.multiplicativeExpression(0)) as Expression

        val ops = ctx.multiplicativeExpression().drop(1).zip(ctx.addOperator())

        return ops.fold(left, { accum, (right, op) -> binaryExpression {
            this.left = accum
            operator = visit(op) as BinaryOperator
            this.right = visit(right) as Expression
        } })
    }

    override fun visitMultiplicativeExpression(ctx: GlottonyGrammar.MultiplicativeExpressionContext): Expression {
        val left = visit(ctx.callExpression(0)) as Expression

        val ops = ctx.callExpression().drop(1).zip(ctx.multiplyOperator())

        return ops.fold(left, { accum, (right, op) -> binaryExpression {
            this.left = accum
            operator = visit(op) as BinaryOperator
            this.right = visit(right) as Expression
        } })
    }

    override fun visitCallExpression(ctx: GlottonyGrammar.CallExpressionContext): Expression {
        return visit(ctx.memberAccessExpression()) as Expression
    }

    override fun visitPrimaryExpression(ctx: GlottonyGrammar.PrimaryExpressionContext): Expression {
        return visit(ctx.getChild(0)) as Expression
    }

    override fun visitBlock(ctx: GlottonyGrammar.BlockContext): BlockExpression = blockExpression {
        block = block {
            statements += ctx.statement().map { visit(it) as Statement }
        }
    }

    override fun visitStatement(ctx: GlottonyGrammar.StatementContext): Statement =
        visit(ctx.getChild(0)) as Statement

    override fun visitValStatement(ctx: GlottonyGrammar.ValStatementContext): ValStatement = valStatement {
        name = ctx.name.text
        type = if (ctx.type() != null && !ctx.type().isEmpty) visit(ctx.type()) as GType else null
        expression = visit(ctx.expression()) as Expression
    }

    override fun visitReturnStatement(ctx: GlottonyGrammar.ReturnStatementContext): ReturnStatement = returnStatement {
         expression = visit(ctx.expression()) as Expression
    }

    override fun visitLambdaLiteral(ctx: GlottonyGrammar.LambdaLiteralContext): LambdaLiteral = lambdaLiteral {
        parameters += visit(ctx.lambdaParameters()) as List<Parameter>
    }

    override fun visitLambdaParameters(ctx: GlottonyGrammar.LambdaParametersContext): List<Parameter> =
        ctx.parameterDeclaration().map { visit(it) as Parameter }

    override fun visitArgumentList(ctx: GlottonyGrammar.ArgumentListContext): ArgumentList = argumentList {
        arguments += ctx.expression().map { argument { value = visit(it) as Expression }}
    }

    override fun visitNumberLiteral(ctx: GlottonyGrammar.NumberLiteralContext): Expression {
        return numberLiteral { value = ctx.NUMBER().text }
    }

    override fun visitIntegerLiteral(ctx: GlottonyGrammar.IntegerLiteralContext): Expression {
        return integerLiteral { value = ctx.INTEGER().text }
    }

    override fun visitStringLiteral(ctx: GlottonyGrammar.StringLiteralContext): Expression {
        return stringLiteral { value = ctx.STRING().text.drop(1).dropLast(1) }
    }

    override fun visitAddOperator(ctx: GlottonyGrammar.AddOperatorContext): BinaryOperator =
        if (ctx.PLUS() != null)
            BinaryOperators.Plus
        else if (ctx.MINUS() != null)
            BinaryOperators.Minus
        else
            throw RuntimeException("Unexpected operator: $ctx")

    override fun visitMultiplyOperator(ctx: GlottonyGrammar.MultiplyOperatorContext): BinaryOperator =
        if (ctx.MUL() != null)
            BinaryOperators.Multiply
        else if (ctx.DIV() != null)
            BinaryOperators.Divide
        else
            throw RuntimeException("Unexpected operator: $ctx")

    override fun visitType(ctx: GlottonyGrammar.TypeContext): GType {
        return if (ctx.getChild(0) is TerminalNode) {
            when ((ctx.getChild(0) as TerminalNode).symbol.type) {
                GlottonyGrammar.INTEGER_TYPE -> integerType {}
                GlottonyGrammar.NUMBER_TYPE -> numberType {}
                GlottonyGrammar.STRING_TYPE -> stringType {}
                else -> throw RuntimeException("Can not resolve type:$ctx")
            }
        } else visit(ctx.getChild(0)) as GType
    }

    override fun visitIdentifierExpression(ctx: GlottonyGrammar.IdentifierExpressionContext): ReferenceExpression = referenceExpression {
        target = nodeReference(ctx.referent.text)
    }

    override fun visitSimpleIdentifier(ctx: GlottonyGrammar.SimpleIdentifierContext?): Any {
        throw RuntimeException("Simple identifier not implemented")
    }

    override fun visitIdentifier(ctx: GlottonyGrammar.IdentifierContext?): Any {
        throw RuntimeException("Identifier not implemented")
    }

    override fun visitMemberAccessExpression(ctx: GlottonyGrammar.MemberAccessExpressionContext): Expression =
        if (ctx.identifier() == null) {
            visit(ctx.primaryExpression()) as Expression
        } else {
            memberAccessExpression {
                context = visit(ctx.primaryExpression()) as Expression
                member = nodeReference(ctx.identifier().text)
            }
        }

    override fun visitFunctionTypeParameterList(ctx: GlottonyGrammar.FunctionTypeParameterListContext): List<ParameterTypeDeclaration> =
        ctx.functionTypeParameterDefinition().map { visit(it) as ParameterTypeDeclaration }

    override fun visitFunctionTypeParameterDefinition(ctx: GlottonyGrammar.FunctionTypeParameterDefinitionContext): ParameterTypeDeclaration =
        parameterTypeDeclaration {
            name = ctx.name?.text
            type = visit(ctx.type()) as GType
        }

    override fun visitFunctionType(ctx: GlottonyGrammar.FunctionTypeContext): FunctionType = functionType {
        parameterTypes += visit(ctx.functionTypeParameterList()) as List<ParameterTypeDeclaration>
        returnType = visit(ctx.returnType) as GType
    }

    override fun visitTypeConstructor(ctx: GlottonyGrammar.TypeConstructorContext?): Any? {
        return super.visitTypeConstructor(ctx)
    }

    override fun visitTypeParameterList(ctx: GlottonyGrammar.TypeParameterListContext?): Any? {
        return super.visitTypeParameterList(ctx)
    }

    override fun visitTypeParameterDeclaration(ctx: GlottonyGrammar.TypeParameterDeclarationContext?): Any? {
        return super.visitTypeParameterDeclaration(ctx)
    }
}