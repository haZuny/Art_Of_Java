package downloadManager;

import java.io.*;
import java.net.*;
import java.util.*;

// URL�������� ������ �ٿ�ε�
class Download extends Observable implements Runnable {
	// Max size of download buffer.
	private static final int MAX_BUFFER_SIZE = 1024;

	// ���� �ڵ� �̸�
	public static final String STATUSES[] = { "Downloading", "Paused", "Complete", "Cancelled", "Error" };

	// ���� �ڵ�
	public static final int DOWNLOADING = 0;
	public static final int PAUSED = 1;
	public static final int COMPLETE = 2;
	public static final int CANCELLED = 3;
	public static final int ERROR = 4;

	private URL url; // �ٿ�ε��� URL
	private int size; // �ٿ�ε��� ������ ũ��(����Ʈ)
	private int downloaded; // �ٿ�ε��� ũ��(����Ʈ)
	private int status; // �ٿ�ε��� ���� ����

	// �ٿ�ε��� ������
	public Download(URL url) {
		this.url = url;
		size = -1;
		downloaded = 0;
		status = DOWNLOADING;

		// �ٿ�ε� ����
		download();
	}

	// �ٿ�ε��� URL�� ����
	public String getUrl() {
		return url.toString();
	}

	// �ٿ�ε��� ������ ũ�⸦ ����
	public int getSize() {
		return size;
	}

	// �ٿ�ε��� ������� ����
	public float getProgress() {
		return ((float) downloaded / size) * 100;
	}

	// �ٿ�ε��� ���¸� ����
	public int getStatus() {
		return status;
	}

	// �ٿ�ε� ����
	public void pause() {
		status = PAUSED;
		stateChanged();
	}

	// �ٿ�ε� �簳
	public void resume() {
		status = DOWNLOADING;
		stateChanged();
		download();
	}

	// �ٿ�ε� ���
	public void cancel() {
		status = CANCELLED;
		stateChanged();
	}

	// �� �ٿ�ε忡 ������ ������ ǥ��
	private void error() {
		status = ERROR;
		stateChanged();
	}

	// �ٿ�ε带 �����ϰų� �簳
	private void download() {
		Thread thread = new Thread(this);
		thread.start();
	}

	// URL���� ���� �̸� �κ��� ����
	private String getFileName(URL url) {
		String fileName = url.getFile();
		return fileName.substring(fileName.lastIndexOf('/') + 1);
	}

	// ���� �ٿ�ε�
	public void run() {
		RandomAccessFile file = null;
		InputStream stream = null;
		URLConnection connection = null;

		try {

			// HTTP��������
			if (url.getProtocol().equals("http"))
				// URL ���� ��ü ����
				connection = (HttpURLConnection) url.openConnection();
			else if (url.getProtocol().equals("ftp"))
				// URL ���� ��ü ����
				connection = url.openConnection();

			// ������ ��� �κ� �ٿ�ε��� ������ ��(ó������ ������ �ٿ�)
			connection.setRequestProperty("Range", "bytes=" + downloaded + "-");
			// ������ ����
			connection.connect();
			// ���� �ڵ尡 200�� �뿡 �ִ��� Ȯ��
			if (url.getProtocol().equals("http")) {
				if (((HttpURLConnection) connection).getResponseCode() / 100 != 2) {
					error();
				}
			}
			// ��ȿ�� contentlength�� �˻�
			int contentLength = connection.getContentLength();
			if (contentLength < 1) {
				error();
			}
			// ���� �ٿ�ε忡 ���� ũ�Ⱑ �������� �ʾ����� ����
			if (size == -1) {
				size = contentLength;
				stateChanged();
			}
			// ������ �� ����, ���� �����͸� ������ ������ �̵�
			file = new RandomAccessFile(getFileName(url), "rw");
			file.seek(downloaded);

			stream = connection.getInputStream();

			while (status == DOWNLOADING) {
				// �ٿ�ε��� �κ��� �󸶳� �� ���� �ִ����� �ٶ� ������ ũ�⸦ ����
				byte buffer[];
				if (size - downloaded > MAX_BUFFER_SIZE) {
					buffer = new byte[MAX_BUFFER_SIZE];
				} else {
					buffer = new byte[size - downloaded];
				}

				// ������������ ���۷� �о��
				int read = stream.read(buffer);
				if (read == -1)
					break;

				// ������ ������ ���Ͽ� ��
				file.write(buffer, 0, read);
				downloaded += read;
				stateChanged();
			}

			// �� ������ �����ϸ� �ٿ�ε尡 �������� �ǹ�
			// ���°��� �Ϸ�� �ٲ�
			if (status == DOWNLOADING) {
				status = COMPLETE;
				stateChanged();
			}
		} catch (

		Exception e) {
			error();
		} finally {
			// ���� ����
			if (file != null) {
				try {
					file.close();
				} catch (Exception e) {
				}
			}

			// ���� ������ ����
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception e) {
				}
			}
		}
	}

	// �ٿ�ε� ���°� ����Ǿ����� �����ڵ鿡�� �˸�
	private void stateChanged() {
		setChanged();
		notifyObservers();
	}
}
