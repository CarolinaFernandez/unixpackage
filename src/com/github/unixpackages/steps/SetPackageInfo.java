package com.github.unixpackages.steps;


import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SpringLayout;


import com.github.unixpackages.components.CommonStep;
import com.github.unixpackages.data.Constants;
import com.github.unixpackages.data.Variables;
import com.github.unixpackages.utils.SpringUtilities;

@SuppressWarnings("serial")
public class SetPackageInfo extends CommonStep {
	
	protected String[] labels = {"Name: ", "Description (short): ", "Description (long): "};
	protected String[] tooltips = {"Name of the package (lower case)", "Up to 60 characters", "Detailed description"};
	protected String[] variables = {"PACKAGE_NAME", "PACKAGE_SHORT_DESCRIPTION", "PACKAGE_DESCRIPTION"};

	public SetPackageInfo() {
        int numPairs = labels.length;
        // Clear screen first
        this.removeAll();
        // Populate the panel
        this.setLayout(new SpringLayout());
        // Choose DEB or RPM packages
        JRadioButton choiceDEB = new JRadioButton();
        choiceDEB.setText("Create DEB package");
        choiceDEB.setToolTipText("Generate a package for Debian-based distros");
        choiceDEB.setName("DEB_PACKAGE");
        this.add(choiceDEB);
        JRadioButton choiceRPM = new JRadioButton();
        // Fill the width gap
        choiceRPM.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
        choiceRPM.setText("Create RPM package");
        choiceRPM.setToolTipText("Generate a Red hat-based distros");
        choiceRPM.setName("RPM_PACKAGE");
        // XXX Temporarily not available
        choiceRPM.setFocusable(false);
        choiceRPM.setEnabled(false);
        this.add(choiceRPM);
        // Group these
        ButtonGroup choicePackages = new ButtonGroup();
        choicePackages.add(choiceDEB);
        choicePackages.add(choiceRPM);
        
        // Set name of variable where the field should be saved in
        if (Variables.isNull("PACKAGE_TYPE")) {
        	// DEB package by default
        	choicePackages.setSelected(choiceDEB.getModel(), true);	
        } else {
        	if (Variables.PACKAGE_TYPE.equals("RPM")) {
        		choicePackages.setSelected(choiceRPM.getModel(), true);
        	} else {
        		choicePackages.setSelected(choiceDEB.getModel(), true);
        	}
        }
        
        for (int i = 0; i < numPairs; i++) {
            JLabel l = new JLabel(labels[i], JLabel.TRAILING);
            this.add(l);
            JTextField textField = new JTextField(10);
            // Set name of variable where the field should be saved in
        	textField.setName(variables[i]);
            if (!Variables.isNull(this.variables[i])) {
            	textField.setText(Variables.get(this.variables[i]).toString());
            }
            l.setLabelFor(textField);
            textField.setToolTipText(tooltips[i]);
            textField.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
            this.add(textField);
        }
        
        // Version - Revision
        JLabel packageVersionLabel = new JLabel("Version[-Revision]", JLabel.TRAILING);
        this.add(packageVersionLabel);
        JTextField packageVersionField = new JTextField(4);
        if (!Variables.isNull("PACKAGE_VERSION")) {
        	packageVersionField.setText(Variables.get("PACKAGE_VERSION").toString());
        }
        packageVersionLabel.setLabelFor(packageVersionField);
        // Set name of variable where the field should be saved in
        packageVersionField.setName("PACKAGE_VERSION");
        packageVersionField.setToolTipText("Version (and optionally revision) of the package");
        packageVersionField.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
        this.add(packageVersionField);
        
        // License
        JLabel licenseLabel = new JLabel("License:", JLabel.TRAILING);
        DefaultComboBoxModel licensesList = new DefaultComboBoxModel();
        for (String license : Constants.PACKAGE_LICENSES.keySet()) {
        	licensesList.addElement(license);
        }
        JComboBox licensesListBox = new JComboBox(licensesList);
        licenseLabel.setLabelFor(licensesListBox);
        licensesListBox.setToolTipText("Choose a license for your package");
        // Set name of variable where the field should be saved in
        licensesListBox.setName("PACKAGE_LICENSE");
        // TODO Fill in CommonStep
        if (!Variables.isNull(licensesListBox.getName())) {
        	licensesListBox.setSelectedItem(Variables.get(licensesListBox.getName()));
		}
        this.add(licenseLabel);
        this.add(licensesListBox);

        // Class
        JLabel classLabel = new JLabel("Class:", JLabel.TRAILING);
        DefaultComboBoxModel classesList = new DefaultComboBoxModel();
        for (String packageClass : Constants.PACKAGE_CLASSES.keySet()) {
        	classesList.addElement(packageClass);
        }
        JComboBox classesListBox = new JComboBox(classesList);
        classLabel.setLabelFor(classesListBox);
        classesListBox.setToolTipText("Choose a class in which your package fits");
        // Set name of variable where the field should be saved in
        classesListBox.setName("PACKAGE_CLASS");
        // TODO Fill in CommonStep
        if (!Variables.isNull(classesListBox.getName())) {
        	classesListBox.setSelectedItem(Variables.get(classesListBox.getName()));
		}
        this.add(classLabel);
        this.add(classesListBox);

        // Sign with GPG
        JLabel signGPGLabel = new JLabel("Sign package with GPG?");
        JCheckBox signGPG = new JCheckBox();
        signGPG.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
        signGPGLabel.setLabelFor(signGPG);
        // Set name of variable where the field should be saved in
        signGPG.setName("PACKAGE_SIGN");
        // TODO Fill in CommonStep
    	signGPG.setSelected(Boolean.valueOf((String) Variables.get(signGPG.getName())));
        this.add(signGPGLabel);
        this.add(signGPG);

        //Lay out the panel
        SpringUtilities.makeCompactGrid(this,
                                        numPairs+5, 2, //rows, cols
                                        6, 6,        //initX, initY
                                        6, 6);       //xPad, yPad
	}
}
