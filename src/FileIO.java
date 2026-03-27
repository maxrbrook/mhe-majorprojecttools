import java.io.*;

public class FileIO
{
	public static void SaveProjectToFile(ProjectTab new_project_tab, JtableAssembly assembly_table, JtablePurchasing purchasing_table, File file)
	{
		String str_ProjectTitle = new_project_tab.ReturnProjectTitle();
		String str_JobNumber = new_project_tab.ReturnJobNumber();
		String str_Client = new_project_tab.ReturnClient();
		String str_SalesOrder = new_project_tab.ReturnSalesOrder();
		String str_CustomerOrder = new_project_tab.ReturnCustomerOrder();
		String description = new_project_tab.ReturnDescription();
		int spinner_val = new_project_tab.ReturnSpinnerValue();
		try
		{
			Writer new_file  = new FileWriter(file, false);
			int assy_rows = assembly_table.AssyRowCount();
			int purchase_rows = purchasing_table.PurchaseRowCount();

			new_file.write(str_ProjectTitle + "\t" + str_JobNumber+ "\t" + str_Client + "\t");//Line 0
			new_file.write(str_SalesOrder + "\t" + str_CustomerOrder + "\t" + assy_rows + "\t" + purchase_rows + "\t" + spinner_val + "\n");
			int int_AssyColumnCnt = assembly_table.GetAssyTableData().getColumnCount();
			int int_PurchaseColumnCnt = purchasing_table.GetPurchaseTableData().getColumnCount();
			String str_Line = "";
			for (int i = 0; i < assy_rows; i++)
			{
				str_Line = "";
				for(int j = 0; j < int_AssyColumnCnt; j++)
				{
					str_Line = str_Line + assembly_table.AssyValueAt(i, j) + "\t";
				}
				new_file.write(str_Line + "\n");
			}
			for (int k = 0; k < purchase_rows; k++)
			{
				str_Line = "";
				for (int l = 0; l < int_PurchaseColumnCnt; l++)
				{
					str_Line = str_Line + purchasing_table.PurchaseValueAt(k, l) + "\t";
				}
				new_file.write(str_Line + "\n");
			}
			new_file.write(description);
			new_file.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return;
	}
	public static void OpenProjectFile(ProjectTab new_project_tab, JtableAssembly assembly_table, JtablePurchasing purchasing_table, File file)
	{
		assembly_table.ClearData();
		purchasing_table.ClearData();
		try
		{
			boolean project_headings_written = false;
			boolean table_size_is_valid = false;
			BufferedReader buffer = new BufferedReader(new FileReader(file));//read the file
			String read_line = new String();
			String[] seperated_line;
			String regex = "([^-\n\rPL]{0,1})(\\d{1,})((-{0,1})(\\d{1,}))*";
			String description = new String();
			int assy_rows = 0;
			int purchase_rows = 0;
			int i = 0;
			int j = 0;

			while (true)
			{
				read_line = buffer.readLine();
				if (read_line == null)
				{
					break;
				}
				seperated_line = read_line.split("\t");
				if (!project_headings_written)
				{
					project_headings_written = true;
					if (seperated_line.length < 5)
					{
						continue;
					}
					//write the assy_rows and purchase rows
					//if the assy_rows and purchase are in the file,
					//use them instead of regex - mostly just for speed reasons.
					//keeping regex in just in case.
					new_project_tab.SetProjetTitle(seperated_line[0]);
					new_project_tab.SetJobNumber(seperated_line[1]);
					new_project_tab.SetClient(seperated_line[2]);
					new_project_tab.SetSalesOrder(seperated_line[3]);
					new_project_tab.SetCustomerOrder(seperated_line[4]);
					project_headings_written = true;

					if (seperated_line.length == 8)
					{
						try
						{
							assy_rows = Integer.parseInt(seperated_line[5]);
							purchase_rows = Integer.parseInt(seperated_line[6]);
							new_project_tab.SetSpinnerValue(seperated_line[7]);
							table_size_is_valid = true;
						}
						catch (NumberFormatException e)
						{
							table_size_is_valid = false;
						}
					}
					continue;
				}
				if (!table_size_is_valid)
				{
					if (seperated_line[1].matches(regex))
					{
						assembly_table.GetAssyTableData().addRow(seperated_line);
					}
					else
					{
						purchasing_table.GetPurchaseTableData().addRow(seperated_line);
					}
				}
				else
				{
					if (i++ < assy_rows)
					{
						assembly_table.GetAssyTableData().addRow(seperated_line);
						continue;
					}
					if (j++ < purchase_rows)
					{
						purchasing_table.GetPurchaseTableData().addRow(seperated_line);
						continue;
					}
				}
				if (seperated_line.length < 8)
				{
					for(int k = 0; k < seperated_line.length; k++)
					{
						description += seperated_line[k];
					}
					continue;
				}
			}
			new_project_tab.SetDescription(description);
			buffer.close();//close the buffer on success
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return;
	}
}
