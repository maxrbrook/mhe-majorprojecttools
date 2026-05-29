package tables;

import javax.swing.*;
import java.awt.*;
public class AssyRenderer extends JtableRenderer
{
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		int level_column = table.getColumn("LEVEL").getModelIndex();
		String item1 = table.getValueAt(row, level_column) + "";
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		Color l_red = new Color(150, 50, 50);
		if (item1.matches("1(\\.\\d+){1}"))
		{
			c.setBackground(l_red);
			c.setForeground(super.white);
			return c;
		}
		return c;
	}
}
