package tables;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.DefaultCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.DefaultTableModel;
import java.awt.Font;

/**
	Handles all Operations of the Jtables
**/

public class BasicJtable extends JTable
{
	private DefaultTableModel table_data;

	public BasicJtable(Object[] data, boolean[] cols)
	{
		super();
		table_data = new DefaultTableModel(data, 0)
		{
			@Override
			public boolean isCellEditable(int row, int column)
			{
				if (cols.length == 0)
				{
					return true;//all cells are editable
				}
				//TODO: Handle undefined behaviour
				return cols[column];
			}
		};
		super.setModel(table_data);
		super.setDefaultRenderer(Object.class, new JtableRenderer());
		super.getTableHeader().setReorderingAllowed(false);
	}

	/** Getter & Setter **/
	public void SetValue(Object val, int row, int column)
	{
		table_data.setValueAt(val, row, column);
	}
	public String GetValue(int row, int column)
	{
		return table_data.getValueAt(row, column).toString();
	}

	public void ClearData()
	{
		table_data.setRowCount(0);
	}

	public int RowCount()
	{
		return table_data.getRowCount();
	}

	public void AddRow(String[] data)
	{
		table_data.addRow(data);
	}

	protected int CountOccurance(String item, char ch)
	{
		char[] arr = item.toCharArray();
		int rtn = 0;
		for (int i = 0; i < arr.length; i++)
		{
			if (arr[i] == ch)
			{
				rtn++;
			}
		}
		return rtn;
	}

	//Updates a whole tables column by multiplying an input number by the value in a column
	//the result is written into res_column
	public void TableMultIntPos(int input, int column1, int res_column)
	{
		int column1_val = 0;
		int result = 0;
		int rows = this.getRowCount();

		if (rows == 0)
		{
			return;
		}
		for (int i = 0; i < rows; i++)
		{
			try
			{
				column1_val = Integer.parseInt(GetValue(i, column1));
			}
			catch (NumberFormatException err)
			{
				this.SetValue(0, i, res_column);
			}
			result = input * column1_val;

			this.SetValue(result, i, res_column);
		}
	}

	/** Dropdown option creator **/
	public void NewDropdown(String[] dropdown_options, String column_header)
	{
		JComboBox<String> combo_box = new JComboBox<>(dropdown_options);
		TableColumn column = super.getColumn(column_header);
		column.setCellEditor(new DefaultCellEditor(combo_box));
	}

	/** Zooming in/out text **/
	public void ZoomIn()
	{
		int i = 2;
		int font_size = super.getFont().getSize() + i;
		int row_size = super.getRowHeight() + i;
		if (font_size > 48)
		{
			return;
		}
		super.setFont(new Font("Dialog", Font.PLAIN, font_size));
		super.setRowHeight(row_size);
	}

	public void ZoomOut()
	{
		int i = 2;
		int font_size = super.getFont().getSize() - i;
		int row_size = super.getRowHeight() - i;
		if (font_size < 4)
		{
			return;
		}
		super.setFont(new Font("Dialog", Font.PLAIN, font_size));
		super.setRowHeight(row_size);
	}

	public void ResetZoom()
	{
		super.setFont(new Font("Dialog", Font.PLAIN, 12));
		super.setRowHeight(16);
	}

	public void DisableTable()
	{
		super.setEnabled(false);
	}
	public void EnableTable()
	{
		super.setEnabled(true);
	}

	protected void SetColumnWidths(int[] min, int[] max)
	{
		int column_count = super.getColumnModel().getColumnCount();
		for (int i = 0; i < column_count; i++)
		{
			TableColumn column = super.getColumnModel().getColumn(i);
			if (i > min.length || i > max.length)
			{
				return;
			}
			if (min[i] != 0)
			{
				column.setMinWidth(min[i]);
			}
			if (max[i] != 0)
			{
				column.setMaxWidth(max[i]);
			}
		}
	}
}
