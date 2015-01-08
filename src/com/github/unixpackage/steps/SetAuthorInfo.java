package com.github.unixpackage.steps;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import com.github.unixpackage.components.CommonStep;
import com.github.unixpackage.data.Constants;
import com.github.unixpackage.data.Variables;
import com.github.unixpackage.utils.SpringUtilities;

@SuppressWarnings("serial")
public class SetAuthorInfo extends CommonStep {

	protected String[] labels = { "Username: ", "E-mail: " };
	protected String[] tooltips = { "UNIX user",
			"When signing package this must be the same as in GPG keyring" };
	protected String[] variables = { "MAINTAINER_NAME", "MAINTAINER_EMAIL" };

	/**
	 * Name of the variables defined in com.github.unixpackages.data.Variables
	 * that will be used to store the data inputed by the user
	 */
	public SetAuthorInfo() {
		int numPairs = labels.length;
		// Clear screen first
		this.removeAll();
		this.setLayout(new SpringLayout());
		// Populate the panel
		for (int i = 0; i < numPairs; i++) {
			JLabel l = new JLabel(labels[i], JLabel.TRAILING);
			this.add(l);
			JTextField textField = new JTextField();
			// Set name of variable where the field should be saved in
			textField.setName(variables[i]);
			// Fill fields with pre-saved variables
			// TODO Fill in CommonStep
			if (!Variables.isNull(this.variables[i])) {
				textField.setText(Variables.get(this.variables[i]).toString());
			}
			l.setLabelFor(textField);
			textField.setToolTipText(tooltips[i]);
			textField.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
			this.add(textField);
		}

		// Lay out the panel
		SpringUtilities.makeCompactGrid(this, numPairs, 2, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad
	}
}
