// This class contains library routines used by Geaux program

public class GeauxLib{

	private GeauxLib() {
		// not to be instantiated
	}
	private static java.io.PushbackInputStream in =
		new java.io.PushbackInputStream(System.in);

	private static char getChar() {
		int theChar;
		try {
			theChar = in.read();
			if (theChar == -1) {
				throw new java.lang.RuntimeException();
			}
		} catch (java.io.IOException e) {
			throw new java.lang.RuntimeException();
		}
		return (char) theChar;
	} // getChar

	private static void ungetChar(char theChar) {
		try {
			in.unread(theChar);
		} catch (java.io.IOException e) {
			throw new java.lang.RuntimeException();
		}
	} // ungetChar

	public static int readInt() {
		char theChar = getChar();
		double val = 0;
		int sign = 1;
		do {
			while ( theChar != '-' && theChar != '~' &&
				!java.lang.Character.isDigit(theChar)) {
				theChar = getChar();
			}
			if ( theChar == '-' || theChar == '~') {
				sign = -1;
				theChar = getChar();
			}
		} while (!java.lang.Character.isDigit(theChar));

		while ( java.lang.Character.isDigit(theChar)) {
			val=10*val+(theChar-'0');
			theChar = getChar();
		}
		ungetChar(theChar);
		val = val*sign;
		if (val > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		} else if (val < Integer.MIN_VALUE) {
			return Integer.MIN_VALUE;
		} else {
			return (int) val;
		}
	} // readInt()

	public static char readChar() {
		return  getChar();
	} // readChar

	public static void printString(String s) {
		System.out.print(s);
		System.out.flush();
	} // printString

	public static void printChar(char theChar) {
		System.out.print(theChar);
		System.out.flush();
	} // printChar

	public static void printInt(int i) {
		System.out.print(i);
		System.out.flush();
	} // printInt

	public static void printBool(boolean b) {
		System.out.print(b);
		System.out.flush();
	} // printBool

	public static void printCharArray(char theChar[]) {
		for (int i=0;i<theChar.length;i++) {
			System.out.print(theChar[i]);
			System.out.flush();
		}
	} // printCharArray

	public static int[] cloneIntArray(int i[]) {
		return i.clone();
	} // cloneIntArray

	public static char[] cloneCharArray(char i[]) {
		return i.clone();
	} // cloneCharArray

	public static boolean[] cloneBoolArray(boolean i[]) {
		return i.clone();
	} // cloneBoolArray

	public static char[] convertString(String s) {
		return  s.toCharArray();
	} // convertString

	public static int[] checkIntArrayLength(int[] target, int source[]) {
		if (target.length != source.length) {
			throw new ArraySizeException();
		} else {
			return source;
		}
	} // checkIntArrayLength

	public static char[] checkCharArrayLength(char[] target, char source[]) {
		if (target.length != source.length) {
			throw new ArraySizeException();
		} else {
			return source;
		}
	} // checkCharArrayLength

	public static boolean[] checkBoolArrayLength(boolean[] target,
			boolean source[]) {
		if (target.length != source.length) {
			throw new ArraySizeException();
		} else {
			return source;
		}
	} // checkBoolArrayLength

	public static void main(String args[]) {
	// main
    }
} // class GeauxLib

class ArraySizeException extends RuntimeException {
	static final long serialVersionUID = 1L;
	// no other content
}
