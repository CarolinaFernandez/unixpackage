package com.github.unixpackages.steps;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import com.github.unixpackages.components.CommonStep;
import com.github.unixpackages.data.Constants;
import com.github.unixpackages.data.Variables;
import com.github.unixpackages.utils.Files;
import com.github.unixpackages.utils.SpringUtilities;

@SuppressWarnings("serial")
public class SetBundleMode extends CommonStep {
	
	protected String[] labels = {"Description: ", "Extra: "};
	protected String[] tooltips = {"", ""};
	protected String[] variables = {"", "PACKAGE_SHORT_DESCRIPTION", "PACKAGE_DESCRIPTION"};

	public SetBundleMode() {
        // Clear screen first
        this.removeAll();
        // Populate the panel
        this.setLayout(new SpringLayout());
        
    	// Action buttons
    	final JButton addSourceFiles;
    	        
        // Description
        JLabel packageTypeDescription = new JLabel("Choose the type of package:", JLabel.TRAILING);
        this.add(packageTypeDescription);
        
        // Choose DEB or RPM packages
        JRadioButton choiceDEB = new JRadioButton();
        choiceDEB.setText("Create DEB package");
        choiceDEB.setToolTipText("Generate a package for Debian-based distros");
        choiceDEB.setName("DEB_PACKAGE");
//        this.add(choiceDEB);
        JRadioButton choiceRPM = new JRadioButton();
        // Fill the width gap
        choiceRPM.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
        choiceRPM.setText("Create RPM package");
        choiceRPM.setToolTipText("Generate a Red hat-based distros");
        choiceRPM.setName("RPM_PACKAGE");
        // XXX Temporarily not available
        choiceRPM.setFocusable(false);
        choiceRPM.setEnabled(false);
//        this.add(choiceRPM);
        // Group these
        ButtonGroup choicePackages = new ButtonGroup();
        choicePackages.add(choiceDEB);
        choicePackages.add(choiceRPM);
        
        // Add choices to vertical box
        Box choicePackagesBox = Box.createVerticalBox();
        choicePackagesBox.setName("BUNDLE_MODE");
        choicePackagesBox.add(choiceDEB);
        choicePackagesBox.add(choiceRPM);
        this.add(choicePackagesBox);
        
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
        
        // New row
        this.add(new JLabel());
        this.add(new JLabel());
        
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
        
        // New row
        this.add(new JLabel());
        this.add(new JLabel());
        
        // Description
        JLabel bundleChoiceDescription = new JLabel("Choose the bundling mode:", JLabel.TRAILING);
        this.add(bundleChoiceDescription);
        
        // Choose bundle mode
        final JRadioButton bundleSimple = new JRadioButton();
        bundleSimple.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
        bundleSimple.setText(Constants.BUNDLE_MODE_SIMPLE + " mode"); // Able to interpret HTML for multi-line text
        bundleSimple.setToolTipText(Constants.BUNDLE_MODE_DESCRIPTIONS.get(Constants.BUNDLE_MODE_SIMPLE));
        bundleSimple.setName("BUNDLE_MODE_SIMPLE");
        
        final JRadioButton bundleManual = new JRadioButton();
        // Fill the width gap
        bundleManual.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
        bundleManual.setText(Constants.BUNDLE_MODE_MANUAL + " mode");
        bundleManual.setToolTipText(Constants.BUNDLE_MODE_DESCRIPTIONS.get(Constants.BUNDLE_MODE_MANUAL));
        bundleManual.setName("BUNDLE_MODE_MANUAL");
        
        final JRadioButton bundleAdvanced = new JRadioButton();
        bundleAdvanced.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
        bundleAdvanced.setText(Constants.BUNDLE_MODE_ADVANCED + " mode");
        bundleAdvanced.setToolTipText(Constants.BUNDLE_MODE_DESCRIPTIONS.get(Constants.BUNDLE_MODE_ADVANCED));
        bundleAdvanced.setName("BUNDLE_MODE_ADVANCED");
        
        // Group these
        ButtonGroup choiceBundleMode = new ButtonGroup();
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
        		choiceBundleMode.setSelected(bundleSimple.getModel(), true);
        	} else if (Variables.BUNDLE_MODE.equals(Constants.BUNDLE_MODE_MANUAL)) {
        		choiceBundleMode.setSelected(bundleManual.getModel(), true);
        	} else {
        		choiceBundleMode.setSelected(bundleAdvanced.getModel(), true);
        	}
        }
        // Description for previous choice
        final JLabel choiceDescription = new JLabel(Constants.BUNDLE_MODE_DESCRIPTIONS.get(Variables.BUNDLE_MODE), JLabel.TRAILING);
        this.add(new JLabel());
        this.add(choiceDescription);
        
        // New row
        this.add(new JLabel());
        this.add(new JLabel());

        // Description of following action
        final JLabel addSourceFilesPathLabel = new JLabel("Chosen source of files:", JLabel.TRAILING);
        final JLabel addSourceFilesPath = new JLabel((String) Variables.get("BUNDLE_MODE_ADVANCED_PATH"), JLabel.TRAILING);
        
        addSourceFiles = new JButton("Add path of source files");
        addSourceFiles.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
        addSourceFiles.addActionListener (new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				String sourcePath = Files.choosePath();
				if (sourcePath != null && !sourcePath.isEmpty()) {
					try {
						Variables.set("BUNDLE_MODE_ADVANCED_PATH", sourcePath);
	        			addSourceFilesPathLabel.setVisible(true);
	        			addSourceFilesPath.setText(Variables.BUNDLE_MODE_ADVANCED_PATH);
	        			addSourceFilesPath.setVisible(true);
					} catch (Exception ex) {
					}
				}
			}
		});
        
		// Define visibility for fields related to Advanced mode
        if (Variables.isNull("BUNDLE_MODE") || !Variables.get("BUNDLE_MODE").equals(Constants.BUNDLE_MODE_ADVANCED)) {
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

        // One per radio button
        bundleSimple.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
        		if (bundleSimple.isSelected()) {
        			choiceDescription.setText(Constants.BUNDLE_MODE_DESCRIPTIONS.get(Constants.BUNDLE_MODE_SIMPLE));
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
        			choiceDescription.setText(Constants.BUNDLE_MODE_DESCRIPTIONS.get(Constants.BUNDLE_MODE_MANUAL));
        			Variables.set("BUNDLE_MODE", Constants.BUNDLE_MODE_MANUAL);
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
        			choiceDescription.setText(Constants.BUNDLE_MODE_DESCRIPTIONS.get("Advanced"));
        			Variables.set("BUNDLE_MODE", Constants.BUNDLE_MODE_ADVANCED);
        			// Allow to input a path to import package files
        			addSourceFiles.setVisible(true);
        			// Show path information once it was select at least one time
        			if (!Variables.isNull("BUNDLE_MODE_ADVANCED_PATH")) {
        				addSourceFilesPathLabel.setVisible(true);
        				addSourceFilesPath.setVisible(true);
        			}
        		}
			}
        });
       
        // Lay out the panel
        SpringUtilities.makeCompactGrid(this,
                                        9, 2,		//rows, cols
                                        6, 6,		//initX, initY
                                        6, 6);		//xPad, yPad
        
		/* Layout */
//		GroupLayout layout = new GroupLayout(this);
//		this.setLayout(layout);
//
//		layout.setAutoCreateGaps(true);
//		layout.setAutoCreateContainerGaps(true);
//
//		layout.setHorizontalGroup(
//			layout.createParallelGroup()
//				.addGroup(layout.createSequentialGroup()
//						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//								.addComponent(bundleChoiceDescription)
//								)
//						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//							.addComponent(choiceBundleModeBox)
//							)
//						)
//				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//					.addComponent(choiceDescription)
//					)
//				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//					.addComponent(addSourceFiles)
//					)
//				)
//				;
//		layout.setVerticalGroup(
//				layout.createSequentialGroup()
//				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//						.addComponent(bundleChoiceDescription)
//						.addComponent(choiceBundleModeBox)
//				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//						.addComponent(choiceDescription)
//					)
//				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//						.addComponent(addSourceFiles)
//					)
//						)
//				);
	}
}
