package com.github.unixpackages.steps;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
//import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SpringLayout;


import com.github.unixpackages.components.CommonStep;
import com.github.unixpackages.data.Constants;
import com.github.unixpackages.data.Variables;
import com.github.unixpackages.utils.Shell;
import com.github.unixpackages.utils.SpringUtilities;

@SuppressWarnings("serial")
public class GeneratePackage extends CommonStep {

//	private static final Logger log = Logger.getLogger(GeneratePackage.class.getCanonicalName());

	public GeneratePackage() {
        // Clear screen first
        this.removeAll();
        this.setLayout(new SpringLayout());
        // Populate the panel
        JLabel splashLabel = new JLabel("Your UNIX package is about to be generated. Check that");
        this.add(splashLabel, BorderLayout.CENTER);
        splashLabel = new JLabel("the information is correct and then press the button");
        this.add(splashLabel, BorderLayout.CENTER);

		// Add sources
		JButton addSourcePath = new JButton("Generate");
		addSourcePath.addActionListener (new ActionListener() {
            public void actionPerformed (ActionEvent e) {
                String command = "bash";
                // Default is "DEB"
                String scriptLocation = Constants.TMP_SCRIPT_DEBIAN_PATH;
                if (Variables.PACKAGE_TYPE.equals("RPM")) {
                	scriptLocation = Constants.TMP_SCRIPT_REDHAT_PATH;
                }
	            List<String> commandList = generateCommandForDebianPackages();
	            commandList.add(0, command);
	            commandList.add(1, scriptLocation);
	            // Run script in non-interactive mode by passing all required arguments
				Shell.execute(commandList);
            }
        });
		this.add(addSourcePath);

        //Lay out the panel
        SpringUtilities.makeCompactGrid(this,
                                        3, 1, //rows, cols
                                        6, 6,        //initX, initY
                                        6, 6);       //xPad, yPad
	}
	
	public List<String> generateCommandForDebianPackages() {
        List<String> commandList = new ArrayList<String>();
        List<String> commandListValidated = new ArrayList<String>();
        HashMap<String, String> argumentList = new HashMap<String, String>();
        
        argumentList.put("-y", null);
        argumentList.put("-s", Variables.PACKAGE_NAME);
        argumentList.put("-v", Variables.PACKAGE_VERSION);
        argumentList.put("-n", Variables.MAINTAINER_NAME);
        argumentList.put("-e", Variables.MAINTAINER_EMAIL);
        // Translate to something understandable by the script
        argumentList.put("-l", Constants.PACKAGE_LICENSES.get(Variables.PACKAGE_LICENSE));
        // Translate to something understandable by the script
        argumentList.put("-C", Constants.PACKAGE_CLASSES.get(Variables.PACKAGE_CLASS));
        argumentList.put("-p", Variables.PACKAGE_NAME);
        // In advanced mode, a path is passed to copy the user's scripts from
        if (!Variables.isNull("BUNDLE_MODE") && Variables.BUNDLE_MODE.equals(Constants.BUNDLE_MODE_ADVANCED)) {
        	argumentList.put("-d", Variables.BUNDLE_MODE_ADVANCED_PATH);
        }
        
        boolean checkArgumentsExist = true;
        for (Entry<String, String> entry : argumentList.entrySet()) {
        	commandListValidated.add(entry.getKey());
        	if (entry.getKey() != "-y") {
            	commandListValidated.add(entry.getValue());
        		if (entry.getValue() == null) {
        			checkArgumentsExist = false;
        			break;
        		}
        	}
        }
        
        // If all arguments required exist, return list with parameters
        if (checkArgumentsExist) {
        	commandList = commandListValidated;
        }
        
        return commandList;
	}
}
