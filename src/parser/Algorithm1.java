package parser;

import java.util.ArrayList;

/*
 �˰���1
 a = ù��° �ǿ�����
 while (�ǿ����ڰ� ����_
 	op = ������
 	b = �ι�° �ǿ�����
 	a = a op b
 */
public class Algorithm1 {
	
	String str;
	ArrayList<String> list;
	
	
	// ������, ���� ��±��� ����
	public Algorithm1(String str) {
		this.str = str;
		list = new ArrayList<String>();
		
		int idx1 = 0;
		int idx2 = 0;
		
		int a, b;	// �ǿ�����
		int result;
		String operator;	// ������
		
		// ǥ�� ������ ���������� �и�
		while(idx2 < str.length()) {
			if(str.charAt(idx2) == ' ') {	// �������� ������ ����
				list.add(str.substring(idx1, idx2));
				idx1 = idx2 + 1;
			}
			idx2++;
		}
		list.add(str.substring(idx1, idx2));
		
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
		
		// �ǿ����ڰ� �ϳ��� ������ ���
		System.out.println("�����: " + list.get(0));		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String str = "10 - 2 * 3";
		
		System.out.println("�Է� ����: " + str);
		
		Algorithm1 obj = new Algorithm1(str);
	}

}
