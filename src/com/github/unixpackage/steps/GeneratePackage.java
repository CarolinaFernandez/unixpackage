package com.github.unixpackage.steps;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SpringLayout;

import com.github.unixpackage.components.CommonStep;
import com.github.unixpackage.data.Arguments;
import com.github.unixpackage.data.Constants;
import com.github.unixpackage.data.Variables;
import com.github.unixpackage.utils.Shell;
import com.github.unixpackage.utils.SpringUtilities;

@SuppressWarnings("serial")
public class GeneratePackage extends CommonStep {

	// private static final Logger log =
	// Logger.getLogger(GeneratePackage.class.getCanonicalName());
	
	public GeneratePackage() {
		// Clear screen first
		this.removeAll();
		this.setLayout(new SpringLayout());

		// Populate the panel
		JLabel splashLabel = new JLabel(
				"Your UNIX package is about to be generated. Check that");
		this.add(splashLabel, BorderLayout.CENTER);
		splashLabel = new JLabel(
				"the information is correct and then press the button");
		this.add(splashLabel, BorderLayout.CENTER);

		// New row
		this.add(new JLabel());

		// Add sources
		JButton addSourcePath = new JButton("Generate");
		addSourcePath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					GeneratePackage.generateDebianPackage();
			}
		});
		this.add(addSourcePath);

		// New row
		this.add(new JLabel());

		// Final information
		JLabel outputLabel = new JLabel("You can find the output of the script in the console");
		this.add(outputLabel);

		// Lay out the panel
		SpringUtilities.makeCompactGrid(this, 4 + 2, 1, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad
	}
	
	public static StringBuilder generateDebianPackage() {
		List<String> commandList = Arguments.generateArgumentsForDebianPackage();
		String command = "bash";
		commandList.add(0, command);
		// Default is "DEB"
		String scriptLocation = Constants.TMP_SCRIPT_DEBIAN_PATH;
		if (!Variables.isNull("PACKAGE_TYPE") && Variables.PACKAGE_TYPE.equals("RPM")) {
			scriptLocation = Constants.TMP_SCRIPT_REDHAT_PATH;
		}
		commandList.add(1, scriptLocation);
		// Run script in non-interactive mode by passing all required arguments
		return Shell.execute(commandList);
		// Open browser in directory where the package is created
//		Shell.execute("xdg-open /");
	}
}
