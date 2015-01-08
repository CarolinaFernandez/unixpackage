package com.github.unixpackage.utils;

import java.io.File;
import java.io.FileNotFoundException;

import com.github.unixpackage.MainApp;

public class Locations {

	public String getAbsolutePath(String path) {

		File pathDir;
		String checkedPath = null;

		try {
			// Execute on normal file system, first
			pathDir = new File(getClass().getClassLoader().getResource(path)
					.toString());
		} catch (Exception e) {
			// Otherwise, look within jar file
			pathDir = new File("jar:" + getAbsolutePathOfJarFile() + "!/"
					+ path);
		}

		checkedPath = pathDir.toString() + "/";

		return checkedPath;
	}

	private File getAbsolutePathOfJarFile() {
		String path = MainApp.class.getResource(
				MainApp.class.getSimpleName() + ".class").getFile();
		if (path.startsWith("/")) {
			try {
				throw new FileNotFoundException("This is not a jar file: \n"
						+ path);
			} catch (FileNotFoundException e) {
			}
		}
		path = ClassLoader.getSystemClassLoader().getResource(path).getFile();
		return new File(path.substring(0, path.lastIndexOf("!")));
	}
}
