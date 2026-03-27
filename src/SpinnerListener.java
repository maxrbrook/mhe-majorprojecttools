import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class SpinnerListener implements ChangeListener
{
	JtableAssembly assy_table;
	JtablePurchasing purchase_table;
	SpinnerListener(JtableAssembly assy, JtablePurchasing purchase)
	{
		this.assy_table = assy;
		this.purchase_table = purchase;
	}
	public void stateChanged(ChangeEvent e)
	{
		JSpinner spinner = (JSpinner)e.getSource();
		System.out.println("Value changed: " + spinner.getValue());
		assy_table.UpdateJTableOrdering(spinner.getValue() + "");
		purchase_table.UpdateJTableOrdering(spinner.getValue() + "");
	}
}
