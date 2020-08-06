// Generated from C:/Big Drive/development/dialector/glottony/src/main/antlr\GlottonyGrammar.g4 by ANTLR 4.8
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link GlottonyGrammar}.
 */
public interface GlottonyGrammarListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link GlottonyGrammar#file}.
	 * @param ctx the parse tree
	 */
	void enterFile(GlottonyGrammar.FileContext ctx);
	/**
	 * Exit a parse tree produced by {@link GlottonyGrammar#file}.
	 * @param ctx the parse tree
	 */
	void exitFile(GlottonyGrammar.FileContext ctx);
	/**
	 * Enter a parse tree produced by {@link GlottonyGrammar#functionDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDeclaration(GlottonyGrammar.FunctionDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link GlottonyGrammar#functionDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDeclaration(GlottonyGrammar.FunctionDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link GlottonyGrammar#parameterDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterParameterDeclaration(GlottonyGrammar.ParameterDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link GlottonyGrammar#parameterDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitParameterDeclaration(GlottonyGrammar.ParameterDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link GlottonyGrammar#body}.
	 * @param ctx the parse tree
	 */
	void enterBody(GlottonyGrammar.BodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link GlottonyGrammar#body}.
	 * @param ctx the parse tree
	 */
	void exitBody(GlottonyGrammar.BodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link GlottonyGrammar#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(GlottonyGrammar.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link GlottonyGrammar#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(GlottonyGrammar.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link GlottonyGrammar#addExpression}.
	 * @param ctx the parse tree
	 */
	void enterAddExpression(GlottonyGrammar.AddExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link GlottonyGrammar#addExpression}.
	 * @param ctx the parse tree
	 */
	void exitAddExpression(GlottonyGrammar.AddExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link GlottonyGrammar#multiplyExpression}.
	 * @param ctx the parse tree
	 */
	void enterMultiplyExpression(GlottonyGrammar.MultiplyExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link GlottonyGrammar#multiplyExpression}.
	 * @param ctx the parse tree
	 */
	void exitMultiplyExpression(GlottonyGrammar.MultiplyExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link GlottonyGrammar#literalExpression}.
	 * @param ctx the parse tree
	 */
	void enterLiteralExpression(GlottonyGrammar.LiteralExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link GlottonyGrammar#literalExpression}.
	 * @param ctx the parse tree
	 */
	void exitLiteralExpression(GlottonyGrammar.LiteralExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link GlottonyGrammar#addOperator}.
	 * @param ctx the parse tree
	 */
	void enterAddOperator(GlottonyGrammar.AddOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link GlottonyGrammar#addOperator}.
	 * @param ctx the parse tree
	 */
	void exitAddOperator(GlottonyGrammar.AddOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link GlottonyGrammar#multiplyOperator}.
	 * @param ctx the parse tree
	 */
	void enterMultiplyOperator(GlottonyGrammar.MultiplyOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link GlottonyGrammar#multiplyOperator}.
	 * @param ctx the parse tree
	 */
	void exitMultiplyOperator(GlottonyGrammar.MultiplyOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link GlottonyGrammar#type}.
	 * @param ctx the parse tree
	 */
	void enterType(GlottonyGrammar.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link GlottonyGrammar#type}.
	 * @param ctx the parse tree
	 */
	void exitType(GlottonyGrammar.TypeContext ctx);
}