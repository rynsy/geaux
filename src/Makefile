DESTPATH = ./classes
COURSEDIR = C:/Users/rmli223.AD/bin/java/geaux
CUPPATH = ../lib
CLASSPATH = $(DESTPATH):$(CUPPATH)/java-cup-10k.jar
JASMINPATH = $(CUPPATH)/cup.jar:$(CUPPATH)/jasmin-sable.jar
VPATH = $(DESTPATH)
JCFLAGS = -Xlint:all,-auxiliaryclass,-rawtypes -deprecation -classpath $(CLASSPATH) -d $(DESTPATH)
JFLAGS = -classpath $(CLASSPATH)
TESTDIR = tests
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
	jflex geaux.jlex
parser.class: ASTNode.class Scanner.class SyntaxErrorException.class
Yylex.class: sym.class 
SymbolTable.class: Symb.class EmptySTException.class DuplicateException.class
Scanner.class: Yylex.class Types.class
SymbolInfo.class: Kinds.class
ASTNode.class: ast.java sym.class SymbolTable.class SymbolInfo.class
	javac $(JCFLAGS) ast.java
P5.class: Yylex.class Scanner.class parser.class ASTNode.class

jazz:
	java -classpath $(JASMINPATH) jasmin.Main $(JASMINTEST)
	java -classpath .;./classes $(JASMINTESTNAME)
	@echo

editjazz:
	vim $(JASMINTEST)
	@echo

test: all GeauxLib.class
	java $(JFLAGS) Geaux $(TESTDIR)/test-$(TESTNUM).geaux
	java -classpath $(JASMINPATH) jasmin.Main test.j
	java -classpath .:./classes p$(TESTNUM)csx 
	@echo

testall: all GeauxLib.class
	./testall.sh < GeauxTests.input > GeauxTests.output 2>&1
	@echo
	
###
# style check
###

HOW = basic,braces,clone,design,finalizers,migrating,unusedcode,imports,optimizations,strictexception,strings,sunsecure,typeresolution

style: 
	echo "Need to configure the pmd style checker and invoke it here"

###
# lint check
###

lint: all
	echo "Need to configure jlint"
	#jlint +all classes

###
# clean
###

clean: 
	rm -f $(DESTPATH)/*.class Yylex.java* sym.java parser.java test.j *.class
