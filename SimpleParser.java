package cop5556sp18;
/* *
 * Initial code for SimpleParser for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Spring 2018.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Spring 2018 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2018
 */


import cop5556sp18.Scanner.Token;
import cop5556sp18.Scanner.Kind;
import sun.tools.tree.OrExpression;

import static cop5556sp18.Scanner.Kind.*;


public class SimpleParser {
	
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}



	Scanner scanner;
	Token t;

	SimpleParser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}


	public void parse() throws SyntaxException {
		program();
		matchEOF();
	}

	/*
	 * Program ::= Identifier Block
	 */
	public void program() throws SyntaxException {
		match(IDENTIFIER);
		block();
	}

	/*
	 * Block ::=  { (  (Declaration | Statement) ; )* }
	 */

	Kind[] firstDec = { KW_int, KW_boolean, KW_image, KW_float, KW_filename };
	Kind[] firstStatement = {KW_input, KW_write, IDENTIFIER, KW_if, KW_show, KW_sleep, KW_red, KW_green, KW_blue,KW_alpha };
	Kind[] functionName = {KW_sin, KW_cos, KW_atan, KW_abs, KW_log, KW_cart_x, KW_cart_y, KW_polar_a, KW_polar_r,
	KW_int, KW_float, KW_width, KW_height, KW_red, KW_green, KW_blue, KW_alpha};
	Kind[] predefinedName = {KW_Z, KW_default_height, KW_default_width};

	public void block() throws SyntaxException {
		match(LBRACE);
		while (isKind(firstDec)|isKind(firstStatement)) {
	     if (isKind(firstDec)) {
			declaration();
		} else if (isKind(firstStatement)) {
			statement();
		}
			match(SEMI);
		}
		match(RBRACE);

	}

	public void declaration() throws SyntaxException{

		if(isKind(KW_image)){
			match(IDENTIFIER);
			if(isKind(LSQUARE)) {
				match(LSQUARE);
				expression();
				match(COMMA);
				expression();
				match(LSQUARE);
			}
		}else if( isKind(KW_int) || isKind(KW_float) || isKind(KW_boolean) || isKind(KW_filename)  ) {
			type();
			match(IDENTIFIER);
		}else{
			throw new SyntaxException(t,"Wrong way of declaring type");
		}
	}

	public void statement() throws SyntaxException{

		if(isKind(KW_input)){
			statementInput();
		}else if(isKind(KW_write)){
			statementWrite();
		}else if(isKind(IDENTIFIER) || isKind(KW_red) || isKind(KW_green) || isKind(KW_blue) || isKind(KW_alpha)){
			statementAssignment();
		}else if(isKind(KW_while)) {
			statementWhile();
		}else if(isKind(KW_if)){
			statementIf();
		}else if(isKind(KW_show)){
			statementShow();
		}else if(isKind(KW_sleep)){
			statementSleep();
		}else{
			throw new SyntaxException(t,"Invalid statement");
		}
	}

	public void expression() throws SyntaxException{
		orExpression();
		if(isKind(OP_QUESTION)){
			expression();
			match(OP_COLON);
			expression();
		}
	}

	public void orExpression() throws SyntaxException {
		andExpression();
		while(isKind(OP_OR)){
			match(OP_OR);
			andExpression();
		}
	}

	public void andExpression() throws SyntaxException {
		eqExpression();
		while(isKind(OP_AND)){
			match(OP_AND);
			eqExpression();
		}
	}

	public void eqExpression() throws SyntaxException {
		relExpression();
		while(isKind(OP_EQ) || isKind(OP_NEQ)){
			if(isKind(OP_EQ)){
				match(OP_EQ);
			}else{
				match(OP_NEQ);
			}
			relExpression();
		}

	}

	public void relExpression() throws SyntaxException {
		addExpression();
		while(isKind(OP_LT) || isKind(OP_LE) || isKind(OP_GT) || isKind(OP_GE)){
			if(isKind(OP_LT)){
				match(OP_LT);
			}else if(isKind(OP_LE)){
				match(OP_LE);
			}if(isKind(OP_GT)){
				match(OP_GT);
			}else{
				match(OP_GE);
			}
			addExpression();
		}


	}

	public void addExpression() throws SyntaxException {
		multExpression();
		while(isKind(OP_PLUS) || isKind(OP_MINUS)){
			if(isKind(OP_PLUS)){
				match(OP_PLUS);
			}else{
				match(OP_MINUS);
			}
			multExpression();
		}
	}
	public void multExpression() throws SyntaxException {
		powerExpression();
		while(isKind(OP_TIMES) || isKind(OP_DIV) || isKind(OP_MOD)){
			if(isKind(OP_TIMES)){
				match(OP_TIMES);
			}else if(isKind(OP_DIV)){
				match(OP_DIV);
			}else{
				match(OP_MOD);
			}
			powerExpression();
		}
	}

	public void powerExpression() throws SyntaxException {
		unaryExpression();
		if(isKind(OP_POWER)){
			match(OP_POWER);
			powerExpression();
		}
	}

	public void unaryExpression() throws SyntaxException {
		if(isKind(OP_PLUS)){
			match(OP_PLUS);
			unaryExpression();
		}else if(isKind(OP_MINUS)){
			match(OP_MINUS);
			unaryExpression();
		}else{
			if(isKind(OP_EXCLAMATION)){
				match(OP_EXCLAMATION);
				unaryExpression();
			}else{
				primary();
			}
		}

	}
	public void primary() throws SyntaxException {
		if(isKind(INTEGER_LITERAL)){
			match(INTEGER_LITERAL);
		}else if(isKind(BOOLEAN_LITERAL)){
			match(BOOLEAN_LITERAL);
		}else if(isKind(FLOAT_LITERAL)){
			match(FLOAT_LITERAL);
		}else if(isKind(LPAREN)){
			match(LPAREN);
			expression();
			match(RPAREN);
		}else if(isKind(functionName)){
			functionApplication();
		}else if(isKind(IDENTIFIER)){
			match(IDENTIFIER);
			if(isKind(LSQUARE)){
				pixelSelector();
			}
		}else if(isKind(predefinedName)){
				if(isKind(KW_Z)){
					match(KW_Z);
				}else if(isKind(KW_default_height)){
					match(KW_default_height);
				}else if(isKind(KW_default_width)){
					match(KW_default_width);
				}
		}else if(isKind(LPIXEL)){
			pixelConstructor();
		}else{
			throw new SyntaxException(t,"Invalid primary");
		}
	}

	public void functionApplication() throws SyntaxException{

		if(isKind(KW_sin) ){
			match(KW_sin);
		}else if(isKind(KW_cos)){
			match(KW_cos);
		}else if(isKind(KW_atan)){
			match(KW_atan);
		}else if(isKind(KW_abs)){
			match(KW_abs);
		}else if(isKind(KW_log)){
			match(KW_log);
		}else if(isKind(KW_cart_x)){
			match(KW_cart_x);
		}else if(isKind(KW_cart_y)){
			match(KW_cart_y);
		}else if(isKind(KW_polar_a)){
			match(KW_polar_a);
		}else if(isKind(KW_polar_r)){
			match(KW_polar_r);
		}else if(isKind(KW_int)){
			match(KW_int);
		}else if(isKind(KW_float)){
			match(KW_float);
		}else if(isKind(KW_width)){
			match(KW_width);
		}else if(isKind(KW_height)){
			match(KW_height);
		}else if(isKind(KW_red)){
			match(KW_red);
		}else if(isKind(KW_green)){
			match(KW_green);
		}else if(isKind(KW_blue)){
			match(KW_blue);
		}else if(isKind(KW_alpha)){
			match(KW_alpha);
		}

		if(isKind(LPAREN)){
			match(LPAREN);
			expression();
			match(RPAREN);
		}else if(isKind(LSQUARE)){
			match(LSQUARE);
			expression();
			match(COMMA);
			expression();
			match(RSQUARE);
		}else{
			throw new SyntaxException(t,"Invalid function Application");
		}
	}

	public void pixelConstructor() throws SyntaxException{
			match(LPIXEL);
			expression();
			match(COMMA);
			expression();
			match(COMMA);
			expression();
			match(COMMA);
			expression();
			match(RPIXEL);
	}

	public void type() throws SyntaxException{
		if(isKind(KW_int) ){
			match(KW_int);
		}else if(isKind(KW_float)){
			match(KW_float);
		}else if(isKind(KW_boolean)){
			match(KW_boolean);
		}else if(isKind(KW_filename)){
			match(KW_filename);
		}
	}

	public void statementInput() throws SyntaxException{
		match(KW_input);
		match(IDENTIFIER);
		match(KW_from);
		match(OP_AT);
		expression();
	}

	public void statementWrite() throws SyntaxException{
		match(KW_write);
		match(IDENTIFIER);
		match(KW_to);
		match(IDENTIFIER);
	}
	public void  statementAssignment() throws SyntaxException{
		LHS();
		match(OP_ASSIGN);
		expression();
	}

	public void statementWhile() throws SyntaxException{
		match(KW_while);
		match(LPAREN);
		expression();
		match(RPAREN);
		block();
	}

	public void statementIf() throws SyntaxException{
		match(KW_if);
		match(LPAREN);
		expression();
		match(RPAREN);
		block();
	}

	public void statementShow() throws SyntaxException{
		match(KW_show);
		expression();
	}

	public void statementSleep() throws SyntaxException{
		match(KW_sleep);
		expression();
	}
	public void LHS() throws SyntaxException{
		if(isKind(IDENTIFIER)){
			match(IDENTIFIER);
			if(isKind(LSQUARE)){
				pixelSelector();
			}
		}else if(isKind(KW_red) || isKind(KW_green) || isKind(KW_blue) || isKind(KW_alpha) ){
			color();
			match(LPAREN);
			match(IDENTIFIER);
			pixelSelector();
			match(RPAREN);
		}else{
			throw new SyntaxException(t,"Invalid LHS");
		}
	}

	public void color() throws SyntaxException {
		if(isKind(KW_red)){
			match(KW_red);
		} else if(isKind(KW_green)){
			match(KW_green);
		} else if (isKind(KW_blue)){
			match(KW_blue);
		} else if (isKind(KW_alpha)){
			match(KW_alpha);
		}else{
			throw new SyntaxException(t,"Invalid color");
		}
	}

	public void pixelSelector() throws SyntaxException {
		match(LSQUARE);
		expression();
		match(COMMA);
		expression();
		match(RSQUARE);
	}

	protected boolean isKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean isKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}


	/**
	 * Precondition: kind != EOF
	 *
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		Token tmp = t;
		if (isKind(kind)) {
			consume();
			return tmp;
		}
		throw new SyntaxException(t,"Syntax Error"); //TODO  give a better error message!
	}


	private Token consume() throws SyntaxException {
		Token tmp = t;
		if (isKind( EOF)) {
			throw new SyntaxException(t,"Syntax Error"); //TODO  give a better error message!
			//Note that EOF should be matched by the matchEOF method which is called only in parse().
			//Anywhere else is an error. */
		}
		t = scanner.nextToken();
		return tmp;
	}


	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 *
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (isKind(EOF)) {
			return t;
		}
		throw new SyntaxException(t,"Syntax Error"); //TODO  give a better error message!
	}


}

