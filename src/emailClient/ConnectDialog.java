package emailClient;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/* �̸��� ������ �����ϱ� ���� ���� ���� ������
 * �Է��ϰ� ���ִ� Ŭ���� */
public class ConnectDialog extends JDialog
{
  // �̸��� ���� Ÿ��
  private static final String[] TYPES = {"pop3", "imap"};

  // �̸��� ���� Ÿ���� �����ϱ� ���� �޺��ڽ�
  private JComboBox typeComboBox;

  // ����, ����� �̸�, SMTP ���� �ؽ�Ʈ �ʵ�
  private JTextField serverTextField, usernameTextField;
  private JTextField smtpServerTextField;

  // ��й�ȣ �ؽ�Ʈ �ʵ�
  private JPasswordField passwordField;

  //������
  public ConnectDialog(Frame parent)
  {
    // ���� Ŭ���� ������, ��ȭ���ڰ� ���(modal)���� ��
    super(parent, true);

    // Ÿ��Ʋ ����
    setTitle("Connect");

    // closing �̺�Ʈ �ڵ鸵
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        actionCancel();
      }
    });

    // ���� �г�
    JPanel settingsPanel = new JPanel();
    settingsPanel.setBorder(
      BorderFactory.createTitledBorder("Connection Settings"));
    GridBagConstraints constraints;
    GridBagLayout layout = new GridBagLayout();
    settingsPanel.setLayout(layout);
    JLabel typeLabel = new JLabel("Type:");
    constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.EAST;
    constraints.insets = new Insets(5, 5, 0, 0);
    layout.setConstraints(typeLabel, constraints);
    settingsPanel.add(typeLabel);
    typeComboBox = new JComboBox(TYPES);
    constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.WEST;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.insets = new Insets(5, 5, 0, 5);
    constraints.weightx = 1.0D;
    layout.setConstraints(typeComboBox, constraints);
    settingsPanel.add(typeComboBox);
    JLabel serverLabel = new JLabel("Server:");
    constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.EAST;
    constraints.insets = new Insets(5, 5, 0, 0);
    layout.setConstraints(serverLabel, constraints);
    settingsPanel.add(serverLabel);
    serverTextField = new JTextField(25);
    constraints = new GridBagConstraints();
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.insets = new Insets(5, 5, 0, 5);
    constraints.weightx = 1.0D;
    layout.setConstraints(serverTextField, constraints);
    settingsPanel.add(serverTextField);
    JLabel usernameLabel = new JLabel("Username:");
    constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.EAST;
    constraints.insets = new Insets(5, 5, 0, 0);
    layout.setConstraints(usernameLabel, constraints);
    settingsPanel.add(usernameLabel);
    usernameTextField = new JTextField();
    constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.insets = new Insets(5, 5, 0, 5);
    constraints.weightx = 1.0D;
    layout.setConstraints(usernameTextField, constraints);
    settingsPanel.add(usernameTextField);
    JLabel passwordLabel = new JLabel("Password:");
    constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.EAST;
    constraints.insets = new Insets(5, 5, 5, 0);
    layout.setConstraints(passwordLabel, constraints);
    settingsPanel.add(passwordLabel);
    passwordField = new JPasswordField();
    constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.insets = new Insets(5, 5, 5, 5);
    constraints.weightx = 1.0D;
    layout.setConstraints(passwordField, constraints);
    settingsPanel.add(passwordField);
    JLabel smtpServerLabel = new JLabel("SMTP Server:");
    constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.EAST;
    constraints.insets = new Insets(5, 5, 5, 0);
    layout.setConstraints(smtpServerLabel, constraints);
    settingsPanel.add(smtpServerLabel);
    smtpServerTextField = new JTextField(25);
    constraints = new GridBagConstraints();
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.insets = new Insets(5, 5, 5, 5);
    constraints.weightx = 1.0D;
    layout.setConstraints(smtpServerTextField, constraints);
    settingsPanel.add(smtpServerTextField);

    // ��ư �г� ����
    JPanel buttonsPanel = new JPanel();
    JButton connectButton = new JButton("Connect");
    connectButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionConnect();
      }
    });
    buttonsPanel.add(connectButton);
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionCancel();
      }
    });
    buttonsPanel.add(cancelButton);

    // �г��� �����̳ʿ� ����
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(settingsPanel, BorderLayout.CENTER);
    getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

    // ������Ʈ�� ���缭 ��ȭ
    pack();

    // ��ȭ���ڸ� ���ø����̼��� �߾ӿ� ����
    setLocationRelativeTo(parent);
  }

  // ���� ������ Ȯ���ϰ� ��ȭ���ڸ� ����
  private void actionConnect() {
    if (serverTextField.getText().trim().length() < 1
        || usernameTextField.getText().trim().length() < 1
        || passwordField.getPassword().length < 1
        || smtpServerTextField.getText().trim().length() < 1) {
      JOptionPane.showMessageDialog(this,
        "One or more settings is missing.",
        "Missing Setting(s)", JOptionPane.ERROR_MESSAGE);
      return;
    }

    // ��ȭ���ڸ� ����
    dispose();
  }

  // ������ ����ϰ� ���α׷��� ����
  private void actionCancel() {
    System.exit(0);
  }

  // �̸��� ���� Ÿ�� ������
  public String getServerType() {
    return (String) typeComboBox.getSelectedItem();
  }

  // �̸��� ���� �ּ� ������
  public String getServer() {
    return serverTextField.getText();
  }

  // �̸��� ����� �̸� ������
  public String getUsername() {
	  return usernameTextField.getText();
  }

  // ��й�ȣ ������
  public String getPassword() {
    return new String(passwordField.getPassword());
  }

  // SMTP ���� �ּ� ������
  public String getSmtpServer() {
    return smtpServerTextField.getText();
  }
}
