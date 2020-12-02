package dev.dialector.glottony.parser

import dev.dialector.glottony.ast.File
import dev.dialector.glottony.ast.FunctionDeclaration
import dev.dialector.glottony.ast.Parameter
import dev.dialector.glottony.ast.ParameterList
import dev.dialector.glottony.ast.TopLevelConstruct
import dev.dialector.glottony.ast.Type
import dev.dialector.glottony.ast.file
import dev.dialector.glottony.ast.functionDeclaration
import dev.dialector.glottony.ast.integerType
import dev.dialector.glottony.ast.numberType
import dev.dialector.glottony.ast.parameter
import dev.dialector.glottony.ast.parameterList
import dev.dialector.glottony.ast.stringType
import dev.dialector.model.Node
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.TerminalNode
import java.nio.file.Path

object GlottonyParser {
    fun parseFile(path: Path): File = parseFile(CharStreams.fromPath(path))


    fun parseFile(string: String): File = parseFile(CharStreams.fromString(string))


    private fun parseFile(charStream: CharStream): File {
        val tokens = CommonTokenStream(GlottonyLexer(charStream))
        val grammar = GlottonyGrammar(tokens)
        return ParserVisitor().visit(GlottonyGrammar(tokens).file()) as File
    }
}

class ParserVisitor : GlottonyGrammarBaseVisitor<Node>() {
    override fun visitFile(ctx: GlottonyGrammar.FileContext): File =
        file {
            contents += ctx.topLevelConstruct().map { visit(it) as TopLevelConstruct }
        }

    override fun visitTopLevelConstruct(ctx: GlottonyGrammar.TopLevelConstructContext): TopLevelConstruct =
        visit(ctx.getChild(0)) as TopLevelConstruct

    override fun visitFunctionDeclaration(ctx: GlottonyGrammar.FunctionDeclarationContext): FunctionDeclaration =
        functionDeclaration {
            name = ctx.IDENTIFIER().text
            parameters = visit(ctx.functionParameters()) as ParameterList
            type = visit(ctx.type()) as Type
            // TODO parse dis plz
            body = ctx.body().text

        }

    override fun visitFunctionParameters(ctx: GlottonyGrammar.FunctionParametersContext): ParameterList {
        return parameterList {
            parameters += ctx.parameterDeclaration().map { visit(it) as Parameter}
        }
    }

    override fun visitParameterDeclaration(ctx: GlottonyGrammar.ParameterDeclarationContext): Parameter {
        return parameter {
            name = ctx.IDENTIFIER().text
            type = visit(ctx.type()) as Type
        }
    }

    override fun visitBody(ctx: GlottonyGrammar.BodyContext): Node {
        TODO("Not implemented yet")
    }

    override fun visitExpression(ctx: GlottonyGrammar.ExpressionContext): Node {
        return super.visitExpression(ctx)
    }

    override fun visitStructDeclaration(ctx: GlottonyGrammar.StructDeclarationContext): Node {
        return super.visitStructDeclaration(ctx)
    }

    override fun visitFieldDeclaration(ctx: GlottonyGrammar.FieldDeclarationContext): Node {
        return super.visitFieldDeclaration(ctx)
    }

    override fun visitAdditiveExpression(ctx: GlottonyGrammar.AdditiveExpressionContext?): Node {
        return super.visitAdditiveExpression(ctx)
    }

    override fun visitMultiplicativeExpression(ctx: GlottonyGrammar.MultiplicativeExpressionContext?): Node {
        return super.visitMultiplicativeExpression(ctx)
    }

    override fun visitCallExpression(ctx: GlottonyGrammar.CallExpressionContext?): Node {
        return super.visitCallExpression(ctx)
    }

    override fun visitPrimaryExpression(ctx: GlottonyGrammar.PrimaryExpressionContext?): Node {
        return super.visitPrimaryExpression(ctx)
    }

    override fun visitBlock(ctx: GlottonyGrammar.BlockContext?): Node {
        return super.visitBlock(ctx)
    }

    override fun visitLambdaLiteral(ctx: GlottonyGrammar.LambdaLiteralContext?): Node {
        return super.visitLambdaLiteral(ctx)
    }

    override fun visitLambdaParameters(ctx: GlottonyGrammar.LambdaParametersContext?): Node {
        return super.visitLambdaParameters(ctx)
    }

    override fun visitArgumentList(ctx: GlottonyGrammar.ArgumentListContext?): Node {
        return super.visitArgumentList(ctx)
    }

    override fun visitNumberLiteral(ctx: GlottonyGrammar.NumberLiteralContext?): Node {
        return super.visitNumberLiteral(ctx)
    }

    override fun visitIntegerLiteral(ctx: GlottonyGrammar.IntegerLiteralContext?): Node {
        return super.visitIntegerLiteral(ctx)
    }

    override fun visitStringLiteral(ctx: GlottonyGrammar.StringLiteralContext?): Node {
        return super.visitStringLiteral(ctx)
    }

    override fun visitAddOperator(ctx: GlottonyGrammar.AddOperatorContext?): Node {
        return super.visitAddOperator(ctx)
    }

    override fun visitMultiplyOperator(ctx: GlottonyGrammar.MultiplyOperatorContext?): Node {
        return super.visitMultiplyOperator(ctx)
    }

    override fun visitType(ctx: GlottonyGrammar.TypeContext): Type {
        return if (ctx.getChild(0) is TerminalNode) {
            when ((ctx.getChild(0) as TerminalNode).symbol.type) {
                GlottonyGrammar.INTEGER_TYPE -> integerType {}
                GlottonyGrammar.NUMBER_TYPE -> numberType {}
                GlottonyGrammar.STRING_TYPE -> stringType {}
                else -> throw RuntimeException("Can not resolve type:$ctx")
            }
        } else visit(ctx.getChild(0)) as Type
    }

    override fun visitSimpleIdentifier(ctx: GlottonyGrammar.SimpleIdentifierContext?): Node {
        return super.visitSimpleIdentifier(ctx)
    }

    override fun visitIdentifier(ctx: GlottonyGrammar.IdentifierContext?): Node {
        throw RuntimeException("Identifier not implemented")
    }


}