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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Scanner.Kind;
import cop5556sp18.Parser;
import cop5556sp18.Scanner;
import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
import cop5556sp18.AST.Expression;
import cop5556sp18.AST.ExpressionBinary;
import cop5556sp18.AST.ExpressionIdent;
import cop5556sp18.AST.ExpressionIntegerLiteral;
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.StatementShow;
import cop5556sp18.AST.LHS;
import cop5556sp18.AST.LHSIdent;
import cop5556sp18.AST.LHSPixel;
import cop5556sp18.AST.LHSSample;
import cop5556sp18.AST.PixelSelector;
import cop5556sp18.AST.Program;
import cop5556sp18.AST.Statement;
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementWrite;
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


}
	

