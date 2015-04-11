package com.github.unixpackage;

//import java.lang.reflect.Field;

import com.github.unixpackage.components.CommonFrame;
import com.github.unixpackage.data.Arguments;
import com.github.unixpackage.data.Constants;
import com.github.unixpackage.data.UnixPreferences;
import com.github.unixpackage.data.Variables;
import com.github.unixpackage.steps.GeneratePackage;
import com.github.unixpackage.utils.Files;
//import com.github.unixpackage.utils.Shell;
import com.github.unixpackage.utils.StepLoader;

import javax.swing.JPanel;

public class MainApp {

	public static void main(String[] args) {
		// Process arguments
		boolean argumentsCorrectlyParsed = Arguments.parseInputArguments(args);
		
		// When in batch mode, exit with error if some argument
		// was not correctly parsed
		if (!Variables.isNull("BATCH_MODE") && Variables.BATCH_MODE) {
			if (!argumentsCorrectlyParsed) {
				System.out.println("ERRORS PARSING");
				System.exit(1);
			}
			// Preconditions
			Variables.set("BUNDLE_MODE", Constants.BUNDLE_MODE_ADVANCED);
			// Generate files before preparing packages
//			Shell.generateTempFiles();
			Files.copyScriptSourcesIntoTempFolder();
			GeneratePackage.generateDebianPackage();
			// Save input data to disk once data has been validated
			UnixPreferences prefs = new UnixPreferences();
			prefs.saveToFile();
			
			// XXX DEBUG
//			System.out.println("---VARIABLES---");
//			for(Field var : Variables.class.getFields()) {
//				try {
//					System.out.println("> " + var + ": " + Variables.get(var.getName()));
//				} catch (Exception e) {
//				}
//			}
		} else {
			// Schedule a job for the event-dispatching thread:
			// creating and showing this application's GUI.
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// 1st step
					new CommonFrame((JPanel) StepLoader.getStepInstance(StepLoader
							.getStepAt(1)));
				}
			});
		}
	}
}
