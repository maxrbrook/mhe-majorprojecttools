package zohorest;

import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.JButton;
import javax.swing.ListSelectionModel;

import java.util.ArrayList;
import java.util.HashMap;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.FlowLayout;


public class DialogSelectProject extends DialogParent
{
	int PROJECT_SELECTION = -1;
	JList<String> PROJECT_DISPLAY_LIST;
	JTextField REFINE_SEARCH_TEXTFIELD;

	private String[] project_id_arr;
	private String[] project_name_arr;
	/* key = new index, value = og index */
	private HashMap<Integer,Integer> project_filtered_hashmap = new HashMap<>();

	public DialogSelectProject(ArrayList<ArrayList<String>> l)
	{
		super("Select Project");
		this.project_id_arr = l.get(0).toArray(String[]::new);
		this.project_name_arr = l.get(1).toArray(String[]::new);
		super.setLayout(new FlowLayout());
		super.setSize(450, 450);
		super.add(this.SearchField());
		super.add(this.RefineSearchBtn());
		super.add(new JScrollPane(this.CreateList()));
		super.add(this.SelectProjectBtn());
		super.setVisible(true);
	}

	private JTextField SearchField()
	{
		REFINE_SEARCH_TEXTFIELD = new JTextField(30);
		return REFINE_SEARCH_TEXTFIELD;
	}

	private JButton RefineSearchBtn()
	{
		JButton refine_list_btn = new JButton("Refine Search");
		refine_list_btn.addActionListener(e -> {
			String refine_string = this.REFINE_SEARCH_TEXTFIELD.getText();
			ArrayList<String> new_project_list = new ArrayList<>();
			int j = 0;

			if(!project_filtered_hashmap.isEmpty())
			{
				project_filtered_hashmap.clear();
			}

			if (refine_string.length() == 0)
			{
				PROJECT_DISPLAY_LIST.setListData(project_name_arr);
				return;
			}

			for(int i = 0; i < project_name_arr.length; i++)
			{
				if (project_name_arr[i].contains(refine_string))
				{
					project_filtered_hashmap.put(j++, i);
					new_project_list.add(project_name_arr[i]);
				}
			}
			PROJECT_DISPLAY_LIST.setListData(new_project_list.toArray(String[]::new));
		});
		return refine_list_btn;
	}

	private JButton SelectProjectBtn()
	{
		JButton upload_btn = new JButton("Select Project");
		upload_btn.addActionListener(e -> {
			int selected_item = this.PROJECT_DISPLAY_LIST.getSelectedIndex();
			int n = JOptionPane.showConfirmDialog(new JFrame(), "Select " + this.PROJECT_DISPLAY_LIST.getSelectedValue() + "?", "Select Project", JOptionPane.YES_NO_OPTION);
			if(n == JOptionPane.YES_OPTION)
			{
				if(project_filtered_hashmap.isEmpty())
				{
					PROJECT_SELECTION = selected_item;
				}
				else
				{
					// the Key is the filtered list index | returns the project_name_arrs value (index)
					PROJECT_SELECTION = project_filtered_hashmap.get(selected_item);
				}
				super.UserConfirmsDialog();
				return;
			}
		});
		return upload_btn;
	}

	private JList<String> CreateList()
	{
		PROJECT_DISPLAY_LIST = new JList<String>(this.project_name_arr);
		PROJECT_DISPLAY_LIST.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		PROJECT_DISPLAY_LIST.setLayoutOrientation(JList.VERTICAL);
		PROJECT_DISPLAY_LIST.setVisibleRowCount(16);

		return PROJECT_DISPLAY_LIST;
	}

	public String ReturnProjectId()
	{
		return this.project_id_arr[PROJECT_SELECTION];
	}
}
