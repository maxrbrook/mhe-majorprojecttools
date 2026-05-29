package btnfunctions;

import tables.AssemblyTable;
import tables.PurchasingTable;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import javax.swing.filechooser.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.*;

public class BtnBomImport
{
	boolean plate_make_bool;
	JSpinner spinner;
	JLabel file_dir_lbl;
	AssemblyTable ASSY_TABLE;
	PurchasingTable PURCHASING_TABLE;

	public BtnBomImport(boolean bool, JSpinner spinner, JLabel lbl, AssemblyTable a, PurchasingTable p)
	{
		this.plate_make_bool = bool;
		this.spinner = spinner;
		this.file_dir_lbl = lbl;
		this.ASSY_TABLE = a;
		this.PURCHASING_TABLE = p;
		this.StartImport();

	}
	private String AssyItemType(String desc)
	{
		//checks for plate parts
		if (desc.contains(" PLATE"))
		{
			return ("PLATE PART");
		}
		return ("");
	}
	private String PurchaseItemType(String desc)
	{
		//checks for hydraulic or electric parts
		if (desc.matches("(.)+( - )([\\d.]){1,}( mm)"))
		{
			return ("MATERIAL LENGTH");
		}
		if (desc.contains("HYD ") || desc.contains("HYDRAULIC"))
		{
			return ("HYDRAULIC PART");
		}
		if (desc.contains("ELEC"))
		{
			return ("ELECTRICAL PART");
		}
		return ("");
	}
	private String AssyOrderType(String desc)
	{
		if (plate_make_bool)
		{
			return ("TO MAKE");
		}
		if (desc.contains(" PLATE"))
		{
			return ("TO ORDER/OUTSOURCE");
		}
		return ("TO MAKE");
	}

	private String SetQty(String number, int val)
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
	private boolean CheckFileFormat(String[] seperated_line) throws Exception
	{
		System.out.println(seperated_line[0]);
		if(!seperated_line[0].contains("Level"))
		{
			throw new Exception("File Format Error");
		}
		return true;
	}
	private boolean BeginImport(File file_to_import)
	{
		String[] data = new String[11];
		ASSY_TABLE.ClearData();
		PURCHASING_TABLE.ClearData();
		int a_count = 1;
		int p_count = 1;
		boolean regex_bool = false;

		System.out.println("importing...");

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
					check_file_layout = CheckFileFormat(seperated_line);
					continue;
				}
				//match the regex to use for sorting the data into the table.
				regex_bool = seperated_line[2].matches(regex);

				data[0] = regex_bool ? String.valueOf(a_count++) : String.valueOf(p_count++);
				data[1] = seperated_line[0];//Level
				data[2] = seperated_line[2];//Item Number
				data[3] = seperated_line[3];//Description
				data[4] = regex_bool ? AssyItemType(seperated_line[3]) : PurchaseItemType(seperated_line[3]);//Item Type
				data[5] = regex_bool ? AssyOrderType(seperated_line[3]) : "TO ORDER";//Order Type
				data[6] = SetQty(seperated_line[6], 1);
				data[7] = SetQty(seperated_line[6], (Integer) spinner.getValue());//TOTAL
				data[8] = "";//NOTES
				data[9] = "";//vault url
				data[10] = seperated_line[8];//Issue

				if (regex_bool)
				{
					ASSY_TABLE.AddRow(data);
					continue;
				}
				PURCHASING_TABLE.AddRow(data);
			}
			buffer.close();//close the buffer on success
		}
		catch(Exception e)
		{
			ASSY_TABLE.ClearData();
			PURCHASING_TABLE.ClearData();
			return false;
		}
		System.out.println("importing complete");
		return true;
	}

	private void ImportError()
	{
		JFrame frame = new JFrame();
		frame.setAlwaysOnTop(true);
		JOptionPane.showMessageDialog(frame, "ERROR - Invalid file, Please check that the Item Master export is correct", "ERROR", JOptionPane.ERROR_MESSAGE);
		file_dir_lbl.setText("ERROR - Invalid File");
	}

	private void StartImport()
	{
		JFileChooser fc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		fc.setFileFilter(new FileNameExtensionFilter("Text Files", "txt", "TXT"));
		if(fc.showOpenDialog(null) == fc.APPROVE_OPTION)
		{
			File f = fc.getSelectedFile();
			boolean import_success = BeginImport(f);
			if(import_success)
			{
				file_dir_lbl.setText(f.getPath());
				return;
			}
			ImportError();
			return;
		}
	}
}
