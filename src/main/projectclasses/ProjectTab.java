package projectclasses;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.SpinnerNumberModel;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Font;
import java.awt.Insets;

public class ProjectTab extends JPanel
{
	private JTextField project_title, client_field, job_number;
	private JTextArea project_description;
	private JSpinner item_spinner;
	private Font std_font;
	private GridBagConstraints c;
	private GridBagLayout gridbag_layout;

	public ProjectTab()
	{
		this.SetLayout();
		this.CreateProjectTitle();
		this.CreateJobNumber();
		this.CreateClientField();
		this.CreateSpinner();
		this.ProjectDescription();
	}

	public String[] GetProjectDetails()
	{
		String[] rtn = new String[4];
		rtn[0] = project_title.getText();
		rtn[1] = job_number.getText();
		rtn[2] = client_field.getText();
		rtn[3] = project_description.getText();

		return rtn;
	}

	private void SetLayout()
	{
		gridbag_layout = new GridBagLayout();
		c = new GridBagConstraints();
		std_font = new Font("Dialog", Font.BOLD, 24);
		super.setLayout(gridbag_layout);
		c.insets = new Insets(5, 5, 5, 5);
	}

	/** Project Title **/
	private void CreateProjectTitle()
	{
		project_title = new JTextField(20);
		project_title.setFont(std_font);

		JLabel project_title_label = new JLabel("Project Title: ");
		project_title_label.setLabelFor(project_title);

		c.gridx = 0;
		c.gridy = 0;
		super.add(project_title_label, c);
		c.gridx = 1;
		super.add(project_title, c);
	}

	/** Job Number **/
	private void CreateJobNumber()
	{
		job_number = new JTextField(20);
        job_number.setFont(std_font);

        JLabel job_number_label = new JLabel("Job Number: ");
		job_number_label.setLabelFor(job_number);
		c.gridx = 0;
		c.gridy = 1;
		super.add(job_number_label, c);
		c.gridx = 1;
		super.add(job_number, c);
	}

	/** Client **/
	private void CreateClientField()
	{
		client_field = new JTextField(20);
        client_field.setFont(std_font);

		JLabel client_label = new JLabel("Client: ");
		client_label.setLabelFor(client_field);
		c.gridx = 0;
		c.gridy = 2;
		super.add(client_label, c);
		c.gridx = 1;
		super.add(client_field, c);
	}

	/** Item Spinner **/
	private void CreateSpinner()
	{
		item_spinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

		JLabel item_spin_label = new JLabel("Number of Items: ");
		item_spin_label.setLabelFor(item_spinner);
		c.gridx = 0;
		c.gridy = 3;
		super.add(item_spin_label, c);
		c.gridx = 1;
		super.add(item_spinner, c);
	}

	public JSpinner GetSpinner()
	{
		return item_spinner;
	}

	public void SetSpinnerVal(int n)
	{
		item_spinner.setValue(n);
	}

	private void ProjectDescription()
	{
		project_description = new JTextArea("", 10, 30);
        project_description.setFont(std_font);

		JLabel description_label = new JLabel("Project Description: ");
		description_label.setLabelFor(project_description);
		c.gridx = 0;
		c.gridy = 4;
		super.add(description_label, c);
		c.gridx = 1;
		super.add(project_description, c);
	}

	public String GetProjectTitle()
    {
        return project_title.getText();
    }

    public void SetProjetTitle(String s)
	{
		project_title.setText(s);
	}

	public String GetJobNumber()
	{
		return job_number.getText();
	}
	public void SetJobNumber(String s)
	{
		job_number.setText(s);
	}

	public String GetClient()
	{
		return client_field.getText();
	}
	public void SetClient(String s)
	{
		client_field.setText(s);
	}

	public String GetProjectDescription()
	{
		return project_description.getText();
	}
	public void SetProjectDescription(String s)
	{
		project_description.setText(s);
	}
}
