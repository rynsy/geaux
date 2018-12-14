import java.io.*;
import java.util.List;
import java.util.ArrayList;

abstract class ASTNode {
	int linenum;
	int colnum;
	
	static PrintStream afile;	// File to generate JVM code into
    
    static int typeErrors = 0; // Total number of type errors found 
	static int cgErrors =  0;       // Total number of code gen errors 

	static int numberOfLocals =  0; // Total number of local CSX-lite vars
            
	static int labelCnt = 0;	// counter used to gen unique labels

	static void genIndent(int indent) {
		for (int i = 1; i <= indent; i += 1) {
			System.out.print("\t");
		}
	} // genIndent
	static void myAssert(boolean assertion){
		if (! assertion) {
			 throw new RuntimeException();
        }
	} // myAssert
	static void mustBe(boolean assertion) {
		if (! assertion) {
			throw new RuntimeException();
		}
	} // mustBe
	static void typeMustBe(int testType,int requiredType,String errorMsg) {
		if ((testType != Types.Error) && ((testType & requiredType) == 0) ) { 
			System.out.println(errorMsg);
			typeErrors++;
		}
	} // typeMustBe
	static void typesMustBeEqual(int type1,int type2,String errorMsg) {
		if ((type1 != Types.Error) && (type2 != Types.Error) &&
				(type1 != type2)) {
			System.out.println(errorMsg);
			typeErrors++;
		}
	} // typesMustBeEqual
	String error() {
		return "Error (line " + linenum + "): ";
	} // error

    String toJasminType(Types t) {
        switch (t.val) {
            case Types.Integer:
                return "I";
            case Types.Boolean:
                return "Z";
            case Types.Character:
                return "C";
            default:
                return "V";
        } // switch (t.val)
    }

    String toJasmin(Types t, Kinds k) {
        switch (k.val) {
            case Kinds.Var:
            case Kinds.Value:
            case Kinds.ScalarParam:
            case Kinds.Constant:
            case Kinds.Other:
            case Kinds.Function: // TODO may need to change, assuming functions can return arrays
                return toJasminType(t);
            case Kinds.ArrayParam:
            case Kinds.Array:
                return "[" + toJasminType(t);
            default:
                return (new Kinds(k.val)).toString();
        } // switch (k.val)
    } // convertKind

    void loadArray(Types t) {
        if (t.val == Types.Integer) {
            gen("iaload");
        } else if (t.val == Types.Character) {
            gen("caload");
        } else if (t.val == Types.Boolean) {
            gen("baload");
        } else {
            gen("aaload");
        }
    } // loadArray

    void storeArray(Types t) {
        if (t.val == Types.Integer) {
            gen("iastore");
        } else if (t.val == Types.Character) {
            gen("castore");
        } else if (t.val == Types.Boolean) {
            gen("bastore");
        } else {
            gen("aastore");
        }
    } // storeArray

    void createArray(Types t) {
        // Assumes that the size of the array is at the top of the stack
        // Leaves reference to array on stack
        switch (t.val) {
            case Types.Integer:
                gen("newarray", "int");
                break;
            case Types.Boolean:
                gen("newarray", "boolean");
                break;
            case Types.Character:
            case Types.String:
                gen("newarray", "char");
                break;
            default:
                mustBe(false);
        } // switch (elementType.type.val)
    } // createArray

	// generate an instruction w/ 0 operands
	static void    gen(String opcode){
        	afile.println("\t"+opcode);
	}

        // generate an instruction w/ 1 operand
	static void  gen(String opcode, String operand){
        	afile.println("\t"+opcode+"\t"+operand);
	}

        // generate an instruction w/ 1 operand
	static void  gen(String opcode, int operand){
        	afile.println("\t"+opcode+"\t"+operand);
	}


	//  generate an instruction w/ 2 operands
	static void  gen(String opcode, String operand1, String operand2){
        	afile.println("\t"+opcode+"\t"+ operand1+"  "+ operand2);
	}

	//  generate an instruction w/ 2 operands
	static void  gen(String opcode, String operand1, int operand2){
        	afile.println("\t"+opcode+"\t"+ operand1+"  "+operand2);
	}

	//      build label of form labeln
	String   buildlabel(int suffix){
                return "label"+suffix;
	}

	//      generate a label for an instruction
	void    genlab(String label){
        	afile.println(label+":");
	}
	public static SymbolTable st = new SymbolTable();

	ASTNode() {linenum = -1; colnum = -1;}
	ASTNode(int l, int c) {linenum = l;colnum=c;}
	boolean isNull() {return false;}
	void Unparse(int indent) {
         //redefined in subclass
    }
	void checkTypes() {
         //redefined in subclass
    }
    
    boolean codegen(PrintStream asmfile) {
        throw new Error();
    }; // This version of codegen
       // should never be called

    void cg(){
        // This member is normally overridden in subclasses
    } 

} // class ASTNode

class nullNode extends ASTNode {
	nullNode() {super();}
	boolean isNull() {return true;}
	void Unparse(int indent) {
        // do nothing 
    }
	void checkTypes() {
        // do nothing 
    }  
	void cg() {
        // do nothing 
    }  
} // class nullNode

class ProgramNode extends ASTNode {
	ProgramNode(IdentNode id, 
            List<VarDeclNode> vars,
            List<FuncDeclNode> funcs,
            int line, int col) {
		super(line, col);
        packageName = id;
        varList = vars;
        funcList = funcs;
	} // ProgramNode 

	void Unparse(int indent) {
		System.out.print(linenum + ":");
        genIndent(indent);
		System.out.print("package ");
        packageName.Unparse(indent);
        System.out.print("\n");
        for(int i = 0; i < varList.size(); i++) {
            VarDeclNode var = varList.get(i); 
            if (var != null && !var.isNull()) {
                var.Unparse(0);
            }
        }
        for(int i = 0; i < funcList.size(); i++) {
            FuncDeclNode func = funcList.get(i);
            if (func != null && !func.isNull()) {
                func.Unparse(0);
            }
        }
	} // Unparse

	void checkTypes() {
        for(int i = 0; i < varList.size(); i++) {
            VarDeclNode var = varList.get(i); 
            if (var != null && !var.isNull()) {
                var.checkTypes();
            }
        }
        for(int i = 0; i < funcList.size(); i++) {
            FuncDeclNode func = funcList.get(i);
            if (func != null && !func.isNull()) {
                func.checkTypes();
            }
        }
        SymbolInfo id = (SymbolInfo) st.globalLookup("main");
        if (id == null) {
			System.out.println(error() + "Main function is missing."); 
			typeErrors++;
        } else if (!funcList.get(funcList.size() - 1).main) {
			System.out.println(error() + "Last function is required to be main."); 
			typeErrors++;
        }
	} // checkTypes

	boolean isTypeCorrect() {
		checkTypes();
		return (typeErrors == 0);
	} // isTypeCorrect

	boolean codegen(PrintStream asmfile) {
        	afile = asmfile;
        	cg();
        	return (cgErrors == 0);
 	} // codegen

	void cg() {
        List<VarDeclNode> needInitialization = new ArrayList<VarDeclNode>();

        gen(".class","public",packageName.idname); 
        gen(".super","java/lang/Object");

        for (VarDeclNode var: varList) {
            // Initializing these as statics. In order to initialize with expressions,
            // we'll give these a value at the top of Main
            String name = var.varName.idname + " " 
                + toJasmin(var.varName.idinfo.type, var.varName.idinfo.kind);
            gen(".field", "public static", name);
            name = packageName.idname + "/" + name;
            SymbolInfo varinfo = (SymbolInfo) st.globalLookup(var.varName.idname);
            varinfo.invocation = name;
        }
        
        for (FuncDeclNode func: funcList) {
            if (!func.main) {
                func.name.idinfo.invocation = packageName.idname 
                    + "/" + func.name.idname 
                    + "(" + func.name.idinfo.typesig + ")" 
                    + toJasminType(func.returnType.type);
                func.cg();
            } else {
                func.name.idinfo.invocation = packageName.idname 
                    + "/" + "main([Ljava/lang/String;)V";
                gen(".method"," public static", "main([Ljava/lang/String;)V");
                gen(".limit","locals", 100);
                
                for (VarDeclNode var: varList) {
                    var.cg();
                }

                func.cg();
            }
        }
	} // cg

	private final IdentNode             packageName;
    private final List<VarDeclNode>     varList;
    private final List<FuncDeclNode>    funcList;
} // class ProgramNode

// abstract superclass; only subclasses are actually created
abstract class DeclNode extends ASTNode {
	DeclNode() {super();}
	DeclNode(int l, int c) {super(l, c);}
} // class DeclNode

class VarDeclNode extends DeclNode {
	VarDeclNode(IdentNode id, TypeNode t, ExprNode e, int line, int col) {
		super(line, col);
		varName = id;
		varType = t;
		initValue = e;
	} // VarDeclNode

    void Unparse(int indent) {
        System.out.print(linenum + ":");
        genIndent(indent);
        System.out.print("var ");
        varName.Unparse(0);
        System.out.print(" ");
        varType.Unparse(0);
        initValue.Unparse(0);
        System.out.println(";");
    } // Unparse

	void checkTypes() {
		SymbolInfo id;
		id = (SymbolInfo) st.globalLookup(varName.idname);
        if (id != null && id.kind.val == Kinds.Label) {
            // This is a global label, can't be redefined. 
			System.out.println(error() + id.name() 
                    + " is already declared as a label.");
			typeErrors++;
			varName.type = new Types(Types.Error);
        } else if (id != null) {
			System.out.println(error() + id.name() 
                    + " is already declared in this scope.");
			typeErrors++;
			varName.type = new Types(Types.Error);
            return;
        } else { 
            id = (SymbolInfo) st.localLookup(varName.idname);
            if (id == null) {
                id = new SymbolInfo(varName.idname,
                    new Kinds(Kinds.Var),varType.type);
                varName.type = varType.type;
                try {
                    st.insert(id);
                } catch (DuplicateException d) {
                    /* can't happen */
                } catch (EmptySTException e) {
                    /* can't happen */
                }
                varName.idinfo = id;
            } else {
                System.out.println(error() + id.name() 
                        + " is already declared.");
                typeErrors++;
                varName.type = new Types(Types.Error);
            } // id != null
        }

        varType.checkTypes();
        if (initValue != ExprNode.NULL) {
            initValue.checkTypes();

            typesMustBeEqual(varType.type.val, initValue.type.val,
                error() + "Both the left and right"
                + " hand sides of an assignment must "
                + "have the same type.");
        }
	} // checkTypes

	void cg() {
        if (varName.idinfo.invocation == "") {
            varName.idinfo.varIndex = numberOfLocals++;
            if (initValue != ExprNode.NULL) {
                initValue.cg();
            } else {
                if (varType.type.val == Types.Character) {
                    gen("ldc", 65);
                } else {
                    gen("ldc", 0);      
                }
            }
            gen("istore", varName.idinfo.varIndex);
        } else {
            if (initValue != ExprNode.NULL) {
                initValue.cg();
            } else {
                if (varType.type.val == Types.Character) {
                    gen("ldc", 65);
                } else {
                    gen("ldc", 0);     
                }
            }
            gen("putstatic", varName.idinfo.invocation);
        }
	} // cg

	public IdentNode varName;
	public TypeNode varType;
	public ExprNode initValue;
} // class VarDeclNode

class VarAsgDeclNode extends VarDeclNode {
	VarAsgDeclNode(IdentNode id, TypeNode t, ExprNode e, int line, int col) {
		super(id,t,e,line, col);
        varName = id;
        varType = t;
        initValue = e;
	} // VarAsgDeclNode

    void Unparse(int indent) {
        System.out.print(linenum + ":");
        genIndent(indent);
        System.out.print("var ");
        varName.Unparse(0);
        System.out.print(" ");
        varType.Unparse(0);
        System.out.print(" = ");
        initValue.Unparse(0);
        System.out.println(";");
    } // Unparse
	
	private final IdentNode varName;
	private final TypeNode varType;
	private final ExprNode initValue;
} // class VarAsgDeclNode

class VarArrayDeclNode extends VarDeclNode {
	VarArrayDeclNode(IdentNode id, TypeNode t, IntLitNode i, int line, int col) {
		super(id,t,ExprNode.NULL,line, col);
        varName = id;
        varType = t;
        arraySize = i;
	} // VarArrayDeclNode

    void Unparse(int indent) {
        genIndent(indent);
        System.out.print(linenum + ":" + "var");
        System.out.print(" ");
        varName.Unparse(0);
        System.out.print(" ");
        varType.Unparse(0);
        System.out.print("[");
        arraySize.Unparse(0);
        System.out.print("]");
        System.out.println(";");
    } // Unparse

    void checkTypes() {
        arraySize.checkTypes();
        varType.checkTypes();
		SymbolInfo id;
		id = (SymbolInfo) st.localLookup(varName.idname);
		if (id == null) {
			id = new SymbolInfo(varName.idname,
				new Kinds(Kinds.Array),varType.type, arraySize.value());
			varName.type = varType.type;
			try {
				st.insert(id);
			} catch (DuplicateException d) {
				/* can't happen */
			} catch (EmptySTException e) {
				/* can't happen */
			}
			varName.idinfo = id;
		} else {
			System.out.println(error() + id.name() + " is already declared.");
			typeErrors++;
			varName.type = new Types(Types.Error);
		} // id != null
        
        if (arraySize.value() < 1) {
			System.out.println(error() 
                    + "Arrays must have size greater than zero.");
			typeErrors++;
			varName.type = new Types(Types.Error);
        }
    } // checkTypes

    void cg() {
        arraySize.cg();
        createArray(varType.type);

        if (varName.idinfo.invocation == "") {         
            varName.idinfo.varIndex = numberOfLocals++;
            gen("astore", varName.idinfo.varIndex);
        } else {
            gen("putstatic", varName.idinfo.invocation);
        }
    } // cg

    private final IntLitNode arraySize;
	private final IdentNode varName;
	private final TypeNode varType;
} // class VarArrayDeclNode

class VarConstDeclNode extends VarDeclNode {
	VarConstDeclNode(IdentNode id, ExprNode e, int line, int col) {
		super(id, TypeNode.NULL, e, line, col);
        varName = id;
        initValue = e;
	} // VarConstDeclNode

    void Unparse(int indent) {
        genIndent(indent);
        System.out.print(linenum + ":" + "const");
        System.out.print(" ");
        varName.Unparse(0);
        System.out.print(" = ");
        initValue.Unparse(0);
        System.out.println(";");
    } // Unparse

    void checkTypes() {
        initValue.checkTypes();
		SymbolInfo id;
        int size = 1;
		id = (SymbolInfo) st.localLookup(varName.idname);
        if (initValue instanceof StrLitNode) {
            size = ((StrLitNode) initValue).length();
        } 
        if (id == null) {
			id = new SymbolInfo(varName.idname,
				new Kinds(Kinds.Constant),initValue.type, size);
			varName.type = initValue.type;
			try {
				st.insert(id);
			} catch (DuplicateException d) {
				/* can't happen */
			} catch (EmptySTException e) {
				/* can't happen */
			}
			varName.idinfo = id;
		} else {
			System.out.println(error() + id.name() + " is already declared.");
			typeErrors++;
			varName.type = new Types(Types.Error);
		} // id != null
    } // checkTypes

    private final IdentNode varName;
    private final ExprNode initValue;
} // class VarConstDeclNode

class VarDeclsNode extends DeclNode { 
	VarDeclsNode(VarDeclNode v, VarDeclsNode vs, int line, int col) {
		super(line, col);
        thisVar = v;
        moreVars = vs;
	} // VarDeclsNode
    VarDeclsNode() {
        // do nothing
    }

    public List<VarDeclNode> toList() {
        List<VarDeclNode> list = new ArrayList<VarDeclNode>();
        VarDeclsNode current = moreVars;
        if (thisVar != null) {
            list.add(thisVar);
        }
        while (current != null && !current.isNull()) {
            list.add(current.thisVar);
            current = current.moreVars;
        }
        return list;
    } // toList

    static nullVarDeclsNode NULL = new nullVarDeclsNode(); 
	private VarDeclNode thisVar;
	private VarDeclsNode moreVars;
} // class VarDeclsNode

class nullVarDeclsNode extends VarDeclsNode {
	nullVarDeclsNode() {
        // do nothing 
    }
	boolean   isNull() {return true;}
	void Unparse(int indent) {
        // do nothing 
    }
	void checkTypes() {
        // do nothing 
    }  
    void cg() {
        // do nothing 
    }
} // class nullVarDeclsNode

class ConstDeclNode extends DeclNode {
	ConstDeclNode(IdentNode id,  ExprNode e, int line, int col) {
		super(line, col);
		constName = id;
		constValue = e;
	} // ConstDeclNode

	private final IdentNode constName;
	private final ExprNode constValue;
} // class ConstDeclNode

class ArrayDeclNode extends DeclNode {
	ArrayDeclNode(IdentNode id, TypeNode t, IntLitNode lit, int line, int col) {
		super(line, col);
		arrayName = id;
		elementType = t;
		arraySize = lit;
	} // ArrayDeclNode

	private final IdentNode arrayName;
	private final TypeNode elementType;
	private final IntLitNode arraySize;
} // class ArrayDeclNode

abstract class TypeNode extends ASTNode {
	TypeNode() {super();}
	TypeNode(int l, int c, Types t) {super(l, c);type = t;}
	static nullTypeNode NULL = new nullTypeNode();
	Types type; // Used for typechecking -- the type of this typeNode
} // class TypeNode

class nullTypeNode extends TypeNode {
	nullTypeNode() {
        // do nothing 
    }
	boolean isNull() {return true;}
	void Unparse(int indent) {
        // do nothing 
    }
	void checkTypes() {type = new Types(Types.Void);}
} // class nullTypeNode

class IntTypeNode extends TypeNode {
	IntTypeNode(int line, int col) {super(line,col,new Types(Types.Integer));}
    void Unparse(int indent) {
        System.out.print("int");
    } // Unparse
} // class IntTypeNode

class BoolTypeNode extends TypeNode {
	BoolTypeNode(int line, int col) {super(line,col,new Types(Types.Boolean));}
    void Unparse(int indent) {
        System.out.print("bool");
    } // Unparse
} // class BoolTypeNode

class CharTypeNode extends TypeNode {
	CharTypeNode(int line, int col) {super(line,col,new Types(Types.Character));}
    void Unparse(int indent) {
        System.out.print("char");
    } // Unparse
} // class CharTypeNode

class VoidTypeNode extends TypeNode {
	VoidTypeNode(int line, int col) {super(line,col,new Types(Types.Void));}
    void Unparse(int indent) {
        System.out.print("void");
    } // Unparse
} // class VoidTypeNode

class FuncDeclNode extends ASTNode {
	FuncDeclNode(IdentNode id, List<ArgDeclNode> a, TypeNode t,
			BlockNode b, int line, int col) {
		super(line, col);
		name = id;
		args = a;
		returnType = t;
        body = b;
	} // FuncDeclNode

    void Unparse(int indent) {
        System.out.print(linenum + ":");
        genIndent(indent);
        System.out.print("func ");
        name.Unparse(0);
        System.out.print(" (");
        for( int i = 0; i < args.size(); i++ ) {
            args.get(i).Unparse(0);
            if (i <= args.size() - 2) {
                System.out.print(", ");
            }
        }
        System.out.print(") ");
        if (!returnType.isNull()) {
            returnType.Unparse(0);
        }
        System.out.print("\n");
        if (body != null && !body.isNull()) { 
            body.Unparse(indent+1);
        }
    } // Unparse

    void checkTypes() {
        returnType.checkTypes();
		SymbolInfo id;
		id = (SymbolInfo) st.localLookup(name.idname);
		if (id == null) {
			id = new SymbolInfo(name.idname,
				new Kinds(Kinds.Function),returnType.type);
			name.type = returnType.type;
			try {
				st.insert(id);
			} catch (DuplicateException d) {
				/* can't happen */
			} catch (EmptySTException e) {
				/* can't happen */
			}
			name.idinfo = id;
            if ((main = "main".equals(name.idname))) {
                typeMustBe(returnType.type.val, (Types.Void), 
                        error() + "The main function must be void.");
                if (!args.isEmpty()) {
                    typeMustBe(Types.String, Types.Void, 
                            error() + "Main function cannot have parameters");
                }
            }
		} else {
			System.out.println(error() + id.name() + " is already declared.");
			typeErrors++;
			name.type = new Types(Types.Error);
		} // id != null

        st.openScope(); // For evaluating function arguments/body

        ArgDeclNode arg;
        String signature = "";
        // check argument types and build type-signature
        for (int i = 0; i < args.size(); i++) {
            arg = args.get(i);
            arg.checkTypes();   
            signature += arg.typeSignature();
        }
		
        if (name.idinfo != null) {
            name.idinfo.typesig = signature; 
		} 

        body.checkTypes();
        if (!body.checkExits(returnType.type)) {
            System.out.println(error() + "Only void functions can omit return statements."); 
            typeErrors++;
            name.type = new Types(Types.Error);
        }

        try {
            st.closeScope();
        } catch (EmptySTException e) {
            // do nothing     
        }
    } // checkTypes

    void cg() {
        // main is generated in ProgramNode
        if (!main) {
            gen(".method"," public static", name.idname + "(" + name.idinfo.typesig + ")" + toJasminType(returnType.type));
            int locals = args.size() + body.decls.size();
            locals = (locals == 0) ? 1 : locals;
            gen(".limit","locals", locals); 
            numberOfLocals = 0;
        } // main is generated in ProgramNode

        for (ArgDeclNode arg : args) {
            arg.cg();
        }
        body.cg();
        gen(".limit","stack",100); 
        gen(".end","method");
    } // cg

    public boolean main;  
	public IdentNode name;
	public List<ArgDeclNode> args;
	public TypeNode returnType;
    public BlockNode body;
} // class FuncDeclNode 

class FuncDeclsNode extends ASTNode {
	FuncDeclsNode(FuncDeclNode f, FuncDeclsNode fs, int line, int col) {
		super(line, col);
        func = f;
        moreFuncs = fs;
	} // FuncDeclsNode
    FuncDeclsNode() {
        // do nothing 
    }

    public List<FuncDeclNode> toList() {
        List<FuncDeclNode> list = new ArrayList<FuncDeclNode>();
        FuncDeclsNode current = moreFuncs;
        list.add(func);
        while (current != null && !current.isNull()) {
            list.add(current.func);
            current = current.moreFuncs;
        }
        return list;
    } // toList

    static nullFuncDeclsNode NULL = new nullFuncDeclsNode();
	private FuncDeclNode  func;
    private FuncDeclsNode moreFuncs;
} // class FuncDeclsNode 

class nullFuncDeclsNode extends FuncDeclsNode {
	nullFuncDeclsNode() {
        // do nothing 
    }
	boolean   isNull() {return true;}
	void Unparse(int indent) {
        // do nothing 
    }
	void checkTypes() {
        // do nothing 
    }  
	void cg() {
        // do nothing 
    }  
} // class nullFuncDeclsNode 

// abstract superclass; only subclasses are actually created
abstract class ArgDeclNode extends ASTNode {
	ArgDeclNode() {super();}
	ArgDeclNode(int l, int c) {super(l, c);}
    void checkTypes() {
        // do nothing
    }
    String typeSignature() {return "";}
    void cg() {
        // do nothing
    }
} // class ArgDeclNode

class ArgDeclsNode extends ASTNode {
	ArgDeclsNode() {
        // do nothing 
    }
	ArgDeclsNode(ArgDeclNode arg, ArgDeclsNode args,
			int line, int col) {
		super(line, col);
		thisDecl = arg;
		moreDecls = args;
	} // ArgDeclsNode

	static nullArgDeclsNode NULL = new nullArgDeclsNode();

    public List<ArgDeclNode> toList() {
        List<ArgDeclNode> list = new ArrayList<ArgDeclNode>();
        ArgDeclsNode current = moreDecls;
        if (thisDecl != null) {
            list.add(thisDecl);
        }
        while (current != null && !current.isNull()) {
            list.add(current.thisDecl);
            current = current.moreDecls;
        }
        return list;
    } // toList

    void checkTypes() {
        thisDecl.checkTypes();
        moreDecls.checkTypes();
    } // checkTypes

	private ArgDeclNode thisDecl;
	private ArgDeclsNode moreDecls;
} // class ArgDeclsNode 

class nullArgDeclsNode extends ArgDeclsNode {
	nullArgDeclsNode() {
        // do nothing 
    }
	boolean   isNull() {return true;}
	void Unparse(int indent) {
        // do nothing 
    }
	void checkTypes() {
        // do nothing 
    }  
    void cg() {
        // do nothing 
    }
} // class nullArgDeclsNode 

class ArrayArgDeclNode extends ArgDeclNode {
	ArrayArgDeclNode(IdentNode id, TypeNode t, int line, int col) {
		super(line, col);
		argName = id;
		elementType = t;
	} // ArrayArgDeclNode

    void Unparse(int indent) {
        genIndent(indent);
        argName.Unparse(0);
        System.out.print(" [ ] ");
        elementType.Unparse(0);
    } // Unparse

    void checkTypes() {
		SymbolInfo id;
		id = (SymbolInfo) st.localLookup(argName.idname);
		if (id == null) {
			id = new SymbolInfo(argName.idname,
				new Kinds(Kinds.Array),elementType.type);
			argName.type = elementType.type;
			try {
				st.insert(id);
			} catch (DuplicateException d) {
				/* can't happen */
			} catch (EmptySTException e) {
				/* can't happen */
			}
			argName.idinfo = id;
		} else {
			System.out.println(error() + id.name() + " is already declared.");
			typeErrors++;
			argName.type = new Types(Types.Error);
		} // id != null
    } // checkTypes

    String typeSignature() {
        return toJasmin(elementType.type, new Kinds(Kinds.ArrayParam));
    } // typeSignature

    void cg() {
        argName.idinfo.varIndex = numberOfLocals++;
    } // cg

	private final IdentNode argName;
	private final TypeNode elementType;
} // class ArrayArgDeclNode 

class ValArgDeclNode extends ArgDeclNode {
	ValArgDeclNode(IdentNode id, TypeNode t, int line, int col) {
		super(line, col);
		argName = id;
		argType = t;
	} // ValArgDeclNode
    
    void Unparse(int indent) {
        genIndent(indent);
        argName.Unparse(0);
        System.out.print(" ");
        argType.Unparse(0);
    } // Unparse

    void checkTypes() {
		SymbolInfo id;
		id = (SymbolInfo) st.localLookup(argName.idname);
		if (id == null) {
			id = new SymbolInfo(argName.idname,
				new Kinds(Kinds.Var),argType.type);
			argName.type = argType.type;
			try {
				st.insert(id);
			} catch (DuplicateException d) {
				/* can't happen */
			} catch (EmptySTException e) {
				/* can't happen */
			}
			argName.idinfo = id;
		} else {
			System.out.println(error() + id.name() + " is already declared.");
			typeErrors++;
			argName.type = new Types(Types.Error);
		} // id != null
    } // checkTypes

    String typeSignature() {
        return toJasmin(argType.type, new Kinds(Kinds.ScalarParam));
    } // typeSignature

    void cg() {
        argName.idinfo.varIndex = numberOfLocals++;
    } // cg

	private final IdentNode argName;
	private final TypeNode argType;
} // class ValArgDeclNode 

// abstract superclass; only subclasses are actually created
abstract class StmtNode extends ASTNode {
	StmtNode() {super();}
	StmtNode(int l, int c) {super(l, c);}
	static nullStmtNode NULL = new nullStmtNode();
} // class StmtNode

class nullStmtNode extends StmtNode {
	nullStmtNode() {
        // do nothing 
    }
	boolean   isNull() {return true;}
	void Unparse(int indent) {
        // do nothing 
    }
	void checkTypes() {
        // do nothing 
    }  
	void cg() {
        // do nothing 
    }  
} // class nullStmtNode 

class StmtsNode extends ASTNode {
	StmtsNode(StmtNode stmt, StmtsNode stmts, int line, int col) {
		super(line, col);
		thisStmt = stmt;
		moreStmts = stmts;
	} // StmtsNode
	StmtsNode() {
        // do nothing 
    }

    public List<StmtNode> toList() {
        List<StmtNode> list = new ArrayList<StmtNode>();
        StmtsNode current = moreStmts;
        if (thisStmt != null) {
            list.add(thisStmt);
        }
        while (current != null && !current.isNull()) {
            list.add(current.thisStmt);
            current = current.moreStmts;
        }
        return list;
    } // toList

	void Unparse(int indent) {
		thisStmt.Unparse(indent);
		moreStmts.Unparse(indent);
	} // Unparse

	void cg() {
        thisStmt.cg();
        moreStmts.cg();
	} // cg

	static nullStmtsNode NULL = new nullStmtsNode();
	private StmtNode thisStmt;
	private StmtsNode moreStmts;
} // class StmtsNode 

class nullStmtsNode extends StmtsNode {
	nullStmtsNode() {
        // do nothing 
    }
	boolean   isNull() {return true;}
	void Unparse(int indent) {
        // do nothing 
    }
	void checkTypes() {
        // do nothing 
    }  
} // class nullstmtsnode 

class AsgNode extends StmtNode {
	AsgNode(NameNode n, ExprNode e, int line, int col) {
		super(line, col);
		target = n;
		source = e;
	} // AsgNode

	void Unparse(int indent) {
		System.out.print(linenum + ":");
		genIndent(indent);
		target.Unparse(0);
		System.out.print(" = ");
		source.Unparse(0);
		System.out.println(";");
	} // Unparse

	void checkTypes() { 
		target.checkTypes();
		source.checkTypes();

        switch (target.kind.val) {
            case Kinds.Constant:
                typeMustBe(target.kind.val, ~(Kinds.Constant), // Always false
                        error() + "Assignment into constant variables"
                        + " is forbidden."); 
                break;
            case Kinds.Array:
                switch (source.kind.val) { 
                    case Kinds.Var:
                        if (target.subscriptVal != ExprNode.NULL) {
                            typesMustBeEqual(source.type.val, target.type.val,
                                error() + "Both the left and right"
                                + " hand sides of an assignment must "
                                + "have the same type.");
                        } else {
                            typesMustBeEqual(Types.Integer, Types.Boolean,
                                error() + "Cannot assign a value to a whole array.");
                        }
                        break;
                    case Kinds.Array:
                        typesMustBeEqual(source.type.val, target.type.val,
                            error() + "Both the left and right"
                            + " hand sides of an assignment must "
                            + "have the same type.");
                        if ( ((NameNode) source).idinfo.size != target.idinfo.size) {
                            typesMustBeEqual(Types.Integer, Types.String,
                                error() + "Arrays can only be assigned" 
                                + " arrays of the same type with equal size."); 
                        }
                        break;
                    case Kinds.Value:
                        // The only way to load a value into a character array is 
                        // if the RHS is a String that is the same size as the character array.
                        if (target.type.val == Types.Character 
                                && source.type.val == Types.String) {
                            if (target.idinfo.size != ((StrLitNode)source).length()) {
                                typesMustBeEqual(Types.Integer, Types.String, // Always false
                                    error() + "Character arrays can only receive"
                                    + " String value equal to the size of the array.");
                            }
                        } else {
                            typesMustBeEqual(source.type.val, target.type.val,
                                error() + "Both the left and right"
                                + " hand sides of an assignment must "
                                + "have the same type.");
                        } // target.type.val != Types.Character || source.type.val != Types.String
                        break;
                    case Kinds.Constant:
                        if ( !(source.type.val == Types.String 
                                    && ((NameNode) source).idinfo.size == target.idinfo.size)) {
                            typesMustBeEqual(Types.Integer, Types.String, // Always false
                                error() + "Character arrays can only receive String"
                                + " value equal to the size of the array."); 
                        }  
                        break;
                    default:
                        mustBe(false); 
                        break;
                } // switch (source.kind.val)
                break;
            case Kinds.Var:
                typesMustBeEqual(source.type.val, target.type.val,
                    error() + "Both the left and right"
                    + " hand sides of an assignment must "
                    + "have the same type.");
                break;
            case Kinds.Value:
                typesMustBeEqual(Types.Integer, Types.Boolean,
                    error() + "Cannot assign into a value.");
                break;
            case Kinds.Function:
                typesMustBeEqual(Types.Integer, Types.Boolean,
                    error() + "Cannot assign into a function.");
                break;
            case Kinds.Label:
                typesMustBeEqual(Types.Integer, Types.Boolean,
                    error() + "Cannot assign into a label.");
                break;
            default:
                /*
                 * Nothing wrong with the variables, but they
                 * don't have a Kind that we've accounted for.
                 */
                if (source.type.val != Types.Error 
                    && target.type.val != Types.Error) {
                    System.out.println(error() + " Couldn't discern"
                            + " what kind of variables target and source are");
                }
                break;
        } // switch (target.kind.val)
	} // checkTypes

	void cg() {
        
        String targetRef = "", targetStore = "";
        if ( target.idinfo.invocation != "" ) {
            targetRef = "getstatic " + target.idinfo.invocation;
            targetStore = "putstatic " + target.idinfo.invocation;
        } else {                
            targetRef = "aload " + target.varName.idinfo.varIndex; 
            if (target.kind.val == Kinds.Array) {
                targetStore = "astore " + target.varName.idinfo.varIndex;
            } else {
                targetStore = "istore " + target.varName.idinfo.varIndex;
            }
        }

        if (target.type.val == Types.Character              // Can assume target is an array
                    && source.type.val == Types.String) {
            
            String rhs = ((StrLitNode) source).value().replaceAll("\"", "");
            gen("ldc", rhs.length());
            gen("newarray", "char");
            for( int i = 0; i < rhs.length(); i++ ) {
                gen("dup");       //keep an extra reference 
                gen("ldc", i);
                gen("ldc", (int) rhs.charAt(i));
                gen("castore");
            }
            // should still be array ref here.
            gen(targetStore);
        } else if (source.kind.val == Kinds.Array
                && target.kind.val == Kinds.Array
                && target.subscriptVal == ExprNode.NULL) {  
            /*
             *  Whether or not source.subscriptVal is set,
             *  we're cloning the whole array into target
             */

            String topOfLoop = buildlabel(labelCnt++);
       
            // for i = source.length - 1, ..., 0
            source.cg();
            gen("arraylength");
            gen("ldc", 1);
            gen("isub");
          
            // target.i = source.i; i = i - 1
            genlab(topOfLoop);
            gen("dup");
            gen(targetRef);
            gen("swap");
            gen("dup");
            source.cg();
            gen("swap");
            loadArray(source.type);
            storeArray(target.type); 
            gen("ldc", 1);
            gen("isub");
            gen("dup");
            gen("ifge", topOfLoop);
        
            // leaves nothing on stack
        } else if (target.kind.val == Kinds.Array
                && target.subscriptVal != ExprNode.NULL) {
            gen(targetRef);
            target.subscriptVal.cg();
            source.cg();                // Hopefully this is a value and not a reference
            storeArray(target.type);
        } else {
            source.cg();
            gen(targetStore);
        }
	} // cg

	private final NameNode target;
	private final ExprNode source;
} // class AsgNode 

class IfThenNode extends StmtNode {
	IfThenNode(ExprNode e, BlockNode s1, BlockNode s2, int line, int col) {
		super(line, col);
		condition = e;
		thenPart = s1;
		elsePart = s2;
	} // IfThenNode

	void Unparse(int indent) {
		System.out.print(linenum + ":");
		genIndent(indent);
		System.out.print("if (");
		condition.Unparse(0);
		System.out.println(")");
		thenPart.Unparse(indent+1);
		if (elsePart != BlockNode.NULL) {
            System.out.print(linenum + ":");
            genIndent(indent);
            System.out.println("else");
            elsePart.Unparse(indent+1);
        }
	} // Unparse

	void checkTypes() {
		condition.checkTypes();
		typeMustBe(condition.type.val, Types.Boolean,
			error() + "The control expression of an" +
			" if must be a bool.");
		thenPart.checkTypes();
        elsePart.checkTypes();
	} // checkTypes

	void cg() {
        String out = buildlabel(labelCnt++);
        condition.cg();
        if (elsePart != BlockNode.NULL) {
            String elseLab;
            elseLab = buildlabel(labelCnt++);
            gen("ifeq",elseLab);
            thenPart.cg();
            gen("goto", out);
            genlab(elseLab);
            elsePart.cg();
            genlab(out);
        } else {
            gen("ifeq",out);
            thenPart.cg();
            genlab(out);
        }
	} // cg

	private final ExprNode  condition;
	private final BlockNode thenPart;
	private final BlockNode elsePart;
} // class IfThenNode 

class ForNode extends StmtNode {
	ForNode(IdentNode l, ExprNode cond, BlockNode body, int line, int col) {
		super(line, col);
		label = l;
		condition = cond;
		loopBody = body;
	} // ForNode

	void Unparse(int indent) {
		System.out.print(linenum + ":");
		genIndent(indent);
        if (label != IdentNode.NULL) {
            label.Unparse(0);
            System.out.print(": ");
        }

		System.out.print("for ");
        condition.Unparse(0);
        System.out.print("\n");
        loopBody.Unparse(indent+1);
	} // Unparse

    void checkTypes() {
		SymbolInfo id;
        st.openScope();
        if (label != IdentNode.NULL) {
            id = (SymbolInfo) st.localLookup(label.idname);
            if (id == null) {
                id = new SymbolInfo(label.idname,
                    new Kinds(Kinds.Label),new Types(Types.Void));
                try {
                    st.insert(id);
                } catch (DuplicateException d) {
                    /* can't happen */
                } catch (EmptySTException e) {
                    /* can't happen */
                }
                label.idinfo = id;
            } else {
                System.out.println(error() + id.name() 
                        + " is already declared and can't be used as a label.");
                typeErrors++;
                label.type = new Types(Types.Error);
            } // id != null

            typeMustBe(label.kind.val, Kinds.Label,
                error() + "Variables of kind " + label.kind 
                + " cannot be used as a label."); 
        } 
		condition.checkTypes();
		typeMustBe(condition.type.val, Types.Boolean,
			error() + "The control expression of a" +
			" for must be a bool.");
		loopBody.checkTypes();
        try {
            st.closeScope();
        } catch (EmptySTException e) {
            // do nothing
        }
    } // checkTypes

    void cg() {
        String top = buildlabel(labelCnt++);
        String out = buildlabel(labelCnt++);

        if (label != IdentNode.NULL) {
            label.idinfo.headOfLoop = top;
            label.idinfo.tailOfLoop = out;
        }
        genlab(top);
        condition.cg();
        gen("ifeq", out);
        loopBody.cg();
        gen("goto", top);
        genlab(out);
    } // cg

	private final IdentNode  label;
	private final ExprNode  condition;
	private final BlockNode loopBody;
} // class ForNode 

class ReadNode extends StmtNode {
	ReadNode() {
        // do nothing 
    }
	ReadNode(NameNode n, ReadNode rn, int line, int col) {
		super(line, col);
		 targetVar = n;
		 moreReads = rn;
	} // ReadNode
	static nullReadNode NULL = new nullReadNode();

    void Unparse(int indent) {
        ReadNode current = this;
		System.out.print(linenum + ":");
        genIndent(indent);
        System.out.print("read ");
        while (current != ReadNode.NULL) {
            current.targetVar.Unparse(0);
            current = current.moreReads;
            if (current != ReadNode.NULL) {
                System.out.print(", ");
            }
        }
        System.out.println(";");
    } // Unparse

    void checkTypes() {
        targetVar.checkTypes();
        moreReads.checkTypes();
        typeMustBe(targetVar.type.val, (Types.Integer | Types.Character),
            error() + "Read parameters must be either int or char.");
        typeMustBe(targetVar.kind.val, ~(Kinds.Constant | Kinds.Array),
            error() + "Read parameters cannot be constants or arrays.");
    } // checkTypes

    void cg() {
        if (targetVar.type.val == Types.Integer) {
            gen("invokestatic"," CSXLib/readInt()I");
        } else if (targetVar.type.val == Types.Character) {
            gen("invokestatic"," CSXLib/readChar()C");
        } else if (targetVar.type.val == Types.Boolean) {
            gen("invokestatic"," CSXLib/readBool()Z");
        } else {
            mustBe(false);
        }

        if ( targetVar.idinfo.invocation != "" ) {
            gen("putstatic ", targetVar.idinfo.invocation);
        } else {
            gen("istore", targetVar.idinfo.varIndex);
        }
        moreReads.cg();
    } // cg

	private NameNode targetVar;
	private ReadNode moreReads;
} // class ReadNode 

class nullReadNode extends ReadNode {
	nullReadNode() {
        // do nothing
    }
	boolean   isNull() {return true;}
	void Unparse(int indent) {
        // do nothing
    }
	void checkTypes() {
        // do nothing
    }  
    void cg() {
        // do nothing
    }
} // class nullReadNode 

class DisplayNode extends StmtNode {
	DisplayNode() {
        // do nothing
    }
	DisplayNode(ExprNode val, DisplayNode pn, int line, int col) {
		super(line, col);
		outputValue = val;
		moreDisplays = pn;
	} // DisplayNode
    
    static nullDisplayNode NULL = new nullDisplayNode();

    void Unparse(int indent) {
        DisplayNode current = this;
		System.out.print(linenum + ":");
        genIndent(indent);
        System.out.print("print ");
        while (current != DisplayNode.NULL) {
            current.outputValue.Unparse(0);
            current = current.moreDisplays;
            if (current != DisplayNode.NULL) {
                System.out.print(", ");
            }
        }
        System.out.println(";");
    } // Unparse

	void checkTypes() { 
		outputValue.checkTypes();
        moreDisplays.checkTypes();
		typeMustBe(outputValue.type.val, 
                (Types.Integer | Types.Boolean | Types.Character | Types.String),
			error() + "Only Integers, Booleans, Characters, and Strings" 
            + " may be printed.");

        if (outputValue.kind.val == Kinds.Array) {
            // Can assume that outputValue is a NameNode
            if (((NameNode) outputValue).subscriptVal == ExprNode.NULL) {
                typeMustBe( Types.Integer, Types.Boolean,
                    error() + "Whole arrays cannot be printed.");
            }
        }

	} // checkTypes

	void cg() {
        outputValue.cg();
        if (outputValue.kind.val == Kinds.Array) {
            if (((NameNode) outputValue).subscriptVal == ExprNode.NULL) { 
                gen("ldc", 0);
                loadArray(outputValue.type);
            }           
                       
        }
        switch (outputValue.type.val) {
            case Types.Integer:
                gen("invokestatic"," CSXLib/printInt(I)V");
                break;
            case Types.Boolean:
                gen("invokestatic"," CSXLib/printBool(Z)V");
                break;
            case Types.Character:
                gen("invokestatic"," CSXLib/printChar(C)V");
                break;
            case Types.String:
                gen("invokestatic"," CSXLib/printString(Ljava/lang/String;)V");
                break;
            default:
                mustBe(false);
        }
        moreDisplays.cg();
	} // cg

	private ExprNode outputValue;
	private DisplayNode moreDisplays;
} // class DisplayNode 

class nullDisplayNode extends DisplayNode {
	nullDisplayNode() {
        // do nothing
    }
	boolean   isNull() {return true;}
	void Unparse(int indent) {
        // do nothing
    }
	void checkTypes() {
        // do nothing
    }  
    void cg() {
        // do nothing
    }
} // class nullDisplayNode 

class CallNode extends StmtNode {
	CallNode(IdentNode id, ArgsNode a, int line, int col) {
		super(line, col);
		funcName = id;
		funcArgs = a;
	} // CallNode

    void Unparse(int indent) {
		System.out.print(linenum + ":");
        genIndent(indent);
        funcName.Unparse(0);
        System.out.print("(");
        funcArgs.Unparse(0);
        System.out.print(")");
        System.out.println(";");
    } // Unparse

    void checkTypes() {
        funcName.checkTypes();
        funcArgs.checkTypes(); 
        typeMustBe(funcName.type.val, Types.Void,
                error() + "Only procedures can be called from statements."); 
        String signature = funcArgs.typeSignature();
        if (funcName.type.val != Types.Error && funcName.idinfo != null) { 
            if (!signature.equals(funcName.idinfo.typesig)) {
                System.out.println(error() + "Type signature of arguments does not"
                        + " match procedure definition." 
                        + "\n\tExpected: [" + funcName.idinfo.typesig + "]"
                        + "\n\tReceived: [" + signature + "]");
                typeErrors++;
            }
        }
    } // checkTypes

    void cg() {
        funcArgs.cg();
        gen("invokestatic", funcName.idinfo.invocation);
    } // cg

	private final IdentNode funcName;
	private final ArgsNode funcArgs;
} // class CallNode 

class ReturnNode extends StmtNode {
	ReturnNode(ExprNode e, int line, int col) {
		super(line, col);
		returnVal = e;
        type = new Types();
        kind = new Kinds();
	} // ReturnNode

    void Unparse(int indent) {
		System.out.print(linenum + ":");
        genIndent(indent);
        System.out.print("return ");
        if (returnVal != ExprNode.NULL) {
            returnVal.Unparse(0);
        }
        System.out.println(";");
    } // Unparse

    void checkTypes() {
        returnVal.checkTypes();
        if (returnVal != ExprNode.NULL) {
            type = returnVal.type;
// FIXME: places any type error on the line for function declaration. 
        } else {
            type = new Types(Types.Void);
        }
    } // checkTypes

    void cg() {
        if (returnVal != ExprNode.NULL) {
            returnVal.cg();
            gen("ireturn");
        } else {
            gen("return");
        }
    } // cg

    protected Types type;
    protected Kinds kind;
	private final ExprNode returnVal;
} // class ReturnNode 

class PrintNode extends StmtNode {
    /*
     *      Unsure of what to do with this class. It's mentioned in 
     *      Table 2 of Project 3's specification (DisplayNode is not
     *      mentioned in that table). It's not mentioned in Table 1 
     *      of the same spec (DisplayNode is). They seem like they're
     *      meant to be the same class, but I've left all my code
     *      in DisplayNode for now.
     * */
	PrintNode(int line, int col) {
		super(line, col);
	}
} // class PrintNode 

class BlockNode extends StmtNode {
	BlockNode(List<VarDeclNode> ds, List<StmtNode> ss, Boolean sem,
            int line, int col) {
		super(line, col);
		decls = ds;
		stmts = ss;
        semi = sem;
        exits = new ArrayList<ReturnNode>();
	} // BlockNode
    BlockNode() {decls = null; stmts = null; semi = false; 
        exits = new ArrayList<ReturnNode>();}

    void Unparse(int indent) {
        System.out.print(linenum + ":");
        genIndent(indent);
        System.out.println("{");
        for( int i = 0; i < decls.size(); i++ ) {
            decls.get(i).Unparse(indent+1);
        }
        for( int i = 0; i < stmts.size(); i++ ) {
            stmts.get(i).Unparse(indent+1);
        }
        System.out.print(linenum + ":");
        genIndent(indent);
        System.out.println("}");

        /*
         * TODO unparse semi colon if there is one
         */

    } // Unparse
   
    void checkTypes() {
        StmtNode stmt;
        st.openScope();
        for( int i = 0; i < decls.size(); i++ ) {
            decls.get(i).checkTypes();
        }
        for( int i = 0; i < stmts.size(); i++ ) {
            stmt = stmts.get(i);
            stmt.checkTypes();
            if (stmt instanceof ReturnNode) {
                exits.add((ReturnNode) stmt);
            }
        }
        try {
            st.closeScope();
        } catch (EmptySTException e) {
            // do nothing
        }
    } // checkTypes

    boolean checkExits(Types t) {
        List<ReturnNode> exitPoints = exits;
        ReturnNode exit;
        //check to see if exits are empty
        for (int i = 0; i < exitPoints.size(); i++) {
            exit = exitPoints.get(i); 
            if (exit.type.val != Types.Void 
                    && t.val == Types.Void) {
                System.out.println(error() + "Procedures can't return anything."); 
                typeErrors++;
                exit.type = new Types(Types.Error);
            } else {
                typesMustBeEqual(t.val, exit.type.val,
                    error() + "Return type doesn't match function type."); 
            }
        }
        if (exitPoints.isEmpty()) {
            if (t.val != Types.Void) {
                // THROW ERROR
                return false;
            }
            StmtNode stmt;
            if (!stmts.isEmpty()) {
                stmt = stmts.get(stmts.size() - 1);
            } else {
                stmt = this;
            }
            exit = new ReturnNode(ExprNode.NULL,stmt.linenum+1,1);
            stmts.add(exit);
            exits.add(exit);

//  For testing/viewing implied return statements 
/*
            System.out.println("Added implied return statement: " + stmts.get(stmts.size() -1)
                    + " linenum: " + exit.linenum + " colnum: " + exit.colnum);
*/

        }
        return true;
    } // checkExits

    void cg() {
        for( VarDeclNode var: decls ) {
            var.cg();
        }
        for( StmtNode stmt: stmts ) {
            stmt.cg();
        }
    } // cg

    static nullBlockNode NULL = new nullBlockNode();
    private final List<ReturnNode>      exits;
    public List<VarDeclNode>            decls;
	private final List<StmtNode>        stmts;  
    private final Boolean               semi;
// FIXME semi is never used. 
} // class BlockNode 

class nullBlockNode extends BlockNode {
	nullBlockNode() {
        // do nothing
    }
    boolean   isNull() {return true;}
	void Unparse(int indent) {
        // do nothing
    }
	void checkTypes() {
        // do nothing 
    }  
	void cg() {
        // do nothing 
    }  
} // class nullBlockNode 

class BreakNode extends StmtNode {
	BreakNode(IdentNode i, int line, int col) {
		super(line, col);
		label = i;
	} // BreakNode

    void Unparse(int indent) {
		System.out.print(linenum + ":");
        genIndent(indent);
        System.out.print("break ");
        label.Unparse(0);
        System.out.println(";");
    } // Unparse

    void checkTypes() {
        label.checkTypes();
		SymbolInfo id;
        id = (SymbolInfo) st.globalLookup(label.idname);
		if (id == null) {
			System.out.println(error() + label.idname 
                    + " is not an existing label.");
			typeErrors++;
		} else {
            if (id.kind.val != Kinds.Label) {
                typeMustBe(id.kind.val, (Kinds.Label), 
                            error() 
                            + "Break must reference label within it's scope. ");
                label.type = new Types(Types.Error);
            }
		} // id != null
    } // checkTypes

    void cg() {
        if (label != IdentNode.NULL) {
            gen("goto", label.idinfo.tailOfLoop);
        }
    } // cg

	private final IdentNode label;
} // class BreakNode 

class ContinueNode extends StmtNode {
	ContinueNode(IdentNode i, int line, int col) {
		super(line, col);
		label = i;
	} // ContinueNode

    void Unparse(int indent) {
		System.out.print(linenum + ":");
        genIndent(indent);
        System.out.print("continue ");
        label.Unparse(0);
        System.out.println(";");
    } // Unparse
    
    void checkTypes() {
        label.checkTypes();
		SymbolInfo id;
        id = (SymbolInfo) st.globalLookup(label.idname);
		if (id == null) {
			System.out.println(error() + label.idname 
                    + " is not an existing label.");
			typeErrors++;
		} else {
            if (id.kind.val != Kinds.Label) {
                typeMustBe(id.kind.val, (Kinds.Label), 
                            error() 
                            + "Continue must reference label within it's scope. ");
                label.type = new Types(Types.Error);
            }
		} // id != null
    } // checkTypes

    void cg() {
        if (label != IdentNode.NULL) {
            gen("goto", label.idinfo.headOfLoop);
        }
    } // cg

	private final IdentNode label;
} // class ContinueNode 

class ArgsNode extends ASTNode {
	ArgsNode() {
        // do nothing
    }
	ArgsNode(ExprNode e, ArgsNode a, int line, int col) {
		super(line, col);
		argVal = e;
		moreArgs = a;
	} // ArgsNode

    void Unparse(int indent) {
        argVal.Unparse(indent);
        if (!moreArgs.isNull()) {
            System.out.print(", ");
            moreArgs.Unparse(indent);
        }
    } // Unparse

    public String typeSignature() {
        String signature = "";
        ArgsNode current = moreArgs;
        if (argVal != null) {
            signature += toJasmin(argVal.type,argVal.kind);
        }
        while (current != null && !current.isNull()) {
            signature += toJasmin(current.argVal.type,current.argVal.kind);
            current = current.moreArgs;
        }
        return signature;
    } // typesignature

    void checkTypes() {
        argVal.checkTypes();
        moreArgs.checkTypes();
    } // checkTypes

    void cg() {
        argVal.cg();
        moreArgs.cg();
    } // cg

	static nullArgsNode NULL = new nullArgsNode();
	private ExprNode argVal;
	private ArgsNode moreArgs;
} // class ArgsNode 

class nullArgsNode extends ArgsNode {
	nullArgsNode() {
        // do nothing
    }
	boolean   isNull() {return true;}
	void Unparse(int indent) {
        // do nothing
    }
	void checkTypes() {
        // do nothing
    }
    void cg() {
        // do nothing
    }
} // class nullArgsNode 

class StrLitNode extends ExprNode {
	StrLitNode(String stringval, int line, int col) {
		super(line, col,new Types(Types.String),
                new Kinds(Kinds.Value));
		strval = stringval;
	}
    void Unparse(int indent) {
        System.out.print(strval);
    } // Unparse
	void checkTypes() { 
        // StrLitNodes are type-correct by definition
	} // checkTypes
	void cg() {
        gen("ldc",strval);
	} // cg
    public String value() {
        return strval;
    } // value
    public int length() {
        // Returns length without open/close quotes
        return strval.length() - 2; 
    } // length
	private final String strval;
} // class StrLitNode 

abstract class ExprNode extends ASTNode {
	ExprNode() {
        super();
        type = new Types();
        kind = new Kinds();
    }
	ExprNode(int l, int c) {
		super(l, c);
        type = new Types();
        kind = new Kinds();
	} // ExprNode
	ExprNode(int l,int c,Types t,Kinds k) {
		super(l,c);
		type = t;
		kind = k;
	} // ExprNode
	static nullExprNode NULL = new nullExprNode();
    protected Types type;
    protected Kinds kind;
} // class ExprNode

class nullExprNode extends ExprNode {
	nullExprNode() {super();}
	boolean   isNull() {return true;}
	void Unparse(int indent) {
        // do nothing
    }
	void checkTypes() {
        // do nothing
    }  
    void cg() {
        // do nothing
    }
// TODO: put a comment in empty blocks. -0
} // class nullExprNode 

class BinaryOpNode extends ExprNode {
	BinaryOpNode(ExprNode e1, int op, ExprNode e2, int line, int col, 
            Types resultType) {
		super(line, col, resultType, new Kinds(Kinds.Value));
		operatorCode = op;
		leftOperand = e1;
		rightOperand = e2;
	} // BinaryOpNode

	static void printOp(int op) {
		switch (op) {
			case sym.PLUS:
				System.out.print(" + ");
				break;
			case sym.MINUS:
				System.out.print(" - ");
				break;
			case sym.SLASH:
				System.out.print(" / ");
				break;
			case sym.TIMES:
				System.out.print(" * ");
				break;
			case sym.COR:
				System.out.print(" || ");
				break;
			case sym.CAND:
				System.out.print(" && ");
				break;
			case sym.LT:
				System.out.print(" < ");
				break;
			case sym.GT:
				System.out.print(" > ");
				break;
            case sym.LEQ:
				System.out.print(" <= ");
				break;
            case sym.GEQ:
				System.out.print(" >= ");
				break;
            case sym.EQ:
				System.out.print(" == ");
				break;
            case sym.NOTEQ:
				System.out.print(" != ");
				break;
			default:
				mustBe(false);
		} // switch (op)
	} // printOp

	static String toString(int op) {
		switch (op) {
			case sym.PLUS:
				return(" + ");
			case sym.MINUS:
				return(" - ");
			case sym.SLASH:
				return(" / ");
			case sym.TIMES:
				return(" * ");
			case sym.COR:
				return(" || ");
			case sym.CAND:
				return(" && ");
			case sym.LT:
				return(" < ");
			case sym.GT:
				return(" > ");
            case sym.LEQ:
				return(" <= ");
            case sym.GEQ:
				return(" >= ");
            case sym.EQ:
				return(" == ");
            case sym.NOTEQ:
				return(" != ");
			default:
				mustBe(false);
                return "";
		} // switch (op)
	} // toString 

	void Unparse(int indent) {
        System.out.print("(");
		leftOperand.Unparse(0);
		printOp(operatorCode);
		rightOperand.Unparse(0);
        System.out.print(")");
	} // checkTypes

	void checkTypes() {
        switch (operatorCode) {
			case sym.PLUS:
			case sym.MINUS:
			case sym.SLASH:
			case sym.TIMES:
                mustBe(operatorCode== sym.PLUS
                ||operatorCode==sym.MINUS
                ||operatorCode==sym.SLASH
                ||operatorCode==sym.TIMES);
                leftOperand.checkTypes();
                rightOperand.checkTypes();
                type = new Types(Types.Integer);
                typeMustBe(leftOperand.type.val, (Types.Integer | Types.Character),
                    error() + "Left operand of" + toString(operatorCode) 
                            + "must be an int or char.");
                typeMustBe(rightOperand.type.val, (Types.Integer | Types.Character),
                    error() + "Right operand of" + toString(operatorCode) 
                            + "must be an int or char.");
                break;
			case sym.COR:
			case sym.CAND:
                mustBe(operatorCode== sym.CAND
                ||operatorCode==sym.COR);
                leftOperand.checkTypes();
                rightOperand.checkTypes();
                type = new Types(Types.Boolean);
                typeMustBe(leftOperand.type.val, Types.Boolean,
                    error() + "Left operand of" + toString(operatorCode) 
                            + "must be a boolean.");
                typeMustBe(rightOperand.type.val, Types.Boolean,
                    error() + "Right operand of" + toString(operatorCode) 
                            + "must be boolean.");
                break;
            case sym.EQ:
            case sym.LT:
            case sym.GT:
            case sym.NOTEQ:
            case sym.GEQ:
            case sym.LEQ:
                // ( (int || char) && (int || char) ) || (bool && bool)
                mustBe(operatorCode== sym.EQ
                ||operatorCode== sym.LT
                ||operatorCode== sym.GT
                ||operatorCode== sym.NOTEQ
                ||operatorCode== sym.GEQ
                ||operatorCode== sym.LEQ);
                leftOperand.checkTypes();
                rightOperand.checkTypes();
                type = new Types(Types.Boolean);

                if (leftOperand.type.val == Types.Boolean 
                    ||rightOperand.type.val == Types.Boolean) 
                {
                    typesMustBeEqual(leftOperand.type.val, rightOperand.type.val,
                        error() + "Both operands of" + toString(operatorCode) 
                                + "must be boolean. Alternatively, each operand"
                                + " could be an int or char."); 
// FIXME: allows arrayVar == arrayVar. 
                } else {
                    typeMustBe(leftOperand.type.val, 
                            (Types.Integer | Types.Character),
                        error() + "Left operand of" + toString(operatorCode) 
                                + "must be an int or char, or both operands" 
                                + " must be boolean.");
                    typeMustBe(rightOperand.type.val, 
                            (Types.Integer | Types.Character),
                        error() + "Right operand of" + toString(operatorCode) 
                                + "must be an int or char, or both operands"
                                + " must be boolean.");
                } // leftOperand.type.val != Types.Boolean
                break;
			default:
				mustBe(false);
        }
	} // checkTypes

	void cg() {
        // First translate the left and right operands
        String pass, out;
        leftOperand.cg();
        rightOperand.cg();
        // Now the values of the operands are on the stack
        switch (operatorCode) {
            case sym.PLUS:
                gen("iadd");
                break;
            case sym.MINUS:
                gen("isub");
                break;
            case sym.TIMES:
                gen("imul");
                break;
            case sym.SLASH:
                gen("idiv");
                break;
			case sym.COR:
                gen("ior");
				break;
			case sym.CAND:
                gen("iand");
				break;
			case sym.LT:     // lOp < rOp --> rOp - lOp > 0
                pass = buildlabel(labelCnt++);
                out = buildlabel(labelCnt++);
                gen("if_icmplt", pass);
                gen("ldc", 0);
                gen("goto", out);
                genlab(pass);
                gen("ldc", 1);
                genlab(out);
				break;
			case sym.GT:     // lOp > rOp --> rOp - lOp < 0
                pass = buildlabel(labelCnt++);
                out = buildlabel(labelCnt++);
                gen("if_icmpgt", pass);
                gen("ldc", 0);
                gen("goto", out);
                genlab(pass);
                gen("ldc", 1);
                genlab(out);
				break;
            case sym.LEQ:    // lOp <= rOp --> rOp - lOp <= 0
                pass = buildlabel(labelCnt++);
                out = buildlabel(labelCnt++);
                gen("if_icmple", pass);
                gen("ldc", 0);
                gen("goto", out);
                genlab(pass);
                gen("ldc", 1);
                genlab(out);
				break;
            case sym.GEQ:    // lOp >= rOp --> rOp - lOp >= 0
                pass = buildlabel(labelCnt++);
                out = buildlabel(labelCnt++);
                gen("if_icmpge", pass);
                gen("ldc", 0);
                gen("goto", out);
                genlab(pass);
                gen("ldc", 1);
                genlab(out);
				break;
            case sym.EQ:     // lOp == rOp --> rOp - lOp == 0
                pass = buildlabel(labelCnt++);
                out = buildlabel(labelCnt++);
                gen("if_icmpeq", pass);
                gen("ldc", 0);
                gen("goto", out);
                genlab(pass);
                gen("ldc", 1);
                genlab(out);
				break;
            case sym.NOTEQ:  // lOp != rOp --> rOp - lOp != 0
                pass = buildlabel(labelCnt++);
                out = buildlabel(labelCnt++);
                gen("if_icmpne", pass);
                gen("ldc", 0);
                gen("goto", out);
                genlab(pass);
                gen("ldc", 1);
                genlab(out);
				break;
            default:
                throw new Error(); 
        } // switch (operatorCode)
	} // cg

	private final ExprNode leftOperand;
	private final ExprNode rightOperand;
	private final int operatorCode; // Token code of the operator
} // class BinaryOpNode 

class UnaryOpNode extends ExprNode {
	UnaryOpNode(int op, ExprNode e, int line, int col, Types resultType) {
		super(line, col, resultType, new Kinds(Kinds.Value));
		operand = e;
		operatorCode = op;
	} // UnaryOpNode

	static void printOp(int op) {
        if( op == sym.NOT ) {
            System.out.print("!");
            return;
        } else {
            throw new Error("toString: case not found");
        }
	} // printOp

	static String toString(int op) {
        if( op == sym.NOT ) {
            return(" ! ");
        } else {
            throw new Error("toString: case not found");
        }
	} // toString 

    void Unparse(int indent) {
		printOp(operatorCode);
		System.out.print("(");
		operand.Unparse(0);
		System.out.print(")");
    } // Unparse

    void checkTypes() {
        operand.checkTypes();
        mustBe(operatorCode==sym.NOT);
        type = new Types(Types.Boolean);
        typeMustBe(operand.type.val, Types.Boolean,
            error() + "Operand of" + toString(operatorCode) 
                    + "must be a boolean.");
    } // checkTypes

    void cg() {
        operand.cg();
        gen("ldc", 1);
        gen("ixor");
    } // cg

	private final ExprNode operand;
	private final int operatorCode; // Token code of the operator
} // class UnaryOpNode 

class CastNode extends ExprNode {
	CastNode(TypeNode t, ExprNode e, int line, int col) {
		super(line, col);
		operand = e;
		resultType = t;
	} // CastNode

    void Unparse(int indent) {
		resultType.Unparse(0);
		System.out.print("(");
		operand.Unparse(0);
		System.out.print(")");
    } // Unparse

    void checkTypes() {
        operand.checkTypes();
        resultType.checkTypes();
        typeMustBe(operand.type.val, 
                (Types.Boolean | Types.Integer | Types.Character),
            error() + "Only Integer, Boolean, or Character expressions"
            + " can be typecasted."); 
        typeMustBe(resultType.type.val, 
                (Types.Boolean | Types.Integer | Types.Character),
            error() + "Expressions can only be typecast to Integer,"
            + " Boolean, or Character types."); 
        type = resultType.type;
    } // checkTypes 

    void cg() {
        operand.cg();
        if (resultType.type.val == Types.Character) {
            gen("ldc", 256);
            gen("irem");
        } else if (resultType.type.val == Types.Boolean) {
            String hasValue = buildlabel(labelCnt++);
            String out = buildlabel(labelCnt++);
            gen("ifgt", hasValue);
            gen("ldc", 0);
            gen("goto", out);
            genlab(hasValue);
            gen("ldc", 1);
            genlab(out);
        }
    } // cg

	private final ExprNode operand;
	private final TypeNode resultType;
} // class CastNode 

class FuncCallNode extends ExprNode {
	FuncCallNode(IdentNode id, ArgsNode a, int line, int col) {
		super(line, col);
		methodName = id;
		methodArgs = a;
	} // FuncCallNode

    void Unparse(int indent) {
        methodName.Unparse(0);
        System.out.print("(");
        methodArgs.Unparse(0);
        System.out.print(")");
    } // Unparse

    void checkTypes() {
        methodName.checkTypes();
        methodArgs.checkTypes();
        type = methodName.type;
        kind = new Kinds(Kinds.Other);
        String signature = methodArgs.typeSignature();
		if (methodName.idinfo != null) {
            if (!signature.equals(methodName.idinfo.typesig)) {
                System.out.println(error() + 
                        "Type signature of arguments does not match function definition." 
                        + "\n\tExpected: [" + methodName.idinfo.typesig + "]"
                        + "\n\tReceived: [" + signature + "]");
                typeErrors++;
            }
        }
    } // checkTypes

    void cg() {
        methodArgs.cg();
        gen("invokestatic", methodName.idinfo.invocation);
    } // cg

	private final IdentNode methodName;
	private final ArgsNode methodArgs;
} // class FuncCallNode 

class IdentNode extends ExprNode {
	IdentNode(String identname, Types t, Kinds k, int line, int col) {
		super(line,col,t,k);
		idname = identname;
        nullFlag = false;
	} // IdentNode
	IdentNode(String identname, int line, int col) {
		super(line,col,new Types(Types.Unknown),new Kinds(Kinds.Other));
		idname = identname;
        nullFlag = false;
	} // IdentNode
	IdentNode(boolean flag) {
		super(0,0,new Types(Types.Unknown),new Kinds(Kinds.Other));
		idname = "";
        nullFlag = flag;
	} // IdentNode

    boolean isNull() {return nullFlag;}

    static IdentNode NULL = new IdentNode(true);

	void Unparse(int indent) {
		System.out.print(idname);
	} // Unparse

	void checkTypes() {
		SymbolInfo id;
		id = (SymbolInfo) st.localLookup(idname);
		if (id == null) {
            id = (SymbolInfo) st.globalLookup(idname);
        }
		if (id == null) {
			System.out.println(error() + idname + " is not declared.");
			typeErrors++;
			type = new Types(Types.Error);
            kind = new Kinds(Kinds.Other);
		} else {
			type = id.type; 
            kind = id.kind;
			idinfo = id; // Save ptr to correct symbol table entry
		} // id != null
	} // checkTypes

	public String idname;
    public SymbolInfo idinfo;
    private final boolean nullFlag;
} // class IdentNode 

class NameNode extends ExprNode {
	NameNode(IdentNode id, ExprNode expr, int line, int col) {
		super(line, col,new Types(Types.Unknown),new Kinds(Kinds.Other));
		varName = id;
		subscriptVal = expr;
	} // NameNode

	void Unparse(int indent) {
		varName.Unparse(0);
        if (subscriptVal != ExprNode.NULL) {
            System.out.print("[");
            subscriptVal.Unparse(0);
            System.out.print("]");
        }
	} // Unparse
	
    void checkTypes() {
		varName.checkTypes();
        subscriptVal.checkTypes();

		type = varName.type;
        kind = varName.kind;
        idinfo = varName.idinfo;

        if (subscriptVal != ExprNode.NULL) {
            typeMustBe(varName.kind.val, (Kinds.Array), 
                    error() + "Cannot apply subscript to non-array " 
                    + idinfo.name() + ".");
            typeMustBe(subscriptVal.type.val, 
                    (Types.Integer | Types.Character), 
                    error() + "Array indices can only be of type Integer"
                    + " or Character."); 
        }
	} // checkTypes

	void cg() {
        if (idinfo.invocation == "") {
            if (subscriptVal != ExprNode.NULL) {
                gen("aload",varName.idinfo.varIndex);
                subscriptVal.cg();  
                loadArray(idinfo.type);
            } else {
                if (idinfo.kind.val == Kinds.Array) {
                    gen("aload",varName.idinfo.varIndex);
                } else {
                    gen("iload",varName.idinfo.varIndex);
                }
            } 
        } else {
            gen("getstatic", idinfo.invocation);
            if (idinfo.kind.val == Kinds.Array) {
                if (subscriptVal != ExprNode.NULL) {
// FIXME: can combine these nested statements.
// probably other places where I'm doing something similar
                    subscriptVal.cg();
                    loadArray(idinfo.type);
                }
            }
        }
	} // cg

    public SymbolInfo idinfo;
	public IdentNode varName; 
	public ExprNode subscriptVal;
} // class NameNode 

class IntLitNode extends ExprNode {
	IntLitNode(int val, int line, int col) {
		super(line, col,new Types(Types.Integer),
                new Kinds(Kinds.Value));
		intval = val;
	} // IntLitNode
    void Unparse(int indent) {
        System.out.print(intval);
    } // Unparse
	void checkTypes() {
		// All CharLits are automatically type-correct
	} // checkTypes
	void cg() {
        // Load value of this literal onto stack
        gen("ldc",intval);
	} // cg
    public int value() {
        return intval;
    } // value
	private final int intval;
} // class IntLitNode 

class CharLitNode extends ExprNode {
	CharLitNode(char val, int line, int col) {
		super(line, col,new Types(Types.Character),
                new Kinds(Kinds.Value));
		charval = val;
	} // CharLitNode
    void Unparse(int indent) {
        System.out.print(charval);
    } // Unparse
	void checkTypes() {
		// All CharLits are automatically type-correct
	} // checkTypes
	void cg() {
        // Load value of this literal onto stack
        gen("ldc", charval);
	} // cg
    public char value() {
        return charval;
    } // value
	private final char charval;
} // class CharLitNode 

class TrueNode extends ExprNode {
	TrueNode(int line, int col) {
		super(line, col,new Types(Types.Boolean),
                new Kinds(Kinds.Value));
	} // TrueNode
    void Unparse(int indent) {
        genIndent(indent);
        System.out.print("true");
    } // Unparse
	void cg() {
        // Load value of this literal onto stack
        gen("iconst_1");
	} // cg
    public boolean value() {
        return true;
    } // value
} // class TrueNode 

class FalseNode extends ExprNode {
	FalseNode(int line, int col) {
		super(line, col,new Types(Types.Boolean),
                new Kinds(Kinds.Value));
	} // FalseNode
    void Unparse(int indent) {
        genIndent(indent);
        System.out.print("false");
    } // Unparse
	void cg() {
        // Load value of this literal onto stack
        gen("iconst_0");
	} // cg
    public boolean value() {
        return false;
    } // value
} // class FalseNode 
