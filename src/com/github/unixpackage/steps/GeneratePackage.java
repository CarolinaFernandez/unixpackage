package com.github.unixpackage.steps;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SpringLayout;

import com.github.unixpackage.components.CommonStep;
import com.github.unixpackage.data.Arguments;
import com.github.unixpackage.data.Constants;
import com.github.unixpackage.data.Variables;
import com.github.unixpackage.utils.Files;
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
	
	/**
	 * Initial step: dh_make (generates Debian sources)
	 * @return
	 */
	public static StringBuilder generateDebianFiles() {
		StringBuilder outputLines = new StringBuilder();
		System.out.println("... packages sources on disk: " + Files.isPackageSourcesOnDisk());
		// Only generate Debian files if not already existing
		if (!Files.isPackageSourcesOnDisk()) {
			List<String> commandList = Arguments.generateArgumentsForDebianFiles();
			System.out.println(".... commands (JUST GENERATED) ===> " + commandList.toArray().toString());
			String command = "bash";
			commandList.add(0, command);
			// Default is "DEB"
			String scriptLocation = Constants.TMP_SCRIPT_DEBIAN_PATH;
			if (!Variables.isNull("PACKAGE_TYPE") && Variables.PACKAGE_TYPE.equals("RPM")) {
				scriptLocation = Constants.TMP_SCRIPT_REDHAT_PATH;
			}
			commandList.add(1, scriptLocation);
			// Run script in non-interactive mode by passing all required arguments
			outputLines = Shell.execute(commandList);
		}
		return outputLines;
	}
	
	/**
	 * Final step: dpkg-buildpackage (generates Debian package)
	 */
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
		
		// Rename edited sample (".ex") files before running the build script (manual mode only)
		if (!Variables.isNull("BUNDLE_MODE") && Variables.BUNDLE_MODE.equals(Constants.BUNDLE_MODE_MANUAL)) {
			if (Variables._PACKAGE_CONTENT_FILES_EDITION_STATUS != null) {
				File[] packageSourcesFiles = Files.getPackageSourcesFiles();
				for (File packageSourcesFile : packageSourcesFiles) {
					String fileName = packageSourcesFile.getName();
					String filePath = packageSourcesFile.getPath();
					// In case the file has been edited, remove any possible ".ex" termination
					if (fileName.matches(".*\\.[eE][xX]$") && Variables._PACKAGE_CONTENT_FILES_EDITION_STATUS.get(fileName).equals("*")) {
						// Rename (remove the ".ex" termination)
						File packageSourcesNewFile = new File(filePath.substring(0,filePath.length()-3));
						packageSourcesFile.renameTo(packageSourcesNewFile);
					}
				}
			}
		}
		
		// Run script in non-interactive mode by passing all required arguments
		return Shell.execute(commandList);
		
		// Open browser in directory where the package is created
//		Shell.execute("xdg-open /");
	}
}
