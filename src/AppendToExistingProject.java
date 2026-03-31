import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.filechooser.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.*;

public class AppendToExistingProject extends AbstractAction
{
	int dialog_selection = -1;
	boolean modified_list = false;
	JtableAssembly assembly_table;
	JtablePurchasing purchasing_table;

	private void ErrorOccured()
	{
		JFrame frame = new JFrame();
		frame.setAlwaysOnTop(true);
		JOptionPane.showMessageDialog(frame, "ERROR - Team Folder ID could not be retreived (err_code: 0002)", "Purchasing Folder Error", JOptionPane.ERROR_MESSAGE);
		return;
	}

	private String GetTeamFolderName(String s)
	{
		int start = s.indexOf(" ");
		int end = s.lastIndexOf(" |");
		/* get index of the first ' ' and '|' */
		return(s.subSequence(start + 1, end).toString());/*start is inclusive*/
	}

	private String GetPurchasingFolderName(String s)
	{
		int start = s.indexOf(" ");
		return(s.subSequence(0, start).toString());
	}

	public int CreateDialog(String[] active_projects)
	{
		JDialog dialog = new JDialog(new JFrame(), "Project Appending", true);
		JList<String> list;
		JButton upload_btn, refine_list_btn;
		JTextField search_field = new JTextField(30);

		HashMap<Integer,Integer> updated_list = new HashMap<>();


		dialog.setLayout(new FlowLayout());
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new CustomDialogExit(dialog));

		list = new JList<String>(active_projects);
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(16);


		/** Button to upload the project to a selected project **/
		upload_btn = new JButton("Upload Project");
		upload_btn.addActionListener(e -> {
			int n = JOptionPane.showConfirmDialog(new JFrame(), "Are you sure you want to proceed with the upload?", "Confirm Upload", JOptionPane.YES_NO_OPTION);
			if(n == JOptionPane.YES_OPTION)
			{
				if(!modified_list)
				{
					dialog_selection = list.getSelectedIndex();
				}
				else
				{
					dialog_selection = updated_list.get(list.getSelectedIndex());
				}
				dialog.dispose();
			}
		});

		/** Button to refine the search results in the Jlist **/
		refine_list_btn = new JButton("Refine Search");
		refine_list_btn.addActionListener(e -> {
			/* key = new index, value = og index */
			String refine = search_field.getText();
			ArrayList<String> new_project_list = new ArrayList<>();
			int j = 0;

			if(!updated_list.isEmpty())
			{
				updated_list.clear();
			}

			if (refine.equals("") || refine.equals(null))
			{
				list.setListData(active_projects);
				modified_list = false;
				return;
			}

			for(int i = 0; i < active_projects.length; i++)
			{
				if (active_projects[i].contains(refine))
				{
					updated_list.put(j++, i);
					new_project_list.add(active_projects[i]);
				}
			}
			modified_list = true;
			list.setListData(new_project_list.toArray(String[]::new));
		});

		/**  Add everything to the Dialog **/
		dialog.add(search_field);
		dialog.add(refine_list_btn);
		dialog.add(new JScrollPane(list));

		//dialog.add(new JLabel("This is a custom dialog box!"));
		dialog.add(upload_btn);
		dialog.setSize(450, 450);
		dialog.setVisible(true);

		return (dialog_selection);
	}

	class CustomDialogExit extends WindowAdapter
	{
		JDialog dlg;
		public CustomDialogExit(JDialog dialog)
		{
			this.dlg = dialog;
		}
		public void windowClosing(WindowEvent e)
		{
			int n = JOptionPane.showConfirmDialog(new JFrame(), "Are you sure you want to exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
			if(n == JOptionPane.YES_OPTION)
			{
				dialog_selection = -1;
				dlg.dispose();
			}
		}
	}

	public AppendToExistingProject(JtableAssembly assy, JtablePurchasing purchasing)
	{
		super("Append to Project");
		this.assembly_table = assy;
		this.purchasing_table = purchasing;
	}
	public void actionPerformed(ActionEvent e)
	{
		String selected_project_name = new String();
		JsonNode zoho_projects_list = null;
		JsonNode json_workdrive_return = null;
		ArrayList<String> major_project_info = new ArrayList<>();
		//
		String purchasing_folder_id = new String();
		//
		String job_number = new String();
		String team_folder_name = new String();
		String project_id = new String();
		ArrayList<String> project_id_list = new ArrayList<>();
		ArrayList<String> project_name_list = new ArrayList<>();
		Iterator<JsonNode> elements;
		int list_selection = -1;

		try
		{
			ApiMethods.GetMajorProjectInfo();
			zoho_projects_list = ApiMethods.GetZohoProjects();
		}
		catch(Exception err)
		{
			ErrorOccured();
			err.printStackTrace();
			return;
		}

		elements = zoho_projects_list.elements();
		while (elements.hasNext())
		{
			JsonNode item = elements.next();
			if(item.get("project_type").asText().matches("active"))
			{
				project_id_list.add(item.get("id").asText());
				project_name_list.add(item.get("name").asText());
			}
		}

		list_selection = CreateDialog(project_name_list.toArray(String[]::new));
		if (list_selection == -1)
		{
			return;
		}
		project_id = project_id_list.get(list_selection);
		selected_project_name = project_name_list.get(list_selection);

		/** Upload to Projects **/
		team_folder_name = GetTeamFolderName(selected_project_name);
		job_number = GetPurchasingFolderName(team_folder_name);

		try
		{
			json_workdrive_return = ApiMethods.ZohoWorkdriveGetTeamFolder(job_number + " Purchasing Sheet");
			Iterator<JsonNode> workdrive_data = json_workdrive_return.get("data").elements();
			String parent_folder_name = new String();
			while(workdrive_data.hasNext())
			{
				JsonNode item = workdrive_data.next();
				parent_folder_name = item.get("attributes").get("lib_info").get("name").asText();
				if(parent_folder_name.equals(team_folder_name))
				{
					purchasing_folder_id = item.get("attributes").get("parent_id").asText();
					break;
				}
			}
		}
		catch(Exception err)
		{
			ErrorOccured();
			return;
		}

		int row_count = assembly_table.AssyRowCount();
		JFrame frame = new JFrame();
		frame.setAlwaysOnTop(true);
		if(row_count == 0)
		{
			JOptionPane.showMessageDialog(frame, "ERROR - No File Found", "No File Selected", JOptionPane.ERROR_MESSAGE);
		}
		else if(purchasing_folder_id == null)
		{
			ErrorOccured();
		}
		else
		{
			int n = JOptionPane.showConfirmDialog(frame, "Are you sure you want to proceed with the upload?", "Upload Project?", JOptionPane.YES_NO_OPTION);
			if(n == JOptionPane.YES_OPTION)
			{
				RunUpload upload_project = new RunUpload(
					selected_project_name,
					job_number,
					null,
					null,
					null,
					null,
					assembly_table.GetAssyTableData(),
					purchasing_table.GetPurchaseTableData(),
					project_id,//project_id
					purchasing_folder_id);//Puchasing folder id
				Thread UploadThread = new Thread(upload_project);
				assembly_table.Disable();
				purchasing_table.Disable();
				UploadThread.start();
				assembly_table.Enable();
				purchasing_table.Enable();
			}
		}
	}
}
