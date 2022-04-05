package parser;

import java.util.ArrayList;

public class Algorithm1 {
	
	String str;
	ArrayList<String> list;
	
	
	// ������, ���� ��±��� ����
	 int operate(String str) {
		this.str = str;
		list = new ArrayList<String>();
		
		int a, b;	// �ǿ�����
		int result;
		String operator;	// ������
		
		// ǥ�� ������ ���������� �и�
		getToken(str);
		
		// �ǿ����ڰ� �������� ���� �� ���� �ݺ�
		while (list.size() > 1) {
			
			// ����Ʈ ���� ���
			System.out.println(list);
			
			a = Integer.parseInt(list.get(0));	// �ǿ����ڸ� ������ �ٲ㼭 ��ȯ
			b = Integer.parseInt(list.get(2));
			
			operator = list.get(1);
			
			// ����� �ǿ����� ����
			list.remove(0);
			list.remove(0);
			list.remove(0);
			
			// switch �������� �����ڿ� ���� ���� ����
			switch (operator) {
			case "+":
				a = a + b;
				break;
				
			case "-":
				a = a - b;
				break;
				
			case "/":
				a = a / b;
				break;
				
			case "*":
				a = a * b;
				break;
				
			case "^":
				a = a ^ b;
				break;
				
			case "%":
				a = a % b;
				break;

			default:
				break;
			}
			
			// ����Ʈ �� �տ� �ǿ����� �߰�
			list.add(0, "" + a);					
		}
		
		// �ǿ����ڰ� �ϳ��� ������ ����
		return(Integer.parseInt(list.get(0)));	

	}
	
	
	// ��ū ����
	public void getToken(String exp) {
		
		// ������ �ǿ����� ���� ����
		String buf = "";
		int idx = 0;
		
		while(idx < exp.length()) {
			// �ǿ������� ���
			if (("1234567890".indexOf(exp.charAt(idx))) != -1){
				buf += exp.charAt(idx);
			}
			// �������� ���
			else if (("+-/*%^=()".indexOf(exp.charAt(idx))) != -1){
				list.add(buf);
				buf = "";
				list.add("" + exp.charAt(idx));
			}
			// ���� ����
			else if(exp.charAt(idx) == ' '){
				
			}
			idx++;
		}
		list.add(buf);
	}

	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String str = "10 - 2 * 3";
		int result;
		
		System.out.println("�Է� ����: " + str);
		
		Algorithm1 obj = new Algorithm1();
		result = obj.operate(str);
		
		System.out.println("�����: " + result);
	}

}
