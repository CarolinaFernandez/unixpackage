package com.github.unixpackage.steps;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

import com.github.unixpackage.components.CommonStep;
import com.github.unixpackage.data.Arguments;
import com.github.unixpackage.data.Constants;
import com.github.unixpackage.data.Variables;
import com.github.unixpackage.utils.Files;
import com.github.unixpackage.utils.GenerateSourcesListener;
import com.github.unixpackage.utils.Shell;
import com.github.unixpackage.utils.SpringUtilities;

@SuppressWarnings("serial")
public class GeneratePackage extends CommonStep {

//    public static JTextArea textArea = new JTextArea();
    public static JTextArea textArea = new JTextArea();
    public JScrollPane textareaScrollPane;
    public static String textAreaContent;
    public static boolean replace = true;
    
	// private static final Logger log =
	// Logger.getLogger(GeneratePackage.class.getCanonicalName());
	
	public GeneratePackage() {
		// Clear screen first
		this.removeAll();
		this.setLayout(new SpringLayout());

		// Populate the panel
		JLabel splashLabel = new JLabel(
				"Ensure the information is correct, then press the button to generate your UNIX package.");
		this.add(splashLabel, BorderLayout.CENTER);
		splashLabel = new JLabel(
				"Your " + Variables.PACKAGE_TYPE + " package will be shown in a new browser.");
		this.add(splashLabel, BorderLayout.CENTER);

		// New row
		this.add(new JLabel());

		textArea = new JTextArea();
		JLabel rerunLabel = new JLabel("To generate a new package, rerun the application");
		if (!Variables._PACKAGE_GENERATED) {
			textArea.setEditable(false);
			textArea.setVisible(false);
			rerunLabel.setVisible(false);
		} else {
			// Set the previously generated contents in the textarea
			textArea.setText(Variables._PACKAGE_GENERATED_OUTPUT);
		}
		textareaScrollPane = new JScrollPane(textArea);
		textareaScrollPane.setVerticalScrollBarPolicy(
		                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		textareaScrollPane.setPreferredSize(new Dimension(Constants.SCREEN_DIMENSION.width/2, Constants.SCREEN_DIMENSION.height/6));
		if (!Variables._PACKAGE_GENERATED) {
			textareaScrollPane.setVisible(false);
			rerunLabel.setVisible(false);
		}
		
		// Set black colour for background, green colour for text, and set border
		textareaScrollPane.setOpaque(false);
		textareaScrollPane.getViewport().setOpaque(false);
		textArea.setBackground(new Color(0, 0, 0));
		textArea.setForeground(new Color(149, 242, 138));
		
		// -- Original
		// Generate sources
		JButton generateSources = new JButton("Generate");
		
		if (Variables._PACKAGE_GENERATED) {
			generateSources.setEnabled(false);
		}
		this.add(generateSources);
		
		// Actions for button set on listener
		ActionListener actionListener = new GenerateSourcesListener(textareaScrollPane, textArea, rerunLabel);
		generateSources.addActionListener(actionListener);
		
		// Block "Generate" button after packet is processed
//		addSourcePath.setEnabled(!Variables._PACKAGE_GENERATED);

		// New row
		this.add(new JLabel());
		
		this.add(textareaScrollPane);

		// New row
		this.add(new JLabel());

		// Final information
		this.add(rerunLabel, BorderLayout.CENTER);
		
		// Lay out the panel
		SpringUtilities.makeCompactGrid(this, 4 + 2 + 2, 1, // rows, cols
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
		String scriptLocation = Constants.TMP_SCRIPT_DEBIAN_PATH;
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
		
		// Open browser in directory where the package is created
		commandList.add(3, "&& " + Constants.OPEN_COMMAND + " $(ls -lt | grep " + Constants.APP_NAME + " | head -n 1 | cut -d -f 9)");
		// Set flag to true after package generation
		Variables._PACKAGE_GENERATED = true;
		
		StringBuilder resultExecute = new StringBuilder();
		resultExecute = Shell.execute(commandList);
        Variables._PACKAGE_GENERATED_OUTPUT = resultExecute.toString();
		// Show output package in the browser
        GeneratePackage.openOutputPath();
        
        return resultExecute;
	}
	
	/**
	 * Initial step: dh_make (generates RedHat sources)
	 * @return
	 */
	public static StringBuilder generateRedHatFiles() {
		StringBuilder outputLines = new StringBuilder();
		System.out.println("... packages sources on disk: " + Files.isPackageSourcesOnDisk());
		// Only generate RedHat files if not already existing
		if (!Files.isPackageSourcesOnDisk()) {
			List<String> commandList = Arguments.generateArgumentsForRedHatFiles();
			String command = "bash";
			commandList.add(0, command);
			// Default is "DEB"
			String scriptLocation = Constants.TMP_SCRIPT_REDHAT_PATH;
			commandList.add(1, scriptLocation);
			// Run script in non-interactive mode by passing all required arguments
			outputLines = Shell.execute(commandList);
		}
		return outputLines;
	}
	
	/**
	 * Final step: rpmbuild (generates RedHat package)
	 */
	public static StringBuilder generateRedHatPackage() {
		List<String> commandList = Arguments.generateArgumentsForRedHatPackage();
		String command = "bash";
		commandList.add(0, command);
		String scriptLocation = Constants.TMP_SCRIPT_REDHAT_PATH;
		commandList.add(1, scriptLocation);
		
		// Open browser in directory where the package is created
//		commandList.add(3, "&& " + Constants.OPEN_COMMAND + " $(ls -lt | grep " + Constants.APP_NAME + " | head -n 1 | cut -d -f 9)");
		// Set flag to true after package generation
		Variables._PACKAGE_GENERATED = true;
		
		StringBuilder resultExecute = new StringBuilder();
		resultExecute = Shell.execute(commandList);
        Variables._PACKAGE_GENERATED_OUTPUT = resultExecute.toString();
		// Show output package in the browser
        GeneratePackage.openOutputPath();
        
        return resultExecute;
	}
	
	public static void openOutputPath() {
		if (!Variables.BATCH_MODE) {
			// Retrieve output and show in browser
	        String textAreaText = Variables._PACKAGE_GENERATED_OUTPUT;
	        Pattern pattern = Pattern.compile("The output is located under (.+)/");
	        Matcher matcher = pattern.matcher(textAreaText);
	        String outputPathDebianPackage = "";
	        while (matcher.find()) {
	        	outputPathDebianPackage = matcher.group(1);
	        }
			// Default: DEB
//			String xdgCheck = "[[ ! -z $(dpkg -l | grep xdg-utils) ]]";
//			if (Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_RPM)) {
//				xdgCheck = "[[ ! -z $(yum list | grep xdg-utils) ]]";
//			}
//	        Shell.execute(xdgCheck + " && xdg-open " + outputPathDebianPackage);
	        Shell.execute("xdg-open " + outputPathDebianPackage);
		}
	}
}
