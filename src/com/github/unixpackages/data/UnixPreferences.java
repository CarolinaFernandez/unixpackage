package com.github.unixpackages.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
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
				if (Variables.get("BUNDLE_MODE").equals(
						Constants.BUNDLE_MODE_ADVANCED)) {
					props.setProperty(key, value.toString());
				}
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
				if (Variables.isNull(fieldName) && (fieldValue != null)) {
					Variables.set(fieldName, fieldValue);
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
			props.store(out, "UNIX package information");
		} catch (Exception e) {
			// Nothing happens if the file is not saved
		}
	}
}
