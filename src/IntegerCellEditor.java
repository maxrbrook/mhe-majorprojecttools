import java.awt.*;
import javax.swing.*;
/*
	Checks the cell to ensure that only integers are written to the cell
*/
public class IntegerCellEditor extends DefaultCellEditor
{
	JTextField text_field;
	public IntegerCellEditor()
	{
		super (new JTextField());
		text_field = new JTextField();
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		JTextField text_field = (JTextField)super.getTableCellEditorComponent(table, value, isSelected, row, column);

		text_field.setText(value + "");
		return text_field;
    }

	public boolean stopCellEditing()
	{
		JTextField text_field = (JTextField)getComponent();
		int x = 0;

		try
		{
			x = Integer.parseInt((String) text_field.getText());
			if (x < 0)
			{
				text_field.setText(0 + "");
			}
		}
		catch (NumberFormatException e)
		{
			return false;
		}
		return super.stopCellEditing();
	}
}
