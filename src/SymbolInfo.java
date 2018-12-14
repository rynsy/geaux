/**************************************************
*  class used to hold information associated w/
*  Symbs (which are stored in SymbolTables)
*
****************************************************/
class SymbolInfo extends Symb {
 public Kinds kind;
 public Types type;
 public int size;
 public String typesig;
 public String invocation;
 public int varIndex;
 public String headOfLoop;
 public String tailOfLoop;

 public SymbolInfo(String id, Kinds k, Types t, int s){
	super(id);
	kind = k; type = t; size = s; 
    typesig = ""; invocation = "";
    headOfLoop = ""; tailOfLoop = "";};
 public SymbolInfo(String id, Kinds k, Types t){
	super(id);
	kind = k; type = t; size = 1;
    typesig = ""; invocation = "";
    headOfLoop = ""; tailOfLoop = "";};
 public SymbolInfo(String id, int k, int t){
	super(id);
	kind = new Kinds(k); type = new Types(t); size = 1;typesig = ""; invocation = "";};
 public String toString(){
             return "(" + name() + ": kind=" + kind 
                 + ", type=" + type + ", size=" + size
                 + ", varIndex=" + varIndex 
                 + ", typesig=" + typesig
                 + ", invocation="+ invocation 
                 + ", headOfLoop="+ headOfLoop 
                 + ", tailOfLoop="+ tailOfLoop + ")";};
}

