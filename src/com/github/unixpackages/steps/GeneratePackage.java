package com.github.unixpackages.steps;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
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
                
	            List<String> commandList = new ArrayList<String>();
	            commandList.add(command);
	            commandList.add(scriptLocation);
	            
	            // Non-interactive mode
	            commandList.add("-y");

	            commandList.add("-s");
	            commandList.add(Variables.PACKAGE_NAME);
	            
	            commandList.add("-v");
	            commandList.add(Variables.PACKAGE_VERSION);
	            
	            commandList.add("-n");
	            commandList.add(Variables.MAINTAINER_NAME);
	            
	            commandList.add("-e");
	            commandList.add(Variables.MAINTAINER_EMAIL);
	            
	            commandList.add("-l");
	            // Translate to something understandable by the script
	            commandList.add(Constants.PACKAGE_LICENSES.get(Variables.PACKAGE_LICENSE));
	            
	            commandList.add("-C");
	            // Translate to something understandable by the script
	            commandList.add(Constants.PACKAGE_CLASSES.get(Variables.PACKAGE_CLASS));
	            
	            commandList.add("-p");
	            commandList.add(Variables.PACKAGE_NAME);
	            
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
}
