package com.github.unixpackage.steps;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import com.github.unixpackage.components.CommonStep;
import com.github.unixpackage.data.Constants;
import com.github.unixpackage.data.Variables;
import com.github.unixpackage.utils.SpringUtilities;

@SuppressWarnings("serial")
public class SetPackageInfo extends CommonStep {

	protected String[] labels = { "Name: ", "Description (short): ",
			"Description (long): ", "Website: " };
	protected String[] tooltips = { "Name of the package (lower case)",
			"Up to 60 characters", "Detailed description", "URL of the website" };
	protected String[] variables = { "PACKAGE_NAME",
			"PACKAGE_SHORT_DESCRIPTION", "PACKAGE_DESCRIPTION",
	"PACKAGE_WEBSITE" };

	public SetPackageInfo() {
		int numPairs = labels.length;
		// Clear screen first
		this.removeAll();
		// Populate the panel
		this.setLayout(new SpringLayout());

		for (int i = 0; i < numPairs; i++) {
			JLabel l = new JLabel(labels[i], JLabel.TRAILING);
			this.add(l);
			JTextField textField = new JTextField(10);
			// Set name of variable where the field should be saved in
			textField.setName(variables[i]);
			if (!Variables.isNull(this.variables[i])) {
				textField.setText(Variables.get(this.variables[i]).toString());
				textField.setColumns(Constants.TEXTFIELD_COLUMNS_MAX);
			}
			l.setLabelFor(textField);
			textField.setToolTipText(tooltips[i]);
			textField.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
			this.add(textField);
		}

		// Version - Revision
		JLabel packageVersionLabel = new JLabel("Version[-Revision]",
				JLabel.TRAILING);
		this.add(packageVersionLabel);
		JTextField packageVersionField = new JTextField(4);
		if (!Variables.isNull("PACKAGE_VERSION")) {
			packageVersionField.setText(Variables.get("PACKAGE_VERSION")
					.toString());
		}
		packageVersionLabel.setLabelFor(packageVersionField);
		// Set name of variable where the field should be saved in
		packageVersionField.setName("PACKAGE_VERSION");
		packageVersionField
		.setToolTipText("Version (and optionally revision) of the package");
		packageVersionField.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
		packageVersionField.setColumns(Constants.TEXTFIELD_COLUMNS_MAX);		
		this.add(packageVersionField);
		
		// Licence
		JLabel licenceLabel = new JLabel("Licence:", JLabel.TRAILING);
		DefaultComboBoxModel licencesList = new DefaultComboBoxModel();
		for (String licence : Constants.PACKAGE_LICENCES.keySet()) {
			licencesList.addElement(licence);
		}
		JComboBox licencesListBox = new JComboBox(licencesList);
		licenceLabel.setLabelFor(licencesListBox);
		licencesListBox.setToolTipText("Choose a licence for your package");
		// Set name of variable where the field should be saved in
		licencesListBox.setName("PACKAGE_LICENCE");
		// TODO Fill in CommonStep
		if (!Variables.isNull(licencesListBox.getName())) {
			licencesListBox.setSelectedItem(Variables.get(licencesListBox
					.getName()));
		}
		this.add(licenceLabel);
		this.add(licencesListBox);

		// Class
		JLabel classLabel = new JLabel("Class:", JLabel.TRAILING);
		DefaultComboBoxModel classesList = new DefaultComboBoxModel();
		for (String packageClass : Constants.PACKAGE_CLASSES.keySet()) {
			classesList.addElement(packageClass);
		}
		JComboBox classesListBox = new JComboBox(classesList);
		classLabel.setLabelFor(classesListBox);
		classesListBox
		.setToolTipText("Choose a class in which your package fits");
		// Set name of variable where the field should be saved in
		classesListBox.setName("PACKAGE_CLASS");
		// TODO Fill in CommonStep
		if (!Variables.isNull(classesListBox.getName())) {
			classesListBox.setSelectedItem(Variables.get(classesListBox
					.getName()));
		}
		this.add(classLabel);
		this.add(classesListBox);

		// Section
		JLabel sectionLabel = new JLabel("Section:", JLabel.TRAILING);
		DefaultComboBoxModel sectionsList = new DefaultComboBoxModel();
		for (String section : Constants.PACKAGE_SECTIONS) {
			sectionsList.addElement(section);
		}
		JComboBox sectionsListBox = new JComboBox(sectionsList);
		sectionLabel.setLabelFor(sectionsListBox);
		sectionsListBox.setToolTipText("Choose the section more related to your package");
		// Set name of variable where the field should be saved in
		sectionsListBox.setName("PACKAGE_SECTION");
		// TODO Fill in CommonStep
		if (!Variables.isNull(sectionsListBox.getName())) {
			sectionsListBox.setSelectedItem(Variables.get(sectionsListBox
					.getName()));
		}
		this.add(sectionLabel);
		this.add(sectionsListBox);
		
		// Priority
		JLabel priorityLabel = new JLabel("Priority:", JLabel.TRAILING);
		DefaultComboBoxModel prioritiesList = new DefaultComboBoxModel();
		for (String priority : Constants.PACKAGE_PRIORITIES) {
			prioritiesList.addElement(priority);
		}
		JComboBox prioritiesListBox = new JComboBox(prioritiesList);
		priorityLabel.setLabelFor(prioritiesListBox);
		prioritiesListBox.setToolTipText("Set a suitable priority");
		// Set name of variable where the field should be saved in
		prioritiesListBox.setName("PACKAGE_PRIORITY");
		// TODO Fill in CommonStep
		if (!Variables.isNull(prioritiesListBox.getName())) {
			prioritiesListBox.setSelectedItem(Variables.get(prioritiesListBox
					.getName()));
		}
		this.add(priorityLabel);
		this.add(prioritiesListBox);
		
		// New row
		this.add(new JLabel());
		this.add(new JLabel());

		if (!Variables.isNull("BUNDLE_MODE") && Variables.BUNDLE_MODE.equals(Constants.BUNDLE_MODE_ADVANCED) && !Variables.isNull("BUNDLE_MODE_ADVANCED_PATH")) {
			JLabel warningLabel1 = new JLabel("Warning: ensure the",
					JLabel.TRAILING);
			JLabel warningLabel2 = new JLabel(
					"package information is exactly the same as in your templates",
					JLabel.TRAILING);
			numPairs += 1;
			this.add(warningLabel1);
			this.add(warningLabel2);
		}

		// Lay out the panel
		SpringUtilities.makeCompactGrid(this, numPairs + 6, 2, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad
	}
}
