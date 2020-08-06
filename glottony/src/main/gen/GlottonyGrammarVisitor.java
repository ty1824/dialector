// Generated from C:/Big Drive/development/dialector/glottony/src/main/antlr\GlottonyGrammar.g4 by ANTLR 4.8
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link GlottonyGrammar}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface GlottonyGrammarVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link GlottonyGrammar#file}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFile(GlottonyGrammar.FileContext ctx);
	/**
	 * Visit a parse tree produced by {@link GlottonyGrammar#functionDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionDeclaration(GlottonyGrammar.FunctionDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link GlottonyGrammar#parameterDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterDeclaration(GlottonyGrammar.ParameterDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link GlottonyGrammar#body}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBody(GlottonyGrammar.BodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link GlottonyGrammar#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(GlottonyGrammar.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GlottonyGrammar#addExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddExpression(GlottonyGrammar.AddExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GlottonyGrammar#multiplyExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplyExpression(GlottonyGrammar.MultiplyExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GlottonyGrammar#literalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteralExpression(GlottonyGrammar.LiteralExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GlottonyGrammar#addOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddOperator(GlottonyGrammar.AddOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link GlottonyGrammar#multiplyOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplyOperator(GlottonyGrammar.MultiplyOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link GlottonyGrammar#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType(GlottonyGrammar.TypeContext ctx);
}