import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.filechooser.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.*;

public class ImportBomFile extends AbstractAction
{
	boolean plates_bool;
	int spinner;
	JLabel file_path_label;
	JtableAssembly assembly_table;
	JtablePurchasing purchasing_table;

	public ImportBomFile(boolean bool, int num, JLabel label, JtableAssembly assy, JtablePurchasing purchase)
	{
		super("Import Vault BOM");
		this.plates_bool = bool;
		this.spinner = num;
		this.file_path_label = label;
		this.assembly_table = assy;
		this.purchasing_table = purchase;

	}
	public void actionPerformed(ActionEvent e)
	{
		JFileChooser file_to_open = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		file_to_open.setFileFilter(new FileNameExtensionFilter("Text Files", "txt", "TXT"));
		if(file_to_open.showOpenDialog(null) == file_to_open.APPROVE_OPTION)
		{
			File file_selected = file_to_open.getSelectedFile();
			boolean import_success = BeginImport(file_selected, plates_bool, spinner);
			if(import_success)
			{
				file_path_label.setText(file_selected.getAbsolutePath());
				return;
			}
			JFrame frame = new JFrame();
			frame.setAlwaysOnTop(true);
			JOptionPane.showMessageDialog(frame, "ERROR - Invalid file, Please check that the Item Master export is correct", "ERROR", JOptionPane.ERROR_MESSAGE);
			file_path_label.setText("ERROR - Invalid File");
			return;
		}
		int row_count = assembly_table.AssyRowCount();
		if(row_count == 0)
		{
			file_path_label.setText("No File Selected");
		}
	}

	private static String SetAssyLevel(String item_level, String description)
	{
		char[] dots;
		int item_depth = 0;

		if(item_level.equals("1"))
		{
			return "Assy";
		}
		if(item_level.matches("1(\\.\\d+){1}"))
		{
			return "Sub Assy";
		}
		if (description.contains(" PLATE"))
		{
			return "Plate Part";
		}
		dots = item_level.toCharArray();
		for(char ch : dots)
		{
			if(ch == '.')
			{
				item_depth++;
			}
		}
		return ("Assy Level " + (item_depth - 1));
	}

	private static String SetOrderType(String description, boolean plates_as_to_make)
	{
		if (plates_as_to_make)
		{
			return ("TO MAKE");
		}
		if (description.contains(" PLATE"))
		{
			return ("ORDER/OUTSOURCE");
		}
		return ("TO MAKE");
	}

	private static String SetPurchaseType(String[] arr_WordsArray)
	{
		if (arr_WordsArray[3].contains("HYD ") || arr_WordsArray[3].contains("HYDRAULIC"))
		{
			return ("HYDRAULIC");
		}
		if (arr_WordsArray[3].contains("ELEC"))
		{
			return ("ELECTRICAL");
		}
		return ("");
	}

	private static String SetQty(String number, int val)
	{
		int num = 1;

		if(number == "")
		{
			number = "1";
		}

		try
		{
			num = Integer.parseInt(number);
		}
		catch (NumberFormatException e)
		{
			return "1";
		}
		return ((num * val) + "");
	}

	private boolean BeginImport(File file_to_import, boolean plates_as_to_make, int spinner_val)
	{
		String[] assy_data = new String[11];
		String[] purchase_data = new String[8];
		assembly_table.ClearData();
		purchasing_table.ClearData();

		try
		{
			boolean check_file_layout = false;
			BufferedReader buffer = new BufferedReader(new FileReader(file_to_import));//read the file
			String read_line = null;
			String[] seperated_line;
			String regex = "([^-\n\rPL]{0,1})(\\d{1,})((-{0,1})(\\d{1,}))*";
			while (true)
			{
				read_line = buffer.readLine();
				if (read_line == null)
				{
					break;
				}
				seperated_line = read_line.split("\t");
				if (!check_file_layout)
				{
					if (seperated_line[0].contains("Level"))
					{
						check_file_layout = true;
						continue;
					}
					throw new Exception();
				}
				if (seperated_line[2].matches(regex))
				{
					assy_data[0] = seperated_line[0];//Level
					assy_data[1] = seperated_line[2];//Drawing #
					assy_data[2] = seperated_line[3];//Description
					assy_data[3] = SetQty(seperated_line[6], 1);
					assy_data[4] = SetQty(seperated_line[6], spinner_val);//TOTAL
					assy_data[5] = SetOrderType(seperated_line[3], plates_as_to_make);//Order Type
					assy_data[6] = "";//EST. HOURS
					assy_data[7] = "";//NOTES
					assy_data[8] = "";//VAULT URL
					assy_data[9] = SetAssyLevel(seperated_line[0], seperated_line[3]);//Assy Level
					assy_data[10] = seperated_line[8];//Issue
					assembly_table.GetAssyTableData().addRow(assy_data);
					continue;
				}
				purchase_data[0] = seperated_line[0];//Level
				purchase_data[1] = seperated_line[2];//Part #
				purchase_data[2] = seperated_line[3];//Description
				purchase_data[3] = SetQty(seperated_line[6], 1);
				purchase_data[4] = SetQty(seperated_line[6], spinner_val);//TOTAL
				purchase_data[5] = SetPurchaseType(seperated_line);//Purchase Type - TO REMOVE ON IMPLEMENTATION OF TYPE FIELD
				purchase_data[6] = "";//Notes
				purchase_data[7] = seperated_line[8];//Issue
				purchasing_table.GetPurchaseTableData().addRow(purchase_data);
			}
			buffer.close();//close the buffer on success
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			assembly_table.ClearData();
			purchasing_table.ClearData();
			return false;
		}
	}
}
