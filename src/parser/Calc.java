package parser;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;

/*
	<applet code = "Calc" width = 200 height = 150>
	</applet>
 */

public class Calc extends Applet implements ActionListener{
	
	TextField expText, resText;
	Parser p;
	
	public void init() {
		
		// Title
		Label heading = new Label("Expression Calculator ", Label.CENTER);
		
		// Sub Title
		Label explab = new Label("Expression ", Label.CENTER);
		Label reslab = new Label("Result	 ", Label.CENTER);
		
		// ��ĭ
		expText = new TextField(24);
		resText = new TextField(24);
		
		// ��� �ؽ�Ʈ �ʵ�� ���� �Ұ�
		resText.setEditable(false);	// ȭ�� ����� ���� ��� �ʵ�
		
		add(heading);
		add(explab);
		add(expText);
		add(reslab);
		add(resText);
		
		// �ؽ�Ʈ �ʵ� �׼� �����ʷ� ���
		expText.addActionListener(this);
		
		// �ļ� ����
		p = new Parser();
	}
		
	// ���� ������ ó��
	public void  actionPerformed(ActionEvent ae) {
		repaint();
	}
	
	
	
	public void paint(Graphics g) {
		double result = 0.0;
		String expstr = expText.getText();
		
		try {
			if(expstr.length() != 0)
				result = p.evaluate(expstr);
		
		// ������ ���� ����Ű�� ������ ���� ǥ���� �����ϱ�
		// expText.setText("");
		
		resText.setText(Double.toString(result));
		
		showStatus("");	// ���� �޽��� ����
		} catch (ParserException exc) {
			showStatus(exc.toString());
			resText.setText("");
		}
	}
}
