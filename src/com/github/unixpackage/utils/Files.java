package com.github.unixpackage.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.JFileChooser;

import com.github.unixpackage.data.Constants;
import com.github.unixpackage.data.Variables;

import sun.net.www.protocol.file.FileURLConnection;

public class Files {

	public static String choosePath() {
		return choosePath(Variables.BUNDLE_MODE_ADVANCED_PATH);
	}

	public static String choosePath(String path) {
		String chosenPath = "";
		JFileChooser pathChooser;
		// Initialized with the current directory. Otherwise use home folder
		if (path == null) {
			pathChooser = new JFileChooser();
		} else {
			pathChooser = new JFileChooser(new File(path));
		}
		pathChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		int returnValue = pathChooser.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = pathChooser.getSelectedFile();
			chosenPath = selectedFile.getAbsolutePath();
		}
		return chosenPath;
	}

	public static boolean containsFolder(String location, final String folderName) {
	    /*
		File dir = new File(location);
	    File[] contentsDirectory = dir.listFiles(new FilenameFilter() {
	        @Override
	        public boolean accept(File dir, String name) {
	            return name.matches(folderName);
	        }
	    });
	    */
	    
		File locationPath = new File(location);
		boolean foundFolder = false;
		String[] contentsDirectory = locationPath.list();
		if (contentsDirectory != null) {
			for (String content : contentsDirectory) {
				File currentPath = new File(location + "/" + content);
				if (content.equals(folderName)) {
					foundFolder = true;
					break;
				} else {
					if (currentPath.isDirectory()) {
						return containsFolder(currentPath.getAbsolutePath(), folderName);
					}
				}
			}
		}
		return foundFolder;
	}
	
	public static boolean copyFile(final File toCopy, final File destFile) {
		try {
			return copyStream(new FileInputStream(toCopy),
					new FileOutputStream(destFile));
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static boolean copyFilesRecursively(final File toCopy, final File destDir) {
		assert destDir.isDirectory();

		if (!toCopy.isDirectory()) {
			return Files.copyFile(toCopy, new File(destDir, toCopy.getName()));
		} else {
			final File newDestDir = new File(destDir, toCopy.getName());
			if (!newDestDir.exists() && !newDestDir.mkdir()) {
				return false;
			}
			for (final File child : toCopy.listFiles()) {
				if (!copyFilesRecursively(child, newDestDir)) {
					return false;
				}
			}
		}
		return true;
	}

	public static void copyJarResourcesRecursively(File destination,
			JarURLConnection jarConnection) throws IOException {
		JarFile jarFile = jarConnection.getJarFile();
		ArrayList<JarEntry> jarEntries = Collections.list(jarFile.entries());
		for (JarEntry entry : jarEntries) {
			if (entry.getName().startsWith(jarConnection.getEntryName())) {
				String fileName = Files.removeStart(entry.getName(),
						jarConnection.getEntryName());
				if (!entry.isDirectory()) {
					InputStream entryInputStream = null;
					try {
						entryInputStream = jarFile.getInputStream(entry);
						Files.copyStream(entryInputStream, new File(destination,
								fileName));
					} finally {
						Files.safeClose(entryInputStream);
					}
				} else {
					Files.ensureDirectoryExists(new File(destination, fileName));
				}
			}
		}
	}

	public static boolean copyFolderIntoTempFolder(String source, String destination) {
		boolean result = false;

		Locations loc = new Locations();
		String validatedSource = loc.getAbsolutePath(source);
		File sourceDir = new File(validatedSource);

		File destDir = new File(destination);
		// if (destDir.exists()) {
		// return true;
		// } else {
		try {
			// Create first all intermediate directories as needed
			destDir.mkdirs();
			result = true;
		} catch (SecurityException se) {
			result = false;
		}
		// }

		try {
			Files.copyResourcesRecursively(new URL(sourceDir.toString()), destDir);
		} catch (IOException e1) {
			System.out.println("E: Cannot copy into folder!");
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean copyPackageSourcesIntoTempFolder() {
		Locations loc = new Locations();
		String validatedSource = loc
				.getAbsolutePath(Constants.ROOT_PACKAGE_FILES_PATH);
		String validatedDestination = Constants.ROOT_TMP_PACKAGE_FILES_PATH;
		if (validatedSource.startsWith("jar:")) {
			validatedDestination = Constants.TMP_PACKAGE_FILES_PATH;
		}
		return Files.copyFolderIntoTempFolder(Constants.ROOT_PACKAGE_FILES_PATH,
				validatedDestination);
	}

	public static boolean copyScriptSourcesIntoTempFolder() {
		Locations loc = new Locations();
		String validatedSource = loc
				.getAbsolutePath(Constants.ROOT_SCRIPT_FILES_PATH);
		String validatedDestination = Constants.ROOT_TMP_PACKAGE_FILES_PATH;
		if (validatedSource.startsWith("jar:")) {
			validatedDestination = Constants.TMP_SCRIPT_FILES_PATH;
		}

		return copyFolderIntoTempFolder(Constants.ROOT_SCRIPT_FILES_PATH,
				validatedDestination);
	}

	public static void copyResourcesRecursively(URL originUrl, File destination)
			throws Exception {
		URLConnection urlConnection = originUrl.openConnection();

		if (urlConnection instanceof JarURLConnection) {
			Files.copyJarResourcesRecursively(destination,
					(JarURLConnection) urlConnection);
		} else if (urlConnection instanceof FileURLConnection) {
			Files.copyFilesRecursively(new File(originUrl.getPath()), destination);
		} else {
			throw new Exception("URLConnection["
					+ urlConnection.getClass().getSimpleName()
					+ "] is not a recognized/implemented connection type.");
		}
	}

	private static boolean copyStream(final InputStream is, final File f) {
		try {
			return Files.copyStream(is, new FileOutputStream(f));
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static boolean copyStream(final InputStream is, final OutputStream os) {
		try {
			final byte[] buf = new byte[1024];

			int len = 0;
			while ((len = is.read(buf)) > 0) {
				os.write(buf, 0, len);
			}
			is.close();
			os.close();
			return true;
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static boolean ensureDirectoryExists(final File f) {
		return f.exists() || f.mkdir();
	}

	// StringUtils method
	public static boolean isEmpty(CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

	// StringUtils method
	public static String removeStart(String str, String remove) {
		if (isEmpty(str) || isEmpty(remove)) {
			return str;
		}
		if (str.startsWith(remove)) {
			return str.substring(remove.length());
		}
		return str;
	}

	private static void safeClose(InputStream is) {
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
