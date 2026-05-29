package btnfunctions;

import tables.AssemblyTable;
import tables.PurchasingTable;

import java.io.File;
import java.io.Writer;
import java.io.FileWriter;

import javax.swing.JOptionPane;
import javax.swing.JSpinner;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import java.awt.event.ActionEvent;

public class BtnProjectSave extends JFileChooser
{
	@Override
	public void approveSelection()
	{
		File f = getSelectedFile();
		if(f.exists() && getDialogType() == SAVE_DIALOG)
		{
			int user = JOptionPane.showConfirmDialog(this, "This file exists, overwrite?", "Overwrite Existing File?", JOptionPane.YES_NO_CANCEL_OPTION);
			switch(user)
			{
				case JOptionPane.YES_OPTION:
					super.approveSelection();
					return;
				case JOptionPane.NO_OPTION:
					return;
				case JOptionPane.CLOSED_OPTION:
					cancelSelection();
					return;
				case JOptionPane.CANCEL_OPTION:
					cancelSelection();
					return;
			}
		}
		super.approveSelection();
	};


	JSpinner spinner;
	AssemblyTable ASSY_TABLE;
	PurchasingTable PURCHASING_TABLE;
	String[] PROJECT_DETAILS;

	public BtnProjectSave(AssemblyTable a, PurchasingTable p, String[] pd, JSpinner spinner)
	{
		this.spinner = spinner;
		this.ASSY_TABLE = a;
		this.PURCHASING_TABLE = p;
		this.PROJECT_DETAILS = pd;
		this.DialogSave();
	}
	private void DialogSave()
	{
		super.setCurrentDirectory(FileSystemView.getFileSystemView().getHomeDirectory());
		super.setSelectedFile(new File(this.PROJECT_DETAILS[0] + ".tsv"));

		super.setFileFilter(new FileNameExtensionFilter("Tab Seperated Values", "tsv"));
		if(super.showSaveDialog(null) == super.APPROVE_OPTION)
		{
			this.SaveProject(super.getSelectedFile());
		}
		return;
	}
	private void SaveProject(File file)
	{
		try
		{
			Writer new_file  = new FileWriter(file, false);
			int assy_rows = this.ASSY_TABLE.RowCount();
			int purchase_rows = this.PURCHASING_TABLE.RowCount();

			new_file.write(String.format("%s\t%s\t%s\n", assy_rows, purchase_rows, this.spinner.getValue()));// Line 0
			new_file.write(String.format("%s\t%s\t%s\n", this.PROJECT_DETAILS[0], this.PROJECT_DETAILS[1], this.PROJECT_DETAILS[2]));//Line 1
			int assy_column_total = this.ASSY_TABLE.getColumnCount();
			int purchase_column_total = this.PURCHASING_TABLE.getColumnCount();
			String line_writer = "";
			for (int i = 0; i < assy_rows; i++)
			{
				line_writer = "";
				for(int j = 0; j < assy_column_total; j++)
				{
					line_writer += this.ASSY_TABLE.GetValue(i, j) + "\t";
				}
				new_file.write(line_writer + "\n");
			}
			for (int k = 0; k < purchase_rows; k++)
			{
				line_writer = "";
				for (int l = 0; l < purchase_column_total; l++)
				{
					line_writer += this.ASSY_TABLE.GetValue(k, l) + "\t";
				}
				new_file.write(line_writer + "\n");
			}
			new_file.write(this.PROJECT_DETAILS[3]);
			new_file.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
