package com.github.unixpackage.data;

import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;

public class Variables {

	/**
	 * Information set by user
	 */
	// Batch mode (default is false)
	public static Boolean BATCH_MODE = false;
	public static Boolean _GPG_KEY_EXISTS = false;
	public static String MAINTAINER_EMAIL;
	public static String MAINTAINER_NAME;
	public static String PACKAGE_NAME;
	public static String PACKAGE_SHORT_DESCRIPTION;
	public static String PACKAGE_DESCRIPTION;
	public static String PACKAGE_WEBSITE;
	public static String PACKAGE_VERSION;
	public static String _PACKAGE_INSTALL_PATH;
	public static String PACKAGE_LICENCE;
	public static String PACKAGE_CLASS;
	public static String PACKAGE_SECTION;
	public static String PACKAGE_PRIORITY;
	public static Boolean PACKAGE_SIGN;
	public static ArrayList<ArrayList<String>> PACKAGE_SOURCE_INSTALL_PAIRS;
	// Can be "DEB" or "RPM"
	public static String PACKAGE_TYPE;
	public static String _DEB_PACKAGE;
	public static String _RPM_PACKAGE;
	// Can be "Simple", "Manual" or "Advanced"
	public static String BUNDLE_MODE;
	public static String _BUNDLE_MODE_SIMPLE;
	public static String _BUNDLE_MODE_MANUAL;
	public static String _BUNDLE_MODE_ADVANCED;
	public static String BUNDLE_MODE_ADVANCED_PATH;
	// Package files to be edited
	public static ArrayList<ArrayList<String>> _PACKAGE_CONTENT_FILES;
	public static HashMap<String,String> _PACKAGE_CONTENT_FILES_HASH;
	public static HashMap<String,String> _PACKAGE_CONTENT_FILES_MODIFIED_HASH;
	public static HashMap<String,String> _PACKAGE_CONTENT_FILES_EDITION_STATUS;
	
	public static Object get(String key) {
		Object value = null;
		try {
			try {
				value = Variables.class.getField(key).get(key);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return value;
	}

	public static boolean isNull(String key) {
		return (Variables.get(key) == null);
	}

	public static void set(String key, String value) {
		Object e;
		try {
			e = Variables.get(key);
			String variableType = Variables.class.getField(key).getType()
					.getCanonicalName();
			if (ArrayList.class.getCanonicalName().equals(variableType)) {
				ArrayList<ArrayList<String>> valueModified = new ArrayList<ArrayList<String>>();
				// Remove enclosing brackets first
				value = value.substring(1, value.length() - 1);
				ArrayList<String> pairValue = new ArrayList<String>();
				// Split pairs
				if (value.indexOf("],") >= 0) {
					for (String splittedValue : value.split("],")) {
						// Then clean and add to array
						splittedValue = splittedValue.replace("[", "").replace(
								"]", "");
						String[] splittedValuePair = splittedValue.split(",");
						pairValue = new ArrayList<String>();
						pairValue.add(splittedValuePair[0].trim());
						pairValue.add(splittedValuePair[1].trim());
						valueModified.add(pairValue);
					}
				} else {
					value = value.substring(1, value.length() - 1);
					value = value.replace("[", "").replace("]", "");
					String[] splittedValuePair = value.split(",");
					pairValue = new ArrayList<String>();
					pairValue.add(splittedValuePair[0].trim());
					pairValue.add(splittedValuePair[1].trim());
					valueModified.add(pairValue);
				}
				Variables.class.getField(key).set(e, valueModified);
			} else if (Boolean.class.getCanonicalName().equals(variableType)) {
				Variables.class.getField(key).set(e, new Boolean(value));
			} else {
				// Should be a String here
				Variables.class.getField(key).set(e, value);
			}
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (NoSuchFieldException e1) {
			e1.printStackTrace();
		}
	}
}
