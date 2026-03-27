import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class ProjectTab
{
	private JTextField project_title, client_field, job_number, customer_order, sales_order;
	private JTextArea project_description;
	private JSpinner item_spinner;
	//
	// -- Singleton Returns for textFields
	//
    public String ReturnProjectTitle()
    {
        return project_title.getText();
    }
	public String ReturnClient()
	{
		return client_field.getText();
	}
	public String ReturnJobNumber()
	{
		return job_number.getText();
	}
	public String ReturnSalesOrder()
	{
		return sales_order.getText();
	}
	public String ReturnCustomerOrder()
	{
		return customer_order.getText();
	}
	public String ReturnDescription()
	{
		return project_description.getText();
	}
	public int ReturnSpinnerValue()
	{
		return ((Integer) item_spinner.getValue());
	}

	public void SetProjetTitle(String s)
	{
		project_title.setText(s);
	}
	public void SetClient(String s)
	{
		client_field.setText(s);
	}
	public void SetJobNumber(String s)
	{
		job_number.setText(s);
	}
	public void SetSalesOrder(String s)
	{
		sales_order.setText(s);
	}
	public void SetCustomerOrder(String s)
	{
		customer_order.setText(s);
	}
	public void SetDescription(String s)
	{
		project_description.setText(s);
	}
	public void SetSpinnerValue(String s)
	{
		int i = 0;
		try
		{
			i = Integer.parseInt((String) s);
			item_spinner.setValue(i);
		}
		catch (NumberFormatException e)
		{
			item_spinner.setValue(1);
		}
	}

	public void ClearAllText()
	{
		project_title.setText(null);
		client_field.setText(null);
		job_number.setText(null);
		customer_order.setText(null);
		sales_order.setText(null);
		project_description.setText(null);
		item_spinner.setValue(1);
	}

	public JPanel NewProjectTab(JtableAssembly assembly_table, JtablePurchasing purchasing_table)
	{
		JPanel project_panel = new JPanel();
		GridBagLayout gridbag_layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		JLabel project_title_label, job_number_label, client_label, sales_order_label, customer_order_label, item_spin_label, description_label;

		project_panel.setLayout(gridbag_layout);
		c.insets = new Insets(5, 5, 5, 5);

		/** Create new Project Tab **/
		project_title = new JTextField(20);
		project_title.setFont(new Font("Serif", Font.BOLD, 24));
		client_field = new JTextField(20);
        client_field.setFont(new Font("Serif", Font.BOLD, 24));
		job_number = new JTextField(20);
        job_number.setFont(new Font("Serif", Font.BOLD, 24));
		customer_order = new JTextField(20);
        customer_order.setFont(new Font("Serif", Font.BOLD, 24));
		sales_order = new JTextField(20);
        sales_order.setFont(new Font("Serif", Font.BOLD, 24));
		item_spinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
		project_description = new JTextArea("", 10, 30);
        project_description.setFont(new Font("Serif", Font.BOLD, 18));
        project_description.setLineWrap(true);

		/** Project Title **/
		project_title_label = new JLabel("Project Title: ");
		project_title_label.setLabelFor(project_title);
		c.gridx = 0;
		c.gridy = 0;
		project_panel.add(project_title_label, c);
		c.gridx = 1;
		project_panel.add(project_title, c);

		/** Job Number **/
		job_number_label = new JLabel("Job Number: ");
		job_number_label.setLabelFor(job_number);
		c.gridx = 0;
		c.gridy = 1;
		project_panel.add(job_number_label, c);
		c.gridx = 1;
		project_panel.add(job_number, c);

		/** Client **/
		client_label = new JLabel("Client: ");
		client_label.setLabelFor(client_field);
		c.gridx = 0;
		c.gridy = 2;
		project_panel.add(client_label, c);
		c.gridx = 1;
		project_panel.add(client_field, c);

		/** Sales Order Number **/
		sales_order_label = new JLabel("Sales Order Number: ");
		sales_order_label.setLabelFor(sales_order);
		c.gridx = 0;
		c.gridy = 3;
		project_panel.add(sales_order_label, c);
		c.gridx = 1;
		project_panel.add(sales_order, c);

		/** Customer Order Number **/
		customer_order_label = new JLabel("Customer Order Number: ");
		customer_order_label.setLabelFor(customer_order);
		c.gridx = 0;
		c.gridy = 4;
		project_panel.add(customer_order_label, c);
		c.gridx = 1;
		project_panel.add(customer_order, c);

		/** Item Spinner **/
		item_spin_label = new JLabel("Number of Items: ");
		item_spin_label.setLabelFor(item_spinner);
		c.gridx = 0;
		c.gridy = 5;
		item_spinner.addChangeListener(new SpinnerListener(assembly_table, purchasing_table));
		project_panel.add(item_spin_label, c);
		c.gridx = 1;
		project_panel.add(item_spinner, c);

		/** Project Description **/
		description_label = new JLabel("Project Description: ");
		description_label.setLabelFor(project_description);
		c.gridx = 0;
		c.gridy = 6;
		project_panel.add(description_label, c);
		c.gridx = 1;
		project_panel.add(new JScrollPane(project_description), c);

		return project_panel;
	}
}
