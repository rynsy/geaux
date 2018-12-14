class Types{
 public static final int Character  = 1;
 public static final int Integer    = 2;
 public static final int Boolean    = 4;
 public static final int Void       = 8;
 public static final int String     = 16;
 public static final int Error      = 32;
 public static final int Unknown    = 64;

 Types(int i){val = i;}
 Types(){val = Unknown;}

 public String toString() {
	switch(val){
	  case 1: return "Character";
	  case 2: return "Integer";
	  case 4: return "Boolean";
	  case 8: return "Void";
      case 16: return "String";
	  case 32: return "Error";
	  case 64: return "Unknown";
	  default: throw new RuntimeException();
	}
 }

 int val;
}

