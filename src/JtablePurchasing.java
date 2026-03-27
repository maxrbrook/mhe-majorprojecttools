import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.util.*;

public class JtablePurchasing
{
	private DefaultTableModel purchase_table_data;
	private JTable purchase_jtable;

	private final Object[] PURCHASE_COLUMN_HEADINGS = {"LEVEL", "PART #", "DESCRIPTION", "VAULT QTY", "TO ORDER QTY", "PURCHASE TYPE", "NOTES", "ISSUE"};

	public DefaultTableModel GetPurchaseTableData()
	{
		return purchase_table_data;
	}

	public void SetPurchaseVal(Object val, int row, int column)
	{
		GetPurchaseTableData().setValueAt(val, row, column);
	}

	public String PurchaseValueAt(int row, int column)
	{
		return GetPurchaseTableData().getValueAt(row, column).toString();
	}

	public int PurchaseRowCount()
	{
		return purchase_table_data.getRowCount();
	}

	public void Disable()
	{
		purchase_jtable.setEnabled(false);
	}

	public void Enable()
	{
		purchase_jtable.setEnabled(true);
	}

	public void IncreaseFont()
	{
		int i = 2;
		int font_size = purchase_jtable.getFont().getSize();
		int row_size = purchase_jtable.getRowHeight();
		if ((font_size + i) > 48 || (font_size + i) > 48)
		{
			return;
		}
		purchase_jtable.setFont(new Font("Dialog", Font.PLAIN, font_size + i));
		purchase_jtable.setRowHeight(row_size + i);
	}

	public void DecreaseFont()
	{
		int i = 2;
		int font_size = purchase_jtable.getFont().getSize();
		int row_size = purchase_jtable.getRowHeight();
		if ((font_size - i) < 4 || (font_size - i) < 4)
		{
			return;
		}
		purchase_jtable.setFont(new Font("Dialog", Font.PLAIN, font_size - i));
		purchase_jtable.setRowHeight(row_size - i);
	}

	public void ResetFont()
	{
		purchase_jtable.setFont(new Font("Dialog", Font.PLAIN, 12));
		purchase_jtable.setRowHeight(16);
	}

	public void ClearData()
	{
		int purchase_rows = PurchaseRowCount();
		if(purchase_rows > 0)
		{
			for(int i = 0; i < purchase_rows; i++)
			{
				GetPurchaseTableData().removeRow((purchase_rows - 1) - i);
			}
		}
	}

	private void SetPurchaseTableColumnSize()
	{
		int column_count = purchase_jtable.getColumnModel().getColumnCount();
		TableColumn column = null;
		for(int i = 0; i < column_count; i++)
		{
			column = purchase_jtable.getColumnModel().getColumn(i);
			if(i == 0 || i == 1)
			{
				column.setPreferredWidth(50);
			}
			if(i == 2)
			{
				column.setPreferredWidth(250);
			}
			if(i == 3 || i == 4)
			{
				column.setPreferredWidth(20);
			}
			if(i == 5 || i == 6 || i == 7)
			{
				column.setPreferredWidth(15);
			}
		}
	}

	private void CreatePurchaseTypeDropdown()
	{
		JComboBox<String> combo_box = new JComboBox<>(new String[]{"", "DO NOT ORDER", "HYDRAULIC", "ELECTRICAL", "MATERIAL"});
		TableColumn column_table = purchase_jtable.getColumn("PURCHASE TYPE");
		column_table.setCellEditor(new DefaultCellEditor(combo_box));
	}

	private void SetPurchaseIntegersChecks()
	{
		TableColumn column_table = purchase_jtable.getColumn("TO ORDER QTY");
		column_table.setCellEditor(new IntegerCellEditor());
		column_table = purchase_jtable.getColumn("ISSUE");
		column_table.setCellEditor(new IntegerCellEditor());
	}

	public void UpdateJTableOrdering(String input)
	{
		int vault_qty_column = 3;//Vault Qty is located on Column 3
		int to_make_column = 4; //To Order/To Make is located at Column 4

		int purchase_rows = PurchaseRowCount();

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

		if(purchase_rows > 0)
		{
			for (int i = 0; i < purchase_rows; i++)
			{
				try
				{
					vault_qty = Integer.parseInt(PurchaseValueAt(i, vault_qty_column));
					make_qty = Integer.parseInt(PurchaseValueAt(i, to_make_column));
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
				SetPurchaseVal(make_qty, i, to_make_column);
			}
		}
	}

	public JTable CreatePurchasingTable()
	{
		purchase_table_data = new DefaultTableModel(PURCHASE_COLUMN_HEADINGS, 0)
		{
			@Override
			public boolean isCellEditable(int row, int column)
			{
				return column != 3;
			};
		};
		purchase_jtable = new JTable(purchase_table_data);
		purchase_jtable.getTableHeader().setReorderingAllowed(false);
		purchase_jtable.setDefaultRenderer(Object.class, new DefaultPurchaseRenderer());

		SetPurchaseTableColumnSize();
		CreatePurchaseTypeDropdown();
		SetPurchaseIntegersChecks();
		purchase_jtable.setFillsViewportHeight(true);

		return (purchase_jtable);
	}
}
