package com.github.unixpackages.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.github.unixpackages.data.Constants;

public class Shell {

	protected static Process proc;

	public static void execute(List<String> commandList) {
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
				System.out.println(line);
			}

			// Read the output from the command
			// System.out.println("Here is the standard output of the command:\n");
			// while (out != null) {
			// System.out.println(out);
			// }

			// Read any errors from the attempted command
			// System.out.println("Here is the standard error of the command (if any):\n");
			// while (error != null) {
			// System.out.println(error);
			// }

			/*
			 * // Ugly way to run from console... proc =
			 * Runtime.getRuntime().exec(commandList.toString()); BufferedReader
			 * input = new BufferedReader(new
			 * InputStreamReader(proc.getInputStream())); BufferedReader error =
			 * new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			 * // Read the output from the command
			 * System.out.println("Here is the standard output of the command:\n"
			 * ); while ((s = input.readLine()) != null) {
			 * System.out.println(s); } // Read any errors from the attempted
			 * command System.out.println(
			 * "Here is the standard error of the command (if any):\n"); while
			 * ((s = error.readLine()) != null) { System.out.println(s); }
			 */
		} catch (Exception e) {
		}
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
