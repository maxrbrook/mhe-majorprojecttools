package zohorest;

import javax.swing.JFrame;
import javax.swing.JDialog;

class DialogParent extends JDialog
{
	private boolean USER_CONFIRM = false;

	public DialogParent(String dialog_name)
	{
		super(new JFrame(), dialog_name, true);
		super.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		super.addWindowListener(new DialogBoxExit(this));
	}

	public void UserConfirmsDialog()
	{
		this.USER_CONFIRM = true;
		this.dispose();
	}

	public boolean UserConfirmSelection()
	{
		System.out.println(this.USER_CONFIRM);
		return this.USER_CONFIRM;
	}
}
