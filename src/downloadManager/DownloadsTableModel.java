package downloadManager;

import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

// �ٿ�ε� ���̺��� �����͸� �����ϴ� Ŭ����
class DownloadsTableModel extends AbstractTableModel
  implements Observer
{
  // ���̺��� �� ���� ���� �̸���
  private static final String[] columnNames = {"URL", "Size",
    "Progress", "Status"};

  // �� ���� ���� ���� Ŭ������
  private static final Class[] columnClasses = {String.class,
    String.class, JProgressBar.class, String.class};

  // ���̺��� �ٿ�ε� ����Ʈ
  private ArrayList downloadList = new ArrayList();

  // ���ο� �ٿ�ε带 ���̺� �߰�
  public void addDownload(Download download) {
    // �ٿ�ε尡 ����� �� �뺸�޵��� ���
    download.addObserver(this);

    downloadList.add(download);

    // ���̺� �� ������ ���̺��� �뺸
    fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
  }

  // ������ �࿡ ���� �ٿ�ε带 ����
  public Download getDownload(int row) {
    return (Download) downloadList.get(row);
  }

  // ����Ʈ���� �ٿ�ε带 ����
  public void clearDownload(int row) {
    downloadList.remove(row);

    // ���̺� �� ������ ���̺��� �뺸
    fireTableRowsDeleted(row, row);
  }

  // ���̺��� �� ������ ����
  public int getColumnCount() {
    return columnNames.length;
  }

  // ���� �̸��� ����
  public String getColumnName(int col) {
     return columnNames[col];
  }

  // ���� Ŭ������ ����
  public Class getColumnClass(int col) {
    return columnClasses[col];
  }

  // ���̺��� �� ������ ����
  public int getRowCount() {
    return downloadList.size();
  }

  // ������ ���� ���� ���տ� ���� ���� ����
  public Object getValueAt(int row, int col) {
    Download download = (Download) downloadList.get(row);
    switch (col) {
      case 0: // URL
        return download.getUrl();
      case 1: // Size
        int size = download.getSize();
        return (size == -1) ? "" : Integer.toString(size);
      case 2: // Progress
        return new Float(download.getProgress());
      case 3: // Status
        return Download.STATUSES[download.getStatus()];
    }
    return "";
  }

  // Download�� ���� ��ȭ�� �Ͼ�� Download ��ü�� �����ڵ鿡�� �뺸�� �� ȣ��
  public void update(Observable o, Object arg) {
    int index = downloadList.indexOf(o);

    // ���̺� �� ������ ���̺��� �뺸
    fireTableRowsUpdated(index, index);
  }
}
