DESTPATH = ../out/classes
PROJDIR = /d/Home/Code/java/geaux
LIBPATH = $(PROJDIR)/lib
JFLEXPATH = $(LIBPATH)/jflex-full-1.7.0.jar
JASMINPATH = $(LIBPATH)/jasmin-sable.jar
CUPPATH = $(LIBPATH)/java-cup-10k.jar
CLASSPATH = $(DESTPATH):$(CUPPATH)
JASMINCLASSPATH = $(CUPPATH):$(JASMINPATH)
JCFLAGS = -Xlint:all,-auxiliaryclass,-rawtypes -deprecation -classpath $(CLASSPATH) -d $(DESTPATH)
JFLAGS = -classpath $(CLASSPATH)
TESTDIR = ../tests
TESTNUM = 00
JASMINTESTNAME = debug
JASMINTEST = ../jasmin_tests/$(JASMINTESTNAME).j

%.class: %.java
	javac $(JCFLAGS) $<

.PHONY: all 
all: Geaux.class Yylex.class parser.class 

sym.java parser.java: geaux.cup 
	java $(JFLAGS) java_cup/Main < geaux.cup
Yylex.java: geaux.jlex
	java -jar $(JFLEXPATH) geaux.jlex
parser.class: ASTNode.class Scanner.class SyntaxErrorException.class
Yylex.class: sym.class 
SymbolTable.class: Symb.class EmptySTException.class DuplicateException.class
Scanner.class: Yylex.class Types.class
SymbolInfo.class: Kinds.class
ASTNode.class: ast.java sym.class SymbolTable.class SymbolInfo.class
	javac $(JCFLAGS) ast.java
Geaux.class: Yylex.class Scanner.class parser.class ASTNode.class

jazz:
	java -classpath $(JASMINCLASSPATH) jasmin.Main $(JASMINTEST)
	java -classpath .;./classes $(JASMINTESTNAME)
	@echo

editjazz:
	vim $(JASMINTEST)
	@echo

test: all GeauxLib.class
	java $(JFLAGS) Geaux $(TESTDIR)/test-$(TESTNUM).geaux
	java -classpath $(JASMINCLASSPATH) jasmin.Main test.j
	java -classpath .:./classes p$(TESTNUM)go 
	@echo

testall: all GeauxLib.class
	./testall.sh < GeauxTests.input > GeauxTests.output 2>&1
	@echo
	
clean: 
	rm -f $(DESTPATH)/*.class Yylex.java* sym.java parser.java test.j *.class
