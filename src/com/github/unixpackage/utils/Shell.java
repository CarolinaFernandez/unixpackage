package com.github.unixpackage.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.github.unixpackage.data.Constants;

public class Shell {

	protected static Process proc;

	public static StringBuilder execute(String commands) {
		List<String> commandList = new ArrayList<String>();
		for (String command : commands.split(" ")) {
			commandList.add(command);
		}
		return execute(commandList);
	}
	
	public static StringBuilder execute(List<String> commandList) {
		StringBuilder lines = new StringBuilder();
		try {
			// Use ProcessBuilder rather than Runtime.exec
			ProcessBuilder pb = new ProcessBuilder(commandList);
			pb.directory(new File(Constants.ROOT_TMP_PACKAGE_FILES_PATH));
			proc = pb.start();

			// Read the output from the command
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					proc.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				lines.append(line);
				System.out.println(line);
			}
		} catch (Exception e) {
			System.out.println("Error: could not invoke bash script. Details: " + e);
		}
		return lines;
	}

	public static boolean generateTempFiles() {
		boolean result = false;
		result = Files.copyPackageSourcesIntoTempFolder();
		result &= Files.copyScriptSourcesIntoTempFolder();
		return result;
	}
	
	public static void outputHelpInformation() {
		String helpOutput = "unixpackage - create a UNIX package, version " + Constants.APP_VERSION + "\n"
		+ "\n"
		+ "This is free software; see the source for copying conditions.  There is NO\n"
		+ "warranty; not even for MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.\n"
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
		+ "  %R3%, %R3l% <class>                Package class (s|i|k|l|m|n)\n"
		+ "  %R4%, %R4l% <type>             Use <type> of licence in copyright file\n"
        + "                                     (apache|artistic|bsd|gpl|gpl2|gpl3|lgpl|lgpl2|\n"
        + "                                     lgpl3|mit)\n"
		+ "  %R5%, %R5l% <name>          Package name (better use lowercase, digits,\n"
        + "                                     dashes)\n"
		+ "  %R6%, %R6l% <version>    Version of the package\n"
		+ "\n"
		+ " Arguments (optional - provide extra information or operations):\n"
		+ "  %O1%, %O1l% <dir>                 Source directory (???)\n"
		+ "  %O2%, %O2l%                         Sign package. This will use name and email to look\n"
		+ "                                     for a matching GPG key on the system.\n"
		+ "  %O3%, %O3l% <text>     Description of the package (up to 60 characters)\n"
		+ "  %O4%, %O4l% <text>      Detailed description of the package\n"
		+ "  %O5%, %O5l% <url>                URL of the software upstream homepage\n"
		+ "  %O6%, %O6l% <dir>              Use customizing templates in <dir> for dh_make\n"
		+ "\n"
		+ " Examples:\n"
		+ "  unixpackage %M2% %R1% Jane Doe %R2% jane.doe@trivi.al %R4% m %R5% lgpl3 -S \\\n"
		+ "  %O3% Trivial package %O4% Trivial package that does trivial stuff \\\n"
		+ "  %O5% /root/path/for/deb/templates %O1% trivialpackage %R6% trivialpackage \\\n"
		+ "  %R7% 0.1 %R3% http://trivial.package.com\n";
		
		// Replace proper arguments into place holders
		helpOutput = helpOutput.replace("%M1%", Constants.ARGUMENT_BATCH);
		helpOutput = helpOutput.replace("%M1l%", Constants.ARGUMENT_BATCH_LONG);
		helpOutput = helpOutput.replace("%M2%", Constants.ARGUMENT_HELP);
		helpOutput = helpOutput.replace("%M2l%", Constants.ARGUMENT_HELP_LONG);
		helpOutput = helpOutput.replace("%M3%", Constants.ARGUMENT_VERSION);
		helpOutput = helpOutput.replace("%M3l%", Constants.ARGUMENT_VERSION_LONG);
		
		helpOutput = helpOutput.replace("%R1%", Constants.ARGUMENT_NAME);
		helpOutput = helpOutput.replace("%R1l%", Constants.ARGUMENT_NAME_LONG);
		helpOutput = helpOutput.replace("%R2%", Constants.ARGUMENT_EMAIL);
		helpOutput = helpOutput.replace("%R2l%", Constants.ARGUMENT_EMAIL_LONG);
		helpOutput = helpOutput.replace("%R3%", Constants.ARGUMENT_CLASS);
		helpOutput = helpOutput.replace("%R3l%", Constants.ARGUMENT_CLASS_LONG);
		helpOutput = helpOutput.replace("%R4%", Constants.ARGUMENT_COPYRIGHT);
		helpOutput = helpOutput.replace("%R4l%", Constants.ARGUMENT_COPYRIGHT_LONG);
		helpOutput = helpOutput.replace("%R5%", Constants.ARGUMENT_PACKAGE_NAME);
		helpOutput = helpOutput.replace("%R5l%", Constants.ARGUMENT_PACKAGE_NAME_LONG);
		helpOutput = helpOutput.replace("%R6%", Constants.ARGUMENT_PACKAGE_VERSION);
		helpOutput = helpOutput.replace("%R6l%", Constants.ARGUMENT_PACKAGE_VERSION_LONG);

		helpOutput = helpOutput.replace("%O1%", Constants.ARGUMENT_SOURCE);
		helpOutput = helpOutput.replace("%O1l%", Constants.ARGUMENT_SOURCE_LONG);
		helpOutput = helpOutput.replace("%O2%", Constants.ARGUMENT_SIGN);
		helpOutput = helpOutput.replace("%O2l%", Constants.ARGUMENT_SIGN_LONG);
		helpOutput = helpOutput.replace("%O3%", Constants.ARGUMENT_DESCRIPTION_SHORT);
		helpOutput = helpOutput.replace("%O3l%", Constants.ARGUMENT_DESCRIPTION_SHORT_LONG);
		helpOutput = helpOutput.replace("%O4%", Constants.ARGUMENT_DESCRIPTION);
		helpOutput = helpOutput.replace("%O4l%", Constants.ARGUMENT_DESCRIPTION_LONG);
		helpOutput = helpOutput.replace("%O5%", Constants.ARGUMENT_WEBSITE);
		helpOutput = helpOutput.replace("%O5l%", Constants.ARGUMENT_WEBSITE_LONG);
		helpOutput = helpOutput.replace("%O6%", Constants.ARGUMENT_TEMPLATES);
		helpOutput = helpOutput.replace("%O6l%", Constants.ARGUMENT_TEMPLATES_LONG);
		
		System.out.println(helpOutput);
	}
	
	public static void outputVersionInformation() {
		System.out.println(Constants.APP_VERSION);
	}

	public static boolean preProcess() {
		return Shell.generateTempFiles();
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
