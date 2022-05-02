package interpreter;

//A Small Basic ����������. 

import java.io.*;
import java.util.*;

//���������� ���� ���� Ŭ���� 
class InterpreterException extends Exception {
	String errStr; // ���� ����

	public InterpreterException(String str) {
		errStr = str;
	}

	public String toString() {
		return errStr;
	}
}

//The Small Basic ���������� 
class SBasic {
	final int PROG_SIZE = 10000; // �ִ� ũ�α׷� ũ��

// ��ū Ÿ��  
	final int NONE = 0;
	final int DELIMITER = 1;	// �����ڿ� ��ȣ
	final int VARIABLE = 2;	// ����
	final int NUMBER = 3;	// ����
	final int COMMAND = 4;	// BASIC Ű����
	final int QUOTEDSTR = 5;	// �ο��ȣ�� �ִ� ���ڿ�

// ���� Ÿ�� 
	final int SYNTAX = 0;
	final int UNBALPARENS = 1;
	final int NOEXP = 2;
	final int DIVBYZERO = 3;
	final int EQUALEXPECTED = 4;
	final int NOTVAR = 5;
	final int LABELTABLEFULL = 6;
	final int DUPLABEL = 7;
	final int UNDEFLABEL = 8;
	final int THENEXPECTED = 9;
	final int TOEXPECTED = 10;
	final int NEXTWITHOUTFOR = 11;
	final int RETURNWITHOUTGOSUB = 12;
	final int MISSINGQUOTE = 13;
	final int FILENOTFOUND = 14;
	final int FILEIOERROR = 15;
	final int INPUTIOERROR = 16;

// Small Basic�� Ű���忡 ���� ���� ǥ��
	final int UNKNCOM = 0;
	final int PRINT = 1;
	final int INPUT = 2;
	final int IF = 3;
	final int THEN = 4;
	final int FOR = 5;
	final int NEXT = 6;
	final int TO = 7;
	final int GOTO = 8;
	final int GOSUB = 9;
	final int RETURN = 10;
	final int END = 11;
	final int EOL = 12;

// ���α׷� �� ��ū
	final String EOP = "\0";

// <= �� ���� ���� ������ �ڵ�
	final char LE = 1;
	final char GE = 2;
	final char NE = 3;

// ���� �迭 
	private double vars[];

// Ű������� Ű��Ʈ ��ū�� �����Ű�� Ŭ����
	class Keyword {
		String keyword; // ���ڿ� ����
		int keywordTok; // ���� ǥ��

		Keyword(String str, int t) {
			keyword = str;
			keywordTok = t;
		}
	}

// ���� ǥ���� ������ Ű������ ���̺�
// ��� Ű����� �ҹ��ڷ� ����
	Keyword kwTable[] = { new Keyword("print", PRINT), // in this table.
			new Keyword("input", INPUT), new Keyword("if", IF), new Keyword("then", THEN), new Keyword("goto", GOTO),
			new Keyword("for", FOR), new Keyword("next", NEXT), new Keyword("to", TO), new Keyword("gosub", GOSUB),
			new Keyword("return", RETURN), new Keyword("end", END) };

	private char[] prog; // ���α׷� �迭�� ����
	private int progIdx; // ���α׷��� ��ġ�� ���� �ε���

	private String token; // ������ ��ū ����
	private int tokType; // ��ū�� Ÿ�� ����

	private int kwToken; // Ű������ ���� ǥ��

// FOR ������ ����
	class ForInfo {
		int var; // ī���� ����
		double target; // ��ǥ ��
		int loc; // ������ ���� �ҽ� �ڵ� �� �ε���
	}

// FOR ������ ���� ����
	private Stack fStack;

// ���̺� ���̺� �׸�鿡 ���� ���� 
	class Label {
		String name; // ���̺�
		int loc; // �ҽ� ���� ������ ���̺��� ��ġ�� ���� �ε���

		public Label(String n, int i) {
			name = n;
			loc = i;
		}
	}

// ���̺�鿡 ���� ����
	private TreeMap labelTable;

// gosub�� ���� ���� 
	private Stack gStack;

// ���� ������
	char rops[] = { GE, NE, LE, '<', '>', '=', 0 };

// ���� ���ϰ� Ȯ���ϱ� ���� ���� ������ �����ϴ� ���ڿ� ����
	String relops = new String(rops);

// SBasic ������
	public SBasic(String progName) throws InterpreterException {

		char tempbuf[] = new char[PROG_SIZE];
		int size;

		// �����ϱ� ���� ���α׷��� �޸𸮿� �о����
		size = loadProgram(tempbuf, progName);

		if (size != -1) {
			// ���α׷��� ������ ������ ũ���� �迭 ����
			prog = new char[size];

			// ���α׷��� ���α׷� �迭�� ����
			System.arraycopy(tempbuf, 0, prog, 0, size);
		}
	}

// ���α׷��� �޸𸮿� �о����
	private int loadProgram(char[] p, String fname) throws InterpreterException {
		int size = 0;

		try {
			FileReader fr = new FileReader(fname);

			BufferedReader br = new BufferedReader(fr);

			size = br.read(p, 0, PROG_SIZE);

			fr.close();
		} catch (FileNotFoundException exc) {
			handleErr(FILENOTFOUND);
		} catch (IOException exc) {
			handleErr(FILEIOERROR);
		}

		// ������ EOF ��ȣ�� �����°��, ũ�⸦ 1��ŭ ���ҽ�Ŵ
		if (p[size - 1] == (char) 26)
			size--;

		return size; // ũ�α׷� ũ�� ����
	}

// ���α׷� ����
	public void run() throws InterpreterException {

		// �� ���α׷� ������ ���� �ʱ�ȭ
		vars = new double[26];
		fStack = new Stack();
		labelTable = new TreeMap();
		gStack = new Stack();
		progIdx = 0;

		scanLabels(); // ���α׷� ������ ���̺��� �˻�

		sbInterp(); // ����

	}

// Small BASIC ������������ ������
	private void sbInterp() throws InterpreterException {

		// ������������ ���� ����
		do {
			getToken();
			// �Ҵ� ������ ���� �˻�
			if (tokType == VARIABLE) {
				assignment(); // �Ҵ� ������ ó��
			} else // Ű����
				switch (kwToken) {
				case PRINT:
					print();
					break;
				case GOTO:
					execGoto();
					break;
				case IF:
					execIf();
					break;
				case FOR:
					execFor();
					break;
				case NEXT:
					next();
					break;
				case INPUT:
					input();
					break;
				case GOSUB:
					gosub();
					break;
				case RETURN:
					greturn();
					break;
				case END:
					return;
				}
		} while (!token.equals(EOP));
	}

// ��� ���̺��� �˻�  
	private void scanLabels() throws InterpreterException {
		int i;
		Object result;

		// ������ ù��° ��ū�� ���̺����� �˻�
		getToken();
		if (tokType == NUMBER)
			labelTable.put(token, new Integer(progIdx));

		findEOL();

		do {
			getToken();
			if (tokType == NUMBER) {// �� ��ȣ
				result = labelTable.put(token, new Integer(progIdx));
				if (result != null)
					handleErr(DUPLABEL);
			}

			// ���� ���� �ƴϸ� ���� �� �˻�
			if (kwToken != EOL)
				findEOL();
		} while (!token.equals(EOP));
		progIdx = 0; // ���α׷��� ���������� �ε����� �缳��
	}

// ���� ���� �������� �˻� 
	private void findEOL() {
		while (progIdx < prog.length && prog[progIdx] != '\n')
			++progIdx;
		if (progIdx < prog.length)
			progIdx++;
	}

// ������ ���� �Ҵ�
	private void assignment() throws InterpreterException {
		int var;
		double value;
		char vname;

		// ���� �̸��� ����
		vname = token.charAt(0);

		if (!Character.isLetter(vname)) {
			handleErr(NOTVAR);
			return;
		}

		// ���� ���̺� ���� �ε����� ��ȯ
		var = (int) Character.toUpperCase(vname) - 'A';

		// ��ȣ�� ����
		getToken();
		if (!token.equals("=")) {
			handleErr(EQUALEXPECTED);
			return;
		}

		// �Ҵ��� ���� ����
		value = evaluate();

		// ���� �Ҵ�
		vars[var] = value;
	}

// PRINT ������ ������ ������ ����
	private void print() throws InterpreterException {
		double result;
		int len = 0, spaces;
		String lastDelim = "";

		do {
			getToken(); // ���� ����Ʈ �������� ����
			if (kwToken == EOL || token.equals(EOP))
				break;

			// "" ���ڿ�
			if (tokType == QUOTEDSTR) {
				System.out.print(token);
				len += token.length();
				getToken();
			} 
			// ����
			else {
				putBack();
				result = evaluate();
				getToken();
				System.out.print(result);

				// ������ �հ迡 ��� ���̸� ����
				Double t = new Double(result);
				len += t.toString().length(); // ���� ����
			}
			lastDelim = token;

			// ��ǥ�̸� ���� ������ �̵�
			if (lastDelim.equals(",")) {
				// ���� ������ �̵��ϱ� ���� ������ ���� ���
				spaces = 8 - (len % 8);
				len += spaces; // �� ��ġ�� �߰�
				while (spaces != 0) {
					System.out.print(" ");
					spaces--;
				}
			}
			// �����ݷ� ����
			else if (token.equals(";")) {
				System.out.print(" ");
				len++;
			}
			// ������ ������ ������ ���� ����
			else if (kwToken != EOL && !token.equals(EOP))
				handleErr(SYNTAX);
		} while (lastDelim.equals(";") || lastDelim.equals(","));

		// ��� ��
		if (kwToken == EOL || token.equals(EOP)) {
			if (!lastDelim.equals(";") && !lastDelim.equals(","))
				System.out.println();
		}
		else
			handleErr(SYNTAX);
	}

// GOTO ���� ����
	private void execGoto() throws InterpreterException {
		Integer loc;

		getToken(); // �̵��� ���̺��� ����

		// ���̺��� ��ġ �˻�
		loc = (Integer) labelTable.get(token);

		if (loc == null)
			handleErr(UNDEFLABEL); // ���ǵ��� ���� ���̺�
		else // loc���� ���α׷� ���� ����
			progIdx = loc.intValue();
	}

// IF ���� ����
	private void execIf() throws InterpreterException {
		double result;

		result = evaluate(); // ������ �� ����

		// ����� ���̸�(0�� �ƴҶ�), if�� Ÿ���� ����, ������;�� ���α׷��� ���� �ٷ� �̵�
		if (result != 0.0) {
			getToken();
			if (kwToken != THEN) {
				handleErr(THENEXPECTED);
				return;
			} // else, target statement will be executed
		} else
			findEOL(); // ���� ���� ���� ������ �˻�
	}

// FOR ���� ����
	private void execFor() throws InterpreterException {
		ForInfo stckvar = new ForInfo();
		double value;
		char vname;

		getToken(); // ���� ������ ����
		vname = token.charAt(0);
		if (!Character.isLetter(vname)) {
			handleErr(NOTVAR);
			return;
		}

		// ���� ������ �ε����� ����
		stckvar.var = Character.toUpperCase(vname) - 'A';

		getToken(); // ��ȣ�� ����
		if (token.charAt(0) != '=') {
			handleErr(EQUALEXPECTED);
			return;
		}

		value = evaluate(); // �ʱⰪ ����

		vars[stckvar.var] = value;

		getToken(); // TO�� ���� ���� ����
		if (kwToken != TO)
			handleErr(TOEXPECTED);

		stckvar.target = evaluate(); // Ÿ�� ���� ����

		// ������ �ּ��� �� �� ����� �� ������ ���ÿ� ������ ����
		if (value >= vars[stckvar.var]) {
			stckvar.loc = progIdx;
			fStack.push(stckvar);
		} else // �׷��� ������ ���� �ڵ带 ��������
			while (kwToken != NEXT)
				getToken();
	}

// NEXT ���� ����
	private void next() throws InterpreterException {
		ForInfo stckvar;

		try {
			// ������ FOR ������ ���� ������ �˻�
			stckvar = (ForInfo) fStack.pop();
			vars[stckvar.var]++; // ���� ������ ������Ŵ

			// ���� �����ٸ� ����
			if (vars[stckvar.var] > stckvar.target)
				return;

			// �׷��� ������ ������ ȸ����Ŵ
			fStack.push(stckvar);
			progIdx = stckvar.loc; // ����
		} catch (EmptyStackException exc) {
			handleErr(NEXTWITHOUTFOR);
		}
	}

// ������ ������ INPUT�� ����
	private void input() throws InterpreterException {
		int var;
		double val = 0.0;
		String str;

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		getToken();
		// ������Ʈ ���ڿ��� �����ϴ��� Ȯ��
		if (tokType == QUOTEDSTR) {
			// ���� �ִٸ� ������Ʈ�� ����� ���� ��ǥ�� �˻�
			System.out.print(token);
			getToken();
			if (!token.equals(","))
				handleErr(SYNTAX);
			getToken();
		} else
			System.out.print("? "); // �׷��� ������ '?'�� ������Ʈ�� ���

		// var���� ����
		var = Character.toUpperCase(token.charAt(0)) - 'A';

		try {
			str = br.readLine();
			val = Double.parseDouble(str); // ���� ����
		} catch (IOException exc) {
			handleErr(INPUTIOERROR);
		} catch (NumberFormatException exc) {
			// �� ������ �ٸ� ������ �ٸ��� ó���� ���� ����
			System.out.println("Invalid input.");
		}

		vars[var] = val; // ���� ����
	}

// GOSUB�� ����  
	private void gosub() throws InterpreterException {
		Integer loc;

		getToken();

		// ȣ���� ���̺��� ã��
		loc = (Integer) labelTable.get(token);

		if (loc == null)
			handleErr(UNDEFLABEL); // ���ǵ��� ���� ���̺�
		else {
			// ���ϵ� ��ġ�� ����
			gStack.push(new Integer(progIdx));

			// loc�� ����� ��ġ���� ���α׷� ������ ����
			progIdx = loc.intValue();
		}
	}

// GOSUB���� ���� 
	private void greturn() throws InterpreterException {
		Integer t;

		try {
			// ���α׷� �ε����� ����
			t = (Integer) gStack.pop();
			progIdx = t.intValue();
		} catch (EmptyStackException exc) {
			handleErr(RETURNWITHOUTGOSUB);
		}

	}

// **************** ���� �ļ� **************** 

// �ļ��� ������  
	private double evaluate() throws InterpreterException {
		double result = 0.0;

		getToken();
		if (token.equals(EOP))
			handleErr(NOEXP); // ������ �������� ����

		// ������ �м��ؼ� ���
		result = evalExp1();

		putBack();

		return result;
	}

// ���迬���� ó�� 
	private double evalExp1() throws InterpreterException {
		double l_temp, r_temp, result;
		char op;

		result = evalExp2();
		// ���α׷� ���̸� ����
		if (token.equals(EOP))
			return result;

		op = token.charAt(0);

		// ���迬������ ���
		if (isRelop(op)) {
			l_temp = result;
			getToken();
			r_temp = evalExp1();
			switch (op) { // ���迬���� ����
			case '<':
				if (l_temp < r_temp)
					result = 1.0;
				else
					result = 0.0;
				break;
			case LE:
				if (l_temp <= r_temp)
					result = 1.0;
				else
					result = 0.0;
				break;
			case '>':
				if (l_temp > r_temp)
					result = 1.0;
				else
					result = 0.0;
				break;
			case GE:
				if (l_temp >= r_temp)
					result = 1.0;
				else
					result = 0.0;
				break;
			case '=':
				if (l_temp == r_temp)
					result = 1.0;
				else
					result = 0.0;
				break;
			case NE:
				if (l_temp != r_temp)
					result = 1.0;
				else
					result = 0.0;
				break;
			}
		}
		return result;
	}

// �� �׸��� ���ϰų� �� 
	private double evalExp2() throws InterpreterException {
		char op;
		double result;
		double partialResult;

		result = evalExp3();

		while ((op = token.charAt(0)) == '+' || op == '-') {
			getToken();
			partialResult = evalExp3();
			switch (op) {
			case '-':
				result = result - partialResult;
				break;
			case '+':
				result = result + partialResult;
				break;
			}
		}
		return result;
	}

// �� ���ڸ� ���ϰų� ����   
	private double evalExp3() throws InterpreterException {
		char op;
		double result;
		double partialResult;

		result = evalExp4();

		while ((op = token.charAt(0)) == '*' || op == '/' || op == '%') {
			getToken();
			partialResult = evalExp4();
			switch (op) {
			case '*':
				result = result * partialResult;
				break;
			case '/':
				if (partialResult == 0.0)
					handleErr(DIVBYZERO);
				result = result / partialResult;
				break;
			case '%':
				if (partialResult == 0.0)
					handleErr(DIVBYZERO);
				result = result % partialResult;
				break;
			}
		}
		return result;
	}

// ������ ó����   
	private double evalExp4() throws InterpreterException {
		double result;
		double partialResult;
		double ex;
		int t;

		result = evalExp5();

		if (token.equals("^")) {
			getToken();
			partialResult = evalExp4();
			ex = result;
			if (partialResult == 0.0) {
				result = 1.0;
			} else
				for (t = (int) partialResult - 1; t > 0; t--)
					result = result * ex;
		}
		return result;
	}

// ������ ������ + �Ǵ� -�� ��� 
	private double evalExp5() throws InterpreterException {
		double result;
		String op;

		op = "";
		if ((tokType == DELIMITER) && token.equals("+") || token.equals("-")) {
			op = token;
			getToken();
		}
		result = evalExp6();

		if (op.equals("-"))
			result = -result;

		return result;
	}

// ��ȣ�� �ִ� ���� ó��  
	private double evalExp6() throws InterpreterException {
		double result;

		if (token.equals("(")) {
			getToken();
			result = evalExp2();
			if (!token.equals(")"))
				handleErr(UNBALPARENS);
			getToken();
		} else
			result = atom();

		return result;
	}

// ���ڳ� ������ ���� ����
	private double atom() throws InterpreterException {
		double result = 0.0;

		switch (tokType) {
		case NUMBER:
			try {
				result = Double.parseDouble(token);
			} catch (NumberFormatException exc) {
				handleErr(SYNTAX);
			}
			getToken();
			break;
		case VARIABLE:
			result = findVar(token);
			getToken();
			break;
		default:
			handleErr(SYNTAX);
			break;
		}
		return result;
	}

// ������ ���� ����
	private double findVar(String vname) throws InterpreterException {
		if (!Character.isLetter(vname.charAt(0))) {
			handleErr(SYNTAX);
			return 0.0;
		}
		return vars[Character.toUpperCase(vname.charAt(0)) - 'A'];
	}

// �Է� ��Ʈ������ ��ū�� ����  
	private void putBack() {
		if (token == EOP)
			return;
		for (int i = 0; i < token.length(); i++)
			progIdx--;
	}

// ���� ó��   
	private void handleErr(int error) throws InterpreterException {
		String[] err = { "Syntax Error", "Unbalanced Parentheses", "No Expression Present", "Division by Zero",
				"Equal sign expected", "Not a variable", "Label table full", "Duplicate label", "Undefined label",
				"THEN expected", "TO expected", "NEXT without FOR", "RETURN without GOSUB", "Closing quotes needed",
				"File not found", "I/O error while loading file", "I/O error on INPUT statement" };

		throw new InterpreterException(err[error]);
	}

// ���� ��ū�� ����   
	private void getToken() throws InterpreterException {
		char ch;

		tokType = NONE;
		token = "";
		kwToken = UNKNCOM;

		// ���α׷� ������ �˻�
		if (progIdx == prog.length) {
			token = EOP;
			return;
		}

		// ���� ���ڴ� �ǳʶ�
		while (progIdx < prog.length && isSpaceOrTab(prog[progIdx]))
			progIdx++;

		// ���α׷� ��
		if (progIdx == prog.length) {
			token = EOP;
			tokType = DELIMITER;
			return;
		}

		// ���� �˻�
		if (prog[progIdx] == '\r') { // crlf�� ó��
			progIdx += 2;
			kwToken = EOL;
			token = "\r\n";
			return;
		}

		// ���� �����ڿ� ���� �˻�
		ch = prog[progIdx];
		if (ch == '<' || ch == '>') {
			if (progIdx + 1 == prog.length) // ���� �������� �ǿ����� ������ ����
				handleErr(SYNTAX);

			switch (ch) {
			case '<':
				if (prog[progIdx + 1] == '>') {
					progIdx += 2;
					token = String.valueOf(NE);
				} else if (prog[progIdx + 1] == '=') {
					progIdx += 2;
					token = String.valueOf(LE);
				} else {
					progIdx++;
					token = "<";
				}
				break;
			case '>':
				if (prog[progIdx + 1] == '=') {
					progIdx += 2;
					token = String.valueOf(GE);
				} else {
					progIdx++;
					token = ">";
				}
				break;
			}
			tokType = DELIMITER;
			return;
		}

		// �������� ���
		if (isDelim(prog[progIdx])) {
			token += prog[progIdx];
			progIdx++;
			tokType = DELIMITER;
		}
		// ���� �Ǵ� Ű������ ���
		else if (Character.isLetter(prog[progIdx])) {
			while (!isDelim(prog[progIdx])) {
				token += prog[progIdx];
				progIdx++;
				if (progIdx >= prog.length)
					break;
			}

			kwToken = lookUp(token);
			if (kwToken == UNKNCOM)
				tokType = VARIABLE;
			else
				tokType = COMMAND;
		}
		// ������ ���
		else if (Character.isDigit(prog[progIdx])) {
			while (!isDelim(prog[progIdx])) {
				token += prog[progIdx];
				progIdx++;
				if (progIdx >= prog.length)
					break;
			}
			tokType = NUMBER;
		}
		// �ο��ȣ�� �ִ� ���ڿ��� ���
		else if (prog[progIdx] == '"') {
			progIdx++;
			ch = prog[progIdx];
			while (ch != '"' && ch != '\r') {
				token += ch;
				progIdx++;
				ch = prog[progIdx];
			}
			// " " ¦�� �ȸ���
			if (ch == '\r')
				handleErr(MISSINGQUOTE);
			progIdx++;
			tokType = QUOTEDSTR;
		}
		// ���ǵ��� ���� ������ ��� ���α׷� ����
		else { 
			token = EOP;
			return;
		}
	}

// c�� �������� �̸� true ����   
	private boolean isDelim(char c) {
		if ((" \r,;<>+-/*%^=()".indexOf(c) != -1))
			return true;
		return false;
	}

// c�� �����̰ų� ���̸� true ����
	boolean isSpaceOrTab(char c) {
		if (c == ' ' || c == '\t')
			return true;
		return false;
	}

// c�� ���迬�����̸� true ���� 
	boolean isRelop(char c) {
		if (relops.indexOf(c) != -1)
			return true;
		return false;
	}

	// ��ū ���̺��� ��ū�� ���� ǥ���� �˻�
	private int lookUp(String s) {
		int i;

		// �ҹ��ڷ� ��ȯ
		s = s.toLowerCase();

		// ���̺� ��ū�� �ִ��� �˻�
		for (i = 0; i < kwTable.length; i++)
			if (kwTable[i].keyword.equals(s))
				return kwTable[i].keywordTok;
		return UNKNCOM; // �˷����� ���� Ű����
	}
}
