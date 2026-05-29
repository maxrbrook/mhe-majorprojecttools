package zohorest;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

import java.time.Instant;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient;

import java.net.URI;
import java.net.ConnectException;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.*;

import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.ProgressMonitor;

public class ZohoApiMethods
{
	private String LOCALHOST_URL;
	private String ACCESS_TOKEN, PORTAL_ID, PROJECT_LAYOUT_ID, TASK_LAYOUT_ID, WORKDRIVE_ID, BLUEPRINT_ID, MACHINE_TRANSITION_ID, FAB_TRANSITION_ID;
	private String OAUTH_ERR = "INVALID_OAUTHTOKEN";

	private ProgressMonitor pm;

	protected boolean SERVER_CONNECTED = false;
	protected Instant LAST_TIME_CALLED;

	public ZohoApiMethods(String url)
	{
		this.LOCALHOST_URL = url;
	}

	protected boolean GetProjectInfo()
	{
		try
		{
			this.TestConnection();
			this.GetMajorProjectInfo();
		}
		catch (ConnectException err)
		{
			this.CallErrorMsg(String.format("ERROR: Cannot connect to server @%s", this.LOCALHOST_URL));
			return (false);
		}
		catch (Exception err)
		{
			this.CallErrorMsg(String.format("ERROR: Unexpected Error"));
			return (false);
		}
		return (true);
	}
	/**
	*
	* Public Zoho Project Methods
	*
	**/
	protected String PostTask(String project_id, String task_name, String description, String ...ids) throws Exception
	{
		Map<String, String> params = new HashMap<>();
		String url = this.ProjectsUrlBuilder();

		url += "/projects/" + project_id + "/tasks";

		params.put("name", task_name);
		params.put("description", description);
		if (ids.length == 1)
		{
			params.put("tasklist", String.format("{id:%s}", ids[0]));
		}
		if (ids.length == 2)
		{
			params.put("parental_info", String.format("{parent_task_id:%s}", ids[1]));
		}
		return (this.ZohoProjectsItemPost(params, url));
	}

	protected String PostTasklist(String project_id, String tasklist_name) throws Exception
	{
		Map<String, String> params = new HashMap<>();
		String url = this.ProjectsUrlBuilder();

		url += "/projects/" + project_id + "/tasklists";
		params.put("name", tasklist_name);

		return (this.ZohoProjectsItemPost(params, url));
	}

	protected String PostProject(String[] pd, String project_name) throws Exception
	{
		Map<String, String> params = new HashMap<>();
		String url = this.ProjectsUrlBuilder();
		url += "/projects";
		/*
		pd[0] project title
		pd[1] job number
		pd[2] client
		pd[3] project description
		*/
		params.put("name", String.format("%s | %s - %s", pd[1], pd[2], project_name));
		params.put("description", pd[3]);
		params.put("layout", String.format("{id:%s}", this.PROJECT_LAYOUT_ID));
		params.put("sub_module_settings", String.format("{sub_module_layouts_configuration:{tasks:{id:%s,is_copy_as_private:false}}}", this.TASK_LAYOUT_ID));

		return (this.ZohoProjectsItemPost(params, url));
	}

	protected ArrayList<ArrayList<String>> GetProjectsWithIdName() throws Exception
	{
		ArrayList<ArrayList<String>> rtn = new ArrayList<ArrayList<String>>();
		ArrayList<String> item_id_list = new ArrayList<>();
		ArrayList<String> item_name_list = new ArrayList<>();
		Iterator<JsonNode> elements;
		JsonNode item;

		String url = this.ProjectsUrlBuilder();
		url += "/projects";

		elements = this.ZohoProjectsItemGet(url).elements();
		while (elements.hasNext())
		{
			item = elements.next();
			if(item.get("project_type").asText().matches("active"))
			{
				item_id_list.add(item.get("id").asText());
				item_name_list.add(item.get("name").asText());
			}
		}
		rtn.add(item_id_list);
		rtn.add(item_name_list);

		return (rtn);
	}

	protected ArrayList<ArrayList<String>> GetTasklistsWithIdName(String project_id) throws Exception
	{
		ArrayList<ArrayList<String>> rtn = new ArrayList<ArrayList<String>>();
		ArrayList<String> item_id_list = new ArrayList<>();
		ArrayList<String> item_name_list = new ArrayList<>();
		Iterator<JsonNode> elements;
		JsonNode item;

		String url = this.ProjectsUrlBuilder();
		url += String.format("/projects/%s/tasklists", project_id);

		elements = this.ZohoProjectsItemGet(url).get("tasklists").elements();
		while (elements.hasNext())
		{
			item = elements.next();
			item_id_list.add(item.get("id").asText());
			item_name_list.add(item.get("name").asText());
		}
		rtn.add(item_id_list);
		rtn.add(item_name_list);

		return (rtn);
	}

	protected void CreateProgressMonitor(int i)
	{
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame();
			pm = new ProgressMonitor(frame, "Uploading Project:", "Completed: 0/" + i, 0, i);
			frame.setAlwaysOnTop(true);
		});
	}
	protected void CloseProgressMonitor()
	{
		SwingUtilities.invokeLater(() -> {
			pm.close();
		});
	}

	protected boolean UpdateProgress(int i)
	{
		if (pm.isCanceled())
		{
			return true;
		}
		SwingUtilities.invokeLater(() -> {
			pm.setProgress(i);
			pm.setNote("Completed: " + i + "/" + pm.getMaximum());
		});
		return false;
	}

	protected void CallWarningMsg(String err, String title)
	{
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame();
			frame.setAlwaysOnTop(true);
			JOptionPane.showMessageDialog(frame, err, title, JOptionPane.ERROR_MESSAGE);
		});
		return;
	}

	protected void CallErrorMsg(String err)
	{
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame();
			frame.setAlwaysOnTop(true);
			JOptionPane.showMessageDialog(frame, err, "Fatal Error Occured", JOptionPane.ERROR_MESSAGE);
		});
		return;
	}

	/** URL getters **/
	private String ProjectsUrlBuilder()
	{
		return "https://projectsapi.zoho.com.au/api/v3/portal/" + this.PORTAL_ID;
	}

	private String ZohoProjectsItemPost(Map<String,String> params, String url) throws Exception
	{
		/* Post an item to Zoho */
		HttpResponse<String> response = null;
		ObjectMapper response_map = new ObjectMapper();
		ObjectMapper json_map = new ObjectMapper();

		String json_string = json_map.writeValueAsString(params);

		HttpRequest request = HttpRequest.newBuilder()
		.uri(URI.create(url))
		.header("Authorization", "Zoho-oauthtoken " + this.ACCESS_TOKEN)
		.header("Content-Type", "application/json")
		.POST(HttpRequest.BodyPublishers.ofString(json_string))
		.build();

		response = HttpClient.newHttpClient()
		.send(request, HttpResponse.BodyHandlers.ofString());

		JsonNode json_node = response_map.readTree(response.body());
		if(response.statusCode() >= 300)
		{
			String err_type = json_node.get("error").get("title").asText();
			if (err_type.equals(OAUTH_ERR))
			{
				/* return a 401 Error */
				throw new ConnectException();
			}
			System.out.println(json_node);
			throw new Exception();
		}
		return (json_node.get("id").asText());
	}

	private JsonNode ZohoProjectsItemGet(String url) throws Exception
	{
		/** Get the List of Projects under the Portal ID **/
		HttpResponse<String> response = null;
		ObjectMapper response_map = new ObjectMapper();

		HttpRequest request = HttpRequest.newBuilder()
		.uri(URI.create(url))
		.header("Authorization", "Zoho-oauthtoken " + this.ACCESS_TOKEN)
		.GET()
		.build();

		response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		JsonNode json_node = response_map.readTree(response.body());
		if(response.statusCode() >= 300)
		{
			String err_type = json_node.get("error").get("title").asText();
			if (err_type.equals(OAUTH_ERR))
			{
				/* return a 401 Error */
				throw new ConnectException();
			}
			throw new Exception();
		}
		return (json_node);
	}

	private boolean TestConnection() throws Exception
	{
		/* Check if the URL is returning a response */
		HttpResponse<String> response = null;
		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(this.LOCALHOST_URL))
			.GET()
			.build();

		response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		if(response.statusCode() >= 300)
		{
			throw new ConnectException();
		}
		return (true);
	}

	private boolean GetMajorProjectInfo() throws Exception
	{
		HttpResponse<String> response = null;

		HttpRequest request = HttpRequest.newBuilder()
		.uri(URI.create(this.LOCALHOST_URL + "/major"))
		.GET()
		.build();

		response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		if(response.statusCode() >= 300)
		{
			throw new ConnectException();
		}

		ObjectMapper response_map = new ObjectMapper();
		JsonNode json_node = response_map.readTree(response.body());

		this.ACCESS_TOKEN = json_node.get("access_token").asText();
		this.PORTAL_ID = json_node.get("portal_id").asText();
		this.PROJECT_LAYOUT_ID = json_node.get("project_layout_id").asText();
		this.TASK_LAYOUT_ID = json_node.get("task_layout_id").asText();
		this.WORKDRIVE_ID = json_node.get("workdrive_team_id").asText();
		this.BLUEPRINT_ID = json_node.get("blueprint_id").asText();
		this.MACHINE_TRANSITION_ID = json_node.get("machine_transition_id").asText();
		this.FAB_TRANSITION_ID = json_node.get("fab_transition_id").asText();

		this.LAST_TIME_CALLED = Instant.now();
		System.out.println(this.LAST_TIME_CALLED);
		return (true);
	}
}
