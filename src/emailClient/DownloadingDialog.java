package emailClient;

import java.awt.*;
import javax.swing.*;

/* �޽����� �ٿ�ε� �ǰ� ������ ����ڿ��� �˸��� ������ ��ȭ���� */
public class DownloadingDialog extends JDialog
{
  // ������
  public DownloadingDialog(Frame parent)
  {
    // ���� Ŭ���� ������, ��ȭ���ڰ� ������� ��
    super(parent, true);

    // Ÿ��Ʋ ����
    setTitle("E-mail Client");

    // X�� Ŭ���Ǵ��� �����찡 ������ �ʵ��� ��
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

    // �ٿ�ε� �ϴ� ���� �޽��� ���
    JPanel contentPane = new JPanel();
    contentPane.setBorder(
      BorderFactory.createEmptyBorder(5, 5, 5, 5));
    contentPane.add(new JLabel("Downloading messages..."));
    setContentPane(contentPane);

    // ������Ʈ�� ���缭 ��ȭ������ ũ�⸦ ����
    pack();

    // ��ȭ���ڸ� ���ø����̼��� �߾ӿ� ����
    setLocationRelativeTo(parent);
  }
}
