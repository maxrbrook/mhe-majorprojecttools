import tables.AssemblyTable;
import tables.PurchasingTable;
import btnfunctions.*;
import projectclasses.*;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

class Main extends JFrame
{
	AssemblyTable ASSY_TABLE;
	PurchasingTable PURCHASE_TABLE;
	ProjectTab PROJECT_PANEL;

	static String LOCALHOST_URL = "http://192.168.16.197:800";

	boolean PLATES_TOGGLE;
	JLabel FILE_DIR_LBL;

	/** Class Constructor **/
	public Main()
	{
		super("Major Project Tools");
		this.SetJFrameOptions();
		ASSY_TABLE = new AssemblyTable();
		PURCHASE_TABLE = new PurchasingTable();
		super.add(this.CreateTabbedPane(), BorderLayout.CENTER);
		super.add(this.CreateDirLbl(), BorderLayout.PAGE_START);
		super.setJMenuBar(this.CreateMenuBar());

		super.setVisible(true);//make the frame visible
	}

	/** The Main function to create the window **/
	private void SetJFrameOptions()
	{
		super.setLayout(new BorderLayout());
		super.setSize(550, 600);
		super.setExtendedState(JFrame.MAXIMIZED_BOTH);
		super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private JTabbedPane CreateTabbedPane()
	{
		JTabbedPane tabbed_panes = new JTabbedPane();
		PROJECT_PANEL = new ProjectTab();
		this.PROJECT_PANEL.GetSpinner().addChangeListener(new SpinnerListener(ASSY_TABLE, PURCHASE_TABLE));
		JPanel assembly_panel = new JPanel(new BorderLayout());
		JPanel purchasing_panel = new JPanel(new BorderLayout());

		assembly_panel.add(new JScrollPane(ASSY_TABLE), BorderLayout.CENTER);
		purchasing_panel.add(new JScrollPane(PURCHASE_TABLE), BorderLayout.CENTER);

		tabbed_panes.add("Project", PROJECT_PANEL);
		tabbed_panes.add("Purchasing", purchasing_panel);
		tabbed_panes.add("Assembly", assembly_panel);

		return tabbed_panes;
	}

	private JMenuBar CreateMenuBar()
	{
		JMenuBar new_menu = new JMenuBar();
		new_menu.add(this.FileMenu());
		new_menu.add(this.EditMenu());
		new_menu.add(this.ViewMenu());

		return new_menu;
	}

	private JMenu FileMenu()
	{
		JMenu file_menu = new JMenu("File");
		JMenuItem new_project = new JMenuItem("New Project");
		new_project.addActionListener(e -> {
			if (this.DialogSavePrompt())
			{
				this.ASSY_TABLE.ClearData();
				this.PURCHASE_TABLE.ClearData();
				this.PROJECT_PANEL.SetProjetTitle("");
				this.PROJECT_PANEL.SetJobNumber("");
				this.PROJECT_PANEL.SetClient("");
				this.PROJECT_PANEL.SetProjectDescription("");
				this.PROJECT_PANEL.SetSpinnerVal(1);
				return;
			}
			//Run Save and then clear
		});
		JMenuItem open_project = new JMenuItem("Open Project");
		open_project.addActionListener(e -> {
			if (this.DialogSavePrompt())
			{
				new BtnProjectOpen(ASSY_TABLE, PURCHASE_TABLE, PROJECT_PANEL, FILE_DIR_LBL);
			}
		});
		JMenuItem save_project = new JMenuItem("Save Project");
		save_project.addActionListener(e -> {
			new BtnProjectSave(ASSY_TABLE, PURCHASE_TABLE, PROJECT_PANEL.GetProjectDetails(), PROJECT_PANEL.GetSpinner());
		});
		JMenuItem exit_project = new JMenuItem("Exit Project");
		exit_project.addActionListener(e -> {
			if (this.DialogSavePrompt())
			{
				System.exit(0);
			}
		});
		JMenuItem import_project = new JMenuItem("Import BOM");
		import_project.addActionListener(e -> {
			if (this.DialogSavePrompt())
			{
				new BtnBomImport(false, PROJECT_PANEL.GetSpinner(), FILE_DIR_LBL, ASSY_TABLE, PURCHASE_TABLE);
			}
		});
		JMenuItem upload_project = new JMenuItem("Upload Project");
		upload_project.addActionListener(e -> {
			if (this.DialogSavePrompt())
			{
				new BtnProjectUpload(ASSY_TABLE, PURCHASE_TABLE, LOCALHOST_URL, PROJECT_PANEL.GetProjectDetails());
			}
		});

		new_project.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		open_project.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		save_project.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		exit_project.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		import_project.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		upload_project.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));

		file_menu.add(new_project);
		file_menu.add(open_project);
		file_menu.add(new JSeparator());
		file_menu.add(save_project);
		file_menu.add(new JSeparator());
		file_menu.add(import_project);//new JMenuItem(new BtnBomImport(false, PROJECT_PANEL.GetSpinner(), FILE_DIR_LBL, ASSY_TABLE, PURCHASE_TABLE)));
		file_menu.add(new JSeparator());
		file_menu.add(upload_project);
		file_menu.add(new JSeparator());
		file_menu.add(exit_project);

		return file_menu;
	}

	private JMenu EditMenu()
	{
		JMenu edit_menu = new JMenu("Edit");
		edit_menu.add(new JCheckBoxMenuItem("Toggle Plate Items as To Make"));

		return edit_menu;
	}

	private JMenu ViewMenu()
	{
		JMenu view_menu = new JMenu("View");
		JMenuItem plus_font = new JMenuItem("Zoom In");
		plus_font.addActionListener(e -> {
			ASSY_TABLE.ZoomIn();
			PURCHASE_TABLE.ZoomIn();
		});
		JMenuItem sub_font = new JMenuItem("Zoom Out");
		sub_font.addActionListener(e -> {
			ASSY_TABLE.ZoomOut();
			PURCHASE_TABLE.ZoomOut();
		});
		JMenuItem reset_font = new JMenuItem("Reset Zoom");
		reset_font.addActionListener(e -> {
			ASSY_TABLE.ResetZoom();
			PURCHASE_TABLE.ResetZoom();
		});

		plus_font.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, ActionEvent.CTRL_MASK));
		sub_font.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, ActionEvent.CTRL_MASK));

		view_menu.add(plus_font);
		view_menu.add(sub_font);
		view_menu.add(reset_font);

		return view_menu;
	}

	private JLabel CreateDirLbl()
	{
		FILE_DIR_LBL = new JLabel("No File Selected");
		FILE_DIR_LBL.setForeground(Color.BLUE);
		FILE_DIR_LBL.setFont(new Font("Dialog", Font.BOLD, 14));
		FILE_DIR_LBL.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));

		return FILE_DIR_LBL;
	}

	private boolean DialogSavePrompt()
	{
		if (this.ASSY_TABLE.RowCount() == 0 && this.PURCHASE_TABLE.RowCount() == 0)
		{
			return true;
		}
		JFrame frame = new JFrame();
		frame.setAlwaysOnTop(true);
		int n = JOptionPane.showConfirmDialog(frame, "Do you wish to save the current project?", "Save Project?", JOptionPane.YES_NO_CANCEL_OPTION);
		if(n == JOptionPane.CANCEL_OPTION || n == JOptionPane.CLOSED_OPTION)
		{
			return false;
		}
		if (n == JOptionPane.YES_OPTION)
		{
			new BtnProjectSave(ASSY_TABLE, PURCHASE_TABLE, PROJECT_PANEL.GetProjectDetails(), PROJECT_PANEL.GetSpinner());
		}
		return true;
	}

	public static void main(String[] args)
	{
		LOCALHOST_URL = "http://192.168.16.197:800";
		if (args.length != 0)
		{
			LOCALHOST_URL = args[0];
		}
		SwingUtilities.invokeLater(() -> {
			new Main();
		});
	}
}
