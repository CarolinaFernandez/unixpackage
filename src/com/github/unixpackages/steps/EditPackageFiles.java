package com.github.unixpackages.steps;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;


import com.github.unixpackages.components.CommonStep;
import com.github.unixpackages.data.Constants;
import com.github.unixpackages.data.Variables;
import com.github.unixpackages.utils.Shell;
import com.github.unixpackages.utils.SpringUtilities;

@SuppressWarnings("serial")
public class EditPackageFiles extends CommonStep {

	public EditPackageFiles() {
		// Clear screen first
		this.removeAll();
		this.setLayout(new SpringLayout());

		JLabel splashLabel = new JLabel("Edit any package file by double-clicking it");
		this.add(splashLabel, BorderLayout.CENTER);
		File dir;
		
		// Regenerate temporary files if needed (e.g. folder deleted accidentally)
		Shell.generateTempFiles();
		
		// Default is "DEB"
		if (Variables.PACKAGE_TYPE.equals("RPM")) {
			dir = new File(Constants.TMP_PACKAGE_REDHAT_FILES_PATH);
		} else {
			dir = new File(Constants.TMP_PACKAGE_DEBIAN_FILES_PATH);
		}
		
		// Add files under temporary folder into the list shown to the user
		ArrayList<String> fileList = new ArrayList<String>();
		try {
			for (File file : dir.listFiles()) {
				fileList.add(file.getName());
			}
		} catch (Exception e) {
			System.out.println("E: Could not find UNIX package files. Exception: " + e);
		}
		JList fileChooser = new JList(fileList.toArray());
		// Changes orientation
		fileChooser
				.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		fileChooser.setLayoutOrientation(JList.VERTICAL);
		fileChooser.addMouseListener(new MouseAdapter() {
		    @Override
			public void mouseClicked(MouseEvent evt) {
		        JList list = (JList)evt.getSource();
		        int index = -1;
		        if (evt.getClickCount() >= 2) {
		            index = list.locationToIndex(evt.getPoint());
		            
		            List<String> commandList = new ArrayList<String>();
		            commandList.add("gedit");
		            if (Variables.PACKAGE_TYPE.equals("RPM")) {
		            	commandList.add(Constants.TMP_PACKAGE_REDHAT_FILES_PATH + "/" + list.getModel().getElementAt(index));
		            } else {
		            	commandList.add(Constants.TMP_PACKAGE_DEBIAN_FILES_PATH + "/" + list.getModel().getElementAt(index));
		            }
		            Shell.execute(commandList);
		    	}
		    }
		});

		// Add scroll bar to the fileChooser component
		JScrollPane fileChooserScrollBar = new JScrollPane(fileChooser);
		fileChooserScrollBar.setPreferredSize(Constants.FILECHOOSER_DIMENSION);

		// Never show horizontal scrollbar
		fileChooserScrollBar
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.add(fileChooserScrollBar, BorderLayout.CENTER);

		// Lay out the panel
		SpringUtilities.makeCompactGrid(this, 2, 1, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad
	}
}
