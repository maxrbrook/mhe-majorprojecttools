package tables;

import javax.swing.table.TableColumn;
public class PurchasingTable extends BasicJtable
{
	protected static final Object[] COLUMN_HEADINGS = {"*", "LEVEL", "ITEM NUMBER", "DESCRIPTION", "ITEM TYPE", "ORDER TYPE", "VAULT QTY", "TO ORDER QTY", "NOTES", "VAULT URL", "ISSUE"};
	protected static final boolean[] COLUMN_EDITABLE = {false, false, false, true, true, true, false, true, true, true, false};

	public PurchasingTable()
	{
		super(COLUMN_HEADINGS, COLUMN_EDITABLE);
		super.setFillsViewportHeight(true);
		super.setDefaultRenderer(Object.class, new PurchaseRenderer());

		String[] dropdown = {"", "MATERIAL LENGTH", "HYDRAULIC PART", "ELECTRICAL PART"};
		super.NewDropdown(dropdown, "ITEM TYPE");

		String[] dropdown2 = {"TO ORDER", "DO NOT ORDER"};
		super.NewDropdown(dropdown2, "ORDER TYPE");
		this.ColumnWidths();
	}

	private void ColumnWidths()
	{
		int[] min_width = {45, 90, 120, 0, 140, 140, 90, 90, 0, 0, 45};
		int[] max_width = {50, 90, 120, 0, 180, 180, 90, 90, 0, 0, 50};
		super.SetColumnWidths(min_width, max_width);
	}
}
