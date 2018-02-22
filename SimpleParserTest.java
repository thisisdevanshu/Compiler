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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Parser.SyntaxException;
import cop5556sp18.Scanner.LexicalException;

public class SimpleParserTest {

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
	 * because it lacks an identifier and a block. The test case passes because
	 * it expects an exception
	 *  
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = "";  //The input is the empty string.  
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
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
		parser.parse();
	}	
	
	
	//This test should pass in your complete parser.  It will fail in the starter code.
	//Of course, you would want a better error message. 
	@Test
	public void testDec0() throws LexicalException, SyntaxException {
		String input = "b{int c;}";
		Parser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void testDec1() throws LexicalException, SyntaxException {
		String input = "b{int c; " +
				"float b; " +
				"boolean c; " +
				"image d; " +
				"filename e; " +
				"image f [true,4]; }";
		Parser parser = makeParser(input);
		parser.parse();
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
		parser.parse();
	}

	@Test
	public void testDec3() throws LexicalException, SyntaxException {
		String input = "b{ " +
				"show +true;" +
				"red ( IDENTIFIER  [ 3+5+2-1-5 , 9**9/3%2 ] ):= 45 != 9;" +
				"sleep true  ?  123.56  :  1;" +
				"while(default_height){}; }";
		Parser parser = makeParser(input);
		parser.parse();
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
		parser.parse();
	}

	@Test
	public void testDec5() throws LexicalException, SyntaxException {
		String input = "a{ i:=sin(a);" +
				"k := ! default_height; }";
		Parser parser = makeParser(input);
		parser.parse();
	}

}
	

