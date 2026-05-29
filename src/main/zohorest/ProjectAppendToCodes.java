package zohorest;

import tables.AssemblyTable;
import tables.PurchasingTable;

import java.util.HashMap;
import java.util.ArrayList;

import java.net.ConnectException;

/** Upload a full project, this includes the project creation itself **/
public class ProjectAppendToCodes extends ZohoApiMethods implements Runnable
{
	AssemblyTable ASSY_TABLE;
	PurchasingTable PURCHASE_TABLE;

	public ProjectAppendToCodes(AssemblyTable a, PurchasingTable p, String url)
	{
		super(url);
		this.ASSY_TABLE = a;
		this.PURCHASE_TABLE = p;
	}
	public void run()
	{
		HashMap<String, String> level_tasklist_ids = new HashMap<>();

		ArrayList<String> task_err_log = new ArrayList<>();
		ArrayList<String> parent_level_ids = new ArrayList<>();

		String project_id = new String();
		String tasklist_id = new String();
		String vault_level = new String();

		int level_depth = 0;

		if(!super.GetProjectInfo())
		{
			System.out.println("ERROR CONNECTING TO SERVER");
			return;
		}
		if (ASSY_TABLE.RowCount() == 0)
		{
			super.CallErrorMsg("No BoM found - Please import a BOM or open a project.");
			return;
		}

		try
		{
			DialogSelectProject project_selection = new DialogSelectProject(super.GetProjectsWithIdName());
			project_id = project_selection.ReturnProjectId(); //Gets the chosen Project Value
			if(!project_selection.UserConfirmSelection())
			{
				return;
			}

			DialogAssyLink assy_link = new DialogAssyLink(super.GetTasklistsWithIdName(project_id), this.ASSY_TABLE.getModel());
			level_tasklist_ids = assy_link.ReturnLinkedTasklist();
			if(!assy_link.UserConfirmSelection())
			{
				return;
			}
		}
		catch (IndexOutOfBoundsException err)
		{
			System.out.println("Index/No project Selected");
			return;
		}
		catch (Exception err)
		{
			err.printStackTrace();
			return;
		}

		super.CreateProgressMonitor(ASSY_TABLE.RowCount());

		for (int i = 0; i < ASSY_TABLE.RowCount(); i++)
		{
			if (super.UpdateProgress(i))
			{
				//if true - the upload has been canceled
				break;
			}
			if (ASSY_TABLE.ToMakeItem(i))
			{
				vault_level = ASSY_TABLE.GetLevel(i);
				if (level_tasklist_ids.containsKey(vault_level))
				{
					 tasklist_id = level_tasklist_ids.get(vault_level);
				}
				try
				{
					super.PostTask(project_id, ASSY_TABLE.ItemTitle(i), ASSY_TABLE.ItemDesc(i), tasklist_id);
				}
				catch (Exception err)
				{
					err.printStackTrace();
					System.out.println(err.getMessage());
					task_err_log.add(ASSY_TABLE.ItemTitle(i));
				}
			}
			try
			{
				Thread.sleep(1000);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		super.CloseProgressMonitor();
		if(!task_err_log.isEmpty())
		{
			String err_msg = "The following tasks failed to upload:\n";
			for (String item: task_err_log)
			{
				err_msg += String.format("%s\n", item);
			}
			CallWarningMsg(err_msg, "Tasks that failed to upload");
		}
	}
}
