/**
 * Starter code for CodeGenerator.java used n the class project in COP5556 Programming Language Principles 
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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.FieldVisitor;

import cop5556sp18.Types.Type;
import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
import cop5556sp18.AST.ExpressionBinary;
import cop5556sp18.AST.ExpressionBooleanLiteral;
import cop5556sp18.AST.ExpressionConditional;
import cop5556sp18.AST.ExpressionFloatLiteral;
import cop5556sp18.AST.ExpressionFunctionAppWithExpressionArg;
import cop5556sp18.AST.ExpressionFunctionAppWithPixel;
import cop5556sp18.AST.ExpressionIdent;
import cop5556sp18.AST.ExpressionIntegerLiteral;
import cop5556sp18.AST.ExpressionPixel;
import cop5556sp18.AST.ExpressionPixelConstructor;
import cop5556sp18.AST.ExpressionPredefinedName;
import cop5556sp18.AST.ExpressionUnary;
import cop5556sp18.AST.LHSIdent;
import cop5556sp18.AST.LHSPixel;
import cop5556sp18.AST.LHSSample;
import cop5556sp18.AST.PixelSelector;
import cop5556sp18.AST.Program;
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementIf;
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.StatementShow;
import cop5556sp18.AST.StatementSleep;
import cop5556sp18.AST.StatementWhile;
import cop5556sp18.AST.StatementWrite;
import cop5556sp18.Scanner.Kind;

import cop5556sp18.CodeGenUtils;

import java.util.HashMap;
import java.util.Map;

public class CodeGenerator implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */

	static final int Z = 255;

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	Map<String,Integer> indexMap = new HashMap<>();

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	final int defaultWidth;
	final int defaultHeight;
	// final boolean itf = false;
	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 * @param defaultWidth
	 *            default width of images
	 * @param defaultHeight
	 *            default height of images
	 */
	int slotNumber = 1;
	Label start;
	Label end;

	public CodeGenerator(boolean DEVEL, boolean GRADE, String sourceFileName,
			int defaultWidth, int defaultHeight) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO refactor and extend as necessary

		for (ASTNode node : block.decsOrStatements) {
			node.visit(this, null);
		}

		return null;
	}

	@Override
	public Object visitBooleanLiteral(
			ExpressionBooleanLiteral expressionBooleanLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionBooleanLiteral.value);
		return null;
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg)
			throws Exception {

		String fieldName = declaration.name;
		String fieldType = getASMType(declaration.type);
		declaration.slotNumber = slotNumber++;

		mv.visitLocalVariable(fieldName, fieldType, null,start, end, declaration.slotNumber);

		//CodeGenUtils.genLogTOS(GRADE, mv, Types.getType(declaration.type));
		if(declaration.type == Kind.KW_image) {
			if (declaration.height != null || declaration.width != null) {
				declaration.width.visit(this,null);
				declaration.height.visit(this,null);
			}else{
				mv.visitLdcInsn(defaultWidth);
				mv.visitLdcInsn(defaultHeight);
			}
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "makeImage",
					RuntimeImageSupport.makeImageSig, false);
			mv.visitVarInsn(ASTORE,declaration.slotNumber);
		}
		return null;
	}

	public String getASMType(Kind kind){
		String fieldType = null;
		if(kind == Kind.KW_int){
			fieldType = "I";
		}else if(kind == Kind.KW_float){
			fieldType = "F";
		}else if(kind == Kind.KW_boolean){
			fieldType = "Z";
		}else if(kind == Kind.KW_image){
			fieldType = "Ljava/awt/image/BufferedImage;";
		}else if(kind == Kind.KW_filename){
			fieldType = "Ljava/lang/String;";
		}
		return fieldType;
	}

	public String getASMType(Type type){
		String fieldType = null;
		if(type == Type.INTEGER){
			fieldType = "I";
		}else if(type == Type.FLOAT){
			fieldType = "F";
		}else if(type == Type.BOOLEAN){
			fieldType = "Z";
		}else if(type == Type.IMAGE){
			fieldType = "Ljava/awt/image/BufferedImage;";
		}else if(type == Type.FILE){
			fieldType = "Ljava/lang/String;";
		}
		return fieldType;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary,
			Object arg) throws Exception {

		Kind op = expressionBinary.op;
		if(expressionBinary.leftExpression.type == Type.INTEGER && expressionBinary.rightExpression.type == Type.INTEGER) {
			expressionBinary.leftExpression.visit(this,arg);
			expressionBinary.rightExpression.visit(this,arg);
			if (op == Kind.OP_PLUS) {
				mv.visitInsn(IADD);
			} else if (op == Kind.OP_MINUS) {
				mv.visitInsn(ISUB);
			} else if (op == Kind.OP_TIMES) {
				mv.visitInsn(IMUL);
			} else if (op == Kind.OP_DIV) {
				mv.visitInsn(IDIV);
			} else if (op == Kind.OP_MOD) {
				mv.visitInsn(IREM);
			} else if (op == Kind.OP_POWER) {
				mv.visitInsn(POP);
				mv.visitInsn(POP);
				expressionBinary.leftExpression.visit(this,arg);
				mv.visitInsn(I2D);
				expressionBinary.rightExpression.visit(this,arg);
				mv.visitInsn(I2D);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math","pow", "(DD)D", false);
				mv.visitInsn(Opcodes.D2I);
			} else if (op == Kind.OP_AND) {
				mv.visitInsn(IAND);
			} else if (op == Kind.OP_OR) {
				mv.visitInsn(IOR);
			} else if (op == Kind.OP_EQ) {
				Label f = new Label();
				mv.visitJumpInsn(IF_ICMPNE, f);
				mv.visitInsn(ICONST_1);
				Label t = new Label();
				mv.visitJumpInsn(GOTO, t);
				mv.visitLabel(f);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(t);
			} else if (op == Kind.OP_NEQ) {
				Label f = new Label();
				mv.visitJumpInsn(IF_ICMPEQ, f);
				mv.visitInsn(ICONST_1);
				Label t = new Label();
				mv.visitJumpInsn(GOTO, t);
				mv.visitLabel(f);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(t);
			} else if (op == Kind.OP_LE) {
				Label f = new Label();
				mv.visitJumpInsn(IF_ICMPGT, f);
				mv.visitInsn(ICONST_1);
				Label t = new Label();
				mv.visitJumpInsn(GOTO, t);
				mv.visitLabel(f);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(t);
			} else if (op == Kind.OP_LT) {
				Label f = new Label();
				mv.visitJumpInsn(IF_ICMPGE, f);
				mv.visitInsn(ICONST_1);
				Label t = new Label();
				mv.visitJumpInsn(GOTO, t);
				mv.visitLabel(f);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(t);
			} else if (op == Kind.OP_GE) {
				Label f = new Label();
				mv.visitJumpInsn(IF_ICMPLT, f);
				mv.visitInsn(ICONST_1);
				Label t = new Label();
				mv.visitJumpInsn(GOTO, t);
				mv.visitLabel(f);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(t);
			} else if (op == Kind.OP_GT) {
				Label f = new Label();
				mv.visitJumpInsn(IF_ICMPLE, f);
				mv.visitInsn(ICONST_1);
				Label t = new Label();
				mv.visitJumpInsn(GOTO, t);
				mv.visitLabel(f);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(t);
			}
		}else if(expressionBinary.leftExpression.type == Type.BOOLEAN && expressionBinary.rightExpression.type == Type.BOOLEAN){
				expressionBinary.leftExpression.visit(this,arg);
				expressionBinary.rightExpression.visit(this,arg);
				if (op == Kind.OP_AND) {
					mv.visitInsn(IAND);
				} else if (op == Kind.OP_OR) {
					mv.visitInsn(IOR);
				} else if (op == Kind.OP_EQ) {
					Label f = new Label();
					mv.visitJumpInsn(IF_ICMPNE, f);
					mv.visitInsn(ICONST_1);
					Label t = new Label();
					mv.visitJumpInsn(GOTO, t);
					mv.visitLabel(f);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(t);
				} else if (op == Kind.OP_NEQ) {
					Label f = new Label();
					mv.visitJumpInsn(IF_ICMPEQ, f);
					mv.visitInsn(ICONST_1);
					Label t = new Label();
					mv.visitJumpInsn(GOTO, t);
					mv.visitLabel(f);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(t);
				} else if (op == Kind.OP_LE) {
					Label f = new Label();
					mv.visitJumpInsn(IF_ICMPGT, f);
					mv.visitInsn(ICONST_1);
					Label t = new Label();
					mv.visitJumpInsn(GOTO, t);
					mv.visitLabel(f);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(t);
				} else if (op == Kind.OP_LT) {
					Label f = new Label();
					mv.visitJumpInsn(IF_ICMPGE, f);
					mv.visitInsn(ICONST_1);
					Label t = new Label();
					mv.visitJumpInsn(GOTO, t);
					mv.visitLabel(f);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(t);
				} else if (op == Kind.OP_GE) {
					Label f = new Label();
					mv.visitJumpInsn(IF_ICMPLT, f);
					mv.visitInsn(ICONST_1);
					Label t = new Label();
					mv.visitJumpInsn(GOTO, t);
					mv.visitLabel(f);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(t);
				} else if (op == Kind.OP_GT) {
					Label f = new Label();
					mv.visitJumpInsn(IF_ICMPLE, f);
					mv.visitInsn(ICONST_1);
					Label t = new Label();
					mv.visitJumpInsn(GOTO, t);
					mv.visitLabel(f);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(t);
				}
			}else if(expressionBinary.leftExpression.type == Type.FLOAT || expressionBinary.rightExpression.type == Type.FLOAT){

				expressionBinary.leftExpression.visit(this,arg);
				if(expressionBinary.leftExpression.type == Type.INTEGER) {
					mv.visitInsn(Opcodes.I2F);
				}
				expressionBinary.rightExpression.visit(this,arg);
				if(expressionBinary.rightExpression.type == Type.INTEGER) {
					mv.visitInsn(Opcodes.I2F);
				}

				if (op == Kind.OP_PLUS) {
					mv.visitInsn(FADD);
				} else if (op == Kind.OP_MINUS) {
					mv.visitInsn(FSUB);
				} else if (op == Kind.OP_TIMES) {
					mv.visitInsn(FMUL);
				} else if (op == Kind.OP_DIV) {
					mv.visitInsn(FDIV);
				} else if (op == Kind.OP_MOD) {
					mv.visitInsn(FREM);
				} else if (op == Kind.OP_POWER) {
					mv.visitInsn(POP);
					mv.visitInsn(POP);
					expressionBinary.leftExpression.visit(this,arg);
					if(expressionBinary.leftExpression.type == Type.INTEGER) {
						mv.visitInsn(Opcodes.I2F);
					}
					mv.visitInsn(Opcodes.F2D);
					expressionBinary.rightExpression.visit(this,arg);
					if(expressionBinary.rightExpression.type == Type.INTEGER) {
						mv.visitInsn(Opcodes.I2F);
					}
					mv.visitInsn(Opcodes.F2D);
					mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math","pow", "(DD)D", false);
					mv.visitInsn(Opcodes.D2F);
				}else if (op == Kind.OP_EQ) {
					Label f = new Label();
					mv.visitJumpInsn(IFNE, f);
					mv.visitInsn(ICONST_1);
					Label t = new Label();
					mv.visitJumpInsn(GOTO, t);
					mv.visitLabel(f);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(t);
				} else if (op == Kind.OP_NEQ) {
					Label f = new Label();
					mv.visitJumpInsn(IFEQ, f);
					mv.visitInsn(ICONST_1);
					Label t = new Label();
					mv.visitJumpInsn(GOTO, t);
					mv.visitLabel(f);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(t);
				} else if (op == Kind.OP_LE) {
					Label f = new Label();
					mv.visitJumpInsn(IFGT, f);
					mv.visitInsn(ICONST_1);
					Label t = new Label();
					mv.visitJumpInsn(GOTO, t);
					mv.visitLabel(f);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(t);
				} else if (op == Kind.OP_LT) {
					Label f = new Label();
					mv.visitJumpInsn(IFGE, f);
					mv.visitInsn(ICONST_1);
					Label t = new Label();
					mv.visitJumpInsn(GOTO, t);
					mv.visitLabel(f);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(t);
				} else if (op == Kind.OP_GE) {
					Label f = new Label();
					mv.visitJumpInsn(IFLT, f);
					mv.visitInsn(ICONST_1);
					Label t = new Label();
					mv.visitJumpInsn(GOTO, t);
					mv.visitLabel(f);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(t);
				} else if (op == Kind.OP_GT) {
					Label f = new Label();
					mv.visitJumpInsn(IFLE, f);
					mv.visitInsn(ICONST_1);
					Label t = new Label();
					mv.visitJumpInsn(GOTO, t);
					mv.visitLabel(f);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(t);
				}
			}

		return null;
	}

	@Override
	public Object visitExpressionConditional(
			ExpressionConditional expressionConditional, Object arg)
			throws Exception {
		expressionConditional.guard.visit(this,arg);
		Label f = new Label();
		mv.visitJumpInsn(IFEQ, f);
		expressionConditional.trueExpression.visit(this,arg);
		Label t = new Label();
		mv.visitJumpInsn(GOTO, t);
		mv.visitLabel(f);
		expressionConditional.falseExpression.visit(this,arg);
		mv.visitLabel(t);
		return null;
	}

	@Override
	public Object visitExpressionFloatLiteral(
			ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionFloatLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg,
			Object arg) throws Exception {
		expressionFunctionAppWithExpressionArg.e.visit(this,arg);
		Kind function = expressionFunctionAppWithExpressionArg.function;
		if(expressionFunctionAppWithExpressionArg.e.type == Type.INTEGER){
			if(function == Kind.KW_abs){
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math","abs", "(I)I", false);
			}else if(function == Kind.KW_red){
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimePixelOps", "getRed",
						RuntimePixelOps.getRedSig, false);
			}else if(function == Kind.KW_green){
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimePixelOps", "getGreen",
						RuntimePixelOps.getGreenSig, false);
			}else if(function == Kind.KW_blue){
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimePixelOps", "getBlue",
						RuntimePixelOps.getBlueSig, false);
			}else if(function == Kind.KW_alpha){
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimePixelOps", "getAlpha",
						RuntimePixelOps.getAlphaSig, false);
			}
			else if(function == Kind.KW_int){

			}
			else if(function == Kind.KW_float){
				mv.visitInsn(I2F);
			}
		}else if(expressionFunctionAppWithExpressionArg.e.type == Type.FLOAT){

			if (function == Kind.KW_sin) {
				mv.visitInsn(F2D);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
				mv.visitInsn(D2F);
			} else if (function == Kind.KW_cos) {
				mv.visitInsn(F2D);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
				mv.visitInsn(D2F);
			} else if (function == Kind.KW_atan) {
				mv.visitInsn(F2D);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "atan", "(D)D", false);
				mv.visitInsn(D2F);
			} else if (function == Kind.KW_log) {
				mv.visitInsn(F2D);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "log", "(D)D", false);
				mv.visitInsn(D2F);
			} else if (function == Kind.KW_abs) {
				mv.visitInsn(F2D);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "abs", "(D)D", false);
				mv.visitInsn(D2F);
			} else if(function == Kind.KW_int){
				mv.visitInsn(F2I);
			} else if(function == Kind.KW_float){

			}

		}else if(expressionFunctionAppWithExpressionArg.e.type == Type.IMAGE){
		 	if(function == Kind.KW_height){
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "getHeight",
						RuntimeImageSupport.getHeightSig, false);
			}else if(function == Kind.KW_width){
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "getWidth",
						RuntimeImageSupport.getWidthSig, false);
			}
		}
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(
			ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		if(expressionFunctionAppWithPixel.name == Kind.KW_cart_x){
			expressionFunctionAppWithPixel.e0.visit(this,arg);
			mv.visitInsn(F2D);
			expressionFunctionAppWithPixel.e1.visit(this,arg);
			mv.visitInsn(F2D);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
			mv.visitInsn(DMUL);
			mv.visitInsn(D2I);
		}else if(expressionFunctionAppWithPixel.name == Kind.KW_cart_y){
			expressionFunctionAppWithPixel.e0.visit(this,arg);
			mv.visitInsn(F2D);
			expressionFunctionAppWithPixel.e1.visit(this,arg);
			mv.visitInsn(F2D);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
			mv.visitInsn(DMUL);
			mv.visitInsn(D2I);
		}else if(expressionFunctionAppWithPixel.name == Kind.KW_polar_a){
			expressionFunctionAppWithPixel.e1.visit(this,arg);
			mv.visitInsn(I2D);
			expressionFunctionAppWithPixel.e0.visit(this,arg);
			mv.visitInsn(I2D);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "atan2", "(DD)D", false);
			mv.visitInsn(D2F);
		}else if(expressionFunctionAppWithPixel.name == Kind.KW_polar_r){
			expressionFunctionAppWithPixel.e0.visit(this,arg);
			mv.visitInsn(I2D);
			expressionFunctionAppWithPixel.e1.visit(this,arg);
			mv.visitInsn(I2D);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "hypot", "(DD)D", false);
			mv.visitInsn(D2F);
		}
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent,
			Object arg) throws Exception {
		//check for type and load accordingly
		if(expressionIdent.type == Type.INTEGER || expressionIdent.type == Type.BOOLEAN) {
			mv.visitVarInsn(ILOAD, expressionIdent.dec.slotNumber);
		}else if(expressionIdent.type == Type.FLOAT){
			mv.visitVarInsn(FLOAD, expressionIdent.dec.slotNumber);
		}else if(expressionIdent.type == Type.FILE){
			mv.visitVarInsn(ALOAD, expressionIdent.dec.slotNumber);
		}else if(expressionIdent.type == Type.IMAGE){
			mv.visitVarInsn(ALOAD, expressionIdent.dec.slotNumber);
		}
		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(
			ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		// This one is all done!
		mv.visitLdcInsn(expressionIntegerLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel,
			Object arg) throws Exception {
		mv.visitVarInsn(ALOAD, expressionPixel.dec.slotNumber);
		expressionPixel.pixelSelector.visit(this,arg);
		mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "getPixel",
				RuntimeImageSupport.getPixelSig, false);
		return null;
	}

	@Override
	public Object visitExpressionPixelConstructor(
			ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		expressionPixelConstructor.alpha.visit(this,arg);
		expressionPixelConstructor.red.visit(this,arg);
		expressionPixelConstructor.green.visit(this,arg);
		expressionPixelConstructor.blue.visit(this,arg);
		mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimePixelOps", "makePixel",
				RuntimePixelOps.makePixelSig, false);

		return null;
	}

	@Override
	public Object visitExpressionPredefinedName(
			ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		if(expressionPredefinedName.firstToken.kind == Kind.KW_Z){
			mv.visitLdcInsn(Z);
		}else if(expressionPredefinedName.firstToken.kind == Kind.KW_default_height){
			mv.visitLdcInsn(defaultHeight);
		}else if(expressionPredefinedName.firstToken.kind == Kind.KW_default_width){
			mv.visitLdcInsn(defaultWidth);
		}
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary,
			Object arg) throws Exception {
		expressionUnary.expression.visit(this,arg);
		if(expressionUnary.op == Kind.OP_EXCLAMATION){
			if(expressionUnary.expression.type == Type.INTEGER){
				mv.visitLdcInsn(-1);
				mv.visitInsn(IXOR);
			}else if(expressionUnary.expression.type == Type.BOOLEAN){
				mv.visitLdcInsn(1);
				mv.visitInsn(IXOR);
			}
		}else if(expressionUnary.op == Kind.OP_MINUS){
			if(expressionUnary.expression.type == Type.INTEGER){
				mv.visitInsn(INEG);
			}else if(expressionUnary.expression.type == Type.FLOAT){
				mv.visitInsn(FNEG);
			}
		}

		return null;
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg)
			throws Exception {
		if(lhsIdent.type == Kind.KW_int || lhsIdent.type == Kind.KW_boolean) {
			mv.visitVarInsn(ISTORE, lhsIdent.dec.slotNumber);
		}else if(lhsIdent.type == Kind.KW_float){
			mv.visitVarInsn(FSTORE, lhsIdent.dec.slotNumber);
		}else if(lhsIdent.type == Kind.KW_filename){
			mv.visitVarInsn(ASTORE, lhsIdent.dec.slotNumber);
		}else if(lhsIdent.type == Kind.KW_image){
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,"cop5556sp18/RuntimeImageSupport","deepCopy",RuntimeImageSupport.deepCopySig,false);
			mv.visitVarInsn(ASTORE, lhsIdent.dec.slotNumber);
		}
		return null;
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg)
			throws Exception {
		//mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
		mv.visitVarInsn(ALOAD,lhsPixel.dec.slotNumber);
		lhsPixel.pixelSelector.visit(this,arg);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC,"cop5556sp18/RuntimeImageSupport","setPixel",RuntimeImageSupport.setPixelSig,false);
		return null;
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg)
			throws Exception {
		mv.visitVarInsn(ALOAD,lhsSample.dec.slotNumber);
		lhsSample.pixelSelector.visit(this,arg);
		if(lhsSample.color ==  Kind.KW_alpha){
			mv.visitLdcInsn(RuntimePixelOps.ALPHA);
		}else if(lhsSample.color == Kind.KW_red){
			mv.visitLdcInsn(RuntimePixelOps.RED);
		}else if(lhsSample.color == Kind.KW_green){
			mv.visitLdcInsn(RuntimePixelOps.GREEN);
		}else if(lhsSample.color == Kind.KW_blue){
			mv.visitLdcInsn(RuntimePixelOps.BLUE);
		}
		mv.visitMethodInsn(Opcodes.INVOKESTATIC,"cop5556sp18/RuntimeImageSupport","updatePixelColor",RuntimeImageSupport.updatePixelColorSig,false);
		return null;
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg)
			throws Exception {
		pixelSelector.ex.visit(this,arg);
		pixelSelector.ey.visit(this,arg);
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		// cw = new ClassWriter(0); //If the call to mv.visitMaxs(1, 1) crashes,
		// it is
		// sometime helpful to
		// temporarily run it without COMPUTE_FRAMES. You probably
		// won't get a completely correct classfile, but
		// you will be able to see the code that was
		// generated.
		className = program.progName;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null,
				"java/lang/Object", null);
		cw.visitSource(sourceFileName, null);

		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();

		// add label before first instruction
		Label mainStart = new Label();
		Label mainEnd = new Label();
		start = mainStart;
		end = mainEnd;
		mv.visitLabel(mainStart);

		CodeGenUtils.genLog(DEVEL, mv, "entering main");
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart,
				mainEnd, 0);
		program.block.visit(this, arg);

		// generates code to add string to log
		CodeGenUtils.genLog(DEVEL, mv, "leaving main");

		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code

		mv.visitLabel(mainEnd);


		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the
		// constructor,
		// asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily change the parameter in the ClassWriter constructor
		// from COMPUTE_FRAMES to 0.
		// The generated classfile will not be correct, but you will at least be
		// able to see what is in it.
		mv.visitMaxs(0, 0);

		// terminate construction of main method
		mv.visitEnd();

		// terminate class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign,
			Object arg) throws Exception {
		statementAssign.e.visit(this,arg);
		statementAssign.lhs.visit(this,arg);
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg)
			throws Exception {
		statementIf.guard.visit(this,arg);
		Label f = new Label();
		mv.visitJumpInsn(IFEQ,f);
		statementIf.b.visit(this,arg);
		mv.visitLabel(f);
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg)
			throws Exception {
		mv.visitVarInsn(ALOAD,0);
		statementInput.e.visit(this,arg);
		mv.visitInsn(AALOAD);
		if(statementInput.dec.type == Kind.KW_int){
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer","parseInt", "(Ljava/lang/String;)I", false);
			mv.visitVarInsn(ISTORE,statementInput.dec.slotNumber);
		}else if(statementInput.dec.type == Kind.KW_float){
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float","parseFloat", "(Ljava/lang/String;)F", false);
			mv.visitVarInsn(FSTORE,statementInput.dec.slotNumber);
		}else if(statementInput.dec.type == Kind.KW_boolean){
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean","parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitVarInsn(ISTORE,statementInput.dec.slotNumber);
		}else if(statementInput.dec.type == Kind.KW_image){
			if(statementInput.dec.height != null && statementInput.dec.width != null) {
				statementInput.dec.width.visit(this,arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				statementInput.dec.height.visit(this,arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);

			}else{
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
			}
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,"cop5556sp18/RuntimeImageSupport","readImage",RuntimeImageSupport.readImageSig,false);
			mv.visitVarInsn(ASTORE,statementInput.dec.slotNumber);
		}else if(statementInput.dec.type == Kind.KW_filename){
			mv.visitVarInsn(ASTORE,statementInput.dec.slotNumber);
		}
		return null;
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg)
			throws Exception {
		/**
		 * TODO refactor and complete implementation.
		 * 
		 * For integers, booleans, and floats, generate code to print to
		 * console. For images, generate code to display in a frame.
		 * 
		 * In all cases, invoke CodeGenUtils.genLogTOS(GRADE, mv, type); before
		 * consuming top of stack.
		 */
		statementShow.e.visit(this, arg);
		Type type = statementShow.e.getType();
		switch (type) {
			case INTEGER : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(I)V", false);
			}
				break;
			case BOOLEAN : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(Z)V", false);
			}
			break; //commented out because currently unreachable. You will need
			// it.
			case FLOAT : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(F)V", false);
			}
			 break; //commented out because currently unreachable. You will need
			// it.
			case IMAGE : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "makeFrame",
						RuntimeImageSupport.makeFrameSig, false);

			}

		}
		return null;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg)
			throws Exception {
		statementSleep.duration.visit(this,arg);
		mv.visitInsn(Opcodes.I2L);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Thread","sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg)
			throws Exception {
		Label t = new Label();
		mv.visitLabel(t);
		statementWhile.guard.visit(this,arg);
		Label f = new Label();
		mv.visitJumpInsn(IFEQ,f);
		statementWhile.b.visit(this,arg);
		mv.visitJumpInsn(GOTO,t);
		mv.visitLabel(f);
		return null;
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg)
			throws Exception {

		mv.visitVarInsn(ALOAD,statementWrite.sourceDec.slotNumber);
		mv.visitVarInsn(ALOAD,statementWrite.destDec.slotNumber);
		statementWrite.destDec.visit(this,arg);
		mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "write",
				RuntimeImageSupport.writeSig, false);
		return null;
	}

}
