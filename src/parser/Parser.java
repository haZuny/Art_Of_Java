package parser;

import java.time.temporal.ValueRange;


// �ļ� ���� ó���� ���� Exception Ŭ����
class ParserException extends Exception{
	String errStr;	// ���� ���� ���ڿ�
	
	public ParserException(String str) {
		errStr = str;
	}
	
	public String toString() {
		return errStr;
	}
}



public class Parser {

	// TOKEN Ÿ�� (����) �����
	final int NONE = 0;
	final int DELIMITER = 1;	// �����ڿ� ��ȣ
	final int VARIABLE = 2;
	final int NUMBER = 3;
	final int UNKNOWN = 4;	// ������
	
	// ���� ������ ���� �����
	final int SYNTAX = 0;	// ���ƿ� ���� �ʴ� ǥ��
	final int UNBALPARENS = 1;	// ��ȣ ��ȣ�� ���� ����
	final int NOEXP = 2;	// � ǥ���� �������� ����
	final int DIVBYZERO = 3;	// � ���� 0���� ����
	
	// ǥ������ ���� ��Ÿ���� ���
	final String EOE = "\0";
	
	private String exp;	// ǥ���� ��� �ִ� ���ڿ�
	private int expIdx; 	// ǥ���� ���� �ε���
	private String token;	// ���� �ε��� �� ��ū
	private int tokType;	// ���� �ε��� �� ��ū�� Ÿ��
	
	// �������� ���� �迭
	private double vars[] = new double[26];
	
	// ������ �����
	private int expPos;	// �Ǻ����� ���� ��ġ / 0: ����, 1: ����
	private int unknownPos;	// �������� �ִ� ��ġ
	private double coefficient;	// ���
	
	
	
	// �ļ��� ������, ���� ����
	public double evaluate(String expstr) throws ParserException{
		double result;
		exp = expstr;
		expIdx = 0;
		
		expPos = 0;
		unknownPos = 0;
		coefficient = 1;
		
		getToken();
		if(token.equals(EOE))
			handleErr(NOEXP);	// ǥ���� �������� ����
		
		// ǥ���� �Ľ��ϰ� ���� ���Ѵ�
		result = evalExp1();
		
		if(!token.equals(EOE)) 
			handleErr(SYNTAX);
		
		return result;
	}
	
	
	// �Ҵ�, ������ ó��
	private double evalExp1() throws ParserException{
		double result;
		int varIdx;
		int ttokType;
		String temptoken;
		
		if(tokType == VARIABLE) {
			// ���� ��ū ����, ���� ���� ��� ���ؼ�
			temptoken = new String(token);
			ttokType = tokType;
			
			// ������ �ε��� ���
			varIdx = Character.toUpperCase(token.charAt(0)) - 'A';
			
			getToken();
			// ������ �ƴ� ���
			if(!token.equals("=")) {
				putBack();	// �ε��� �ǵ���
				// ���� ��ū�� ����. �Ҵ�X
				token = new String(temptoken);
				tokType = ttokType;
			}
			// ���� �����ϰ� ����� �迭�� ����
			else {
				getToken();	// ���� �κ��� ���ڿ��� ������
				result = evalExp1_2();
				vars[varIdx] = result;
				return result;
			}
		}
		return evalExp1_2();
	}
	
	// ������ ���� ó��
	private double evalExp1_2() throws ParserException{
		double result;
		double patialResult;
		double unknownResult;
		
		result = evalExp2();	// ���������� ���� ���
		
		// ������
		if(token.equals("=")) {
			getToken();
			// ���� ���
			expPos = 1;
			patialResult = evalExp2();

			if(unknownPos == 0)
				unknownResult = (patialResult - result)/coefficient;
			else
				unknownResult = (result - patialResult)/coefficient;
			
			
			return unknownResult;
		}
		
		return result;
	}
	
	
	// ���ϱ�, ����
	private double evalExp2() throws ParserException{
		char op;	// ������
		double result;
		double partialResult;
		
		result = evalExp3();
		if (tokType == UNKNOWN) {
			getToken();
		}
		
		while((op = token.charAt(0)) == '+' || op == '-') {
			getToken();
			partialResult = evalExp3();
			if (tokType == UNKNOWN) {
				getToken();
			}
			switch(op) {
				case '-':
					coefficient = -coefficient;
					result -= partialResult;
					break;
				case '+':
					result += partialResult;
					break;
			}
		}
		return result;
	}
	
	
	// ���ϱ� ������
	private double evalExp3() throws ParserException{
		char op;
		double result;
		double partialResult;
		boolean checkCoefficient = false;
		
		result = evalExp4();
		// �������ϰ�� ��� ����
		if(tokType == UNKNOWN) {
			checkCoefficient = true;
			getToken();
		}
		
		while((op = token.charAt(0)) == '*' || op == '/' || op == '%') {
			getToken();
			partialResult = evalExp4();
			// �������� ��� ��� ����
			if (checkCoefficient) {
				coefficient = partialResult;
				checkCoefficient = false;
				}
			if (tokType == UNKNOWN) {
				coefficient = result;
				getToken();
			}
			
			// �������� ��� �н�
			switch (op) {
			case '*':
				result *= partialResult;
				break;
			case '/':
				if (partialResult == 0.0)	// 0���� ������ ����
					handleErr(DIVBYZERO);
				result /= partialResult;
				break;
			case '%':
				if (partialResult == 0.0)	// 0���� ������ ����
					handleErr(DIVBYZERO);
				result %= partialResult;
				break;
			}
		}
		return result;
	}
	
	
	// ����
	private double evalExp4() throws ParserException{
		double result;
		double partialResult;
		double ex;
		int t;
		
		result = evalExp5();
		
		if(token.equals("^")) {
			getToken();
			partialResult = evalExp4();	// ���ӵ� ���� ���� ó�� ���� 4�� ��ݺ�
			ex = result;
			if(partialResult == 0.0) 
				result = 1.0;
			else {
				for(t = (int)partialResult - 1; t > 0; t--)
					result = result * ex;
			}
		}
		return result;
	}
	
	
	// ������ +, -�� ó��
	private double evalExp5() throws ParserException{
		double result;
		String op;
		
		op = "";
		if((tokType == DELIMITER) && token.equals("+") || token.equals("-")) {
			op = token;
			getToken();
		}
		result = evalExp6();
		
		if(op.equals("-"))
			result = -result;
		
		return result;		
	}
	
	// ���ȣ []
	private double evalExp6() throws ParserException{
		double result;
		
		if(token.contentEquals("[")) {
			getToken();
			result = evalExp2();
			if(!token.equals("]"))
				handleErr(UNBALPARENS);
			getToken();
		}
		
		else
			result = evalExp7();
		
		return result;
	}
	
	// �߰�ȣ {}
	private double evalExp7() throws ParserException{
		double result;
		
		if(token.contentEquals("{")) {
			getToken();
			result = evalExp2();
			if(!token.equals("}"))
				handleErr(UNBALPARENS);
			getToken();
		}
		else
			result = evalExp8();
		
		
		return result;
	}
	
	// �Ұ�ȣ()
	private double evalExp8() throws ParserException{
		double result;
		
		if(token.contentEquals("(")) {
			getToken();
			result = evalExp2();
			if(!token.equals(")"))
				handleErr(UNBALPARENS);
			getToken();
		}
		else
			result = atom();
		
		return result;
	}
	
	
	
	
	// ���ڳ� ������ ���� �����´�
	private double atom() throws ParserException{
		double result = 0.0;
		
		switch(tokType) {
			case NUMBER:
				try {
					result = Double.parseDouble(token);
				}catch(NumberFormatException exc) {
					handleErr(SYNTAX);
				}
				getToken();
				break;
				
			case VARIABLE:
				result = findVar(token);
				getToken();
				break;
				
			case UNKNOWN:
				result = 0;
				unknownPos = expPos;
				break;
				
				
			default:
				handleErr(SYNTAX);
				break;
				
		}
		return result;
	}
	
	
	// ���� ó��
	private void handleErr(int error) throws ParserException{
		String[] err = {
				"Syntax Error\n",
				"Unbalanced Parentheses\n",
				"No Expression Present\n",
				"Division by Zero\n"
		};
		throw new ParserException(err[error]);
	}
	
	
	
	// ��ū�� ��� �޼ҵ�
	private void getToken() {
		
		// �ʱ�ȭ
		tokType = NONE;
		token = "";
		
		// ǥ���� ���� Ȯ��
		if(expIdx == exp.length()) {
			token = EOE;
			return;
		}
		
		// �����̸� ���� ǥ������ �Ѿ��.
		while (expIdx < exp.length() && Character.isWhitespace(exp.charAt(expIdx))) 
			++expIdx;
		
		// ǥ���� �������̶�� �޼ҵ� ����
		if (expIdx == exp.length()) {
			token = EOE;
			return;
		}
		
		
		// ���������� �Ǻ�
		if(isDelim(exp.charAt(expIdx))) {
			token += exp.charAt(expIdx);
			expIdx++;
			tokType = DELIMITER;
		}
		// ���������� �Ǻ�
		else if(Character.isLetter(exp.charAt(expIdx))) {
			while(!isDelim(exp.charAt(expIdx))) {	//�����ڰ� ���ö����� ����
				token += exp.charAt(expIdx);
				expIdx++;
				if(expIdx >= exp.length())	// ǥ������ �Ѿ�� ����
					break;
			}
			tokType = VARIABLE;
		}
		// ���������� �Ǻ�
		else if(Character.isDigit(exp.charAt(expIdx))) {
			while(!isDelim(exp.charAt(expIdx))) {	// �����ڰ� ���ö����� ����
				token += exp.charAt(expIdx);
				expIdx++;
				if(expIdx >= exp.length())	// ǥ���� �Ѿ�� ����
					break;
			}
			tokType = NUMBER;
		}
		// ���������� �Ǻ�
		else if(isUnknown(exp.charAt(expIdx))) {
			token += exp.charAt(expIdx);
			expIdx++;
			tokType = UNKNOWN;
		}
		// �� �� ���ǵ��� ���� ���̸� ǥ������ ����� ������ ����
		else {
			token = EOE;
			return;
		}
	}
	
	
	// �������� ��� true �ƴϸ� false
	private boolean isDelim(char c) {
		if((" +-/*%^=(){}[]".indexOf(c) != -1))
			return true;
		return false;
	}
	
	// �������� ��� true
	private boolean isUnknown(char c) {
		if("#".indexOf(c) != -1) {
			return true;
			}
		return false;
	}
	
	
	// ������ ���� �����Ѵ�.
	private double findVar(String vname) throws ParserException{
		if(!Character.isLetter(vname.charAt(0))) {
			handleErr(SYNTAX);
			return 0.0;
		}
		return vars[Character.toUpperCase(vname.charAt(0)) - 'A'];
	}
	
	
	// �Է� ��Ʈ���� ����ŭ �ε��� ���� �ǵ�����.
	private void putBack() {
		if (token == EOE)
			return;
		for (int i = 0; i < token.length(); i++)
			expIdx--;
	}
	
	
}
