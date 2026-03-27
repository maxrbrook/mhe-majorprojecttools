import java.util.*;
import java.net.http.*;
import java.net.*;
import javax.swing.*;
import javax.swing.table.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.*;
import java.lang.Thread;
import java.time.*;
/*
	Methods related to uploading the project to Zoho Projects
*/
/*
TODO
	Update api_url to project_url, tasklist_url and task_url
	Set these when the project is created or if they are not null (append)

	breakdown the methods in the run() to their seperate functions to be called
	Api try / catch to be updated so they can be recalled with a new access_token
*/

public class RunUpload implements Runnable
{
	private static String PROJECT_TITLE, JOB_NUMBER, CLIENT, SALES_ORDER, CUSTOMER_ORDER, DESCRIPTION, PROJECT_ID, PURCHASING_FOLDER_ID;
	private static DefaultTableModel assy_table, purchase_table;

	RunUpload(String project_title, String job_number, String client, String sales_order, String customer_order, String desc, DefaultTableModel assy_table, DefaultTableModel purchase_table, String project_id, String purchasing_folder_id)
	{
		this.PROJECT_TITLE = project_title;
		this.JOB_NUMBER = job_number;
		this.CLIENT = client;
		this.SALES_ORDER = sales_order;
		this.CUSTOMER_ORDER = customer_order;
		this.DESCRIPTION = desc;
		this.assy_table = assy_table;
		this.purchase_table = purchase_table;
		this.PROJECT_ID = project_id;
		this.PURCHASING_FOLDER_ID = purchasing_folder_id;
    }
	// ---- IMPORTANT
	private static boolean SYSTEM_LOGGING_ENABLED = false;//Enables/Disables Logging to terminal
	private static boolean SYSTEM_UPLOADING_ENABLED = false;
	private static int ASSY_ROW_COUNT;
	private static int PURCHASE_ROW_COUNT;
	// ---- IMPORTANT

	//COMMON POSITIONS
	private static int INT_VAULT_LEVEL = 0;
	private static int INT_DRAWING_NUMBER = 1;
	private static int INT_DESCRIPTION = 2;
	private static int VAULT_QTY = 3;//called Vault QTY
	private static int ORDER_QTY = 4;//called TO ORDER QTY
	private static int INT_ORDER_TYPE = 5;
	// ASSY ITEMS ONLY
	private static int INT_EST_HOURS = 6;
	private static int INT_NOTES = 7;
	private static int INT_VAULT_URL = 8;
	private static int INT_ASSY_LEVEL = 9;
	private static int INT_ISSUE = 10;

	private ProgressMonitor pm;
	boolean UPLOAD_CANCELLED = false;
	/*
		HashMapToJsonString - formats a Hashmap to a JSON string
		Map<string,string> m: the map of srings to be convert

		Returns: the formatted JSON file as a string
	*/
	private static String AssyValueAt(int row, int column)
	{
		return (assy_table.getValueAt(row, column).toString());
	}

	private static String PurchaseValueAt(int row, int column)
	{
		return (purchase_table.getValueAt(row, column).toString());
	}

	private static String HashMapToJsonString(Map<String,String> m)
	{
		String s = new String();
		try
		{
			ObjectMapper json_map = new ObjectMapper();
			s = json_map.writeValueAsString(m);
		}
		catch(Exception e)
		{
			System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
			e.printStackTrace();
			return "";
		}
		return(s);
	}

	private static int AssyDots(String vault_level)
	{
		char[] char_array = vault_level.toCharArray();
		int dots = 0;
		for(int j = 0; j < char_array.length; j++)
		{
			if(char_array[j] == '.')
			{
				dots++;
			}
		}
		return (dots);
	}

	private static String MakeProjectDescription()
	{
		String s = "Job Number: " + JOB_NUMBER + "<br>Client: " + CLIENT;
		s += "<br>Sales Order Number: " + SALES_ORDER + "<br>Customer Order Number: " + CUSTOMER_ORDER;
		s += "<br>Project Overview:<br>" + DESCRIPTION;

		return(s);
	}

	private static String TaskDetails(int i)
	{
		String s = new String();
		String drawing_number = AssyValueAt(i, INT_DRAWING_NUMBER);
		String vault_qty = AssyValueAt(i, VAULT_QTY);
		String build_qty = AssyValueAt(i, ORDER_QTY);
		String est_hours = AssyValueAt(i, INT_EST_HOURS);
		String notes = AssyValueAt(i, INT_NOTES);
		String vault_url = AssyValueAt(i, INT_VAULT_URL);
		String issue = AssyValueAt(i, INT_ISSUE);

		s = "Job #: " + JOB_NUMBER + " Client: " + CLIENT + "<br>Drawing Number: " + drawing_number + " Issue: [" + issue + "] Vault URL: " + vault_url;
		s += "<br>QTY: " + vault_qty + "<br>QTY TO MAKE: " + build_qty + "<br>Notes:<br>" + notes;

		return(s);
	}

	private static String AssyMapping(int i)
	{
		Map<String, String> api_atts = new HashMap<>();

		api_atts.put(Main.ZS_ITEM_NUMBER, AssyValueAt(i, INT_DRAWING_NUMBER));
		api_atts.put(Main.ZS_DESCRIPTION, AssyValueAt(i, INT_DESCRIPTION));
		api_atts.put(Main.ZS_VAULT_QTY, AssyValueAt(i, VAULT_QTY));
		api_atts.put(Main.ZS_ORDER_QTY, AssyValueAt(i, ORDER_QTY));
		api_atts.put(Main.ZS_PURCHASE_TYPE, "Plate Part");
		api_atts.put(Main.ZS_NOTES, AssyValueAt(i, INT_NOTES));

		if (SYSTEM_LOGGING_ENABLED)
		{
			System.out.println(api_atts);
		}
		return(HashMapToJsonString(api_atts));
	}

	/** Get the Assembly and Purchasing items directly related to the sub assembly **/
	private static ArrayList<String> SubAssyMaterialsSheet(int i)
	{
		Map<String,String> api_atts = new HashMap<>();
		ArrayList<String> material_list = new ArrayList<String>();
		String task_level = AssyValueAt(i, INT_VAULT_LEVEL);
		String vault_level = new String();
		
		for(int j = (i + 1); j < ASSY_ROW_COUNT; j++)
		{
			vault_level = AssyValueAt(j, INT_VAULT_LEVEL);
			if(vault_level.matches("1(\\.\\d+){1}")) // -> 1.xxx
			{
				break;//This will be the next sub assembly - which will handle this function when called
			}
			if(vault_level.matches("(" + task_level + ")(\\.\\d+){1}"))
			{
				if(AssyValueAt(j, INT_ASSY_LEVEL).equalsIgnoreCase("Plate Part") && AssyValueAt(j, INT_ORDER_TYPE).equalsIgnoreCase("Order/Outsource"))
				{
					material_list.add(AssyMapping(j));
				}
			}
		}
		/** Purchasing **/
		for(int k = 0; k < PURCHASE_ROW_COUNT; k++)
		{
			if(PurchaseValueAt(k, INT_VAULT_LEVEL).matches("(" + task_level + ")(\\.\\d+){0,}"))
			{
				api_atts.put(Main.ZS_ITEM_NUMBER, PurchaseValueAt(k, INT_DRAWING_NUMBER));
				api_atts.put(Main.ZS_DESCRIPTION, PurchaseValueAt(k, INT_DESCRIPTION));
				api_atts.put(Main.ZS_VAULT_QTY, PurchaseValueAt(k, VAULT_QTY));
				api_atts.put(Main.ZS_ORDER_QTY,  PurchaseValueAt(k, ORDER_QTY));
				api_atts.put(Main.ZS_PURCHASE_TYPE, PurchaseValueAt(k, INT_ORDER_TYPE));
				api_atts.put(Main.ZS_NOTES, PurchaseValueAt(k, 6));
				material_list.add(HashMapToJsonString(api_atts));
			}
		}
		material_list.add(AssyValueAt(i, INT_DRAWING_NUMBER));
		material_list.add(AssyValueAt(i, INT_DESCRIPTION));
		return(material_list);
	}

	private static String TaskDescriptionBillOfMaterials(int i)
	{
		String order_string = new String();
		String assemblies = "<br><u>ELEMENTS OF SUB ASSEMBLY:</u><br>";
		String plates = "<br><u>PLATE ITEMS:</u><br>";
		String purchasing = "<br><u>MATERIAL ITEMS:</u><br>";
		String vault_level = new String();//0
		String drawing_number = new String();//1
		String description = new String();//2
		String assy_level = new String();//3
		String order_type = new String();//4
		String vault_qty = new String();//5
		String total_qty = new String();//6

		String task_level = AssyValueAt(i, INT_VAULT_LEVEL);

		/** Run through the Assembly Tab **/
		for(int j = (i + 1); j < ASSY_ROW_COUNT; j++)
		{
			vault_level = AssyValueAt(j, INT_VAULT_LEVEL);

			if(vault_level.matches("1(\\.\\d+){1}"))
			{
				break;//This will be the next sub assembly - which wil handle this function when called
			}
			if(vault_level.matches("(" + task_level + ")(\\.\\d+){1}"))
			{
				drawing_number = AssyValueAt(j, INT_DRAWING_NUMBER);
				description = AssyValueAt(j, INT_DESCRIPTION);
				assy_level = AssyValueAt(j, INT_ASSY_LEVEL);
				order_type = AssyValueAt(j, INT_ORDER_TYPE);
				vault_qty = AssyValueAt(j, VAULT_QTY);

				order_string = "&emsp;<b>" + drawing_number + " [" + order_type + "][QTY: " + vault_qty + "]:</b> " + description + "<br>";

				if(assy_level.equalsIgnoreCase("Plate Part"))
				{
					plates = plates + order_string;
				}
				else
				{
					assemblies = assemblies + order_string;
				}
			}
		}
		/** Run through the Purchasing Tab **/
		for(int k = 0; k < PURCHASE_ROW_COUNT; k++)
		{
			vault_level = PurchaseValueAt(k, INT_VAULT_LEVEL);
			if(vault_level.matches("(" + task_level + ")(\\.\\d+){1}"))
			{
				description = PurchaseValueAt(k, INT_DESCRIPTION);
				vault_qty = PurchaseValueAt(k, VAULT_QTY);

				purchasing = purchasing + "&emsp;[QTY: " + vault_qty + "]: " + description + "<br>";
			}
		}
		return(assemblies + plates + purchasing);
	}

	private static String GetAssyLevelsFromPurchasing(ArrayList<String> assembly_list)
	{
		String purchase_level = new String();
		String vault_level = new String();
		String drawing_number = new String();
		ArrayList<String> drawing_number_list = new ArrayList<String>();

		for(int i = 0; i < assembly_list.size(); i++)
		{
			purchase_level = assembly_list.get(i);
			for(int j = 0; j < ASSY_ROW_COUNT; j++)
			{
				vault_level = AssyValueAt(j, INT_VAULT_LEVEL);
				if(purchase_level.matches("(" + vault_level + ")(\\.\\d+){1}"))
				{
					drawing_number = AssyValueAt(j, INT_DRAWING_NUMBER);
					/* prevent double ups with drawing_number_list */
					if(drawing_number_list.contains(drawing_number))
					{
						continue;
					}
					drawing_number_list.add(drawing_number);
				}
			}
		}
		return (drawing_number_list.toString());
	}

	private static String IterateThroughPurchasing(String item_desc, String length_as_string, String purchase_type, boolean is_material, int i)
	{
		Map<String,String> api_atts = new HashMap<>();
		ArrayList<String> list_of_added_items = new ArrayList<String>();

		String str_Qty = new String();//5
		String str_Total = new String();//6

		float material_length = 0;
		float material_length_qty = 0;
		float material_length_total = 0;
		int purchase_qty = 0;
		int purchase_total = 0;

		for(int j = i; j < PURCHASE_ROW_COUNT; j++)
		{
			if (PurchaseValueAt(j, INT_DESCRIPTION).contains(item_desc))
			{
				if (is_material)
				{
					material_length = Float.parseFloat(length_as_string.replace("mm", "").replace(" ", ""));
					material_length_qty += (material_length * Integer.parseInt(PurchaseValueAt(j, VAULT_QTY)));
					material_length_total += (material_length * Integer.parseInt(PurchaseValueAt(j, ORDER_QTY)));
				}
				else
				{
					purchase_qty += Integer.parseInt(PurchaseValueAt(j, VAULT_QTY));
					purchase_total += Integer.parseInt(PurchaseValueAt(j, ORDER_QTY));
				}
				list_of_added_items.add(PurchaseValueAt(j, INT_VAULT_LEVEL));
			}
		}
		if(is_material)
		{
			str_Qty = Float.toString(material_length_qty);
			str_Total = Float.toString(material_length_total);
		}
		else
		{
			str_Qty = Integer.toString(purchase_qty);
			str_Total = Integer.toString(purchase_total);
		}
		if (SYSTEM_LOGGING_ENABLED)
		{
			System.out.println(item_desc + "QTY: "+ str_Qty + " TOTAL: " + str_Total);
		}
		api_atts.put(Main.ZS_DESCRIPTION, item_desc);
		api_atts.put(Main.ZS_PURCHASE_TYPE, purchase_type);
		api_atts.put(Main.ZS_ITEM_NUMBER, PurchaseValueAt(i, INT_DRAWING_NUMBER));
		api_atts.put(Main.ZS_VAULT_QTY, str_Qty);
		api_atts.put(Main.ZS_ORDER_QTY, str_Total);
		api_atts.put(Main.ZS_WHERE_USED, GetAssyLevelsFromPurchasing(list_of_added_items));

		return (HashMapToJsonString(api_atts));
	}

	private static ArrayList<String> RollUpPurchaseSheet()
	{
		ArrayList<String> material_roll_up = new ArrayList<String>();
		ArrayList<String> item_processed_list = new ArrayList<String>();

		String item_to_check = new String();
		String str_SplitDesc[] = new String[2];

		for(int i = 0; i < PURCHASE_ROW_COUNT; i++)
		{
			item_to_check = PurchaseValueAt(i, INT_DESCRIPTION);
			/*
			 * (.)+: if any character any number of times.
			 * ( - ): if 2 spaces surround a dash.
			 * ([\\d.]){1,}: if any digit and any character once or more.
			 * ( mm): has a space and mm.
			 * Item_name - 100.5 mm <- will be a match
			 */
			if(item_to_check.matches("(.)+( - )([\\d.]){1,}( mm)"))
			{
				str_SplitDesc = item_to_check.split(" - ");
				if(item_processed_list.contains(str_SplitDesc[0]))
				{
					continue;
				}
				item_processed_list.add(str_SplitDesc[0]);
				material_roll_up.add(IterateThroughPurchasing(str_SplitDesc[0], str_SplitDesc[1], "Material Length", true, i));
			}
			else
			{
				if(item_processed_list.contains(item_to_check))
				{
					continue;
				}
				item_processed_list.add(item_to_check);
				material_roll_up.add(IterateThroughPurchasing(item_to_check, "", "", false, i));
			}
		}
		material_roll_up.add(JOB_NUMBER + " ROLL UP");//Drawing Number
		material_roll_up.add(PROJECT_TITLE);//Description
		return (material_roll_up);
	}

	private static ArrayList<String> CreatePlateRollUp()
	{
		Map<String, String> api_atts = new HashMap<>();
		ArrayList<String> plate_roll_up = new ArrayList<String>();
		String assy_level = new String();
		String order_type = new String();

		for(int i = 0; i < ASSY_ROW_COUNT; i++)
		{
			if(AssyValueAt(i, INT_ASSY_LEVEL).equalsIgnoreCase("Plate Part") && AssyValueAt(i, INT_ORDER_TYPE).equalsIgnoreCase("Order/Outsource"))
			{
				plate_roll_up.add(AssyMapping(i));
			}
		}
		plate_roll_up.add(JOB_NUMBER + " PLATES");//drawing number
		plate_roll_up.add("PLATE PARTS");//Description
		return (plate_roll_up);
	}

	private static String UrlEncodeString(String s)
	{
		String rtn = new String();
		try
		{
			rtn = URLEncoder.encode(s, "UTF-8");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return rtn;
	}

	/** Recursive functions for the Api calls **/
	private String CreateNewZSWorkbook() throws Exception
	{
		String url = "https://www.zohoapis.com.au/workdrive/api/v1/files";
		Map<String, String> workdrive_atts = new HashMap<>();
		Map<String, Object> workdrive_params = new HashMap<>();//workdrive_params
		Map<String, Object> workdrive_data = new HashMap<>();
		/** Make the Sheet **/
		if(PROJECT_ID == null)/* Project Uploaded */
		{
			workdrive_atts.put("name", JOB_NUMBER + " Purchasing Sheet");
		}
		else/* Project Appended */
		{
			workdrive_atts.put("name", JOB_NUMBER + " Purchasing Sheet_" + (LocalDate.now()).toString());
		}
		workdrive_atts.put("parent_id", PURCHASING_FOLDER_ID);
		workdrive_atts.put("service_type", "zohosheet");
		workdrive_params.put("type", "files");
		workdrive_params.put("attributes", workdrive_atts);
		workdrive_data.put("data", workdrive_params);
		try
		{
			return(ApiMethods.ZohoWorkdrivePost(workdrive_data, url));
		}
		catch(InvalidOauthException err)
		{
			ApiMethods.GetMajorProjectInfo();
			return(ApiMethods.ZohoWorkdrivePost(workdrive_data, url));
		}
	}

	private String CreateWorkdriveFolder(String folder_id, String title, String url, boolean team_folder) throws Exception
	{
		String rtn_val = new String();

		Map<String, String> workdrive_atts = new HashMap<>();
		Map<String, Object> workdrive_params = new HashMap<>();
		Map<String, Object> workdrive_data = new HashMap<>();

		workdrive_atts.put("name", title);
		workdrive_atts.put("parent_id", folder_id);
		if (team_folder)
		{
			workdrive_atts.put("is_public_within_team", "false");
			workdrive_params.put("type", "teamfolders");
		}
		else
		{
			workdrive_params.put("type", "files");
		}
		workdrive_params.put("attributes", workdrive_atts);
		workdrive_data.put("data", workdrive_params);

		try
		{
			return(ApiMethods.ZohoWorkdrivePost(workdrive_data, url));
		}
		catch(InvalidOauthException err)
		{
			ApiMethods.GetMajorProjectInfo();
			return(ApiMethods.ZohoWorkdrivePost(workdrive_data, url));
		}
	}

	private String CreateNewTask(ArrayList<String> assy_ids, String item_name, int i, int dots) throws Exception
	{
		Map<String, String> task_params = new HashMap<>();
		String url = "https://projectsapi.zoho.com.au/api/v3/portal/" + Main.PORTAL_ID + "/projects/" + PROJECT_ID + "/tasks";

		task_params.put("name", item_name);
		task_params.put("tasklist", "{id:" + (assy_ids.get(0)) + "}");
		task_params.put("description", TaskDetails(i) + TaskDescriptionBillOfMaterials(i));
		if (dots > 1)
		{
			task_params.put("parental_info", "{parent_task_id:" + (assy_ids.get(dots - 1)) + "}");
		}

		try
		{
			return (ApiMethods.ProjectsItemPOST(task_params, url));
		}
		catch(InvalidOauthException err)
		{
			ApiMethods.GetMajorProjectInfo();
			return(ApiMethods.ProjectsItemPOST(task_params, url));
		}
	}

	private String CreateNewTasklist(String sheets_id, String item_name, int i) throws Exception
	{
		Map<String, String> task_params = new HashMap<>();
		String url = "https://projectsapi.zoho.com.au/api/v3/portal/" + Main.PORTAL_ID + "/projects/" + PROJECT_ID + "/tasklists";

		task_params.put("name", item_name);
		try
		{
			ApiMethods.MakeNewSheetPage(sheets_id, true, true, SubAssyMaterialsSheet(i));
		}
		catch(InvalidOauthException err)
		{
			ApiMethods.GetMajorProjectInfo();
			ApiMethods.MakeNewSheetPage(sheets_id, true, true, SubAssyMaterialsSheet(i));
		}
		catch(Exception err)
		{
			SetWarningMsg("Creating sheet for " + item_name, err.toString());
		}

		try
		{
			return (ApiMethods.ProjectsItemPOST(task_params, url));
		}
		catch(InvalidOauthException err)
		{
			ApiMethods.GetMajorProjectInfo();
			return(ApiMethods.ProjectsItemPOST(task_params, url));
		}
	}

	private String CreateNewProject(String item_name) throws Exception
	{
		Map<String, String> task_params = new HashMap<>();
		String url = "https://projectsapi.zoho.com.au/api/v3/portal/" + Main.PORTAL_ID + "/projects";

		task_params.put("name", "JOB#: " + JOB_NUMBER + " " + PROJECT_TITLE + " | " + item_name);
		//full title -> Job#: JobNumber ProjectTitle | Drawing Number: Description
		task_params.put("description", MakeProjectDescription());
		task_params.put("layout", "{id:" + Main.PROJECT_LAYOUT_ID + "}");
		task_params.put("sub_module_settings", "{sub_module_layouts_configuration:{tasks:{id:" + Main.TASK_LAYOUT_ID + ",is_copy_as_private:false}}}");


		try
		{
			return (ApiMethods.ProjectsItemPOST(task_params, url));
		}
		catch(InvalidOauthException err)
		{
			ApiMethods.GetMajorProjectInfo();
			return (ApiMethods.ProjectsItemPOST(task_params, url));
		}
	}

	private void SetItemStatus(String item_id, int i)
	{
		String desc = AssyValueAt(i, INT_DESCRIPTION);
		try
		{
			ApiMethods.SetWorkColumn(item_id, desc.contains("[MACHINE]"), desc.contains("[WELD]"));
		}
		catch(InvalidOauthException err)
		{
			try{
				ApiMethods.GetMajorProjectInfo();
				ApiMethods.SetWorkColumn(item_id, desc.contains("[MACHINE]"), desc.contains("[WELD]"));
			}
			catch(Exception e)
			{
				return;
			}
		}
		catch(Exception err)
		{
			err.printStackTrace();
		}
	}

	/*
		Progress Monitor Functions
	*/
	private void CreateProgressMonitor(int i)
	{
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame();
			pm = new ProgressMonitor(frame, "Uploading Project:", "Completed: 0/" + i, 0, i);
			frame.setAlwaysOnTop(true);
		});
	}
	private void UpdateProgressMonitor(int i)
	{
		SwingUtilities.invokeLater(() -> {
			pm.setProgress(i);
			pm.setNote("Completed: " + i + "/" + pm.getMaximum());
			if(pm.isCanceled())
			{
				UPLOAD_CANCELLED = true;
			}
		});
	}
	private void CloseProgressMonitor()
	{
		SwingUtilities.invokeLater(() -> {
			pm.close();
		});
	}

	private void SetErrorMsg(String location, String error)
	{
		final String err_msg = "ERROR AT: " + location + "\n" + error + "\n. Please contact Admin for Assistance";
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame();
			frame.setAlwaysOnTop(true);
			JOptionPane.showMessageDialog(frame, err_msg, "Error Uploading has Occured", JOptionPane.ERROR_MESSAGE);
		});
		CloseProgressMonitor();
		return;
	}

	private void SetWarningMsg(String location, String error)
	{
		final String err_msg = "WARNING: " + location + "\n" + error + "\n. Project will continue to upload";
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame();
			frame.setAlwaysOnTop(true);
			JOptionPane.showMessageDialog(frame, err_msg, "Error Uploading has Occured", JOptionPane.WARNING_MESSAGE);
		});
		CloseProgressMonitor();
		return;
	}

	public void run()
	{
		ArrayList<String> sub_assy_ids = new ArrayList<String>();
		String item_name = new String();
		String workdrive_team_folder = new String();
		String vault_level = new String();
		String api_url = new String();
		String zoho_item_id = new String();
		String zoho_sheets_id = new String();

		String folder_name = JOB_NUMBER + " " + PROJECT_TITLE;
		String workdrive_url = "https://www.zohoapis.com.au/workdrive/api/v1/teamfolders";

		int dots = 0;

		ASSY_ROW_COUNT = assy_table.getRowCount();
		PURCHASE_ROW_COUNT = purchase_table.getRowCount();

		CreateProgressMonitor(ASSY_ROW_COUNT);

		if(PROJECT_ID == null)
		{
			try
			{
				/** Upload the Project **/
				item_name = AssyValueAt(0, INT_DRAWING_NUMBER) + ": " + AssyValueAt(0, INT_DESCRIPTION);
				PROJECT_ID = CreateNewProject(item_name);

				/** Generate Folder Structure **/
				workdrive_team_folder = CreateWorkdriveFolder(Main.WORKDRIVE_ID, folder_name, workdrive_url, true);
				folder_name = JOB_NUMBER + " Purchasing";
				workdrive_url = "https://www.zohoapis.com.au/workdrive/api/v1/files";
				PURCHASING_FOLDER_ID = CreateWorkdriveFolder(workdrive_team_folder, folder_name, workdrive_url, false);
			}
			catch(Exception err)
			{
				SetErrorMsg("Creating new project board", err.toString());
				return;
			}
		}
		try
		{
			zoho_sheets_id = CreateNewZSWorkbook();
			ApiMethods.MakeNewSheetPage(zoho_sheets_id, true, true, RollUpPurchaseSheet());
			ApiMethods.MakeNewSheetPage(zoho_sheets_id, true, false, CreatePlateRollUp());
		}
		catch(Exception err)
		{
			SetWarningMsg("Creating Rollup Sheets", err.toString());
			//return;
		}
		for(int i = 1; i < ASSY_ROW_COUNT; i++)
		{
			if(UPLOAD_CANCELLED)
			{
				break;
			}
			UpdateProgressMonitor(i);
			item_name = AssyValueAt(i, INT_DRAWING_NUMBER) + ": " + AssyValueAt(i, INT_DESCRIPTION);//Add Description
			vault_level = AssyValueAt(i, INT_VAULT_LEVEL);
			if(vault_level.matches("1(\\.\\d+){1}"))
			{
				if(!sub_assy_ids.isEmpty())
				{
					sub_assy_ids.clear();
				}
				try
				{
					zoho_item_id = CreateNewTasklist(zoho_sheets_id, item_name, i);
					sub_assy_ids.add(0, zoho_item_id);
				}
				catch(Exception err)
				{
					SetErrorMsg("Creating Tasklist - " + item_name, err.toString());
					err.printStackTrace();
					return;
				}
			}

			if (AssyValueAt(i, INT_ORDER_TYPE).equalsIgnoreCase("TO MAKE"))
			{
				dots = AssyDots(vault_level);
				try
				{
					zoho_item_id = CreateNewTask(sub_assy_ids, item_name, i, dots);
					SetItemStatus(zoho_item_id, i);
					sub_assy_ids.add(dots, zoho_item_id);
				}
				catch(Exception err)
				{
					SetErrorMsg("Creating Task - " + item_name, err.toString());
					return;
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
		try
		{
			ApiMethods.ZohoMarkSheetReady(zoho_sheets_id);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		CloseProgressMonitor();
		return;
	}
}
