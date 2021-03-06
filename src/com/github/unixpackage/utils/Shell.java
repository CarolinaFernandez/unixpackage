package com.github.unixpackage.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;

import org.apache.commons.io.FileUtils;

import com.github.unixpackage.data.Constants;
import com.github.unixpackage.data.UnixLogger;
import com.github.unixpackage.data.Variables;

public class Shell {

	protected static Process proc;

	public static StringBuilder execute(String commands) {
		List<String> commandList = new ArrayList<String>();
		for (String command : commands.split(" ")) {
			commandList.add(command);
		}
		return Shell.execute(commandList);
	}

	// If writeTo = null, Shell will not output to System.out
	public static StringBuilder execute(String commands, Object writeTo) {
		List<String> commandList = new ArrayList<String>();
		for (String command : commands.split(" ")) {
			commandList.add(command);
		}
		return Shell.execute(commandList, writeTo);
	}

	public static void writeContentTo(String content, Object writeTo) {
		String className = writeTo.getClass().getName();
		if (className.contains("JTextArea")) {
			JTextArea writeToArea = (JTextArea) writeTo;
			// Scrolls the text area to the end of data
			writeToArea.setCaretPosition(writeToArea.getDocument().getLength());
		}
		System.out.println(content);
	}

	public static StringBuilder execute(List<String> commandList) {
		// If no object for output is requested, use System.out for output
		return execute(commandList, System.out);
	}

	public static StringBuilder execute(List<String> commandList, Object writeTo) {
		StringBuilder lines = new StringBuilder();
		try {
			String commandListString = "";
			for (String command : commandList) {
				commandListString += command + " ";
			}
			// When "-m" flag is used, sources are generated
			// Otherwise, packages are created
			if (commandListString.indexOf("bash") > -1) {
				// Default: package
				String operationType = "package";
				if (commandListString.indexOf("-m") > -1) {
					operationType = "files";
				}
				// Check validity of package
				if (Variables.isNull("PACKAGE_TYPE")) {
					Variables.set("PACKAGE_TYPE", Constants.BUNDLE_TYPE_DEB);
				}
				UnixLogger.LOGGER.info("Generating " + Variables.PACKAGE_TYPE
						+ " " + operationType + " with commands: "
						+ commandListString);
			}
			
			// Use ProcessBuilder rather than Runtime.exec
			ProcessBuilder pb = new ProcessBuilder(commandList);
			pb.directory(new File(Constants.ROOT_TMP_PACKAGE_FILES_PATH));
			pb.redirectErrorStream(true);
			proc = pb.start();

			// Read the output from the command
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					proc.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				lines.append(line + "\n");
				if (writeTo != null) {
					System.out.println(line);
				}
			}
			proc.destroy();
		} catch (Exception e) {
			UnixLogger.LOGGER.trace("Could not invoke bash script. Details: "
					+ e);
		}
		return lines;
	}

	public static void outputHelpInformation(String argument) {
		String warning = "Unrecognised argument '" + argument + "'.\n";
		// Provide help on different outputs, depending on the source
		if (!Variables.isNull("BATCH_MODE") && Variables.BATCH_MODE) {
			System.out.println(warning);
		}
		UnixLogger.LOGGER.warn(warning);
		outputHelpInformation();
	}

	public static void outputHelpInformation() {
		String helpOutput = "unixpackage - create a UNIX package, version "
				+ Constants.APP_VERSION
				+ "\n"
				+ "\n"
				+ "This is free software; see the source for copying conditions.  There is NO\n"
				+ "warranty; not even for MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.\n\n"
				+ "Usage: unixpackage [mode] [arguments]\n"
				+ "\n"
				+ " Main modes of operation (pick one):\n"
				+ "  %M1%, %M1l%                        Run process in background\n"
				+ "  %M2%, %M2l%                         Print help information\n"
				+ "  %M3%, %M3l%                      Print version\n"
				+ "\n"
				+ " Arguments (required in batch mode - when no config file is available):\n"
				+ "  %R1%, %R1l% <name>                  Full name of the package maintainer\n"
				+ "  %R2%, %R2l% <address>              E-mail address of the package maintainer\n"
				+ "  %R3%, %R3l% <class>                Package class\n"
				+ "                                      ";
		if (Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_DEB)) {
			helpOutput += Constants.RE_PACKAGE_CLASS_DEB.toString()
					.replace("^", "").replace("$", "")
					+ "\n";
		} else {
			helpOutput += Constants.RE_PACKAGE_CLASS_RPM.toString()
					.replace("^", "").replace("$", "")
					+ "\n";
		}
		helpOutput += "  %R4%, %R4l% <type>             Use <type> of licence in copyright file\n"
				+ "                                      ";
		if (Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_DEB)) {
			helpOutput += Constants.RE_PACKAGE_LICENCE_DEB.toString()
					.replace("^", "").replace("$", "")
					+ "\n";
		} else {
			helpOutput += Constants.RE_PACKAGE_LICENCE_RPM.toString()
					.replace("^", "").replace("$", "")
					+ "\n";
		}
		helpOutput += "  %R5%, %R5l% <name>          Package name (better use lowercase, digits, dashes)\n"
				+ "  %R6%, %R6l% <version>    Version of the package\n"
				+ "\n"
				+ " Arguments (optional - provide extra information or operations):\n"
				// + "  %O1%, %O1l% <dir>                 Source directory\n"
				+ "  %O2%, %O2l%                         Sign package. This will use name and email to look\n"
				+ "                                     for a matching GPG key on the system.\n"
				+ "  %O3%, %O3l% <text>     Description of the package (up to 60 characters)\n"
				+ "  %O4%, %O4l% <text>      Detailed description of the package\n"
				+ "  %O5%, %O5l% <section>            Section to which the package belongs\n"
				+ "                                      ";
		if (Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_DEB)) {
			helpOutput += Constants.RE_PACKAGE_SECTION_DEB.toString()
					.replace("^", "").replace("$", "")
					+ "\n";
		} else {
			helpOutput += Constants.RE_PACKAGE_GROUP_RPM.toString()
					.replace("^", "").replace("$", "")
					+ "\n";
		}
		if (Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_DEB)) {
			helpOutput += "  %O6%, %O6l% <section>           Level of priority used for this package\n"
					+ "                                       (required|important|standard|optional|extra)\n";
		}
		helpOutput += "  %O7%, %O7l% <url>                URL of the software upstream homepage\n"
				+ "  %O8%, %O8l% <dir>              Use customizing templates in <dir> for dh_make (advanced mode)\n"
				+ "  %O9%, %O9l% <path:path>            Paths to files to be added (manual mode)\n"
				+ "\n"
				+ "Examples:\n"
				+ "  Refer to the manpages of this package at unixpackage (8).\n";

		// Replace proper arguments into place holders
		helpOutput = helpOutput.replace("%M1%", Constants.ARGUMENT_BATCH);
		helpOutput = helpOutput.replace("%M1l%", Constants.ARGUMENT_BATCH_LONG);
		helpOutput = helpOutput.replace("%M2%", Constants.ARGUMENT_HELP);
		helpOutput = helpOutput.replace("%M2l%", Constants.ARGUMENT_HELP_LONG);
		helpOutput = helpOutput.replace("%M3%", Constants.ARGUMENT_VERSION);
		helpOutput = helpOutput.replace("%M3l%",
				Constants.ARGUMENT_VERSION_LONG);

		helpOutput = helpOutput.replace("%R1%", Constants.ARGUMENT_NAME);
		helpOutput = helpOutput.replace("%R1l%", Constants.ARGUMENT_NAME_LONG);
		helpOutput = helpOutput.replace("%R2%", Constants.ARGUMENT_EMAIL);
		helpOutput = helpOutput.replace("%R2l%", Constants.ARGUMENT_EMAIL_LONG);
		helpOutput = helpOutput.replace("%R3%", Constants.ARGUMENT_CLASS);
		helpOutput = helpOutput.replace("%R3l%", Constants.ARGUMENT_CLASS_LONG);
		helpOutput = helpOutput.replace("%R4%", Constants.ARGUMENT_COPYRIGHT);
		helpOutput = helpOutput.replace("%R4l%",
				Constants.ARGUMENT_COPYRIGHT_LONG);
		helpOutput = helpOutput
				.replace("%R5%", Constants.ARGUMENT_PACKAGE_NAME);
		helpOutput = helpOutput.replace("%R5l%",
				Constants.ARGUMENT_PACKAGE_NAME_LONG);
		helpOutput = helpOutput.replace("%R6%",
				Constants.ARGUMENT_PACKAGE_VERSION);
		helpOutput = helpOutput.replace("%R6l%",
				Constants.ARGUMENT_PACKAGE_VERSION_LONG);

		// helpOutput = helpOutput.replace("%O1%", Constants.ARGUMENT_SOURCE);
		// helpOutput = helpOutput
		// .replace("%O1l%", Constants.ARGUMENT_SOURCE_LONG);
		helpOutput = helpOutput.replace("%O2%", Constants.ARGUMENT_SIGN);
		helpOutput = helpOutput.replace("%O2l%", Constants.ARGUMENT_SIGN_LONG);
		helpOutput = helpOutput.replace("%O3%",
				Constants.ARGUMENT_DESCRIPTION_SHORT);
		helpOutput = helpOutput.replace("%O3l%",
				Constants.ARGUMENT_DESCRIPTION_SHORT_LONG);
		helpOutput = helpOutput.replace("%O4%", Constants.ARGUMENT_DESCRIPTION);
		helpOutput = helpOutput.replace("%O4l%",
				Constants.ARGUMENT_DESCRIPTION_LONG);
		helpOutput = helpOutput.replace("%O5%",
				Constants.ARGUMENT_PACKAGE_SECTION);
		helpOutput = helpOutput.replace("%O5l%",
				Constants.ARGUMENT_PACKAGE_SECTION_LONG);
		helpOutput = helpOutput.replace("%O6%",
				Constants.ARGUMENT_PACKAGE_PRIORITY);
		helpOutput = helpOutput.replace("%O6l%",
				Constants.ARGUMENT_PACKAGE_PRIORITY_LONG);
		helpOutput = helpOutput.replace("%O7%", Constants.ARGUMENT_WEBSITE);
		helpOutput = helpOutput.replace("%O7l%",
				Constants.ARGUMENT_WEBSITE_LONG);
		helpOutput = helpOutput.replace("%O8%", Constants.ARGUMENT_TEMPLATES);
		helpOutput = helpOutput.replace("%O8l%",
				Constants.ARGUMENT_TEMPLATES_LONG);
		helpOutput = helpOutput.replace("%O9%", Constants.ARGUMENT_FILES);
		helpOutput = helpOutput.replace("%O9l%", Constants.ARGUMENT_FILES_LONG);

		// Provide help on different outputs, depending on the source
		if (Variables.BATCH_MODE) {
			System.out.println(helpOutput);
		}
		UnixLogger.LOGGER.info(helpOutput);
	}

	public static void outputVersionInformation() {
		UnixLogger.LOGGER.info("Version: " + Constants.APP_VERSION);
	}

	public static boolean cleanTempFiles() {
		File tempDir = new File(Constants.ROOT_TMP_PACKAGE_FILES_PATH);
		boolean result = false;

		// Remove temporal folder and all its contents
		try {
			FileUtils.deleteDirectory(tempDir);
			result = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean postProcess() {
		return Shell.cleanTempFiles();
	}
}