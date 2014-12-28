package com.github.unixpackages.data;

import java.lang.String;
import java.util.ArrayList;

public class Variables {

	/**
	 * Information set by user
	 */
	public static boolean GPG_KEY_EXISTS = false;
	public static String MAINTAINER_EMAIL;
    public static String MAINTAINER_NAME;
    public static String PACKAGE_NAME;
    public static String PACKAGE_SHORT_DESCRIPTION;
    public static String PACKAGE_DESCRIPTION;
    public static String PACKAGE_VERSION;
    public static String PACKAGE_INSTALL_PATH;
    public static String PACKAGE_LICENSE;
    public static String PACKAGE_CLASS;
    public static String PACKAGE_SIGN;
    public static ArrayList<ArrayList<String>> PACKAGE_SOURCE_INSTALL_PAIRS;
    public static String PACKAGE_SOURCE_PATH;
    // Can be 'DEB' or 'RPM'
    public static String PACKAGE_TYPE;
    public static String DEB_PACKAGE;
    public static String RPM_PACKAGE;

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
			Variables.class.getField(key).set(e, value);
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
