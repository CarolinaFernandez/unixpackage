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
		}
		return lines;
	}

	public static boolean generateTempFiles() {
		boolean result = false;
		Files files = new Files();
		result = files.copyPackageSourcesIntoTempFolder();
		result &= files.copyScriptSourcesIntoTempFolder();
		return result;
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
