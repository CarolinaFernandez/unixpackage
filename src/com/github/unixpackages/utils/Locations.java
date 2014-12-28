package com.github.unixpackages.utils;

import java.io.File;
import com.github.unixpackages.data.Constants;

public class Locations {

	public String getAbsolutePath(String path) {
		
		String checkedPath = null;
		// Local folder for package files
		File pathDir = new File(path);
		
		// The folder may exist if program ran from source code
		if (pathDir.exists()) {
			checkedPath = pathDir.toString();
			return checkedPath;
		}
		
		// Otherwise, it is the JAR file, with its own paths
		String ROOT_PACKAGE_FILES_PATH = Constants.ROOT_PACKAGE_FILES_PATH;
		ROOT_PACKAGE_FILES_PATH = ROOT_PACKAGE_FILES_PATH.substring(ROOT_PACKAGE_FILES_PATH.lastIndexOf("/") + 1);
		pathDir = new File(getClass().getClassLoader().getResource(ROOT_PACKAGE_FILES_PATH).toString());
		
		// Path not understandable by normal FileSystem operations, cannot check if it exists...
		checkedPath = pathDir.toString();
		return checkedPath;
	}
}
