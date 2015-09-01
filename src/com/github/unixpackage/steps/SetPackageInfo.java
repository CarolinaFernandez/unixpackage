package com.github.unixpackage.steps;

import java.util.ArrayList;
import java.util.Map;

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

	@SuppressWarnings({ "unchecked", "rawtypes" })
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
		String versionRevision = "Version[-Revision]";
		String versionToolTip = "Version (and optionally revision) of the package";
		if (!Variables.isNull("PACKAGE_TYPE")
				&& Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_RPM)) {
			versionRevision = "Version-Revision";
			versionToolTip = "Version and revision of the package";
		}
		JLabel packageVersionLabel = new JLabel(versionRevision,
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
		packageVersionField.setToolTipText(versionToolTip);
		packageVersionField.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
		packageVersionField.setColumns(Constants.TEXTFIELD_COLUMNS_MAX);
		this.add(packageVersionField);

		// Licence
		Map<String, String> packageLicences = Constants.PACKAGE_LICENCES_DEB;
		if (!Variables.isNull("PACKAGE_TYPE")
				&& Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_RPM)) {
			packageLicences = Constants.PACKAGE_LICENCES_RPM;
		}

		JLabel licenceLabel = new JLabel("Licence:", JLabel.TRAILING);
		DefaultComboBoxModel licencesList = new DefaultComboBoxModel();
		for (String licence : packageLicences.keySet()) {
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
		String packageClassName = "Class";
		Map<String, String> packageClasses = Constants.PACKAGE_CLASSES_DEB;
		String classToolTip = "Choose a class in which your package fits";
		if (!Variables.isNull("PACKAGE_TYPE")
				&& Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_RPM)) {
			packageClassName = "Architecture";
			packageClasses = Constants.PACKAGE_CLASSES_RPM;
			classToolTip = "Choose an architecture in which your package fits";
		}
		JLabel classLabel = new JLabel(packageClassName + ":", JLabel.TRAILING);
		DefaultComboBoxModel classesList = new DefaultComboBoxModel();
		for (String packageClass : packageClasses.keySet()) {
			classesList.addElement(packageClass);
		}
		JComboBox classesListBox = new JComboBox(classesList);
		classLabel.setLabelFor(classesListBox);
		classesListBox.setToolTipText(classToolTip);
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
		String sectionName = "Section";
		String sectionToolTip = "Choose the section more related to your package";
		ArrayList<String> packageSections = Constants.PACKAGE_SECTIONS_DEB;
		if (!Variables.isNull("PACKAGE_TYPE")
				&& Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_RPM)) {
			sectionName = "Group";
			sectionToolTip = "Choose the group more related to your package";
			packageSections = Constants.PACKAGE_SECTIONS_RPM;
		}
		JLabel sectionLabel = new JLabel(sectionName + ":", JLabel.TRAILING);
		DefaultComboBoxModel sectionsList = new DefaultComboBoxModel();
		for (String section : packageSections) {
			sectionsList.addElement(section);
		}
		JComboBox sectionsListBox = new JComboBox(sectionsList);
		sectionLabel.setLabelFor(sectionsListBox);
		sectionsListBox.setToolTipText(sectionToolTip);
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
		if (Variables.isNull("PACKAGE_TYPE")
				|| Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_DEB)) {
			JComboBox prioritiesListBox = new JComboBox(prioritiesList);
			priorityLabel.setLabelFor(prioritiesListBox);
			prioritiesListBox.setToolTipText("Set a suitable priority");
			// Set name of variable where the field should be saved in
			prioritiesListBox.setName("PACKAGE_PRIORITY");
			// TODO Fill in CommonStep
			if (!Variables.isNull(prioritiesListBox.getName())) {
				prioritiesListBox.setSelectedItem(Variables
						.get(prioritiesListBox.getName()));
			}
			this.add(priorityLabel);
			this.add(prioritiesListBox);
			numPairs += 1;
		}

		// New row
		this.add(new JLabel());
		this.add(new JLabel());

		if (!Variables.isNull("BUNDLE_MODE")
				&& Variables.BUNDLE_MODE.equals(Constants.BUNDLE_MODE_ADVANCED)
				&& !Variables.isNull("BUNDLE_MODE_ADVANCED_PATH")) {
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
		SpringUtilities.makeCompactGrid(this, numPairs + 5, 2, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad
	}
}
