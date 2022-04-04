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
	
	
	
	// �ļ��� ������, ���� ����
	public double evaluate(String expstr) throws ParserException{
		double result;
		exp = expstr;
		expIdx = 0;
		
		getToken();
		if(token.equals(EOE))
			handleErr(NOEXP);	// ǥ���� �������� ����
		
		// ǥ���� �Ľ��ϰ� ���� ���Ѵ�
		result = evalExp2();
		
		if(!token.equals(EOE)) 
			handleErr(SYNTAX);
			
		return result;
	}
	
	
	// ���ϱ�, ����
	private double evalExp2() throws ParserException{
		char op;	// ������
		double result;
		double partialResult;
		
		result = evalExp3();
		
		while((op = token.charAt(0)) == '+' || op == '-') {
			getToken();
			partialResult = evalExp3();
			switch(op) {
				case '-':
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
		
		result = evalExp4();
		
		while((op = token.charAt(0)) == '*' || op == '/' || op == '%') {
			getToken();
			partialResult = evalExp4();
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
		
		if(token.equals('^')) {
			getToken();
			partialResult = evalExp4();
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
		if((tokType == DELIMITER) && token.equals('+') || token.equals('-')) {
			op = token;
			getToken();
		}
		result = evalExp6();
		
		if(op.equals('-'))
			result = -result;
		
		return result;		
	}
	
	// ��ȣ�� ó��
	private double evalExp6() throws ParserException{
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
	
	
	// ���ڰ��� ���Ѵ�
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
		// �� �� ���ǵ��� ���� ���̸� ǥ������ ����� ������ ����
		else {
			token = EOE;
			return;
		}
	}
	
	
	// �������� ��� true �ƴϸ� false
	private boolean isDelim(char c) {
		if((" +-/*%^=()".indexOf(c) != -1))
			return true;
		return false;
	}
	
	
}
