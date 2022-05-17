package downloadManager;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

// ���̺� ���� JProgressBar ������
class ProgressRenderer extends JProgressBar
  implements TableCellRenderer
{
  // ������
  public ProgressRenderer(int min, int max) {
    super(min, max);
  }

  // ���̺��� ���� JProgressBar ��ü�� �������μ� ����
  public Component getTableCellRendererComponent(
    JTable table, Object value, boolean isSelected,
    boolean hasFocus, int row, int column)
  {
    // JProgressBar�� �Ϸ� ����� ���� ����
    setValue((int) ((Float) value).floatValue());
    return this;
  }
}
