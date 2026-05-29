package tables;

import java.awt.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.*;

public class JtableRenderer extends DefaultTableCellRenderer
{
	Color grey = new Color(230, 230, 230);
	Color white = Color.WHITE;
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (column == 6)
		{
			c.setBackground(new Color(200, 200, 200));
			return c;
		}
		c.setForeground(Color.BLACK);
		c.setBackground(row % 2 == 0 ? white: grey);
		return c;
	}
}
