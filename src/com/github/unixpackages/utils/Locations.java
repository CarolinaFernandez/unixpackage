package com.github.unixpackages.utils;

import java.io.File;

public class Locations {

	public String getAbsolutePath(String path) {
		
		File pathDir;
		String checkedPath = null;

		try {
			// Execute on normal file system, first
			pathDir = new File(getClass().getClassLoader().getResource(path).toString());
		} catch (Exception e) {
			// Otherwise, look within jar file
			pathDir = new File("jar:file:CreateUNIXPackage.jar!/" + path);
		}
		
		checkedPath = pathDir.toString() + "/";

		return checkedPath;
	}
}
