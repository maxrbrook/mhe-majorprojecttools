package btnfunctions;

import tables.AssemblyTable;
import tables.PurchasingTable;
import projectclasses.ProjectTab;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

import javax.swing.JOptionPane;
import javax.swing.JSpinner;

import javax.swing.JLabel;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

public class BtnProjectOpen extends JFileChooser
{
	AssemblyTable ASSY_TABLE;
	PurchasingTable PURCHASING_TABLE;
	ProjectTab PROJECT_PANEL;
	JLabel FILE_DIR_LBL;

	public BtnProjectOpen(AssemblyTable a, PurchasingTable p, ProjectTab pt, JLabel lbl)
	{
		this.ASSY_TABLE = a;
		this.PURCHASING_TABLE = p;
		this.PROJECT_PANEL = pt;
		this.FILE_DIR_LBL = lbl;
		this.DialogOpen();
	}
	private void DialogOpen()
	{
		super.setCurrentDirectory(FileSystemView.getFileSystemView().getHomeDirectory());

		super.setFileFilter(new FileNameExtensionFilter("Tab Seperated Values", "tsv"));
		if(super.showOpenDialog(null) == super.APPROVE_OPTION)
		{
			this.OpenProject(super.getSelectedFile());
		}
		FILE_DIR_LBL.setText(super.getSelectedFile().getPath());
		return;
	}
	private void OpenProject(File file)
	{
		this.ASSY_TABLE.ClearData();
		this.PURCHASING_TABLE.ClearData();
		try
		{
			BufferedReader buffer = new BufferedReader(new FileReader(file));//read the file
			String[] seperated_line;
			String read_line = new String();
			int i = 0;
			int j = 0;
			int k = 0;
			int assy_rows = 0;
			int purchase_rows = 0;
			int spinner_val = 0;

			while (true)
			{
				read_line = buffer.readLine();
				if (read_line == null)
				{
					break;
				}
				seperated_line = read_line.split("\t");
				if (i == 0)
				{
					//Metadata line
					assy_rows = Integer.parseInt(seperated_line[0]);
					purchase_rows = Integer.parseInt(seperated_line[1]);
					spinner_val = Integer.parseInt(seperated_line[2]);
					this.PROJECT_PANEL.SetSpinnerVal(spinner_val);
					i++;
					continue;
				}
				if (i == 1)
				{
					//project Tab info
					this.PROJECT_PANEL.SetProjetTitle(seperated_line[0]);
					this.PROJECT_PANEL.SetJobNumber(seperated_line[1]);
					this.PROJECT_PANEL.SetClient(seperated_line[2]);
					i++;
					continue;
				}
				if (j < assy_rows)
				{
					this.ASSY_TABLE.AddRow(seperated_line);
					j++;
					continue;
				}
				if (k < purchase_rows)
				{
					this.PURCHASING_TABLE.AddRow(seperated_line);
					k++;
					continue;
				}
				//write desctiption
				this.PROJECT_PANEL.SetProjectDescription(read_line);
			}
		}
		catch (Exception err)
		{
			err.printStackTrace();
		}
	}
}
