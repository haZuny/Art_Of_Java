package emailClient;

import java.util.*;
import javax.mail.*;
import javax.swing.*;
import javax.swing.table.*;

// �̸��� ���̺��� �����͸� �����ϴ� Ŭ����
public class MessagesTableModel extends AbstractTableModel
{
  // ���̺� �� �̸�
  private static final String[] columnNames = {"Sender",
    "Subject", "Date"};

  // �޽��� ����Ʈ
  private ArrayList messageList = new ArrayList();

  // ���̺� �޽������� �߰�
  public void setMessages(Message[] messages) {
    for (int i = messages.length - 1; i >= 0; i--) {
      messageList.add(messages[i]);
    }

    // ���̺��� �����Ϳ� ��ȭ�� ������ �˸��� �̺�Ʈ �߻�
    fireTableDataChanged();
  }

  // Ư�� �࿡ �ش��ϴ� �޽����� ����
  public Message getMessage(int row) {
    return (Message) messageList.get(row);
  }

  // �޽��� ����Ʈ���� �ش� �޽����� ����
  public void deleteMessage(int row) {
    messageList.remove(row);

    // ���̺��� Ư�� ���� �����Ǿ����� �˸��� �̺�Ʈ �߻�
    fireTableRowsDeleted(row, row);
  }

  // ���̺� �� ������ ����
  public int getColumnCount() {
    return columnNames.length;
  }

  // ���̺� �� �̸��� ����
  public String getColumnName(int col) {
     return columnNames[col];
  }

  // ���̺� �� ������ ����
  public int getRowCount() {
    return messageList.size();
  }

  // ���̺��� Ư�� ���� �ش��ϴ� ���� ����
  public Object getValueAt(int row, int col) {
    try {
      Message message = (Message) messageList.get(row);
      switch (col) {
        case 0: // Sender
          Address[] senders = message.getFrom();
          if (senders != null || senders.length > 0) {
            return senders[0].toString();
          } else {
            return "[none]";
          }
        case 1: // Subject
          String subject = message.getSubject();
          if (subject != null && subject.length() > 0) {
            return subject;
          } else {
            return "[none]";
          }
        case 2: // Date
          Date date = message.getSentDate();
          if (date != null) {
            return date.toString();
          } else {
            return "[none]";
          }
      }
    } catch (Exception e) {
      // ���� �߻�
      return "";
    }

    return "";
  }
}
