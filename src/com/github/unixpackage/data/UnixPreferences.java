package com.github.unixpackage.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Properties;

public class UnixPreferences {

	private Properties props = new Properties();

	private void setPreferences() {
		Field[] variablesList = Variables.class.getFields();
		ArrayList<Field> variablesArray = new ArrayList<Field>();

		// Filter variables that start with "_" (private use)
		for (Field var : variablesList) {
			if (!var.getName().startsWith("_")) {
				variablesArray.add(var);
			}
		}
		// Then save them to properties object
		for (Field field : variablesArray) {
			String fieldName = field.getName();
			Object fieldValue = Variables.get(fieldName);
			this.setProperty(fieldName, fieldValue);
		}
	}

	private void setProperty(String key, Object value) {
		if (!Variables.isNull(key)) {
			// Extra checks for variables depending on each other
			if (key.equals("BUNDLE_MODE_ADVANCED_PATH")) {
				if (!Variables.isNull("BUNDLE_MODE") && Variables.get("BUNDLE_MODE").equals(
						Constants.BUNDLE_MODE_ADVANCED)) {
					props.setProperty(key, value.toString());
				}
			} else if (key.equals("PACKAGE_SOURCE_INSTALL_PAIRS")) {
				StringBuilder packageSourceInstallPairs = new StringBuilder();
				for (int i = 0; i < Variables.PACKAGE_SOURCE_INSTALL_PAIRS.size(); i++) {
					ArrayList<String> sourceInstallPair = Variables.PACKAGE_SOURCE_INSTALL_PAIRS.get(i);
					if (sourceInstallPair.size() == 2) {
						packageSourceInstallPairs.append(sourceInstallPair.get(0) + Constants.FOLDER_IFS + sourceInstallPair.get(1) + Constants.FOLDER_IFS + Constants.FOLDER_IFS);
//						packageSourceInstallPairs.append(sourceInstallPair.get(0) + ":" + sourceInstallPair.get(1) + ";");
					}
				}
				props.setProperty(key, packageSourceInstallPairs.toString());
			} else {
				props.setProperty(key, value.toString());
			}
		}
	}

	public void loadFromFile() {
		try {
			InputStream in = new FileInputStream(
					Constants.APP_PREFERENCES_FILE_PATH);
			props.load(in);
			in.close();
			// Load file data into variables
			for (Entry<Object, Object> property : props.entrySet()) {
				String fieldName = property.getKey().toString();
				String fieldValue = property.getValue().toString();
				System.out.println("read [" + fieldName + "] -> " + fieldValue);
				if (Variables.isNull(fieldName) && (fieldValue != null)) {
					// Check syntax and parse appropriately for the "source:install" pairs
					//
					// Separation between <source> and <install> within a pair = "\0"
					// Separation between pairs of <source>:<install> = "\0\0"
					if (fieldName.equals("PACKAGE_SOURCE_INSTALL_PAIRS") && !fieldValue.isEmpty()) {
						Variables.PACKAGE_SOURCE_INSTALL_PAIRS = new ArrayList<ArrayList<String>>();
						ArrayList<String> packageSourceInstallPairsFile = new ArrayList<String>(Arrays.asList(fieldValue.split("\\" + Constants.FOLDER_IFS + "\\" + Constants.FOLDER_IFS)));
						for (String packageSourceInstallPair : packageSourceInstallPairsFile) {
							if (!packageSourceInstallPair.isEmpty()) {
								ArrayList<String> packageSourceInstallPairFile = new ArrayList<String>(Arrays.asList(packageSourceInstallPair.split("\\" + Constants.FOLDER_IFS)));
								Variables.PACKAGE_SOURCE_INSTALL_PAIRS.add(packageSourceInstallPairFile);
							}
						}
					} else {
						// Remove external brackets
						fieldValue = fieldValue.replace("[", "").replace("]", "");
						Variables.set(fieldName, fieldValue);
					}
				}
			}
		} catch (Exception e) {
			// Nothing happens if the file is not loaded
		}
	}

	public void saveToFile() {
		// Set preferences first
		this.setPreferences();
		try {
			File directory = new File(Constants.APP_PREFERENCES_PATH);
			if (!directory.exists()) {
				directory.mkdirs();
			}
			File properties = new File(Constants.APP_PREFERENCES_FILE_PATH);
			OutputStream out = new FileOutputStream(properties);
			props.store(out, " UNIX package information\n# File generated automatically. Do NOT change.\n");
		} catch (Exception e) {
			// Nothing happens if the file is not saved
		}
	}
}
