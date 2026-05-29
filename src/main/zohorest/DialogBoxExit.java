package zohorest;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JDialog;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class DialogBoxExit extends WindowAdapter
{
	JDialog d;
	DialogBoxExit(JDialog d)
	{
		this.d = d;
	}
	public void windowClosing(WindowEvent e)
	{
		int n = JOptionPane.showConfirmDialog(new JFrame(), "Are you sure you want to exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
		if(n == JOptionPane.YES_OPTION)
		{
			d.dispose();
		}
	}
}
