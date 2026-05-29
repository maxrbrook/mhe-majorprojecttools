package zohorest;

import tables.BasicJtable;

import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.KeyStroke;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;


public class DialogAssyLink extends DialogParent
{
	private String[] TL_ID_ARR;
	private String[] TL_NAME_ARR;
	ImageIcon icon = new ImageIcon("../resources/middle.gif");

	private HashMap<String, Integer> TL_NAME_ARR_POS;

	private BasicJtable TL_TABLE;

	public DialogAssyLink(ArrayList<ArrayList<String>> l, TableModel data)
	{
		super("Assign Tasklists to Items");
		this.TL_ID_ARR = l.get(0).toArray(String[]::new);
		this.TL_NAME_ARR = l.get(1).toArray(String[]::new);
		super.setLayout(new BorderLayout());
		super.setSize(650, 650);
		super.setJMenuBar(CreateMenuBar());
		super.add(new JScrollPane(this.CreateJTable()), BorderLayout.CENTER);
		super.add(this.BtnSetAssyLink(), BorderLayout.PAGE_END);
		this.SetSubAssyData(data);

		super.setVisible(true);
	}

	private JMenuBar CreateMenuBar()
	{
		JMenuBar new_menu = new JMenuBar();

		new_menu.add(this.EditMenu());
		new_menu.add(this.ViewMenu());

		return new_menu;
	}

	private JMenu EditMenu()
	{
		JMenu edit_menu = new JMenu("Edit");

		JMenuItem mass_tag_set = new JMenuItem("Group Tags to Tasklists");
		mass_tag_set.addActionListener(e -> {
			String item = new String();
			String tag_to_use = (String)JOptionPane.showInputDialog(new JFrame(), "Write the [TAG] (Case Sensitive)");

			if (tag_to_use.equals(""))
			{
				return;
			}
			String user_selection = (String)JOptionPane.showInputDialog(new JFrame(),
			"Associate Tag to Tasklist",
			"Associate Tag to Tasklist",
			JOptionPane.PLAIN_MESSAGE,
			icon,
			TL_NAME_ARR,
			"");

			for (int i = 0; i < TL_TABLE.RowCount(); i++)
			{
				if(TL_TABLE.GetValue(i, 2).contains(tag_to_use))
				{
					TL_TABLE.SetValue(user_selection, i, 3);
				}
			}
		});
		edit_menu.add(mass_tag_set);

		return (edit_menu);
	}

	private JMenu ViewMenu()
	{
		JMenu view_menu = new JMenu("View");
		JMenuItem plus_font = new JMenuItem("Zoom In");
		plus_font.addActionListener(e -> {
			TL_TABLE.ZoomIn();
		});
		JMenuItem sub_font = new JMenuItem("Zoom Out");
		sub_font.addActionListener(e -> {
			TL_TABLE.ZoomOut();
		});
		JMenuItem reset_font = new JMenuItem("Reset Zoom");
		reset_font.addActionListener(e -> {
			TL_TABLE.ResetZoom();
		});
		plus_font.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, ActionEvent.CTRL_MASK));
		sub_font.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, ActionEvent.CTRL_MASK));

		view_menu.add(plus_font);
		view_menu.add(sub_font);
		view_menu.add(reset_font);

		return view_menu;
	}

	private JTable CreateJTable()
	{
		Object[] headings = {"*", "LEVEL", "SUB ASSY", "TASKLIST"};
		boolean[] editable = {false, false, false, true};
		TL_TABLE = new BasicJtable(headings, editable);

		TL_TABLE.NewDropdown(this.TL_NAME_ARR, "TASKLIST");
		TL_TABLE.setFillsViewportHeight(true);

		return TL_TABLE;
	}

	private String GetTasklistVal(int i)
	{
		return TL_TABLE.GetValue(i, 3).toString();
	}

	private String GetLevelVal(int i)
	{
		return TL_TABLE.GetValue(i, 1).toString();
	}

	private void SetSubAssyData(TableModel imported_data)
	{
		String[] data = new String[4];
		int count = 0;
		String level = new String();
		String order = new String();

		TL_NAME_ARR_POS = new HashMap<>();

		for (int i = 0; i < imported_data.getRowCount(); i++)
		{
			level = imported_data.getValueAt(i, 1) + "";
			//if (level.matches("1(\\.\\d+){1}"))
			order = imported_data.getValueAt(i, 5) + "";
			if (order.matches("TO MAKE"))
			{
				data[0] = String.valueOf(++count);
				data[1] = level;
				data[2] = imported_data.getValueAt(i, 3) + "";
				TL_TABLE.AddRow(data);
			}
		}
		for (int j = 0; j < TL_NAME_ARR.length; j++)
		{
			TL_NAME_ARR_POS.put(TL_NAME_ARR[j], j);
		}
	}

	private JButton BtnSetAssyLink()
	{
		JButton upload_btn = new JButton("Set Links");
		upload_btn.addActionListener(e -> {
			int n = JOptionPane.showConfirmDialog(new JFrame(), "Set the links and upload the project? ", "Set Tasklist Links", JOptionPane.YES_NO_OPTION);
			if(n == JOptionPane.YES_OPTION)
			{
				super.UserConfirmsDialog();
				return;
			}
		});

		return upload_btn;
	}

	/* Returns hashmap to get the TasklistIDs */
	public HashMap<String, String> ReturnLinkedTasklist()
	{
		/*Sub Assy Level | Tasklist ID */
		HashMap<String, String> rtn = new HashMap<>();
		int tl_pos;

		for (int  i = 0; i < TL_TABLE.RowCount(); i++)
		{
			tl_pos = TL_NAME_ARR_POS.get(GetTasklistVal(i));
			rtn.put(GetLevelVal(i), TL_ID_ARR[tl_pos]);
		}
		return rtn;
	}
}
