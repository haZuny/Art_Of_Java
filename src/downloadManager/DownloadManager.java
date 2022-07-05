package downloadManager;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

// �ٿ�ε� �Ŵ���
public class DownloadManager extends JFrame
  implements Observer
{
  // �ٿ�ε带 �߰��ϴ� �ؽ�Ʈ �ʵ�
  private JTextField addTextField;

  // �ٿ�ε� ���̺��� ������ ��
  private DownloadsTableModel tableModel;
  
  // �ٿ�ε� ����Ʈ ���̺�
  private JTable table;

  // ���õ� �ٿ�ε带 �����ϴ� ��ư��
  private JButton pauseButton, resumeButton;
  private JButton cancelButton, clearButton;

  // ���� ���õ� �ٿ�ε�
  private Download selectedDownload;

  // ���̺� ������ �����Ǿ����� ���θ� ��Ÿ���� �÷���
  private boolean clearing;

  // ������
  public DownloadManager()
  {
    // Ÿ��Ʋ ����
    setTitle("Download Manager");

    // ������ ũ�� ����
    setSize(640, 480);

    // �����찡 ���� ���� �̺�Ʈ�� ó��
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        actionExit();
      }
    });

    // ���� �޴� ����
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);
    JMenuItem fileExitMenuItem = new JMenuItem("Exit",
      KeyEvent.VK_X);
    fileExitMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionExit();
      }
    });
    fileMenu.add(fileExitMenuItem);
    menuBar.add(fileMenu);
    setJMenuBar(menuBar);

    // add �г��� ����
    JPanel addPanel = new JPanel();
    addTextField = new JTextField(30);
    addPanel.add(addTextField);
    JButton addButton = new JButton("Add Download");
    addButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionAdd();
      }
    });
    addPanel.add(addButton);

    // Downloads ���̺��� ����
    tableModel = new DownloadsTableModel();
    table = new JTable(tableModel);
    table.getSelectionModel().addListSelectionListener(new
      ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        tableSelectionChanged();
      }
    });
    // �� ���� �� �ุ ���õǵ��� ��
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
  
    // ProgressBar�� progress ���� �������� ����
    ProgressRenderer renderer = new ProgressRenderer(0, 100);
    renderer.setStringPainted(true); // show progress text
    table.setDefaultRenderer(JProgressBar.class, renderer);

    // JProgressBar�� �µ��� ���̺� ���� ���̸� ����� ũ�� ����
    table.setRowHeight(
      (int) renderer.getPreferredSize().getHeight());

    // downloads �г��� ����
    JPanel downloadsPanel = new JPanel();
    downloadsPanel.setBorder(
      BorderFactory.createTitledBorder("Downloads"));
    downloadsPanel.setLayout(new BorderLayout());
    downloadsPanel.add(new JScrollPane(table),
      BorderLayout.CENTER);

    // buttons �г��� ����
    JPanel buttonsPanel = new JPanel();
    pauseButton = new JButton("Pause");
    pauseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionPause();
      }
    });
    pauseButton.setEnabled(false);
    buttonsPanel.add(pauseButton);
    resumeButton = new JButton("Resume");
    resumeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionResume();
      }
    });
    resumeButton.setEnabled(false);
    buttonsPanel.add(resumeButton);
    cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionCancel();
      }
    });
    cancelButton.setEnabled(false);
    buttonsPanel.add(cancelButton);
    clearButton = new JButton("Clear");
    clearButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionClear();
      }
    });
    clearButton.setEnabled(false);
    buttonsPanel.add(clearButton);

    // ����� �гε��� �߰�
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(addPanel, BorderLayout.NORTH);
    getContentPane().add(downloadsPanel, BorderLayout.CENTER);
    getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
  }

  // ���α׷��� ����
  private void actionExit() {
    System.exit(0);
  }

  // ���ο� �ٿ�ε带 �߰�
  private void actionAdd() {
    URL verifiedUrl = verifyUrl(addTextField.getText());
    if (verifiedUrl != null) {
      tableModel.addDownload(new Download(verifiedUrl));
      addTextField.setText(""); // add �ؽ�Ʈ �ʵ� ����
    } else {
      JOptionPane.showMessageDialog(this,
        "Invalid Download URL", "Error",
        JOptionPane.ERROR_MESSAGE);
    }
  }

  // �ٿ�ε��� URL�� Ȯ��
  private URL verifyUrl(String url) {
    // Only allow HTTP URLs.
    if (!url.toLowerCase().startsWith("http://"))
      return null;

    // URL�� ������ ����
    URL verifiedUrl = null;
    try {
      verifiedUrl = new URL(url);
    } catch (Exception e) {
      return null;
    }

    // URL�� ������ �����ϰ� �ִ��� Ȯ��
    if (verifiedUrl.getFile().length() < 2)
      return null;

    return verifiedUrl;
  }

  // ���̺��� �� ������ �ٱ� �� ȣ���
  private void tableSelectionChanged() {
    // ������ ���õȴٿ�ε�κ��� �뺸�ޱ⸦ �����
    if (selectedDownload != null)
      selectedDownload.deleteObserver(DownloadManager.this);

    // �ٿ�ε带 ����Ʈ���� �����ϴ� ���� �ƴ϶�� ���õ� �ٿ�ε带 ����,
    // �� �ٿ�ε尡 ���� ��ü�κ��� �뺸���� �� �յ��� ����
    if (!clearing) {
      selectedDownload =
        tableModel.getDownload(table.getSelectedRow());
      selectedDownload.addObserver(DownloadManager.this);
      updateButtons();
    }
  }

  // ���õ� �ٿ�ε� ����
  private void actionPause() {
    selectedDownload.pause();
    updateButtons();
  }

  // ���õ� �ٿ�ε� �簳
  private void actionResume() {
    selectedDownload.resume();
    updateButtons();
  }

  // ���õ� �ٿ�ε� ���
  private void actionCancel() {
    selectedDownload.cancel();
    updateButtons();
  }

  // ���õ� �ٿ�ε带 ����Ʈ���� ����
  private void actionClear() {
    clearing = true;
    tableModel.clearDownload(table.getSelectedRow());
    clearing = false;
    selectedDownload = null;
    updateButtons();
  }

  // ���� ���õ� �ٿ�ε��� ���¿� ����ؼ� �� ��ư�� ���¸� ������
  private void updateButtons() {
    if (selectedDownload != null) {
      int status = selectedDownload.getStatus();
      switch (status) {
        case Download.DOWNLOADING:
          pauseButton.setEnabled(true);
          resumeButton.setEnabled(false);
          cancelButton.setEnabled(true);
          clearButton.setEnabled(false);
          break;
        case Download.PAUSED:
          pauseButton.setEnabled(false);
          resumeButton.setEnabled(true);
          cancelButton.setEnabled(true);
          clearButton.setEnabled(false);
          break;
        case Download.ERROR:
          pauseButton.setEnabled(false);
          resumeButton.setEnabled(true);
          cancelButton.setEnabled(false);
          clearButton.setEnabled(true);
          break;
        default: // COMPLETE �Ǵ� CANCELLED
          pauseButton.setEnabled(false);
          resumeButton.setEnabled(false);
          cancelButton.setEnabled(false);
          clearButton.setEnabled(true);
      }
    } else {
      // ���̺� �ִ� ��� �ٿ�ε嵵 ���õ��� ���� ���
      pauseButton.setEnabled(false);
      resumeButton.setEnabled(false);
      cancelButton.setEnabled(false);
      clearButton.setEnabled(false);
    }
  }

  // Download�� ���� ��ȭ�� �Ͼ�� Download ��ü�� �����ڵ鿡�� �뺸�Ҵ� ȣ���
  public void update(Observable o, Object arg) {
    // Update buttons if the selected download has changed.
    if (selectedDownload != null && selectedDownload.equals(o))
      updateButtons();
  }

  // �ٿ�ε� �Ŵ����� �����Ŵ
  public static void main(String[] args) {
    DownloadManager manager = new DownloadManager();
    manager.show();
  }
}
