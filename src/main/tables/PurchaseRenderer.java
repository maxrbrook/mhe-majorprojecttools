package tables;

import javax.swing.*;
import java.awt.*;
public class PurchaseRenderer extends JtableRenderer
{
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		int level_column = table.getColumn("ITEM TYPE").getModelIndex();
		String item1 = table.getValueAt(row, level_column) + "";
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		Color l_red = new Color(50, 50, 150);
		Color l_green = new Color(50, 150, 50);
		if (item1.equals("HYDRAULIC PART"))
		{
			c.setBackground(l_red);
			c.setForeground(super.white);
			return c;
		}
		if (item1.equals("ELECTRICAL PART"))
		{
			c.setBackground(l_green);
			c.setForeground(super.white);
			return c;
		}
		return c;
	}
}
