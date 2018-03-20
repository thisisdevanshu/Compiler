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
			super(message +" "+ t.getText() +" at position "+t.pos);
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

	SymbolTable symbolTable = new SymbolTable();

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		symbolTable.enterScope();
		for(ASTNode astNode : block.decsOrStatements){
			if(astNode instanceof Declaration || astNode instanceof Statement){
				astNode.visit(this,arg);
			}else{
				throw new SemanticException(astNode.firstToken,"Not a statement or declaration ");
			}
		}
		symbolTable.leaveScope();
		return null;
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg) throws Exception {
		if(Types.getType(declaration.type) == null){
			throw new SemanticException(declaration.firstToken,"Invalid Type");
		}
		if(symbolTable.lookup(declaration.name) == null){
			symbolTable.insert(declaration.name,declaration);
		}else{
			if(symbolTable.getScope(declaration.name) != -1) {
				throw new SemanticException(declaration.firstToken, "Name Conflict");
			}else{
				symbolTable.insert(declaration.name,declaration);
			}
		}
		if(declaration.type == Kind.KW_image && declaration.width != null){
			Kind e1 = (Kind)declaration.width.visit(this,arg);
			Kind e2 = (Kind)declaration.height.visit(this,arg);
			if(e1 != Kind.KW_int || e2!=Kind.KW_int){
				throw new SemanticException(declaration.firstToken,"Invalid Type ");
			}
		}
		return null;
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg) throws Exception {
		Declaration source = symbolTable.lookup(statementWrite.sourceName);
		Declaration destination = symbolTable.lookup(statementWrite.destName);
		if(source == null ){
			throw new SemanticException(statementWrite.firstToken,"SourceName not defined");
		}else if(destination == null){
			throw new SemanticException(statementWrite.firstToken,"DestName not defined");
		}else if(source.type != Kind.KW_image){
			throw new SemanticException(statementWrite.firstToken,"SourceName must be of type image");
		}else if(destination.type != Kind.KW_filename){
			throw new SemanticException(statementWrite.firstToken,"DestName must be of type filename");
		}
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws Exception {
		//check statementInput.destName on symbol table
		if(symbolTable.lookup(statementInput.destName) == null){
			throw new SemanticException(statementInput.firstToken,"DestName not defined");
		}
		Kind e1 = (Kind)statementInput.e.visit(this,arg);
		if( e1 != Kind.KW_int){
			throw new SemanticException(statementInput.firstToken,"Invalid Expression");
		}
		return null;
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		Kind ex = (Kind) pixelSelector.ex.visit(this,arg);
		Kind ey = (Kind) pixelSelector.ey.visit(this,arg);
		if( ex != ey || (ex != Kind.KW_int || ex != Kind.KW_float) ){
			throw new SemanticException(pixelSelector.firstToken,"Invalid pixelSelector");
		}
		return null;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		Kind guard = (Kind)expressionConditional.guard.visit(this,arg);
		Kind trueExpression = (Kind)expressionConditional.trueExpression.visit(this,arg);
		Kind falseExpression = (Kind)expressionConditional.falseExpression.visit(this,arg);
		if( guard != Kind.KW_boolean || trueExpression!= falseExpression ){
			throw new SemanticException(expressionConditional.firstToken,"Invalid expressionConditional");
		}
		expressionConditional.type = Types.getType(trueExpression);
		return trueExpression;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		Kind e1 = (Kind) expressionBinary.leftExpression.visit(this,arg);
		Kind e2 = (Kind)expressionBinary.rightExpression.visit(this,arg);
		Kind op = expressionBinary.op;
		if(op == Kind.OP_AND || op == Kind.OP_OR || op == Kind.OP_EQ ||  op == Kind.OP_NEQ ||
				op == Kind.OP_GT ||  op == Kind.OP_GE ||  op == Kind.OP_LT ||  op == Kind.OP_LE){
			if(e1 == e2){
				expressionBinary.type = Types.getType(Kind.KW_boolean);
				return Kind.KW_boolean;
			}else{
				throw new SemanticException(expressionBinary.rightExpression.firstToken,"Invalid Expression");
			}
		}else if (op == Kind.OP_PLUS || op == Kind.OP_MINUS || op == Kind.OP_TIMES || op == Kind.OP_DIV || op == Kind.OP_POWER){
			if(e1 == e2){
				expressionBinary.type = Types.getType(e1);
				return e1;
			}else if( e1 == Kind.KW_float || e2 == Kind.KW_float){
				expressionBinary.type = Types.getType(Kind.KW_float);
				return Kind.KW_float;
			}else{
				throw new SemanticException(expressionBinary.rightExpression.firstToken,"Invalid Expression");
			}
		}else if( op == Kind.OP_AND || op == Kind.OP_OR){
			if( e1 == e2 && (e1 == Kind.KW_boolean || e1 == Kind.KW_int)){
				expressionBinary.type = Types.getType(Kind.KW_float);
				return Kind.KW_boolean;
			}else{
				throw new SemanticException(expressionBinary.rightExpression.firstToken,"Invalid Expression");
			}
		}else if(op == Kind.OP_MOD){
			if( e1 == Kind.KW_int && e2 == Kind.KW_int){
				expressionBinary.type = Types.getType(Kind.KW_int);
				return Kind.KW_int;
			}else{
				throw new SemanticException(expressionBinary.rightExpression.firstToken,"Invalid Expression");
			}
		}
		throw new SemanticException(expressionBinary.rightExpression.firstToken,"Invalid Expression");
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		expressionUnary.type = Types.getType(expressionUnary.firstToken.kind);
		return expressionUnary.firstToken.kind;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		expressionIntegerLiteral.type = Types.getType(Kind.KW_int);
		return Kind.KW_int;
	}

	@Override
	public Object visitBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		expressionBooleanLiteral.type = Types.getType(Kind.KW_boolean);
		return Kind.KW_boolean;
	}

	@Override
	public Object visitExpressionPredefinedName(ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		expressionPredefinedName.type = Types.getType(Kind.KW_int);
		return Kind.KW_int;
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		expressionFloatLiteral.type = Types.getType(Kind.KW_float);
		return Kind.KW_float;
	}

	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg, Object arg)
			throws Exception {
		Kind function = expressionFunctionAppWithExpressionArg.function;
		Kind e =  (Kind)expressionFunctionAppWithExpressionArg.e.visit(this,arg);
		if(function == Kind.KW_abs){
			if(e == Kind.KW_int || e == Kind.KW_float){
				expressionFunctionAppWithExpressionArg.type = Types.getType(e);
				return e;
			}else{
				throw new SemanticException(expressionFunctionAppWithExpressionArg.firstToken,"Invalid expressionFunctionAppWithExpressionArg");
			}
		}else if(function == Kind.KW_red || function ==  Kind.KW_green|| function ==  Kind.KW_blue || function ==  Kind.KW_alpha){
			if(e == Kind.KW_int){
				expressionFunctionAppWithExpressionArg.type = Types.getType(e);
				return e;
			}else{
				throw new SemanticException(expressionFunctionAppWithExpressionArg.firstToken,"Invalid expressionFunctionAppWithExpressionArg");
			}
		}else if(function == Kind.KW_sin || function ==  Kind.KW_cos || function ==  Kind.KW_atan || function ==  Kind.KW_log){
			if(e == Kind.KW_float){
				expressionFunctionAppWithExpressionArg.type = Types.getType(e);
				return e;
			}else{
				throw new SemanticException(expressionFunctionAppWithExpressionArg.firstToken,"Invalid expressionFunctionAppWithExpressionArg");
			}
		}else if (function == Kind.KW_width || function == Kind.KW_height) {
			if(e == Kind.KW_image){
				expressionFunctionAppWithExpressionArg.type = Types.getType(Kind.KW_int);
				return Kind.KW_int;
			}else{
				throw new SemanticException(expressionFunctionAppWithExpressionArg.firstToken,"Invalid expressionFunctionAppWithExpressionArg");
			}
		}else if(function == Kind.KW_float){
			if(e == Kind.KW_float || e == Kind.KW_int){
				expressionFunctionAppWithExpressionArg.type = Types.getType(Kind.KW_float);
				return Kind.KW_float;
			}else{
				throw new SemanticException(expressionFunctionAppWithExpressionArg.firstToken,"Invalid expressionFunctionAppWithExpressionArg");
			}
		}else if(function == Kind.KW_int){
			if(e == Kind.KW_float || e == Kind.KW_int){
				expressionFunctionAppWithExpressionArg.type = Types.getType(Kind.KW_int);
				return Kind.KW_int;
			}else{
				throw new SemanticException(expressionFunctionAppWithExpressionArg.firstToken,"Invalid expressionFunctionAppWithExpressionArg");
			}
		}
		throw new SemanticException(expressionFunctionAppWithExpressionArg.firstToken,"Invalid expressionFunctionAppWithExpressionArg");
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		Kind function = expressionFunctionAppWithPixel.name;
		Kind e0 = (Kind) expressionFunctionAppWithPixel.e0.visit(this,arg);
		Kind e1 = (Kind) expressionFunctionAppWithPixel.e1.visit(this,arg);
		if(function == Kind.KW_cart_x || function == Kind.KW_cart_y){
				if( e0 == Kind.KW_float && e1 == Kind.KW_float){
					expressionFunctionAppWithPixel.type = Types.getType(Kind.KW_int);
					return Kind.KW_int;
				}else{
					throw new SemanticException(expressionFunctionAppWithPixel.firstToken,"Invalid expressionFunctionAppWithPixel");
				}
		}else if(function == Kind.KW_polar_a || function == Kind.KW_polar_r){
			if( e0 == Kind.KW_int && e1 == Kind.KW_int){
				expressionFunctionAppWithPixel.type = Types.getType(Kind.KW_float);
				return Kind.KW_float;
			}else{
				throw new SemanticException(expressionFunctionAppWithPixel.firstToken,"Invalid expressionFunctionAppWithPixel");
			}
		}
		throw new SemanticException(expressionFunctionAppWithPixel.firstToken,"Invalid expressionFunctionAppWithPixel");
	}

	@Override
	public Object visitExpressionPixelConstructor(ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		Kind alpha = (Kind) expressionPixelConstructor.alpha.visit(this,arg);
		Kind red = (Kind) expressionPixelConstructor.red.visit(this,arg);
		Kind green = (Kind) expressionPixelConstructor.green.visit(this,arg);
		Kind blue = (Kind) expressionPixelConstructor.blue.visit(this,arg);
		if(alpha != Kind.KW_int || red != Kind.KW_int || green != Kind.KW_int || blue != Kind.KW_int ){
			throw new SemanticException(expressionPixelConstructor.firstToken,"Invalid expressionPixelConstructor");
		}
		expressionPixelConstructor.type = Types.getType(Kind.KW_int);
		return Kind.KW_int;
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
		Kind lhs = (Kind)statementAssign.lhs.visit(this,arg);
		if(lhs != statementAssign.e.visit(this,arg)){
			System.out.println(lhs+ " "+statementAssign.e.visit(this,arg));
			throw new SemanticException(statementAssign.firstToken,"Type mismatch in assignment");
		}
		return null;
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg) throws Exception {
		Kind e = (Kind) statementShow.e.visit(this,arg);
		if (!(e == Kind.KW_int || e == Kind.KW_boolean || e == Kind.KW_float || e == Kind.KW_image)){
			throw new SemanticException(statementShow.firstToken,"Invalid Type for show");
		}
		return null;
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel, Object arg) throws Exception {
		Declaration declaration = symbolTable.lookup(expressionPixel.name);
		if(declaration == null || declaration.type != Kind.KW_image){
			throw new SemanticException(expressionPixel.firstToken,"Invalid expressionPixel");
		}
		expressionPixel.type = Types.getType(Kind.KW_int);
		return Kind.KW_int;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws Exception {
		Declaration declaration = symbolTable.lookup(expressionIdent.name);
		if(declaration == null){
			throw new SemanticException(expressionIdent.firstToken,"Invalid expressionIdent");
		}
		expressionIdent.type = Types.getType(declaration.type);
		return declaration.type;
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg) throws Exception {
		Declaration declaration = symbolTable.lookup(lhsSample.name);
		if( declaration != null && declaration.type == Kind.KW_image ){
			lhsSample.type = Kind.KW_int;
		}else{
			throw new SemanticException(lhsSample.firstToken,"Invalid lhsSample");
		}
		return Kind.KW_int;
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg) throws Exception {
		Declaration declaration = symbolTable.lookup(lhsPixel.name);
		if( declaration != null && declaration.type == Kind.KW_image ){
			lhsPixel.type = Kind.KW_int;
		}else{
			throw new SemanticException(lhsPixel.firstToken,"Invalid lhsPixel");
		}
		return Kind.KW_int;
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg) throws Exception {
		Declaration declaration = symbolTable.lookup(lhsIdent.name);
		if( declaration != null ){
			lhsIdent.type = declaration.type;
		}else{
			throw new SemanticException(lhsIdent.firstToken,"Invalid lhs ident");
		}
		return declaration.type;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws Exception {
		if(statementIf.guard.visit(this,arg) != Kind.KW_boolean){
			throw new SemanticException(statementIf.firstToken,"Invalid if condition");
		}
		statementIf.b.visit(this,arg);
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws Exception {
		if(statementWhile.guard.visit(this,arg) != Kind.KW_boolean){
			throw new SemanticException(statementWhile.firstToken,"Invalid while condition");
		}
		statementWhile.b.visit(this,arg);
		return null;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg) throws Exception {
		Kind e = (Kind) statementSleep.duration.visit(this,arg);
		if(e != Kind.KW_int ){
			throw new SemanticException(statementSleep.firstToken,"Invalid type for sleep");
		}
		return  null;
	}


}
