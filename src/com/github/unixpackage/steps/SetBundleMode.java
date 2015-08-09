package com.github.unixpackage.steps;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.github.unixpackage.components.CommonStep;
import com.github.unixpackage.data.Constants;
import com.github.unixpackage.data.Variables;
import com.github.unixpackage.utils.Files;
import com.github.unixpackage.utils.SpringUtilities;

@SuppressWarnings("serial")
public class SetBundleMode extends CommonStep {

	protected String[] labels = { "Description: ", "Extra: " };
	protected String[] tooltips = { "", "" };
	protected String[] variables = { "", "PACKAGE_SHORT_DESCRIPTION",
	"PACKAGE_DESCRIPTION" };

	public SetBundleMode() {
		// Clear screen first
		this.removeAll();
		// Populate the panel
		this.setLayout(new SpringLayout());

		// Action button and checkbox
		final JButton addSourceFiles;
		final JCheckBox signGPG = new JCheckBox();

		// Description
		JLabel packageTypeDescription = new JLabel(
				"Choose the type of package:", JLabel.TRAILING);
		this.add(packageTypeDescription);
		final JLabel warningMessageOS = new JLabel("");
		final String warningOSRPM = "Warning: RPM generation under Debian-based OS";
		final String warningOSDEB = "Warning: DEB generation under RedHat-based OS";

		// Choose DEB or RPM packages
		JRadioButton choiceDEB = new JRadioButton();
		choiceDEB.setText("Create DEB package");
		choiceDEB.setToolTipText("Generate a package for Debian-based distros");
		choiceDEB.setName("_DEB_PACKAGE");
		JRadioButton choiceRPM = new JRadioButton();
		// Fill the width gap
		choiceRPM.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
		choiceRPM.setText("Create RPM package");
		choiceRPM.setToolTipText("Generate a Red hat-based distros");
		choiceRPM.setName("_RPM_PACKAGE");
		// Group these
		ButtonGroup choicePackages = new ButtonGroup();
		choicePackages.add(choiceDEB);
		choicePackages.add(choiceRPM);

		// Add choices to vertical box
		Box choicePackagesBox = Box.createVerticalBox();
		choicePackagesBox.setName("PACKAGE_TYPE");
		choicePackagesBox.add(choiceDEB);
		choicePackagesBox.add(choiceRPM);
		this.add(choicePackagesBox);

		// Set name of variable where the field should be saved in
		if (Variables.isNull("PACKAGE_TYPE")
				|| Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_DEB)) {
			// DEB package by default
			choicePackages.setSelected(choiceDEB.getModel(), true);
		} else {
			if (Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_RPM)) {
				choicePackages.setSelected(choiceRPM.getModel(), true);
			}
		}

		// New row
		this.add(new JLabel());
		this.add(new JLabel());

		// Description
		JLabel bundleChoiceDescription = new JLabel(
				"Choose the bundling mode:", JLabel.TRAILING);
		this.add(bundleChoiceDescription);

		// Choose bundle mode
		final JRadioButton bundleSimple = new JRadioButton();
		bundleSimple.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
		bundleSimple.setText(Constants.BUNDLE_MODE_SIMPLE + " mode");
		bundleSimple.setToolTipText(Constants.BUNDLE_MODE_DESCRIPTIONS
				.get(Constants.BUNDLE_MODE_SIMPLE));
		bundleSimple.setName("BUNDLE_MODE_SIMPLE");
		if (Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_RPM)) {
			// Disabled for RPM
			bundleSimple.setEnabled(false);
		}

		final JRadioButton bundleManual = new JRadioButton();
		// Fill the width gap
		bundleManual.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
		bundleManual.setText(Constants.BUNDLE_MODE_MANUAL + " mode");
		bundleManual.setToolTipText(Constants.BUNDLE_MODE_DESCRIPTIONS
				.get(Constants.BUNDLE_MODE_MANUAL));
		bundleManual.setName("BUNDLE_MODE_MANUAL");

		final JRadioButton bundleAdvanced = new JRadioButton();
		bundleAdvanced.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
		bundleAdvanced.setText(Constants.BUNDLE_MODE_ADVANCED + " mode");
		bundleAdvanced.setToolTipText(Constants.BUNDLE_MODE_DESCRIPTIONS
				.get(Constants.BUNDLE_MODE_ADVANCED));
		bundleAdvanced.setName("BUNDLE_MODE_ADVANCED");

		// Group these
		final ButtonGroup choiceBundleMode = new ButtonGroup();
		choiceBundleMode.add(bundleSimple);
		choiceBundleMode.add(bundleManual);
		choiceBundleMode.add(bundleAdvanced);

		// Add choices to vertical box
		Box choiceBundleModeBox = Box.createVerticalBox();
		choiceBundleModeBox.setName("BUNDLE_MODE");
		choiceBundleModeBox.add(bundleSimple);
		choiceBundleModeBox.add(bundleManual);
		choiceBundleModeBox.add(bundleAdvanced);
		this.add(choiceBundleModeBox);

		// Set name of variable where the field should be saved in
		// TODO Fill in CommonStep
		if (Variables.isNull("BUNDLE_MODE")) {
			// Manual bundle by default
			choiceBundleMode.setSelected(bundleManual.getModel(), true);
			Variables.set("BUNDLE_MODE", Constants.BUNDLE_MODE_MANUAL);
		} else {
			if (Variables.BUNDLE_MODE.equals(Constants.BUNDLE_MODE_SIMPLE)) {
				if (Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_DEB)) {
					// Simple mode enabled for DEB only, enable manual mode
					choiceBundleMode.setSelected(bundleSimple.getModel(), true);
				}
			} else if (Variables.BUNDLE_MODE
					.equals(Constants.BUNDLE_MODE_MANUAL)) {
				choiceBundleMode.setSelected(bundleManual.getModel(), true);
			} else {
				choiceBundleMode.setSelected(bundleAdvanced.getModel(), true);
			}
		}
		// Description for previous choice
		final JLabel choiceDescription = new JLabel(
				Constants.BUNDLE_MODE_DESCRIPTIONS.get(Variables.BUNDLE_MODE),
				JLabel.TRAILING);
		this.add(new JLabel());
		this.add(choiceDescription);

		// New row
		this.add(new JLabel());
		this.add(new JLabel());

		// Description of following action
		final JLabel addSourceFilesPathLabel = new JLabel(
				"Chosen source of files:", JLabel.TRAILING);
		final JLabel addSourceFilesPath = new JLabel(
				(String) Variables.get("BUNDLE_MODE_ADVANCED_PATH"),
				JLabel.TRAILING);
		addSourceFilesPath.setToolTipText(Variables.BUNDLE_MODE_ADVANCED_PATH);

		addSourceFiles = new JButton("Add path of source files");
		addSourceFiles.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
		addSourceFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sourcePath = Files.choosePath();
				if (sourcePath != null && !sourcePath.isEmpty()) {
					// Debian by default
					String bundleTypeFolder = Constants.BUNDLE_TYPE_DEB_FOLDER;
					// Otherwise, use RPM convention
					if (Variables.PACKAGE_TYPE
							.equals(Constants.BUNDLE_TYPE_RPM)) {
						bundleTypeFolder = Constants.BUNDLE_TYPE_RPM_FOLDER;
					}
					while (!sourcePath.isEmpty()
							&& !(Files.isFolder(sourcePath, bundleTypeFolder) || Files
									.containsFolder(sourcePath,
											bundleTypeFolder))) {
						JOptionPane
						.showMessageDialog(
								null,
								"The chosen path is not (or does not contain) a '"
										+ bundleTypeFolder
										+ "' folder."
										+ "\n"
										+ "Please choose an appropriate folder");
						sourcePath = Files.choosePath();
					}
					try {
						Variables.set("BUNDLE_MODE_ADVANCED_PATH", sourcePath);
						addSourceFilesPathLabel.setVisible(true);
						addSourceFilesPath
						.setText(Variables.BUNDLE_MODE_ADVANCED_PATH);
						addSourceFilesPath.setVisible(true);
					} catch (Exception ex) {
					}
				}
				// If location of sources is empty (user cancelled choice of
				// path)
				// then automatically choose another type of bundle
				if (sourcePath.isEmpty()) {
					SetBundleMode.setDefaultBundleMode(bundleManual);
				}
			}
		});

		choiceDEB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Variables.set("PACKAGE_TYPE", Constants.BUNDLE_TYPE_DEB);
				bundleSimple.setEnabled(true);
				signGPG.setEnabled(true);
				// Update consistency warning if required
				String osDistro = Files.getOSDistro();
				if (osDistro != null) {
					if (osDistro.equals("Fedora")) {
						warningMessageOS.setText(warningOSDEB);
					} else {
						warningMessageOS.setText("");
					}
				}
			}
		});
		choiceRPM.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Variables.set("PACKAGE_TYPE", Constants.BUNDLE_TYPE_RPM);
				if (Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_RPM)) {
					// Disable simple mode for RPM, enable manual mode
					bundleSimple.setEnabled(false);
					choiceBundleMode.setSelected(bundleManual.getModel(), true);
					// Disable automatic signature for RPM
					signGPG.setEnabled(false);
					signGPG.setSelected(false);
					// Update consistency warning if required
					String osDistro = Files.getOSDistro();
					if (osDistro != null) {
						if (osDistro.equals("Debian")) {
							warningMessageOS.setText(warningOSRPM);
						} else {
							warningMessageOS.setText("");
						}
					}
				}
			}
		});

		// Define visibility for fields related to Advanced mode
		if (Variables.isNull("BUNDLE_MODE")
				|| !Variables.get("BUNDLE_MODE").equals(
						Constants.BUNDLE_MODE_ADVANCED)) {
			addSourceFilesPathLabel.setVisible(false);
			addSourceFilesPath.setVisible(false);
			addSourceFiles.setVisible(false);
		} else {
			addSourceFilesPathLabel.setVisible(true);
			addSourceFilesPath.setVisible(true);
			addSourceFiles.setVisible(true);
		}

		this.add(new JLabel());
		this.add(addSourceFiles);

		// Placement of description for previous action
		this.add(addSourceFilesPathLabel);
		this.add(addSourceFilesPath);

		// New row
		this.add(new JLabel());
		this.add(new JLabel());

		// Sign with GPG
		// XXX: Currently not working properly for RPM. Cannot validate
		// passphrase for key
		JLabel signGPGLabel = new JLabel("Sign package with GPG?");
		signGPG.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
		signGPGLabel.setLabelFor(signGPG);
		// Set name of variable where the field should be saved in
		signGPG.setName("PACKAGE_SIGN");
		// TODO Fill in CommonStep
		if (!Variables.isNull(signGPG.getName())) {
			signGPG.setSelected((Boolean) Variables.get(signGPG.getName()));
			if (Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_RPM)) {
				signGPG.setSelected(false);
				signGPG.setEnabled(false);
			}
		}
		this.add(signGPGLabel);
		this.add(signGPG);

		// New row
		this.add(new JLabel());
		this.add(new JLabel());

		// Independently of the package type stored in the preferences, this warns about inaccuracies between selected package type and current OS
		String osDistro = Files.getOSDistro();
		if (osDistro != null) {
			if (osDistro.equals("Debian") && choiceRPM.isSelected()) {
				warningMessageOS.setText(warningOSRPM);
			} else if (osDistro.equals("Fedora") && choiceDEB.isSelected()) {
				warningMessageOS.setText(warningOSDEB);
			}
		}

		this.add(new JLabel());
		warningMessageOS.setForeground(Constants.WARNING_FOREGROUND);
		this.add(warningMessageOS);

		// One per radio button
		bundleSimple.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (bundleSimple.isSelected()) {
					choiceDescription
					.setText(Constants.BUNDLE_MODE_DESCRIPTIONS
							.get(Constants.BUNDLE_MODE_SIMPLE));
					Variables.set("BUNDLE_MODE", Constants.BUNDLE_MODE_SIMPLE);
					// Disabling importing package files
					addSourceFiles.setVisible(false);
					addSourceFilesPathLabel.setVisible(false);
					addSourceFilesPath.setVisible(false);
				}
			}
		});
		bundleManual.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (bundleManual.isSelected()) {
					choiceDescription
					.setText(Constants.BUNDLE_MODE_DESCRIPTIONS
							.get(Constants.BUNDLE_MODE_MANUAL));
					SetBundleMode.setDefaultBundleMode(bundleManual);
					// Disabling importing package files
					addSourceFiles.setVisible(false);
					addSourceFilesPathLabel.setVisible(false);
					addSourceFilesPath.setVisible(false);
				}
			}
		});
		bundleAdvanced.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (bundleAdvanced.isSelected()) {
					choiceDescription
					.setText(Constants.BUNDLE_MODE_DESCRIPTIONS
							.get("Advanced"));
					Variables
					.set("BUNDLE_MODE", Constants.BUNDLE_MODE_ADVANCED);
					// Allow to input a path to import package files
					addSourceFiles.setVisible(true);
					// Show path information once it was select at least one
					// time
					if (!Variables.isNull("BUNDLE_MODE_ADVANCED_PATH")) {
						addSourceFilesPathLabel.setVisible(true);
						addSourceFilesPath.setVisible(true);
					}
				}
			}
		});

		// Another for the signing checkbox
		signGPG.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (signGPG.isSelected()) {
					if (Variables.PACKAGE_TYPE
							.equals(Constants.BUNDLE_TYPE_RPM)) {
						signGPG.setSelected(false);
						signGPG.setEnabled(false);
					}
				}
			}
		});

		// Lay out the panel
		SpringUtilities.makeCompactGrid(this, 11, 2, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad
	}

	public static void setDefaultBundleMode(JRadioButton defaultBundleMode) {
		defaultBundleMode.setSelected(true);
		Variables.set("BUNDLE_MODE", Constants.BUNDLE_MODE_MANUAL);
		Variables.set("BUNDLE_MODE_ADVANCED_PATH", "");
	}
}
