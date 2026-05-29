package btnfunctions;

import zohorest.ProjectUpload;
import zohorest.ProjectAppend;
import zohorest.ProjectAppendToCodes;

import tables.AssemblyTable;
import tables.PurchasingTable;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;

import java.awt.event.ActionEvent;

public class BtnProjectUpload
{
	ImageIcon icon = new ImageIcon("../resources/middle.gif");
	Object[] options = {"New Project", "Append with Job Codes", "Append without Job Codes"};
	String[] PROJECT_DETAILS;

	AssemblyTable ASSY_TABLE;
	PurchasingTable PURCHASE_TABLE;
	String LOCALHOST_URL;

	public BtnProjectUpload(AssemblyTable a, PurchasingTable p, String url, String[] pd)
	{
		this.ASSY_TABLE = a;
		this.PURCHASE_TABLE = p;
		this.LOCALHOST_URL = url;
		this.PROJECT_DETAILS = pd;
		this.RunUpload();
	}

	private void RunUpload()
	{
		String user_selection = (String)JOptionPane.showInputDialog(new JFrame(),
		"Select an Upload Type",
		"Upload Project",
		JOptionPane.PLAIN_MESSAGE,
		icon,
		options,
		"New Project");

		if (user_selection == null)
		{
			return;
		}
		if (user_selection.matches((String)options[0]))
		{
			ProjectUpload upload = new ProjectUpload(ASSY_TABLE, PURCHASE_TABLE, LOCALHOST_URL, PROJECT_DETAILS);
			Thread UploadThread = new Thread(upload);
			UploadThread.start();
			return;
		}
		if (user_selection.matches((String)options[1]))
		{
			ProjectAppendToCodes upload = new ProjectAppendToCodes(ASSY_TABLE, PURCHASE_TABLE, LOCALHOST_URL);
			Thread UploadThread = new Thread(upload);
			UploadThread.start();
			return;
		}
		if (user_selection.matches((String)options[2]))
		{
			ProjectAppend upload = new ProjectAppend(ASSY_TABLE, PURCHASE_TABLE, LOCALHOST_URL);
			Thread UploadThread = new Thread(upload);
			UploadThread.start();
			return;
		}
	}
}
