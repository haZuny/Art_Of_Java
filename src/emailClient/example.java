package emailClient;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class example {
	public static void main(String[] args) {
		naverMailSend();
	}

	// smtp ����
	public static void naverMailSend() {
		String host = "smtp.naver.com"; // ���̹��� ��� ���̹� ����, gmail��� gmail ����
		String user = "gkwns5791@naver.com"; // �н�����
		String password = "hajun12o99!";

		// SMTP ���� ������ �����Ѵ�.
		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", 587);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.ssl.protocols", "TLSv1.2");

		Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user, password);
			}
		});

		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(user));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress("gkwns5791@naver.com"));

			// ���� ����
			message.setSubject("KTKO SMTP TEST1111");

			// ���� ����
			message.setText("KTKO Success!!");

			// send the message
			Transport.send(message);
			System.out.println("Success Message Send");

		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	// pop3 ����
	public static void open() throws AddressException, MessagingException {

		String host = "pop.naver.com";

		final String username = "gkwns5791"; // @naver.com �� �����ϰ� ���̵�.
		final String password = "hajun12o99!";
		int port = 995;

		Properties props = System.getProperties();

		props.put("mail.pop3.host", host);
		props.put("mail.pop3.port", port);
		props.put("mail.pop3.auth", "true");
		props.put("mail.pop3.ssl.enable", "true");
		props.put("mail.pop3.ssl.trust", host);
		Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {

			String un = username;
			String pw = password;

			protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
				return new javax.mail.PasswordAuthentication(un, pw);
			}

		});

		session.setDebug(false);
		Store store = session.getStore("pop3");
		store.connect();
		Folder folder = store.getFolder("INBOX");
		folder.open(Folder.READ_ONLY);
		Message[] messages = folder.getMessages();

		for (Message message : messages) {
			System.out.print(":::::::::::::::::::::::::::::::::::");
			System.out.println(message.getSubject());
		}

		store.close();
	}

}