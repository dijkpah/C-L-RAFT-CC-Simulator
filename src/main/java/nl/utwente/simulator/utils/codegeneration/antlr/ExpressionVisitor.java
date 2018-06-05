package nl.utwente.simulator.utils.codegeneration.antlr;// Generated from E:/Google Drive/Final Project/simulator/src/main/resources\Expression.g4 by ANTLR 4.7
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link ExpressionParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface ExpressionVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(ExpressionParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#additiveExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditiveExpression(ExpressionParser.AdditiveExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplicativeExpression(ExpressionParser.MultiplicativeExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#powerExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPowerExpression(ExpressionParser.PowerExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#unaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryExpression(ExpressionParser.UnaryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryExpression(ExpressionParser.PrimaryExpressionContext ctx);
}