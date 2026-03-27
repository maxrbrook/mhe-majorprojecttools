import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

public class DefaultAssyRenderer extends DefaultTableCellRenderer
{
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		String sub_assy = table.getValueAt(row, 0) + "";

		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		Color grey = new Color(230, 230, 230);
		Color white = Color.WHITE;
		Color blue = new Color(100, 100, 230);
		if (sub_assy.matches("1(\\.\\d+){1}"))
		{
			c.setBackground(blue);
			c.setForeground(white);
			return c;
		}
		c.setForeground(Color.BLACK);
		c.setBackground(row % 2 == 0 ? white: grey);
		return c;
	}
}
