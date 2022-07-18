package emailClient;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.mail.*;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.*;
import javax.swing.*;
import javax.swing.event.*;

// �̸��� Ŭ���̾�Ʈ
public class EmailClient extends JFrame {
	// M�޽��� ���̺��� ������ ��
	private MessagesTableModel tableModel;

	// �޽��� ����� ������ ���̺�
	private JTable table;

	// �޽��� ����� ������ �ؽ�Ʈ ����
	private JTextArea messageTextArea;

	/* �޽��� ���̺�� �޽��� �� �ǳ��� ������ ���ø� �ǳ� */
	private JSplitPane splitPane;

	// ����, ����, ���� ��ư
	private JButton replyButton, forwardButton, deleteButton;

	// ���̺��� ���� ���õ� �޽���
	private Message selectedMessage;

	// � �޽����� ���� �����ǰ� �ִ��� ���θ� ��Ÿ���� �÷���
	private boolean deleting;

	// JavaMail ����
	private Session session;

	// ������
	public EmailClient() {
		// Ÿ��Ʋ ����
		setTitle("E-mail Client");

		// ������ ũ�� ����
		setSize(640, 480);

		// ������ ���� �̺�Ʈ ó��
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				actionExit();
			}
		});

		// ���� �޴� ����
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		JMenuItem fileExitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
		fileExitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionExit();
			}
		});
		fileMenu.add(fileExitMenuItem);
		menuBar.add(fileMenu);
		setJMenuBar(menuBar);

		// ��ư �г� ����
		JPanel buttonPanel = new JPanel();
		JButton newButton = new JButton("New Message");
		newButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionNew();
			}
		});
		buttonPanel.add(newButton);

		// �޽��� ���̺� ����
		tableModel = new MessagesTableModel();
		table = new JTable(tableModel);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				tableSelectionChanged();
			}
		});
		// �� ���� �ϳ��� �ุ ���õǵ��� ��
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// �̸��� �г� ����
		JPanel emailsPanel = new JPanel();
		emailsPanel.setBorder(BorderFactory.createTitledBorder("E-mails"));
		messageTextArea = new JTextArea();
		messageTextArea.setEditable(false);
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(table), new JScrollPane(messageTextArea));
		emailsPanel.setLayout(new BorderLayout());
		emailsPanel.add(splitPane, BorderLayout.CENTER);

		// ��ư �г� 2 ����
		JPanel buttonPanel2 = new JPanel();
		replyButton = new JButton("Reply");
		replyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionReply();
			}
		});
		replyButton.setEnabled(false);
		buttonPanel2.add(replyButton);
		forwardButton = new JButton("Forward");
		forwardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionForward();
			}
		});
		forwardButton.setEnabled(false);
		buttonPanel2.add(forwardButton);
		deleteButton = new JButton("Delete");
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionDelete();
			}
		});
		deleteButton.setEnabled(false);
		buttonPanel2.add(deleteButton);

		// �г��� �����̳ʿ� ����
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(buttonPanel, BorderLayout.NORTH);
		getContentPane().add(emailsPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel2, BorderLayout.SOUTH);
	}

	// ���α׷� ����
	private void actionExit() {
		System.exit(0);
	}

	// �� �޽��� �ۼ�
	private void actionNew() {
		sendMessage(MessageDialog.NEW, null);
	}

	// ���̺��� ���� ���õ� ������ ȣ��
	private void tableSelectionChanged() {
		// ���õ� �࿡ ����ִ� �޽����� �������� �ƴ϶��, ����ڿ��� ������
		if (!deleting) {
			selectedMessage = tableModel.getMessage(table.getSelectedRow());
			showSelectedMessage();
			updateButtons();
		}
	}

	// ���� �޽��� ������
	private void actionReply() {
		sendMessage(MessageDialog.REPLY, selectedMessage);
	}

	// �޽��� �����ϱ�
	private void actionForward() {
		sendMessage(MessageDialog.FORWARD, selectedMessage);
	}

	// ���õ� �޽��� ����
	private void actionDelete() {
		deleting = true;

		try {
			// �������� �޽��� ����
			selectedMessage.setFlag(Flags.Flag.DELETED, true);
			Folder folder = selectedMessage.getFolder();
			folder.close(true);
			folder.open(Folder.READ_WRITE);
		} catch (Exception e) {
			showError("Unable to delete message.", false);
		}

		// ���̺��� �޽��� ����
		tableModel.deleteMessage(table.getSelectedRow());

		// GUI ����
		messageTextArea.setText("");
		deleting = false;
		selectedMessage = null;
		updateButtons();
	}

	// �޽��� ������
	private void sendMessage(int type, Message message) {
		// �޽��� ��ȭ���ڸ� ����
		MessageDialog dialog;
		try {
			dialog = new MessageDialog(this, type, message);
			if (!dialog.display()) {
				// ��� ��ư�� ���� ���ϵǴ� ���
				return;
			}
		} catch (Exception e) {
			showError("Unable to send message.", false);
			return;
		}

		try {
			// �޽��� ��ȭ������ �����ڸ� �̿��� �� �޽��� �ۼ�
			Message newMessage = new MimeMessage(session);
			newMessage.setFrom(new InternetAddress(dialog  .getFrom()));
			newMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(dialog.getTo()));
			newMessage.setSubject(dialog.getSubject());
			newMessage.setSentDate(new Date());
			newMessage.setText(dialog.getContent());

			// �� �޽����� ����
			Transport.send(newMessage);
		} catch (Exception e) {
			showError("Unable to send message.", false);
			System.out.println("�޼��� ���� ����====================");
			e.printStackTrace();
			System.out.println("�޼��� ���� ����====================");
		}
	}

	// ���õ� �޽����� ������
	private void showSelectedMessage() {
		// �޽����� �ε��Ǵ� ���� Ŀ���� �𷡽ð�� �ٲ۴�
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			messageTextArea.setText(getMessageContent(selectedMessage));
			messageTextArea.setCaretPosition(0);
		} catch (Exception e) {
			showError("Unabled to load message.", false);
		} finally {
			// Ŀ���� ������� �ǵ�����
			setCursor(Cursor.getDefaultCursor());
		}
	}

	/* �� ��ư�� ���¸� ���̺� ���� ���õ� �޽����� �մ��� ���ο� ���� ���� */
	private void updateButtons() {
		if (selectedMessage != null) {
			replyButton.setEnabled(true);
			forwardButton.setEnabled(true);
			deleteButton.setEnabled(true);
		} else {
			replyButton.setEnabled(false);
			forwardButton.setEnabled(false);
			deleteButton.setEnabled(false);
		}
	}

	// ȭ�鿡 ���ø����̼� �����츦 ����
	public void show() {
		super.show();

		// ���ø� �ǳ��� ������ 50�� 50���� �����
		splitPane.setDividerLocation(.5);
	}

	// �̸��� ������ ����
	public void connect() {
		// ���� ��ȭ���ڸ� ����
		ConnectDialog dialog = new ConnectDialog(this);
		dialog.show();

		// ���� ��ȭ���ڷκ��� ���� url�� �����
		StringBuffer connectionUrl = new StringBuffer();
		connectionUrl.append(dialog.getServerType() + "://");
		connectionUrl.append(dialog.getUsername() + ":");
		connectionUrl.append(dialog.getPassword() + "@");
		System.out.println(dialog.getServerType());
		if(dialog.getServerType().equals("pop3"))		
			connectionUrl.append("pop.");
		else
			connectionUrl.append("imap.");
		connectionUrl.append(dialog.getServer() + "/");

		// �޽����� �ٿ�ε��ϰ� ������ �˸��� �ٿ�ε� ��ȭ���ڸ� ����.
		final DownloadingDialog downloadingDialog = new DownloadingDialog(this);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				downloadingDialog.show();
			}
		});

		// jAVAmAIL ������ �ʱ�ȭ �� �� ������ ����
		Store store = null;
		try {
			// javaMail ������ SMTP ������ �ʱ�ȭ
			Properties props = new Properties();
			props.put("mail.smtp.host", dialog.getSmtpServer());
			props.put("mail.smtp.port", 587); // smtp ��Ʈ ����
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.ssl.protocols", "TLSv1.2"); // ����, Ŭ���̾�Ʈ�� SSL/TLS ������ ���� / smtp

			props.put("mail.pop3.host", dialog.getServer());
			props.put("mail.pop3.port", 995); // pop3 ��Ʈ ����
			props.put("mail.pop3.ssl.protocols", "TLSv1.2"); // ����, Ŭ���̾�Ʈ�� SSL/TLS ������ ���� / pop3
			props.put("mail.pop3.ssl.enable", "true");

			props.put("mail.imap.host", dialog.getServer());
			props.put("mail.imap.port", 995); // imap ��Ʈ ����
			props.put("mail.imap.ssl.protocols", "TLSv1.2"); // ����, Ŭ���̾�Ʈ�� SSL/TLS ������ ���� / imap
			props.put("mail.imap.ssl.enable", "true");

			session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(dialog.getUsername() + "@" + dialog.getServer(),
							dialog.getPassword());
				}
			});

			// �̸��� ������ ����
			URLName urln = new URLName(connectionUrl.toString());
			store = session.getStore(urln);
			store.connect();
		} catch (Exception e) {
			System.out.println("���� ����====================");
			e.printStackTrace();
			System.out.println("���� ����====================");
			// �ٿ�ε� ��ȭ���ڸ� �ݴ´�.
			downloadingDialog.dispose();

			// ���� ���
			showError("Unable to connect.", true);
		}

		// �����κ��� �޽��� ����� �ٿ�ε�
		try {
			// ���� ������ ������ ����.
			Folder folder = store.getFolder("INBOX");
			folder.open(Folder.READ_WRITE);

			// �޽��� ����Ʈ�� �޾ƿ´�.
			Message[] messages = folder.getMessages();

			// ������ �� �޽����� ���� ��� ������ �����´�
			FetchProfile profile = new FetchProfile();
			profile.add(FetchProfile.Item.ENVELOPE);
			folder.fetch(messages, profile);

			// ���̺� �޽����� �ִ´�.
			tableModel.setMessages(messages);
		} catch (Exception e) {
			// �ٿ�ε� ��ȭ���ڸ� �ݴ´�.
			downloadingDialog.dispose();

			// ���� ���
			showError("Unable to download messages.", true);
		}

		// �ٿ�ε� ��ȭ���ڸ� �ݴ´�.
		downloadingDialog.dispose();
	}

	// �ʿ��ϴٸ� ������ ����ϰ� ���α׷��� �����Ѵ�
	private void showError(String message, boolean exit) {
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
		if (exit)
			System.exit(0);
	}

	// �޽��� ������ ��´�.
	public static String getMessageContent(Message message) throws Exception {
		Object content = message.getContent();
		if (content instanceof Multipart) {
			StringBuffer messageContent = new StringBuffer();
			Multipart multipart = (Multipart) content;
			for (int i = 0; i < multipart.getCount(); i++) {
				Part part = (Part) multipart.getBodyPart(i);
				if (part.isMimeType("text/plain")) {
					messageContent.append(part.getContent().toString());
				}
			}
			return messageContent.toString();
		} else {
			return content.toString();
		}
	}

	// �̸��� Ŭ���̾�Ʈ ����
	public static void main(String[] args) {
		EmailClient client = new EmailClient();
		client.show();

		// ���� ��ȭ���ڸ� ����
		client.connect();
	}
}
