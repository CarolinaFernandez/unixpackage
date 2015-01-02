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
		String s = null;
		try {
			// Seems like the proper way to do it
			/*
            ProcessBuilder pb = new ProcessBuilder(commandList);
            System.out.println("Sent command: " + commandList);
            for (String argument : pb.command()) {
            	System.out.println("> Argument: " + argument.toString());
            }
            Map<String, String> env = pb.environment();
//            env.put("VAR1", "myValue");
//            env.remove("OTHERVAR");
//            env.put("VAR2", env.get("VAR1") + "suffix");
            pb.directory(new File(Constants.DEBIAN_SCRIPT_PATH));
            proc = pb.start();
            */
			
            // Cheap way to do it
			System.out.println("Command list.... " + commandList.toString());
//			proc = Runtime.getRuntime().exec(commandList.toString());
			// Ugly hack
			String arguments = "";
			for (String command : commandList) {
				arguments += " " + command;
			}
			proc = Runtime.getRuntime().exec(arguments);
            
            BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader error = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            
            // Read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            while ((s = input.readLine()) != null) {
                System.out.println(s);
            }
             
            // Read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = error.readLine()) != null) {
                System.out.println(s);
            }
            //System.exit(0);
		} catch (Exception e) {
			//System.exit(-1);
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
