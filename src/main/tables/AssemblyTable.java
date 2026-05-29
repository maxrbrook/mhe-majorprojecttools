package tables;

public class AssemblyTable extends BasicJtable
{
	protected static final Object[] COLUMN_HEADINGS = {"*", "LEVEL", "ITEM NUMBER", "DESCRIPTION", "ITEM TYPE", "ORDER TYPE", "VAULT QTY", "TO MAKE QTY", "NOTES", "VAULT URL", "ISSUE"};
	protected static final boolean[] COLUMN_EDITABLE = {false, false, false, true, true, true, false, true, true, true, false};
	/*
	0: *
	1: LEVEL
	2: ITEM NUMBER
	3: DESC
	4: ITEM TYPE
	5: ORDER TYPE
	6: VAULT QTY
	7: TO MAKE QTY
	8: NOTES
	9: VAULT URL
	10: ISSUE
	*/

	public AssemblyTable()
	{
		super(COLUMN_HEADINGS, COLUMN_EDITABLE);
		super.setFillsViewportHeight(true);
		super.setDefaultRenderer(Object.class, new AssyRenderer());
		String[] dropdown = {"", "PLATE PART"};
		super.NewDropdown(dropdown, "ITEM TYPE");

		String[] dropdown2 = {"TO MAKE", "TO ORDER/OUTSOURCE", "DO NOT MAKE"};
		super.NewDropdown(dropdown2, "ORDER TYPE");

		this.ColumnWidths();
	}
	/** String Getters for creating item names and decriptions **/
	public String ItemTitle(int i)
	{
		return String.format("%s: %s",this.GetItemNumber(i), this.GetDescription(i));
	}

	public String ItemDetails(int i)
	{
		String s = String.format("<br>Drawing Number: %s Issue: [%s] Vault URL: %s<br>QTY: %s<br>QTY TO MAKE: %s<br>Notes:<br>%s",
		this.GetItemNumber(i), this.GetIssue(i), this.GetVaultUrl(i), this.GetVaultQty(i), this.GetMakeQty(i), this.GetNotes(i));

		return (s);
	}

	public String ItemsRelatedBom(int i)
	{
		String s = new String();
		String assy_heading = "<br><u>ELEMENTS OF SUB ASSEMBLY:</u><br>";
		String plates_heading = "<br><u>PLATE ITEMS:</u><br>";
		String assy = new String();
		String plates = new String();
		String vault_level = new String();
		String item_type = new String();

		String task_level = String.format("(%s)(\\.\\d+){1}", GetLevel(i));//1.n(1).n(2)...n(n)

		for(int j = (i + 1); j < super.RowCount(); j++)
		{
			vault_level = GetLevel(j);
			/*if(vault_level.matches("1(\\.\\d+){1}"))
			{
				//if vault level matches 1.xx
				break;
			}*/
			if(vault_level.matches(task_level))
			{
				//if vault level matches 1.xx.yy
				item_type = this.GetItemType(j);
				s = String.format("&emsp;<b> %s [%s][QTY: %s]:</b> %s<br>",
				this.GetItemNumber(j), item_type, this.GetVaultQty(j), this.GetDescription(j));

				if(item_type.equals("PLATE PART"))
				{
					plates += s;
					continue;
				}
				assy += s;
			}
		}
		if (assy.length() == 0 && plates.length() == 0)
		{
			return ("");
		}
		if (assy.length() != 0)
		{
			assy = assy_heading + assy;
		}
		if (plates.length() != 0)
		{
			plates = plates_heading + plates;
		}
		return(assy + plates);
	}

	public String ItemDesc(int i)
	{
		return (ItemDetails(i) + ItemsRelatedBom(i));
	}

	public int CountLevel(int i)
	{
		return super.CountOccurance(super.GetValue(i, 1), '.');
	}

	public boolean ToMakeItem(int i)
	{
		return this.GetOrderType(i).equals("TO MAKE");
	}

	/** Getters **/
	public String GetRowNumber(int i)
	{
		return super.GetValue(i, 0);
	}
	public String GetLevel(int i)
	{
		return super.GetValue(i, 1);
	}
	public String GetItemNumber(int i)
	{
		return super.GetValue(i, 2);
	}
	public String GetDescription(int i)
	{
		return super.GetValue(i, 3);
	}
	public String GetItemType(int i)
	{
		return super.GetValue(i, 4);
	}
	public String GetOrderType(int i)
	{
		return super.GetValue(i, 5);
	}
	public String GetVaultQty(int i)
	{
		return super.GetValue(i, 6);
	}
	public String GetMakeQty(int i)
	{
		return super.GetValue(i, 7);
	}
	public String GetNotes(int i)
	{
		return super.GetValue(i, 8);
	}
	public String GetVaultUrl(int i)
	{
		return super.GetValue(i, 9);
	}
	public String GetIssue(int i)
	{
		return super.GetValue(i, 10);
	}

	private void ColumnWidths()
	{
		int[] min_width = {45, 90, 120, 0, 140, 140, 90, 90, 0, 0, 45};
		int[] max_width = {50, 90, 120, 0, 180, 180, 90, 90, 0, 0, 50};
		super.SetColumnWidths(min_width, max_width);
	}
}
