package zohorest;

import tables.AssemblyTable;
import tables.PurchasingTable;

import java.util.ArrayList;

import java.net.ConnectException;

/** Upload a full project, this includes the project creation itself **/
public class ProjectUpload extends ZohoApiMethods implements Runnable
{
	AssemblyTable ASSY_TABLE;
	PurchasingTable PURCHASE_TABLE;
	String[] PROJECT_DETAILS;

	public ProjectUpload(AssemblyTable a, PurchasingTable p, String url, String[] pd)
	{
		super(url);
		this.ASSY_TABLE = a;
		this.PURCHASE_TABLE = p;
		this.PROJECT_DETAILS = pd;
	}
	public void run()
	{
		String project_id = new String();
		String tasklist_id = new String();
		String parent_id = new String();
		ArrayList<String> level_depth_ids = new ArrayList<>();

		ArrayList<String> tasklist_err_log = new ArrayList<>();
		ArrayList<String> task_err_log = new ArrayList<>();
		int level_depth = 0;

		boolean tl_lock = false;

		if(!super.GetProjectInfo())
		{
			System.out.println("ERROR CONNECTING TO SERVER");
			return;
		}
		System.out.println("CONECTION SUCCESSFUL");

		//super.CreateProgressMonitor(ASSY_TABLE.RowCount());
		//TODO: Pass Project Tab variables into Here so they can be used
		//project_id = super.PostProject(job_number, project_title, project_name, project_desc)
		//Start at 1 as row 0 is the Project Upload
		if (ASSY_TABLE.RowCount() == 0)
		{
			super.CallErrorMsg("No BoM found - Please import a BOM or open a project.");
			return;
		}

		super.CreateProgressMonitor(ASSY_TABLE.RowCount());

		try
		{
			project_id = PostProject(PROJECT_DETAILS, ASSY_TABLE.ItemTitle(0));
		}
		catch (Exception err)
		{
			super.CallErrorMsg(String.format("Error: %s", err.getMessage()));
			return;
		}
		for (int i = 1; i < ASSY_TABLE.RowCount(); i++)
		{
			if (super.UpdateProgress(i))
			{
				break;//if true - the upload has been canceled
			}

			level_depth = ASSY_TABLE.CountLevel(i);
			if (level_depth == 1)
			{
				if(!level_depth_ids.isEmpty())
				{
					level_depth_ids.clear();
					// clear the ids as a new tasklist is being made
					// new tasklist -> new Sub Assembly
				}
				try
				{
					tasklist_id = super.PostTasklist(project_id, ASSY_TABLE.ItemTitle(i));
					level_depth_ids.add(0, tasklist_id);
					tl_lock = false;
				}
				catch (Exception err)
				{
					err.printStackTrace();
					System.out.println(err.getMessage());
					tasklist_err_log.add(ASSY_TABLE.ItemTitle(i));
					tl_lock = true;
				}
			}
			try
			{
				if (tl_lock)
				{
					//Parents must have same tasklist - Do not assign a parent task
					super.PostTask(project_id, ASSY_TABLE.ItemTitle(i), ASSY_TABLE.ItemDesc(i));
					continue;
				}
				if ((level_depth - 1) == 0)
				{
					//Parent points to tasklist_id
					super.PostTask(project_id, ASSY_TABLE.ItemTitle(i), ASSY_TABLE.ItemDesc(i), tasklist_id);
					continue;
				}
				if (level_depth_ids.get(level_depth - 1).equals("err"))
				{
					//if a parent failed to upload - continue uploading but do not add parent_id
					super.PostTask(project_id, ASSY_TABLE.ItemTitle(i), ASSY_TABLE.ItemDesc(i), tasklist_id);
					continue;
				}
				parent_id = level_depth_ids.get(level_depth - 1);
				super.PostTask(project_id, ASSY_TABLE.ItemTitle(i), ASSY_TABLE.ItemDesc(i), tasklist_id, parent_id);
			}
			catch (Exception err)
			{
				err.printStackTrace();
				System.out.println(err.getMessage());
				level_depth_ids.add(level_depth, "err");
				task_err_log.add(ASSY_TABLE.ItemTitle(i));
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
		if(!tasklist_err_log.isEmpty())
		{
			String err_msg = "The following tasklists failed to upload:\n";
			for (String item: tasklist_err_log)
			{
				err_msg += String.format("%s\n", item);
			}
			CallWarningMsg(err_msg, "Tasklists that failed to upload");
		}
	}
}
