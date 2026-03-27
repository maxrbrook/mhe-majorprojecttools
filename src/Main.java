import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.filechooser.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.*;
/*
	Handles methods for creating the Window and the main method for the tool
	Contains AbstractActions for adding interaction to the buttons.
*/
public class Main
{
	static Main new_major_project;
	static boolean plates_as_to_make = false;
	static File existing_project_selected = null;

	JtableAssembly assembly_table;
	JtablePurchasing purchasing_table;
	ProjectTab new_project_tab;
	JLabel file_path_label;

	public static String ZS_ITEM_NUMBER = "ITEM NUMBER";
	public static String ZS_DESCRIPTION = "DESCRIPTION";
	public static String ZS_VAULT_QTY = "VAULT QTY";
	public static String ZS_ORDER_QTY = "TO ORDER QTY";
	public static String ZS_PURCHASE_TYPE = "PURCHASE TYPE";
	public static String ZS_WHERE_USED = "WHERE USED";
	public static String ZS_NOTES = "NOTES";

	public static String ACCESS_TOKEN = new String();
	public static String PORTAL_ID = new String();
	public static String WORKDRIVE_ID = new String();
	public static String PROJECT_LAYOUT_ID = new String();
	public static String TASK_LAYOUT_ID = new String();
	public static String BLUEPRINT_ID = new String();
	public static String MACHINE_TRANSITION_ID = new String();
	public static String FAB_TRANSITION_ID = new String();

	public static String localhost_url = new String();

	private void SaveFileDialog()
	{
		JFileChooser save_file = new JFileChooser()
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
			}
		};
		if(existing_project_selected != null)
		{//if a project has been selected - get the file Directory and write it to the system
			save_file.setCurrentDirectory(existing_project_selected);
			save_file.setSelectedFile(existing_project_selected);
		}
		else
		{
			save_file.setCurrentDirectory(FileSystemView.getFileSystemView().getHomeDirectory());
			save_file.setSelectedFile(new File(new_project_tab.ReturnProjectTitle() + ".tsv"));
		}
		save_file.setFileFilter(new FileNameExtensionFilter("Tab Seperated Values", ".tsv", ".TSV"));
		if(save_file.showSaveDialog(null) == save_file.APPROVE_OPTION)
		{
			existing_project_selected = save_file.getSelectedFile();
			FileIO.SaveProjectToFile(new_project_tab, assembly_table, purchasing_table, existing_project_selected);
		}
	}

	private boolean SaveProjectPrompt()
	{
		String project_title = new_project_tab.ReturnProjectTitle();
		String description = new_project_tab.ReturnDescription();
		if((assembly_table.AssyRowCount() == 0) && (project_title.isEmpty()) && (description.isEmpty()))
		{
			return true;
		}
		JFrame frame = new JFrame();
		frame.setAlwaysOnTop(true);
		int n = JOptionPane.showConfirmDialog(frame, "Do you wish to save the current project?", "Save Project?", JOptionPane.YES_NO_CANCEL_OPTION);
		if(n == JOptionPane.YES_OPTION)
		{
			SaveFileDialog();
			return true;
		}
		else if(n == JOptionPane.CANCEL_OPTION || n == JOptionPane.CLOSED_OPTION)
		{
			/* if the user cancels the selection */
			return false;
		}
		return true;
	}

	private void URLTestFailed()
	{
		JFrame frame = new JFrame();
		frame.setAlwaysOnTop(true);
		String err_msg = "ERROR - " + localhost_url + " Cannot connect to the Oauth server (err_code: 0001), please contact System Administration for assistance";
		JOptionPane.showMessageDialog(frame, err_msg, "URL failed to retrieve credentials", JOptionPane.ERROR_MESSAGE);
	}

	//
	// -- AbstractAction Subclasses
	//
	public class IncreaseJTableFontSize extends AbstractAction
	{
		public IncreaseJTableFontSize()
		{
			super("Zoom In");
		}
		public void actionPerformed(ActionEvent e)
		{
			assembly_table.IncreaseFont();
			purchasing_table.IncreaseFont();
		}
	}
	public class DecreaseJTableFontSize extends AbstractAction
	{
		public DecreaseJTableFontSize()
		{
			super("Zoom Out");
		}
		public void actionPerformed(ActionEvent e)
		{
			assembly_table.DecreaseFont();
			purchasing_table.DecreaseFont();
		}
	}
	public class ResetJTableFontSize extends AbstractAction
	{
		public ResetJTableFontSize()
		{
			super("Reset Zoom");
		}
		public void actionPerformed(ActionEvent e)
		{
			assembly_table.ResetFont();
			purchasing_table.ResetFont();
		}
	}

	public class UploadProject extends AbstractAction
	{
		public UploadProject()
		{
			super("Upload Project to Zoho");
		}
		public void actionPerformed(ActionEvent e)
		{
			JFrame frame = new JFrame();
			frame.setAlwaysOnTop(true);
			String project_title = new_project_tab.ReturnProjectTitle();
			String job_num = new_project_tab.ReturnJobNumber();
			if(assembly_table.AssyRowCount() == 0)
			{
				JOptionPane.showMessageDialog(frame, "ERROR - No File Found", "No File Selected", JOptionPane.ERROR_MESSAGE);
			}
			else if(project_title == null || project_title.equals(""))
			{
				JOptionPane.showMessageDialog(frame, "ERROR - Mising Project Title for Upload", "Missing Project Info", JOptionPane.ERROR_MESSAGE);
			}
			else if(job_num == null || job_num.equals(""))
			{
				JOptionPane.showMessageDialog(frame, "ERROR - Mising Job Number for Upload", "Missing Project Info", JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				int n = JOptionPane.showConfirmDialog(frame, "Are you sure you want to proceed with the upload?", "Upload Project?", JOptionPane.YES_NO_OPTION);
				if(n == JOptionPane.YES_OPTION)
				{
					try
					{
						ApiMethods.GetMajorProjectInfo();
					}
					catch(Exception err)
					{
						err.printStackTrace();
						JOptionPane.showMessageDialog(frame, "ERROR - System failed to retrieve credentials", "Missing credentials", JOptionPane.ERROR_MESSAGE);
						return;
					}

					RunUpload upload_project = new RunUpload(
						project_title,
						job_num,
						new_project_tab.ReturnClient(),
						new_project_tab.ReturnSalesOrder(),
						new_project_tab.ReturnCustomerOrder(),
						new_project_tab.ReturnDescription(),
						assembly_table.GetAssyTableData(),
						purchasing_table.GetPurchaseTableData(),
						null,//project_id
						null);//Puchasing folder id
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
	public class TogglePlatesAsTasks extends AbstractAction
	{
		public TogglePlatesAsTasks()
		{
			super("Set Plates as [TO MAKE] Items?");
		}
		public void actionPerformed(ActionEvent e)
		{
			plates_as_to_make = !plates_as_to_make;
			int assy_count = assembly_table.AssyRowCount();
			if(assy_count > 0)
			{
				String str_AssyType = new String();
				String order_type = new String();
				int to_order_column = 5;
				int assy_column = 9;
				for(int i = 0; i < assy_count; i++)
				{
					order_type = assembly_table.AssyValueAt(i, 5);
					if (order_type.equalsIgnoreCase("DO NOT MAKE"))
					{
						continue;
					}
					if(plates_as_to_make)
					{
						assembly_table.SetAssyVal("TO MAKE", i, to_order_column);
						continue;
					}
					str_AssyType = assembly_table.AssyValueAt(i, assy_column);
					if(str_AssyType.equalsIgnoreCase("Plate Part"))
					{
						assembly_table.GetAssyTableData().setValueAt("ORDER/OUTSOURCE", i, to_order_column);
						continue;
					}
					assembly_table.GetAssyTableData().setValueAt("TO MAKE", i, to_order_column);
				}
			}
		}
	}
	public class NewProject extends AbstractAction
	{
		public NewProject()
		{
			super("New Project");
		}
		public void actionPerformed(ActionEvent e)
		{
			if(SaveProjectPrompt())
			{
				//true -> not cancelled
				assembly_table.ClearData();
				purchasing_table.ClearData();
				new_project_tab.ClearAllText();
				file_path_label.setText("No File Selected");
			}
		}
	}
	public class OpenProject extends AbstractAction
	{
		public OpenProject()
		{
			super("Open Project");
		}
		public void actionPerformed(ActionEvent e)
		{
			if(SaveProjectPrompt())
			{
				JFileChooser file_to_open = new JFileChooser();
				if(existing_project_selected != null)
				{
					file_to_open.setCurrentDirectory(existing_project_selected);
				}
				else
				{
					file_to_open.setCurrentDirectory(FileSystemView.getFileSystemView().getHomeDirectory());
				}
				file_to_open.setFileFilter(new FileNameExtensionFilter("Tab Seperated Values", "tsv", "TSV"));
				if(file_to_open.showOpenDialog(null) == file_to_open.APPROVE_OPTION)
				{
					file_path_label.setText("Project Opened: " + file_to_open.getSelectedFile().getAbsolutePath());
					existing_project_selected = file_to_open.getSelectedFile();

					FileIO.OpenProjectFile(new_project_tab, assembly_table, purchasing_table, existing_project_selected);
				}
			}
		}
	}
	public class SaveProject extends AbstractAction
	{
		public SaveProject()
		{
			super("Save Project");
		}
		public void actionPerformed(ActionEvent e)
		{
			SaveFileDialog();
		}
	}
	public class ExitProgram extends AbstractAction
	{
		public ExitProgram()
		{
			super("Exit");
		}
		public void actionPerformed(ActionEvent e)
		{
			if(SaveProjectPrompt())
			{
				System.exit(0);
			}
		}
	}

	private JMenuBar CreateMenuBar()
	{
		JMenuBar manu_bar = new JMenuBar();
		JMenu menu_file = new JMenu("File");
		JMenu edit_menu = new JMenu("Edit");
		JMenu view_menu = new JMenu("View");
		JMenu project_menu = new JMenu("Project");

		/** File Menu **/
		JMenuItem new_project = new JMenuItem(new NewProject());
		JMenuItem open_project = new JMenuItem(new OpenProject());
		JMenuItem save_project = new JMenuItem(new SaveProject());
		JMenuItem exit_project = new JMenuItem(new ExitProgram());
		new_project.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		open_project.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		save_project.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		exit_project.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));

		menu_file.add(new_project);//New Project
		menu_file.add(open_project);//Open Project
		menu_file.add(new JSeparator());
		menu_file.add(save_project);//Save Project
		menu_file.add(new JSeparator());

		menu_file.add(new JMenuItem(new ImportBomFile(plates_as_to_make, new_project_tab.ReturnSpinnerValue(),file_path_label, assembly_table,purchasing_table)));
		menu_file.add(new JSeparator());
		menu_file.add(exit_project);//Exit Program

		/** Edit Menu **/
		edit_menu.add(new JCheckBoxMenuItem(new TogglePlatesAsTasks()));

		/** View Menu **/
		JMenuItem increase_font = new JMenuItem(new IncreaseJTableFontSize());
		JMenuItem decrease_font = new JMenuItem(new DecreaseJTableFontSize());
		increase_font.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, ActionEvent.CTRL_MASK));
		decrease_font.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, ActionEvent.CTRL_MASK));
		view_menu.add(increase_font);
		view_menu.add(decrease_font);
		view_menu.add(new ResetJTableFontSize());

		/** Project Menu **/
		project_menu.add(new JMenuItem(new UploadProject()));//Upload the Project to Zoho
		project_menu.add(new JSeparator());
		project_menu.add(new JMenuItem(new AppendToExistingProject(assembly_table, purchasing_table)));//Upload the Project to Zoho

		manu_bar.add(menu_file);
		manu_bar.add(edit_menu);
		manu_bar.add(view_menu);
		manu_bar.add(project_menu);

		return manu_bar;
	}

	private JTabbedPane CreateTabbedPane()
	{
		JTabbedPane tabbed_panes = new JTabbedPane();
		JPanel project_panel = new JPanel(new BorderLayout());
		JPanel assembly_panel = new JPanel(new BorderLayout());
		JPanel purchasing_panel = new JPanel(new BorderLayout());

		assembly_table = new JtableAssembly();
		purchasing_table = new JtablePurchasing();
		new_project_tab = new ProjectTab();
		assembly_panel.add(new JScrollPane(assembly_table.CreateAssyTable()), BorderLayout.CENTER);
		purchasing_panel.add(new JScrollPane(purchasing_table.CreatePurchasingTable()), BorderLayout.CENTER);
		project_panel.add(new JScrollPane(new_project_tab.NewProjectTab(assembly_table, purchasing_table)), BorderLayout.CENTER);

		tabbed_panes.add("Project", project_panel);
		tabbed_panes.add("Purchasing", purchasing_panel);
		tabbed_panes.add("Assembly", assembly_panel);

		return tabbed_panes;
	}

	private JLabel CreateFileNameLabel()
	{
		file_path_label = new JLabel("No File Selected");
		file_path_label.setForeground(Color.BLUE);
		file_path_label.setFont(new Font("Arial", Font.BOLD, 14));
		file_path_label.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));

		return file_path_label;
	}

	private void CreateGUI()
	{
		JFrame new_frame = new JFrame("Major Project Tools");
		new_frame.setLayout(new BorderLayout());
		new_frame.setSize(550, 600); //sets the default window size
		new_frame.setExtendedState(JFrame.MAXIMIZED_BOTH); //makes the window open as a maximised application
		new_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //exits the program when the JFrame is exited
		new_frame.setVisible(true);
		//
		// Add Functions to the Window
		//
		new_frame.add(new_major_project.CreateTabbedPane(), BorderLayout.CENTER);
		new_frame.add(new_major_project.CreateFileNameLabel(), BorderLayout.PAGE_START);
		new_frame.setJMenuBar(new_major_project.CreateMenuBar());
	}

	public static void main(String[] args)
	{
		//Generate the Window
		SwingUtilities.invokeLater(()->{
			new_major_project = new Main();
			boolean test_success = false;
			if (args.length == 0)
			{
				localhost_url = "http://192.168.16.197:800";
			}
			else
			{
				localhost_url = args[0];
			}
			try
			{
				test_success = ApiMethods.TestValidURL();
				System.out.println("Test successs");
			}
			catch(Exception err)
			{
				test_success = false;
			}
			if (!test_success)
			{
				new_major_project.URLTestFailed();
				Runtime.getRuntime().exit(0);
			}
			new_major_project.CreateGUI();
		});
	}
}
