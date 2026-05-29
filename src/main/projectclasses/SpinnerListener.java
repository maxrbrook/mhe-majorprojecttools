package projectclasses;

import tables.AssemblyTable;
import tables.PurchasingTable;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JSpinner;

public class SpinnerListener implements ChangeListener
{
	AssemblyTable ASSY_TABLE;
	PurchasingTable PURCHASE_TABLE;

	public SpinnerListener(AssemblyTable a, PurchasingTable p)
	{
		this.ASSY_TABLE = a;
		this.PURCHASE_TABLE = p;
	}
	public void stateChanged(ChangeEvent e)
	{
		JSpinner spinner = (JSpinner)e.getSource();
		System.out.println("Value changed: " + spinner.getValue());
		this.ASSY_TABLE.TableMultIntPos((int)spinner.getValue(), 6, 7);
		this.PURCHASE_TABLE.TableMultIntPos((int)spinner.getValue(), 6, 7);
	}
}
