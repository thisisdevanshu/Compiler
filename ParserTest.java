 /**
 * JUunit tests for the Parser for the class project in COP5556 Programming Language Principles 
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

package cop5556sp18;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import cop5556sp18.AST.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Scanner.Kind;
import cop5556sp18.Parser;
import cop5556sp18.Scanner;
import cop5556sp18.Parser.SyntaxException;
import cop5556sp18.Scanner.LexicalException;
import cop5556sp18.Scanner.Token;
import static cop5556sp18.Scanner.Kind.*;

public class ParserTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}


	//creates and returns a parser for the given input.
	private Parser makeParser(String input) throws LexicalException {
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		Parser parser = new Parser(scanner);
		return parser;
	}
	

	
	/**
	 * Simple test case with an empty program.  This throws an exception 
	 * because it lacks an identifier and a block
	 *   
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = "";  //The input is the empty string.  
		thrown.expect(SyntaxException.class);
		Parser parser = makeParser(input);
		@SuppressWarnings("unused")
		Program p = parser.parse();
	}
	
	/**
	 * Smallest legal program.
	 *   
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testSmallest() throws LexicalException, SyntaxException {
		String input = "b{}";  
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals("b", p.progName);
		assertEquals(0, p.block.decsOrStatements.size());
	}	
	
	
	/**
	 * Checks that an element in a block is a declaration with the given type and name.
	 * The element to check is indicated by the value of index.
	 * 
	 * @param block
	 * @param index
	 * @param type
	 * @param name
	 * @return
	 */
	Declaration checkDec(Block block, int index, Kind type,
			String name) {
		ASTNode node = block.decOrStatement(index);
		assertEquals(Declaration.class, node.getClass());
		Declaration dec = (Declaration) node;
		assertEquals(type, dec.type);
		assertEquals(name, dec.name);
		return dec;
	}	
	
	@Test
	public void testDec0() throws LexicalException, SyntaxException {
		String input = "b{int c; image j;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);	
		checkDec(p.block, 0, Kind.KW_int, "c");
		checkDec(p.block, 1, Kind.KW_image, "j");
	}
	
	
	/** This test illustrates how you can test specific grammar elements by themselves by
	 * calling the corresponding parser method directly, instead of calling parse.
	 * This requires that the methods are visible (not private). 
	 * 
	 * @throws LexicalException
	 * @throws SyntaxException
	 */
	
	@Test
	public void testExpression() throws LexicalException, SyntaxException {
		String input = "x + 2";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);	
		assertEquals(ExpressionBinary.class, e.getClass());
		ExpressionBinary b = (ExpressionBinary)e;
		assertEquals(ExpressionIdent.class, b.leftExpression.getClass());
		ExpressionIdent left = (ExpressionIdent)b.leftExpression;
		assertEquals("x", left.name);
		assertEquals(ExpressionIntegerLiteral.class, b.rightExpression.getClass());
		ExpressionIntegerLiteral right = (ExpressionIntegerLiteral)b.rightExpression;
		assertEquals(2, right.value);
		assertEquals(OP_PLUS, b.op);
	}
	@Test
	public void testDec1() throws LexicalException, SyntaxException {
		String input = "b{int c; c := 2+2+3; image j;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=b, block=Block [decsOrStatements=[Declaration [type=KW_int, name=c, width=null, height=null], StatementAssign [lhs=LHSIdent [name=c], e=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=2], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=2]], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=3]]], Declaration [type=KW_image, name=j, width=null, height=null]]]]");
	}



	@Test
	public void testDec12() throws LexicalException, SyntaxException {
		String input = "b{int c; " +
				"float b; " +
				"boolean c; " +
				"image d; " +
				"filename e; " +
				"image f [true,4]; }";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=b, block=Block [decsOrStatements=[Declaration [type=KW_int, name=c, width=null, height=null], Declaration [type=KW_float, name=b, width=null, height=null], Declaration [type=KW_boolean, name=c, width=null, height=null], Declaration [type=KW_image, name=d, width=null, height=null], Declaration [type=KW_filename, name=e, width=null, height=null], Declaration [type=KW_image, name=f, width=ExpressionBooleanLiteral [value=true], height=ExpressionIntegerLiteral [value=4]]]]]");
	}

	@Test
	public void testDec2() throws LexicalException, SyntaxException {
		String input = "b{input IDENTIFIER from @ 1; " +
				"write IDENTIFIER to IDENTIFIER;" +
				"while (abc>def){}; " +
				"if (false){}; " +
				"show atan(1);" +
				"sleep Z; }";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=b, block=Block [decsOrStatements=[StatementInput [destName=IDENTIFIER, e=ExpressionIntegerLiteral [value=1]], StatementWrite [sourceName=IDENTIFIER, destName=IDENTIFIER], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=abc], op=OP_GT, rightExpression=ExpressionIdent [name=def]], b=Block [decsOrStatements=[]]], StatementIf [guard=ExpressionBooleanLiteral [value=false], b=Block [decsOrStatements=[]]], ShowStatement [e=ExpressionFunctionApp [function=KW_atan, e=ExpressionIntegerLiteral [value=1]]], StatementSleep [duration=ExpressionPredefinedName [name=KW_Z]]]]]");
	}

	@Test
	public void testDec3() throws LexicalException, SyntaxException {
		String input = "b{ " +
				"show +true;" +
				"red ( IDENTIFIER  [ 3+5+2-1-5 , 9**9/3%2 ] ):= 45 != 9;" +
				"sleep true  ?  123.56  :  1;" +
				"while(default_height){}; }";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=b, block=Block [decsOrStatements=[ShowStatement [e=ExpressionUnary [op=OP_PLUS, expression=ExpressionBooleanLiteral [value=true]]], StatementAssign [lhs=LHSSample [name=IDENTIFIER, pixelSelector=PixelSelector [ex=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=3], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=5]], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=2]], op=OP_MINUS, rightExpression=ExpressionIntegerLiteral [value=1]], op=OP_MINUS, rightExpression=ExpressionIntegerLiteral [value=5]], ey=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=9], op=OP_POWER, rightExpression=ExpressionIntegerLiteral [value=9]], op=OP_DIV, rightExpression=ExpressionIntegerLiteral [value=3]], op=OP_MOD, rightExpression=ExpressionIntegerLiteral [value=2]]], color=KW_red], e=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=45], op=OP_NEQ, rightExpression=ExpressionIntegerLiteral [value=9]]], StatementSleep [duration=ExpressionConditional [guard=ExpressionBooleanLiteral [value=true], trueExpression=ExpressionFloatLiteral [value=123.56], falseExpression=ExpressionIntegerLiteral [value=1]]], StatementWhile [guard=ExpressionPredefinedName [name=KW_default_height], b=Block [decsOrStatements=[]]]]]]");
	}
	@Test
	public void testDec4() throws LexicalException, SyntaxException {
		String input = "b{ show sin(1); sleep cos(a); show atan(true); while(abs(1+2)){}; " +
				"if(log(2/3)){};" +
				" input IDENTIFIER from @ cart_x(100%4); z := cart_y(5**2)? polar_a(123.9090): polar_r(0.1); " +
				"abc := int(6); xyz := float(2); ret := width(1234); b := height(456) != red(12) & green(234); " +
				" qwe := blue(445) <= alpha(45);" +
				" i := <<  Expression , Expression , Expression , Expression  >>;" +
				" def := +a <= -r;" +
				" }";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=b, block=Block [decsOrStatements=[ShowStatement [e=ExpressionFunctionApp [function=KW_sin, e=ExpressionIntegerLiteral [value=1]]], StatementSleep [duration=ExpressionFunctionApp [function=KW_cos, e=ExpressionIdent [name=a]]], ShowStatement [e=ExpressionFunctionApp [function=KW_atan, e=ExpressionBooleanLiteral [value=true]]], StatementWhile [guard=ExpressionFunctionApp [function=KW_abs, e=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=1], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=2]]], b=Block [decsOrStatements=[]]], StatementIf [guard=ExpressionFunctionApp [function=KW_log, e=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=2], op=OP_DIV, rightExpression=ExpressionIntegerLiteral [value=3]]], b=Block [decsOrStatements=[]]], StatementInput [destName=IDENTIFIER, e=ExpressionFunctionApp [function=KW_cart_x, e=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=100], op=OP_MOD, rightExpression=ExpressionIntegerLiteral [value=4]]]], StatementAssign [lhs=LHSIdent [name=z], e=ExpressionConditional [guard=ExpressionFunctionApp [function=KW_cart_y, e=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=5], op=OP_POWER, rightExpression=ExpressionIntegerLiteral [value=2]]], trueExpression=ExpressionFunctionApp [function=KW_polar_a, e=ExpressionFloatLiteral [value=123.909]], falseExpression=ExpressionFunctionApp [function=KW_polar_r, e=ExpressionFloatLiteral [value=0.1]]]], StatementAssign [lhs=LHSIdent [name=abc], e=ExpressionFunctionApp [function=KW_int, e=ExpressionIntegerLiteral [value=6]]], StatementAssign [lhs=LHSIdent [name=xyz], e=ExpressionFunctionApp [function=KW_float, e=ExpressionIntegerLiteral [value=2]]], StatementAssign [lhs=LHSIdent [name=ret], e=ExpressionFunctionApp [function=KW_width, e=ExpressionIntegerLiteral [value=1234]]], StatementAssign [lhs=LHSIdent [name=b], e=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionFunctionApp [function=KW_height, e=ExpressionIntegerLiteral [value=456]], op=OP_NEQ, rightExpression=ExpressionFunctionApp [function=KW_red, e=ExpressionIntegerLiteral [value=12]]], op=OP_AND, rightExpression=ExpressionFunctionApp [function=KW_green, e=ExpressionIntegerLiteral [value=234]]]], StatementAssign [lhs=LHSIdent [name=qwe], e=ExpressionBinary [leftExpression=ExpressionFunctionApp [function=KW_blue, e=ExpressionIntegerLiteral [value=445]], op=OP_LE, rightExpression=ExpressionFunctionApp [function=KW_alpha, e=ExpressionIntegerLiteral [value=45]]]], StatementAssign [lhs=LHSIdent [name=i], e=ExpressionPixelConstructor [alpha=ExpressionIdent [name=Expression], red=ExpressionIdent [name=Expression], green=ExpressionIdent [name=Expression], blue=ExpressionIdent [name=Expression]]], StatementAssign [lhs=LHSIdent [name=def], e=ExpressionBinary [leftExpression=ExpressionUnary [op=OP_PLUS, expression=ExpressionIdent [name=a]], op=OP_LE, rightExpression=ExpressionUnary [op=OP_MINUS, expression=ExpressionIdent [name=r]]]]]]]");

	}

	@Test
	public void testDec5() throws LexicalException, SyntaxException {
		String input = "a{ i:=sin(a);" +
				"k := ! default_height; }";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=a, block=Block [decsOrStatements=[StatementAssign [lhs=LHSIdent [name=i], e=ExpressionFunctionApp [function=KW_sin, e=ExpressionIdent [name=a]]], StatementAssign [lhs=LHSIdent [name=k], e=ExpressionUnary [op=OP_EXCLAMATION, expression=ExpressionPredefinedName [name=KW_default_height]]]]]]");
	}

	@Test
	public void testDec54() throws LexicalException, SyntaxException {
		String input = "b{ x := 9**9/3%2;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p.block.decsOrStatements.get(0));
		//assertEquals("Program [progName=b, block=Block [decsOrStatements=[StatementAssign [lhs=LHSIdent [name=a], e=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=9], op=OP_POWER, rightExpression=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=9], op=OP_DIV, rightExpression=ExpressionIntegerLiteral [value=3]], op=OP_MOD, rightExpression=ExpressionIntegerLiteral [value=2]]]]]]]",p.toString());
	}

	@Test
	public void testExpressionBooleanLiteral() throws LexicalException, SyntaxException {
		String input = "true";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
		assertEquals(ExpressionBooleanLiteral.class, e.getClass());
		ExpressionBooleanLiteral b = (ExpressionBooleanLiteral)e;
		assertEquals(true, b.value);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testExpressionConditional() throws LexicalException, SyntaxException {
		String input = "a < b ? 1.2 : 0";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
		assertEquals(ExpressionConditional.class, e.getClass());
		ExpressionConditional b = (ExpressionConditional)e;

		assertEquals(ExpressionBinary.class, b.guard.getClass());
		ExpressionBinary cond = (ExpressionBinary)b.guard;
		assertEquals(ExpressionIdent.class, cond.leftExpression.getClass());
		ExpressionIdent left = (ExpressionIdent)cond.leftExpression;
		assertEquals("a", left.name);
		assertEquals(ExpressionIdent.class, cond.rightExpression.getClass());
		ExpressionIdent right = (ExpressionIdent)cond.rightExpression;
		assertEquals("b", right.name);

		assertEquals(ExpressionFloatLiteral.class, b.trueExpression.getClass());
		ExpressionFloatLiteral trueExpr = (ExpressionFloatLiteral)b.trueExpression;
		assertEquals(1.2, trueExpr.value, 0.0001);
		assertEquals(ExpressionIntegerLiteral.class, b.falseExpression.getClass());
		ExpressionIntegerLiteral falseExpr = (ExpressionIntegerLiteral)b.falseExpression;
		assertEquals(0, falseExpr.value);
	}

	@Test
	public void testStatementAssign() throws LexicalException, SyntaxException {
		String input = "x:=0";
		Parser parser = makeParser(input);
		Statement statement = parser.statement();
		show(statement);
		assertEquals(StatementAssign.class, statement.getClass());
		StatementAssign statementAssign = (StatementAssign)statement;
		assertEquals(LHSIdent.class, statementAssign.lhs.getClass());
		LHSIdent lhs= (LHSIdent)statementAssign.lhs;
		assertEquals("x", lhs.name);
		assertEquals(ExpressionIntegerLiteral.class, statementAssign.e.getClass());
		ExpressionIntegerLiteral right = (ExpressionIntegerLiteral)statementAssign.e;
		assertEquals(0, right.value);
	}

	@Test
	public void testDec3451() throws LexicalException, SyntaxException {
		String input = "b{image foo[[3,4]}";
		thrown.expect(SyntaxException.class);
		try {
			Parser parser = makeParser(input);
			Program p = parser.parse();
		} catch (SyntaxException e) {
			show(e);
			assertEquals("Syntax Error: unexpected symbol: [ at 13 in line 1",e.getMessage()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}

	@Test
	public void testSamples() throws LexicalException, SyntaxException {
		String input = "samples { \n"
				+ "image bird; \n"
				+ " input bird from @0; \n"
				+ "show bird; \n"
				+ "sleep(4000); \n"
				+ "image bird2[width(bird),height(bird)]; \n"
				+ "int x; \n"
				+ "x:=0; \n"
				+ "while(x<width(bird2)) {int y; \n"
				+ "y:=0; \n"
				+ "while(y<height(bird2)) {blue(bird2[x,y]):=red(bird[x,y]); \n"
				+ "green(bird2[x,y]):=blue(bird[x,y]); \n"
				+ "red(bird2[x,y]):=green(bird[x,y]); \n"
				+ "alpha(bird2[x,y]):=Z; \n"
				+ "y:=y+1; \n"
				+ "}; \n"
				+ "x:=x+       1; \n"
				+ "}; \n"
				+ "show bird2; \n"
				+ "sleep(4000); \n"
				+ "}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=samples, block=Block [decsOrStatements=[Declaration [type=KW_image, name=bird, width=null, height=null], StatementInput [destName=bird, e=ExpressionIntegerLiteral [value=0]], ShowStatement [e=ExpressionIdent [name=bird]], StatementSleep [duration=ExpressionIntegerLiteral [value=4000]], Declaration [type=KW_image, name=bird2, width=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=bird]], height=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=bird]]], Declaration [type=KW_int, name=x, width=null, height=null], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=bird2]]], b=Block [decsOrStatements=[Declaration [type=KW_int, name=y, width=null, height=null], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=bird2]]], b=Block [decsOrStatements=[StatementAssign [lhs=LHSSample [name=bird2, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]], color=KW_blue], e=ExpressionFunctionApp [function=KW_red, e=ExpressionPixel [name=bird, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]]]], StatementAssign [lhs=LHSSample [name=bird2, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]], color=KW_green], e=ExpressionFunctionApp [function=KW_blue, e=ExpressionPixel [name=bird, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]]]], StatementAssign [lhs=LHSSample [name=bird2, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]], color=KW_red], e=ExpressionFunctionApp [function=KW_green, e=ExpressionPixel [name=bird, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]]]], StatementAssign [lhs=LHSSample [name=bird2, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]], color=KW_alpha], e=ExpressionPredefinedName [name=KW_Z]], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], ShowStatement [e=ExpressionIdent [name=bird2]], StatementSleep [duration=ExpressionIntegerLiteral [value=4000]]]]]");
	}

	@Test
	public void testDeclaration1() throws LexicalException, SyntaxException {
		String input = "ident { image; }";
		thrown.expect(SyntaxException.class);  //Tell JUnit to expect a SyntaxException
		try {
			Parser parser = makeParser(input);
			Program p = parser.parse();
		} catch (SyntaxException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals("Syntax Error: unexpected symbol: ; at 14 in line 1",e.getMessage()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}

	@Test
	public void testDeclaration2() throws LexicalException, SyntaxException {
		String input = "ident { image abc; }";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=ident, block=Block [decsOrStatements=[Declaration [type=KW_image, name=abc, width=null, height=null]]]]");
	}

	@Test
	public void testDeclaration3() throws LexicalException, SyntaxException {
		String input = "ident { image abc[]; }";
		thrown.expect(SyntaxException.class);  //Tell JUnit to expect a SyntaxException
		try {
			Parser parser = makeParser(input);
			Program p = parser.parse();
		} catch (SyntaxException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals("Syntax Error: unexpected symbol: ] at 19 in line 1",e.getMessage()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}

	@Test
	public void testDeclaration4() throws LexicalException, SyntaxException {
		String input = "ident { image abc[a,b]; image abc[1,2]; image abc[a|b,c&d]; int a; float b; boolean bool; filename xyz; }";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=ident, block=Block [decsOrStatements=[Declaration [type=KW_image, name=abc, width=ExpressionIdent [name=a], height=ExpressionIdent [name=b]], Declaration [type=KW_image, name=abc, width=ExpressionIntegerLiteral [value=1], height=ExpressionIntegerLiteral [value=2]], Declaration [type=KW_image, name=abc, width=ExpressionBinary [leftExpression=ExpressionIdent [name=a], op=OP_OR, rightExpression=ExpressionIdent [name=b]], height=ExpressionBinary [leftExpression=ExpressionIdent [name=c], op=OP_AND, rightExpression=ExpressionIdent [name=d]]], Declaration [type=KW_int, name=a, width=null, height=null], Declaration [type=KW_float, name=b, width=null, height=null], Declaration [type=KW_boolean, name=bool, width=null, height=null], Declaration [type=KW_filename, name=xyz, width=null, height=null]]]]");
	}

	@Test
	public void testDeclaration5() throws LexicalException, SyntaxException {
		String input = "ident { int; }";
		thrown.expect(SyntaxException.class);  //Tell JUnit to expect a SyntaxException
		try {
			Parser parser = makeParser(input);
			Program p = parser.parse();
		} catch (SyntaxException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals("Syntax Error: unexpected symbol: ; at 12 in line 1",e.getMessage()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}

	@Test
	public void test1() throws LexicalException, SyntaxException {
		String input = "true{}";
		thrown.expect(SyntaxException.class);  //Tell JUnit to expect a SyntaxException
		try {
			Parser parser = makeParser(input);
			Program p = parser.parse();
		} catch (SyntaxException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals("Syntax Error: unexpected symbol: true at 1 in line 1",e.getMessage()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}

	@Test
	public void test2() throws LexicalException, SyntaxException {
		String input = "crazy{"
				+ "int a;"
				+ "image b;"
				+ "image c [23, 89];"
				+ "input abc from @ 23;"
				+ "while(true) {"
				+ "};"
				+ "}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=crazy, block=Block [decsOrStatements=[Declaration [type=KW_int, name=a, width=null, height=null], Declaration [type=KW_image, name=b, width=null, height=null], Declaration [type=KW_image, name=c, width=ExpressionIntegerLiteral [value=23], height=ExpressionIntegerLiteral [value=89]], StatementInput [destName=abc, e=ExpressionIntegerLiteral [value=23]], StatementWhile [guard=ExpressionBooleanLiteral [value=true], b=Block [decsOrStatements=[]]]]]]");
	}

	@Test
	public void test3() throws LexicalException, SyntaxException {
		String input = "hello{"
				+ "int b;"
				+ "}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=hello, block=Block [decsOrStatements=[Declaration [type=KW_int, name=b, width=null, height=null]]]]");
	}

	@Test
	public void test4() throws LexicalException, SyntaxException {
		String input = "hello{"
				+ "image pix[43,43];"
				+ "}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=hello, block=Block [decsOrStatements=[Declaration [type=KW_image, name=pix, width=ExpressionIntegerLiteral [value=43], height=ExpressionIntegerLiteral [value=43]]]]]");
	}

	@Test
	public void test5() throws LexicalException, SyntaxException {
		String input = "hello{"
				+ "image pix;"
				+ "}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=hello, block=Block [decsOrStatements=[Declaration [type=KW_image, name=pix, width=null, height=null]]]]");
	}

	@Test
	public void test6() throws LexicalException, SyntaxException {
		String input = "hello{"
				+ "input abc from @ 2;"
				+ "}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=hello, block=Block [decsOrStatements=[StatementInput [destName=abc, e=ExpressionIntegerLiteral [value=2]]]]]");
	}

	@Test
	public void test7() throws LexicalException, SyntaxException {
		String input = "yo   {"
				+ "image pix[yo,yo];"
				+ "}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=yo, block=Block [decsOrStatements=[Declaration [type=KW_image, name=pix, width=ExpressionIdent [name=yo], height=ExpressionIdent [name=yo]]]]]");
	}

	@Test
	public void test8() throws LexicalException, SyntaxException {
		String input = "hey{"
				+ "while(true) {"
				+ "};"
				+ "}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=hey, block=Block [decsOrStatements=[StatementWhile [guard=ExpressionBooleanLiteral [value=true], b=Block [decsOrStatements=[]]]]]]");
	}

	@Test
	public void test9() throws LexicalException, SyntaxException {
		String input = "b {"
				+ "int a;"
				+ "a := 90;"
				+ "}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=b, block=Block [decsOrStatements=[Declaration [type=KW_int, name=a, width=null, height=null], StatementAssign [lhs=LHSIdent [name=a], e=ExpressionIntegerLiteral [value=90]]]]]");
	}

	@Test
	public void test10() throws LexicalException, SyntaxException {
		String input = "demo1{"
				+ "image h;"
				+ "input h from @ 0;"
				+ "show h;"
				+ "sleep(4000);"
				+ "image g[width(h), height(h)];"
				+ "int x;"
				+ "x := 0;"
				+ "while(x<width(g)){"
				+ "int y;"
				+ "y:=0;"
				+ "while(y<height(g)){"
				+ "g[x,y] := h[y,x];"
				+ "y := y+1;"
				+ "};"
				+ "x := x+1;"
				+ "};"
				+ "show g;"
				+ "sleep(4000);"
				+ "}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=demo1, block=Block [decsOrStatements=[Declaration [type=KW_image, name=h, width=null, height=null], StatementInput [destName=h, e=ExpressionIntegerLiteral [value=0]], ShowStatement [e=ExpressionIdent [name=h]], StatementSleep [duration=ExpressionIntegerLiteral [value=4000]], Declaration [type=KW_image, name=g, width=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=h]], height=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=h]]], Declaration [type=KW_int, name=x, width=null, height=null], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=g]]], b=Block [decsOrStatements=[Declaration [type=KW_int, name=y, width=null, height=null], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=g]]], b=Block [decsOrStatements=[StatementAssign [lhs=LHSPixel [name=g, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]], e=ExpressionPixel [name=h, pixelSelector=PixelSelector [ex=ExpressionIdent [name=y], ey=ExpressionIdent [name=x]]]], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], ShowStatement [e=ExpressionIdent [name=g]], StatementSleep [duration=ExpressionIntegerLiteral [value=4000]]]]]");
	}

	@Test
	public void random() throws LexicalException, SyntaxException {
		String input = " demo{ " +
				"image h;" +
				"input h from @ 0;" +
				"show h;" +
				"sleep(4000); " +
				"image g[width(h), height(h)];" +
				"int x;" +
				"x := <<343, 433, 3434,434 >>;" +
				"x := 0;" +
				"while (x < width(g)){" +
				"int y;" +
				"y := 0;" +
				"while (y < height(g)){" +
				"g[x,y] := h[y,x];" +
				"y:=y+1;" +
				"};" +
				"x:=x+1;" +
				"};" +
				"show g;" +
				"sleep(4000);" +
				"}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=demo, block=Block [decsOrStatements=[Declaration [type=KW_image, name=h, width=null, height=null], StatementInput [destName=h, e=ExpressionIntegerLiteral [value=0]], ShowStatement [e=ExpressionIdent [name=h]], StatementSleep [duration=ExpressionIntegerLiteral [value=4000]], Declaration [type=KW_image, name=g, width=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=h]], height=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=h]]], Declaration [type=KW_int, name=x, width=null, height=null], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionPixelConstructor [alpha=ExpressionIntegerLiteral [value=343], red=ExpressionIntegerLiteral [value=433], green=ExpressionIntegerLiteral [value=3434], blue=ExpressionIntegerLiteral [value=434]]], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=g]]], b=Block [decsOrStatements=[Declaration [type=KW_int, name=y, width=null, height=null], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=g]]], b=Block [decsOrStatements=[StatementAssign [lhs=LHSPixel [name=g, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]], e=ExpressionPixel [name=h, pixelSelector=PixelSelector [ex=ExpressionIdent [name=y], ey=ExpressionIdent [name=x]]]], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], ShowStatement [e=ExpressionIdent [name=g]], StatementSleep [duration=ExpressionIntegerLiteral [value=4000]]]]]");
	}

	@Test
	public void random1() throws LexicalException, SyntaxException {
		String input = " makeredimage{ " +
				"image im[256,256];" +
				"int x;" +
				"int y;" +
				"x:=0;" +
				"y:= 0; " +
				"while (x < width(im)){" +
				"y := 0;" +
				"while (y < height(im)){" +
				"im[x,y] := <<255,255,0,0 >>;" +
				"y:=y+1;" +
				"};" +
				"x:=x+1;" +
				"};" +
				"show im;" +
				"}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=makeredimage, block=Block [decsOrStatements=[Declaration [type=KW_image, name=im, width=ExpressionIntegerLiteral [value=256], height=ExpressionIntegerLiteral [value=256]], Declaration [type=KW_int, name=x, width=null, height=null], Declaration [type=KW_int, name=y, width=null, height=null], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionIntegerLiteral [value=0]], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=im]]], b=Block [decsOrStatements=[StatementAssign [lhs=LHSIdent [name=y], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=im]]], b=Block [decsOrStatements=[StatementAssign [lhs=LHSPixel [name=im, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]], e=ExpressionPixelConstructor [alpha=ExpressionIntegerLiteral [value=255], red=ExpressionIntegerLiteral [value=255], green=ExpressionIntegerLiteral [value=0], blue=ExpressionIntegerLiteral [value=0]]], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], ShowStatement [e=ExpressionIdent [name=im]]]]]");
	}

	@Test
	public void random2() throws LexicalException, SyntaxException {
		String input = " polarR2{ " +
				"image im[256,256];" +
				"int x;" +
				"x:=0;" +
				"while (x < width(im)){" +
				"int y;" +
				"y := 0;" +
				"while (y < height(im)){" +
				"float p;" +
				"p:=polar_R[x,y];" +
				"int r;" +
				"r := int(p)%Z;" +
				"im[x,y] := <<Z,255,0,r >>;" +
				"y:=y+1;" +
				"};" +
				"x:=x+1;" +
				"};" +
				"show im;" +
				"}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=polarR2, block=Block [decsOrStatements=[Declaration [type=KW_image, name=im, width=ExpressionIntegerLiteral [value=256], height=ExpressionIntegerLiteral [value=256]], Declaration [type=KW_int, name=x, width=null, height=null], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=im]]], b=Block [decsOrStatements=[Declaration [type=KW_int, name=y, width=null, height=null], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=im]]], b=Block [decsOrStatements=[Declaration [type=KW_float, name=p, width=null, height=null], StatementAssign [lhs=LHSIdent [name=p], e=ExpressionPixel [name=polar_R, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]]], Declaration [type=KW_int, name=r, width=null, height=null], StatementAssign [lhs=LHSIdent [name=r], e=ExpressionBinary [leftExpression=ExpressionFunctionApp [function=KW_int, e=ExpressionIdent [name=p]], op=OP_MOD, rightExpression=ExpressionPredefinedName [name=KW_Z]]], StatementAssign [lhs=LHSPixel [name=im, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]], e=ExpressionPixelConstructor [alpha=ExpressionPredefinedName [name=KW_Z], red=ExpressionIntegerLiteral [value=255], green=ExpressionIntegerLiteral [value=0], blue=ExpressionIdent [name=r]]], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], ShowStatement [e=ExpressionIdent [name=im]]]]]");
	}

	@Test
	public void random3() throws LexicalException, SyntaxException {
		String input = " samples{ " +
				"image bird;" +
				"input bird from @0;" +
				"show bird;" +
				"sleep(4000);" +
				"image bird2[width(bird),height(bird)];" +
				"int x; x :=0;" +
				"while (x < width(bird2)){" +
				"int y;" +
				"y := 0;" +
				"while (y < height(bird2)){" +
				"blue(bird2[x,y]) :=  red(bird[x,y]);" +
				"green(bird2[x,y]) :=  blue(bird[x,y]);" +
				"alpha(bird2[x,y]) :=  Z;" +
				"red(bird2[x,y]) :=  green(bird[x,y]);" +
				"y:=y+1;" +
				"};" +
				"x:=x+1;" +
				"};" +
				"show bird2; sleep(4000);" +
				"}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=samples, block=Block [decsOrStatements=[Declaration [type=KW_image, name=bird, width=null, height=null], StatementInput [destName=bird, e=ExpressionIntegerLiteral [value=0]], ShowStatement [e=ExpressionIdent [name=bird]], StatementSleep [duration=ExpressionIntegerLiteral [value=4000]], Declaration [type=KW_image, name=bird2, width=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=bird]], height=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=bird]]], Declaration [type=KW_int, name=x, width=null, height=null], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=bird2]]], b=Block [decsOrStatements=[Declaration [type=KW_int, name=y, width=null, height=null], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=bird2]]], b=Block [decsOrStatements=[StatementAssign [lhs=LHSSample [name=bird2, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]], color=KW_blue], e=ExpressionFunctionApp [function=KW_red, e=ExpressionPixel [name=bird, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]]]], StatementAssign [lhs=LHSSample [name=bird2, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]], color=KW_green], e=ExpressionFunctionApp [function=KW_blue, e=ExpressionPixel [name=bird, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]]]], StatementAssign [lhs=LHSSample [name=bird2, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]], color=KW_alpha], e=ExpressionPredefinedName [name=KW_Z]], StatementAssign [lhs=LHSSample [name=bird2, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]], color=KW_red], e=ExpressionFunctionApp [function=KW_green, e=ExpressionPixel [name=bird, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]]]], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], ShowStatement [e=ExpressionIdent [name=bird2]], StatementSleep [duration=ExpressionIntegerLiteral [value=4000]]]]]");
	}

	@Test
	public void testDec256() throws LexicalException, SyntaxException {
		String input = "abc{float c; boolean re; image abc; filename FILE2; image newImage [320,87];}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
	}

	@Test
	public void testStatementWrite() throws LexicalException, SyntaxException {
		String input = "b{write a to b;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=b, block=Block [decsOrStatements=[StatementWrite [sourceName=a, destName=b]]]]");
	}

	@Test
	public void testStatementInputSmallest() throws LexicalException, SyntaxException {
		String input = "b{input a from @ c;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=b, block=Block [decsOrStatements=[StatementInput [destName=a, e=ExpressionIdent [name=c]]]]]");
	}

	@Test
	public void testStatementShowSmallest() throws LexicalException, SyntaxException {
		String input = "b{show abc;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=b, block=Block [decsOrStatements=[ShowStatement [e=ExpressionIdent [name=abc]]]]]");
	}

	@Test
	public void testStatementSleepSmallest() throws LexicalException, SyntaxException {
		String input = "b{sleep 100;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=b, block=Block [decsOrStatements=[StatementSleep [duration=ExpressionIntegerLiteral [value=100]]]]]");
	}

	@Test
	public void testStatementIfSmallest() throws LexicalException, SyntaxException {
		String input = "b{if (true) {show abc;} ;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=b, block=Block [decsOrStatements=[StatementIf [guard=ExpressionBooleanLiteral [value=true], b=Block [decsOrStatements=[ShowStatement [e=ExpressionIdent [name=abc]]]]]]]]");
	}

	@Test
	public void testStatementWhileSmallest() throws LexicalException, SyntaxException {
		String input = "b{while (m) {sleep 100;} ;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=b, block=Block [decsOrStatements=[StatementWhile [guard=ExpressionIdent [name=m], b=Block [decsOrStatements=[StatementSleep [duration=ExpressionIntegerLiteral [value=100]]]]]]]]");
	}

	@Test
	public void testStatementAssignLHS0() throws LexicalException, SyntaxException {
		String input = "b{m := n;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=b, block=Block [decsOrStatements=[StatementAssign [lhs=LHSIdent [name=m], e=ExpressionIdent [name=n]]]]]");
	}

	@Test
	public void testStatementAssignLHS1Smallest() throws LexicalException, SyntaxException {
		String input = "b{m := abc[5,5];}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=b, block=Block [decsOrStatements=[StatementAssign [lhs=LHSIdent [name=m], e=ExpressionPixel [name=abc, pixelSelector=PixelSelector [ex=ExpressionIntegerLiteral [value=5], ey=ExpressionIntegerLiteral [value=5]]]]]]]");
	}

	@Test
	public void testStatementAssignLHS2Smallest() throws LexicalException, SyntaxException {
		String input = "b{m := (abc[10,10]);}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=b, block=Block [decsOrStatements=[StatementAssign [lhs=LHSIdent [name=m], e=ExpressionPixel [name=abc, pixelSelector=PixelSelector [ex=ExpressionIntegerLiteral [value=10], ey=ExpressionIntegerLiteral [value=10]]]]]]]");
	}

	@Test
	public void testOrExpresssion0Smallest() throws LexicalException, SyntaxException {
		String input = "b{ A := true | xyz | 100;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=b, block=Block [decsOrStatements=[StatementAssign [lhs=LHSIdent [name=A], e=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionBooleanLiteral [value=true], op=OP_OR, rightExpression=ExpressionIdent [name=xyz]], op=OP_OR, rightExpression=ExpressionIntegerLiteral [value=100]]]]]]");
	}

	@Test
	public void testOrExpresssion1Smallest() throws LexicalException, SyntaxException {
		String input = "b{ A := a<b ? 1 : 0;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
	}

	@Test
	public void testAndExpresssionSmallest() throws LexicalException, SyntaxException {
		String input = "b{ s := 5+2 == 9-2 != 6.2 & true & !false ;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=b, block=Block [decsOrStatements=[StatementAssign [lhs=LHSIdent [name=s], e=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=5], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=2]], op=OP_EQ, rightExpression=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=9], op=OP_MINUS, rightExpression=ExpressionIntegerLiteral [value=2]]], op=OP_NEQ, rightExpression=ExpressionFloatLiteral [value=6.2]], op=OP_AND, rightExpression=ExpressionBooleanLiteral [value=true]], op=OP_AND, rightExpression=ExpressionUnary [op=OP_EXCLAMATION, expression=ExpressionBooleanLiteral [value=false]]]]]]]");
	}

	@Test
	public void testEqExpresssionSmallest() throws LexicalException, SyntaxException {
		String input = "b{ s := 5+2 == 9-2 != 6.2;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=b, block=Block [decsOrStatements=[StatementAssign [lhs=LHSIdent [name=s], e=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=5], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=2]], op=OP_EQ, rightExpression=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=9], op=OP_MINUS, rightExpression=ExpressionIntegerLiteral [value=2]]], op=OP_NEQ, rightExpression=ExpressionFloatLiteral [value=6.2]]]]]]");
	}

	@Test
	public void testRelExpresssionSmallest() throws LexicalException, SyntaxException {
		String input = "b{ s := a+b < 5-2 > 65 >= 5**2 <= 6%2 ;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=b, block=Block [decsOrStatements=[StatementAssign [lhs=LHSIdent [name=s], e=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionIdent [name=a], op=OP_PLUS, rightExpression=ExpressionIdent [name=b]], op=OP_LT, rightExpression=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=5], op=OP_MINUS, rightExpression=ExpressionIntegerLiteral [value=2]]], op=OP_GT, rightExpression=ExpressionIntegerLiteral [value=65]], op=OP_GE, rightExpression=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=5], op=OP_POWER, rightExpression=ExpressionIntegerLiteral [value=2]]], op=OP_LE, rightExpression=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=6], op=OP_MOD, rightExpression=ExpressionIntegerLiteral [value=2]]]]]]]");
	}

	@Test
	public void testAddExpresssionSmallest() throws LexicalException, SyntaxException {
		String input = "b{ s := 10*10 + 5/a - b%c ;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=b, block=Block [decsOrStatements=[StatementAssign [lhs=LHSIdent [name=s], e=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=10], op=OP_TIMES, rightExpression=ExpressionIntegerLiteral [value=10]], op=OP_PLUS, rightExpression=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=5], op=OP_DIV, rightExpression=ExpressionIdent [name=a]]], op=OP_MINUS, rightExpression=ExpressionBinary [leftExpression=ExpressionIdent [name=b], op=OP_MOD, rightExpression=ExpressionIdent [name=c]]]]]]]");
	}

	@Test
	public void testMultExpresssionSmallest() throws LexicalException, SyntaxException {
		String input = "b{ s := 10*10**2; show 10/a%b;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=b, block=Block [decsOrStatements=[StatementAssign [lhs=LHSIdent [name=s], e=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=10], op=OP_TIMES, rightExpression=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=10], op=OP_POWER, rightExpression=ExpressionIntegerLiteral [value=2]]]], ShowStatement [e=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=10], op=OP_DIV, rightExpression=ExpressionIdent [name=a]], op=OP_MOD, rightExpression=ExpressionIdent [name=b]]]]]]");
	}

	@Test
	public void testPowerExpresssionSmallest() throws LexicalException, SyntaxException {
		String input = "b{ s := 10*10*2;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=b, block=Block [decsOrStatements=[StatementAssign [lhs=LHSIdent [name=s], e=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=10], op=OP_TIMES, rightExpression=ExpressionIntegerLiteral [value=10]], op=OP_TIMES, rightExpression=ExpressionIntegerLiteral [value=2]]]]]]");
	}

	@Test
	public void testUnaryExpresssionSmallest() throws LexicalException, SyntaxException {
		String input = "b{ if (!true) { \n "
				+ "         while (+ !true) { \n"
				+ "         c := -!false;\n"
				+ "			show true; \n"
				+ "  }; \n"
				+ "};\n"
				+ "}\n";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=b, block=Block [decsOrStatements=[StatementIf [guard=ExpressionUnary [op=OP_EXCLAMATION, expression=ExpressionBooleanLiteral [value=true]], b=Block [decsOrStatements=[StatementWhile [guard=ExpressionUnary [op=OP_PLUS, expression=ExpressionUnary [op=OP_EXCLAMATION, expression=ExpressionBooleanLiteral [value=true]]], b=Block [decsOrStatements=[StatementAssign [lhs=LHSIdent [name=c], e=ExpressionUnary [op=OP_MINUS, expression=ExpressionUnary [op=OP_EXCLAMATION, expression=ExpressionBooleanLiteral [value=false]]]], ShowStatement [e=ExpressionBooleanLiteral [value=true]]]]]]]]]]]");
	}

	@Test
	public void testPredefinedName() throws LexicalException, SyntaxException {
		String input = "abc{ show Z; b:= default_height; input length from @ sdefault_width; }";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=abc, block=Block [decsOrStatements=[ShowStatement [e=ExpressionPredefinedName [name=KW_Z]], StatementAssign [lhs=LHSIdent [name=b], e=ExpressionPredefinedName [name=KW_default_height]], StatementInput [destName=length, e=ExpressionIdent [name=sdefault_width]]]]]");
	}

	@Test
	public void testFunctnAppln() throws LexicalException, SyntaxException {
		String input = "xyz{ sleep sin(-x); \n "
				+ "show cos(+180); \n "
				+ "m := atan(-180); \n "
				+ "input km from @ abs(98.04); \n "
				+ "b := log (-1.414); \n "
				+ "if (cart_x(default_height)) {}; \n "
				+ "while (cart_y [x,y]) {} ;\n"
				+ "a[x,y]:= polar_a[180,190]; \n "
				+ "b[w,r] := polar_r[r[-190,-90.8], m]; \n "
				+ "if (int(int(1))) {};\n"
				+ "sleep float(int(s)); \n "
				+ "m := width [100.0,1.0]; \n "
				+ "cde := height[a,4]; \n "
				+ "input ident from @ red(<<a,d,b,c>>); \n"
				+ "m := blue[<<18,0,-8,0.9>>,<<65,2,w,r>>]; \n"
				+ "a := green(!w); \n"
				+ "b := alpha(false);}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=xyz, block=Block [decsOrStatements=[StatementSleep [duration=ExpressionFunctionApp [function=KW_sin, e=ExpressionUnary [op=OP_MINUS, expression=ExpressionIdent [name=x]]]], ShowStatement [e=ExpressionFunctionApp [function=KW_cos, e=ExpressionUnary [op=OP_PLUS, expression=ExpressionIntegerLiteral [value=180]]]], StatementAssign [lhs=LHSIdent [name=m], e=ExpressionFunctionApp [function=KW_atan, e=ExpressionUnary [op=OP_MINUS, expression=ExpressionIntegerLiteral [value=180]]]], StatementInput [destName=km, e=ExpressionFunctionApp [function=KW_abs, e=ExpressionFloatLiteral [value=98.04]]], StatementAssign [lhs=LHSIdent [name=b], e=ExpressionFunctionApp [function=KW_log, e=ExpressionUnary [op=OP_MINUS, expression=ExpressionFloatLiteral [value=1.414]]]], StatementIf [guard=ExpressionFunctionApp [function=KW_cart_x, e=ExpressionPredefinedName [name=KW_default_height]], b=Block [decsOrStatements=[]]], StatementWhile [guard=ExpressionFunctionAppWithPixel [name=KW_cart_y, e0=ExpressionIdent [name=x], e1=ExpressionIdent [name=y]], b=Block [decsOrStatements=[]]], StatementAssign [lhs=LHSPixel [name=a, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]], e=ExpressionFunctionAppWithPixel [name=KW_polar_a, e0=ExpressionIntegerLiteral [value=180], e1=ExpressionIntegerLiteral [value=190]]], StatementAssign [lhs=LHSPixel [name=b, pixelSelector=PixelSelector [ex=ExpressionIdent [name=w], ey=ExpressionIdent [name=r]]], e=ExpressionFunctionAppWithPixel [name=KW_polar_r, e0=ExpressionPixel [name=r, pixelSelector=PixelSelector [ex=ExpressionUnary [op=OP_MINUS, expression=ExpressionIntegerLiteral [value=190]], ey=ExpressionUnary [op=OP_MINUS, expression=ExpressionFloatLiteral [value=90.8]]]], e1=ExpressionIdent [name=m]]], StatementIf [guard=ExpressionFunctionApp [function=KW_int, e=ExpressionFunctionApp [function=KW_int, e=ExpressionIntegerLiteral [value=1]]], b=Block [decsOrStatements=[]]], StatementSleep [duration=ExpressionFunctionApp [function=KW_float, e=ExpressionFunctionApp [function=KW_int, e=ExpressionIdent [name=s]]]], StatementAssign [lhs=LHSIdent [name=m], e=ExpressionFunctionAppWithPixel [name=KW_width, e0=ExpressionFloatLiteral [value=100.0], e1=ExpressionFloatLiteral [value=1.0]]], StatementAssign [lhs=LHSIdent [name=cde], e=ExpressionFunctionAppWithPixel [name=KW_height, e0=ExpressionIdent [name=a], e1=ExpressionIntegerLiteral [value=4]]], StatementInput [destName=ident, e=ExpressionFunctionApp [function=KW_red, e=ExpressionPixelConstructor [alpha=ExpressionIdent [name=a], red=ExpressionIdent [name=d], green=ExpressionIdent [name=b], blue=ExpressionIdent [name=c]]]], StatementAssign [lhs=LHSIdent [name=m], e=ExpressionFunctionAppWithPixel [name=KW_blue, e0=ExpressionPixelConstructor [alpha=ExpressionIntegerLiteral [value=18], red=ExpressionIntegerLiteral [value=0], green=ExpressionUnary [op=OP_MINUS, expression=ExpressionIntegerLiteral [value=8]], blue=ExpressionFloatLiteral [value=0.9]], e1=ExpressionPixelConstructor [alpha=ExpressionIntegerLiteral [value=65], red=ExpressionIntegerLiteral [value=2], green=ExpressionIdent [name=w], blue=ExpressionIdent [name=r]]]], StatementAssign [lhs=LHSIdent [name=a], e=ExpressionFunctionApp [function=KW_green, e=ExpressionUnary [op=OP_EXCLAMATION, expression=ExpressionIdent [name=w]]]], StatementAssign [lhs=LHSIdent [name=b], e=ExpressionFunctionApp [function=KW_alpha, e=ExpressionBooleanLiteral [value=false]]]]]]");
	}


	@Test
	public void testDec6() throws LexicalException, SyntaxException {
		String input = "b{input IDENTIFIER from @ 1; " +
				"write IDENTIFIER to IDENTIFIER;" +
				"while (abc>def){}; " +
				"if (false){}; " +
				"show atan(1);" +
				"sleep Z; }";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=b, block=Block [decsOrStatements=[StatementInput [destName=IDENTIFIER, e=ExpressionIntegerLiteral [value=1]], StatementWrite [sourceName=IDENTIFIER, destName=IDENTIFIER], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=abc], op=OP_GT, rightExpression=ExpressionIdent [name=def]], b=Block [decsOrStatements=[]]], StatementIf [guard=ExpressionBooleanLiteral [value=false], b=Block [decsOrStatements=[]]], ShowStatement [e=ExpressionFunctionApp [function=KW_atan, e=ExpressionIntegerLiteral [value=1]]], StatementSleep [duration=ExpressionPredefinedName [name=KW_Z]]]]]");
	}


}
	

