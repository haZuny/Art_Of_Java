package emailClient;

import java.awt.*;
import java.awt.event.*;
import javax.mail.*;
import javax.swing.*;

// ���� �޽��� �ۼ��� ���� Ŭ����
public class MessageDialog extends JDialog
{
  // �޽��� Ÿ�� �ĺ���
  public static final int NEW = 0;
  public static final int REPLY = 1;
  public static final int FORWARD = 2;

  // �۽���, ������, ���� �ؽ�Ʈ �ʵ�
  private JTextField fromTextField, toTextField;
  private JTextField subjectTextField;

  // �޽��� ���� �ؽ�Ʈ ����
  private JTextArea contentTextArea;

  // ��ȭ���ڰ� ��� ��ư�� Ŭ���ؼ� �������� ���θ� ��Ÿ���� �÷���
  private boolean cancelled;

  // ������
  public MessageDialog(Frame parent, int type, Message message)
    throws Exception
  {
    // ���� Ŭ���� ������, ��ȭ���ڰ� ������� ��
    super(parent, true);

    /* Ÿ��Ʋ ���� �� �޽��� Ÿ�Կ� ���� ������, ����, ���� ���� ���� */
    String to = "", subject = "", content = "";
    switch (type) {
      // ���� �޽���
      case REPLY:
        setTitle("Reply To Message");

        // ������
        Address[] senders = message.getFrom();
        if (senders != null || senders.length > 0) {
          to = senders[0].toString();
        }
        to = message.getFrom()[0].toString();

        // ����
        subject = message.getSubject();
        if (subject != null && subject.length() > 0) {
          subject = "RE: " + subject;
        } else {
          subject = "RE:";
        }

        // �޽��� ����
        content = "\n----------------- " +
                  "REPLIED TO MESSAGE" +
                  " -----------------\n" +
                  EmailClient.getMessageContent(message);
        break;

      // ���� �޽���
      case FORWARD:
        setTitle("Forward Message");

        // ����
        subject = message.getSubject();
        if (subject != null && subject.length() > 0) {
          subject = "FWD: " + subject;
        } else {
          subject = "FWD:";
        }

        // �޽��� ����
        content = "\n----------------- " +
                  "FORWARDED MESSAGE" +
                  " -----------------\n" +
                  EmailClient.getMessageContent(message);
        break;

      // �� �޽���
      default:
        setTitle("New Message");
    }

    // closing �̺�Ʈ �ڵ鸵
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        actionCancel();
      }
    });

    // �޽��� �ʵ� �ǳ� ����
    JPanel fieldsPanel = new JPanel();
    GridBagConstraints constraints;
    GridBagLayout layout = new GridBagLayout();
    fieldsPanel.setLayout(layout);
    JLabel fromLabel = new JLabel("From:");
    constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.EAST;
    constraints.insets = new Insets(5, 5, 0, 0);
    layout.setConstraints(fromLabel, constraints);
    fieldsPanel.add(fromLabel);
    fromTextField = new JTextField();
    constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.insets = new Insets(5, 5, 0, 0);
    layout.setConstraints(fromTextField, constraints);
    fieldsPanel.add(fromTextField);
    JLabel toLabel = new JLabel("To:");
    constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.EAST;
    constraints.insets = new Insets(5, 5, 0, 0);
    layout.setConstraints(toLabel, constraints);
    fieldsPanel.add(toLabel);
    toTextField = new JTextField(to);
    constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.insets = new Insets(5, 5, 0, 0);
    constraints.weightx = 1.0D;
    layout.setConstraints(toTextField, constraints);
    fieldsPanel.add(toTextField);
    JLabel subjectLabel = new JLabel("Subject:");
    constraints = new GridBagConstraints();
    constraints.insets = new Insets(5, 5, 5, 0);
    layout.setConstraints(subjectLabel, constraints);
    fieldsPanel.add(subjectLabel);
    subjectTextField = new JTextField(subject);
    constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.insets = new Insets(5, 5, 5, 0);
    layout.setConstraints(subjectTextField, constraints);
    fieldsPanel.add(subjectTextField);

    // �޽��� ���� �ǳ� ����
    JScrollPane contentPanel = new JScrollPane();
    contentTextArea = new JTextArea(content, 10, 50);
    contentPanel.setViewportView(contentTextArea);

    // ��ư �ǳ� ����
    JPanel buttonsPanel = new JPanel();
    JButton sendButton = new JButton("Send");
    sendButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionSend();
      }
    });
    buttonsPanel.add(sendButton);
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionCancel();
      }
    });
    buttonsPanel.add(cancelButton);

    // �г��� �����̳ʿ� ����
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(fieldsPanel, BorderLayout.NORTH);
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

    // ������Ʈ�� ���缭 ��ȭ������ ũ�⸦ ����
    pack();

    // ��ȭ���ڸ� ���ø����̼��� �߾ӿ� ����
    setLocationRelativeTo(parent);
  }

  // �޽��� �ʵ带 Ȯ���ϰ� ��ȭ���ڸ� �ݴ´�.
  private void actionSend() {
    if (fromTextField.getText().trim().length() < 1
        || toTextField.getText().trim().length() < 1
        || subjectTextField.getText().trim().length() < 1
        || contentTextArea.getText().trim().length() < 1) {
      JOptionPane.showMessageDialog(this,
        "One or more fields is missing.",
        "Missing Field(s)", JOptionPane.ERROR_MESSAGE);
      return;
    }

    // ��ȭ���ڸ� ����
    dispose();
  }

  // �޽��� ������ ����ϰ� ��ȭ���ڸ� �ݴ´�.
  private void actionCancel() {
    cancelled = true;

    // ��ȭ���ڸ� ����
    dispose();
  }

  // ��ȭ���ڸ� �����ش�.
  public boolean display() {
    show();

    // ���� ��ư�� ��� ��ư �� � �Ϳ� ���� �����°��� ����
    return !cancelled;
  }

  // �۽��� �ʵ� ������
  public String getFrom() {
    return fromTextField.getText();
  }

  // ������ �ʵ� ������
  public String getTo() {
    return toTextField.getText();
  }

  // ���� �ʵ� ������
  public String getSubject() {
    return subjectTextField.getText();
  }

  // �޽��� ���� �ʵ� ������
  public String getContent() {
    return contentTextArea.getText();
  }
}
