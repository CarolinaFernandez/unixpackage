package com.github.unixpackages.utils;

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

import org.apache.commons.io.FileUtils;

import com.github.unixpackages.data.Constants;

import sun.net.www.protocol.file.FileURLConnection;

public class Files {

	public boolean copyFile(final File toCopy, final File destFile) {
		try {
			return copyStream(new FileInputStream(toCopy),
					new FileOutputStream(destFile));
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;

		/*
		 * try { FileUtils.copyFile(toCopy, destFile); return true; } catch
		 * (IOException e1) { // TODO Auto-generated catch block
		 * e1.printStackTrace(); }
		 */
	}

	private boolean copyFilesRecursively(final File toCopy, final File destDir) {
		assert destDir.isDirectory();

		if (!toCopy.isDirectory()) {
			return copyFile(toCopy, new File(destDir, toCopy.getName()));
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

	public void copyJarResourcesRecursively(File destination,
			JarURLConnection jarConnection) throws IOException {
		JarFile jarFile = jarConnection.getJarFile();
		ArrayList<JarEntry> jarEntries = Collections.list(jarFile.entries());
		for (JarEntry entry : jarEntries) {
			if (entry.getName().startsWith(jarConnection.getEntryName())) {
				String fileName = removeStart(entry.getName(),
						jarConnection.getEntryName());
				if (!entry.isDirectory()) {
					InputStream entryInputStream = null;
					try {
						entryInputStream = jarFile.getInputStream(entry);
						copyStream(entryInputStream, new File(destination,
								fileName));
					} finally {
						safeClose(entryInputStream);
					}
				} else {
					ensureDirectoryExists(new File(destination, fileName));
				}
			}
		}
	}
	
	public boolean copyFolderIntoTempFolder(String localResource, String source,
			String destination) {

		boolean result = false;
		
		Locations loc = new Locations();
		String validatedSource = loc.getAbsolutePath(source);
		File sourceDir = new File(validatedSource);

		File destDir = new File(destination);
		if (destDir.exists()) {
			return true;
		} else {
			try {
				// Create first all intermediate directories as needed
				destDir.mkdirs();
				result = true;
			} catch (SecurityException se) {
				result = false;
			}
		}

		if (sourceDir.exists()) {
			// Normal copy: copy files from bundled examples to temporal folder
			try {
				FileUtils.copyDirectory(sourceDir, destDir);
			} catch (IOException e) {
				result = false;
				e.printStackTrace();
			}
		} else {
			// Internal copy: files within JAR to temp folder
			try {
				System.out.println("Source does not exist. Copying " + super.getClass().getResource("/" + localResource) + " to " + destDir);
				copyResourcesRecursively(
						super.getClass().getResource("/" + localResource), destDir);
			} catch (Exception e) {
				System.out.println("Could not copy packages recursively! "
						+ e);
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public boolean copyPackageSourcesIntoTempFolder() {
		return copyFolderIntoTempFolder("package", Constants.ROOT_PACKAGE_FILES_PATH, 
													Constants.TMP_PACKAGE_FILES_PATH);
	}
	
	public boolean copyScriptSourcesIntoTempFolder() {
		return copyFolderIntoTempFolder("script", Constants.ROOT_SCRIPT_FILES_PATH, 
													Constants.TMP_SCRIPT_FILES_PATH);		
	}

	public void copyResourcesRecursively(URL originUrl, File destination)
			throws Exception {
		URLConnection urlConnection = originUrl.openConnection();
		if (urlConnection instanceof JarURLConnection) {
			copyJarResourcesRecursively(destination,
					(JarURLConnection) urlConnection);
		} else if (urlConnection instanceof FileURLConnection) {
			copyFilesRecursively(new File(originUrl.getPath()), destination);
		} else {
			throw new Exception("URLConnection["
					+ urlConnection.getClass().getSimpleName()
					+ "] is not a recognized/implemented connection type.");
		}
	}

	private boolean copyStream(final InputStream is, final File f) {
		try {
			return copyStream(is, new FileOutputStream(f));
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	/* END StringUtils methods */

	private boolean copyStream(final InputStream is, final OutputStream os) {
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

	private boolean ensureDirectoryExists(final File f) {
		return f.exists() || f.mkdir();
	}

	// StringUtils method
	public boolean isEmpty(CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

	// StringUtils method
	public String removeStart(String str, String remove) {
		if (isEmpty(str) || isEmpty(remove)) {
			return str;
		}
		if (str.startsWith(remove)) {
			return str.substring(remove.length());
		}
		return str;
	}

	private void safeClose(InputStream is) {
		try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
