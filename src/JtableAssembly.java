import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.util.*;

public class JtableAssembly
{
	private DefaultTableModel assy_table_data;
	private JTable assy_jtable;
	private final Object[] ASSY_COLUMN_HEADINGS = {"LEVEL", "DRAWING #", "DESCRIPTION", "VAULT QTY", "TO MAKE QTY", "ORDER TYPE", "EST. HOURS", "NOTES", "VAULT URL", "ASSY LEVEL", "ISSUE"};

	/** Private Methods */
	private void SetAssyTableColumnSize()
	{
		int column_count = assy_jtable.getColumnModel().getColumnCount();
		TableColumn column = null;
		for (int i = 0; i < column_count; i++)
		{
			column = assy_jtable.getColumnModel().getColumn(i);
			if (i == 0 || i == 1)// Level and Drawing #
			{
				column.setPreferredWidth(50);
			}
			if (i == 2 || i == 7)// Order and Notes
			{
				column.setPreferredWidth(300);
			}
			if (i == 3 || i == 4 || i == 6)
			{
				column.setPreferredWidth(20);
			}
			if (i == 5)
			{
				column.setPreferredWidth(50);
			}
			else
			{
				column.setPreferredWidth(15);
			}
		}
	}

	private void CreateOrderTypeDropdown()
	{
		JComboBox<String> combo_box = new JComboBox<>(new String[]{"TO MAKE", "ORDER/OUTSOURCE", "DO NOT MAKE"});
		TableColumn column_table = assy_jtable.getColumn("ORDER TYPE");
		column_table.setCellEditor(new DefaultCellEditor(combo_box));
	}

	private void SetAssyIntegersChecks()
	{
		TableColumn column_table = assy_jtable.getColumn("TO MAKE QTY");
		column_table.setCellEditor(new IntegerCellEditor());
		column_table = assy_jtable.getColumn("ISSUE");
		column_table.setCellEditor(new IntegerCellEditor());
		column_table = assy_jtable.getColumn("EST. HOURS");
		column_table.setCellEditor(new IntegerCellEditor());
	}

	/** Public Methods */
	public DefaultTableModel GetAssyTableData()
	{
		return assy_table_data;
	}

	public void Disable()
	{
		assy_jtable.setEnabled(false);
	}

	public void Enable()
	{
		assy_jtable.setEnabled(true);
	}

	public void SetAssyVal(Object val, int row, int column)
	{
		GetAssyTableData().setValueAt(val, row, column);
	}

	public String AssyValueAt(int row, int column)
	{
		return GetAssyTableData().getValueAt(row, column).toString();
	}

	public int AssyRowCount()
	{
		return assy_table_data.getRowCount();
	}

	public void IncreaseFont()
	{
		int i = 2;
		int font_size = assy_jtable.getFont().getSize();
		int row_size = assy_jtable.getRowHeight();
		if ((font_size + i) > 48 || (font_size + i) > 48)
		{
			return;
		}
		assy_jtable.setFont(new Font("Dialog", Font.PLAIN, font_size + i));
		assy_jtable.setRowHeight(row_size + i);
	}

	public void DecreaseFont()
	{
		int i = 2;
		int font_size = assy_jtable.getFont().getSize();
		int row_size = assy_jtable.getRowHeight();
		if ((font_size - i) < 4 || (font_size - i) < 4)
		{
			return;
		}
		assy_jtable.setFont(new Font("Dialog", Font.PLAIN, font_size - i));
		assy_jtable.setRowHeight(row_size - i);
	}

	public void ResetFont()
	{
		assy_jtable.setFont(new Font("Dialog", Font.PLAIN, 12));
		assy_jtable.setRowHeight(16);
	}

	public void ClearData()
	{
		int assy_rows = AssyRowCount();
		if(assy_rows > 0)
		{
			for(int i = 0; i < assy_rows; i++)
			{
				GetAssyTableData().removeRow((assy_rows - 1) - i);
			}
		}
	}
	public void UpdateJTableOrdering(String input)
	{
		int vault_qty_column = 3;//Vault Qty is located on Column 3
		int to_make_column = 4; //To Order/To Make is located at Column 4

		int assy_rows = AssyRowCount();

		int vault_qty = 0;
		int make_qty = 0;
		int init = 0;

		try
		{
			init = Integer.parseInt(input);
		}
		catch (NumberFormatException e)
		{
			return;
		}
		if(assy_rows > 0)
		{
			for (int i = 0; i < assy_rows; i++)
			{
				try
				{
					vault_qty = Integer.parseInt(AssyValueAt(i, vault_qty_column));
					//make_qty = Integer.parseInt(AssyValueAt(i, to_make_column));
				}
				catch (NumberFormatException e)
				{
					break;
				}
				make_qty = (init * vault_qty);
				if(make_qty < 0)
				{
					make_qty = 0;
				}
				SetAssyVal(make_qty, i, to_make_column);
			}
		}
	}

	public JTable CreateAssyTable()
	{
		assy_table_data = new DefaultTableModel(ASSY_COLUMN_HEADINGS, 0)
		{
			@Override
			public boolean isCellEditable(int row, int column)
			{
				return column != 3;
			};
		};
		assy_jtable = new JTable(assy_table_data);
		assy_jtable.getTableHeader().setReorderingAllowed(false);
		assy_jtable.setDefaultRenderer(Object.class, new DefaultAssyRenderer());

		CreateOrderTypeDropdown();
		SetAssyTableColumnSize();
		SetAssyIntegersChecks();
		assy_jtable.setFillsViewportHeight(true);

		return (assy_jtable);
	}
}
