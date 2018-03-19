package cop5556sp18;

import cop5556sp18.AST.*;
import cop5556sp18.Scanner.Token;
import cop5556sp18.Scanner.Kind;
import cop5556sp18.Types.Type;

public class TypeChecker implements ASTVisitor {


	TypeChecker() {
	}

	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}

	
	
	// Name is only used for naming the output file. 
	// Visit the child block to type check program.
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		program.block.visit(this, arg);
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// enter scope
		for(ASTNode astNode : block.decsOrStatements){
			if(astNode instanceof Declaration || astNode instanceof Statement){
				astNode.visit(this,arg);
			}else{
				throw new SemanticException(astNode.firstToken,"Not a statement or declaration ");
			}
		}
		//leave scope
		return null;
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg) throws Exception {
		if(Types.getType(declaration.type) == null){
			throw new SemanticException(declaration.firstToken,"Invalid Type");
		}
		// check declaration.name in symbol table
		if(declaration.width != null){
			declaration.width.visit(this,arg);
			declaration.height.visit(this,arg);
		}
		return null;
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws Exception {
		//check statementInput.destName on symbol table
		statementInput.e.visit(this,arg);
		return null;
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		Kind e1 = (Kind) expressionBinary.leftExpression.visit(this,arg);
		Kind e2 = (Kind)expressionBinary.rightExpression.visit(this,arg);
		Kind op = expressionBinary.op;
		if(op == Kind.OP_AND || op == Kind.OP_OR || op == Kind.OP_EQ ||  op == Kind.OP_NEQ ||
				op == Kind.OP_GT ||  op == Kind.OP_GE ||  op == Kind.OP_LT ||  op == Kind.OP_LE){
			if(e1 == e2){
				return e1;
			}else{
				throw new SemanticException(expressionBinary.rightExpression.firstToken,"Invalid Expression");
			}
		}else if (op == Kind.OP_PLUS || op == Kind.OP_MINUS || op == Kind.OP_TIMES || op == Kind.OP_DIV || op == Kind.OP_POWER){
			if(e1 == e2){
				return e1;
			}else if( e1 == Kind.KW_float || e2 == Kind.KW_float){
				return Kind.KW_float;
			}else{
				throw new SemanticException(expressionBinary.rightExpression.firstToken,"Invalid Expression");
			}
		}else if( op == Kind.OP_AND || op == Kind.OP_OR){
			if( e1 == e2 && (e1 == Kind.KW_boolean || e1 == Kind.KW_int)){
				return e1;
			}else{
				throw new SemanticException(expressionBinary.rightExpression.firstToken,"Invalid Expression");
			}
		}
		return Kind.NONE;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		return expressionUnary.firstToken.kind;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		return Kind.KW_int;
	}

	@Override
	public Object visitBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		return Kind.KW_boolean;
	}

	@Override
	public Object visitExpressionPredefinedName(ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		return Kind.KW_float;
	}

	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionPixelConstructor(ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg) throws Exception {
		statementShow.e.visit(this,arg);
		return null;
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


}
