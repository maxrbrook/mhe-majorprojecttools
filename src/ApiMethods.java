import java.util.*;
import java.net.http.*;
import java.net.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.*;

/** Class for handling API calls **/
public class ApiMethods
{
	private static String ZSHEETS_HEADERS = "&data_array=[\"" + Main.ZS_ITEM_NUMBER + "\",\"" + Main.ZS_PURCHASE_TYPE + "\",\"" + Main.ZS_DESCRIPTION + "\",\"" + Main.ZS_VAULT_QTY + "\",\"" + Main.ZS_ORDER_QTY + "\",\"" + Main.ZS_NOTES + "\",";
	private static String ZSHEETS_HEADERS_ALT = "&data_array=[\"" + Main.ZS_ITEM_NUMBER + "\",\"" + Main.ZS_PURCHASE_TYPE + "\",\"" + Main.ZS_DESCRIPTION + "\",\"" + Main.ZS_VAULT_QTY + "\",\"" + Main.ZS_ORDER_QTY + "\",\"" + Main.ZS_WHERE_USED + "\",\"" + Main.ZS_NOTES + "\",";
	private static String ZSHEETS_HEADERS_ORDERING = "\"SUPPLIER\",\"PO NUMBER\",\"PURCHASE DATE\",\"INITIALS\",\"DATE RECEIVED\",\"QC (INITIALS)\",\"DATE MOVED\",\"LOCATION\"]";

	private static String OauthReturn = "400_OAUTH";
	private static String OauthErrorTitle = "INVALID_OAUTHTOKEN";

	static String UrlEncodeString(String s)
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
		return (rtn);
	}

	public static boolean TestValidURL() throws Exception
	{
		HttpRequest http_request = HttpRequest.newBuilder()
			.uri(URI.create(Main.localhost_url))
			.GET()
			.build();

		HttpResponse<String> http_response = null;
		http_response = HttpClient.newHttpClient().send(http_request, HttpResponse.BodyHandlers.ofString());
		if(http_response.statusCode() >= 300)
		{
			throw new Exception(Integer.toString(http_response.statusCode()));
		}
		return (true);
	}

	/** retrieve credentials from localhost server **/
	public static void GetMajorProjectInfo() throws Exception
	{
		HttpRequest http_request = HttpRequest.newBuilder()
			.uri(URI.create(Main.localhost_url + "/major"))
			.GET()
			.build();

		HttpResponse<String> http_response = null;
		http_response = HttpClient.newHttpClient().send(http_request, HttpResponse.BodyHandlers.ofString());
		if(http_response.statusCode() >= 300)
		{
			throw new Exception(Integer.toString(http_response.statusCode()));
		}
		ObjectMapper response_map = new ObjectMapper();
		JsonNode json_node = response_map.readTree(http_response.body());

		Main.ACCESS_TOKEN = json_node.get("access_token").asText();
		Main.PORTAL_ID = json_node.get("portal_id").asText();
		Main.PROJECT_LAYOUT_ID = json_node.get("project_layout_id").asText();
		Main.TASK_LAYOUT_ID = json_node.get("task_layout_id").asText();
		Main.WORKDRIVE_ID = json_node.get("workdrive_team_id").asText();
		Main.BLUEPRINT_ID = json_node.get("blueprint_id").asText();
		Main.MACHINE_TRANSITION_ID = json_node.get("machine_transition_id").asText();
		Main.FAB_TRANSITION_ID = json_node.get("fab_transition_id").asText();
	}

	/** Zoho Projects Methods **/
	public static JsonNode GetZohoProjects() throws InvalidOauthException, Exception
	{
		/** Get the List of Projects under the Portal ID **/
		String api_url = "https://projectsapi.zoho.com.au/api/v3/portal/" + Main.PORTAL_ID + "/projects";
		HttpResponse<String> http_response = null;

		HttpRequest http_request = HttpRequest.newBuilder()
		.uri(URI.create(api_url))
		.header("Authorization", "Zoho-oauthtoken " + Main.ACCESS_TOKEN)
		.GET()
		.build();

		http_response = HttpClient.newHttpClient().send(http_request, HttpResponse.BodyHandlers.ofString());

		ObjectMapper response_map = new ObjectMapper();
		JsonNode json_node = response_map.readTree(http_response.body());

		if(http_response.statusCode() >= 300)
		{
			String err_type = json_node.get("error").get("title").asText();
			if (err_type.equals(OauthErrorTitle))
			{
				throw new InvalidOauthException(OauthReturn);
			}
			throw new Exception(Integer.toString(http_response.statusCode()));
		}

		return (json_node);
	}
	
	public static String ProjectsItemPOST(Map<String,String> params, String url) throws InvalidOauthException, Exception
	{
		/** Posts a tasklist/task/project to Zoho **/
		HttpResponse<String> http_response = null;
		ObjectMapper json_map = new ObjectMapper();
		String json_string = json_map.writeValueAsString(params);

		HttpRequest http_request = HttpRequest.newBuilder()
		.uri(URI.create(url))
		.header("Authorization", "Zoho-oauthtoken " + Main.ACCESS_TOKEN)
		.header("Content-Type", "application/json")
		.POST(HttpRequest.BodyPublishers.ofString(json_string))
		.build();

		http_response = HttpClient.newHttpClient().send(http_request, HttpResponse.BodyHandlers.ofString());

		ObjectMapper response_map = new ObjectMapper();
		JsonNode json_node = response_map.readTree(http_response.body());
		if(http_response.statusCode() >= 300)
		{
			String err_type = json_node.get("error").get("title").asText();
			if (err_type.equals(OauthErrorTitle))
			{
				throw new InvalidOauthException(OauthReturn);
			}
			throw new Exception(Integer.toString(http_response.statusCode()));
		}
		return (json_node.get("id").asText());
	}

	public static void SetWorkColumn(String task_id, boolean MachineItem, boolean WeldItem) throws InvalidOauthException, Exception
	{
		String api_url = "https://projectsapi.zoho.com.au/api/v3/portal/" + Main.PORTAL_ID + "/automation/blueprint/" + Main.BLUEPRINT_ID + "/transition/";
		String transition_id = new String();
		Map<String, String> blueprint_map = new HashMap<>();
		blueprint_map.put("entity_id", task_id);
		
		if(!(MachineItem) && !(WeldItem))// if false && false
		{
			return;
		}

		if(MachineItem)
		{
			transition_id = Main.MACHINE_TRANSITION_ID;//Machine Awaiting Start
		}
		if(WeldItem)
		{
			transition_id = Main.FAB_TRANSITION_ID;//Fab Awaiting Start
		}
		api_url = api_url + transition_id + "/execute";

		HttpResponse<String> http_response = null;
		ObjectMapper objm_JsonParameters = new ObjectMapper();
		String json_string = objm_JsonParameters.writeValueAsString(blueprint_map);

		HttpRequest http_request = HttpRequest.newBuilder()
		.uri(URI.create(api_url))
		.header("Authorization", "Zoho-oauthtoken " + Main.ACCESS_TOKEN)
		.header("Content-Type", "application/json")
		.POST(HttpRequest.BodyPublishers.ofString(json_string))
		.build();

		http_response = HttpClient.newHttpClient().send(http_request, HttpResponse.BodyHandlers.ofString());

		ObjectMapper response_map = new ObjectMapper();
		JsonNode json_node = response_map.readTree(http_response.body());
		if(http_response.statusCode() >= 300)
		{
			String err_type = json_node.get("error").get("title").asText();
			if (err_type.equals(OauthErrorTitle))
			{
				throw new InvalidOauthException(OauthReturn);
			}
			throw new Exception(Integer.toString(http_response.statusCode()));
		}
		return;
	}
	
	/** ZOHO SHEETS **/
	public static void MakeNewSheetPage(String zoho_sheets_id, boolean new_sheet, boolean alt_heading, ArrayList<String> sheet_data) throws Exception
	{
		String url = "https://sheet.zoho.com.au/api/v2/" + zoho_sheets_id;
		String sheet_description = new String();
		String sheet_drawing_number = new String();
		String params = new String();
		String encoded_heading = new String();
		int len = sheet_data.size();
		// if the only items are the title and description
		if (len == 2)
		{
			return;
		}
		sheet_description = sheet_data.get(--len);
		sheet_data.remove(len);
		sheet_drawing_number = sheet_data.get(--len);
		sheet_data.remove(len);
		encoded_heading = UrlEncodeString(sheet_drawing_number + " - " + sheet_description);
		System.out.println(encoded_heading);
		try
		{
			if (new_sheet)
			{
				try
				{
					params = "method=worksheet.insert&worksheet_name=" + sheet_drawing_number;
					WorksheetDataPOST(params, url);
				}
				catch(InvalidOauthException err)
				{
					GetMajorProjectInfo();
					WorksheetDataPOST(params, url);
				}
			}
			if (alt_heading)
			{
				try
				{
					params = "method=row.content.set&worksheet_name=" + sheet_drawing_number + "&row=1&column_array=[1,8,12]";
					params += "&data_array=[\"" + encoded_heading + "\",\"PURCHASING\",\"WAREHOUSE\"]";
					WorksheetDataPOST(params, url);
				}
				catch(InvalidOauthException err)
				{
					GetMajorProjectInfo();
					WorksheetDataPOST(params, url);
				}

				try
				{
					params = "method=row.content.set&worksheet_name=" + sheet_drawing_number + "&row=2&column_array=[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15]";
					params += ZSHEETS_HEADERS_ALT + ZSHEETS_HEADERS_ORDERING;
					WorksheetDataPOST(params, url);
				}
				catch(InvalidOauthException err)
				{
					GetMajorProjectInfo();
					WorksheetDataPOST(params, url);
				}
			}
			else
			{
				try
				{
					params = "method=row.content.set&worksheet_name=" + sheet_drawing_number + "&row=1&column_array=[1,7,11]";
					params += "&data_array=[\"" + encoded_heading + "\",\"PURCHASING\",\"WAREHOUSE\"]";
					WorksheetDataPOST(params, url);
				}
				catch(InvalidOauthException err)
				{
					GetMajorProjectInfo();
					WorksheetDataPOST(params, url);
				}

				try
				{
					params = "method=row.content.set&worksheet_name=" + sheet_drawing_number + "&row=2&column_array=[1,2,3,4,5,6,7,8,9,10,11,12,13,14]";
					params += ZSHEETS_HEADERS + ZSHEETS_HEADERS_ORDERING;
					WorksheetDataPOST(params, url);
				}
				catch(InvalidOauthException err)
				{
					GetMajorProjectInfo();
					WorksheetDataPOST(params, url);
				}
			}
			try
			{
				params = "method=worksheet.jsondata.append&worksheet_name=" + sheet_drawing_number + "&header_row=2&json_data=" + UrlEncodeString(sheet_data.toString());
				WorksheetDataPOST(params, url);
			}
			catch(InvalidOauthException err)
			{
				GetMajorProjectInfo();
				WorksheetDataPOST(params, url);
			}
		}
		catch(Exception err)
		{
			err.printStackTrace();
			throw err;
		}
	}

	public static void WorksheetDataPOST(String params, String url) throws InvalidOauthException, Exception
	{
		HttpResponse<String> http_response = null;
		HttpRequest http_request = HttpRequest.newBuilder()
		.uri(URI.create(url))
		.header("Authorization", "Zoho-oauthtoken " + Main.ACCESS_TOKEN)
		.header("Content-Type", "application/x-www-form-urlencoded")
		.POST(HttpRequest.BodyPublishers.ofString(params))
		.build();
		http_response = HttpClient.newHttpClient().send(http_request, HttpResponse.BodyHandlers.ofString());
		ObjectMapper response_map = new ObjectMapper();

		if(http_response.statusCode() >= 300)
		{
			System.out.println("POST of Worksheet data failed - " + Integer.toString(http_response.statusCode()));
			JsonNode json_node = response_map.readTree(http_response.body());
			String err_type = json_node.get("error").get("title").asText();
			if (err_type.equals(OauthErrorTitle))
			{
				throw new InvalidOauthException(OauthReturn);
			}
			throw new Exception(Integer.toString(http_response.statusCode()));
		}
	}

	public static void ZohoMarkSheetReady(String resource_id) throws InvalidOauthException, Exception
	{
		Map<String, String> api_atts = new HashMap<>();
		Map<String, Object> api_params = new HashMap<>();
		Map<String, Object> api_data = new HashMap<>();

		api_atts.put("status", "1");
		api_params.put("attributes", api_atts);
		api_params.put("type", "files");
		api_data.put("data", api_params);
		String url = "https://www.zohoapis.com.au/workdrive/api/v1/files/" + resource_id;

		HttpResponse<String> http_response = null;
		ObjectMapper json_map = new ObjectMapper();
		String json_string = json_map.writeValueAsString(api_data);

		HttpRequest http_request = HttpRequest.newBuilder()
		.uri(URI.create(url))
		.header("Authorization", "Zoho-oauthtoken " + Main.ACCESS_TOKEN)
		.header("Accept", "application/vnd.api+json")
		.method("PATCH", HttpRequest.BodyPublishers.ofString(json_string))
		.build();

		http_response = HttpClient.newHttpClient().send(http_request, HttpResponse.BodyHandlers.ofString());
		if(http_response.statusCode() >= 300)
		{
			throw new Exception(Integer.toString(http_response.statusCode()));
		}
	}

	/** ZOHO WORKDRIVE **/
	public static String ZohoWorkdrivePost(Map<String,Object> params, String url) throws InvalidOauthException, Exception
	{
		HttpResponse<String> http_response = null;
		ObjectMapper json_map = new ObjectMapper();
		String json_string = json_map.writeValueAsString(params);
		HttpRequest http_request = HttpRequest.newBuilder()
		.uri(URI.create(url))
		.header("Authorization", "Zoho-oauthtoken " + Main.ACCESS_TOKEN)
		.header("Accept", "application/vnd.api+json")
		.POST(HttpRequest.BodyPublishers.ofString(json_string.toString()))
		.build();

		http_response = HttpClient.newHttpClient().send(http_request, HttpResponse.BodyHandlers.ofString());

		ObjectMapper response_map = new ObjectMapper();
		JsonNode json_node = response_map.readTree(http_response.body());

		if(http_response.statusCode() >= 300)
		{
			String err_type = json_node.get("error").get("title").asText();
			if (err_type.equals(OauthErrorTitle))
			{
				throw new InvalidOauthException(OauthReturn);
			}
			throw new Exception(Integer.toString(http_response.statusCode()));
		}

		return (json_node.get("data").get("id").asText());
	}

	public static JsonNode ZohoWorkdriveGetTeamFolder(String folder_name) throws InvalidOauthException, Exception
	{
		String search_params = "/records?search%5Ball%5D=" + UrlEncodeString(folder_name);
		String url = "https://www.zohoapis.com.au/workdrive/api/v1/teams/" + Main.WORKDRIVE_ID + search_params;

		HttpResponse<String> http_response = null;
		HttpRequest http_request = HttpRequest.newBuilder()
		.uri(URI.create(url))
		.header("Authorization", "Zoho-oauthtoken " + Main.ACCESS_TOKEN)
		.header("Accept", "application/vnd.api+json")
		.GET()
		.build();

		http_response = HttpClient.newHttpClient().send(http_request, HttpResponse.BodyHandlers.ofString());

		ObjectMapper response_map = new ObjectMapper();
		JsonNode json_node = response_map.readTree(http_response.body());
		if(http_response.statusCode() >= 300)
		{
			String err_type = json_node.get("error").get("title").asText();
			if (err_type.equals(OauthErrorTitle))
			{
				throw new InvalidOauthException(OauthReturn);
			}
			throw new Exception(Integer.toString(http_response.statusCode()));
		}
		return (json_node);
	}
}
