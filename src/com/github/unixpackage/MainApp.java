package com.github.unixpackage;

import com.github.unixpackage.components.CommonFrame;
import com.github.unixpackage.data.Arguments;
import com.github.unixpackage.data.Constants;
import com.github.unixpackage.data.UnixPreferences;
import com.github.unixpackage.data.Variables;
import com.github.unixpackage.steps.GeneratePackage;
import com.github.unixpackage.utils.Files;
import com.github.unixpackage.utils.Shell;
import com.github.unixpackage.utils.StepLoader;

import java.awt.Toolkit;

import javax.swing.JPanel;

public class MainApp {

	public static void main(String[] args) {
		// Initialise log
		Files.initialiseLog();
		// Process arguments
		boolean argumentsCorrectlyParsed = Arguments.parseInputArguments(args);

		// When in batch mode, exit with error if some argument
		// was not correctly parsed
		if (!Variables.isNull("BATCH_MODE") && Variables.BATCH_MODE) {
			// Hack. Otherwise, this would not allow remote executions without
			// X11
			Constants.SCREEN_DIMENSION = Toolkit.getDefaultToolkit()
					.getScreenSize();
			if (!argumentsCorrectlyParsed) {
				System.exit(1);
			}
			// Preconditions
			if (Variables.PACKAGE_SOURCE_INSTALL_PAIRS.size() > 0) {
				Variables.set("BUNDLE_MODE", Constants.BUNDLE_MODE_MANUAL);
			} else {
				Variables.set("BUNDLE_MODE", Constants.BUNDLE_MODE_ADVANCED);
			}
			Files.copyScriptSourcesIntoTempFolder();
			if (!Variables.isNull("PACKAGE_TYPE")) {
				// Default: DEB
				if (Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_DEB)) {
					GeneratePackage.generateDebianPackage();
				} else if (Variables.PACKAGE_TYPE
						.equals(Constants.BUNDLE_TYPE_RPM)) {
					GeneratePackage.generateRedHatFiles();
					GeneratePackage.generateRedHatPackage();
				}
			}

			// Save input data to disk once data has been validated
			UnixPreferences prefs = new UnixPreferences();
			prefs.saveToFile();
			Shell.postProcess();
		} else {
			// Schedule a job for the event-dispatching thread:
			// creating and showing this application's GUI.
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// 1st step
					new CommonFrame((JPanel) StepLoader
							.getStepInstance(StepLoader.getStepAt(1)));
				}
			});
		}
	}
}
