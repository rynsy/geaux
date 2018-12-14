class Kinds{ //TODO fix formatting?
 public static final int Var         = 1;
 public static final int Value       = 2;
 public static final int Function    = 4;
 public static final int Constant    = 8;
 public static final int Array       = 16;
 public static final int ScalarParam = 32;
 public static final int ArrayParam  = 64;
 public static final int Label       = 128;
 public static final int Other       = 256;

 Kinds(int i){val = i;}
 Kinds(){val = Other;}

 public String toString() {
        switch(val){
          case 1: return "Var"; 
          case 2: return "Value";
          case 4: return "Function";
          case 8: return "Constant";
          case 16: return "Array";
          case 32: return "ScalarParam";
          case 64: return "ArrayParam";
          case 128: return "Label";
          case 256: return "Other";
          default: throw new RuntimeException();
        }
 }

 int val;
}
