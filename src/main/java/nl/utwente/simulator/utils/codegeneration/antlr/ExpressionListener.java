package nl.utwente.simulator.utils.codegeneration.antlr;// Generated from E:/Google Drive/Final Project/simulator/src/main/resources\Expression.g4 by ANTLR 4.7
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ExpressionParser}.
 */
public interface ExpressionListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(ExpressionParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(ExpressionParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#additiveExpression}.
	 * @param ctx the parse tree
	 */
	void enterAdditiveExpression(ExpressionParser.AdditiveExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#additiveExpression}.
	 * @param ctx the parse tree
	 */
	void exitAdditiveExpression(ExpressionParser.AdditiveExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicativeExpression(ExpressionParser.MultiplicativeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicativeExpression(ExpressionParser.MultiplicativeExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#powerExpression}.
	 * @param ctx the parse tree
	 */
	void enterPowerExpression(ExpressionParser.PowerExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#powerExpression}.
	 * @param ctx the parse tree
	 */
	void exitPowerExpression(ExpressionParser.PowerExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpression(ExpressionParser.UnaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpression(ExpressionParser.UnaryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryExpression(ExpressionParser.PrimaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryExpression(ExpressionParser.PrimaryExpressionContext ctx);
}